package com.atguigu.gulimall.product.web;

import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.Catelog2Vo;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Controller
public class IndexController {

    @Autowired
    CategoryService categoryService;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @GetMapping({"/", "/index.html"})
    public String indexPage(Model model) {

        // TODO 1 查出所有1级分类
        List<CategoryEntity> categoryEntityList = categoryService.getLevel1Categories();

        // 默认前缀 classpath:template/
        // 默认后缀 .html
        model.addAttribute("categorys", categoryEntityList);
        return "index";
    }

//    index/catalog.json
    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<String, List<Catelog2Vo>> getCatelogJson() {
        Map<String, List<Catelog2Vo>> catelogJson = categoryService.getCatelogJson();
        return catelogJson;
    }

    @ResponseBody
    @GetMapping("/hello")
    public String hello() {
        // 1 获取一把锁 只要锁的名字一样，就是同一把锁
        RLock lock = redissonClient.getLock("my-lock");

        // 2 加锁
        lock.lock(10, TimeUnit.SECONDS); // 10s自动解锁 自动解锁时间一定要大于业务的执行时间
        // lock.lock(10, TimeUnit.SECONDS); 锁时间到后，不会自动续期
        // 如果传递了锁的超时时间，就发送给redis执行脚本进行占锁，默认超时就是我们指定的时间
        // 未指定时间，就使用30 * 1000 LockWatchdogTimeout看门狗默认时间
        // 只要占锁成功，就会启动一个定时任务【重新给锁设置过期时间，新的过期时间就是看门狗的默认时间】
        // 三分之一看门狗时间，10s一续期 自动续期到满时间 30s - 20s - 30s
        // 最佳实战 lock.lock(10, TimeUnit.SECONDS); 省掉了整个续期操作 手动解锁

        // 1 锁的自动续期 如果业务超长 运行期间自动给锁续上新的30s，不用担心业务时间长，锁自动过期被删掉
        // 2 加锁的业务只要运行完成，就不会给当前锁续期，即使不手动解锁，锁默认会在30s后自动删除
        try {

            System.out.println("加锁成功，执行业务···" + Thread.currentThread().getId());
            Thread.sleep(30000);
        } catch (Exception e) {

        }
        finally {
            // 3 解锁
            System.out.println("释放锁···" + Thread.currentThread().getId());
            lock.unlock();
        }
        return "hello";
    }

    // 读写锁 保证一定能读到最新数据 修改期间 写锁是一个排他锁（互斥锁 独享锁） 读锁是一个共享锁
    // 写锁没释放读锁必须等待
    // 读 + 读 相当于无锁 并发读会同时加锁成功 只会在redis中记录好当前的读锁
    // 写 + 读 等待写锁释放
    // 写 + 写 阻塞
    // 读 + 写 有读锁 写也需要等待
    // 只要有写的存在 都必须等待
    @GetMapping("/write")
    public String writeValue() {
        RReadWriteLock lock = redissonClient.getReadWriteLock("rw-lock");
        String s = "";
        RLock rLock = lock.writeLock();
        try {
            // 1 改数据 加写锁 读数据 加读锁
            rLock.lock();
            s = UUID.randomUUID().toString();
            Thread.sleep(30000);
            stringRedisTemplate.opsForValue().set("writeValue", s);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            rLock.unlock();
        }
        return s;
    }

    @GetMapping("/read")
    @ResponseBody
    public String readValue() {
        RReadWriteLock lock = redissonClient.getReadWriteLock("rw-lock");
        String s = "";
        // 加读锁
        RLock rLock = lock.readLock();
        rLock.lock();
        try {
            s = stringRedisTemplate.opsForValue().get("writeValue");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            rLock.unlock();
        }
        return s;
    }

    /**
     * 车库停车
     * 3车位
     * 信号量也可以用作分布式限流
     */
    @GetMapping("/park")
    @ResponseBody
    public String park() throws InterruptedException {
        RSemaphore park = redissonClient.getSemaphore("park");
//        park.acquire(); // 获取一个信号 占一个车位
        boolean b = park.tryAcquire();
        return "ok=>" + b;
    }

    @GetMapping("/go")
    @ResponseBody
    public String go() throws InterruptedException {
        RSemaphore park = redissonClient.getSemaphore("park");
        park.release(); // 释放一个车位
        return "ok";
    }

    /**
     * 放假 锁门
     * 5个班全部走完，可以锁大门
     */
    @GetMapping("/lockDoor")
    @ResponseBody
    public String lockDoor() throws InterruptedException {

        RCountDownLatch door = redissonClient.getCountDownLatch("door");
        door.trySetCount(5);
        door.await(); // 等待闭锁都完成

        return "放假了···";
    }

    @GetMapping("/gogogo/{id}")
    @ResponseBody
    public String gogogo(@PathVariable("id") Long id) {

        RCountDownLatch door = redissonClient.getCountDownLatch("door");
        door.countDown(); // 计数减一
        return id + "班的人都走了";
    }
}
