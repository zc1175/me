package com.ollearning.testing.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jfinal.aop.Before;
import com.jfinal.kit.PathKit;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.ehcache.CacheKit;
import com.jfinal.upload.UploadFile;
import com.ollearning.common.controller.BaseController;
import com.ollearning.common.util.QuestionImportUtil;
import com.ollearning.global.BIConfig;
import com.ollearning.interceptors.AdminACLInterceptor;
import com.ollearning.testing.model.ExamType;
import com.ollearning.testing.model.Question;
import com.ollearning.testing.model.QuestionQuery;
import com.ollearning.testing.model.QuestionSub;
import com.ollearning.testing.model.QuestionType;

@Before(AdminACLInterceptor.class)
@SuppressWarnings("rawtypes")
public class QuestionController extends BaseController {

	public void index() {

		setAttr("examTypeList", ExamType.dao.getTopList());
		setAttr("typeList", QuestionType.dao.findAll());

		int pageNumber = getParaToInt("pageNumber", 1);
		setAttr("pageNumber", pageNumber);
		setSessionAttr("pageNumber", pageNumber);

		QuestionQuery queryBean = getModel(QuestionQuery.class);
		setAttr("questionQuery", queryBean);
		setSessionAttr("questionQuery", queryBean);

		Page<Question> page = Question.dao.findByCondition(queryBean,
				pageNumber);
		System.err.println("总行数：============================="+page.getTotalRow());
		setAttr("page", page);

		render("/admin/question/list.html");
	}

	public void add() {
		_form();
	}

	//修改题库管理
	@SuppressWarnings("unchecked")
	public void save() {
		Question question = getModel(Question.class);
		question.set("questionTypeName",
				QuestionType.dao.findById(question.getInt("questionTypeId"))
						.getStr("name"));
		//选择题答案集合
		List<QuestionSub> questionSubs = getQuestionSubs();

		if (null == question.getInt("id")) {
			question.set("childNum", questionSubs.size())
					.set("typeName",
							ExamType.dao
									.findById(question.getInt("examTypeId"))
									.getStr("name")).save();
			for (QuestionSub sub : questionSubs) {
				sub.set("questionId", question.getInt("id")).save();
			}
		} else {
			question.set("childNum", questionSubs.size())
					.set("typeName",
							ExamType.dao
									.findById(question.getInt("examTypeId"))
									.getStr("name")).update();
			question.clearSubs();
			for (QuestionSub sub : questionSubs) {
				sub.set("questionId", question.getInt("id")).save();
			}
		}

		try {
			String key = "questionsub-" + question.getInt("id");
			CacheKit.put(QuestionSub.cacheList, key, questionSubs);
		} catch (Exception ex) {
			;
		}

		QuestionQuery queryBean = getSessionAttr("questionQuery");
		setAttr("questionQuery", queryBean);

		int pageNumber = getSessionAttr("pageNumber");

		Page<Question> page = Question.dao.findByCondition(queryBean,
				pageNumber);
		setAttr("page", page);

		render("/admin/question/list.html");

		// redirect("/admin/question?queryBean.examTypeId="+question.getInt("examTypeId"));
	}

	//修改题库管理进入
	private List<QuestionSub> getQuestionSubs() {
		List<QuestionSub> subs = new ArrayList<QuestionSub>();
		String[] title = getParaValues("title");
		String[] optiona = getParaValues("optiona");
		String[] optionb = getParaValues("optionb");
		String[] optionc = getParaValues("optionc");
		String[] optiond = getParaValues("optiond");
		String[] answer = getParaValues("answer");
		for (int i = 0; i < title.length; i++) {
			subs.add(new QuestionSub().set("title", title[i])
					.set("optiona", optiona[i]).set("optionb", optionb[i])
					.set("optionc", optionc[i]).set("optiond", optiond[i])
					.set("answer", answer[i]));
		}
		return subs;
	}

	public void edit() {
		setAttr("question", Question.dao.findById(getParaToInt()));
		_form();
	}

	public void delete() {
		Question.dao.deleteById(getParaToInt());
		redirect("/admin/question");
	}

	public void _form() {
		setAttr("examTypeList", ExamType.dao.getTopList());
		setAttr("questionTypeList", QuestionType.dao.findAll());
		render("/admin/question/question.html");
	}

	public void batchImport() {
		setAttr("examTypeList", ExamType.dao.getTopList());
		render("/admin/question/import.html");
	}

	// 导入试题
	public void doImport() {
		// 上传文件
		String saveDir = PathKit.getWebRootPath() + BIConfig.QUESTION_DIR;

		UploadFile upFile = getFile("file1", saveDir, 50 * 1000 * 1000, "utf-8");
		if (null == upFile) {
			return;
		}

		File file = upFile.getFile();
		String fileExt = upFile.getOriginalFileName().substring(
				upFile.getOriginalFileName().lastIndexOf("."));

		if (!fileExt.equals(".doc") && !fileExt.equals(".docx")
				&& !fileExt.equals(".xls") && !fileExt.equals(".xlsx")) {
			renderHtml("<script>alert('文件格式不正确');history.back();</script>");
			return;
		}
		String newFileName = System.currentTimeMillis() + "";
		String filePath = saveDir + newFileName + fileExt;

		file.renameTo(new File(filePath));
		int examTypeId = getParaToInt("examTypeId");
		Map<String, Integer> ret = new HashMap<String, Integer>();
		if (fileExt.equals(".doc") || fileExt.equals(".docx")) {
			ret = QuestionImportUtil.doImportWord(filePath, examTypeId);
		} else if (fileExt.equals(".xls") || fileExt.equals(".xlsx")) {
			ret = QuestionImportUtil.doImportExcel(filePath, examTypeId);
		}

		setSessionAttr("fileName", file.getName());
		setSessionAttr("filePath", filePath);
		setSessionAttr("ret", ret);
		redirect("/admin/question/importResult");
	}

	public void importResult() {
		render("/admin/question/importResult.html");
	}

	public void batchDelete() {

		String[] questionIds = getParaValues("questionIds");
		for (String questionId : questionIds) {
			Question question = Question.dao.findById(questionId);
			if (null != question)
				question.delete();
		}

		QuestionQuery queryBean = getSessionAttr("questionQuery");
		setAttr("questionQuery", queryBean);

		int pageNumber = null != getSessionAttr("pageNumber") ? (Integer) getSessionAttr("pageNumber")
				: 1;

		Page<Question> page = Question.dao.findByCondition(queryBean,
				pageNumber);
		setAttr("page", page);

		render("/admin/question/list.html");
	}

	private QuestionQuery getQueryCondition() {
		QuestionQuery bean = getModel(QuestionQuery.class);
		Enumeration<String> enums = getAttrNames();
		for (String s = enums.nextElement(); enums.hasMoreElements();) {
			try {
				bean.set(s, getPara(s));
			} catch (Exception e) {
				e.printStackTrace();
			}
			s = enums.nextElement();
		}
		return bean;
	}

}
