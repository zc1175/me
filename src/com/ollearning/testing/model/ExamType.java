package com.ollearning.testing.model;

import java.util.List;

import com.ollearning.common.jfinal.Model;

/**
 * 考试类型（二级考试 ，职称考试等）
 * 
 * @author Administrator
 * 
 */
public class ExamType extends Model<ExamType> {

	private static final long serialVersionUID = -3347607578622868956L;

	private static final String CACHE_MODEL = "examType";

	public static ExamType dao = new ExamType();

	public ExamType() {
		super(CACHE_MODEL);
	}

	public List<ExamType> findAll() {
		return find("select * from exam_type");
	}

	public List<ExamType> getTopList() {
		return find("select * from exam_type where (parentId is null or parentId = -1) order by rank asc ");
	}

	/**
	 * 查询所有子类
	 * 
	 * @return
	 */
	public List<ExamType> getChildren() {
		return find("select * from exam_type where parentId=" + getInt("id")
				+ " order by rank asc");
	}

}
