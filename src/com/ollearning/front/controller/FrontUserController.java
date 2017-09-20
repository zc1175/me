package com.ollearning.front.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Page;
import com.ollearning.common.controller.BaseController;
import com.ollearning.common.util.DateUtil;
import com.ollearning.common.util.ExcelUtil;
import com.ollearning.common.util.MD5;
import com.ollearning.common.util.TempFileRender;
import com.ollearning.testing.model.Exam;
import com.ollearning.testing.model.UserAnswer;
import com.ollearning.testing.model.UserAnswerDetail;
import com.ollearning.user.model.User;

/**
 * 前台用户操作模块
 * 
 * @author xingry
 * 
 */
public class FrontUserController extends BaseController {

	/**
	 * 转至登录页
	 */
	public void login() {
		setAttr("returnUrl", getPara("returnUrl"));
		setAttr("error", getPara("error"));
		render("/front/user/login.html");
	}

	public void doLogin() {
		User user = User.dao.login(getPara("loginName"), getPara("loginPwd"));
		String returnUrl = getPara("returnUrl");
		if (null != user) {
			user.set("curLoginTime", user.get("lastLoginTime"))
					.set("lastLoginTime", DateUtil.curTime())
					.set("lastLoginIp", getRealIpAddr())
					.set("loginCount", user.getInt("loginCount") + 1).update();
			setSessionAttr("loginUser", user);
			if (StrKit.notBlank(returnUrl)) {
				System.out.println("---login success ");
				if (returnUrl.startsWith(getBasePath())) {
					redirect(returnUrl);
				} else {
					redirect(getBasePath() + returnUrl);
				}
				return;
			} else {
				System.out.println("---login success " + getBasePath());
				redirect(getBasePath());
			}
		} else {
			redirect(getBasePath() + "/user/login?error=true");
		}
	}

	public void logout() {
		removeSessionAttr("loginUser");
		String returnUrl = getPara("returnUrl");
		if (null != returnUrl)
			redirect(returnUrl);
		else
			redirect(getBasePath());
	}

	public void center() {
		if (null == getLoginCust()) {
			redirect(getBasePath() + "/user/login");
			return;
		}
		String method = getPara();
		if ("exam".equals(method))
			centerExam();
		else if ("examdetail".equals(method))
			examDetail();
		else if ("examquestion".equals(method))
			examQuestion();
		else if ("practice".equals(method))
			centerPractice();
		else if ("repwd".equals(method))
			centerRepwd();
		else if ("question".equals(method))
			centerQuestion();
		else if ("doRepwd".equals(method))
			doRepwd();
		else
			render("/front/user/center.html");
	}

	private void examQuestion() {
		Page<UserAnswerDetail> uadPage = UserAnswerDetail.dao
				.findUserWrongPage(getParaToInt("p", 1),
						getLoginCust().getInt("id"), getParaToInt("id"));
		setAttr("uadPage", uadPage);
		setAttr("userAnswerId", getParaToInt("id"));
		setAttr("item", getPara("item")); // 指定左侧导航标识
		render("/front/user/user_exam_question.html");
	}

	private void examDetail() {
		int uaId = getParaToInt("id");
		int userId = getLoginCust().getInt("id");
		UserAnswer ua = UserAnswer.dao.findById(uaId);
		Exam exam = ua.getExam();
		setAttr("userAnswer", ua);
		setAttr("exam", exam);
		setAttr("item", getPara("item"));
		setAttr("user", User.dao.loadModel(userId));
		setAttr("structure", exam.getStructure(userId));
		render("/front/user/user_exam_detail.html");

	}

	public void doRepwd() {
		User loginUser = getLoginCust();
		if (!loginUser.getStr("loginPwd").equals(
				MD5.GetMD5Code(getPara("oldPwd")))) {
			setAttr("errMsg", "原始密码不正确，请重新输入！");
			render("/front/user/user_repwd.html");
			return;
		}
		loginUser.set("loginPwd", MD5.GetMD5Code(getPara("pwd"))).update();
		renderHtml("<script>alert('密码修改成功！');location.href='" + getCtxPath()
				+ "/user/center/repwd';</script>");
	}

	private void centerRepwd() {
		render("/front/user/user_repwd.html");
	}

	private void centerQuestion() {
		Page<UserAnswerDetail> uadPage = UserAnswerDetail.dao
				.findUserWrongPage(getParaToInt("p", 1),
						getLoginCust().getInt("id"), null);
		setAttr("uadPage", uadPage);
		render("/front/user/user_wrong.html");
	}

	private void centerPractice() {
		UserAnswer ua = new UserAnswer();
		ua.set("userId", getLoginCust().getInt("id"));
		ua.setAnswerType("practice");
		Page<UserAnswer> examPage = UserAnswer.dao.getPage(
				getParaToInt("p", 1), ua);
		setAttr("examPage", examPage);
		render("/front/user/user_practice.html");
	}

	private void centerExam() {
		UserAnswer ua = new UserAnswer();
		ua.set("userId", getLoginCust().getInt("id"));
		ua.setAnswerType("testing");
		Page<UserAnswer> examPage = UserAnswer.dao.getPage(
				getParaToInt("p", 1), ua);
		setAttr("examPage", examPage);
		render("/front/user/user_exam.html");
	}

	public void exportExcel() {
		String uaId = getPara("id");
		List<UserAnswerDetail> list = null;
		String companyName = "";
		if (StrKit.notBlank(uaId)) {
			UserAnswer ua = UserAnswer.dao.findById(uaId);
			companyName = ua.getExam().getStr("name") + " - 错题汇总";
			list = UserAnswerDetail.dao.findUserWrong(
					getLoginCust().getInt("id"), Integer.parseInt(uaId));
		} else {
			companyName = "错题汇总";
			list = UserAnswerDetail.dao.findUserWrong(
					getLoginCust().getInt("id"), null);
		}
		String tableHeader = "题型,题目,答案";
		List<List<Object>> data = new ArrayList<List<Object>>();
		for (UserAnswerDetail detail : list) {
			List<Object> item = new ArrayList<Object>();
			item.add(detail.getQuestionType().getStr("name"));
			item.add(detail.getQuestion().getStr("title"));
			item.add(detail.getQuestion().getCorrectAnswer());
			data.add(item);
		}

		String filePath = ExcelUtil.createExcel(companyName, tableHeader, data,
				DateUtil.now("yyyyMMddHHmmss") + ".xls");
		render(new TempFileRender(new File(filePath)));

	}

}
