package com.ollearning.testing.model;

import java.util.List;

import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.ollearning.common.jfinal.Const;
import com.ollearning.common.jfinal.Model;

public class ExamCombo extends Model<ExamCombo> {

	private static final long serialVersionUID = -2102060572541161168L;

	public static ExamCombo dao = new ExamCombo();

	public ExamCombo() {
		super("examCombo");
	}

	public Page<ExamCombo> findByPage(ExamCombo examCombo, int pageNumber) {

		String whereClause = "";
		if (null != examCombo && null != examCombo.getInt("examTypeId")
				&& examCombo.getInt("examTypeId") > 0) {
			whereClause += " and examTypeId=" + examCombo.getInt("examTypeId");
		}
		if (null != examCombo && null != examCombo.getStr("name")
				&& !StrKit.notBlank(examCombo.getStr("name"))) {
			whereClause += " and name like '%" + examCombo.getStr("name")
					+ "%'";
		}
		whereClause += " and deleted=0";
		Page<ExamCombo> page = paginate(pageNumber, Const.PAGE_SIZE_FOR_ADMIN,
				"select *", " from examcombo where 1=1 " + whereClause
						+ " order by id desc");
		return loadModelPage(page);

	}

	public List<ExamComboDetail> getDetails() {
		return ExamComboDetail.dao.findByComboId(getInt("id"));
	}

	public void deleteDetails() {
		Db.update("delete from examcombodetail where comboId=?", getInt("id"));
	}

	public List<ExamCombo> find(ExamCombo examCombo) {
		String whereClause = "";
		if (null != examCombo && null != examCombo.getInt("examTypeId")
				&& examCombo.getInt("examTypeId") > 0) {
			whereClause += " and examTypeId=" + examCombo.getInt("examTypeId");
		}
		if (null != examCombo && null != examCombo.getStr("name")
				&& !StrKit.notBlank(examCombo.getStr("name"))) {
			whereClause += " and name like '%" + examCombo.getStr("name")
					+ "%'";
		}
		return find("select * from examcombo where 1=1 " + whereClause);
	}

	public void updateQuestionNum() {
		List<ExamComboDetail> details = getDetails();
		int questionNum = 0;
		for (ExamComboDetail detail : details) {
			questionNum += detail.getInt("questionNum");
		}
		set("questionNum", questionNum).update();
	}
}
