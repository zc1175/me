package com.ollearning.testing.model;

import java.util.List;

import com.ollearning.common.jfinal.Model;

/**
 * 试题类型
 * 
 * @author Administrator
 * 
 */
public class QuestionType extends Model<QuestionType> {

	private static final long serialVersionUID = -2628299073740648084L;

	private static final String CACHE_MODEL = "questionType";

	public static final QuestionType dao = new QuestionType();

	public QuestionType() {
		super(CACHE_MODEL);
	}

	public List<QuestionType> findAll() {
		return find("select * from questiontype");
	}

	public String getSubjective() {
		boolean issub = getBoolean("isSubjective");
		return issub ? "主观" : "客观";
	}

	public QuestionType findByName(String name) {
		return findFirst("select * from questiontype where name like '%" + name
				+ "%'");
	}

}
