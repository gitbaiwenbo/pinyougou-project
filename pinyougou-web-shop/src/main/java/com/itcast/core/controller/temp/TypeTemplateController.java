package com.itcast.core.controller.temp;

import com.alibaba.dubbo.config.annotation.Reference;
import com.itcast.core.pojo.template.TypeTemplate;
import com.itcast.core.service.template.TemplateService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/typeTemplate")
public class TypeTemplateController {
    @Reference
    private TemplateService templateService;

    @RequestMapping("/findOne.do")
    public TypeTemplate findOne(Long id){
        return templateService.findOne(id);
    }
    @RequestMapping("/findBySpecList")
    public List<Map> findBySpecList(Long id){
        return templateService.findBySpecList(id);
    }
}
