package com.itcast.core.service.specification;


import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.itcast.core.dao.good.BrandDao;
import com.itcast.core.dao.specification.SpecificationDao;
import com.itcast.core.dao.specification.SpecificationOptionDao;
import com.itcast.core.entity.PageResult;
import com.itcast.core.pojo.specification.Specification;
import com.itcast.core.pojo.specification.SpecificationOption;
import com.itcast.core.pojo.specification.SpecificationOptionQuery;
import com.itcast.core.pojo.specification.SpecificationQuery;
import com.itcast.core.vo.SpecificationVo;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
public class SpecificationServiceImpl implements SpecificationService {

    @Resource
    private SpecificationDao specificationDao;

    @Resource
    private SpecificationOptionDao specificationOptionDao;

    @Resource
    private BrandDao brandDao;

    /**
     * 规格列表查询
     * @param page
     * @param rows
     * @param specification
     * @return
     */
    @Override
    public PageResult search(Integer page, Integer rows, Specification specification) {
        // 1、设置分页条件
        PageHelper.startPage(page, rows);
        // 2、设置查询条件
        SpecificationQuery query = new SpecificationQuery();
        if(specification.getSpecName() != null && !"".equals(specification.getSpecName().trim())){
            query.createCriteria().andSpecNameLike("%" + specification.getSpecName().trim() + "%");
        }
        query.setOrderByClause("id desc");
        // 3、查询
        Page<Specification> p = (Page<Specification>) specificationDao.selectByExample(query);
        // 4、封装结果
        return new PageResult(p.getTotal(), p.getResult());
    }

    @Transactional
    @Override
    public void delete(Long[] ids) {
        if (ids!=null&&ids.length>0){
            for (Long id:ids){
                SpecificationQuery specificationQuery=new SpecificationQuery();
                SpecificationQuery.Criteria criteria = specificationQuery.createCriteria();
                criteria.andIdEqualTo(id);
                specificationDao.deleteByExample(specificationQuery);
                specificationDao.deleteByPrimaryKey(id);
            }
        }
    }

    /**
     * 保存规格
     * @param specificationVo
     */
    @Transactional
    @Override
    public void add(SpecificationVo specificationVo) {
        // 保存规格：插入数据后需要返回自增主键的id
        Specification specification = specificationVo.getSpecification();
        specificationDao.insertSelective(specification); // 返回自增主键id
        // 保存规格选项：外键spec_id
        List<SpecificationOption> specificationOptionList = specificationVo.getSpecificationOptionList();
        if(specificationOptionList != null && specificationOptionList.size() > 0){
            for (SpecificationOption specificationOption : specificationOptionList) {
                // 设置外键：specId
                specificationOption.setSpecId(specification.getId());
//                specificationOptionDao.insertSelective(specificationOption);
            }
            // TODO: 插入：报表数据导入到数据库中（一条条插入：内存溢出） 批量插入
            specificationOptionDao.insertSelectives(specificationOptionList);
        }
    }

    @Override
    public SpecificationVo findOne(Long id) {
        //查询主键回显规格返回自增ID
        Specification specification = specificationDao.selectByPrimaryKey(id);
        //创建查询对象，实现查询规格选项
        SpecificationOptionQuery specificationOptionQuery=new SpecificationOptionQuery();
        SpecificationOptionQuery.Criteria criteria = specificationOptionQuery.createCriteria();
        criteria.andSpecIdEqualTo(id);
        List<SpecificationOption> specificationOptions = specificationOptionDao.selectByExample(specificationOptionQuery);
        SpecificationVo specificationVo=new SpecificationVo();
        specificationVo.setSpecification(specification);
        specificationVo.setSpecificationOptionList(specificationOptions);
        return specificationVo;
    }

    @Override
    public List<Map<String, String>> selectOptionList() {
        List<Map<String, String>> maps = specificationDao.selectOptionList();
        return maps;

    }

    @Override
    public void update(SpecificationVo specificationVo) {
        //获得规格并且保存
        Specification specification = specificationVo.getSpecification();
        specificationDao.updateByPrimaryKeySelective(specification);
        //封装查询对象
        SpecificationOptionQuery specificationOptionQuery=new SpecificationOptionQuery();
        SpecificationOptionQuery.Criteria criteria = specificationOptionQuery.createCriteria();
        //封装具体的查询条件
        criteria.andSpecIdEqualTo(specification.getId());
        //删除回显的数据
        specificationOptionDao.deleteByExample(specificationOptionQuery);
        //获得规格选项
        List<SpecificationOption> specificationOptionList = specificationVo.getSpecificationOptionList();
        //判断以及插入规格选项
        if (specificationOptionList!=null&&specificationOptionList.size()>0){
            for (SpecificationOption specificationOption:specificationOptionList){
                specificationOption.setSpecId(specification.getId());
            }
            specificationOptionDao.insertSelectives(specificationOptionList);
        }

    }
}
