package com.ollearning.sys.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.ehcache.CacheKit;
import com.ollearning.common.jfinal.Const;
import com.ollearning.common.jfinal.Model;

public class Role extends Model<Role> {
	private static final long serialVersionUID = -7927398268802961909L;

	public static final String ROLE_CACHE = "role";
	public static final String ROLE_CACHE_LIST = "roleList";

	public Role() {
		super(ROLE_CACHE);
	}

	public static final Role dao = new Role();

	public List<Role> getList() {
		return dao.find("select * from sys_role order by id asc");
	}

	public Page<Role> getPage(Integer pageNumber) {
		Page<Role> rolePage = dao.paginateByCache(ROLE_CACHE_LIST, "roleList",
				pageNumber, Const.PAGE_SIZE_FOR_ADMIN, "select *",
				"from sys_role order by id asc");
		return loadModelPage(rolePage);
	}

	public Role get(int id) {
		return loadModel(id);
	}

	/**
	 * 获取角色对应权限
	 * 
	 * @return
	 */
	public List<Right> getRights() {
		return Right.dao
				.find("select * from sys_right where id in ( select rightId from sys_role_right where roleId=? ) order by code,rank",
						get("id"));
	}

	public Map<Right, List<Right>> getMenus() {
		Map<Right, List<Right>> map = new HashMap<Right, List<Right>>();
		List<Right> rights = Right.dao
				.find("select * from sys_right where id in ( select rightId from sys_role_right where roleId=? ) and length(code)=2 order by rank",
						get("id"));
		for (Right right : rights) {
			List<Right> childs = Right.dao
					.find("select * from sys_right where id in ( select rightId from sys_role_right where roleId=? ) and length(code)>2  and left(code,2)=? order by rank",
							get("id"), right.getStr("code"));
			map.put(right, childs);
		}
		return map;
	}

	/**
	 * 清除原分配的所有权限
	 */
	public void delRights() {
		Db.update("delete from sys_role_right where roleId=?", get("id"));
	}

	public void removeAllCache() {
		CacheKit.removeAll(ROLE_CACHE);
		CacheKit.removeAll(ROLE_CACHE_LIST);
	}

}
