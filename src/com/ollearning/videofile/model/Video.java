package com.ollearning.videofile.model;

import java.util.List;

import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.ollearning.common.jfinal.Const;
import com.ollearning.common.jfinal.Model;
import com.ollearning.common.util.DateUtil;
import com.ollearning.docfile.model.DocType;
import com.ollearning.sys.model.Operator;
import com.ollearning.videofile.model.Video;
import com.ollearning.videofile.model.VideoFile;
import com.ollearning.videofile.model.VideoType;

public class Video extends Model<Video> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6118074744994657621L;
	public static Video dao = new Video();

	public class QueryBean {
		public String time;
		public String orderBy;
		public Integer typeId;

		public String toSqlQuery() {
			String sql = " ";
			if (null != typeId && typeId > 0) {
				sql += " and (typeId = "
						+ typeId
						+ " or typeId in (select id from video_type where parentId="
						+ typeId + ") )";
			}
			if (StrKit.notBlank(time) && !("-1".equals(time))) {
				sql += " and date_add(createTime,INTERVAL " + time + " DAY)>='"
						+ DateUtil.curTime() + "'";
			}
			if (StrKit.notBlank(orderBy) && !("-1".equals(orderBy))) {
				sql += " order by rank asc, " + orderBy;
			} else {
				sql += " order by rank asc,id desc";
			}
			return sql;
		}
	}

	public Video() {
		super("video");
	}

	public Video.QueryBean newQueryBean() {
		return new Video.QueryBean();
	}

	public Video findByPid(String pid) {
		return findFirst("select * from videos where pid=?", pid);
	}

	public VideoFile getVideoFile() {
		return VideoFile.dao.findByVideoId(getInt("id"));
	}

	public Page<Video> getPage(Integer pageNumber, int managerId) {
		String sql = "from videos where 1=1";
		if (Operator.dao.findById(managerId).isEditor()) {
			sql += " and typeId in (select videoTypeId from video_type_manager where managerId="
					+ managerId + ") ";
		}
		sql += " order by id desc";
		return paginate(pageNumber, Const.PAGE_SIZE_FOR_ADMIN, "select *", sql);
	}

	// for front invocation
	public Page<Video> getPageByBean(Integer pageNumber, Video.QueryBean bean) {
		return paginate(pageNumber, 16, "select *",
				"from videos where 1=1 and checked=1 and videoCollectionId is null "
						+ bean.toSqlQuery());
	}

	public List<Video> getVideoListForIndex() {
		return find("select * from videos where checked=1 order by id desc limit 5");
	}

	public String getTypeName() {
		VideoType dt = VideoType.dao.findById(getInt("typeId"));
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
		VideoType videoType = VideoType.dao.loadModel(getInt("typeId"));
		if (null == videoType)
			return;

		videoType.set(
				"videoNum",
				(null == videoType.getInt("videoNum") ? 0 : videoType
						.getInt("videoNum")) + num).update();
		VideoType videoTypeParent = videoType.getParent();
		if (null != videoTypeParent) {
			videoTypeParent.set(
					"videoNum",
					(null == videoTypeParent.getInt("videoNum") ? 0
							: videoTypeParent.getInt("videoNum")) + num)
					.update();
			VideoType docTypePParent = videoTypeParent.getParent();
			if (null != docTypePParent) {
				docTypePParent.set(
						"videoNum",
						(null == docTypePParent.getInt("videoNum") ? 0
								: docTypePParent.getInt("videoNum")) + num)
						.update();
			}
		}
	}

	/**
	 * 查询关联文档(同一类别的其它文档 ，按阅读量倒序
	 */
	public List<Video> getRelatedVideos(Integer videoId) {
		Integer typeId = findById(videoId).getInt("typeId");
		return find(
				"select * from videos where typeId=? and checked=1 and id <> ? order by clickNum desc limit 6",
				typeId, videoId);
	}

	public Page<Video> findByVideoCollectionId(Integer pageNumber,
			Integer videoCollectionId) {
		return paginate(
				pageNumber,
				Const.PAGE_SIZE_FOR_FRONT,
				"select *",
				"from videos where videoCollectionId=? and checked=1 order by rank asc",
				videoCollectionId);
	}

	public VideoCollection getVideoCollection() {
		return VideoCollection.me.findById(getInt("videoCollectionId"));
	}

	public String getVideoCollectionName() {
		VideoCollection vc = getVideoCollection();
		if (null != vc)
			return vc.getStr("name");
		return "未指定";
	}
}
