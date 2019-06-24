package com.itcast.core.service.template;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.itcast.core.dao.specification.SpecificationOptionDao;
import com.itcast.core.dao.template.TypeTemplateDao;
import com.itcast.core.entity.PageResult;
import com.itcast.core.pojo.specification.SpecificationOption;
import com.itcast.core.pojo.specification.SpecificationOptionQuery;
import com.itcast.core.pojo.template.TypeTemplate;
import com.itcast.core.pojo.template.TypeTemplateQuery;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Transactional
@Service
public class TemplateServiceImpl implements TemplateService {
    @Resource
    private TypeTemplateDao typeTemplateDao;
    @Resource
    private SpecificationOptionDao specificationOptionDao;
    @Resource
    private RedisTemplate redisTemplate;
    @Override
    public PageResult search(Integer page, Integer rows, TypeTemplate typeTemplate) {
        List<TypeTemplate> templateList = typeTemplateDao.selectByExample(null);
        if (templateList!=null&&templateList.size()>0){
            for (TypeTemplate template:templateList){
                List<Map> brandList=JSON.parseArray(template.getBrandIds(),Map.class);
                redisTemplate.boundHashOps("brandList").put(template.getId(),brandList);
                List<Map> specList = findBySpecList(template.getId());
                redisTemplate.boundHashOps("specList").put(template.getId(),specList);
            }
        }
        //开始分页
        PageHelper.startPage(page,rows);
        //封装查询对象
        TypeTemplateQuery typeTemplateQuery=new TypeTemplateQuery();
        TypeTemplateQuery.Criteria criteria = typeTemplateQuery.createCriteria();//封装具体查询对象
        if (typeTemplate.getName()!=null&&!"".equals(typeTemplate.getName().trim())){
            criteria.andNameLike("%"+typeTemplate.getName().trim());
        }
        typeTemplateQuery.setOrderByClause("id desc");
        Page<TypeTemplate> p = (Page<TypeTemplate>) typeTemplateDao.selectByExample(typeTemplateQuery);
        PageResult pageResult = new PageResult(p.getTotal(), p.getResult());
        return pageResult;
    }

    @Override
    public void save(TypeTemplate typeTemplate) {
         typeTemplateDao.insertSelective(typeTemplate);
    }

    @Override
    public List<Map> findBySpecList(Long id) {
        TypeTemplate typeTemplate = typeTemplateDao.selectByPrimaryKey(id);
        String specIds = typeTemplate.getSpecIds();
        List<Map> specList = JSON.parseArray(specIds, Map.class);
        if (specList!=null&&specList.size()>0){
            for (Map map:specList){
                Long specId = Long.parseLong(map.get("id").toString());
                SpecificationOptionQuery specificationOptionQuery=new SpecificationOptionQuery();
                specificationOptionQuery.createCriteria().andSpecIdEqualTo(specId);
                List<SpecificationOption> options = specificationOptionDao.selectByExample(specificationOptionQuery);
                map.put("options",options);
            }
        }
        return specList;
    }

    @Override
    public TypeTemplate findOne(Long id) {
        return typeTemplateDao.selectByPrimaryKey(id);
    }

    @Override
    public void update(TypeTemplate typeTemplate) {
        typeTemplateDao.updateByPrimaryKeySelective(typeTemplate);
    }

    @Override
    public void delete(Long[] ids) {

        if (ids!=null&&ids.length>0){
//            for (Long id:ids){
//                typeTemplateDao.deleteByPrimaryKey(id);
//            }
            typeTemplateDao.deleteByPrimaryKeys(ids);
        }
    }

    @Override
    public List<TypeTemplate> findAll() {
        return typeTemplateDao.selectByExample(null);
    }
}
