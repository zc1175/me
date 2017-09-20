package com.ollearning.sys.model;

import java.util.List;

import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.ehcache.CacheKit;
import com.ollearning.common.jfinal.Const;
import com.ollearning.common.jfinal.Model;

/**
 * +------------+--------------+------+-----+---------+----------------+ | Field
 * | Type | Null | Key | Default | Extra |
 * +------------+--------------+------+-----+---------+----------------+ | id |
 * bigint(20) | NO | PRI | NULL | auto_increment | | code | varchar(50) | NO | |
 * NULL | | | parentCode | varchar(50) | YES | | NULL | | | type | varchar(50) |
 * YES | | NULL | | | text | varchar(50) | YES | | NULL | | | url | varchar(100)
 * | YES | | NULL | | | tip | varchar(50) | YES | | NULL | |
 * +------------+--------------+------+-----+---------+----------------+
 * 
 * +---------+------------+------+-----+---------+----------------+ | Field |
 * Type | Null | Key | Default | Extra |
 * +---------+------------+------+-----+---------+----------------+ | id |
 * bigint(20) | NO | PRI | NULL | auto_increment | | roleId | bigint(20) | YES |
 * | NULL | | | rightId | bigint(20) | YES | | NULL | |
 * +---------+------------+------+-----+---------+----------------+
 * 
 * 
 */
public class Right extends Model<Right> {
	private static final long serialVersionUID = 4478014805710460892L;

	private static final String RIGHT_CACHE = "right";
	private static final String RIGHT_CACHE_LIST = "rightList";
	private boolean checked;

	public Right() {
		super(RIGHT_CACHE);
	}

	public static final Right dao = new Right();

	public List<Right> getList() {
		return Right.dao.find("select * from sys_right order by code asc");
	}

	public Page<Right> getPage(Integer pageNumber) {
		Page<Right> rolePage = dao.paginateByCache(RIGHT_CACHE_LIST,
				"roleList", pageNumber, Const.PAGE_SIZE_FOR_ADMIN, "select *",
				"from sys_right order by code asc");
		return loadModelPage(rolePage);
	}

	public List<Right> getChildRights() {
		return Right.dao
				.find("select * from sys_right where left(code,2)=? and length(code)>2 order by code,rank asc",
						getStr("code"));
	}

	public void removeAllCache() {
		CacheKit.removeAll(RIGHT_CACHE);
		CacheKit.removeAll(RIGHT_CACHE_LIST);
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	public boolean isChecked() {
		return checked;
	}

}
