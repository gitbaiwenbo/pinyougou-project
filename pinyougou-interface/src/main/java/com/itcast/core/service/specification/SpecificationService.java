package com.itcast.core.service.specification;


import com.itcast.core.entity.PageResult;
import com.itcast.core.pojo.specification.Specification;
import com.itcast.core.vo.SpecificationVo;

import java.util.List;
import java.util.Map;

public interface SpecificationService {
    public PageResult search(Integer page, Integer rows, Specification specification);
    public void add(SpecificationVo specificationVo);
    public void delete(Long[] ids);
    public SpecificationVo findOne(Long id);
    public void update(SpecificationVo specificationVo);
    public List<Map<String,String>> selectOptionList();
}
