package com.ollearning.news.controller;

import java.util.List;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Page;
import com.ollearning.common.controller.BaseController;
import com.ollearning.common.util.CodeGenerator;
import com.ollearning.common.util.DateUtil;
import com.ollearning.docfile.model.DocType;
import com.ollearning.interceptors.AdminACLInterceptor;
import com.ollearning.news.model.NewsCategory;

@Before(AdminACLInterceptor.class)
public class NewsCategoryController extends BaseController {

	public void index() {
		NewsCategory.dao.removeAllPageCache();
		Page<NewsCategory> categoryPage = NewsCategory.dao
				.getPage(getParaToInt(1, 1));
		setAttr("categoryPage", categoryPage);
		setAttr("actionUrl", "/admin/newsCategory/index-");
		render("/admin/newsCategory/list.html");
	}

	public void add() {
		setAttr("parentCategory", NewsCategory.dao.findById(getParaToInt()));
		_form();
	}

	public void save() {
		String returnUrl = getPara("returnUrl");
		NewsCategory category = getModel(NewsCategory.class);
		if (null != category.getInt("id")) {
			category.set("updateTime", DateUtil.curTime()).update();
		} else {
			try {
				if (null == category.getInt("parentId")
						|| -1 == category.getInt("parentId")) {
					category.set("level", 1).set("childNum", 0);
				} else {
					category.set(
							"level",
							NewsCategory.dao.findById(
									category.getInt("parentId"))
									.getInt("level") + 1);
					NewsCategory parent = NewsCategory.dao.findById(category
							.getInt("parentId"));
					parent.set("childNum", parent.getInt("childNum") + 1)
							.update();
				}
			} catch (Exception e) {
			}
			category.set("createTime", DateUtil.curTime()).set("childNum", 0)
					.set("newsCount", 0).set("pid", CodeGenerator.getToken())
					.save();
		}
		if (!isNullOrEmpty(returnUrl)) {
			redirect(returnUrl);
		} else {
			redirect("/admin/newsCategory");
		}
	}

	public void content() {
		setAttr("newsCategory", NewsCategory.dao.findById(getParaToInt()));
		if (null != getPara("result")) {
			setAttr("msg", "保存成功");
		}
		render("/admin/newsCategory/content.html");
	}

	public void saveContent() {
		NewsCategory temp = getModel(NewsCategory.class);
		NewsCategory channel = NewsCategory.dao.findById(temp.getInt("id"));
		channel.set("content", temp.getStr("content"));
		channel.update();
		redirect("/admin/newsCategory/content/" + temp.getInt("id")
				+ "?result=1");
	}

	public void delete() {
		NewsCategory category = NewsCategory.dao.findById(getParaToInt());
		category.delete();

		Integer parentId = category.getInt("parentId");
		if (null == parentId) {
			index();
			return;
		}
		if (parentId == -1) {
			index();
		} else {
			redirect("/admin/newsCategory/child/" + parentId);
		}
	}

	public void edit() {
		setAttr("parentCategory", NewsCategory.dao.findById(getParaToInt(1)));
		NewsCategory category = NewsCategory.dao.findById(getParaToInt(0));
		setAttr("newsCategory", category);
		_form();
	}

	/**
	 * 转至子类列表
	 */
	public void child() {
		NewsCategory.dao.removeAllPageCache();
		NewsCategory category = NewsCategory.dao.findById(getParaToInt());
		Page<NewsCategory> categoryPage = category
				.getChildPageForAdmin(getParaToInt(1, 1));
		setAttr("category", category);
		setAttr("categoryPage", categoryPage);
		setAttr("actionUrl",
				"/admin/newsCategory/child/" + category.getInt("id") + "-");
		render("/admin/newsCategory/child.html");
	}

	public void parentJson() {
		NewsCategory.dao.removeAllPageCache();
		List<NewsCategory> list = NewsCategory.dao.getTopCategory();
		renderJson(list);
	}

	public void childJson() {
		NewsCategory.dao.removeAllPageCache();
		Integer categoryId = getParaToInt("categoryId");
		NewsCategory category = NewsCategory.dao.findById(categoryId);
		renderJson(category.getChildCategory());
	}

	private void _form() {
		setAttr("parentCategories", NewsCategory.dao.getTopCategory());
		render("/admin/newsCategory/category.html");
	}
}
