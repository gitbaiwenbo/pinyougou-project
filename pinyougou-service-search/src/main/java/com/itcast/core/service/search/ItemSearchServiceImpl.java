package com.itcast.core.service.search;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.itcast.core.dao.item.ItemDao;
import com.itcast.core.pojo.item.Item;
import com.itcast.core.pojo.item.ItemQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import javax.annotation.Resource;
import java.util.*;

@Service
public class ItemSearchServiceImpl implements ItemSearchService {
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private SolrTemplate solrTemplate;
    @Resource
    private ItemDao itemDao;
    @Override
    public Map<String, Object> search(Map<String, String> searchMap) {
        //创建一个大的map，来封装所有的结果集
        Map<String,Object> resultMap=new HashMap<>();
        //处理关键字中包含空格的问题
        String keywords = searchMap.get("item_keywords");
        if (keywords!=null&&!"".equals(keywords)){
            keywords = keywords.replace(" ", "");
            searchMap.put("keywords",keywords);
        }
        //没有高亮的检索与分页方法的调用
        Map<String, Object> map = searchForHighLightPage(searchMap);
        //Map<String, Object> map = searchForPage(searchMap);
        resultMap.putAll(map);
        //商品分类列表
        List<String> categoryList = searchForGroupPage(searchMap);
        if (categoryList!=null&&categoryList.size()>0){
            resultMap.put("categoryList",categoryList);
            Map<String, Object> brandAndSpecMap = searchBrandAndSpecListByCategory(categoryList.get(0));
            resultMap.putAll(brandAndSpecMap);
        }
        return resultMap;
    }

    /**
     * 没有高亮的检索与分页方法
     * @param searchMap
     * @return
     */
    private Map<String,Object> searchForPage(Map<String,String> searchMap){
        //设置检索条件
        Criteria criteria=new Criteria("item_keywords");
        String keywords = searchMap.get("keywords");
        if (keywords!=null&&!"".equals(keywords)){
            criteria.is(keywords);//is是个方法且模糊检索
        }
        SimpleQuery simpleQuery=new SimpleQuery(criteria);
        //设置分页条件
        Integer pageNo = Integer.valueOf(searchMap.get("pageNo"));//当前页码
        Integer pageSize = Integer.valueOf(searchMap.get("pageSize"));//当前页大小
        //起始页
        Integer start = (pageNo - 1) * pageSize;
        simpleQuery.setOffset(start);
        simpleQuery.setRows(pageSize);
        //条件搜索
        ScoredPage<Item> scoredPage = solrTemplate.queryForPage(simpleQuery, Item.class);
        //结果集封装到map中
        Map<String,Object> map=new HashMap<>();
        //总页数
        map.put("totalPages",scoredPage.getTotalPages());
        //总条数
        map.put("total",scoredPage.getTotalElements());
        //结果集
        map.put("rows",scoredPage.getContent());
        return map;
    }
    private Map<String,Object> searchForHighLightPage(Map<String,String> searchMap){
        //设置检索的条件
        Criteria criteria=new Criteria("item_keywords");
        String keywords = searchMap.get("keywords");
        if (keywords!=null&&!"".equals(keywords)){
            criteria.is(keywords);
        }
        SimpleHighlightQuery query=new SimpleHighlightQuery(criteria);
        //设置分页
        Integer pageNo = Integer.valueOf(searchMap.get("pageNo"));//当前页码
        Integer pageSize= Integer.valueOf(searchMap.get("pageSize"));//当前页大小
        //起始页
        Integer start=(pageNo-1)*pageSize;
        //封装分页
        query.setOffset(start);
        query.setRows(pageSize);
        //设置高亮
        HighlightOptions highlightOptions=new HighlightOptions();
        //标题中包含的关键字需要高亮
        highlightOptions.addField("item_title");
        highlightOptions.setSimplePrefix("<font color='red'>");
        highlightOptions.setSimplePostfix("</font>");
        query.setHighlightOptions(highlightOptions);
        //条件过滤
        //分类过滤
        if (searchMap.get("category")!=null&&!"".equals(searchMap.get("category"))){
            Criteria cri=new Criteria("item_category");
            cri.is(searchMap.get("category"));
            FilterQuery filterQuery=new SimpleFilterQuery(cri);
            query.addFilterQuery(filterQuery);
        }
        //品牌过滤
        if (searchMap.get("brand")!=null&&!"".equals(searchMap.get("brand"))){
            Criteria cri=new Criteria("item_brand");
            cri.is(searchMap.get("brand"));
            FilterQuery filterQuery=new SimpleFilterQuery(cri);
            query.addFilterQuery(filterQuery);
        }
        //价格过滤
        if (searchMap.get("price")!=null&&!"".equals(searchMap.get("price"))){
            String[] prices=searchMap.get("price").split("-");
            Criteria cri=new Criteria("item_price");
            if (searchMap.get("price").contains("*")){
                cri.greaterThan(prices[0]);
            }else {
                cri.between(prices[0],prices[1],true,true);
            }
            FilterQuery filterQuery=new SimpleFilterQuery(cri);
            query.addFilterQuery(filterQuery);
        }
        //规格过滤
        if (searchMap.get("spec")!=null&&!"".equals(searchMap.get("spec"))){
            Map<String,String> specMap = JSON.parseObject(searchMap.get("spec"), Map.class);
            Set<Map.Entry<String, String>> entries = specMap.entrySet();
            for (Map.Entry<String, String> entry:entries) {
                Criteria cri = new Criteria("item_spec_" +entry.getKey());
                cri.is(entry.getValue());
                FilterQuery filterQuery = new SimpleFilterQuery(cri);
                query.addFilterQuery(filterQuery);
            }
        }
        //排序
        if (searchMap.get("sort")!=null&&!"".equals(searchMap.get("sort"))){
            if ("ASC".equals(searchMap.get("sort"))){
                Sort sort=new Sort(Sort.Direction.ASC,"item_"+searchMap.get("sortField"));
                query.addSort(sort);
            }else {
                Sort sort=new Sort(Sort.Direction.DESC,"item_"+searchMap.get("sortField"));
                query.addSort(sort);
            }
        }
        //检索
        HighlightPage<Item> highlightPage = solrTemplate.queryForHighlightPage(query, Item.class);
        //处理高亮的结果集
        List<HighlightEntry<Item>> highlighted = highlightPage.getHighlighted();
        if (highlighted!=null&&highlighted.size()>0){
            for (HighlightEntry<Item> highlightEntry:highlighted){
                //没有高亮的普通值
                Item item = highlightEntry.getEntity();
                //高亮
                List<HighlightEntry.Highlight> highlights = highlightEntry.getHighlights();
                if (highlights!=null&&highlights.size()>0){
                    String title = highlights.get(0).getSnipplets().get(0);
                    item.setTitle(title);
                }
            }
        }
        //封装结果集
        Map<String,Object> map=new HashMap<>();
        map.put("totalPages",highlightPage.getTotalPages());
        map.put("total",highlightPage.getTotalElements());
        map.put("rows",highlightPage.getContent());
        return map;
    }
    private List<String> searchForGroupPage(Map<String,String> specMap){
        //设置检索条件
        Criteria criteria=new Criteria("item_keywords");
        String keywords = specMap.get("keywords");
        if (keywords!=null&&!"".equals(keywords)){
            criteria.is(keywords);
        }
        SimpleQuery query=new SimpleQuery(criteria);
        //设置分组条件
        GroupOptions groupOptions=new GroupOptions();
        groupOptions.addGroupByField("item_category");
        query.setGroupOptions(groupOptions);
        //根据条件查询
        GroupPage<Item> groupPage = solrTemplate.queryForGroupPage(query, Item.class);
        //将结果集封装到List中
        List<String> list=new ArrayList<>();
        GroupResult<Item> groupResult = groupPage.getGroupResult("item_category");
        Page<GroupEntry<Item>> groupEntries = groupResult.getGroupEntries();
        for (GroupEntry<Item> groupEntry:groupEntries){
            String groupValue = groupEntry.getGroupValue();
            list.add(groupValue);
        }
        return list;
    }
    /**
     * 通过分类找到品牌和规格
     */
    private Map<String,Object> searchBrandAndSpecListByCategory(String category){
        Map<String,Object> brandAndSpecMap=new HashMap<>();
        //获取模板Id
        Object typeId = redisTemplate.boundHashOps("itemCat").get(category);
        //获取品牌
        List<Map> brandList = (List<Map>) redisTemplate.boundHashOps("brandList").get(typeId);
        //获取规格
        List<Map> specList = (List<Map>) redisTemplate.boundHashOps("specList").get(typeId);
        brandAndSpecMap.put("brandList",brandList);
        brandAndSpecMap.put("specList",specList);
        return brandAndSpecMap;
    }

    /**
     * 添加到缓存
     * @param id
     */
    @Override
    public void addItemToSolr(long id) {
        ItemQuery itemQuery=new ItemQuery();
        itemQuery.createCriteria().andGoodsIdEqualTo(id).andStatusEqualTo("1").andIsDefaultEqualTo("1").andNumGreaterThan(0);
        List<Item> itemList = itemDao.selectByExample(itemQuery);
        if(itemList!=null&&itemList.size()>0){
            for (Item item:itemList){
                String spec = item.getSpec();
                Map<String,String> specMap = JSON.parseObject(spec, Map.class);
                item.setSpecMap(specMap);
            }
            solrTemplate.saveBeans(itemList);
            solrTemplate.commit();
        }

    }

    @Override
    public void deleteItemFromSolr(Long id) {
        SimpleQuery query=new SimpleQuery("item_goodsid:"+id);
        solrTemplate.delete(query);
        solrTemplate.commit();
    }
}
