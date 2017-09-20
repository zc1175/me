package com.ollearning.site.model;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.plugin.activerecord.Page;
import com.ollearning.common.jfinal.Const;
import com.ollearning.common.jfinal.Model;

/**
 * @author XINGRY
 * 
 */
public class NavMenu extends Model<NavMenu> {

	private static final long serialVersionUID = -4565169447737257022L;
	private static final String MENU_CACHE = "menuCache";
	private static final String MENU_CACHE_LIST = "menuCacheList";
	private static final String MENU_CACHE_LIST_CHILD = "menuCacheListChild";
	private static final String MENU_CACHE_TOP = "menuCacheTop";
	private static final String MENU_CACHE_CHILD = "menuCacheList";

	public static NavMenu dao = new NavMenu();

	public NavMenu() {
		super(MENU_CACHE);
	}

	public List<NavMenu> getList() {
		return find("select * from nav_menu where parentId is null and visible=1 order by rank asc");
	}

	public List<NavMenu> getChilds() {
		List<NavMenu> list = find(
				"select * from nav_menu where parentId=? and visible=1 order by rank asc",
				getInt("id"));
		if (null == list)
			list = new ArrayList<NavMenu>();
		return list;

	}

	public Page<NavMenu> getChildPageForAdmin(Integer pageNumber) {
		Page<NavMenu> page = dao.paginate(pageNumber,
				Const.PAGE_SIZE_FOR_ADMIN, "select *",
				"from nav_menu where parentId=? ", getInt("id"));
		return loadModelPage(page);
	}

	public List<NavMenu> getTopCategory(Integer companyId) {
		String sqlCondition = (null != companyId) ? " and companyId="
				+ companyId : " and companyId is null";
		return dao.find("select * from nav_menu where 1=1 " + sqlCondition
				+ " and (parentId=-1 or parentId is null)");
	}

	public List<NavMenu> getChildCategory() {
		return dao.find("select * from nav_menu where parentId=? ",
				getInt("id"));
	}

	@Override
	public boolean update() {
		super.update();
		if (null != getInt("parentId")) {
			NavMenu parent = findById(getInt("parentId"));
			parent.set("childNum", parent.getInt("childNum") + 1).update();
		}
		removeAllPageCache();
		return true;
	}

	public void removeAllPageCache() {
		super.removeAllPageCache(MENU_CACHE, MENU_CACHE_LIST,
				MENU_CACHE_LIST_CHILD);
	}

}
