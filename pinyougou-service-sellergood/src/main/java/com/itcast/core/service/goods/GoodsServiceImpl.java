package com.itcast.core.service.goods;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.itcast.core.dao.good.BrandDao;
import com.itcast.core.dao.good.GoodsDao;
import com.itcast.core.dao.good.GoodsDescDao;
import com.itcast.core.dao.item.ItemCatDao;
import com.itcast.core.dao.item.ItemDao;
import com.itcast.core.dao.seller.SellerDao;
import com.itcast.core.entity.PageResult;
import com.itcast.core.pojo.good.Goods;
import com.itcast.core.pojo.good.GoodsDesc;
import com.itcast.core.pojo.good.GoodsQuery;
import com.itcast.core.pojo.item.Item;
import com.itcast.core.pojo.item.ItemQuery;
import com.itcast.core.service.staticPage.StaticPageService;
import com.itcast.core.vo.GoodsVo;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.jms.*;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
@Transactional
@Service
public class GoodsServiceImpl implements GoodsService {

    @Resource
    private GoodsDao goodsDao;

    @Resource
    private GoodsDescDao goodsDescDao;

    @Resource
    private ItemDao itemDao;

    @Resource
    private ItemCatDao itemCatDao;

    @Resource
    private SellerDao sellerDao;

    @Resource
    private BrandDao brandDao;

    @Resource
    private SolrTemplate solrTemplate;
    @Resource
    private JmsTemplate jmsTemplate;
   @Resource
   private Destination topicPageAndSolrDestination;
   @Resource
   private Destination queueSolrDeleteDestination;

    /**
     * 商品录入
     * @param goodsVo
     */
    @Transactional
    @Override
    public void add(GoodsVo goodsVo) {
        // 保存商品基本信息
        Goods goods = goodsVo.getGoods();
        goods.setAuditStatus("0");  // 商品的审核状态：待审核
//        goods.setSellerId(sellerId);
        goodsDao.insertSelective(goods);    // 返回自增主键的id
        // 保存商品描述信息
        GoodsDesc goodsDesc = goodsVo.getGoodsDesc();
        goodsDesc.setGoodsId(goods.getId());
        goodsDescDao.insertSelective(goodsDesc);
        // 保存商品库存信息
        // 分析：判断是否启用规格   启用规格：spu 1 -- sku n  不启用规格：spu 1 -- sku 1
        if("1".equals(goods.getIsEnableSpec())){
            // 启用规格 spu 1 -- sku n
            List<Item> itemList = goodsVo.getItemList();
            if(itemList != null && itemList.size() > 0){
                for (Item item : itemList) {
                    // title = spu名称 + spu副标题 + 规格名称
                    String title = goods.getGoodsName() + " " + goods.getCaption();
                    // 数据：{"机身内存":"16G","网络":"联通3G"}
                    String spec = item.getSpec();
                    Map<String, String> map = JSON.parseObject(spec, Map.class);
                    Set<Map.Entry<String, String>> entries = map.entrySet();
                    for (Map.Entry<String, String> entry : entries) {
                        title += " " + entry.getValue();
                    }
                    item.setTitle(title);
                    // 设置库存属性
                    setAttributeForItem(goods, goodsDesc, item);

                    // 保存
                    itemDao.insertSelective(item);
                }
            }

        }else{
            // 未启用规格
            Item item = new Item();
            item.setTitle(goods.getGoodsName() + " " + goods.getCaption());
            item.setPrice(goods.getPrice());
            item.setNum(9999);
            item.setIsDefault("1");
            item.setSpec("{}");
            setAttributeForItem(goods, goodsDesc, item);
            itemDao.insertSelective(item);
        }
    }

    // 设置库存属性
    private void setAttributeForItem(Goods goods, GoodsDesc goodsDesc, Item item) {
        // 图片
        // 数据：[{"color":"粉色","url":"http://192.168.25.133/group1/M00/00/00/wKgZhVmOXq2AFIs5AAgawLS1G5Y004.jpg"},
        // {"color":"黑色","url":"http://192.168.25.133/group1/M00/00/00/wKgZhVmOXrWAcIsOAAETwD7A1Is874.jpg"}]
        String itemImages = goodsDesc.getItemImages();
        List<Map> images = JSON.parseArray(itemImages, Map.class);
        if(images != null && images.size() > 0){
            String image = images.get(0).get("url").toString();
            item.setImage(image);
        }
        item.setCategoryid(goods.getCategory3Id()); // 三级分类id
        item.setStatus("1");    // 库存商品的状态
        item.setCreateTime(new Date());
        item.setUpdateTime(new Date());
        item.setGoodsId(goods.getId()); // spu的id
        item.setSellerId(goods.getSellerId());  // 商家id
        item.setCategory(itemCatDao.selectByPrimaryKey(goods.getCategory3Id()).getName()); // 三级分类名称
        item.setBrand(brandDao.selectByPrimaryKey(goods.getBrandId()).getName());    // 品牌名称
        item.setSeller(sellerDao.selectByPrimaryKey(goods.getSellerId()).getNickName());   // 商家的店铺名称
    }

    /**
     * 查询商品列表
     * @param page
     * @param rows
     * @param goods
     * @return
     */
    @Override
    public PageResult search(Integer page, Integer rows, Goods goods) {
        PageHelper.startPage(page,rows);
        GoodsQuery query=new GoodsQuery();
        query.setOrderByClause("id desc");
        if (goods.getGoodsName()!=null&&!"".equals(goods.getGoodsName().trim())){
            query.createCriteria().andGoodsNameEqualTo("%"+goods.getGoodsName().trim()+"%");
        }
        if (goods.getSellerId()!=null){
            query.createCriteria().andSellerIdEqualTo(goods.getSellerId());
        }
        Page<Goods> p = (Page<Goods>) goodsDao.selectByExample(query);

        return new PageResult(p.getTotal(),p.getResult());
    }

    /**
     * 回显商品
     * @param id
     * @return
     */
    @Override
    public GoodsVo findOne(Long id) {
        GoodsVo goodsVo=new GoodsVo();
        Goods goods = goodsDao.selectByPrimaryKey(id);
        goodsVo.setGoods(goods);
        GoodsDesc goodsDesc = goodsDescDao.selectByPrimaryKey(id);
        goodsVo.setGoodsDesc(goodsDesc);
        ItemQuery query=new ItemQuery();
        query.createCriteria().andGoodsIdEqualTo(id);
        List<Item> items = itemDao.selectByExample(query);
        goodsVo.setItemList(items);
        return goodsVo;
    }

    /**
     * 更新商品
     * @param goodsVo
     */
    @Override
    public void update(GoodsVo goodsVo) {
        Goods goods = goodsVo.getGoods();
        goodsDao.updateByPrimaryKeySelective(goods);
        goods.setAuditStatus("0");
        GoodsDesc goodsDesc = goodsVo.getGoodsDesc();
        goodsDescDao.updateByPrimaryKeySelective(goodsDesc);
        ItemQuery query=new ItemQuery();
        query.createCriteria().andGoodsIdEqualTo(goods.getId());
        itemDao.deleteByExample(query);
        if ("1".equals(goods.getIsEnableSpec())){
            List<Item> itemList = goodsVo.getItemList();
            if (itemList!=null&&itemList.size()>0){
                for (Item item:itemList){
                    String title=goods.getGoodsName()+" "+goods.getCaption();
                    String spec = item.getSpec();
                    Map<String,String> map=JSON.parseObject(spec,Map.class);
                    Set<Map.Entry<String, String>> entries = map.entrySet();
                    for (Map.Entry<String, String> entry:entries){
                        String value = entry.getValue();
                        title+=" "+value;
                    }
                    item.setTitle(title);
                    setAttributeForItem(goods,goodsDesc,item);
                    itemDao.updateByPrimaryKeySelective(item);
                }
            }
        }else {
            Item item=new Item();
            item.setTitle(goods.getGoodsName()+" "+goods.getCaption());
            item.setIsDefault("1");
            item.setNum(999);
            item.setPrice(goods.getPrice());
            item.setSpec("{}");
            setAttributeForItem(goods,goodsDesc,item);
            itemDao.updateByPrimaryKeySelective(item);
        }
    }

    /**
     * 运行商商品列表查询
     * @param page
     * @param rows
     * @param goods
     * @return
     */
    @Override
    public PageResult searchForManager(Integer page, Integer rows, Goods goods) {
        //设置分页
        PageHelper.startPage(page,rows);
        GoodsQuery query=new GoodsQuery();
        GoodsQuery.Criteria criteria = query.createCriteria();
        if (goods.getGoodsName()!=null&&!"".equals(goods.getGoodsName().trim())){
            criteria.andGoodsNameEqualTo("%"+goods.getGoodsName().trim()+"%");
        }
        if (goods.getAuditStatus()!=null&&!"".equals(goods.getAuditStatus().trim())){
            criteria.andAuditStatusEqualTo(goods.getAuditStatus().trim());
        }
        criteria.andIsDeleteIsNull();
        query.setOrderByClause("id desc");
        Page<Goods> p = (Page<Goods>) goodsDao.selectByExample(query);
        return new PageResult(p.getTotal(),p.getResult());
    }

    /**
     * 审核商品
     * @param ids
     * @param status
     */
    @Override
    public void updateStatus(Long[] ids, String status) {
        Goods goods=new Goods();
        goods.setAuditStatus(status);
        if (ids!=null&&ids.length>0){
            for (final Long id:ids){
                goods.setId(id);
                goodsDao.updateByPrimaryKeySelective(goods);
                if ("1".equals(status)){
                    //TODO商品进行上架；将数据保存到索引库中
                    //为了测试检索，把这里的所有库存数据全部保存到索引库中
                    //dateImportToSolr();
                    //审核一个加一个到缓存中
                    //saveItemToSolr(id);
                    //生成静态页面
                    //staticPageService.getHtml(id);
                    jmsTemplate.send(topicPageAndSolrDestination, new MessageCreator() {
                        @Override
                        public Message createMessage(Session session) throws JMSException {
                            //建立消息体发送到MQ中
                            TextMessage textMessage = session.createTextMessage(String.valueOf(id));
                            return textMessage;
                        }
                    });
                    }
            }
        }
    }

    /**
     * 审核一个加一个到缓存中
     * @param id

    private void saveItemToSolr(Long id){
        ItemQuery itemQuery=new ItemQuery();
        itemQuery.createCriteria().andStatusEqualTo("1").andIsDefaultEqualTo("1").
                andGoodsIdEqualTo(id).andNumGreaterThan(0);
        List<Item> items = itemDao.selectByExample(itemQuery);
        if (items!=null&&items.size()>0){
            for (Item item:items){
                String spec = item.getSpec();
                Map<String,String> specMap = JSON.parseObject(spec, Map.class);
                item.setSpecMap(specMap);
            }
            solrTemplate.saveBeans(items);
            solrTemplate.commit();
        }
    }
     */

    /**
     * 删除商品
     * @param ids
     */
    @Override
    public void delete(Long[] ids) {
        if (ids!=null&&ids.length>0){
            Goods goods=new Goods();
            goods.setIsDelete("1");
            for (final Long id:ids){
                goods.setId(id);
                goodsDao.updateByPrimaryKeySelective(goods);
                jmsTemplate.send(queueSolrDeleteDestination, new MessageCreator() {
                    @Override
                    public Message createMessage(Session session) throws JMSException {
                        TextMessage textMessage = session.createTextMessage(String.valueOf(id));
                        return textMessage;
                    }
                });
                //TODO商品下架;将删除索引库
//                SimpleQuery simpleQuery=new SimpleQuery("item_goodsid:"+id);
//                solrTemplate.delete(simpleQuery);
//                solrTemplate.commit();
                //删除静态页面（可选）

            }
        }
    }
}
