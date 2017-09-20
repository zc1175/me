package com.ollearning.sys.controller;

import com.jfinal.aop.Before;
import com.ollearning.common.controller.BaseController;
import com.ollearning.common.jfinal.Const;
import com.ollearning.common.util.DateUtil;
import com.ollearning.common.util.MD5;
import com.ollearning.interceptors.AdminACLInterceptor;
import com.ollearning.sys.model.Operator;

public class AdminController extends BaseController {

	public void index() {
		render("/admin/login.html");
	}

	@Before(AdminACLInterceptor.class)
	public void main() {
	}

	public void login() {
		String ret = getPara("ret");
		if ("error".equals(ret)) {
			setAttr("msg", "登录名密码不匹配，或账号被禁用!");
		}
		render("/admin/login.html");
	}

	public void doLogin() {
		Operator operator = getModel(Operator.class);
		operator = operator.get(operator.getStr("loginName"),
				MD5.GetMD5Code(operator.getStr("loginPwd")));
		if (null != operator && operator.getInt("flag") == 1) {
			operator.set("curLoginTime", operator.get("lastLoginTime"))
					.set("lastLoginTime", DateUtil.curTime())
					.set("lastLoginIp", getRealIpAddr())
					.set("loginCount", operator.getInt("loginCount") + 1)
					.update();
			setSessionAttr(Const.OPERATOR_SESSION_KEY, operator);
			redirect("/admin/main");
			return;
		} else {
			redirect("/admin/login?ret=error");
		}
	}

	public void logout() {
		removeSessionAttr(Const.OPERATOR_SESSION_KEY);
		redirect("/admin/login");
	}

	public void top() {

	}

	public void left() {

	}

	public void welcome() {

	}

	public void checklogin() {
		if (null == getLoginOperator()) {
			renderHtml("<script>alert('登录超时，请重新登录！');parent.parent.location.href='"
					+ getCtxPath() + "/admin/login';</script>");
			return;
		} else {
			return;
		}
	}
}
