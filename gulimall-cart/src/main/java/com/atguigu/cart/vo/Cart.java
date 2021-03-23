package com.atguigu.cart.vo;

import com.alibaba.nacos.client.naming.utils.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * 整个购物车
 * 需要计算属性 必须重写get方法 保证每次获取属性都会进行计算
 */
public class Cart {

    List<CartItem> items;

    private Integer countNum; // 商品数量
    private Integer countType; // 商品类型数量

    private BigDecimal totalAmount; // 商品总价
    private BigDecimal reduce = new BigDecimal("0.00"); // 减免价格

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public Integer getCountNum() {
        int count = 0;
        if (items != null && items.size() > 0) {
            for (CartItem item : items) {
                count += item.getCount();
            }
        }
        return count;
    }

    public Integer getCountType() {
        int count = 0;
        if (items != null && items.size() > 0) {
            for (CartItem item : items) {
                count += 1;
            }
        }
        return count;
    }

    public BigDecimal getTotalAmount() {
        BigDecimal amount = new BigDecimal("0");
        // 计算购物项总价
        if (!CollectionUtils.isEmpty(items)) {
            for (CartItem cartItem : items) {
                if (cartItem.getCheck()) {
                    amount = amount.add(cartItem.getTotalPrice());
                }
            }
        }
        // 计算优惠后的价格
        return amount.subtract(getReduce());
    }

    public BigDecimal getReduce() {
        return reduce;
    }

    public void setReduce(BigDecimal reduce) {
        this.reduce = reduce;
    }
}
