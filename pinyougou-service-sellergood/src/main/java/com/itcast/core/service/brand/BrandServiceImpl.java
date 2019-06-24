package com.itcast.core.service.brand;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.itcast.core.dao.good.BrandDao;
import com.itcast.core.entity.PageResult;
import com.itcast.core.pojo.good.Brand;
import com.itcast.core.pojo.good.BrandQuery;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;


@Service
public class BrandServiceImpl implements BrandService {
    @Resource
    private BrandDao brandDao;
    @Override
    public List<Brand> findAll() {
        List<Brand> brands = brandDao.selectByExample(null);
        return brands;
    }

    @Override
    public PageResult findPage(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        Page<Brand> page = (Page<Brand>) brandDao.selectByExample(null);
        return new PageResult(page.getTotal(),page.getResult());
    }


    @Override
    public void update(Brand brand) {
        brandDao.updateByPrimaryKeySelective(brand);
    }

    @Override
    public Brand findOne(Long id) {
        return brandDao.selectByPrimaryKey(id);
    }

    @Override
    public void delete(Long[] ids) {
        if (ids!=null&&ids.length>0){
//            for (Long id:ids){
//                brandDao.deleteByPrimaryKey(id);
//            }
            brandDao.deleteByPrimaryKeys(ids);
        }
    }

    @Override
    public List<Map<String, String>> selectOptionList() {
        return brandDao.selectOptionList();
    }

    @Override
    public PageResult search(Integer pageNum, Integer pageSize, Brand brand) {
        //工具分页
        PageHelper.startPage(pageNum,pageSize);
        //设置查询条件，封装查询条件对象XXXquery
        BrandQuery brandQuery=new BrandQuery();
        BrandQuery.Criteria criteria = brandQuery.createCriteria();//封装具体的查询条件对象
        //拼接SQL语句
        if (brand.getName()!=null&&!"".equals(brand.getName().trim())){
            criteria.andNameLike("%"+brand.getName().trim()+"%");
        }
        if (brand.getFirstChar()!=null&&!"".equals(brand.getFirstChar().trim())){
            criteria.andFirstCharLike(brand.getFirstChar().trim());
        }
        //倒序
        brandQuery.setOrderByClause("id desc");
        //查询
        Page<Brand> page = (Page<Brand>) brandDao.selectByExample(brandQuery);
        //讲结果封装到PageResult中
        PageResult pageResult=new PageResult(page.getTotal(),page.getResult());
        return pageResult;
    }

    @Override
    public void save(Brand brand) {
        brandDao.insertSelective(brand);
    }
}
