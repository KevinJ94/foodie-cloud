package com.shop.search.service;

import com.shop.search.pojo.Category;
import com.shop.search.pojo.vo.CategoryVO;
import com.shop.search.pojo.vo.NewItemsVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("foodie-search-service")
@RequestMapping("category-api")
public interface CategoryService {

    /**
     * 查询一级分类
     * @return
     */
    @GetMapping("allRootLevelCat")
    public List<Category> queryAllRootLevelCat();

    /**
     * 根据一级分类查询子分类
     * @param rootCatId
     * @return
     */
    @GetMapping("subCatList")
    public List<CategoryVO> getSubCatList(@RequestParam("rootCatId") Integer rootCatId);

    /**
     * 查询首页每个一级分类下的6条最新商品数据
     * @param rootCatId
     * @return
     */
    @GetMapping("sixNewItemsLazy")
    public List<NewItemsVO> getSixNewItemsLazy(@RequestParam("rootCatId") Integer rootCatId);

}
