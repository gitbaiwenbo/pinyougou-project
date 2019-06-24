package com.itcast.core.service.seller;

import com.itcast.core.entity.PageResult;
import com.itcast.core.entity.Result;
import com.itcast.core.pojo.seller.Seller;

public interface SellerService {
    public void add(Seller seller);
    public PageResult search(Integer page,Integer rows,Seller seller);
    public Seller findOne(String sellerId);
    public void updateStatus(String sellerId,String status);

    /**
     * 保存商家信息
     * @param seller
     */
    void insert(Seller seller);
}
