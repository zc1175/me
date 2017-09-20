package com.ollearning.front.controller;

import java.util.HashMap;
import java.util.Map;

import com.jfinal.kit.StrKit;
import com.ollearning.common.controller.BaseController;
import com.ollearning.testing.model.Question;
import com.ollearning.testing.model.UserAnswer;
import com.ollearning.testing.model.UserAnswerDetail;

public class FrontQuestionController extends BaseController {

	public void getQuestionJson() {
		int questionId = getParaToInt("questionId");
		Question question = Question.dao.loadModel(questionId);
		renderJson(question);
	}

	// 根据试题的序号获取题目
	public void getQuestionJsonByIndex() {
		Map<String, Object> ret = new HashMap<String, Object>();
		try {
			UserAnswer ua = getSessionAttr("userAnswer");
			int index = getParaToInt("index");
			int questionTypeId = getParaToInt("questionTypeId");
			boolean showUserAnswer = getParaToInt("ss", 0) == 1 ? true : false;
			Question question = Question.dao.findByExamIdIndex(ua,
					questionTypeId, index);
			ret.put("result", "success");
			ret.put("html", question.toHtml(ua, showUserAnswer));
		} catch (Exception ex) {
			ret.put("result", ex.getMessage());
			ex.printStackTrace();
		}
		renderJson(ret);
	}

	// 保存用户答案
	public void saveAnswer() {
		Map<String, Object> ret = new HashMap<String, Object>();
		try {
			int userId = getLoginCust().getInt("id");
			int examId = getParaToInt("examId");
			int questionId = getParaToInt("questionId");
			int num = getParaToInt("num");
			String answer = getPara("answer");
			int subId = getParaToInt("subId");
			String op = getPara("op");
			String showAnswer = getPara("ss");
			UserAnswer ua = getSessionAttr("userAnswer");

			// 查询用户原来答案
			String oldAnswer = UserAnswerDetail.dao.loadAnswer(ua, questionId,
					subId);
			if (null == oldAnswer)
				oldAnswer = "";
			if ("add".equalsIgnoreCase(op)) {
				;
			} else if ("append".equalsIgnoreCase(op)) {
				if (oldAnswer.indexOf(answer) == -1) {
					answer = oldAnswer + answer;
				}
			} else if ("remove".equalsIgnoreCase(op)) {
				answer = oldAnswer.replace(answer, "");
			}

			// save
			Map<String, Object> ansRet = UserAnswerDetail.dao.saveUserAnswer(
					ua.getInt("id"), userId, examId, questionId, subId, answer,
					num);
			if (StrKit.notBlank(showAnswer)) {
				ret.put("intCorrect", ansRet.get("correct"));
				ret.put("correct",
						((Integer) ansRet.get("correct")).equals(1) ? "正确"
								: "错误");
				ret.put("answer", ansRet.get("answer"));
				ret.put("description", ansRet.get("description") == null ? "无"
						: ansRet.get("description"));
			}

			ret.put("result", "success");
		} catch (Exception ex) {
			ex.printStackTrace();
			ret.put("result", "error");
		}
		renderJson(ret);
	}

	// 载入试题已有答案
	public void loadAnswer() {
		Map<String, Object> ret = new HashMap<String, Object>();
		try {
			UserAnswer ua = getSessionAttr("userAnswer");
			int questionId = getParaToInt("questionId");
			int subId = getParaToInt("subId");
			String answer = UserAnswerDetail.dao.loadAnswer(ua, questionId,
					subId);
			ret.put("result", "success");
			ret.put("answer", answer);
		} catch (Exception ex) {
			ret.put("result", "error");
		}
		renderJson(ret);
	}
}
