package com.ollearning.testing.controller;

import java.util.List;

import com.jfinal.aop.Before;
import com.ollearning.common.controller.BaseController;
import com.ollearning.interceptors.AdminACLInterceptor;
import com.ollearning.testing.model.ExamType;
import com.ollearning.testing.model.Subject;

@Before(AdminACLInterceptor.class)
public class ExamTypeController extends BaseController {

	public void index() {
		setAttr("examTypeList", ExamType.dao.getTopList());
		render("/admin/examType/list.html");
	}

	public void add() {
		setAttr("parentMenu", ExamType.dao.findById(getParaToInt()));
		_form();
	}

	public void save() {
		String returnUrl = getPara("returnUrl");
		ExamType examType = getModel(ExamType.class);
		if (null != examType.getInt("id")) {
			examType.update();
		} else {
			examType.save();
		}
		if (!isNullOrEmpty(returnUrl)) {
			redirect(returnUrl);
		} else {
			redirect("/admin/examType");
		}
	}

	public void delete() {
		ExamType examType = ExamType.dao.loadModel(getParaToInt());
		examType.delete();

		Integer parentId = examType.getInt("parentId");
		if (null == parentId) {
			index();
			return;
		}
		if (parentId == -1) {
			index();
		} else {
			redirect("/admin/examType/child/" + parentId);
		}
	}

	public void edit() {
		setAttr("parentMenu", ExamType.dao.findById(getParaToInt(1)));
		ExamType examType = ExamType.dao.findById(getParaToInt(0));
		setAttr("examType", examType);
		_form();
	}

	/**
	 * 转至子类列表
	 */
	public void child() {
		ExamType.dao.removeAllPageCache();
		ExamType examType = ExamType.dao.findById(getParaToInt());
		setAttr("examType", examType);
		setAttr("examTypeList", examType.getChildren());
		setAttr("actionUrl", "/admin/examType/child/" + examType.getInt("id")
				+ "-");
		render("/admin/examType/child.html");
	}

	public void parentJson() {
		ExamType.dao.removeAllPageCache();
		List<ExamType> list = ExamType.dao.getTopList();
		renderJson(list);
	}

	public void childJson() {
		ExamType.dao.removeAllPageCache();
		Integer pid = getParaToInt("id");
		ExamType examType = ExamType.dao.loadModel(pid);
		renderJson(examType.getChildren());
	}

	private void _form() {
		setAttr("parentMenus", ExamType.dao.getTopList());
		render("/admin/examType/examType.html");
	}
}
