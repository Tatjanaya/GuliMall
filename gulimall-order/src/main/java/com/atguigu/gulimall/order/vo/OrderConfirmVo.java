package com.atguigu.gulimall.order.vo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

// 订单确认页需要用到的数据
//@Data
@ToString
public class OrderConfirmVo {

    // 收货地址 ums_member_receive_address 表
    @Getter @Setter
    List<MemberAddressVo> address;

    // 所有选中的购物项
    @Getter @Setter
    List<OrderItemVo> items;

    // 发票记录...

    // 优惠券信息
    @Getter @Setter
    Integer integration;

    @Getter @Setter
    Map<Long, Boolean> stocks;

    // 防重令牌
    @Getter @Setter
    String orderToken;

    public Integer getCount() {
        Integer i = 0;
        if (items != null) {
            for (OrderItemVo item : items) {
                i += item.getCount();
            }
        }
        return i;
    }

//    BigDecimal total; // 订单总额

    public BigDecimal getTotal() {
        BigDecimal totalNum = BigDecimal.ZERO;
        if (items != null && items.size() > 0) {
            for (OrderItemVo item : items) {
                //计算当前商品的总价格
                BigDecimal itemPrice = item.getPrice().multiply(new BigDecimal(item.getCount().toString()));
                //再计算全部商品的总价格
                totalNum = totalNum.add(itemPrice);
            }
        }
        return totalNum;
    }

//    BigDecimal payPrice; // 应付价格
    public BigDecimal getPayPrice() {
        return getTotal();
    }
}
