package com.itcast.core.service.order;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.itcast.core.dao.item.ItemDao;
import com.itcast.core.dao.log.PayLogDao;
import com.itcast.core.dao.order.OrderDao;
import com.itcast.core.dao.order.OrderItemDao;
import com.itcast.core.pojo.cart.Cart;
import com.itcast.core.pojo.item.Item;
import com.itcast.core.pojo.log.PayLog;
import com.itcast.core.pojo.order.Order;
import com.itcast.core.pojo.order.OrderItem;
import com.itcast.core.utils.uniquekey.IdWorker;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    @Resource
    private OrderDao orderDao;
    @Resource
    private IdWorker idWorker;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private ItemDao itemDao;
    @Resource
    private OrderItemDao orderItemDao;
    @Resource
    private PayLogDao payLogDao;

    @Override
    public void add(String username, Order order) {
        // 保存订单：根据商家进行分类
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("BUYER_CART").get(username);
        if (cartList != null && cartList.size() > 0) {
            //创建List保存订单id
            List<Long> orderIds=new ArrayList<>();
            //支付总金额
            double totalPrice=0f;

            for (Cart cart : cartList) {
                long orderId = idWorker.nextId();
                order.setOrderId(orderId);
                double payment = 0f;        // 订单的总金额（购买的该商家下的商品的总金额）
                order.setPaymentType("1");  // 支付方式：在线支付
                order.setStatus("1");       // 订单状态：待付款
                order.setCreateTime(new Date()); // 订单创建日期
                order.setUserId(username);  // 订单用户
                order.setSellerId(cart.getSellerId());  // 商家id
                // 保存订单明细(购物项)
                List<OrderItem> orderItemList = cart.getOrderItemList();
                if (orderItemList != null && orderItemList.size() > 0) {
                    for (OrderItem orderItem : orderItemList) {
                        Long id = idWorker.nextId();
                        orderItem.setId(id);
                        Item item = itemDao.selectByPrimaryKey(orderItem.getItemId());
                        orderItem.setGoodsId(item.getGoodsId());
                        orderItem.setOrderId(orderId);
                        orderItem.setTitle(item.getTitle());
                        orderItem.setPrice(item.getPrice());
                        Double totalFee = item.getPrice().doubleValue() * orderItem.getNum();
                        payment += totalFee;
                        orderItem.setTotalFee(new BigDecimal(totalFee));
                        orderItem.setPicPath(item.getImage());
                        orderItem.setSellerId(item.getSellerId());
                        orderItemDao.insertSelective(orderItem);
                    }
                }
                // 总金额 = 该商家的订单明细的价格
                order.setPayment(new BigDecimal(payment));
                orderDao.insertSelective(order);
                //全部商家的总金额
                totalPrice+=payment;
            }
            PayLog payLog=new PayLog();
            payLog.setOutTradeNo(String.valueOf(idWorker.nextId()));
            payLog.setCreateTime(new Date());
            payLog.setTotalFee((long) totalPrice);
            payLog.setUserId(username);
            payLog.setTradeState("0");
            payLog.setPayType("1");
            payLog.setOrderList(orderIds.toString().replace("[","").replace("]",""));
            payLogDao.insertSelective(payLog);
            redisTemplate.boundHashOps("payLog").put(username,payLog);
            redisTemplate.boundHashOps("BUYER_CART").delete(username);
        }
    }
}