package com.ollearning.front.controller;

import java.util.List;

import com.ollearning.common.controller.BaseController;
import com.ollearning.videofile.model.Video;
import com.ollearning.videofile.model.VideoCollection;
import com.ollearning.videofile.model.VideoType;

public class FrontVideoCollectionController extends BaseController {

	public void index() {

		List<VideoType> typeList = VideoType.dao.getParentList();
		setAttr("typeList", typeList);
		setAttr("basePath", getBasePath());

		setAttr("page", VideoCollection.me.findByPage(getParaToInt(1, 1)));
		setAttr("actionUrl", "/videoCollection/index-");
		render("/front/videoCollection/index.html");

	}

	public void view() {

		List<VideoType> typeList = VideoType.dao.getParentList();
		setAttr("typeList", typeList);
		setAttr("basePath", getBasePath());

		setAttr("viewCollection",
				VideoCollection.me.findById(getParaToInt("id")));
		setAttr("page", Video.dao.findByVideoCollectionId(
				getParaToInt("page", 1), getParaToInt("id")));
		render("/front/videoCollection/view.html");

	}

}
