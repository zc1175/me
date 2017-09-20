package com.ollearning.testing.model;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.jfinal.kit.StrKit;

/**
 * 试题查询条件封装
 * 
 * @author Administrator
 * 
 */
public class QuestionQuery {

	private String examTypeId; // 试卷分类
	private String subjectId; // 科目编号
	private String typeId; // 试题类型
	private String keywords; // 关键字
	private String knowledges; // 知识点
	private String diffcult; // 难易程度
	private String title; // 标题
	private String selectFrom; // 开始题目序号
	private String selectTo; // 结束题目序号
	private String year;	//根据年份查询

	
	/**
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public QuestionQuery set(String key, Object value) {
		if (null == key || key.isEmpty())
			return this;
		Field[] fields = QuestionQuery.class.getDeclaredFields();
		for (Field field : fields) {
			try {
				if (field.getName().equals(key)) {
					String setMethodName = "set"
							+ key.substring(0, 1).toUpperCase()
							+ key.substring(1);
					Method method = QuestionQuery.class.getMethod(
							setMethodName, new Class[] { String.class });
					method.invoke(this, new Object[] { String.valueOf(value) });
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return this;
	}

	public String toUrlParam() {
		String param = "?2>1";
		if (null != examTypeId && !examTypeId.isEmpty()
				&& Integer.parseInt(examTypeId) > 0) {
			param += "&questionQuery.examTypeId=" + examTypeId;
		}
		if (null != subjectId && !subjectId.isEmpty()
				&& Integer.parseInt(subjectId) > 0) {
			param += "&questionQuery.subjectId=" + subjectId;
		}
		if (null != typeId && !typeId.isEmpty() && Integer.parseInt(typeId) > 0) {
			param += "&questionQuery.typeId=" + typeId;
		}
		if (null != keywords && !keywords.isEmpty()) {
			param += "&questionQuery.keywords=" + keywords + "";
		}
		if (null != title && !title.isEmpty()) {
			param += "&questionQuery.title=" + title + "";
		}
		if (null != knowledges && !knowledges.isEmpty()) {
			param += "&questionQuery.knowledges=" + knowledges + "";
		}
		if (null != diffcult && !diffcult.isEmpty()) {
			param += "&questionQuery.diffcult=" + diffcult + "";
		}
		return param;
	}

	public String toString() {
		String sql = "";
		if (null != examTypeId && !examTypeId.isEmpty()
				&& Integer.parseInt(examTypeId) > 0) {
			sql += " and  examTypeId=" + examTypeId;
		}
		if (null != subjectId && !subjectId.isEmpty()
				&& Integer.parseInt(subjectId) > 0) {
			sql += " and subjectId=" + subjectId;
		}
		if (null != typeId && !typeId.isEmpty() && Integer.parseInt(typeId) > 0) {
			sql += " and questionTypeId=" + typeId;
		}
		if (null != keywords && !keywords.isEmpty()) {
			sql += " and keywords like '%" + keywords + "%'";
		}
		if (null != title && !title.isEmpty()) {
			sql += " and title like '%" + title + "%'";
		}
		if (null != knowledges && !knowledges.isEmpty()) {
			sql += " and knowledges like '%" + knowledges + "%'";
		}
		if (null != diffcult && !diffcult.isEmpty()) {
			sql += " and diffcult='" + diffcult + "'";
		}
		
		//判断条件年份
		if (null != year && !year.isEmpty()) {
			sql += " and year='" + year + "'";
		}
		
		if (StrKit.notBlank(selectTo) && Integer.parseInt(selectTo) > 0) {
			sql += " limit "
					+ selectFrom
					+ ","
					+ (Integer.parseInt(selectTo)
							- Integer.parseInt(selectFrom) + 1);
		}
		sql += " order by id desc";
		System.out.println("QueryBean ==> " + sql);
		return sql;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	
	public String getExamTypeId() {
		return examTypeId;
	}

	public void setExamTypeId(String examTypeId) {
		this.examTypeId = examTypeId;
	}

	public String getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(String subjectId) {
		this.subjectId = subjectId;
	}

	public String getTypeId() {
		return typeId;
	}

	public void setTypeId(String typeId) {
		this.typeId = typeId;
	}

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public String getKnowledges() {
		return knowledges;
	}

	public void setKnowledges(String knowledges) {
		this.knowledges = knowledges;
	}

	public String getDiffcult() {
		return diffcult;
	}

	public void setDiffcult(String diffcult) {
		this.diffcult = diffcult;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSelectFrom() {
		return selectFrom;
	}

	public void setSelectFrom(String selectFrom) {
		this.selectFrom = selectFrom;
	}

	public String getSelectTo() {
		return selectTo;
	}

	public void setSelectTo(String selectTo) {
		this.selectTo = selectTo;
	}

}
