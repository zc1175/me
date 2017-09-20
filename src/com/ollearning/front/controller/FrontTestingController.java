package com.ollearning.front.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.ollearning.common.controller.BaseController;
import com.ollearning.common.jfinal.Const;
import com.ollearning.common.util.StringUtil;
import com.ollearning.testing.model.Exam;
import com.ollearning.testing.model.ExamComboDetail;
import com.ollearning.testing.model.ExamType;
import com.ollearning.testing.model.QuestionSub;
import com.ollearning.testing.model.UserAnswer;
import com.ollearning.testing.model.UserAnswerDetail;
import com.ollearning.user.model.User;

/**
 * 前台在线测试模块
 * 
 * @author xingry
 * 
 */
public class FrontTestingController extends BaseController {

	public void index() {
		Integer examTypeId = getParaToInt("examTypeId", -1);
		String time = getPara("time", "-1");
		String type = getPara("type", "-1");
		Integer pageNo = getParaToInt("pageNo", 1);
		setAttr("examTypeId", examTypeId);
		setAttr("time", time);
		type = "testing"; // 模考
		setAttr("type", type);

		Exam.QueryBean qb = Exam.dao.newQueryBean();
		qb.examTypeId = examTypeId;
		qb.time = time;
		qb.type = type;

		// 查询已经发布且生成的，在时间范围内试卷列表
		Page<Exam> examPage = Exam.dao.getExamListForFront(pageNo, qb);
		UserAnswer.dao.checkUserAnswered(examPage.getList(), getLoginCust());
		setAttr("examPage", examPage);
		setAttr("examTypeList", ExamType.dao.getTopList());
		setAttr("actionUrl", "/front/testing/index-");
		render("/front/testing/index.html");
	}

	/**
	 * 开始答题：分练习和模考
	 */
	public void exam() {
		if (null == getLoginCust()) {
			renderHtml("<script>location.href='" + super.getCtxPath()
					+ "/user/login?returnUrl=" + getBasePath()
					+ "/testing/exam/" + getPara() + "';</script>");
			return;
		}

		long timeLeft = 0;
		String pid = getPara();
		Exam exam = Exam.dao.findByPid(pid);
		setSessionAttr("curExamId", exam.getInt("id"));
		setSessionAttr("curExamPid", pid);
		if (null == exam) {
			renderHtml("<p align=center>非法访问!</p>");
			return;
		}

		// 判断是否为练习
		if ("practice".equalsIgnoreCase(exam.getStr("answerType"))) {
			practice(exam);
			return;
		}

		// 判断考试是否已结束
		if (new Date().getTime() > exam.getDate("endTime").getTime()) {
			renderHtml("<script>alert('本场考试已结束!');location.href='"
					+ getCtxPath() + "/testing'</script>");
			return;
		}

		// 对于同步考试，判断是否开始，若未开始则进入倒计进页面
		if (exam.getStr("syncType").indexOf("同步") != -1
				&& new Date().getTime() < exam.getDate("startTime").getTime()) {
			timeLeft = (exam.getDate("startTime").getTime() - new Date()
					.getTime()) / 1000;
			setSessionAttr("timeLeft", timeLeft);
			redirect("/testing/timer");
			return;
		}

		// 保存用户开始考试信息,如果已经进入考试，则读取信息。最后发送开考时间至客户端
		User loginUser = getLoginCust();
		UserAnswer ua = UserAnswer.dao.findUserAnswer(loginUser.getInt("id"),
				exam.getInt("id"));
		// 判断是否已交卷
		if (null != ua && ua.getStr("status").equals(UserAnswer.STATUS_END)) {
			renderHtml("<script>alert('您已参加过本场考试!');location.href='"
					+ getCtxPath() + "/testing'</script>");
			return;
		}

		if (null == ua) {
			ua = UserAnswer.dao.startAnswer(loginUser.getInt("id"),
					exam.getInt("id"), super.getRealIpAddr());
			exam.createExamForUser(ua);
			timeLeft = exam.getInt("timeLength") * 60;
		} else {
			timeLeft = exam.getInt("timeLength")
					* 60
					- ((new Date().getTime() - ua.getDate("startTime")
							.getTime()) / 1000);
		}
		setSessionAttr("userAnswer", ua);
		// 前台题号列表
		setAttr("examStructureHtml", exam.toStructureHtml(ua, false));
		setAttr("timeLeft", timeLeft);
		// ExamCombo combo = ExamCombo.dao.findById(exam.getInt("comboId"));
		// setAttr("combo", combo);
		setAttr("exam", exam);
		render("/front/testing/exam.html");
	}

	// 进入倒计进页面
	public void timer() {
		render("/front/testing/timer.html");
	}

	// 为考生生成试卷
	public void createExam() {
		Map<String, Object> ret = new HashMap<String, Object>();
		try {
			String pid = getPara();
			Exam exam = Exam.dao.findByPid(pid);
			// 判断是否已经开考过，即试卷生成过
			UserAnswer ua = UserAnswer.dao.findUserAnswer(getLoginCust()
					.getInt("id"), exam.getInt("id"));
			if (null == ua) {
				ua = UserAnswer.dao.startAnswer(getLoginCust().getInt("id"),
						exam.getInt("id"), super.getRealIpAddr());
				ua.set("userId", getLoginCust().getInt("id"));
				exam.createExamForUser(ua);
			}
			ret.put("result", "success");
		} catch (Exception ex) {
			ex.printStackTrace();
			ret.put("result", "error");
		}
		renderJson(ret);
	}

	// 交卷
	public void submit() {
		User loginUser = getLoginCust();
		Integer examId = getSessionAttr("curExamId");
		Integer userAnswerId = getParaToInt("userAnswerId");
		if (null == examId) {
			renderHtml("<p align=center>提交失败，请返回重试！</p>");
			return;
		}
		// UserAnswer ua = UserAnswer.dao.findUserAnswer(loginUser.getInt("id"),
		// examId);
		UserAnswer ua = UserAnswer.dao.findById(userAnswerId);
		Exam exam = ua.getExam();
		if ("practice".equals(exam.getStr("answerType"))) {
			int seconds = getParaToInt("time");
			Integer usedSeconds = ua.getInt("usedSeconds");
			usedSeconds = null == usedSeconds ? 0 : usedSeconds;
			ua.set("status", Const.ExamStatus.SUBMIT)
					.set("usedSeconds", usedSeconds + seconds).update();
			setAttr("ua", ua);
			int correctNum = ua.getInt("correctNum");
			int wrongNum = ua.getInt("wrongNum");
			setSessionAttr("submitStatus", "本套试题已答 " + (correctNum + wrongNum)
					+ "题，正确：" + correctNum + "，错误：" + wrongNum);
		} else {
			if (null != ua) {
				double score = calScore(loginUser.getInt("id"), examId);
				setSessionAttr("score", score);
				setSessionAttr("submitStatus", "交卷成功! 得分：" + score);
			}
		}
		setSessionAttr("exam", exam);
		setSessionAttr("examTypeName",
				Exam.dao.loadModel(examId).getStr("examTypeName"));
		setSessionAttr("examName", Exam.dao.loadModel(examId).getStr("name"));
		redirect(getBasePath() + "/testing/submited");
	}

	public void submited() {
		try {
			Exam exam = getSessionAttr("exam");
			// 对于test用户不保存考试记录
			if ("test".equalsIgnoreCase(getLoginCust().getStr("loginName"))) {
				UserAnswer ua = UserAnswer.dao.findUserAnswer(getLoginCust()
						.getInt("id"), exam.getInt("id"));
				if (null != ua) {
					ua.delete();
				}
			}
		} catch (Exception e) {
		}

		render("/front/testing/submited.html");
	}

	// 评分
	private double calScore(Integer userId, Integer examId) {
		double score = 0.0;
		UserAnswer ua = UserAnswer.dao.findUserAnswer(userId, examId);
		score = ua.calScore();
		return score;
	}

	// 进入练习界面
	public void practice(Exam exam) {
		String pid = getPara();
		// 判断是否已经开考过，即试卷生成过
		UserAnswer ua = UserAnswer.dao.findUserAnswer(
				getLoginCust().getInt("id"), exam.getInt("id"));
		if (null == ua) {
			ua = UserAnswer.dao.startAnswer(getLoginCust().getInt("id"),
					exam.getInt("id"), super.getRealIpAddr());
			exam.createExamForUser(ua);
		}
		setSessionAttr("userAnswer", ua);
		setAttr("exam", exam);
		setAttr("examStructureHtml", exam.toStructureHtml(ua, true));
		render("/front/testing/practice.html");
	}

	// 练习题，最后一次答案清空 ,AJAX 返回
	public void reset() {
		Exam exam = Exam.dao.findById(getParaToInt("examId"));
		UserAnswer ua = UserAnswer.dao.findUserAnswer(
				getLoginCust().getInt("id"), exam.getInt("id"));
		if (null != ua) {
			ua.resetAnswer();
		}
		setAttr("exam", exam);
		setAttr("examStructureHtml", exam.toStructureHtml(ua, true));
		redirect(getBasePath() + "/testing/exam/" + exam.getStr("pid"));
	}

}
