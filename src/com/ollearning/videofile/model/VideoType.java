package com.ollearning.videofile.model;

import java.util.List;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.ollearning.common.jfinal.Const;
import com.ollearning.common.jfinal.Model;
import com.ollearning.sys.model.Operator;

public class VideoType extends Model<VideoType> {

	private static final long serialVersionUID = -3787569129948031007L;
	public static VideoType dao = new VideoType();

	public VideoType() {
		super("videoType");
	}

	public VideoType getParent() {
		return loadModel(getInt("parentId"));
	}

	public Page<VideoType> getParents(int pageNumber) {
		return paginate(pageNumber, Const.PAGE_SIZE_FOR_ADMIN, "select *",
				"from video_type where parentId=-1 and deleted=0");
	}

	// 根据管理员查询文档分类
	public List<VideoType> findByManagerId(int managerId) {
		return find(
				"select * from video_type where id in ( select videoTypeId from video_type_manager where managerId=? and deleted=0)",
				managerId);
	}

	// 添加管理员
	public void addManager(int managerId) {
		Db.update(
				"insert into video_type_manager(videoTypeId,managerId) values(?,?)",
				this.getInt("id"), managerId);
		List<VideoType> childList = getListByParentId(getInt("id"), managerId);
		for (VideoType dt : childList) {
			Db.update(
					"insert into video_type_manager(videoTypeId,managerId) values(?,?)",
					dt.getInt("id"), managerId);
		}
	}

	public void clearManager() {
		Db.update("delete from video_type_manager where videoTypeId=?",
				getInt("id"));
		Db.update(
				"delete from video_type_manager where VideoTypeId in (select id from video_type where parentId=?)",
				getInt("id"));
	}

	// 获取管理员列表
	public List<Operator> getManagerList() {
		return Operator.dao.findByVideoTypeId(getInt("id"));
	}

	public String getManagerStr() {
		List<Operator> list = Operator.dao.findByVideoTypeId(getInt("id"));
		String ret = "";
		for (int i = 0; i < list.size(); i++) {
			ret += list.get(i).getStr("name");
			if (i < list.size() - 1) {
				ret += ",";
			}
		}
		return ret;
	}

	public List<VideoType> getChild() {
		return find("select * from video_type where deleted=0 and parentId=?",
				getInt("id"));
	}

	public List<VideoType> getParentList() {
		return find("select * from video_type where parentId=-1 and deleted=0");
	}

	public List<VideoType> getAllChild() {
		return find("select * from video_type where parentId!=-1 and deleted=0");
	}

	public List<VideoType> getParentList(Integer managerId) {
		if (Operator.dao.findById(managerId).isEditor()) {
			return find(
					"select * from video_type where deleted=0 and  id in (select videoTypeId from video_type_manager where managerId=?)",
					managerId);
		} else {
			return find("select * from video_type where deleted=0 and  parentId=-1");
		}
	}

	public Page<VideoType> getPageByParentId(int pageNumber, int parentId) {
		return paginate(pageNumber, Const.PAGE_SIZE_FOR_ADMIN, "select *",
				"from video_type where deleted=0 and parentId=?", parentId);
	}

	public List<VideoType> getListByParentId(int parentId, Integer managerId) {
		if (Operator.dao.findById(managerId).isEditor()) {
			return find(
					"select * from video_type where deleted=0 and parentId=? and id in (select videoTypeId from video_type_manager where managerId=?)",
					parentId, managerId);
		} else {
			return find(
					"select * from video_type where deleted=0 and parentId=?",
					parentId);
		}
	}

	public boolean delete() {
		this.set("deleted", 1).update();
		Db.update("update video_type set deleted=1 where parentId=?",
				getInt("id"));
		return true;
	}
}
