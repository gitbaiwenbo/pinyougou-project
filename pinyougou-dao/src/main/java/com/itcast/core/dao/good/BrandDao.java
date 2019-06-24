package com.itcast.core.dao.good;

import com.itcast.core.pojo.good.Brand;
import com.itcast.core.pojo.good.BrandQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface BrandDao {
    int countByExample(BrandQuery example);

    int deleteByExample(BrandQuery example);

    int deleteByPrimaryKey(Long id);

    int insert(Brand record);

    int insertSelective(Brand record);

    List<Brand> selectByExample(BrandQuery example);

    Brand selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") Brand record, @Param("example") BrandQuery example);

    int updateByExample(@Param("record") Brand record, @Param("example") BrandQuery example);
    void deleteByPrimaryKeys(Long[] ids);

    int updateByPrimaryKeySelective(Brand record);

    int updateByPrimaryKey(Brand record);
    public List<Map<String,String>> selectOptionList();
}