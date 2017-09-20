package com.ollearning.testing.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.math.RandomUtils;

import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.ollearning.common.jfinal.Const;
import com.ollearning.common.jfinal.Model;
import com.ollearning.common.util.DateUtil;
import com.ollearning.user.model.User;

/**
 * 试卷
 * 
 * @author Administrator
 * 
 */
public class Exam extends Model<Exam> {

	private static final long serialVersionUID = -4683305053833699656L;
	private static final String CACHE_MODEL = "exam";
	public static final Exam dao = new Exam();

	// 前台使用，标识此试卷用户是否已答
	private boolean answered = false;
	// 前台使用，标识此试卷用户是否已交卷
	private boolean answerSubmit = false;

	public Exam() {
		super(CACHE_MODEL);
		put("answered", false);
	}

	public QueryBean newQueryBean() {
		return new QueryBean();
	}

	public class QueryBean {
		public Integer examTypeId;
		public String time;
		public String type;

		public String toQuerySQL() {
			String ret = "";
			if (null != examTypeId && examTypeId != -1) {
				ret += " and examTypeId = " + examTypeId;
			}
			if (StrKit.notBlank(time) && !("-1".equals(time))) {
				ret += " and date_add(createTime,INTERVAL " + time + " DAY)>='"
						+ DateUtil.curTime() + "'";
			}
			if (StrKit.notBlank(type) && !(type.equals("-1"))) {
				ret += " and answerType='" + type + "'";
			}
			return ret;
		}
	}

	/**
	 * 根据科目 查询试卷列表
	 */
	public Page<Exam> findByPage(Exam exam, int pageNumber) {
		String condition = "";
		if (null != exam && null != exam.getInt("examTypeId")
				&& exam.getInt("examTypeId") > 0) {
			condition += " and examTypeId=" + exam.getInt("examTypeId");
		}
		if (null != exam && StrKit.notBlank(exam.getStr("name"))) {
			condition += " and name like '%" + exam.getStr("name") + "%'";
		}
		if (StrKit.notBlank(exam.getStr("createType"))) {
			condition += " and createType='" + exam.getStr("createType") + "'";
		}
		if (StrKit.notBlank(exam.getStr("answerType"))) {
			condition += " and answerType='" + exam.getStr("answerType") + "'";
		}
		condition += " and deleted=0";
		Page<Exam> page = paginate(pageNumber, Const.PAGE_SIZE_FOR_ADMIN,
				"select *", "from exams where 1=1" + condition
						+ " order by id desc");
		return page;
	}

	// 查询用户参加的考试
	public Page<Exam> findPageByUserId(Integer userId, Integer pageNumber) {
		String sql = "from exams where id in ( select examId from useranswer where userId="
				+ userId + " order by id desc) and answerType='testing' ";
		return paginate(pageNumber, Const.PAGE_SIZE_FOR_ADMIN, "select *", sql);
	}

	// 查询已经开始的试卷列表
	public List<Exam> findStartedExams() {
		return find("select * from exams where startTime <= now() and enabled=1");
	}

	public String getCreatedDes() {
		if (null == getBoolean("created") || false == getBoolean("created")) {
			return "<font color=red>未生成</font>";
		} else {
			return "<font color=green>已生成</font>";
		}
	}

	public String getPublishedDes() {
		if (null == getBoolean("published") || false == getBoolean("published")) {
			return "<font color=red>未发布</font>";
		} else {
			return "<font color=green>已发布</font>";
		}
	}

	public Exam findByPid(String pid) {
		return findFirst("select * from exams where pid=?", pid);
	}

	public ExamCombo getExamCombo() {
		return ExamCombo.dao.findById(getInt("comboId"));
	}

	// 前台调用，获取当前试卷的“开始时间”状态
	// 异步：判断当前时间是否大于其开始时间，若大于则返回“已开始”，否则返回开始时间
	// 同步：判断当前时间是否大于开始时间 ，若大于则返回已开始，否则返回开始时间
	public String getTimeDes() {
		if (new Date().getTime() > getDate("startTime").getTime()) {
			return "已开始";
		} else {
			return DateUtil.format(getDate("startTime"), "MM-dd HH:mm");
		}
	}

	// 前台调用，判断用户是否可以“进入考试”
	// 如果当前时间大于开考时间 ，返回true可以进入
	// 同步：判断当前时间是否在开考前10分钟，是-则可以进入倒计时；否则不能进入
	// 异步：未到开考时间 ，返回false
	public boolean getExamStarted() {
		if (new Date().getTime() > getDate("startTime").getTime())
			return true;
		if ("同步".equals(getStr("syncType"))) {
			if (DateUtil.dateAdd(new Date(), 10 * 60 * 1000).getTime() > getDate(
					"startTime").getTime()) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	// ------ 前台调用方法 ------

	// 查询已发布，已生成，在时间范围内的试卷列表
	public Page<Exam> getExamListForFront(Integer pageNumber, Exam.QueryBean qb) {
		String sqlExceptSelect = "from exams where published=1 and created=1 and enabled=1 and deleted=0 ";
		sqlExceptSelect += qb.toQuerySQL();
		// 时间范围：开始考试前6个小时可见
		Date startDate = DateUtil.dateAdd(new Date(), 6 * 60 * 60 * 1000);
		sqlExceptSelect += " and startTime<='"
				+ DateUtil.format(startDate, "yyyy-MM-dd HH:mm:ss") + "'";
		sqlExceptSelect += " and endTime>='" + DateUtil.curTime() + "'";
		sqlExceptSelect += " order by id desc";
		return paginate(pageNumber, Const.PAGE_SIZE_FOR_ADMIN, "select *",
				sqlExceptSelect);
	}

	// 首页调用，查询最新题库
	public List<Exam> getExamListForIndex() {
		String sql = "select * from exams where deleted=0 and published=1 and created=1 order by id desc limit 6";
		return find(sql);
	}

	// 输入前台试卷题号结构
	public String toStructureHtml(UserAnswer ua, boolean showUserAnswer) {
		String html = "";
		int num = 1;
		List<ExamComboDetail> details = ExamComboDetail.dao
				.findByComboId(getInt("comboId"));
		for (ExamComboDetail detail : details) {
			html += "<div class='panel panel-primary'>";
			html += "<div class='panel-heading'>";
			html += "<span class='questionType'>"
					+ detail.getStr("questionTypeName") + "</span>";
			html += "</div>";
			html += "<div class='panel-body'>";
			html += "<ul>";
			for (int i = 1; i <= detail.getInt("questionNum"); i++) {
				// 判断用户是否已答题
				UserAnswerDetail answer = UserAnswerDetail.dao.find(ua,
						detail.getInt("questionTypeId"), num);
				html += "<li vid='" + detail.getInt("questionTypeId") + "-" + i
						+ "' class='option ";
				if (null != answer && StrKit.notBlank(answer.getStr("answer"))) {
					html += "answered ";
				}
				if (showUserAnswer && null != answer
						&& null != answer.get("correct")) {
					html += String.valueOf(answer.get("correct")).equals("1") ? "correct"
							: "wrong";
				}
				html += "'>" + num + "</li>";
				num++;
			}
			html += "</ul>";
			html += "</div></div>";
			html += "<div class='clearfix'></div>";
		}
		return html;
	}

	// 输出试卷结构Map<ExamComboDetail,List<Question>>
	public Map<ExamComboDetail, List<Question>> getStructure() {
		Map<ExamComboDetail, List<Question>> map = new HashMap<ExamComboDetail, List<Question>>();
		List<ExamComboDetail> details = ExamComboDetail.dao
				.findByComboId(getInt("comboId"));
		for (ExamComboDetail detail : details) {
			List<ExamQuestion> eqs = ExamQuestion.dao.findByExamIdQuestionType(
					getInt("id"), detail.getInt("subjectId"),
					detail.getInt("questionTypeId"));
			List<Question> questions = new ArrayList<Question>();
			for (ExamQuestion eq : eqs) {
				questions.add(eq.getQuestion());
			}
			map.put(detail, questions);
		}
		return map;
	}

	// 输出试卷结构 Map<ExamComboDetail,List<UserAnswerDetail>>
	public Map<ExamComboDetail, Object> getStructure(Integer userId) {
		Map<ExamComboDetail, Object> ret = new HashMap<ExamComboDetail, Object>();
		List<ExamComboDetail> details = ExamComboDetail.dao
				.findByComboId(getInt("comboId"));
		for (ExamComboDetail detail : details) {
			List<UserAnswerDetail> answers = UserAnswerDetail.dao
					.findByQuestionTypeId(userId, getInt("id"),
							detail.getInt("questionTypeId"));
			ret.put(detail, answers);
		}
		return ret;
	}

	/**
	 * 适用于试卷题目固定情况，生成试卷，随机抽取试题
	 */
	public void createSelf() {
		// 删除此试卷原有的试题
		Db.update("delete from examquestion where examId=?", getInt("id"));
		// 读取试卷对应的试题组合信息
		ExamCombo combo = ExamCombo.dao.findById(getInt("comboId"));
		int examTypeId = combo.getInt("examTypeId");
		List<ExamComboDetail> details = ExamComboDetail.dao
				.findByComboId(getInt("comboId"));
		for (ExamComboDetail detail : details) {
			int subjectId = -1;
			try {
				subjectId = detail.getInt("subjectId");
			} catch (Exception ex) {
				try {
					subjectId = Integer.parseInt(detail.getStr("subjectId"));
				} catch (Exception e) {
					;
				}
			}
			int questionTypeId = detail.getInt("questionTypeId");
			int questionNum = detail.getInt("questionNum");
			// 随机抽题
			// 查询题库中所有题目
			QuestionQuery queryBean = new QuestionQuery();
			queryBean.set("subjectId", subjectId);
			queryBean.set("examTypeId", "" + examTypeId); // 考试类型
			queryBean.set("typeId", "" + questionTypeId); // 试题类型
			queryBean.set("selectFrom", detail.getInt("selectFrom"));
			queryBean.set("selectTo", detail.getInt("selectTo"));
			List<Question> questions = Question.dao.findByQueryBean(queryBean);
			List<Integer> selectedIds = new ArrayList<Integer>();

			// 检查题库中已有最大数量，若指定数量大于题库已有数量，则更正
			int maxNum = Question.dao.getQuestionNum(examTypeId, subjectId,
					questionTypeId);
			if (questionNum < 0) {
				questionNum = maxNum;
				// 同时更新试卷组合明细
				detail.set("questionNum", questionNum).update();
				combo.updateQuestionNum();
			} else if (questionNum > maxNum) {
				questionNum = maxNum;
				// 同时更新试卷组合明细
				detail.set("questionNum", questionNum).update();
				combo.updateQuestionNum();
			}
			for (int i = 0; i < questionNum; i++) {
				int rnd = RandomUtils.nextInt(questions.size());
				int questionId = questions.get(rnd).getInt("id");
				int typeId = questions.get(rnd).getInt("questionTypeId");
				String questionTypeName = questions.get(rnd).getStr(
						"questionTypeName");
				// 判断是否已抽过
				if (selectedIds.contains(questionId)) {
					i--;
					continue;
				}
				selectedIds.add(questionId);
				Db.update(
						"insert into examquestion(examId,subjectId,questionId,questionTypeId,questionTypeName) values(?,?,?,?,?)",
						getInt("id"), subjectId, questionId, typeId,
						questionTypeName);
			}
		}
		set("created", true).update();
	}

	// 为用户生成试卷：读取试卷题目，每题的子题列表，向用户答题明细UserAnswerDetail表中插入题目
	// 判断试卷是否为固定试题，若是固定（每人题目)再判断每人顺序是否相同； 若为不固定，则需重新抽取
	public void createExamForUser(UserAnswer ua) {
		String sql = "insert into useranswerdetail(userAnswerId,examId,questionId,questionSubId,userId,questionTypeId) values(?,?,?,?,?,?)";
		String columns = "userAnswerId,examId,questionId,questionSubId,userId,questionTypeId";
		List<UserAnswerDetail> batchDetails = new ArrayList<UserAnswerDetail>();

		Integer userId = ua.getInt("userId");
		// 试卷生成类型
		String createType = getStr("createType");
		if (Const.Exam.Create.NONE.equals(createType)) {
			// 每人题目不同，需实时抽取
			ExamCombo combo = ExamCombo.dao.findById(getInt("comboId"));
			int examTypeId = combo.getInt("examTypeId");
			List<ExamComboDetail> details = ExamComboDetail.dao
					.findByComboId(getInt("comboId"));
			int globalNum = 1;
			for (ExamComboDetail detail : details) {
				int subjectId = detail.getInt("subjectId");
				int questionTypeId = detail.getInt("questionTypeId");
				int questionNum = detail.getInt("questionNum");
				// 随机抽题
				// 查询题库中所有题目
				QuestionQuery queryBean = new QuestionQuery();
				queryBean.set("subjectId", subjectId);
				queryBean.set("examTypeId", "" + examTypeId); // 考试类型
				queryBean.set("typeId", "" + questionTypeId); // 试题类型
				queryBean.set("selectFrom", detail.getInt("selectFrom"));
				queryBean.set("selectTo", detail.getInt("selectTo"));
				List<Question> questions = Question.dao
						.findByQueryBean(queryBean);
				List<Integer> selectedIds = new ArrayList<Integer>();

				// 检查题库中已有最大数量，若指定数量大于题库已有数量，则更正
				// int maxNum = Question.dao.getQuestionNum(examTypeId,
				// subjectId, questionTypeId);
				int maxNum = questions.size();
				if (questionNum < 0)
					questionNum = maxNum;
				else if (questionNum > maxNum) {
					questionNum = maxNum;
					// 同时更新试卷组合明细
					detail.set("questionNum", questionNum).update();
					combo.updateQuestionNum();
				}

				for (int i = 0; i < questionNum; i++) {
					int rnd = RandomUtils.nextInt(maxNum);
					int questionId = questions.get(rnd).getInt("id");
					// 判断是否已抽过
					if (selectedIds.contains(questionId)) {
						i--;
						continue;
					}
					selectedIds.add(questionId);

					Question question = Question.dao.loadModel(questionId);
					List<QuestionSub> subs = question.getSubs();
					for (QuestionSub sub : subs) {
						UserAnswerDetail uad = new UserAnswerDetail()
								.set("userAnswerId", ua.getInt("id"))
								.set("examId", getInt("id"))
								.set("questionId", question.getInt("id"))
								.set("questionSubId", sub.getInt("id"))
								.set("userId", userId)
								.set("questionTypeId",
										question.getInt("questionTypeId"))
								.set("rank", globalNum++);
						batchDetails.add(uad);
					}
				}
			}
		} else {
			int globalNum = 1;
			if (Const.Exam.Create.DIFF.equals(createType)) {
				// 固定试卷，每人题目相同，顺序不同
				List<ExamComboDetail> details = ExamComboDetail.dao
						.findByComboId(getInt("comboId"));
				for (ExamComboDetail detail : details) {
					int questionTypeId = detail.getInt("questionTypeId");
					List<ExamQuestion> list = ExamQuestion.dao
							.findByExamIdQuestionType(getInt("id"), null,
									questionTypeId);
					List<Integer> selectedIds = new ArrayList<Integer>();
					for (int i = 0; i < list.size(); i++) {
						int rnd = RandomUtils.nextInt(list.size());
						int questionId = list.get(rnd).getInt("questionId");
						// 判断是否已抽过
						if (selectedIds.contains(questionId)) {
							i--;
							continue;
						}
						selectedIds.add(questionId);

						Question question = Question.dao.loadModel(questionId);
						List<QuestionSub> subs = question.getSubs();
						for (QuestionSub sub : subs) {
							UserAnswerDetail uad = new UserAnswerDetail()
									.set("userAnswerId", ua.getInt("id"))
									.set("examId", getInt("id"))
									.set("questionId", question.getInt("id"))
									.set("questionSubId", sub.getInt("id"))
									.set("userId", userId)
									.set("questionTypeId",
											question.getInt("questionTypeId"))
									.set("rank", globalNum++);
							batchDetails.add(uad);
						}
					}
				}
			} else {
				List<ExamQuestion> list = ExamQuestion.dao
						.findByExamId(getInt("id"));
				// 固定试卷，每人题目相同，顺序相同
				for (ExamQuestion eq : list) {
					Question question = Question.dao.loadModel(eq
							.getInt("questionId"));
					List<QuestionSub> subs = question.getSubs();
					for (QuestionSub sub : subs) {
						UserAnswerDetail uad = new UserAnswerDetail()
								.set("userAnswerId", ua.getInt("id"))
								.set("examId", getInt("id"))
								.set("questionId", question.getInt("id"))
								.set("questionSubId", sub.getInt("id"))
								.set("userId", userId)
								.set("questionTypeId",
										question.getInt("questionTypeId"))
								.set("rank", globalNum++);
						batchDetails.add(uad);
					}
				}
			}
		}

		Db.batch(sql, columns, batchDetails, batchDetails.size());

	}

	@Override
	public boolean delete() {
		try {
			Db.update("update exams set enabled=0 , deleted=1 where id=?",
					getInt("id"));
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	// 练习试卷重置
	// 将考生针对此练习试着的答题记录另存为， 答题明细另存为
	public void reset() {
		String answerType = getStr("answerType");
		if ("practice".equals(answerType)) {

		}
	}

	public boolean getAnswered() {
		this.answered = (Boolean) super.get("answered");
		return answered;
	}

	public boolean isAnswered() {
		this.answered = (Boolean) super.get("answered");
		return answered;
	}

	public void setAnswered(boolean answered) {
		this.answered = answered;
		put("answered", answered);
	}

	public boolean isAnswerSubmit() {
		this.answerSubmit = (Boolean) super.get("answerSubmit");
		return answerSubmit;
	}

	public boolean getAnswerSubmit() {
		this.answerSubmit = (Boolean) super.get("answerSubmit");
		return answerSubmit;
	}

	public void setAnswerSubmit(boolean answerSubmit) {
		this.answerSubmit = answerSubmit;
		super.put("answerSubmit", answerSubmit);
	}

}
