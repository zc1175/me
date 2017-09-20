package com.ollearning.sys.controller;

import java.util.List;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Page;
import com.ollearning.common.controller.BaseController;
import com.ollearning.interceptors.AdminACLInterceptor;
import com.ollearning.site.model.NavMenu;

@Before(AdminACLInterceptor.class)
public class NavMenuController extends BaseController {

	public void index() {
		NavMenu.dao.removeAllPageCache();
		List<NavMenu> menuList = NavMenu.dao.getList();
		setAttr("menuList", menuList);
		render("/admin/navMenu/list.html");
	}

	public void add() {
		setAttr("parentMenu", NavMenu.dao.findById(getParaToInt()));
		_form();
	}

	public void save() {
		String returnUrl = getPara("returnUrl");
		NavMenu category = getModel(NavMenu.class);
		if (null != category.getInt("id")) {
			category.update();
		} else {
			category.save();
		}
		if (!isNullOrEmpty(returnUrl)) {
			redirect(returnUrl);
		} else {
			redirect("/admin/navMenu");
		}
	}

	public void delete() {
		NavMenu category = NavMenu.dao.findById(getParaToInt());
		category.delete();

		Integer parentId = category.getInt("parentId");
		if (null == parentId) {
			index();
			return;
		}
		if (parentId == -1) {
			index();
		} else {
			redirect("/admin/navMenu/child/" + parentId);
		}
	}

	public void edit() {
		setAttr("parentMenu", NavMenu.dao.findById(getParaToInt(1)));
		NavMenu category = NavMenu.dao.findById(getParaToInt(0));
		setAttr("navMenu", category);
		_form();
	}

	/**
	 * 转至子类列表
	 */
	public void child() {
		NavMenu.dao.removeAllPageCache();
		NavMenu category = NavMenu.dao.findById(getParaToInt());
		Page<NavMenu> categoryPage = category
				.getChildPageForAdmin(getParaToInt(1, 1));
		setAttr("menu", category);
		setAttr("menuPage", categoryPage);
		setAttr("actionUrl", "/admin/navMenu/child/" + category.getInt("id")
				+ "-");
		render("/admin/navMenu/child.html");
	}

	public void parentJson() {
		NavMenu.dao.removeAllPageCache();
		Integer companyId = getParaToInt("companyId");
		List<NavMenu> list = NavMenu.dao.getTopCategory(null);
		List<NavMenu> list2 = NavMenu.dao.getTopCategory(companyId);
		list.addAll(list2);

		renderJson(list);
	}

	public void childJson() {
		NavMenu.dao.removeAllPageCache();
		Integer categoryId = getParaToInt("menuId");
		NavMenu category = NavMenu.dao.findById(categoryId);
		renderJson(category.getChildCategory());
	}

	private void _form() {
		setAttr("parentMenus", NavMenu.dao.getTopCategory(null));
		render("/admin/navMenu/navMenu.html");
	}

}
