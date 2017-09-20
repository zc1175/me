package com.ollearning.sys.controller;

import com.jfinal.aop.Before;
import com.ollearning.common.controller.BaseController;
import com.ollearning.interceptors.AdminACLInterceptor;
import com.ollearning.sys.model.Dict;

@Before(AdminACLInterceptor.class)
public class DictController extends BaseController {

	public void index() {
		setAttr("dictPage", Dict.dao.findPage(getParaToInt(1, 1)));
		setAttr("actionUrl", "/admin/dict/index-");
		render("/admin/dict/list.html");
	}

	public void save() {
		Dict dict = getModel(Dict.class);
		if (null != dict.getInt("id")) {
			dict.update();
		} else {
			dict.save();
		}
		redirect("/admin/dict");
	}

	public void add() {
		_form();
	}

	public void edit() {
		setAttr("dict", Dict.dao.findById(getParaToInt()));
		_form();
	}

	public void _form() {
		render("/admin/dict/dict.html");
	}

	public void delete() {
		Dict.dao.deleteById(getParaToInt());
		redirect("/admin/dict");
	}
}
