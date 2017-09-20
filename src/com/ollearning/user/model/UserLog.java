package com.ollearning.user.model;

import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Page;
import com.ollearning.common.jfinal.Const;
import com.ollearning.common.jfinal.Model;

public class UserLog extends Model<UserLog> {

	public static final String RESOURCE_TYPE_VIDEO = "video";
	public static final String RESOURCE_TYPE_DOCUMENT = "document";
	public static final String RESOURCE_TYPE_PRACTICE = "practice";
	public static final String RESOURCE_TYPE_EXAM = "exam";
	public static final String RESOURCE_TYPE_TRAINING = "training";

	private static final long serialVersionUID = 5237530388246955364L;
	public static UserLog me = new UserLog();

	public UserLog() {
		super("userLog");
	};

	public Page<UserLog> findByPage(int pageNumber, User user) {
		return paginate(pageNumber, Const.PAGE_SIZE_FOR_ADMIN, "select *",
				getCondition(user));
	}

	public String getCondition(User user) {
		String sql = "from user_logs where 1=1";
		if (null != user) {
			sql += " and userId in ( select id from users where 1=1";
			if (null != user.get("id")) {
				sql += " and id=" + user.getInt("id");
			}
			if (StrKit.notBlank(user.getStr("name"))) {
				sql += " and name like '%" + user.getStr("name") + "%'";
			}
			sql += ")";
		}
		return sql;
	}

	public void add(String resourceType, String resourceName,
			String resourceId, String reourceUrl) {
		try {
			new UserLog().set("resourceType", resourceType)
					.set("resourceName", resourceName)
					.set("resourceId", resourceId)
					.set("resourceUrl", reourceUrl).save();
		} catch (Exception e) {
		}
	}

}
