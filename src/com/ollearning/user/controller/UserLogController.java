package com.ollearning.user.controller;

import com.ollearning.common.controller.BaseController;
import com.ollearning.user.model.User;
import com.ollearning.user.model.UserLog;

public class UserLogController extends BaseController {

	public void index() {
		User user = getModel(User.class);
		int pageNumber = getParaToInt("pageNumber", 1);
		setAttr("user", user);
		setAttr("page", UserLog.me.findByPage(pageNumber, user));
		render("/admin/user_log/list.html");
	}

}
