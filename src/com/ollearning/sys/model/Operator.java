package com.ollearning.sys.model;

import java.util.List;

import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.ehcache.CacheKit;
import com.ollearning.common.jfinal.Const;
import com.ollearning.common.jfinal.Model;

public class Operator extends Model<Operator> {
	private static final long serialVersionUID = -6044900160431214815L;

	private static final String OPERATOR_CACHE = "operator";
	private static final String OPERATOR_CACHE_LIST = "operatorList";

	public static Operator dao = new Operator();

	private boolean checked; // 前台使用

	public Operator() {
		super(OPERATOR_CACHE);
	}

	public Page<Operator> getPage(Integer pageNumber) {
		Page<Operator> rolePage = dao.paginateByCache(OPERATOR_CACHE_LIST,
				"roleList", pageNumber, Const.PAGE_SIZE_FOR_ADMIN, "select *",
				"from operator order by id asc");
		return loadModelPage(rolePage);
	}

	public Operator get(String loginName, String loginPwd) {
		return dao.findFirst(
				"select * from operator where loginName=? and loginPwd=?",
				loginName, loginPwd);
	}

	public List<Operator> findByRoleId(Integer roleId) {
		return find("select * from operator where roleId=?", roleId);
	}

	// 根据文档分类ID查询管理员列表
	public List<Operator> findByDocTypeId(Integer docTypeId) {
		return find(
				"select * from operator where id in ( select managerId from doc_type_manager where docTypeId=?)",
				docTypeId);
	}

	// 根据文档视频分类ID查询管理员列表
	public List<Operator> findByVideoTypeId(Integer videoTypeId) {
		return find(
				"select * from operator where id in ( select managerId from video_type_manager where videoTypeId=?)",
				videoTypeId);
	}

	// 判断当前操作员是否为编辑
	public boolean isEditor() {
		return getInt("roleId") == Integer.parseInt(Dict.dao.getValue(
				"SYS_VALUE", "ROLE_EDITOR_ID"));
	}

	public Role getRole() {
		return Role.dao.get(getInt("roleId"));
	}

	public void removeAllCache() {
		CacheKit.removeAll(OPERATOR_CACHE);
		CacheKit.removeAll(OPERATOR_CACHE_LIST);
	}

	public boolean getChecked() {
		if (null == getBoolean("checked")) {
			super.put("checked", checked);
			return checked;
		}
		this.checked = super.get("checked");
		return checked;
	}

	public boolean isChecked() {
		return getChecked();
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
		super.put("checked", checked);
	}

	@Override
	public boolean equals(Object o) {
		if (null == o)
			return false;
		if (!(o instanceof Operator))
			return false;
		Operator op = (Operator) o;
		if (op.getInt("id").intValue() == getInt("id").intValue()) {
			return true;
		}
		return false;
	}

}
