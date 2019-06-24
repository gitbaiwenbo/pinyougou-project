package com.itcast.core.controller.goods;

import com.alibaba.dubbo.config.annotation.Reference;
import com.itcast.core.entity.PageResult;
import com.itcast.core.entity.Result;
import com.itcast.core.pojo.good.Goods;
import com.itcast.core.pojo.item.ItemCat;
import com.itcast.core.service.goods.GoodsService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/goods")
public class GoodsController {
    @Reference
    private GoodsService goodsService;
    @RequestMapping("/search.do")
    public PageResult searchForManager(Integer page, Integer rows,@RequestBody Goods goods){
        return goodsService.searchForManager(page,rows,goods);
    }
    @RequestMapping("/updateStatus.do")
    public Result updateStatus(Long[] ids,String status){
        try {
            goodsService.updateStatus(ids,status);
            return new Result(true,"操作成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"操作失败");
        }
    }
    @RequestMapping("/delete.do")
    public Result delete(Long[] ids){
        try {
            goodsService.delete(ids);
            return new Result(true,"操作成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"操作失败");
        }
    }
}
