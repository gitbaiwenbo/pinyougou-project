package com.itcast.core.service.brand;

import com.itcast.core.entity.PageResult;
import com.itcast.core.pojo.good.Brand;

import java.util.List;
import java.util.Map;

public interface BrandService {
    public List<Brand> findAll();
    public PageResult findPage(Integer pageNum,Integer pageSize);
    public PageResult search(Integer pageNum,Integer pageSize,Brand brand);
    public void save(Brand brand);

    public Brand findOne(Long id);
    public void update(Brand brand);
    public void delete(Long[] ids);
    public List<Map<String,String>> selectOptionList();
}
