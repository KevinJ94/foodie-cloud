package com.shop.order.controller;

import com.shop.controller.BaseController;
import com.shop.enums.OrderStatusEnum;
import com.shop.enums.PayMethod;

import com.shop.order.pojo.OrderStatus;
import com.shop.order.pojo.bo.PlaceOrderBO;
import com.shop.order.pojo.bo.SubmitOrderBO;
import com.shop.order.pojo.vo.MerchantOrdersVO;
import com.shop.order.pojo.vo.OrderVO;
import com.shop.order.service.OrderService;
import com.shop.search.pojo.JSONResult;
import com.shop.search.pojo.ShopcartBO;
import com.shop.utils.CookieUtils;
import com.shop.utils.JsonUtils;
import com.shop.utils.RedisOperator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Api(
    value = "订单相关",
    tags = {"订单相关的api接口"})
@RequestMapping("orders")
@RestController
public class OrdersController extends BaseController {
  static final Logger logger = LoggerFactory.getLogger(OrdersController.class);

  @Autowired private OrderService orderService;

  @Autowired private RestTemplate restTemplate;

  @Autowired private RedisOperator redisOperator;

  @Autowired private RedissonClient redisson;

  //    @Autowired
  //    private RabbitSender rabbitSender;
  @ApiOperation(value = "获取订单token", notes = "获取订单token", httpMethod = "POST")
  @PostMapping("/getOrderToken")
  public JSONResult getOrderToken(HttpSession session) {
    //生成订单页token, 一个页面唯一, 防止重复提交实现接口幂等性!
    String token = UUID.randomUUID().toString();
    redisOperator.set("ORDER_TOKEN_" + session.getId(), token, 600);
    return JSONResult.ok(token);
  }

  @ApiOperation(value = "用户下单", notes = "用户下单", httpMethod = "POST")
  @PostMapping("/create")
  public JSONResult create(
      @RequestBody SubmitOrderBO submitOrderBO,
      HttpServletRequest request,
      HttpServletResponse response) {

    //将订单token存入redis, 保证一个页面只能提交一次
    //使用分布式锁防止并发下重复下单!
    String orderTokenKey = "ORDER_TOKEN_" + request.getSession().getId();
    String lockKey = "LOCK_KEY_" + request.getSession().getId();
    RLock rLock = redisson.getLock(lockKey);
    rLock.lock(5, TimeUnit.SECONDS);

    try {
      String orderToken = redisOperator.get(orderTokenKey);
      if (StringUtils.isBlank(orderToken)) throw new RuntimeException("orderToken不存在");
      boolean correctToken = orderToken.equals(submitOrderBO.getToken());
      if (!correctToken) throw new RuntimeException("orderToken不正确");
      redisOperator.del(orderTokenKey);
    } finally {
      rLock.unlock();
    }

    if (submitOrderBO.getPayMethod() != PayMethod.WEIXIN.type
        && submitOrderBO.getPayMethod() != PayMethod.ALIPAY.type) {
      return JSONResult.errorMsg("支付方式不支持！");
    }

    String shopcartJson = redisOperator.get(FOODIE_SHOPCART + ":" + submitOrderBO.getUserId());
    List<ShopcartBO> shopcartList = null;
    if (StringUtils.isBlank(shopcartJson)) {
      return JSONResult.errorMsg("购物车数据不正确");
    }
    shopcartList = JsonUtils.jsonToList(shopcartJson, ShopcartBO.class);

    // 1. 创建订单
    PlaceOrderBO orderBO = new PlaceOrderBO(submitOrderBO, shopcartList);
    OrderVO orderVO = orderService.createOrder(orderBO);
    String orderId = orderVO.getOrderId();

    // 2. 创建订单以后，移除购物车中已结算（已提交）的商品
    /** 1001 2002 -> 用户购买 3003 -> 用户购买 4004 */
    // 整合redis之后，完善购物车中的已结算商品清除，并且同步到前端的cookie
    shopcartList.removeAll(orderVO.getToBeRemovedShopCartList());
    redisOperator.set(
        FOODIE_SHOPCART + ":" + submitOrderBO.getUserId(), JsonUtils.objectToJson(shopcartList));
    CookieUtils.setCookie(
        request, response, FOODIE_SHOPCART, JsonUtils.objectToJson(shopcartList), true);

    // 3. 向支付中心发送当前订单，用于保存支付中心的订单数据
    MerchantOrdersVO merchantOrdersVO = orderVO.getMerchantOrdersVO();
    merchantOrdersVO.setReturnUrl(payReturnUrl);

    // 为了方便测试购买，所以所有的支付金额都统一改为1分钱
    merchantOrdersVO.setAmount(1);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.add("imoocUserId", "5121685-544392727");
    headers.add("password", "tf43-09tk-j045-99ky");

    HttpEntity<MerchantOrdersVO> entity = new HttpEntity<>(merchantOrdersVO, headers);

    ResponseEntity<JSONResult> responseEntity =
        restTemplate.postForEntity(paymentUrl, entity, JSONResult.class);
    JSONResult paymentResult = responseEntity.getBody();
    if (paymentResult.getStatus() != 200) {
      logger.error("发送错误：{}", paymentResult.getMsg());
      return JSONResult.errorMsg("支付中心订单创建失败，请联系管理员！");
    }

    // 发送订单到消息队列服务器
    // rabbitSender.send(orderVO);

    return JSONResult.ok(orderId);
  }

  @PostMapping("notifyMerchantOrderPaid")
  public Integer notifyMerchantOrderPaid(String merchantOrderId) {
    orderService.updateOrderStatus(merchantOrderId, OrderStatusEnum.WAIT_DELIVER.type);
    return HttpStatus.OK.value();
  }

  @PostMapping("getPaidOrderInfo")
  public JSONResult getPaidOrderInfo(String orderId) {

    OrderStatus orderStatus = orderService.queryOrderStatusInfo(orderId);
    return JSONResult.ok(orderStatus);
  }
}
