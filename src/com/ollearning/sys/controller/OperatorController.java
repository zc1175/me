package com.ollearning.sys.controller;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Page;
import com.ollearning.common.controller.BaseController;
import com.ollearning.common.util.MD5;
import com.ollearning.interceptors.AdminACLInterceptor;
import com.ollearning.sys.model.Operator;
import com.ollearning.sys.model.Role;

@Before(AdminACLInterceptor.class)
public class OperatorController extends BaseController {

	public void index() {
		Operator.dao.removeAllCache();
		Page<Operator> operatorPage = Operator.dao.getPage(getParaToInt(1, 1));
		setAttr("operatorPage", operatorPage);
		setAttr("actionUrl", "/admin/operator/index-");
		render("/admin/operator/list.html");
	}

	public void add() {
		_form();
	}

	public void save() {
		Operator operator = getModel(Operator.class);
		if (null != operator.getInt("id")) {
			// 判断若未修改密码,则使用原有的
			if (null == operator.getStr("loginPwd")
					|| operator.getStr("loginPwd").trim().length() == 0) {
				Operator op = Operator.dao.findById(operator.getInt("id"));
				operator.set("loginPwd", op.get("loginPwd"));
			} else {
				operator.set("loginPwd",
						MD5.GetMD5Code(operator.getStr("loginPwd")));
			}
			operator.update();
		} else {
			operator.set("loginPwd",
					MD5.GetMD5Code(operator.getStr("loginPwd")));
			operator.save();
		}
		redirect("/admin/operator");
	}

	public void delete() {
		Operator.dao.deleteById(getParaToInt());
		index();
	}

	public void edit() {
		Operator operator = Operator.dao.findById(getParaToInt());
		setAttr("operator", operator);
		_form();
	}

	public void view() {
		setAttr("roleList", Role.dao.getList());
		setAttr("operator", Operator.dao.findById(getParaToInt()));
		render("/admin/operator/view.html");
	}

	private void _form() {
		setAttr("roleList", Role.dao.getList());
		render("/admin/operator/operator.html");
	}
}
