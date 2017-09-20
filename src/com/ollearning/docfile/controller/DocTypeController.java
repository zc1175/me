package com.ollearning.docfile.controller;

import java.util.List;

import com.jfinal.aop.Before;
import com.ollearning.common.controller.BaseController;
import com.ollearning.docfile.model.DocType;
import com.ollearning.interceptors.AdminACLInterceptor;
import com.ollearning.sys.model.Dict;
import com.ollearning.sys.model.Operator;

@Before(AdminACLInterceptor.class)
public class DocTypeController extends BaseController {

	public void index() {
		setAttr("docPage", DocType.dao.getParents(getParaToInt(1, 1)));
		setAttr("actionUrl", "/admin/docType/index-");
		render("/admin/docType/list.html");
	}

	public void child() {
		int parentId = getParaToInt();
		setAttr("docType", DocType.dao.findById(parentId));
		setAttr("docList", DocType.dao.getListByParentId(parentId,
				getLoginOperator().getInt("id")));
		setAttr("parentId", parentId);
		render("/admin/docType/child.html");
	}

	public void add() {
		int parentId = getParaToInt(0, -1);
		setAttr("docType", new DocType().set("parentId", parentId));
		List<Operator> managerList = Operator.dao.findByRoleId(Integer
				.parseInt(Dict.dao.getValue("SYS_VALUE", "ROLE_EDITOR_ID")));
		setAttr("managerList", managerList);
		_form();
	}

	public void edit() {
		setAttr("docType", DocType.dao.findById(getParaToInt()));
		List<Operator> managerList = Operator.dao.findByRoleId(Integer
				.parseInt(Dict.dao.getValue("SYS_VALUE", "ROLE_EDITOR_ID")));
		List<Operator> checkedManagerList = DocType.dao
				.findById(getParaToInt()).getManagerList();
		for (Operator op : managerList) {
			if (checkedManagerList.contains(op)) {
				op.setChecked(true);
			}
		}
		setAttr("managerList", managerList);
		_form();
	}

	public void save() {
		DocType docType = getModel(DocType.class);
		String[] managerIds = getParaValues("managerId");

		docType.set("deleted", 0);
		if (null != docType.getInt("id")) {
			docType.update();
		} else {
			try {
				if (null == docType.getInt("parentId")
						|| -1 == docType.getInt("parentId")) {
					docType.set("level", 1).set("childNum", 0);
				} else {
					docType.set("level",
							DocType.dao.findById(docType.getInt("parentId"))
									.getInt("level") + 1);
					DocType parent = DocType.dao.findById(docType
							.getInt("parentId"));
					parent.set("childNum", parent.getInt("childNum") + 1)
							.update();
				}
			} catch (Exception e) {
			}
			docType.set("docNum", 0).save();
		}
		docType.clearManager();
		if (null != managerIds) {
			for (String managerId : managerIds) {
				docType.addManager(Integer.parseInt(managerId));
			}
		}
		if (-1 == docType.getInt("parentId")) {
			redirect("/admin/docType");
		} else {
			redirect("/admin/docType/child/" + docType.getInt("parentId"));
		}

	}

	public void delete() {
		DocType.dao.findById(getParaToInt()).delete();
		redirect("/admin/docType");
	}

	private void _form() {
		setAttr("parentList",
				DocType.dao.getParentList(getLoginOperator().getInt("id")));
		render("/admin/docType/docType.html");
	}

	// 异步获取子分类列表
	public void getChildJson() {
		renderJson(DocType.dao.getListByParentId(getParaToInt(0),
				getLoginOperator().getInt("id")));
	}

}
