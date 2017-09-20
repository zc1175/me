package com.ollearning.testing.model;

import java.util.List;

import com.ollearning.common.jfinal.Model;

public class ExamQuestion extends Model<ExamQuestion> {

	private static final long serialVersionUID = -6275663555862843070L;

	public static ExamQuestion dao = new ExamQuestion();
	private static final String cacheList = "examQuestionList";

	public ExamQuestion() {
		super("examQuestion");
	}

	// 根据试卷编号查询题目明细
	public List<ExamQuestion> findByExamId(Integer examId) {
		// String key = cacheList + "-" + examId;
		// return
		// findByCache(cacheList,key,"select * from examquestion where examId=?",examId);
		return find("select * from examquestion where examId=?", examId);
	}

	// 根据试卷编号，题目序号查询题目明细
	public ExamQuestion findByExamIdIndex(Integer examId, Integer index) {
		List<ExamQuestion> list = findByExamId(examId);
		if (list.size() >= index) {
			return list.get(index - 1);
		}
		return null;
	}

	// 根据试卷编号，题型编号查询试题列表
	public List<ExamQuestion> findByExamIdQuestionType(Integer examId,
			Integer subjectId, Integer questionTypeId) {
		String sql = "select * from examquestion where 1=1";
		if (null != examId && examId > 0)
			sql += " and examId=" + examId;
		if (null != subjectId && subjectId > 0)
			sql += " and subjectId=" + subjectId;
		if (null != questionTypeId && questionTypeId > 0)
			sql += " and questionTypeId=" + questionTypeId;

		String key = "examQuestion-" + examId + "-" + subjectId + "-"
				+ questionTypeId;
		// return findByCache(cacheList,key ,sql);
		return find(sql);
	}

	public Question getQuestion() {
		return Question.dao.loadModel(getInt("questionId"));
	}

}
