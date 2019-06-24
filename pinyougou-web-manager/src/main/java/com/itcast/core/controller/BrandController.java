package com.itcast.core.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.itcast.core.entity.PageResult;
import com.itcast.core.entity.Result;
import com.itcast.core.pojo.good.Brand;
import com.itcast.core.service.brand.BrandService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/brand")
public class BrandController {
    @Reference
    private BrandService brandService;
    @RequestMapping("/findAll.do")
    public List<Brand> findAll(){
        return brandService.findAll();
    }
    @RequestMapping("/findPage.do")
    public PageResult findPage(Integer pageNum,Integer pageSize){
        return brandService.findPage(pageNum, pageSize);
    }
    @RequestMapping("/search.do")
    public PageResult search(Integer pageNum,Integer pageSize,@RequestBody Brand brand){
        return brandService.search(pageNum,pageSize,brand);
    }
    @RequestMapping("/add.do")
    public Result save(@RequestBody Brand brand){
        try {
            brandService.save(brand);
            return new Result(true,"保存成功");
        } catch (Exception e) {
            e.printStackTrace();
                    return new Result(false,"保存失败");
        }
    }
    @RequestMapping("/findOne.do")
    public Brand findOne(Long id){
        return brandService.findOne(id);
    }
    @RequestMapping("/update.do")
    public Result update(@RequestBody Brand brand){
        try {
            brandService.update(brand);
            return new Result(true,"修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"修改失败");
        }
    }
    @RequestMapping("/delete.do")
    public Result delete(Long[] ids){
        try {
            brandService.delete(ids);
            return new Result(true,"删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"删除失败");
        }
    }
    @RequestMapping("/selectOptionList.do")
    public List<Map<String,String>> selectOptionList(){
        return brandService.selectOptionList();
    }
}
