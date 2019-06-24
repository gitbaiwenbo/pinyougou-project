package com.itcast.core.service.staticPage;

import com.itcast.core.dao.good.GoodsDao;
import com.itcast.core.dao.good.GoodsDescDao;
import com.itcast.core.dao.item.ItemCatDao;
import com.itcast.core.dao.item.ItemDao;
import com.itcast.core.pojo.good.Goods;
import com.itcast.core.pojo.good.GoodsDesc;
import com.itcast.core.pojo.item.Item;
import com.itcast.core.pojo.item.ItemCat;
import com.itcast.core.pojo.item.ItemQuery;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StaticPageServiceImpl implements StaticPageService,ServletContextAware {
    @Resource
    private ItemCatDao itemCatDao;
    @Resource
    private ItemDao itemDao;
    @Resource
    private GoodsDao goodsDao;
    @Resource
    private GoodsDescDao goodsDescDao;

    private Configuration configuration;
    public void setFreeMarkerConfigurer(FreeMarkerConfigurer freeMarkerConfigurer){
        this.configuration=freeMarkerConfigurer.getConfiguration();
    }

    private ServletContext servletContext;
    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext=servletContext;
    }

    @Override
    public void getHtml(Long id) {
        try {
            //获取该位置下的模板
            Template template = configuration.getTemplate("item.ftl");
            Map<String,Object> dataModel=getDataModel(id);
            //指定静态页生成的位置
            String pathname="/"+id+".html";
            String path=servletContext.getRealPath(pathname);
            File file=new File(path);
            template.process(dataModel,new OutputStreamWriter(new FileOutputStream(file),"UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Map<String, Object> getDataModel(Long id) {
        Map<String,Object> map=new HashMap<>();
        //获取 基本信息
        Goods goods = goodsDao.selectByPrimaryKey(id);
        map.put("goods",goods);
        //获取描述信息
        GoodsDesc goodsDesc = goodsDescDao.selectByPrimaryKey(id);
        map.put("goodsDesc",goodsDesc);
        //分类信息
        ItemCat itemCat1 = itemCatDao.selectByPrimaryKey(goods.getCategory1Id());
        ItemCat itemCat2 = itemCatDao.selectByPrimaryKey(goods.getCategory2Id());
        ItemCat itemCat3 = itemCatDao.selectByPrimaryKey(goods.getCategory3Id());
        map.put("itemCat1",itemCat1);
        map.put("itemCat2",itemCat2);
        map.put("itemCat3",itemCat3);
        //获取库存信息
        ItemQuery query=new ItemQuery();
        query.createCriteria().andGoodsIdEqualTo(id);
        List<Item> itemList = itemDao.selectByExample(query);
        map.put("itemList",itemList);
        return map;
    }

}
