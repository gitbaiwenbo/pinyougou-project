package com.itcast.core.controller.seller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.itcast.core.entity.Result;
import com.itcast.core.pojo.seller.Seller;
import com.itcast.core.service.seller.SellerService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seller")
public class SellerController {
    @Reference
    private SellerService sellerService;
    @RequestMapping("/add.do")
    public Result add(@RequestBody Seller seller){
        try {
            sellerService.add(seller);
            return new Result(true,"注册成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"注册失败");
        }
    }
    //回显商家信息回显
    @RequestMapping("/findSeller.do")
    public Seller findSeller(){
        String sellerId= SecurityContextHolder.getContext().getAuthentication().getName();
        return sellerService.findOne(sellerId);
    }
    //保存商家信息
    @RequestMapping("/insert.do")
    public Result insert(@RequestBody Seller seller){
        try {
            sellerService.insert(seller);
            return new Result(true,"保存成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(true,"保存失败");
        }
    }
}
