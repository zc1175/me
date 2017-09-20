package com.ollearning.testing.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jfinal.aop.Before;
import com.jfinal.plugin.ehcache.CacheKit;
import com.ollearning.common.controller.BaseController;
import com.ollearning.interceptors.AdminACLInterceptor;
import com.ollearning.testing.model.ExamCombo;
import com.ollearning.testing.model.ExamComboDetail;
import com.ollearning.testing.model.ExamType;
import com.ollearning.testing.model.Question;
import com.ollearning.testing.model.QuestionType;

@Before(AdminACLInterceptor.class)
public class ExamComboController extends BaseController<ExamCombo> {

	public void index() {
		ExamCombo examCombo = getModel(ExamCombo.class);
		setAttr("examCombo", examCombo);
		setAttr("examTypeList", ExamType.dao.getTopList());
		setAttr("comboPage",
				ExamCombo.dao.findByPage(examCombo, getParaToInt(1, 1)));
		render("/admin/examCombo/list.html");
	}

	public void add() {
		_form();
	}

	public void view() {
		setAttr("examCombo", ExamCombo.dao.findById(getParaToInt()));
		render("/admin/examCombo/view.html");
	}

	public void edit() {
		setAttr("examCombo", ExamCombo.dao.findById(getParaToInt()));
		_form();
	}

	public void _form() {
		setAttr("examTypeList", ExamType.dao.getTopList());
		setAttr("questionTypeList", QuestionType.dao.findAll());
		render("/admin/examCombo/examCombo.html");
	}

	public void delete() {
		ExamCombo combo = ExamCombo.dao.findById(getParaToInt());
		combo.set("deleted", 1).update();
		// combo.deleteDetails();
		// combo.delete();
		redirect("/admin/examCombo");
	}

	public void save() {
		ExamCombo combo = getModel(ExamCombo.class);
		combo.set("examTypeName",
				ExamType.dao.findById(combo.getInt("examTypeId"))
						.getStr("name"));
		combo.set("createTime", new Date());
		if (null == combo.getInt("id")) {
			combo.set("deleted", 0);
			combo.save();
		} else {
			combo.update();
		}

		// details
		String[] subjectIds = getParaValues("subjectId");
		String[] questionTypeIds = getParaValues("questionTypeId");
		String[] questionNums = getParaValues("questionNum");
		String[] unitScores = getParaValues("unitScore");
		String[] selectFrom = getParaValues("selectFrom");
		String[] selectTo = getParaValues("selectTo");

		combo.deleteDetails();
		List<ExamComboDetail> details = new ArrayList<ExamComboDetail>();
		for (int i = 0; i < questionTypeIds.length; i++) {
			try {
				ExamComboDetail detail = new ExamComboDetail();
				detail.set("subjectId", subjectIds[i])
						.set("comboId", combo.getInt("id"))
						.set("questionTypeId", questionTypeIds[i])
						.set("selectFrom", selectFrom[i])
						.set("selectTo", selectTo[i])
						.set("questionTypeName",
								QuestionType.dao.findById(
										Integer.parseInt(questionTypeIds[i]))
										.getStr("name"))
						.set("questionNum", questionNums[i])
						.set("unitScore", unitScores[i]).save();
				details.add(detail);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		CacheKit.remove(ExamComboDetail.cacheList, ExamComboDetail.cacheList
				+ "-" + combo.getInt("id"));
		CacheKit.put(ExamComboDetail.cacheList, ExamComboDetail.cacheList + "-"
				+ combo.getInt("id"), details);

		redirect("/admin/examCombo");

	}

	public void listJson() {
		ExamCombo combo = getModel(ExamCombo.class);
		Map<String, Object> ret = new HashMap<String, Object>();
		ret.put("data", ExamCombo.dao.find(combo));
		renderJson(ret);
	}

	// JSON查询题库中现有题目数量
	public void getQuestionNum() {
		Map<String, Object> ret = new HashMap<String, Object>();
		try {
			Integer examTypeId = getParaToInt("examTypeId", -1);
			Integer subjectId = getParaToInt("subjectId", -1);
			String questionNum = Question.dao.getQuestionNum(examTypeId,
					subjectId);
			ret.put("questionNum", questionNum);
			ret.put("result", "success");
		} catch (Exception ex) {
			ret.put("result", "error");
			ret.put("errMsg", ex.getMessage());
		}
		renderJson(ret);
	}

}
