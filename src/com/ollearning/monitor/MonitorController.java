package com.ollearning.monitor;

import com.jfinal.aop.Before;
import com.ollearning.common.controller.BaseController;
import com.ollearning.interceptors.AdminACLInterceptor;
import com.ollearning.testing.model.Exam;
import com.ollearning.testing.model.UserAnswer;

/**
 * 实时监控
 * 
 * @author xingry
 * 
 */
@Before(AdminACLInterceptor.class)
public class MonitorController extends BaseController {

	// 用户答题监控
	public void testing() {
		UserAnswer bean = new UserAnswer();
		Integer examId = getParaToInt("examId");
		String loginName = getPara("loginName");
		bean.set("examId", examId);
		bean.setLoginName(loginName);
		bean.set("status", UserAnswer.STATUS_START);
		// bean.setAnswerType("testing");

		setAttr("examId", examId);
		setAttr("loginName", loginName);

		setAttr("examList", Exam.dao.findStartedExams());
		setAttr("answerPage", UserAnswer.dao.getPage(getParaToInt(1, 1), bean));
		setAttr("actionUrl", "/admin/monitor/testing/index-");

		render("/admin/monitor/testing.html");
	}

}
