package com.itcast.core.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.itcast.core.entity.PageResult;
import com.itcast.core.entity.Result;
import com.itcast.core.pojo.template.TypeTemplate;
import com.itcast.core.service.template.TemplateService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/typeTemplate")
public class TypeTemplateController {
    @Reference
    private TemplateService templateService;
    @RequestMapping("/search.do")
    public PageResult search(Integer page, Integer rows, @RequestBody TypeTemplate typeTemplate){
        return templateService.search(page,rows,typeTemplate);
    }
    @RequestMapping("/add.do")
    public Result save(@RequestBody TypeTemplate typeTemplate){
        try {
            templateService.save(typeTemplate);
            return new Result(true,"保存成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"保存失败");
        }
    }
    @RequestMapping("/findOne.do")
    public TypeTemplate findOne(Long id){
        return templateService.findOne(id);
    }
    @RequestMapping("/update.do")
    public Result update(@RequestBody TypeTemplate typeTemplate){
        try {
            templateService.update(typeTemplate);
            return new Result(true,"修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"修改失败");
        }
    }
    @RequestMapping("/delete.do")
    public Result delete(Long[] ids){
        try {
            templateService.delete(ids);
            return new Result(true,"删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return  new Result(false,"删除失败");
        }
    }
    @RequestMapping("/findAll")
    public List<TypeTemplate> findAll(){
        return templateService.findAll();
    }
}
