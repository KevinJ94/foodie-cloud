package com.shop.order;

import com.shop.item.service.ItemService;
import com.shop.order.fallback.itemservice.ItemCommentsFeignClient;
import com.shop.user.service.AddressService;
import com.shop.user.service.UserService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import tk.mybatis.spring.annotation.MapperScan;


@SpringBootApplication
// 扫描 mybatis 通用 mapper 所在的包
@MapperScan(basePackages = "com.shop.order.mapper")
// 扫描所有包以及相关组件包
@ComponentScan(basePackages = {"com.shop", "org.n3r.idworker"})
@EnableDiscoveryClient
@EnableFeignClients(
        clients = {
                ItemCommentsFeignClient.class,
                ItemService.class,
                UserService.class,
                AddressService.class
        }
//        basePackages = {
//        "com.shop.user.service",
//        "com.shop.item.service",
//        "com.shop.order.fallback.itemservice"}
        )
@EnableScheduling
public class OrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }

}
