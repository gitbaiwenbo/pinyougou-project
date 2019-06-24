package com.itcast.core.service.addr;

import com.itcast.core.pojo.address.Address;

import java.util.List;

public interface AddrService {
    /**
     * 获取当前登录人的收货地址列表
     * @param name
     * @return
     */
    public List<Address> findListByLoginUser(String name);
}
