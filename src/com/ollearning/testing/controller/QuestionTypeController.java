package com.ollearning.testing.controller;

import com.jfinal.aop.Before;
import com.ollearning.common.controller.BaseController;
import com.ollearning.interceptors.AdminACLInterceptor;
import com.ollearning.testing.model.QuestionType;

@Before(AdminACLInterceptor.class)
public class QuestionTypeController extends BaseController {

	public void index() {
		setAttr("typeList", QuestionType.dao.findAll());
		render("/admin/questiontype/list.html");
	}

	public void add() {
		_form();
	}

	public void save() {
		QuestionType type = getModel(QuestionType.class);
		if (null != type.getInt("id")) {
			type.update();
		} else {
			type.save();
		}
		redirect("/admin/questiontype");
	}

	public void delete() {
		QuestionType type = QuestionType.dao.findById(getParaToInt());
		type.delete();
		redirect("/admin/questiontype");
	}

	public void edit() {
		QuestionType type = QuestionType.dao.findById(getParaToInt(0));
		setAttr("questionType", type);
		_form();
	}

	private void _form() {
		render("/admin/questiontype/questiontype.html");
	}

}
