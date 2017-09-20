package com.ollearning.interceptors;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.ollearning.common.controller.BaseController;
import com.ollearning.common.jfinal.Const;
import com.ollearning.common.util.DateUtil;
import com.ollearning.common.util.SysValidate;
import com.ollearning.sys.model.Log;
import com.ollearning.sys.model.Operator;

public class LogInterceptor implements Interceptor {

	@Override
	public void intercept(Invocation ai) {

		try {
			SysValidate.validate();
		} catch (Exception e1) {
			ai.getController().renderText(Const.SYS_ERR_MSG);
			return;
		}

		try {
			BaseController controller = (BaseController) ai.getController();
			String actionKey = ai.getActionKey();
			String methodName = ai.getMethodName();
			HttpServletRequest request = controller.getRequest();

			if (!"POST".equals(request.getMethod())) {
				ai.invoke();
				return;
			}

			Operator loginOperator = controller
					.getSessionAttr(Const.OPERATOR_SESSION_KEY);

			Log log = new Log();
			log.set("module", ai.getControllerKey())
					.set("type", methodName)
					.set("url", actionKey + "/" + methodName)
					.set("operator",
							(null != loginOperator) ? loginOperator
									.getStr("loginName") : "")
					.set("ctime", DateUtil.curTime())
					.set("description", getRequestParams(controller, request));

			try {
				ai.invoke();
				log.set("result", "200");
			} catch (Exception ex) {
				ex.printStackTrace();
				log.set("result", "500," + ex.getMessage());
			}
			// log.save();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String getRequestParams(BaseController controller,
			HttpServletRequest request) {
		StringBuffer buf = new StringBuffer();
		String ip = controller.getRealIpAddr();
		buf.append("[IP:");
		buf.append(ip);
		buf.append("]");
		Enumeration<String> enums = request.getParameterNames();
		for (; enums.hasMoreElements();) {
			String paramName = enums.nextElement();
			String value = request.getParameter(paramName);
			buf.append(paramName + ":" + value);
			buf.append(" ");
		}
		return buf.toString();
	}

}
