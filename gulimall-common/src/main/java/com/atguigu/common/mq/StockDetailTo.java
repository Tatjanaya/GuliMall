package com.atguigu.common.mq;

import lombok.Data;

/**
 * 库存单详情
 * wms_ware_order_task_detail
 */
@Data
public class StockDetailTo {

    private Long id;
    /**
     * sku_id
     */
    private Long skuId;
    /**
     * sku_name
     */
    private String skuName;
    /**
     * 购买个数
     */
    private Integer skuNum;
    /**
     * 工作单id
     */
    private Long taskId;

    /**
     * 仓库id
     */
    private Long wareId;

    /**
     * 锁定状态
     * 1-锁定 2-解锁 3-扣减
     */
    private Integer lockStatus;

}
