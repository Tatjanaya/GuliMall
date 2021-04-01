package com.atguigu.common.mq;

import lombok.Data;

import java.util.List;

@Data
public class StockLockedTo {

    private Long id; // 库存工作单id
    private StockDetailTo detailTo; // 工作详情的所有id
}
