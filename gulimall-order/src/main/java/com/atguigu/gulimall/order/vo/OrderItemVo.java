package com.atguigu.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderItemVo {
    private Long skuId;
//    private Boolean check = true;
    private String title;
    private String image;
    private List<String> skuAttr;
    private BigDecimal price;
    private Integer count;
    private BigDecimal totalPrice;
    private BigDecimal weight = new BigDecimal("0.085");
    // TODO 查询库存状态
//    private boolean hasStock;
}
