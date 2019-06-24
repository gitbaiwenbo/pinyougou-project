package com.itcast.core.controller.seller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.itcast.core.entity.PageResult;
import com.itcast.core.entity.Result;
import com.itcast.core.pojo.seller.Seller;
import com.itcast.core.service.seller.SellerService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seller")
public class SellerController {
    @Reference
    private SellerService sellerService;
    @RequestMapping("/search")
    public PageResult search(Integer page, Integer rows, @RequestBody Seller seller){
        return sellerService.search(page,rows,seller);
    }
    @RequestMapping("/findOne.do")
    public Seller findOne(String id){
        return sellerService.findOne(id);
    }
    @RequestMapping("/updateStatus.do")
    public Result updateStatus(String sellerId, String status){
        try {
            sellerService.updateStatus(sellerId,status);
            return new Result(true,"审核成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"审核失败");
        }
    }
}
