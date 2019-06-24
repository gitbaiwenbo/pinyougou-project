package com.itcast.core.service.template;

import com.itcast.core.entity.PageResult;
import com.itcast.core.pojo.template.TypeTemplate;

import java.util.List;
import java.util.Map;

public interface TemplateService {
    public PageResult search(Integer page, Integer rows, TypeTemplate typeTemplate);
    public void save(TypeTemplate typeTemplate);
    public TypeTemplate findOne(Long id);
    public void update(TypeTemplate typeTemplate);
    public void delete(Long[] ids);
    public List<TypeTemplate> findAll();
    public List<Map> findBySpecList(Long id);
}
