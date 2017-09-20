package com.ollearning.sys.model;

import java.util.List;

import com.jfinal.plugin.activerecord.Page;
import com.ollearning.common.jfinal.Const;
import com.ollearning.common.jfinal.Model;

public class Broadcast extends Model<Broadcast> {

	private static final long serialVersionUID = -380339892863493901L;

	public static Broadcast dao = new Broadcast();

	public Broadcast() {
		super("broadcast");
	}

	public Page<Broadcast> getPage(int pageNumber) {
		return paginate(pageNumber, Const.PAGE_SIZE_FOR_ADMIN, "select *",
				"from broadcast order by id desc");
	}

	public List<Broadcast> findForIndex() {
		return find("select * from broadcast where  endTime>now() and date_add(startTime,interval 3 day)<now() order by id desc limit 5");

	}
}
