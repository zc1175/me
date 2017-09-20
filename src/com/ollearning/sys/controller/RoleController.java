package com.ollearning.sys.controller;

import java.util.List;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Page;
import com.ollearning.common.controller.BaseController;
import com.ollearning.interceptors.AdminACLInterceptor;
import com.ollearning.sys.model.Right;
import com.ollearning.sys.model.Role;
import com.ollearning.sys.model.RoleRight;

@Before(AdminACLInterceptor.class)
public class RoleController extends BaseController {

	public void index() {
		Role.dao.removeAllCache();
		Page<Role> rolePage = Role.dao.getPage(getParaToInt(1, 1));
		setAttr("rolePage", rolePage);
		render("/admin/role/list.html");
	}

	public void add() {
		_form();
	}

	public void save() {
		Role role = getModel(Role.class);
		if (null != role.get("id", null)) {
			role.update();
		} else {
			role.save();
		}
		redirect("/admin/role");
	}

	public void delete() {
		Role.dao.deleteById(getParaToInt());
		index();
	}

	public void edit() {
		Role role = Role.dao.findById(getParaToInt());
		setAttr("role", role);
		_form();
	}

	/**
	 * 转至指派权限页面
	 */
	public void assign() {
		Role role = Role.dao.findById(getParaToInt());
		List<Right> assignedRights = role.getRights();
		List<Right> allRights = Right.dao.getList();
		for (Right r : allRights) {
			for (Right rt : assignedRights) {
				if (r.equals(rt)) {
					r.setChecked(true);
				}
			}
		}
		setAttr("role", role);
		setAttr("rights", allRights);
		render("/admin/role/assign.html");
	}

	/**
	 * 保存指派的权限
	 */
	public void saveAssign() {
		int roleId = getParaToInt("role.id");
		Role role = Role.dao.findById(roleId);
		role.delRights();

		String[] rightIds = getParaValues("rightChecked");
		for (String rightId : rightIds) {
			RoleRight rr = new RoleRight();
			rr.set("roleId", roleId);
			rr.set("rightId", rightId);
			rr.save();
		}
		redirect("/admin/role");
	}

	private void _form() {
		render("/admin/role/role.html");
	}

}
