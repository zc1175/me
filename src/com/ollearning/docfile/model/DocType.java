package com.ollearning.docfile.model;

import java.util.List;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.ollearning.common.jfinal.Const;
import com.ollearning.common.jfinal.Model;
import com.ollearning.sys.model.Dict;
import com.ollearning.sys.model.Operator;

public class DocType extends Model<DocType> {

	private static final long serialVersionUID = -3682738748432138362L;

	public static DocType dao = new DocType();

	public DocType() {
		super("docType");
	}

	public DocType getParent() {
		return findById(getInt("parentId"));
	}

	public Page<DocType> getParents(int pageNumber) {
		return paginate(pageNumber, Const.PAGE_SIZE_FOR_ADMIN, "select *",
				"from doc_type where parentId=-1  and deleted= 0 ");
	}

	// 根据管理员查询文档分类
	public List<DocType> findByManagerId(int managerId) {
		return find(
				"select * from doc_type where id in ( select docTypeId from doc_type_manager where managerId=? and deleted=0)",
				managerId);
	}

	// 添加管理员
	public void addManager(int managerId) {
		Db.update(
				"insert into doc_type_manager(docTypeId,managerId) values(?,?)",
				this.getInt("id"), managerId);
		List<DocType> childList = getListByParentId(getInt("id"), managerId);
		for (DocType dt : childList) {
			Db.update(
					"insert into doc_type_manager(docTypeId,managerId) values(?,?)",
					dt.getInt("id"), managerId);
		}
	}

	public void clearManager() {
		Db.update("delete from doc_type_manager where docTypeId=?",
				getInt("id"));
		Db.update(
				"delete from doc_type_manager where docTypeId in (select id from doc_type where parentId=?)",
				getInt("id"));
	}

	// 获取管理员列表
	public List<Operator> getManagerList() {
		return Operator.dao.findByDocTypeId(getInt("id"));
	}

	public String getManagerStr() {
		List<Operator> list = Operator.dao.findByDocTypeId(getInt("id"));
		String ret = "";
		for (int i = 0; i < list.size(); i++) {
			ret += list.get(i).getStr("name");
			if (i < list.size() - 1) {
				ret += ",";
			}
		}
		return ret;
	}

	public List<DocType> getParentList() {
		return find("select * from doc_type where parentId=-1 and deleted=0 order by rank asc,id desc");
	}

	public List<DocType> getAllChild() {
		return find("select * from doc_type where parentId!=-1 and deleted=0 order by rank asc,id desc");
	}

	public List<DocType> getChild() {
		return find(
				"select * from doc_type where parentId=? and deleted=0 order by rank asc,id desc",
				getInt("id"));
	}

	public List<DocType> getParentList(Integer managerId) {
		if (Operator.dao.findById(managerId).isEditor()) {
			return find(
					"select * from doc_type where deleted=0 and  id in (select docTypeId from doc_type_manager where managerId=?) order by rank asc",
					managerId);
		} else {
			return find("select * from doc_type where deleted=0 and  parentId=-1 order by rank asc,id desc");
		}
	}

	public Page<DocType> getPageByParentId(int pageNumber, int parentId) {
		return paginate(
				pageNumber,
				Const.PAGE_SIZE_FOR_ADMIN,
				"select *",
				"from doc_type where deleted=0 and parentId=? order by rank asc",
				parentId);
	}

	public List<DocType> getListByParentId(int parentId, int managerId) {
		if (Operator.dao.findById(managerId).isEditor()) {
			return find(
					"select * from doc_type where parentId=? and deleted=0 and id in (select docTypeId from doc_type_manager where managerId=?) order by rank asc,id desc",
					parentId, managerId);
		} else {
			return find(
					"select * from doc_type where deleted=0 and parentId=? order by rank asc,id desc",
					parentId);
		}
	}

	public boolean delete() {
		this.set("deleted", 1).update();
		Db.update("update doc_type set deleted=1 where parentId=?",
				getInt("id"));
		return true;
	}

}
