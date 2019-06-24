package com.itcast.core.service.itemcat;

import com.itcast.core.pojo.item.ItemCat;

import java.util.List;

public interface ItemCatService {
    public List<ItemCat> findByParentId(Long parentId);
    public void add(ItemCat itemCat);
    public ItemCat findOne(Long id);
    public void update(ItemCat itemCat);
    public void delete(Long[] ids);

    /**
     * 查询分类所有
     * @return
     */
    public List<ItemCat> findAll();
}
