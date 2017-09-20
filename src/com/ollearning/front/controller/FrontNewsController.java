package com.ollearning.front.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Page;
import com.ollearning.common.controller.BaseController;
import com.ollearning.common.jfinal.Const;
import com.ollearning.news.model.News;
import com.ollearning.news.model.NewsCategory;
import com.ollearning.sys.model.Dict;

public class FrontNewsController extends BaseController {

	public void index() {
		Map<NewsCategory, Page<News>> ret = new TreeMap<NewsCategory, Page<News>>();

		// 获取数据字典中前台资讯版块显示的分类ID列表
		String newsCateIds = Dict.dao.getValue("SYS_VALUE",
				"FRONT_NEWS_CATE_ID");
		List<NewsCategory> list = new ArrayList();
		if (StrKit.notBlank(newsCateIds)) {
			String[] newsCateIdArray = newsCateIds.split(",");
			for (String newsCateId : newsCateIdArray) {
				NewsCategory cate = NewsCategory.dao.findById(newsCateId);
				list.add(cate);
				ret.put(cate,
						News.dao.getPageForFront(new Integer(newsCateId), 1, 6));
			}
		}
		setAttr("list", list);
		setAttr("newsMap", ret);
		render("/front/news/index.html");
	}

	// 栏目 列表
	public void c() {
		String catePid = getPara();
		NewsCategory cate = NewsCategory.dao.getByPid(catePid);
		Page<News> newsPage = cate.getPageNews(getParaToInt("pageNo", 1),
				Const.PAGE_SIZE_FOR_FRONT);
		setAttr("category", cate);
		setAttr("newsPage", newsPage);
		render("/front/news/cate.html");
	}

	public void view() {
		News news = News.dao.getByPid(getPara());
		if (null == news) {
			renderHtml("无效访问");
			return;
		}
		news.set("clickCount", news.getInt("clickCount") + 1).update();
		setAttr("news", news);
		setAttr("relatedNews", News.dao.getRelatedNews(news.getInt("id")));
		render("/front/news/view.html");
	}

}
