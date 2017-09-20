package com.ollearning.testing.model;

import java.util.Date;
import java.util.List;

import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.ollearning.common.jfinal.Const;
import com.ollearning.common.jfinal.Model;
import com.ollearning.common.util.StringUtil;
import com.ollearning.user.model.User;

public class UserAnswer extends Model<UserAnswer> {

	private static final long serialVersionUID = 8342414973170512003L;

	public static String STATUS_START = "已开考";
	public static String STATUS_END = "已交卷";

	public static UserAnswer dao = new UserAnswer();
	private String loginName; // 前台查询条件
	private String answerType; // 前台查询条件，试卷类型（机考，练习 ）
	private String orderBy = "id desc"; // 前台查询条件

	public UserAnswer() {
		super("userAnswer");
	}

	public Page<UserAnswer> getPage(Integer pageNumber, UserAnswer bean) {
		return paginate(pageNumber, Const.PAGE_SIZE_FOR_ADMIN, "select *",
				getCondition(bean));
	}

	public void checkUserAnswered(List<Exam> examList, User user) {
		if (null == user)
			return;
		for (Exam exam : examList) {
			String sql = "select * from useranswer where examId=? and userId=? order by id desc";
			UserAnswer ua = findFirst(sql, exam.getInt("id"), user.getInt("id"));
			if (null != ua) {
				exam.setAnswered(true);
				if (Const.ExamStatus.SUBMIT.equalsIgnoreCase((ua
						.getStr("status")))) {
					exam.setAnswerSubmit(true);
				} else {
					exam.setAnswerSubmit(false);
				}
			}
		}
	}

	private String getCondition(UserAnswer bean) {
		String sql = " from useranswer where 1=1";
		if (null != bean.getInt("examId") && bean.getInt("examId") > 0) {
			sql += " and examId=" + bean.getInt("examId");
		}
		if (StrKit.notBlank(bean.getAnswerType())) {
			sql += " and examId in (select id from exams where answerType='"
					+ bean.getAnswerType() + "')";
		}
		if (null != bean.getInt("userId")) {
			sql += " and userId=" + bean.getInt("userId");
		}
		if (StrKit.notBlank(bean.getLoginName())) {
			sql += " and userId in (select id from users where loginName like '%"
					+ bean.getLoginName() + "%') ";
		}
		if (StrKit.notBlank(bean.getStr("status"))) {
			sql += " and status='" + bean.getStr("status") + "'";
		}
		if (null != bean.getOrderBy() && bean.getOrderBy().equals("-1"))
			sql += " order by id desc";
		else
			sql += " order by " + bean.getOrderBy();
		return sql;
	}

	public UserAnswer findUserAnswer(Integer userId, Integer examId) {
		return findFirst(
				"select * from useranswer where userId=? and examId=? and status=? order by id desc",
				userId, examId, Const.ExamStatus.START);
	}

	public UserAnswer startAnswer(Integer userId, Integer examId, String loginIp) {
		// 判断是否存在
		// UserAnswer ua =
		// findFirst("select * from useranswer where userId=? and examId=? order by id desc",
		// userId,
		// examId);
		// if (null == ua) {
		UserAnswer ua = new UserAnswer();
		ua.set("examId", examId).set("userId", userId)
				.set("startTime", new Date()).set("status", STATUS_START)
				.set("answerNum", 0).set("loginIp", loginIp).save();
		return ua;
		// }
	}

	public User getUser() {
		return User.dao.loadModel(getInt("userId"));
	}

	public Exam getExam() {
		return Exam.dao.loadModel(getInt("examId"));
	}

	// 获取答题用时
	public String getUsedTime() {
		Exam exam = getExam();
		String used = "";
		try {
			if ("testing".equals(exam.getStr("answerType"))) {
				Date submitTime = getDate("submitTime");
				if (null == submitTime)
					submitTime = new Date();
				long usedSeconds = (submitTime.getTime() - getDate("startTime")
						.getTime()) / 1000;
				if (usedSeconds > 60) {
					used += (int) (usedSeconds / 60) + "分";
				}
				used += (usedSeconds % 60) + "秒";
			} else {
				int usedSeconds = getInt("usedSeconds");
				if (usedSeconds > 60) {
					used += (int) (usedSeconds / 60) + "分";
				}
				used += (usedSeconds % 60) + "秒";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return used;
	}

	// 获取剩余时间
	public String getRestTime() {
		Exam exam = Exam.dao.loadModel(getInt("examId"));
		String rest = "";
		try {
			long leftTime = exam.getInt("timeLength")
					* 60
					- ((new Date().getTime() - getDate("startTime").getTime()) / 1000);
			if (leftTime <= 0) {
				return "已结束";
			}
			if (leftTime > 60) {
				rest += (int) (leftTime / 60) + "分";
			}
			rest += (leftTime % 60) + "秒";
		} catch (Exception e) {
		}
		return rest;
	}

	// 清空答案
	public void resetAnswer() {
		Db.update("update useranswerdetail set answer='' where userAnswerId=?",
				getInt("id"));
		List<UserAnswerDetail> details = UserAnswerDetail.dao
				.find(getInt("id"));
		for (UserAnswerDetail detail : details) {
			detail.set("answer", "").set("correct", null);
		}
	}

	public double calScore() {
		double score = 0.0;
		String sql = "update useranswerdetail set correct=? where id=?";
		String columns = "correct,id";
		Exam exam = Exam.dao.findById(getInt("examId"));
		int comboId = exam.getInt("comboId");
		// 读取用户答题列表
		List<UserAnswerDetail> details = UserAnswerDetail.dao
				.find(getInt("id"));
		for (UserAnswerDetail detail : details) {
			int questionTypeId = detail.getInt("questionTypeId");
			// 读取所做题目对应“题目组合信息”
			ExamComboDetail comboDetail = ExamComboDetail.dao.find(comboId,
					questionTypeId);
			// 获取题目组合中题型对应的分值
			double unitScore = comboDetail.getDouble("unitScore");
			// 读取题目正确答案
			String correctAnswer = QuestionSub.dao.loadModel(
					detail.getInt("questionSubId")).getStr("answer");
			String userAnswer = detail.getStr("answer");
			if (null == userAnswer || "null".equals(userAnswer)) {
				userAnswer = "";
			}
			userAnswer = userAnswer.replace("null", "");
			// 判断是否正确
			if (StringUtil.isEqualsIgnoreCase(correctAnswer, userAnswer)) {
				score += unitScore;
				detail.set("correct", 1);
			} else {
				detail.set("correct", 0);
			}
		}
		try {
			Db.batch(sql, columns, details, details.size());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		this.set("score", score).set("submitTime", new Date())
				.set("status", UserAnswer.STATUS_END).update();
		return score;
	}

	public String getAnswerType() {
		return answerType;
	}

	public void setAnswerType(String answerType) {
		this.answerType = answerType;
	}

	public String getLoginName() {
		return loginName;
	}

	public String getOrderBy() {
		return orderBy;
	}

	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}

	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}

}
