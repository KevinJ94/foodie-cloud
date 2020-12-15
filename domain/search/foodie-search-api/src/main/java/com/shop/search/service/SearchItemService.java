package com.shop.search.service;

import com.shop.search.pojo.PagedGridResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("foodie-search-service")
@RequestMapping("search-item-api")
public interface SearchItemService {
        /**
     * 搜索商品列表
     * @param keywords
     * @param sort
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("ItemsKey")
    public PagedGridResult searchItems(@RequestParam("keywords") String keywords,
                                       @RequestParam("sort") String sort,
                                       @RequestParam("page") Integer page,
                                       @RequestParam("pageSize") Integer pageSize);

    /**
     * 根据分类id搜索商品列表
     * @param catId
     * @param sort
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("ItemsCat")
    public PagedGridResult searchItems(@RequestParam("catId") Integer catId,
                                       @RequestParam("sort") String sort,
                                       @RequestParam("page") Integer page,
                                       @RequestParam("pageSize") Integer pageSize);
}
