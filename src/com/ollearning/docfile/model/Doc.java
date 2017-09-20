package com.ollearning.docfile.model;

import java.util.List;

import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.ollearning.common.jfinal.Const;
import com.ollearning.common.jfinal.Model;
import com.ollearning.common.util.DateUtil;
import com.ollearning.sys.model.Operator;

public class Doc extends Model<Doc> {

	private static final long serialVersionUID = -2986884772965811140L;

	public static Doc dao = new Doc();

	public class QueryBean {
		public String time;
		public String orderBy;
		public Integer typeId;

		public String toSqlQuery() {
			String sql = " ";
			if (null != typeId && typeId > 0) {
				sql += " and (typeId = "
						+ typeId
						+ " or typeId in (select id from doc_type where parentId="
						+ typeId + ") )";
			}
			if (StrKit.notBlank(time) && !("-1".equals(time))) {
				sql += " and date_add(createTime,INTERVAL " + time + " DAY)>='"
						+ DateUtil.curTime() + "'";
			}
			if (StrKit.notBlank(orderBy) && !("-1".equals(orderBy))) {
				sql += " order by " + orderBy + " desc";
			} else {
				sql += " order by id desc";
			}
			return sql;
		}
	}

	public Doc() {
		super("doc");
	}

	public Doc.QueryBean newQueryBean() {
		return new Doc.QueryBean();
	}

	public Doc findByPid(String pid) {
		return findFirst("select * from docs where pid=?", pid);
	}

	public DocFile getDocFile() {
		return DocFile.dao.findByDocId(getInt("id"));
	}

	public Page<Doc> getPage(Integer pageNumber, int managerId) {
		String sql = "from docs where 1=1";
		if (Operator.dao.findById(managerId).isEditor()) {
			sql += " and typeId in (select docTypeId from doc_type_manager where managerId="
					+ managerId + ") ";
		}
		sql += " order by id desc";
		return paginate(pageNumber, Const.PAGE_SIZE_FOR_ADMIN, "select *", sql);
	}

	// for front invocation
	public Page<Doc> getPageByBean(Integer pageNumber, Doc.QueryBean bean) {
		return paginate(pageNumber, 16, "select *",
				"from docs where 1=1 and checked=1 " + bean.toSqlQuery());
	}

	public List<Doc> getDocListForIndex() {
		return find("select * from docs where checked=1 order by id desc limit 5");
	}

	// 清除所有关键字
	public void clearKeywords() {
		Db.update("delete from docs_keywords where docId=?", getInt("id"));
	}

	// 添加关键字
	public void addKeywords(String keywords) {
		try {
			clearKeywords();
			String[] keywordArray = keywords.split(" ");
			for (String keyword : keywordArray) {
				Db.update(
						"insert into docs_keywords(docId,keywords) values(?,?)",
						getInt("id"), keyword);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getKeywords() {
		List<DocKeywords> list = DocKeywords.dao.getByDocId(getInt("id"));
		String ret = "";
		for (DocKeywords s : list) {
			ret += s.getStr("keywords");
			ret += " ";
		}
		return ret;
	}

	// -------------

	public String getTypeName() {
		DocType dt = DocType.dao.findById(getInt("typeId"));
		if (null != dt)
			return dt.getStr("name");
		return "";
	}

	public String getCheckedStr() {
		Boolean checked = getBoolean("checked");
		if (null != checked && checked)
			return "<font color=green>已审</font>";
		return "<font color=red>未审</font>";
	}

	/**
	 * 更新所属分类对应的文档的数量
	 * 
	 * @param num
	 */
	public void updateTypeNum(int num) {
		DocType docType = DocType.dao.loadModel(getInt("typeId"));
		if (null == docType)
			return;

		docType.set(
				"docNum",
				(null == docType.getInt("docNum") ? 0 : docType
						.getInt("docNum")) + num).update();
		DocType docTypeParent = docType.getParent();
		if (null != docTypeParent) {
			docTypeParent.set(
					"docNum",
					(null == docTypeParent.getInt("docNum") ? 0 : docTypeParent
							.getInt("docNum")) + num).update();
			DocType docTypePParent = docTypeParent.getParent();
			if (null != docTypePParent) {
				docTypePParent.set(
						"docNum",
						(null == docTypePParent.getInt("docNum") ? 0
								: docTypePParent.getInt("docNum")) + num)
						.update();
			}
		}
	}

	/**
	 * 查询关联文档(按关键字查询)
	 */
	public List<Doc> getRelatedDocs(Integer docId) {
		String sql = "select * from docs where id in (select distinct docId from docs_keywords where docId<>? and keywords in ( select keywords from docs_keywords where docId=? ))";
		return find(sql, docId, docId);
	}

	/**
	 * 返回文档的图片标识
	 * 
	 * @return
	 */
	public String getDocTypeImg() {
		DocFile file = getDocFile();
		if (null == file)
			return "";
		String oriFileName = getDocFile().getStr("oriFileName");
		String fileExt = oriFileName
				.substring(oriFileName.lastIndexOf(".") + 1);
		if (fileExt.equalsIgnoreCase("doc") || fileExt.equalsIgnoreCase("docx")) {
			return "word.png";
		} else if (fileExt.equalsIgnoreCase("xls")
				|| fileExt.equalsIgnoreCase("xlsx")) {
			return "excel.png";
		} else if (fileExt.equalsIgnoreCase("ppt")
				|| fileExt.equalsIgnoreCase("pptx")) {
			return "ppt.png";
		} else if (fileExt.equalsIgnoreCase("pdf")) {
			return "pdf.png";
		}
		return "word.png";

	}

}
