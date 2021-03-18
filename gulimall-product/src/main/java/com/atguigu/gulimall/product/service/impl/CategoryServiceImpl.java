package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import com.atguigu.gulimall.product.vo.Catelog2Vo;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;

import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        // 1. 查出所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);
        // 2. 组装成父子的树型结构

        // 2.1 找到所有的一级分类
        List<CategoryEntity> level1Menus = entities.stream().filter(categoryEntity ->
             categoryEntity.getParentCid() == 0
        ).map((menu) -> {
            menu.setChildren(getChildrens(menu, entities));
            return menu;
        }).sorted((menu1, menu2) -> {
            return (menu1.getSort()==null?0:menu1.getSort()) - (menu2.getSort()==null?0:menu2.getSort());
        }).collect(Collectors.toList());

        return level1Menus;
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        // TODO 检查当前删除的菜单，是否被别的地方引用

        // 逻辑删除 数据记录很宝贵，删除了就没了，所以设置一个标识位表示它被“删除”
        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(catelogId, paths);

        Collections.reverse(parentPath);

        return parentPath.toArray(new Long[parentPath.size()]);
    }

    // 级联更新所有关联的数据

    /**
     * @CacheEvict 失效模式
     * @param category
     */

    // 组合多个操作缓存 同时进行多种缓存操作
//    @Caching(evict = {
//            @CacheEvict(value = "category", key = "'getLevel1Categories'"),
//            @CacheEvict(value = "category", key = "'getCatelogJson'")
//    })
    // 清空这个区域里所有缓存
    @CacheEvict(value = "category", allEntries = true)
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
    }

    /**
     * 默认行为
     *  1 如果缓存中有 方法不用调用
     *  2 key默认自动生成 缓存的名字::SimpleKey [] （自主生成的key值）
     *  3 缓存的value值 默认使用JDK序列化机制 将序列化后的数据存到redis
     *  4 默认时间是-1 代表永不过期
     *
     * 自定义
     *  1 指定生成的缓存使用的key key属性指定 接受一个spEL
     *  2 指定缓存的数据的存活时间 在配置文件中修改ttl
     *  3 将数据保存为json格式
     *      CacheAutoConfiguration -> RedisCacheConfiguration -> 自动配置了缓存管理器 -> 初始化所有的缓存
     *      -> 每个缓存决定使用什么配置 -> 如果RedisCacheConfiguration有就用已有的 没有就用默认配置 -> 想改缓存的配置
     *      只需要给容器中放一个RedisCacheConfiguration即可 -> 就会应用到当前RedisCacheConfiguration管理的所有缓存分区中
     *  4 存储同一类型的数据，都可以指定成同一个分区 分区名默认就是缓存的前缀
     *
     * @CachePut 双写模式
     *
     * Spring-Cach 不足
     * 1 读模式
     *  缓存穿透 查询一个null数据 解决：缓存空数据 spring.cache.redis.cache-null-values=true
     *  缓存击穿 大量并发进来同时查询一个正好过期的数据 解决：加锁 默认无加锁
     *  缓存雪崩 大量key同时过期 解决：加随机时间 加上过期时间
     * 2 写模式 缓存与数据库一致
     *  1 读写加锁
     *  2 引入中间件 Canal 感知到MySQL的更新去更新数据库
     *  3 读多写多 直接去数据库查询就行
     *
     * 总结
     *  常规数据（读多写少 即时性 一致性要求不高的数据 完全可以使用Spring-cache） 只要有过期时间
     *  特殊数据 特殊设计
     *
     * @return
     */
    // 每一个需要缓存的数据都来指定要放到哪个命名空间的缓存【缓存的分区（按照业务的类型分）】
    @Cacheable(value = {"category"}, key = "#root.method.name", sync = true) // 代表当前方法结果需要缓存，如果缓存中有 方法不用调用 如果缓存中没有 会调用方法 最后将方法的结果放入缓存
    @Override
    public List<CategoryEntity> getLevel1Categories() {
        List<CategoryEntity> entities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        return entities;
    }

    @Cacheable(value = "category",key = "#root.methodName")
    @Override
    public Map<String, List<Catelog2Vo>> getCatelogJson() {
        System.out.println("查询了数据库");

        //将数据库的多次查询变为一次
        List<CategoryEntity> selectList = this.baseMapper.selectList(null);

        //1、查出所有分类
        //1、1）查出所有一级分类
        List<CategoryEntity> level1Categorys = getParent_cid(selectList, 0L);

        //封装数据
        Map<String, List<Catelog2Vo>> parentCid = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            //1、每一个的一级分类,查到这个一级分类的二级分类
            List<CategoryEntity> categoryEntities = getParent_cid(selectList, v.getCatId());

            //2、封装上面的结果
            List<Catelog2Vo> catelog2Vos = null;
            if (categoryEntities != null) {
                catelog2Vos = categoryEntities.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName().toString());

                    //1、找当前二级分类的三级分类封装成vo
                    List<CategoryEntity> level3Catelog = getParent_cid(selectList, l2.getCatId());

                    if (level3Catelog != null) {
                        List<Catelog2Vo.Catelog3Vo> category3Vos = level3Catelog.stream().map(l3 -> {
                            //2、封装成指定格式
                            Catelog2Vo.Catelog3Vo category3Vo = new Catelog2Vo.Catelog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());

                            return category3Vo;
                        }).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(category3Vos);
                    }

                    return catelog2Vo;
                }).collect(Collectors.toList());
            }

            return catelog2Vos;
        }));

        return parentCid;
    }

    // TODO 堆外内存溢出 OutOfDirectMemoryError
    // 1 springboot2.0以后默认使用lettuce作为操作redis的客户端，使用netty进行网络通信
    // 2 lettuce的bug导致netty堆外内存溢出 netty如果没有指定堆外内存 -Xmx300m
    // 可以通过-Dio.netty.maxDirectMemory进行设置
    // 解决方案 不能使用-Dio.netty.maxDirectMemory只去调大堆外内存
    // 升级lettuce客户端 、 切换使用jedis
    // redisTemplate:
    //      lettuce jedis 操作redis底层客户端 Spring再次封装redisTemplate
//    @Override
    public Map<String, List<Catelog2Vo>> getCatelogJsons() {
        // 给缓存中放json字符串，拿出的json字符串，还要逆转为能用的对象类型 【序列化和反序列化】

        /**
         * 1 空结果缓存：解决缓存穿透
         * 2 设置过期时间（随机） 解决缓存雪崩
         * 3 加锁 解决缓存击穿
         */

        // 1 加入缓存逻辑 缓存中存的数据是json字符串
        // JSON跨语言 跨平台兼容
        String catelogJSON = stringRedisTemplate.opsForValue().get("catelogJSON");
        if (StringUtils.isEmpty(catelogJSON)) {
            // 2 缓存中没有 就应该从数据库中查询出来
            System.out.println("缓存不命中····查询数据库");
            Map<String, List<Catelog2Vo>> catelogJsonFromDb = getCatelogJsonFromDbWithRedisLock();

            return catelogJsonFromDb;
        }
        System.out.println("缓存命中····直接返回····");
        // 转为指定的对象
        Map<String, List<Catelog2Vo>> result = JSON.parseObject(catelogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>(){});
        return result;
    }

    /**
     * 缓存里的数据如何和数据库保持一致
     * 缓存数据一致性
     * 双写模式
     * 失效模式
     * 系统一致性解决方案
     * 1 缓存的所有数据都有过期时间，数据过期下一次查询触发主动更新
     * 2 读写数据的时候，加上分布式的读写锁 读多写少的情况下
     * @return
     */
    // 分布式锁
    public Map<String, List<Catelog2Vo>> getCatelogJsonFromDbWithRedissonLock() {

        // 1 锁的名字 锁的粒度 越细越快
        // 锁的粒度，具体缓存的是某个数据
        RLock lock = redissonClient.getLock("catalogJson-lock");
        lock.lock();
        Map<String, List<Catelog2Vo>> dataFromDb;
        try {

            dataFromDb = getDataFromDb();
        } finally {
            lock.unlock();
        }

        return dataFromDb;

    }

    // 分布式锁
    public Map<String, List<Catelog2Vo>> getCatelogJsonFromDbWithRedisLock() {

        // 1 占分布式锁，去redis占坑
//        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", "111");
        String uuid = UUID.randomUUID().toString();
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS);
        if (lock) {
            System.out.println("获取分布式锁成功···");
            // 加锁成功··· 执行业务
            // 2 设置过期时间 必须和加锁是同步的 原子的
//            stringRedisTemplate.expire("lock", 30, TimeUnit.SECONDS);
            Map<String, List<Catelog2Vo>> dataFromDb;
            try {

                dataFromDb = getDataFromDb();
            } finally {
                String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
                // 删除锁
                Long lock1 = stringRedisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class)
                        , Arrays.asList("lock"), uuid);
            }
//            stringRedisTemplate.delete("lock"); // 删除锁
            // 获取值对比+对比成功删除=原子操作 lua脚本解锁
//            String lockValue = stringRedisTemplate.opsForValue().get("lock");
//            if (uuid.equals(lockValue)) {
//                // 删除我自己的锁
//                stringRedisTemplate.delete("lock"); // 删除锁
//            }


            return dataFromDb;
        } else {
            // 加锁失败··· 重试 自旋锁
            // 休眠
            System.out.println("获取分布式锁失败···等待重试");
            // 这里默认无限制重试 xx
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getCatelogJsonFromDbWithRedisLock();
        }

//        return getDataFromDb();

    }

    private Map<String, List<Catelog2Vo>> getDataFromDb() {
        String catelogJSON = stringRedisTemplate.opsForValue().get("catelogJSON");
        if (!StringUtils.isEmpty(catelogJSON)) {
            // 缓存不为null直接返回
            Map<String, List<Catelog2Vo>> result = JSON.parseObject(catelogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
            });
            return result;
        }

        System.out.println("查询了数据库······");

        List<CategoryEntity> selectList = baseMapper.selectList(null);

        // 1. 查出所有1级分类
        List<CategoryEntity> level1Categories = getParent_cid(selectList, 0L);

        // 2. 封装数据
        Map<String, List<Catelog2Vo>> parent_cid = level1Categories.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            // 1 每一个的1级分类 查到这个一级分类的二级分类
            List<CategoryEntity> categoryEntities = getParent_cid(selectList, v.getCatId());
            // 2 封装上面的结果
            List<Catelog2Vo> catelog2Vos = null;
            if (categoryEntities != null) {

                catelog2Vos = categoryEntities.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                    // 1 找当前二级分类的三级分类封装成vo
                    List<CategoryEntity> level3Catelog = getParent_cid(selectList, l2.getCatId());
                    if (level3Catelog != null) {
                        List<Catelog2Vo.Catelog3Vo> collect = level3Catelog.stream().map(l3 -> {
                            // 2 封装成指定格式
                            Catelog2Vo.Catelog3Vo catelog3Vo = new Catelog2Vo.Catelog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                            return catelog3Vo;
                        }).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(collect);

                    }
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }

            return catelog2Vos;
        }));

        // 3 查到的数据再放入缓存 将对象转为json放在缓存中
        String s = JSON.toJSONString(parent_cid);
        stringRedisTemplate.opsForValue().set("catelogJSON", s, 1, TimeUnit.DAYS);
        return parent_cid;
    }

    // 从数据库查询并封装分类数据
    public Map<String, List<Catelog2Vo>> getCatelogJsonFromDb() {

        /**
         * 加锁解决方案：
         * synchronized(this){}
         * 只要是同一把锁，就能锁住需要这个锁的所有线程
         * SpringBoot所有组件在容器中都是单例的
         * 然而 本地锁 只能锁住当前进程，所以需要分布式锁
         */

        /**
         * 1 将数据库多次查询变为一次
         */

        // TODO 本地锁 synchronized JUC(lock) 在分布式情况下，想要锁住所有，必须使用分布式锁
        synchronized (this) {

            // 得到锁以后，应该再去缓存中确定一次，如果没有才需要继续查询
            return getDataFromDb();
        }

    }

    private List<CategoryEntity> getParent_cid(List<CategoryEntity> selectList, Long parent_cid) {
        List<CategoryEntity> collect = selectList.stream().filter(item -> item.getParentCid() == parent_cid).collect(Collectors.toList());
//        return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", v.getCatId()));
        return collect;
    }

    private List<Long> findParentPath(Long catelogId, List<Long> paths) {
        // 1. 收集当前节点id
        paths.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        if (byId.getParentCid() != 0) {
            findParentPath(byId.getParentCid(), paths);
        }
        return paths;
    }

    // 递归查找所有菜单的子菜单
    private List<CategoryEntity> getChildrens(CategoryEntity root, List<CategoryEntity> all) {

        List<CategoryEntity> children = all.stream().filter(categoryEntity -> {
            return categoryEntity.getParentCid() == root.getCatId();
        }).map(categoryEntity -> {
            // 1. 找到子菜单
            categoryEntity.setChildren(getChildrens(categoryEntity, all));
            return categoryEntity;
        }).sorted((menu1, menu2) -> {
            // 2. 菜单的排序
            return (menu1.getSort()==null?0:menu1.getSort()) - (menu2.getSort()==null?0:menu2.getSort());
        }).collect(Collectors.toList());
        return children;
    }

}