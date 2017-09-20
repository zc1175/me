package com.ollearning.sys.controller;

import java.util.List;

import javax.servlet.http.HttpSession;

import com.ollearning.common.controller.BaseController;
import com.ollearning.common.jfinal.Const;
import com.ollearning.common.util.SessionUtil;

public class SessionController extends BaseController {

	public void index() {
		setAttr("sessions", Const.ONLINE_USERS);
		render("/admin/session/list.html");
	}

	public void kill() {
		String sessionId = getPara();
		SessionUtil.killUserBySessionId(sessionId);
		redirect("/admin/session");
	}

}
