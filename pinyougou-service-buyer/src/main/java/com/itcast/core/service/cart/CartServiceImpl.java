package com.itcast.core.service.cart;

import com.alibaba.dubbo.config.annotation.Service;
import com.itcast.core.dao.item.ItemDao;
import com.itcast.core.dao.seller.SellerDao;
import com.itcast.core.pojo.cart.Cart;
import com.itcast.core.pojo.item.Item;
import com.itcast.core.pojo.order.OrderItem;
import com.itcast.core.pojo.seller.Seller;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
@Service
public class CartServiceImpl implements CartService {
    @Resource
    private ItemDao itemDao;
    @Resource
    private SellerDao sellerDao;
    @Resource
    private RedisTemplate redisTemplate;

    /**
     * 获取sku
     * @param id
     * @return
     */
    @Override
    public Item findOne(Long id) {
        return itemDao.selectByPrimaryKey(id);
    }

    @Override
    public List<Cart> autoDataToCart(List<Cart> cartList) {
        for (Cart cart:cartList){
            Seller seller = sellerDao.selectByPrimaryKey(cart.getSellerId());
            cart.setSellerName(seller.getNickName());
            // 填充购物项的数据
            List<OrderItem> orderItemList = cart.getOrderItemList();
            if (orderItemList!=null&&orderItemList.size()>0){
                for (OrderItem orderItem:orderItemList){
                    Item item = itemDao.selectByPrimaryKey(orderItem.getItemId());
                    orderItem.setPicPath(item.getImage());  // 填充商品图片
                    orderItem.setTitle(item.getTitle());    // 填充商品标题
                    orderItem.setPrice(item.getPrice());    // 填充商品单价
                    BigDecimal totalFee=new BigDecimal(item.getPrice().doubleValue()*orderItem.getNum());
                    orderItem.setTotalFee(totalFee);
                }
            }
        }
        return cartList;
    }

    @Override
    public void mergeCartList(List<Cart> newCartList, String username) {

        // 从redis中取出老车
        List<Cart> oldCartList= (List<Cart>) redisTemplate.boundHashOps("BUYER_CART").get(username);
        // 将新车合并到老车中
        oldCartList=newCartListMergeOldCartList(newCartList,oldCartList);
        // 将老车保存到redis中
        redisTemplate.boundHashOps("BUYER_CART").put(username,oldCartList);
    }

    private List<Cart> newCartListMergeOldCartList(List<Cart> newCartList, List<Cart> oldCartList) {
        if (newCartList!=null){
            if (oldCartList!=null){
                for (Cart newCart:newCartList){
                    int sellerIndexOf = oldCartList.indexOf(newCart);
                    if (sellerIndexOf!=-1){
                        List<OrderItem> newOrderItemList = newCart.getOrderItemList();//新车购物项
                        List<OrderItem> oldOrderItemList = oldCartList.get(sellerIndexOf).getOrderItemList();//老车购物项
                        //判断是否同一商品
                        for (OrderItem newOrderItem:newOrderItemList){
                            int itemIndexOf = oldOrderItemList.indexOf(newOrderItem);
                            if (itemIndexOf!=-1){
                                //是同一商品,合并数量
                                OrderItem oldOrderItem = oldOrderItemList.get(itemIndexOf);//老车购物项
                                oldOrderItem.setNum(oldOrderItem.getNum()+newOrderItem.getNum());
                            }else {
                                //不是同一商品，直接加到商品的购物项中
                                oldOrderItemList.add(newOrderItem);
                            }
                        }

                    }else {
                        //不是同一商家，直接装车
                        oldCartList.add(newCart);
                    }
                }
            }else {
                //，老车为空，返回新车
                return newCartList;
            }
        }else {
            //新车为空，直接返回老车
            return oldCartList;
        }
        return oldCartList;
    }

    @Override
    public List<Cart> findCartListFromRedis(String username) {
        List<Cart> cartList  = (List<Cart>) redisTemplate.boundHashOps("BUYER_CART").get(username);
        return cartList;
    }
}
