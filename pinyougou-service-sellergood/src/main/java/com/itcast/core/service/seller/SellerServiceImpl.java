package com.itcast.core.service.seller;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.itcast.core.dao.seller.SellerDao;
import com.itcast.core.entity.PageResult;
import com.itcast.core.pojo.seller.Seller;
import com.itcast.core.pojo.seller.SellerQuery;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.annotation.Resource;
import java.util.Date;

@Service
public class SellerServiceImpl implements SellerService{
    @Resource
    private SellerDao sellerDao;
    @Override
    public void add(Seller seller) {
        seller.setStatus("0");
        seller.setCreateTime(new Date());
        BCryptPasswordEncoder bCryptPasswordEncoder=new BCryptPasswordEncoder();
        String password = bCryptPasswordEncoder.encode(seller.getPassword());
        seller.setPassword(password);
        sellerDao.insertSelective(seller);
    }

    @Override
    public void updateStatus(String sellerId,String status) {
        Seller seller=new Seller();
        seller.setSellerId(sellerId);
        seller.setStatus(status);
        sellerDao.updateByPrimaryKeySelective(seller);
    }

    @Override
    public Seller findOne(String sellerId) {
        Seller seller = sellerDao.selectByPrimaryKey(sellerId);
        return seller;
    }

    @Override
    public PageResult search(Integer page, Integer rows, Seller seller) {
        PageHelper.startPage(page,rows);
        String status = seller.getStatus();
        SellerQuery sellerQuery=new SellerQuery();
        if (status!=null&&"".equals(status.trim())){
            sellerQuery.createCriteria().andStatusEqualTo(status);
        }
        Page<Seller> p= (Page<Seller>) sellerDao.selectByExample(sellerQuery);

        return new PageResult(p.getTotal(),p.getResult());
    }

    @Override
    public void insert(Seller seller) {
        sellerDao.updateByPrimaryKeySelective(seller);
    }
}
