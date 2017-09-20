package com.ollearning.interceptors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.ollearning.common.jfinal.Const;
import com.ollearning.common.util.SessionUtil;
import com.ollearning.site.model.SiteInfo;

public class FrontInterceptor implements Interceptor {

	@Override
	public void intercept(Invocation inv) {

		String actionKey = inv.getActionKey();
		if (actionKey.indexOf("/admin") != -1) {
			inv.invoke();
			return;
		}

		HttpSession session = inv.getController().getSession();
		session.setAttribute("loginIp", getRealIpAddr(inv.getController()
				.getRequest()));
		session.setAttribute("siteInfo", SiteInfo.dao.loadModel(1));
		SessionUtil.addSession(session);
		SessionUtil.setActPath(session.getId(), actionKey);

		inv.invoke();
	}

	public String getRealIpAddr(HttpServletRequest req) {
		String ip = req.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip))
			ip = req.getHeader("Proxy-Client-IP");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip))
			ip = req.getHeader("WL-Proxy-Client-IP");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip))
			ip = req.getRemoteAddr();
		return ip;
	}

}
