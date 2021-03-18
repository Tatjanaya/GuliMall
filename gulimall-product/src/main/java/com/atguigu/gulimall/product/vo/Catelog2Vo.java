package com.atguigu.gulimall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Catelog2Vo {
    private String catalog1Id; // 1级父分类
    private List<Catelog2Vo.Catelog3Vo> catalog3List; // 3级子分类

    private String id;
    private String name;


    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class Catelog3Vo {
        private String catalog2Id; // 父分类，2级分类id
        private String id;
        private String name;
    }
}
