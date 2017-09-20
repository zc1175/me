package com.ollearning.user.controller;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Page;
import com.ollearning.common.controller.BaseController;
import com.ollearning.common.util.MD5;
import com.ollearning.interceptors.AdminACLInterceptor;
import com.ollearning.user.model.User;
import com.ollearning.user.model.UserLevel;

@Before(AdminACLInterceptor.class)
public class UserController extends BaseController {

	public void index() {
		User user = getModel(User.class); // 封装查询参数
		Page<User> userPage = User.dao.getPage(getParaToInt(1, 1), user);
		setAttr("userPage", userPage);
		setAttr("actionUrl", "/admin/user/index-");
		render("/admin/user/list.html");
	}

	public void add() {
		_form();
	}

	public void save() {
		User user = getModel(User.class);
		if (null != user.getInt("id")) {
			// 判断若未修改密码,则使用原有的
			if (null == user.getStr("loginPwd")
					|| user.getStr("loginPwd").trim().length() == 0) {
				User op = User.dao.findById(user.getInt("id"));
				user.set("loginPwd", op.get("loginPwd"));
			} else {
				user.set("loginPwd", MD5.GetMD5Code(user.getStr("loginPwd")));
			}
			user.update();
		} else {
			user.set("loginPwd", MD5.GetMD5Code(user.getStr("loginPwd")));
			user.save();
		}
		redirect(getBasePath() + "/admin/user");
	}

	public void delete() {
		User user = User.dao.findById(getParaToInt());
		user.delete();
		redirect(getBasePath() + "/admin/user");
	}

	public void edit() {
		User user = User.dao.findById(getParaToInt());
		setAttr("user", user);
		_form();
	}

	private void _form() {
		setAttr("userLevelList", UserLevel.dao.getAll());
		render("/admin/user/user.html");
	}

}
