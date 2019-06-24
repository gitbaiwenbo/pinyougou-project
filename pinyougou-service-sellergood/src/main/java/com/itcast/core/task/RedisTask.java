package com.itcast.core.task;

import com.alibaba.fastjson.JSON;
import com.itcast.core.dao.item.ItemCatDao;
import com.itcast.core.dao.item.ItemDao;
import com.itcast.core.dao.specification.SpecificationOptionDao;
import com.itcast.core.dao.template.TypeTemplateDao;
import com.itcast.core.pojo.item.Item;
import com.itcast.core.pojo.item.ItemCat;
import com.itcast.core.pojo.specification.SpecificationOption;
import com.itcast.core.pojo.specification.SpecificationOptionQuery;
import com.itcast.core.pojo.template.TypeTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Component
public class RedisTask {
    @Resource
    private ItemCatDao itemCatDao;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private TypeTemplateDao typeTemplateDao;
    @Resource
    private SpecificationOptionDao specificationOptionDao;
    //将分类的数据写到缓存中
    @Scheduled(cron = "30 50 12 27 03 *")
    public void autoItemToRedis(){
        List<ItemCat> itemCatList = itemCatDao.selectByExample(null);
        if (itemCatList!=null&&itemCatList.size()>0){
            for (ItemCat itemCat:itemCatList){
                redisTemplate.boundHashOps("itemCatList").put(itemCat.getName(),itemCat.getTypeId());
            }
        }
    }
    //将模板写到缓存中
    @Scheduled(cron = "30 50 12 27 03 *")
    public void autoTypeToRedis(){
        List<TypeTemplate> typeTemplateList = typeTemplateDao.selectByExample(null);
        if (typeTemplateList!=null&&typeTemplateList.size()>0){
            for (TypeTemplate typeTemplate:typeTemplateList){
                String brandIds = typeTemplate.getBrandIds();
                List<Map> brandList= JSON.parseArray(brandIds,Map.class);
                redisTemplate.boundHashOps("brandList").put(typeTemplate.getId(),brandList);
                List<Map> specList=findBySpecList(typeTemplate.getId());
            }
        }
    }

    private List<Map> findBySpecList(Long id) {
        TypeTemplate typeTemplate = typeTemplateDao.selectByPrimaryKey(id);
        String specIds = typeTemplate.getSpecIds();
        List<Map> specList=JSON.parseArray(specIds,Map.class);
        if (specList!=null&&specList.size()>0){
            for (Map map:specList){
                Long specId= Long.valueOf(map.get("id").toString());
                SpecificationOptionQuery query=new SpecificationOptionQuery();
                query.createCriteria().andSpecIdEqualTo(specId);
                List<SpecificationOption> options = specificationOptionDao.selectByExample(query);
                map.put("options",options);
            }
        }
        return specList;
    }
}
