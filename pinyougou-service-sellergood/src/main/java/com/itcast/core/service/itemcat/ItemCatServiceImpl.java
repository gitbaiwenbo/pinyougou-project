package com.itcast.core.service.itemcat;

import com.alibaba.dubbo.config.annotation.Service;
import com.itcast.core.dao.item.ItemCatDao;
import com.itcast.core.pojo.item.ItemCat;
import com.itcast.core.pojo.item.ItemCatQuery;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;

import javax.annotation.Resource;
import java.util.List;
@Service
public class ItemCatServiceImpl implements ItemCatService {
    @Resource
    private ItemCatDao itemCatDao;
    @Resource
    private RedisTemplate redisTemplate;
    @Override
    public List<ItemCat> findByParentId(Long parentId) {
        //查询商品分类是把它存入Redis中：通过模板ID
        List<ItemCat> itemCatList = itemCatDao.selectByExample(null);
        if (itemCatList!=null&&itemCatList.size()>0){
            for (ItemCat itemCat:itemCatList) {
                redisTemplate.boundHashOps("itemCat").put(itemCat.getName(),itemCat.getTypeId());
            }
        }
        ItemCatQuery query=new ItemCatQuery();
        query.createCriteria().andParentIdEqualTo(parentId);
        List<ItemCat> itemCats = itemCatDao.selectByExample(query);
        return itemCats;
    }

    @Override
    public void add(ItemCat itemCat) {
         itemCatDao.insertSelective(itemCat);
    }

    @Override
    public ItemCat findOne(Long id) {
        return itemCatDao.selectByPrimaryKey(id);
    }

    @Override
    public void update(ItemCat itemCat) {

    }

    @Override
    public void delete(Long[] ids) {

    }

    /**
     * 查询分类所有
     * @return
     */
    @Override
    public List<ItemCat> findAll() {
        return itemCatDao.selectByExample(null);
    }
}
