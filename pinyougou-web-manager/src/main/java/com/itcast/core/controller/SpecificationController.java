package com.itcast.core.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.itcast.core.entity.PageResult;
import com.itcast.core.entity.Result;
import com.itcast.core.pojo.specification.Specification;
import com.itcast.core.service.brand.BrandService;
import com.itcast.core.service.specification.SpecificationService;
import com.itcast.core.vo.SpecificationVo;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/specification") //specification
public class SpecificationController {
    @Reference
    private SpecificationService specificationService;
    @Reference
    private BrandService brandService;
    @RequestMapping("/search.do")
    public PageResult search(Integer page, Integer rows, @RequestBody Specification specification){
        return specificationService.search(page,rows,specification);
    }
    @RequestMapping("/add.do")
    public Result add(@RequestBody SpecificationVo specificationVo){
        try {
            specificationService.add(specificationVo);
            return new Result(true,"保存成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"保存失败");
        }
    }
    @RequestMapping("/delete.do")
    public Result delete(Long[] ids){
        try {
            specificationService.delete(ids);
            return new Result(true,"删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return  new Result(false,"删除失败");

        }
    }
    @RequestMapping("/findOne.do")
    public SpecificationVo findOne(Long id){
        return specificationService.findOne(id);
    }
    @RequestMapping("/update.do")
    public Result update(@RequestBody SpecificationVo specificationVo){
        try {
            specificationService.update(specificationVo);
            return new Result(true,"修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"修改失败");
        }
    }
    @RequestMapping("/selectOptionList.do")
    public List<Map<String,String>> selectOptionList(){
        List<Map<String, String>> maps = specificationService.selectOptionList();
        return maps ;
    }
}
