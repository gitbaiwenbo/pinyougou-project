package com.itcast.core.service.goods;

import com.itcast.core.entity.PageResult;
import com.itcast.core.pojo.good.Goods;
import com.itcast.core.vo.GoodsVo;

public interface GoodsService {
    /**
     * 商品录入
     * @param goodsVo
     */
    public void add(GoodsVo goodsVo);

    /**
     * 商品列表查询
     * @param page
     * @param rows
     * @param goods
     * @return
     */
    public PageResult search(Integer page, Integer rows, Goods goods);

    /**
     * 回显商品
     * @param id
     * @return
     */
    public GoodsVo findOne(Long id);

    /**
     * 修改
     * @param goodsVo
     */
    public void update(GoodsVo goodsVo);

    /**
     * 运行商商品列表查询
     * @param page
     * @param rows
     * @param goods
     * @return
     */
    public PageResult searchForManager(Integer page,Integer rows,Goods goods);

    /**
     * 商品审核
     * @param ids
     * @param status
     */
    public void updateStatus(Long[] ids,String status);

    /**
     * 删除商品
     * @param ids
     */
    public void delete(Long[] ids);
}
