package com.ollearning.sys.controller;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Page;
import com.ollearning.common.controller.BaseController;
import com.ollearning.interceptors.AdminACLInterceptor;
import com.ollearning.sys.model.Right;

@Before(AdminACLInterceptor.class)
public class RightController extends BaseController {

	public void index() {
		Right.dao.removeAllCache();
		Page<Right> rightPage = Right.dao.getPage(getParaToInt(1, 1));
		setAttr("rightPage", rightPage);
		setAttr("actionUrl", "/admin/right/index-");
		render("/admin/right/list.html");
	}

	public void add() {
		_form();
	}

	public void save() {
		Right right = getModel(Right.class);
		if (null != right.get("id", null)) {
			right.update();
		} else {
			right.save();
		}
		redirect("/admin/right");
	}

	public void delete() {
		Right.dao.deleteById(getParaToInt());
		index();
	}

	public void edit() {
		Right right = Right.dao.findById(getParaToInt());
		setAttr("right", right);
		_form();
	}

	private void _form() {
		render("/admin/right/right.html");
	}

}
