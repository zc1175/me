package com.ollearning.news.model;

import java.util.List;

import com.jfinal.plugin.activerecord.Page;
import com.ollearning.common.jfinal.Const;
import com.ollearning.common.jfinal.Model;

public class News extends Model<News> {
	private static final long serialVersionUID = -3147052408554652602L;

	private static final String NEWS_CACHE = "news";
	private static final String NEWS_CACHE_LIST = "newsList";
	private static final String NEWS_CACHE_LIST_FRONT = "newsListFront";

	public static News dao = new News();

	public News() {
		super(NEWS_CACHE);
	}

	public String getTitleStr() {
		StringBuffer buf = new StringBuffer();
		String title = getStr("title");
		buf.append("<font style='");
		if (null != get("isBold")) {
			buf.append("font-weight:bold;");
		}
		if (null != get("color")) {
			buf.append("color:#" + getStr("color"));
		}
		buf.append("'>");
		buf.append(title);
		buf.append("</font>");
		return buf.toString();
	}

	public NewsCategory getCategory() {
		return NewsCategory.dao.findById(getInt("categoryId"));
	}

	public String getLinkUrl() {
		if (null != getStr("redirectUrl")) {
			return getStr("redirectUrl");
		}
		return "/news/view/" + getStr("pid");
	}

	public News getByPid(String pid) {
		return findFirst("select * from news where pid=? and status=1", pid);
	}

	public List<News> getHostNews() {
		return find("select * from news where status=1 order by clickCount desc limit 0,5");
	}

	public void removeAllPageCache() {
		super.removeAllPageCache(NEWS_CACHE, NEWS_CACHE_LIST);
	}

	public Page<News> getPageForAdmin(Integer categoryId, Integer pageNumber) {
		String sqlSelect = " from news where 1=1";
		String condition = (null == categoryId) ? " "
				: " and (categoryId="
						+ categoryId
						+ " or categoryId in (select id from news_categories where parentId="
						+ categoryId + "))";
		sqlSelect += condition;
		sqlSelect += " order by rank desc, id desc";
		Page<News> page = super.paginate(pageNumber, Const.PAGE_SIZE_FOR_ADMIN,
				"select *", sqlSelect);
		return loadModelPage(page);
	}

	public Page<News> getPageForFront(Integer categoryId, Integer pageNumber,
			Integer pageSize) {
		String sqlSelect = "from news where 1=1 and status=1";
		String condition = (null == categoryId) ? "" : " and categoryId="
				+ categoryId;
		sqlSelect += condition;
		sqlSelect += " order by rank desc, id desc";
		Page<News> page = super.paginate(pageNumber, pageSize, "select *",
				sqlSelect);
		return loadModelPage(page);
	}

	public Page<News> getPageForFront(String[] categoryIds, Integer pageNumber,
			Integer pageSize) {
		String sqlSelect = "from news where 1=1 and status=1 ";
		String condition = "";
		String whereCIds = "";
		if (null != categoryIds && categoryIds.length > 0) {
			for (int i = 0; i < categoryIds.length; i++) {
				whereCIds += categoryIds[i];
				if (i < categoryIds.length - 1) {
					whereCIds += ",";
				}
			}
			condition = " and categoryId in ( " + whereCIds + ")";
		}
		sqlSelect += condition;
		sqlSelect += " order by rank desc, id desc";
		Page<News> page = super.paginate(pageNumber, pageSize, "select *",
				sqlSelect);
		return loadModelPage(page);
	}

	public List<News> getRelatedNews(Integer newsId) {
		return find(
				"select * from news where status=1 and id<>? and categoryId=? order by id desc",
				newsId, findById(newsId).getInt("categoryId"));
	}

	/**
	 * 更新所属分类对应的文档的数量
	 * 
	 * @param num
	 */
	public void updateCategoryNum(int num) {
		NewsCategory docType = NewsCategory.dao.loadModel(getInt("categoryId"));
		if (null == docType)
			return;

		docType.set(
				"newsCount",
				(null == docType.getInt("newsCount") ? 0 : docType
						.getInt("newsCount")) + num).update();
		NewsCategory docTypeParent = docType.getParent();
		if (null != docTypeParent) {
			Integer parentNewsCount = docTypeParent.getInt("newsCount");
			parentNewsCount = (null == parentNewsCount ? 0 : parentNewsCount);
			docTypeParent.set("newsCount", parentNewsCount + num).update();
			NewsCategory docTypePParent = docTypeParent.getParent();
			if (null != docTypePParent) {
				docTypePParent.set(
						"newsCount",
						(null == docTypePParent.getInt("newsCount") ? 0
								: docTypePParent.getInt("newsCount")) + num)
						.update();
			}
		}
	}

}
