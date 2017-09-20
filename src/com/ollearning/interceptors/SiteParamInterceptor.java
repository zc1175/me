package com.ollearning.interceptors;

import java.util.List;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import com.ollearning.site.model.NavMenu;
import com.ollearning.site.model.SiteInfo;

public class SiteParamInterceptor implements Interceptor {

	@Override
	public void intercept(Invocation ai) {
		// load system data
		loadSystemData(ai);

		ai.invoke();

	}

	private void loadSystemData(Invocation ai) {

		Controller controller = ai.getController();

		// 加载站点信息
		SiteInfo siteInfo = controller.getSessionAttr("siteInfo");
		if (null == siteInfo) {
			controller.setSessionAttr("siteInfo", SiteInfo.dao.findById(1));
		}

		List<NavMenu> menus = controller.getSessionAttr("menus");
		if (null == menus) {
			controller.setSessionAttr("menus", NavMenu.dao.getList());
		}

	}

}
