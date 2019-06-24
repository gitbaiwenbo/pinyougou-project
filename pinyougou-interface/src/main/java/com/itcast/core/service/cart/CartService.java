package com.itcast.core.service.cart;

import com.itcast.core.pojo.cart.Cart;
import com.itcast.core.pojo.item.Item;

import java.util.List;

public interface CartService {
    /**
     * 获取sku
     * @param id
     * @return
     */
    public Item findOne(Long id);

    /**
     * 填充购物车的数据
     * @param cartList
     * @return
     */
    public List<Cart> autoDataToCart(List<Cart> cartList);

    /**
     * 将本地购物车合并到redis中
     * @param cartList
     * @param username
     */
    public void mergeCartList(List<Cart> cartList,String username);

    public List<Cart> findCartListFromRedis(String username);
}
