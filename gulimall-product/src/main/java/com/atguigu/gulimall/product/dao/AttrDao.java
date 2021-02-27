package com.atguigu.gulimall.product.dao;

import com.atguigu.gulimall.product.entity.AttrEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品属性
 * 
 * @author lnj
 * @email tongjianwhu@pku.edu.cn
 * @date 2021-02-27 12:42:03
 */
@Mapper
public interface AttrDao extends BaseMapper<AttrEntity> {
	
}
