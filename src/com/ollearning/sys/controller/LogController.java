package com.ollearning.sys.controller;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Page;
import com.ollearning.common.controller.BaseController;
import com.ollearning.interceptors.AdminACLInterceptor;
import com.ollearning.sys.model.Log;

@Before(AdminACLInterceptor.class)
public class LogController extends BaseController {

	public void index() {
		Page<Log> logPage = Log.dao.getPage(getParaToInt(1, 1));
		setAttr("logPage", logPage);
		setAttr("actionUrl", "/admin/log/index-");
		render("/admin/log/list.html");
	}
}
