package com.shop.search.service;

import com.shop.search.pojo.Carousel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("foodie-search-service")
@RequestMapping("carousel-api")
public interface CarouselService {
    /**
     * 查询所有轮播图
     * @param isShow
     * @return
     */
    @GetMapping("queryAll")
    public List<Carousel> queryAll(@RequestParam("isShow") Integer isShow);

}
