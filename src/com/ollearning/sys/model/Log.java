package com.ollearning.sys.model;

import com.jfinal.plugin.activerecord.Page;
import com.ollearning.common.jfinal.Const;
import com.ollearning.common.jfinal.Model;

public class Log extends Model<Log> {
	public static Log dao = new Log();

	public Log() {
		super("LOG_CACHE");
	}

	public Page<Log> getPage(Integer pageNumber) {
		Page<Log> logPage = dao.paginate(pageNumber, Const.PAGE_SIZE_FOR_ADMIN,
				"select *", "from sys_log order by id desc");
		return loadModelPage(logPage);
	}

}
