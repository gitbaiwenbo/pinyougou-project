package com.itcast.core.controller.goods;

import com.itcast.core.entity.PageResult;
import com.itcast.core.entity.Result;
import com.itcast.core.pojo.good.Goods;
import com.itcast.core.pojo.item.Item;
import com.itcast.core.service.goods.GoodsService;
import com.itcast.core.vo.GoodsVo;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Reference
    private GoodsService goodsService;

    /**
     * 保存商品
     * @param goodsVo
     * @return
     */
    @RequestMapping("/add.do")
    public Result add(@RequestBody GoodsVo goodsVo){
        try {
            String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
            goodsVo.getGoods().setSellerId(sellerId);   // 设置商家的id
            goodsService.add(goodsVo);
            return new Result(true, "保存成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "保存失败");
        }
    }

    /**
     * 查询列所有
     * @param page
     * @param rows
     * @param goods
     * @return
     */
    @RequestMapping("/search.do")
    public PageResult search(Integer page, Integer rows,@RequestBody Goods goods){
        String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
        goods.setSellerId(sellerId);
        return goodsService.search(page,rows,goods);
    }

    /**
     * 回显商品
     * @param id
     * @return
     */
    @RequestMapping("/findOne.do")
    public GoodsVo findOne(Long id){
        return goodsService.findOne(id);
    }
    @RequestMapping("/update.do")
    public Result update(@RequestBody GoodsVo goodsVo){
        try {
            goodsService.update(goodsVo);
            return new Result(true,"修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"修改失败");
        }
    }
}
