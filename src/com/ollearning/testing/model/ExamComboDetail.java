package com.ollearning.testing.model;

import java.util.List;

import com.ollearning.common.jfinal.Model;

public class ExamComboDetail extends Model<ExamComboDetail> {
	private Subject subject;

	private static final long serialVersionUID = 4772634089043627967L;

	public static ExamComboDetail dao = new ExamComboDetail();
	public static final String cacheList = "examComboDetailList";

	public ExamComboDetail() {
		super("examComboDetail");
	}

	public List<ExamComboDetail> findByComboId(int comboId) {
		// String key = cacheList+"-"+comboId;
		// return
		// findByCache(cacheList,key,"select * from examcombodetail where comboid=?",
		// comboId);
		return find("select * from examcombodetail where comboid=?", comboId);
	}

	public ExamComboDetail find(Integer comboId, Integer questionTypeId) {
		return findFirst(
				"select * from examcombodetail where comboId=? and questionTypeId=?",
				comboId, questionTypeId);
	}

	public Subject getSubject() {
		Subject subject = (Subject) super.get("subject");
		if (null == subject) {
			subject = Subject.dao.findById(getInt("subjectId"));
		}
		return subject;
	}

	public void setSubject(Subject subject) {
		super.put("subject", subject);
	}

	public Double getScore() {
		Double unitScore = getBigDecimal("unitScore").doubleValue();
		int questionNum = getInt("questionNum");
		if (null == unitScore)
			return 0.0d;
		return unitScore * questionNum;
	}

	@Override
	public boolean equals(Object o) {
		if (null == o)
			return false;
		if (!(o instanceof ExamComboDetail))
			return false;
		ExamComboDetail obj = (ExamComboDetail) o;
		if (obj.getInt("id").equals(getInt("id")))
			return true;
		return false;
	}

}
