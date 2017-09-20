package com.ollearning.stat;

import java.util.HashMap;
import java.util.Map;

import com.jfinal.aop.Before;
import com.jfinal.kit.PathKit;
import com.ollearning.common.controller.BaseController;
import com.ollearning.common.util.FreemarkerUtil;
import com.ollearning.interceptors.AdminACLInterceptor;
import com.ollearning.testing.model.Exam;
import com.ollearning.testing.model.UserAnswer;
import com.ollearning.user.model.User;

/**
 * 成绩统计
 * 
 * @author xingry
 * 
 */
@Before(AdminACLInterceptor.class)
public class StatController extends BaseController {

	// 成绩统计
	public void score() {
		UserAnswer bean = new UserAnswer();
		Integer examId = getParaToInt("examId");
		String loginName = getPara("loginName");
		String orderBy = getPara("orderBy", " id desc");
		bean.set("examId", examId);
		bean.setLoginName(loginName);
		bean.setOrderBy(orderBy);
		bean.set("status", UserAnswer.STATUS_END);

		setAttr("examId", examId);
		setAttr("loginName", loginName);
		setAttr("orderBy", orderBy);

		setAttr("examList", Exam.dao.findStartedExams());
		setAttr("answerPage", UserAnswer.dao.getPage(getParaToInt(1, 1), bean));
		setAttr("actionUrl", "/admin/stat/score/index-");

		render("/admin/stat/score.html");
	}

	// 查询用户答题明细
	public void scoreDetail() {
		int examId = getParaToInt("examId");
		int userId = getParaToInt("userId");
		Exam exam = Exam.dao.loadModel(examId);
		setAttr("userAnswer", UserAnswer.dao.findUserAnswer(userId, examId));
		setAttr("exam", exam);
		setAttr("user", User.dao.loadModel(userId));
		setAttr("structure", exam.getStructure(userId));
		render("/admin/stat/scoreDetail.html");
	}

	// 导出为word
	public void exportWord() {
		int examId = getParaToInt("examId");
		int userId = getParaToInt("userId");
		Exam exam = Exam.dao.loadModel(examId);
		UserAnswer ua = UserAnswer.dao.findUserAnswer(userId, examId);

		Map<String, Object> data = new HashMap<String, Object>();
		data.put("ExamName", exam.getStr("name"));
		data.put("UserName", User.dao.loadModel(userId).getStr("name"));
		data.put("Score", ua.getDouble("score"));
		data.put("structure", exam.getStructure(userId));

		String fileAbsPath = "/WEB-INF/exam_files/" + examId + "_" + userId
				+ ".doc";
		String filePath = PathKit.getWebRootPath() + fileAbsPath;

		String templateName = "ExamTemplate.ftl";

		FreemarkerUtil.exportWord(data, templateName, filePath);

		renderFile(fileAbsPath);

	}

}
