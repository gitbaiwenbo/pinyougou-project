package com.itcast.core.controller.order;

import com.alibaba.dubbo.config.annotation.Reference;
import com.itcast.core.entity.Result;
import com.itcast.core.pojo.order.Order;
import com.itcast.core.service.order.OrderService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order")
public class OrderController {
    @Reference
    private OrderService orderService;
    @RequestMapping("/add.do")
    public Result add(@RequestBody Order order){
        try {
            String username= SecurityContextHolder.getContext().getAuthentication().getName();
            orderService.add(username,order);
            return new Result(true,"下单成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(true,"下单失败");
        }
    }


}
