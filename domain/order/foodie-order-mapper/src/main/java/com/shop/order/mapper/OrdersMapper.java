package com.shop.order.mapper;


import com.shop.my.mapper.MyMapper;
import com.shop.order.pojo.Orders;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdersMapper extends MyMapper<Orders> {
}