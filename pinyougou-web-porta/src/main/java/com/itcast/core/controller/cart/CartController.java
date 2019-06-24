package com.itcast.core.controller.cart;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.itcast.core.entity.Result;
import com.itcast.core.pojo.cart.Cart;
import com.itcast.core.pojo.item.Item;
import com.itcast.core.pojo.order.OrderItem;
import com.itcast.core.service.cart.CartService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {
    @Reference
    private CartService cartService;
    @RequestMapping("/addGoodsToCartList.do")
    @CrossOrigin(origins ={"http://localhost:8887"})
    public Result addGoodsToCartList(Long itemId, Integer num,
                   HttpServletRequest request, HttpServletResponse response){
        try {
            String name= SecurityContextHolder.getContext().getAuthentication().getName();
            //1、定义一个空的存放购物车的集合对象
            boolean flag=false;
        List<Cart> cartList=null;
        //2、判断本地是否有购物车，有，取出来赋值给定义的空车集合
        Cookie[] cookies=request.getCookies();
        if (cookies!=null&&cookies.length>0){
            for (Cookie cookie:cookies){
                if ("BUYER_CART".equals(cookie.getName())){
                    String decode = URLDecoder.decode(cookie.getValue());
                    cartList=JSON.parseArray(decode,Cart.class);
                    flag=true;
                    break;
                }
            }
        }
        //没有：第一次，创建一个新车
        if (cartList==null){
            cartList=new ArrayList<>();
        }
        //有车了
        //填充数据
        Cart cart=new Cart();
        Item item = cartService.findOne(itemId);
        cart.setSellerId(item.getSellerId());
        List<OrderItem> orderItemList=new ArrayList<>();
        OrderItem orderItem=new OrderItem();
        orderItem.setItemId(itemId);
        orderItem.setNum(num);
        orderItemList.add(orderItem);
        cart.setOrderItemList(orderItemList);
       // 4、有车了，将商品装车：
        //4-1、判断该商品是否属于同一个商家
        int sellerIndexOf = cartList.indexOf(cart);
        //4-1-1：属于：
        if (sellerIndexOf!=-1){
            // 属于同一个商家：继续判断是否属于同款商品（库存id）
            // 取出之前购物项的数据
            List<OrderItem> oldOrderItemList= cartList.get(sellerIndexOf).getOrderItemList();
            // 判断本次的购物项在之前是否存在
            int itemIndexOf = oldOrderItemList.indexOf(orderItem);
            if (itemIndexOf!=-1){
                // 同款商品，合并数量
                OrderItem oldOrderItem = oldOrderItemList.get(itemIndexOf);
                oldOrderItem.setNum(oldOrderItem.getNum()+num);
            }else {
                oldOrderItemList.add(orderItem);
            }
        }else {
            // 不属于同一个商家：直接装车
            cartList.add(cart);
        }
        if (!"anonymousUser".equals(name)){
            //已登录
            //将车子保存到Redis中
            cartService.mergeCartList(cartList,name);
            if (flag){
                Cookie cookie=new Cookie("BUYER_CART",null);
                cookie.setMaxAge(0);
                cookie.setPath("/");
                response.addCookie(cookie);
            }
        }
        // 6、将车子保存到cookie中
        Cookie cookie=new Cookie("BUYER_CART",  URLEncoder.encode(JSON.toJSONString(cartList),"UTF-8"));
        cookie.setMaxAge(60*60);
        cookie.setPath("/");
        response.addCookie(cookie);

            return new Result(true,"加入购物车成功");
        }catch (Exception e){
            e.printStackTrace();
            return new Result(true,"加入购物车失败");
        }
    }
    @RequestMapping("/findCartList.do")
    public List<Cart> findCartList(HttpServletRequest request,HttpServletResponse response) throws Exception {
        List<Cart> cartList=null;
        Cookie[] cookies = request.getCookies();
        if (cookies!=null&&cookies.length>0){
            for(Cookie cookie:cookies){
                if ("BUYER_CART".equals(cookie.getName())){
                    ;
                    cartList=JSON.parseArray(URLDecoder.decode(cookie.getValue(),"UTF-8"),Cart.class);
                    break;
                }
            }
        }
        String name=SecurityContextHolder.getContext().getAuthentication().getName();
        if (!"anonymousUser".equals(name)) {
            // 场景：未登录将商品加入购物车
            // 如果用登录成功跳转到该页面--->【我的购物车】--->将本地的购物车同步到redis中
            if (cartList!=null){
                cartService.mergeCartList(cartList,name);
                Cookie cookie=new Cookie("BUYER_CART",null);
                cookie.setMaxAge(0);
                cookie.setPath("/");
                response.addCookie(cookie);
            }
            cartService.findCartListFromRedis(name);
        }
        if (cartList!=null){
            cartList= cartService.autoDataToCart(cartList);
        }
        return cartList;
    }
}
