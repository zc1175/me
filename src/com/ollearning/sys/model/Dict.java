package com.ollearning.sys.model;

import java.util.List;

import com.jfinal.plugin.activerecord.Page;
import com.ollearning.common.jfinal.Const;
import com.ollearning.common.jfinal.Model;

public class Dict extends Model<Dict> {

	private static final long serialVersionUID = 208153434221912108L;
	public static Dict dao = new Dict();

	public Dict() {
		super("dict");
	}

	public Page<Dict> findPage(Integer pageNumber) {
		return paginate(pageNumber, Const.PAGE_SIZE_FOR_ADMIN, "select *",
				"from base_dict order by id desc");
	}

	public String getValue(String type, String item) {
		Dict dict = findFirst(
				"select value from base_dict where type=? and item=?", type,
				item);
		return (null != dict) ? dict.getStr("value") : "";
	}

	public List<Dict> findByType(String type) {
		return find("select * from base_dict where type=?");
	}

}
