package com.ollearning.user.controller;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Page;
import com.ollearning.common.controller.BaseController;
import com.ollearning.interceptors.AdminACLInterceptor;
import com.ollearning.user.model.UserLevel;

@Before(AdminACLInterceptor.class)
public class UserLevelController extends BaseController {

	public void index() {
		Page<UserLevel> levelPage = UserLevel.dao.getPage(getParaToInt(1, 1));
		setAttr("userLevelPage", levelPage);
		setAttr("actionUrl", "/admin/userLevel/index-");
		render("/admin/userLevel/list.html");
	}

	public void add() {
		_form();
	}

	public void save() {
		UserLevel level = getModel(UserLevel.class);
		if (null != level.getInt("id")) {
			level.update();
		} else {
			level.save();
		}
		redirect("/admin/userLevel");
	}

	public void delete() {
		UserLevel level = UserLevel.dao.findById(getParaToInt());
		level.delete();
		redirect("/admin/userLevel");
	}

	public void edit() {
		UserLevel level = UserLevel.dao.findById(getParaToInt());
		setAttr("userLevel", level);
		_form();
	}

	private void _form() {
		render("/admin/userLevel/userLevel.html");
	}

}
