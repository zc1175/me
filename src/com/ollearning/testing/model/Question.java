package com.ollearning.testing.model;

import java.util.List;

import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.ehcache.CacheKit;
import com.ollearning.common.jfinal.Const;
import com.ollearning.common.jfinal.Model;
import com.ollearning.common.util.HTMLUtil;

/**
 * 
 * @author Administrator
 * 
 */
public class Question extends Model<Question> {

	private static final long serialVersionUID = 9071918139841336008L;

	private static final String CACHE_MODEL = "question";

	public static final Question dao = new Question();
	
	

	public Question() {
		super(CACHE_MODEL);
	}

	// 按条件查询
	public Page<Question> findByCondition(QuestionQuery queryBean,
			Integer pageNumber) {
		String sql = "from questions where 1=1 and deleted=0 "
				+ queryBean.toString();
		return paginate(pageNumber, Const.PAGE_SIZE_FOR_ADMIN, "select *", sql);
	}
	
	

	public List<Question> findByQueryBean(QuestionQuery bean) {
		return find("select * from questions where 1=1 and deleted=0 "
				+ bean.toString());
	}

	// 查询子题数量
	public List<QuestionSub> getSubs() {
		return QuestionSub.dao.findByQuestionId(getInt("id"));
	}

	// 返回正确答案
	public String getCorrectAnswer() {
		String ret = "";
		List<QuestionSub> subs = getSubs();
		for (QuestionSub sub : subs) {
			String answer = sub.getStr("answer");
			for (int i = 0; i < answer.length(); i++) {
				ret += sub.getStr("option"
						+ answer.substring(i, i + 1).toLowerCase());
				if (i < answer.length() - 1) {
					ret += ",";
				}
			}
		}
		return ret;
	}

	public void deleteById(Integer questionId) {
		Db.update("update questions set deleted=1 where id=?", questionId);
		// Db.update("delete from questionsubs where questionId=?", questionId);
		// Db.update("delete from questions where id=?", questionId);
	}

	public Subject getSubject() {
		return Subject.dao.findById(getInt("subjectId"));
	}

	public void clearSubs() {
		Db.update("delete from questionsubs where questionId=?", getInt("id"));
	}

	public List<Question> findByExamId(int examId) {
		List<Question> list = find(
				"select * from questions where id in (select questionId from examquestion where examId=?)",
				examId);
		return list;
	}

	// 根据试卷ID，序号查询试题
	public Question findByExamIdIndex(UserAnswer ua, int questionTypeId,
			int index) {

		UserAnswerDetail uad = UserAnswerDetail.dao.find(ua, questionTypeId,
				index);
		if (null != uad)
			return uad.getQuestion();

		int examId = ua.getInt("examId");
		String cacheKey = "examquestion";
		List<Question> list = CacheKit.get(cacheKey, examId);
		if (null == list) {
			list = find(
					"select * from questions where id in (select questionId from examquestion where examId=?)",
					examId);
			CacheKit.put(cacheKey, examId, list);
		}
		if (list.size() >= index) {
			return list.get(index - 1);
		}
		return null;
	}

	// 根据考试分类，科目，题目类型查询 题库中 现在题目数量
	public int getQuestionNum(Integer examTypeId, Integer subjectId,
			Integer questionTypeId) {
		int rows = 0;
		String sql = "select count(*) as cnt from questions where 1=1";
		if (null != examTypeId && examTypeId > 0) {
			sql += " and examTypeId=" + examTypeId;
		}
		if (null != subjectId && subjectId > 0) {
			sql += " and subjectId = " + subjectId;
		}
		if (null != questionTypeId && questionTypeId > 0) {
			sql += " and questionTypeId=" + questionTypeId;
		}
		Record rec = Db.findFirst(sql);
		if (null != rec) {
			rows = rec.getLong("cnt").intValue();
		}
		return rows;
	}

	// 根据考试分类、科目查询试题数量
	public String getQuestionNum(Integer examTypeId, Integer subjectId) {
		String sql = "select questionTypeName,count(*) as cnt from questions where 1=1";
		if (null != examTypeId && examTypeId > 0) {
			sql += " and examTypeId=" + examTypeId;
		}
		if (null != subjectId && subjectId > 0) {
			sql += " and subjectId = " + subjectId;
		}
		sql += " group by questionTypeName";
		List<Record> list = Db.find(sql);
		String ret = "";
		if (list.size() == 0)
			ret = "暂无,请继续选择科目查询";
		else {
			for (Record rec : list) {
				ret += rec.getStr("questionTypeName");
				ret += " : " + rec.getLong("cnt");
				ret += ",";
			}
		}
		return ret;
	}

	/**
	 * 根据试题类型返回前台显示的HTML
	 * 
	 * @param examId
	 * @param userId
	 * @param getUserAnswer
	 *            是否读取用户答案，即若用户已经作答，则不可修改，并显示结果（是否正确，真正答案等），用于练习题
	 * @return
	 */
	public String toHtml(UserAnswer ua, boolean getUserAnswer) {
		StringBuffer html = new StringBuffer();
		html.append("<form class='jqtransform' method='post' action='#'>");
		html.append("<div class='title'>" + getStr("title") + "</div>");
		html.append("<div class='clear' style='height:10px'></div>");
		html.append("<div class='options'><ul>");
		List<QuestionSub> subs = getSubs();
		String questionType = (getStr("questionTypeName").indexOf("多选") != -1) ? "checkbox"
				: "radio";

		for (int i = 0; i < subs.size(); i++) {
			QuestionSub sub = subs.get(i);
			if (getUserAnswer == false) {
				// 模考
				html.append(buildQuestion(ua, questionType, sub));
			} else {
				html.append(buildPracticeQuestion(ua, questionType, sub));
			}
		}
		html.append("</ul></div>");
		html.append("</form>");
		html.append("<script>$('.jqtransform').jqTransform();</script>");
		return html.toString();
	}

	private String buildPracticeQuestion(UserAnswer ua, String questionType,
			QuestionSub sub) {
		StringBuffer html = new StringBuffer();
		// 查询用户此题已有答案
		String answer = UserAnswerDetail.dao.loadAnswer(ua, getInt("id"),
				sub.getInt("id"));
		answer = (null == answer) ? "" : answer;
		// 用户答案是否正确
		boolean isCorrect = answer.equalsIgnoreCase(sub.getStr("answer"));

		String onclick = ("radio".equals(questionType)) ? "onclick='saveAnswer(this)'"
				: "";

		if (StrKit.notBlank(sub.getStr("optiona"))) {
			html.append("<li  id='opa'><input " + onclick + " type='"
					+ questionType + "'");
			if (StrKit.notBlank(answer)
					&& answer.toUpperCase().indexOf("A") != -1) {
				html.append(" checked ");
			}
			html.append(" disabled ");
			String optiona = sub.getStr("optiona");
			if (StrKit.notBlank(optiona) && optiona.startsWith("A."))
				optiona = optiona.substring(2);
			html.append("name='choice' value='" + getInt("id") + "_"
					+ sub.getInt("id") + "_A'>A.<label>" + optiona
					+ "</label></li>");
		}
		if (StrKit.notBlank(sub.getStr("optionb"))) {
			html.append("<li id='opb'><input " + onclick + " type='"
					+ questionType + "'");
			if (StrKit.notBlank(answer)
					&& answer.toUpperCase().indexOf("B") != -1) {
				html.append(" checked ");
			}
			html.append(" disabled ");
			String optionb = sub.getStr("optionb");
			if (StrKit.notBlank(optionb) && optionb.startsWith("B."))
				optionb = optionb.substring(2);
			html.append(" name='choice' value='" + getInt("id") + "_"
					+ sub.getInt("id") + "_B'>B.<label>" + optionb
					+ "</label></li>");
		}
		if (StrKit.notBlank(sub.getStr("optionc"))) {
			html.append("<li  id='opc'><input " + onclick + " type='"
					+ questionType + "'");
			if (StrKit.notBlank(answer)
					&& answer.toUpperCase().indexOf("C") != -1) {
				html.append(" checked ");
			}
			html.append(" disabled ");
			String optionc = sub.getStr("optionc");
			if (StrKit.notBlank(optionc) && optionc.startsWith("C."))
				optionc = optionc.substring(2);
			html.append(" name='choice' value='" + getInt("id") + "_"
					+ sub.getInt("id") + "_C'>C.<label>" + optionc
					+ "</label></li>");
		}
		if (StrKit.notBlank(sub.getStr("optiond"))) {
			html.append("<li id='opd'><input " + onclick + " type='"
					+ questionType + "'");
			if (StrKit.notBlank(answer)
					&& answer.toUpperCase().indexOf("D") != -1) {
				html.append(" checked ");
			}
			html.append(" disabled ");
			String optiond = sub.getStr("optiond");
			if (StrKit.notBlank(optiond) && optiond.startsWith("D."))
				optiond = optiond.substring(2);
			html.append(" name='choice' value='" + getInt("id") + "_"
					+ sub.getInt("id") + "_D'>D.<label>" + optiond
					+ "</label></li>");
		}
		if (StrKit.notBlank(sub.getStr("optione"))) {
			html.append("<li  id='ope'><input " + onclick + " type='"
					+ questionType + "'");
			if (StrKit.notBlank(answer)
					&& answer.toUpperCase().indexOf("E") != -1) {
				html.append(" checked ");
			}
			html.append(" disabled ");
			String optione = sub.getStr("optione");
			if (StrKit.notBlank(optione) && optione.startsWith("E."))
				optione = optione.substring(2);
			html.append(" name='choice' value='" + getInt("id") + "_"
					+ sub.getInt("id") + "_E'>E.<label>" + optione
					+ "</label></li>");
		}
		if (StrKit.notBlank(sub.getStr("optionf"))) {
			html.append("<li id='opf'><input " + onclick + " type='"
					+ questionType + "'");
			if (StrKit.notBlank(answer)
					&& answer.toUpperCase().indexOf("F") != -1) {
				html.append(" checked ");
			}
			html.append(" disabled ");
			String optionf = sub.getStr("optionf");
			if (StrKit.notBlank(optionf) && optionf.startsWith("F."))
				optionf = optionf.substring(2);
			html.append(" name='choice' value='" + getInt("id") + "_"
					+ sub.getInt("id") + "_F'>F.<label>" + optionf
					+ "</label></li>");
		}
		if (StrKit.notBlank(answer)) {
			// 显示用户答案
			html.append("<li ");
			if (StrKit.notBlank(answer))
				html.append("class='userAnswerIdentifier' ");
			html.append("style='color:");
			html.append(isCorrect ? "green" : "red");
			html.append("'><p>结果：" + (isCorrect ? "正确" : "错误") + "</p>");
			if (!isCorrect)
				html.append("<p>正确答案：" + sub.getStr("answer") + "</p>");
			html.append("<p>解析："
					+ (sub.getStr("description") == null ? "无" : sub
							.getStr("description")) + "</p>");
			html.append("</li>");
		} else {
			if ("checkbox".equals(questionType))
				html.append("<li><button class='btn btn-default' onclick='saveBatchAnswer();return false;'>保存</button></li>");
		}
		return html.toString();
	}

	private String buildQuestion(UserAnswer ua, String questionType,
			QuestionSub sub) {
		StringBuffer html = new StringBuffer();
		// 查询用户此题已有答案
		String answer = UserAnswerDetail.dao.loadAnswer(ua, getInt("id"),
				sub.getInt("id"));
		answer = (null == answer) ? "" : answer;
		if (StrKit.notBlank(sub.getStr("optiona"))) {
			html.append("<li  id='opa'><input onclick='saveAnswer(this)' type='"
					+ questionType + "'");
			if (StrKit.notBlank(answer)
					&& answer.toUpperCase().indexOf("A") != -1) {
				html.append(" checked ");
			}
			String optiona = sub.getStr("optiona");
			if (StrKit.notBlank(optiona) && optiona.startsWith("A."))
				optiona = optiona.substring(2);
			html.append("name='choice' value='" + getInt("id") + "_"
					+ sub.getInt("id") + "_A'>A.<label>" + optiona
					+ "</label></li>");
		}
		if (StrKit.notBlank(sub.getStr("optionb"))) {
			html.append("<li id='opb'><input onclick='saveAnswer(this)' type='"
					+ questionType + "'");
			if (StrKit.notBlank(answer)
					&& answer.toUpperCase().indexOf("B") != -1) {
				html.append(" checked ");
			}
			String optionb = sub.getStr("optionb");
			if (StrKit.notBlank(optionb) && optionb.startsWith("B."))
				optionb = optionb.substring(2);
			html.append(" name='choice' value='" + getInt("id") + "_"
					+ sub.getInt("id") + "_B'>B.<label>" + optionb
					+ "</label></li>");
		}
		if (StrKit.notBlank(sub.getStr("optionc"))) {
			html.append("<li  id='opc'><input onclick='saveAnswer(this)' type='"
					+ questionType + "'");
			if (StrKit.notBlank(answer)
					&& answer.toUpperCase().indexOf("C") != -1) {
				html.append(" checked ");
			}
			String optionc = sub.getStr("optionc");
			if (StrKit.notBlank(optionc) && optionc.startsWith("C."))
				optionc = optionc.substring(2);
			html.append(" name='choice' value='" + getInt("id") + "_"
					+ sub.getInt("id") + "_C'>C.<label>" + optionc
					+ "</label></li>");
		}
		if (StrKit.notBlank(sub.getStr("optiond"))) {
			html.append("<li id='opd'><input onclick='saveAnswer(this)' type='"
					+ questionType + "'");
			if (StrKit.notBlank(answer)
					&& answer.toUpperCase().indexOf("D") != -1) {
				html.append(" checked ");
			}
			String optiond = sub.getStr("optiond");
			if (StrKit.notBlank(optiond) && optiond.startsWith("D."))
				optiond = optiond.substring(2);
			html.append(" name='choice' value='" + getInt("id") + "_"
					+ sub.getInt("id") + "_D'>D.<label>" + optiond
					+ "</label></li>");
		}
		if (StrKit.notBlank(sub.getStr("optione"))) {
			html.append("<li  id='ope'><input onclick='saveAnswer(this)' type='"
					+ questionType + "'");
			if (StrKit.notBlank(answer)
					&& answer.toUpperCase().indexOf("E") != -1) {
				html.append(" checked ");
			}
			String optione = sub.getStr("optione");
			if (StrKit.notBlank(optione) && optione.startsWith("E."))
				optione = optione.substring(2);
			html.append(" name='choice' value='" + getInt("id") + "_"
					+ sub.getInt("id") + "_E'>E.<label>" + optione
					+ "</label></li>");
		}
		if (StrKit.notBlank(sub.getStr("optionf"))) {
			html.append("<li id='opf'><input onclick='saveAnswer(this)' type='"
					+ questionType + "'");
			if (StrKit.notBlank(answer)
					&& answer.toUpperCase().indexOf("F") != -1) {
				html.append(" checked ");
			}
			String optionf = sub.getStr("optionf");
			if (StrKit.notBlank(optionf) && optionf.startsWith("F."))
				optionf = optionf.substring(2);
			html.append(" name='choice' value='" + getInt("id") + "_"
					+ sub.getInt("id") + "_F'>F.<label>" + optionf
					+ "</label></li>");
		}
		return html.toString();
	}

	public String getTitle() {
		String title = getStr("title");
		if (StrKit.notBlank(title)) {
			return HTMLUtil.html2text(title);
		}
		return "";
	}

	public String getDescription() {
		String des = getStr("description");
		if (StrKit.notBlank(des)) {
			return HTMLUtil.html2text(des);
		}
		return "";
	}

}
