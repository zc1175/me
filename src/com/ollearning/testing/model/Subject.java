package com.ollearning.testing.model;

import java.util.List;

import com.ollearning.common.jfinal.Model;

public class Subject extends Model<Subject> {

	private static final long serialVersionUID = -6025245671783490029L;

	private static final String CACHE_MODEL = "subject";

	public static final Subject dao = new Subject();

	public Subject() {
		super(CACHE_MODEL);
	}

	/**
	 * 查询指定考试下的科目列表
	 * 
	 * @param examTypeId
	 * @return
	 */
	public List<Subject> findByExamTypeId(Integer examTypeId) {
		String sql = "select * from subjects ";
		if (null != examTypeId && examTypeId > 0) {
			sql += " where examTypeId=" + examTypeId;
		}
		return find(sql);
	}

	public Subject findByName(String name) {
		return findFirst("select * from subjects where name like '%" + name
				+ "%'");
	}

	public ExamType getExamType() {
		return ExamType.dao.findById(getInt("examTypeId"));
	}
}
