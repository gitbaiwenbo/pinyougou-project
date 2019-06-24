package com.itcast.core.service.order;

import com.itcast.core.pojo.order.Order;

public interface OrderService {
    public void add(String username, Order order);
}
