package com.shop.order.pojo.vo;



import com.shop.search.pojo.ShopcartBO;

import java.util.List;

public class OrderVO {

    private String orderId;
    private com.shop.order.pojo.vo.MerchantOrdersVO merchantOrdersVO;
    private List<ShopcartBO> toBeRemovedShopCartList;
    public List<ShopcartBO> getToBeRemovedShopCartList() {
        return toBeRemovedShopCartList;
    }

    public void setToBeRemovedShopCartList(List<ShopcartBO> toBeRemovedShopCartList) {
        this.toBeRemovedShopCartList = toBeRemovedShopCartList;
    }



    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public com.shop.order.pojo.vo.MerchantOrdersVO getMerchantOrdersVO() {
        return merchantOrdersVO;
    }

    public void setMerchantOrdersVO(com.shop.order.pojo.vo.MerchantOrdersVO merchantOrdersVO) {
        this.merchantOrdersVO = merchantOrdersVO;
    }
}