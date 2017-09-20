package com.ollearning.user.model;

import java.util.List;

import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Page;
import com.ollearning.common.jfinal.Const;
import com.ollearning.common.jfinal.Model;
import com.ollearning.common.util.MD5;

public class User extends Model<User> {

	private static final long serialVersionUID = 6942055467872745409L;

	public static User dao = new User();

	public User() {
		super("user");
	}

	public User findByLoginName(String loginName) {
		return findFirst("select * from users where loginName=?", loginName);
	}

	public User login(String loginName, String loginPwd) {
		loginPwd = MD5.GetMD5Code(loginPwd);
		String sql = "select * from users where loginName=? and loginPwd=? and status=1";
		List<User> list = find(sql, loginName, loginPwd);
		if (null == list || list.size() == 0)
			return null;
		else
			return list.get(0);
	}

	public Page<User> getPage(Integer pageNumber, User user) {
		return paginate(pageNumber, Const.PAGE_SIZE_FOR_ADMIN, "select *",
				getCondition(user));
	}

	public String getCondition(User user) {
		String sql = "from users where 1=1";
		if (StrKit.notBlank(user.getStr("loginName"))) {
			sql += " loginName like '%" + user.getStr("loginName") + "%'";
		}
		sql += " order by id desc";
		return sql;
	}

	public UserLevel getUserLevel() {
		return UserLevel.dao.findById(getInt("userLevelId"));
	}

	public String getStatusStr() {
		return 1 == getInt("status") ? "<font color=green>正常</font>"
				: "<font color=red>冻结</font>";

	}

}
