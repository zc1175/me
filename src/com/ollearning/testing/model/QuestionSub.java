package com.ollearning.testing.model;

import java.util.List;

import com.ollearning.common.jfinal.Model;

public class QuestionSub extends Model<QuestionSub> {

	private static final long serialVersionUID = 3631958533597531376L;

	public static QuestionSub dao = new QuestionSub();
	public static final String cacheList = "questionSubList";

	public QuestionSub() {
		super("questionSub");
	}

	public List<QuestionSub> findByQuestionId(Integer questionId) {
		String key = "questionsub-" + questionId;
		return findByCache(cacheList, key,
				"select * from questionsubs where questionId=?", questionId);
	}

}
