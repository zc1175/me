package com.ollearning.testing.controller;

import java.util.HashMap;
import java.util.Map;

import com.jfinal.aop.Before;
import com.ollearning.common.controller.BaseController;
import com.ollearning.interceptors.AdminACLInterceptor;
import com.ollearning.testing.model.ExamType;
import com.ollearning.testing.model.Subject;

@Before(AdminACLInterceptor.class)
public class SubjectController extends BaseController {

	public void index() {
		setAttr("examTypeList", ExamType.dao.getTopList());
		setAttr("subjectList", Subject.dao.findByExamTypeId(getParaToInt()));
		setAttr("subject", new Subject().set("examTypeId", getParaToInt()));
		render("/admin/subject/list.html");
	}

	public void add() {
		setAttr("examType", ExamType.dao.findById(getParaToInt()));
		_form();
	}

	public void save() {
		Subject subject = getModel(Subject.class);
		if (null != subject.getInt("id")) {
			subject.update();
		} else {
			subject.save();
		}
		redirect("/admin/subject");
	}

	public void delete() {
		Subject subject = Subject.dao.findById(getParaToInt());
		subject.delete();
		redirect("/admin/subject");
	}

	public void edit() {
		Subject subject = Subject.dao.findById(getParaToInt(0));
		setAttr("subject", subject);
		_form();
	}

	private void _form() {
		setAttr("examTypeList", ExamType.dao.getTopList());
		render("/admin/subject/subject.html");
	}

	public void listJson() {
		Map<String, Object> ret = new HashMap<String, Object>();
		Integer examTypeId = getParaToInt("examTypeId");
		ret.put("data", Subject.dao.findByExamTypeId(examTypeId));
		renderJson(ret);
	}

}
