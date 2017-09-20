package com.ollearning.news.controller;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Page;
import com.ollearning.common.controller.BaseController;
import com.ollearning.common.util.CodeGenerator;
import com.ollearning.common.util.DateUtil;
import com.ollearning.interceptors.AdminACLInterceptor;
import com.ollearning.news.model.News;
import com.ollearning.news.model.NewsCategory;

@Before(AdminACLInterceptor.class)
public class NewsController extends BaseController {

	public void index() {
		News.dao.removeAllPageCache();
		Page<News> newsPage = News.dao.getPageForAdmin(getParaToInt(2),
				getParaToInt(1, 1));
		if (null != getParaToInt(2)) {
			setAttr("category", NewsCategory.dao.findById(getParaToInt(2)));
		}
		setAttr("newsPage", newsPage);
		setAttr("actionUrl", "/admin/news/index-");
		render("/admin/news/list.html");
	}

	public void add() {
		setAttr("category", NewsCategory.dao.findById(getParaToInt()));
		_form();
	}

	public void save() {
		String returnUrl = getPara("returnUrl");
		News news = getModel(News.class);
		if (null != news.getInt("id")) {
			news.set("updateTime", DateUtil.curTime()).update();
		} else {
			news.set("createTime", DateUtil.curTime()).set("clickCount", 0)
					.set("pid", CodeGenerator.getToken()).save();
			news.updateCategoryNum(1);
		}
		if (!isNullOrEmpty(returnUrl)) {
			redirect(returnUrl);
		} else {
			redirect("/admin/news");
		}
	}

	public void delete() {
		News news = News.dao.findById(getParaToInt());
		news.updateCategoryNum(-1);
		news.delete();
		Integer parentId = news.getInt("parentId");
		if (null == parentId) {
			index();
			return;
		}
		if (parentId == -1) {
			index();
		} else {
			redirect("/admin/news/child/" + parentId);
		}
	}

	public void edit() {
		News news = News.dao.findById(getParaToInt(0));
		setAttr("category", NewsCategory.dao.findById(news.get("categoryId")));
		setAttr("news", news);
		_form();
	}

	private void _form() {
		setAttr("categoryParentList", NewsCategory.dao.getTopCategory());
		render("/admin/news/news.html");
	}

}
