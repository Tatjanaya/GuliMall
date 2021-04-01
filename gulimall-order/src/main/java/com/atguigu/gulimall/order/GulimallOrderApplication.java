package com.atguigu.gulimall.order;

import com.alibaba.cloud.seata.GlobalTransactionAutoConfiguration;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * 使用RabbitMQ
 * 1 引入amqp场景 RabbitAutoConfiguration就会自动生效
 * 2 给容器中自动配置了 RabbitTemplate  AmqpAdmin CachingConnectionFactory RabbitMessagingTemplate
 *  所有属性都是 spring.rabbitmq
 * 3 @EnableRabbit
 * 4 监听消息 使用@RabbitListener 必须有 @EnableRabbit
 *
 * @RabbitListener 类+方法上 监听哪些队列
 * @RabbitHandler 方法上 重载区分不同的消息
 *
 * Seata控制分布式事务
 * 1 每一个微服务必须创建undo_log
 * 2 安装事务协调器
 * 3 整合 导入依赖 spring-cloud-starter-alibaba-seata seata-all-0.7.1
 *      解压并启动 seata-server
 *      registry.conf 注册中心配置 修改registry type=nacos
 *      所有想要弄到分布式事务的微服务使用seata DataSourceProxy代理自己的数据源
 * 4 每个微服务 都必须导入 registry.conf file.conf
 * vgroup_mapping.gulimall-ware-fescar-service-group = "default"
 * 5 启动测试分布式事务
 * 6 给分布式大事务的入口标注@Global Transactional
 * 7 每一个远程的小事务用 @Transactional
 */
@EnableFeignClients
@EnableRedisHttpSession
@EnableRabbit
@EnableDiscoveryClient
@SpringBootApplication(exclude = GlobalTransactionAutoConfiguration.class)
public class GulimallOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallOrderApplication.class, args);
    }

}
