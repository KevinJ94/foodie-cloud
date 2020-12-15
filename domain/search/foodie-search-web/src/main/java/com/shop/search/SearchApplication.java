package com.shop.search;

import com.shop.item.service.ItemService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import tk.mybatis.spring.annotation.MapperScan;


@SpringBootApplication
// 扫描 mybatis 通用 mapper 所在的包
@MapperScan(basePackages = "com.shop.search.mapper")
// 扫描所有包以及相关组件包
@ComponentScan(basePackages = {"com.shop", "org.n3r.idworker"})
@EnableDiscoveryClient
@EnableFeignClients(
        clients = {
                ItemService.class,
        }
        )
public class SearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(SearchApplication.class, args);
    }

}
