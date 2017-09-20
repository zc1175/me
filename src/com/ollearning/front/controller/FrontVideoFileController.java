package com.ollearning.front.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.jfinal.plugin.activerecord.Page;
import com.ollearning.common.controller.BaseController;
import com.ollearning.docfile.model.DocType;
import com.ollearning.videofile.model.Video;
import com.ollearning.videofile.model.VideoCollection;
import com.ollearning.videofile.model.VideoType;
import com.ollearning.videofile.model.VideoFile;

public class FrontVideoFileController extends BaseController {

	public void index() {
		Integer typeId = getParaToInt("typeId", -1);
		Integer time = getParaToInt("time", -1);
		String orderBy = getPara("orderBy", "-1");
		Integer pageNo = getParaToInt("pageNo", 1);

		setAttr("typeId", typeId);
		setAttr("time", time);
		setAttr("orderBy", orderBy);

		Video.QueryBean qb = Video.dao.newQueryBean();
		qb.typeId = typeId;
		qb.time = String.valueOf(time);
		qb.orderBy = orderBy;

		Page<Video> videoPage = Video.dao.getPageByBean(pageNo, qb);
		setAttr("videoPage", videoPage);

		List<VideoType> typeList = VideoType.dao.getParentList();
		setAttr("typeList", typeList);
		setAttr("basePath", getBasePath());

		if (-1 != typeId) {
			VideoType videoType = VideoType.dao.findById(typeId);
			setAttr("videoType", videoType);
			if (null != videoType && videoType.getInt("parentId") != -1) {
				setAttr("parent", videoType.getParent());
				setAttr("parentId", videoType.getInt("parentId"));
				if (null != videoType.getParent()
						&& -1 != videoType.getParent().getInt("parentId")) {
					setAttr("pparent", VideoType.dao.findById(videoType
							.getParent().getInt("parentId")));
					setAttr("ppId",
							VideoType.dao
									.findById(videoType.getInt("parentId"))
									.getInt("parentId"));
				}
			}
		}

		// 查询视频集
		setAttr("videoCollectionPage", VideoCollection.me.findByPage(1));

		render("/front/video/index.html");
	}

	public void view() {
		Video video = Video.dao.findByPid(getPara());
		if (null == video) {
			render("无效访问！");
			return;
		}
		video.set(
				"clickNum",
				(null == video.getInt("clickNum") ? 0 : video
						.getInt("clickNum")) + 1).update();
		setAttr("video", video);
		setAttr("basePath", getBasePath());
		render("/front/video/video.html");
	}

}
