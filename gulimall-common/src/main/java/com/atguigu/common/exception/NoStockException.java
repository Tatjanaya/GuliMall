package com.atguigu.common.exception;

import lombok.Getter;
import lombok.Setter;

public class NoStockException extends RuntimeException {
    @Getter
    @Setter
    private Long skuId;
    public NoStockException(Long skuId) {
        super("商品id：" + skuId + "没有足够的库存了");
    }

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }

    public NoStockException(String msg) {
        super(msg);
    }
}
