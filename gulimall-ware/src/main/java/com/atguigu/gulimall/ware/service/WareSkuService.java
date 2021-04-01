package com.atguigu.gulimall.ware.service;

import com.atguigu.common.mq.StockLockedTo;
import com.atguigu.common.to.OrderTo;
import com.atguigu.gulimall.ware.vo.LockStockResultVo;
import com.atguigu.gulimall.ware.vo.SkuHasStockVo;
import com.atguigu.gulimall.ware.vo.WareSkuLockVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.ware.entity.WareSkuEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author lnj
 * @email tongjianwhu@gmail.com
 * @date 2021-02-27 14:17:58
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds);

    Boolean orderLockStock(WareSkuLockVo vo);

    /**
     * 解锁库存
     */
    void unlockStock(StockLockedTo to);

    /**
     * 解锁订单
     */
    void unlockStock(OrderTo orderTo);
}

