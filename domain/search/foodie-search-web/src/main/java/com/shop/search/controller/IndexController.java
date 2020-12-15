package com.shop.search.controller;

import com.shop.enums.YesOrNo;

import com.shop.search.pojo.Category;


import com.shop.search.pojo.Carousel;
import com.shop.search.pojo.JSONResult;
import com.shop.search.pojo.vo.CategoryVO;
import com.shop.search.pojo.vo.NewItemsVO;
import com.shop.search.service.CarouselService;
import com.shop.search.service.CategoryService;
import com.shop.utils.JsonUtils;
import com.shop.utils.RedisOperator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Api(value = "首页",tags = {"首页展示的相关接口"})
@RestController
@RequestMapping("index")
public class IndexController {

    @Autowired
    private CarouselService carouselService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private RedisOperator redisOperator;

    @ApiOperation(value = "获取首页轮播图",notes = "获取首页轮播图",httpMethod = "GET")
    @GetMapping("/carousel")
    public JSONResult carousel(){
        List<Carousel> list = new ArrayList<>();
        String carousel = redisOperator.get("carousel");
        if(StringUtils.isBlank(carousel)){
           list  =  carouselService.queryAll(YesOrNo.YES.type);
            redisOperator.set("carousel", JsonUtils.objectToJson(list));
        }else {
            list = JsonUtils.jsonToList(carousel,Carousel.class);
        }

        return JSONResult.ok(list);
    }
    /**
     * 1.查询大分类
     * 2.大分类,则加载子分类,如果存在则不需要加载
     */
    @ApiOperation(value = "获取商品分类(一级分类)",notes = "获取商品分类(一级分类)",httpMethod = "GET")
    @GetMapping("/cats")
    public JSONResult cats(){
        List<Category> list =  categoryService.queryAllRootLevelCat();
        return JSONResult.ok(list);
    }

    @ApiOperation(value = "获取商品子分类",notes = "获取商品子分类",httpMethod = "GET")
    @GetMapping("/subCat/{rootCatId}")
    public JSONResult subCat(
            @ApiParam(name = "rootCatId", value = "一级分类id", required = true)
            @PathVariable Integer rootCatId){
        if(rootCatId == null){
            return JSONResult.errorMsg("分类不存在");
        }
        List<CategoryVO> list = new ArrayList<>();
        String catsStr = redisOperator.get("subCat:" + rootCatId);
        if(StringUtils.isBlank(catsStr)){
            list = categoryService.getSubCatList(rootCatId);
            if(list != null){
                redisOperator.set("subCat:" + rootCatId, JsonUtils.objectToJson(list));
            }else{
                redisOperator.set("subCat:" + rootCatId, JsonUtils.objectToJson(list),60);
            }

        }else {
            list = JsonUtils.jsonToList(catsStr, CategoryVO.class);
        }

        return JSONResult.ok(list);
    }

    @ApiOperation(value = "查询每个一级分类下的6条数据",notes = "查询每个一级分类下的6条数据",httpMethod = "GET")
    @GetMapping("/sixNewItems/{rootCatId}")
    public JSONResult sixNewItems(
            @ApiParam(name = "rootCatId", value = "一级分类id", required = true)
            @PathVariable Integer rootCatId){
        if(rootCatId == null){
            return JSONResult.errorMsg("分类不存在");
        }

        List<NewItemsVO> list = categoryService.getSixNewItemsLazy(rootCatId);
        return JSONResult.ok(list);
    }

}
