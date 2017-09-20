package com.ollearning.user.model;

import java.util.List;

import com.jfinal.plugin.activerecord.Page;
import com.ollearning.common.jfinal.Const;
import com.ollearning.common.jfinal.Model;

public class UserLevel extends Model<UserLevel> {

	private static final long serialVersionUID = -8659587495726973166L;

	public static UserLevel dao = new UserLevel();

	public UserLevel() {
		super("userLevel");
	}

	public Page<UserLevel> getPage(Integer pageNumber) {
		return paginate(pageNumber, Const.PAGE_SIZE_FOR_ADMIN, "select *",
				"from user_level");
	}

	public List<UserLevel> getAll() {
		return find("select * from user_level");
	}

}
