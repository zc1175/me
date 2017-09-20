package com.ollearning.interceptors;

import java.util.List;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import com.ollearning.common.jfinal.Const;
import com.ollearning.sys.model.Operator;
import com.ollearning.sys.model.Right;

public class AdminACLInterceptor implements Interceptor {

	private boolean isLogin(Controller controller) {
		Object obj = controller.getSessionAttr(Const.OPERATOR_SESSION_KEY);
		Object obj2 = controller.getSessionAttr(Const.COMPANY_SESSION_KEY);
		if (null == obj && null == obj2) {
			return false;
		}
		return true;
	}

	@Override
	public void intercept(Invocation ai) {

		// check licence data in memory
		// if (StrKit.isBlank(LicenceManager.LICENCE_DATA)
		// || Calendar.getInstance().getTimeInMillis() >
		// LicenceManager.INVALID_TIME) {
		// throw new RuntimeException(LicenceManager.ERR_MSG);
		// }

		Controller controller = ai.getController();
		if (!isLogin(controller)) {
			controller
					.renderHtml("<script>alert('您未登录或登录已过期,请重新登录!');parent.location.href='"
							+ controller.getRequest().getContextPath()
							+ "/admin/login';</script>");
			return;
		}

		Object obj = controller.getSessionAttr(Const.OPERATOR_SESSION_KEY);
		if (null != obj) {
			// 判断当前登录操作员是否具有此操作权限
			Operator op = (Operator) obj;
			String actionKey = ai.getActionKey();
			if (!"/admin/main".equals(actionKey)) {
				List<Right> rights = op.getRole().getRights();
				for (Right right : rights) {
					if (actionKey.startsWith(right.getStr("url"))) {
						ai.invoke();
						return;
					}
				}
			} else {
				ai.invoke();
				return;
			}
		} else {
			ai.invoke();
		}
		// controller.render("/common/403.html");

	}

}
