package com.itcast.core.service.addr;

import com.alibaba.dubbo.config.annotation.Service;
import com.itcast.core.dao.address.AddressDao;
import com.itcast.core.pojo.address.Address;
import com.itcast.core.pojo.address.AddressQuery;

import javax.annotation.Resource;
import java.util.List;
@Service
public class AddrServiceImpl implements AddrService {
    @Resource
    private AddressDao addressDao;
    @Override
    public List<Address> findListByLoginUser(String userId) {
        AddressQuery query=new AddressQuery();
        query.createCriteria().andUserIdEqualTo(userId);
       return addressDao.selectByExample(query);

    }
}
