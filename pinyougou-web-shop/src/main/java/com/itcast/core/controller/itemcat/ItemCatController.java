package com.itcast.core.controller.itemcat;

import com.alibaba.dubbo.config.annotation.Reference;
import com.itcast.core.pojo.item.Item;
import com.itcast.core.pojo.item.ItemCat;
import com.itcast.core.service.itemcat.ItemCatService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/itemCat")
public class ItemCatController {
    @Reference
    private ItemCatService itemCatService;
    @RequestMapping("/findByParentId.do")
    public List<ItemCat> findByParentId(Long parentId){
        return itemCatService.findByParentId(parentId);
    }

    /**
     * 通过商品加载模板id
     * @param id
     * @return
     */
    @RequestMapping("/findOne.do")
    private ItemCat findOne(Long id){
        return itemCatService.findOne(id);
    }

    /**
     * 查询分类列表所有
     * @return
     */
    @RequestMapping("/findAll.do")
    public List<ItemCat> findAll(){
        return itemCatService.findAll();
    }
}
