package com.ollearning.news.model;

import java.util.List;

import com.jfinal.plugin.activerecord.Page;
import com.ollearning.common.jfinal.Const;
import com.ollearning.common.jfinal.Model;

public class NewsCategory extends Model<NewsCategory> implements Comparable {
	private static final long serialVersionUID = -6062419811054239377L;

	private static final String CATEGORY_CACHE = "newsCategory";
	private static final String CATEGORY_CACHE_LIST = "newsCategoryList";
	private static final String CATEGORY_CACHE_TOP = "newsCategoryTop";
	private static final String CATEGORY_CACHE_CHILD = "newsCategoryChild";

	public static NewsCategory dao = new NewsCategory();

	public NewsCategory() {
		super(CATEGORY_CACHE);
	}

	public NewsCategory getByPid(String pid) {
		return findFirst("select * from news_categories where pid=?", pid);
	}

	public NewsCategory getParent() {
		return dao.findById(getInt("parentId"));
	}

	public String getTypeDesc() {
		int type = getInt("type");
		if (0 == type)
			return "父栏目";
		else if (1 == type)
			return "单页模板";
		else if (2 == type)
			return "列表页模板";
		else if (3 == type)
			return "直接跳转";
		else
			return "";
	}

	public String getLink() {
		Integer type = getInt("type");

		List<NewsCategory> childs = getChildCategory();
		if (null != childs && childs.size() > 0) {
			return "#";
		}

		if (1 == type)
			return "/channel/" + getStr("pid");
		else if (2 == type)
			return "/channel/" + getStr("pid");
		else if (3 == type)
			return getStr("linkUrl");
		else if (4 == type)
			return "";
		return "";
	}

	public Page<News> getPageNews(Integer pageNumber, Integer pageSize) {
		return News.dao.getPageForFront(getInt("id"), pageNumber, pageSize);
	}

	public void removeAllPageCache() {
		super.removeAllPageCache(CATEGORY_CACHE, CATEGORY_CACHE_LIST,
				CATEGORY_CACHE_TOP);
	}

	public Page<NewsCategory> getPage(Integer pageNumber) {
		String sqlSelect = "from news_categories where 1=1";
		// String condition = (null == companyId) ? " and companyId is null" :
		// " and companyId=" + companyId;
		// sqlSelect += condition;
		sqlSelect += " and parentId=-1 order by id asc";
		Page<NewsCategory> page = paginateByCache(CATEGORY_CACHE_LIST,
				CATEGORY_CACHE_LIST + "-" + pageNumber, pageNumber,
				Const.PAGE_SIZE_FOR_ADMIN, "select *", sqlSelect);
		return loadModelPage(page);
	}

	public List<NewsCategory> getList() {
		// String condition = (null == companyId) ? " and companyId is null" :
		// " and companyId=" + companyId;
		return findByCache(CATEGORY_CACHE_LIST, CATEGORY_CACHE_LIST,
				"select * from news_categories where 1=1 ");
	}

	public List<NewsCategory> getChilds(Integer parentId) {
		return find(
				"select * from news_categories where parentId=? order by id asc",
				parentId);
	}

	public List<NewsCategory> getChild() {
		return getChilds(getInt("id"));
	}

	public Page<NewsCategory> getChildPageForAdmin(Integer pageNumber) {
		Page<NewsCategory> page = dao.paginateByCache(CATEGORY_CACHE_LIST,
				CATEGORY_CACHE_LIST + "-" + pageNumber, pageNumber,
				Const.PAGE_SIZE_FOR_ADMIN, "select *",
				"from news_categories where parentId=? ", getInt("id"));
		return loadModelPage(page);
	}

	public List<NewsCategory> getTopCategory() {
		// String sqlCondition = (null != companyId) ? " and companyId=" +
		// companyId : " and companyId is null";
		return dao.findByCache(CATEGORY_CACHE_TOP, CATEGORY_CACHE_TOP,
				"select * from news_categories where type=0 "
						+ " and (parentId=-1 or parentId is null)");
	}

	public List<NewsCategory> getChildCategory() {
		return dao
				.findByCache(CATEGORY_CACHE_CHILD, CATEGORY_CACHE_CHILD + "-"
						+ getInt("id"),
						"select * from news_categories where parentId=? ",
						getInt("id"));
	}

	@Override
	public int compareTo(Object o) {
		NewsCategory cate = (NewsCategory) o;
		if (cate.getInt("id") > getInt("id"))
			return -1;
		else if (cate.getInt("id") < getInt("id"))
			return 1;
		return 0;
	}

}
