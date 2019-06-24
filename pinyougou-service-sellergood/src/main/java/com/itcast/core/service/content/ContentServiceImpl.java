package com.itcast.core.service.content;

import java.util.List;

import com.itcast.core.entity.PageResult;
import com.itcast.core.pojo.ad.ContentQuery;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import com.itcast.core.dao.ad.ContentDao;
import com.itcast.core.pojo.ad.Content;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;

@Service
public class ContentServiceImpl implements ContentService {

	@Resource
	private ContentDao contentDao;
	@Resource
	private RedisTemplate redisTemplate;

	@Override
	public List<Content> findAll() {
		List<Content> list = contentDao.selectByExample(null);
		return list;
	}

	@Override
	public PageResult findPage(Content content, Integer pageNum, Integer pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		Page<Content> page = (Page<Content>)contentDao.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public void add(Content content) {
		clearCashe(content.getCategoryId());
		contentDao.insertSelective(content);
	}

	private void clearCashe(Long categoryId) {
		redisTemplate.boundHashOps("content").delete(categoryId);
	}

	@Override
	public void edit(Content content) {
		Long newCategoryId = content.getCategoryId();
		Long oldCategoryId=contentDao.selectByPrimaryKey(content.getId()).getCategoryId();
		//更新缓存
		if (newCategoryId!=oldCategoryId){//分类发生 改变时
			clearCashe(newCategoryId);
			clearCashe(oldCategoryId);
		}else {
			clearCashe(newCategoryId);
		}
		contentDao.updateByPrimaryKeySelective(content);
	}

	@Override
	public Content findOne(Long id) {
		Content content = contentDao.selectByPrimaryKey(id);
		return content;
	}

	@Override
	public void delAll(Long[] ids) {
		if(ids != null){
			for(Long id : ids){
				clearCashe(contentDao.selectByPrimaryKey(id).getCategoryId());
				contentDao.deleteByPrimaryKey(id);
			}
		}
	}

	/**
	 * 首页大广告的轮播图
	 * @param categoryId
	 * @return
	 */
	@Override
	public List<Content> findByCategoryId(Long categoryId) {
		List<Content> list = (List<Content>) redisTemplate.boundHashOps("content").get(categoryId);
		synchronized (this) {
			if (list == null) {
				//继续判断缓存是否有值
				list = (List<Content>) redisTemplate.boundHashOps("content").get(categoryId);
				if (list == null) {
					//缓存中没有，去数据库中拿
					ContentQuery query = new ContentQuery();//设置条件：根据广告分类查询，并且是可用的
					query.createCriteria().andCategoryIdEqualTo(categoryId).andStatusEqualTo("1");
					query.setOrderByClause("sort_order desc");
					list = contentDao.selectByExample(query);
					//数据放入缓存
					redisTemplate.boundHashOps("content").put(categoryId, list);
				}
			}
		}
		return list;
	}
}
