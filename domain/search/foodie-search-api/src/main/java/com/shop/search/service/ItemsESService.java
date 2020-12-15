package com.shop.search.service;


import com.shop.search.pojo.PagedGridResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient("foodie-search-service")
@RequestMapping("item-es-api")
public interface ItemsESService {

    @GetMapping("searchItems")
    public PagedGridResult searchItems(@RequestParam("keywords")String keywords,
                                       @RequestParam("sort") String sort,
                                       @RequestParam("page") Integer page,
                                       @RequestParam("pageSize") Integer pageSize);

}
