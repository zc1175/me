package com.ollearning.sys.controller;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Page;
import com.ollearning.common.controller.BaseController;
import com.ollearning.interceptors.AdminACLInterceptor;
import com.ollearning.sys.model.Broadcast;

@Before(AdminACLInterceptor.class)
public class BroadcastController extends BaseController {

	public void index() {
		Page<Broadcast> page = Broadcast.dao.getPage(getParaToInt(1, 1));
		setAttr("page", page);
		render("/admin/broadcast/list.html");
	}

	public void add() {
		_form();
	}

	public void save() {
		Broadcast Broadcast = getModel(Broadcast.class);
		if (null != Broadcast.get("id", null)) {
			Broadcast.update();
		} else {
			Broadcast.save();
		}
		redirect("/admin/broadcast");
	}

	public void delete() {
		Broadcast.dao.deleteById(getParaToInt());
		index();
	}

	public void edit() {
		Broadcast broadcast = Broadcast.dao.findById(getParaToInt());
		setAttr("broadcast", broadcast);
		_form();
	}

	private void _form() {
		render("/admin/broadcast/broadcast.html");
	}

}
