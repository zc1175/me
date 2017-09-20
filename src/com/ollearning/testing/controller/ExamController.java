package com.ollearning.testing.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.math.RandomUtils;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.ehcache.CacheKit;
import com.ollearning.common.controller.BaseController;
import com.ollearning.common.jfinal.Const;
import com.ollearning.common.util.DateUtil;
import com.ollearning.interceptors.AdminACLInterceptor;
import com.ollearning.testing.model.Exam;
import com.ollearning.testing.model.ExamCombo;
import com.ollearning.testing.model.ExamComboDetail;
import com.ollearning.testing.model.ExamQuestion;
import com.ollearning.testing.model.ExamType;
import com.ollearning.testing.model.Question;
import com.ollearning.testing.model.QuestionQuery;

@Before(AdminACLInterceptor.class)
public class ExamController extends BaseController {

	public void index() {

		int pageNumber = getParaToInt(1, 1);
		Exam condition = getModel(Exam.class);
		setAttr("exam", condition);
		setAttr("examPage", Exam.dao.findByPage(condition, pageNumber));
		setAttr("examTypeList", ExamType.dao.getTopList());
		setAttr("actionUrl", "/admin/exam/index-");
		render("/admin/exam/list.html");

	}

	public void add() {
		_form();
	}

	public void edit() {
		setAttr("exam", Exam.dao.loadModel(getParaToInt()));
		_form();
	}

	public void save() {
		Exam exam = getModel(Exam.class);
		ExamCombo combo = ExamCombo.dao.findById(exam.getInt("comboId"));
		ExamType examType = ExamType.dao.findById(exam.getInt("examTypeId"));
		exam.set("score", combo.getBigDecimal("score"))
				.set("questionNum", combo.get("questionNum"))
				.set("examTypeName", examType.getStr("name"));
		if (Const.Exam.Create.NONE.equals(exam.getStr("createType"))) {
			exam.set("created", 1);
		}
		if (null == exam.get("id")) {
			exam.set("pid", UUID.randomUUID().toString()).set("deleted", 0)
					.set("enabled", 1).set("createTime", DateUtil.curTime())
					.save();
		} else {
			exam.update();
		}
		redirect("/admin/exam");
	}

	/**
	 * 查询试卷试题列表
	 */
	public void question() {
		Exam exam = Exam.dao.loadModel(getParaToInt());
		ExamCombo combo = ExamCombo.dao.loadModel(exam.getInt("comboId"));
		Integer examComboDetailId = getParaToInt("examComboDetailId", -1);
		ExamComboDetail detail = ExamComboDetail.dao
				.loadModel(examComboDetailId);
		List<Question> questions = new ArrayList<Question>();
		List<ExamQuestion> eqlist = null;
		if (null != detail) {
			eqlist = ExamQuestion.dao.findByExamIdQuestionType(
					exam.getInt("id"), detail.getInt("subjectId"),
					detail.getInt("questionTypeId"));
		} else {
			eqlist = ExamQuestion.dao.findByExamIdQuestionType(
					exam.getInt("id"), null, null);
		}
		for (ExamQuestion eq : eqlist) {
			questions.add(eq.getQuestion());
		}
		setAttr("examId", exam.getInt("id"));
		setAttr("examComboDetailId", examComboDetailId);
		setAttr("comboDetails", combo.getDetails());
		setAttr("questions", questions);
		render("/admin/exam/question.html");
	}

	/**
	 * 查看试卷
	 */
	public void view() {
		Exam exam = Exam.dao.loadModel(getParaToInt());
		setAttr("exam", exam);
		setAttr("structure", exam.getStructure());
		render("/admin/exam/view.html");
	}

	/**
	 * 发布
	 */
	public void publish() {
		Map<String, Object> ret = new HashMap<String, Object>();
		try {
			int examId = getParaToInt("examId");
			int published = getParaToInt("published");
			Exam exam = Exam.dao.loadModel(examId);
			exam.set("published", published).update();
			ret.put("result", "success");
		} catch (Exception ex) {
			ret.put("result", "error");
			ret.put("errMsg", ex.getMessage());
		}
		renderJson(ret);
	}

	/**
	 * 生成试卷
	 */
	public void create() {
		Map<String, Object> ret = new HashMap<String, Object>();
		try {
			Exam exam = Exam.dao.findById(getParaToInt());
			exam.createSelf();

			try {
				String cacheKey = "examquestion";
				List<Question> list = Question.dao.findByExamId(exam
						.getInt("id"));
				CacheKit.put(cacheKey, exam.getInt("id"), list);
			} catch (Exception e) {
				e.printStackTrace();
			}

			ret.put("result", "success");
		} catch (Exception ex) {
			ex.printStackTrace();
			ret.put("result", "error");
			ret.put("errMsg", ex.getMessage());
		}
		renderJson(ret);
	}

	/**
	 * 发布
	 */
	public void delete() {
		Map<String, Object> ret = new HashMap<String, Object>();
		try {
			Exam.dao.findById(getParaToInt()).delete();
			ret.put("result", "success");
		} catch (Exception ex) {
			ret.put("result", "error");
			ret.put("errMsg", ex.getMessage());
		}
		renderJson(ret);
	}

	public void _form() {
		setAttr("examTypeList", ExamType.dao.getTopList());
		render("/admin/exam/exam.html");
	}

}
