package com.atguigu.gulimall.order.web;

import com.alipay.api.AlipayApiException;
import com.atguigu.gulimall.order.config.AlipayTemplate;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.service.OrderService;
import com.atguigu.gulimall.order.vo.PayVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;


/**
 *
 * @author: wan
 */
@Slf4j
@Controller
public class PayWebController {

    @Autowired
    private AlipayTemplate alipayTemplate;

    @Autowired
    private OrderService orderService;


    /**
     * 用户下单:支付宝支付
     * 1、让支付页让浏览器展示
     * 2、支付成功以后，跳转到用户的订单列表页
     * @param orderSn
     * @return
     * @throws AlipayApiException
     */
    @ResponseBody
    @GetMapping(value = "/payOrder",produces = "text/html")
    public String aliPayOrder(@RequestParam("orderSn") String orderSn) throws AlipayApiException {

        PayVo payVo = orderService.getOrderPay(orderSn);
        // 支付宝返回一个页面【支付宝账户登录的html页面】
        String pay = alipayTemplate.pay(payVo);
        System.out.println(pay);
        return pay;
    }


    //根据订单号查询订单状态的API
    @GetMapping(value = "/queryByOrderId")
    @ResponseBody
    public OrderEntity queryByOrderId(@RequestParam("orderId") String orderId) {
        log.info("查询支付记录...");
        return orderService.getOrderByOrderSn(orderId);
    }



}
