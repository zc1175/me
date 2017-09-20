package com.ollearning.videofile.controller;

import com.ollearning.common.controller.BaseController;
import com.ollearning.videofile.model.VideoCollection;

public class VideoCollectionController extends BaseController {

	public void index() {
		int pageNumber = getParaToInt(1, 1);
		setAttr("page", VideoCollection.me.findByPage(pageNumber));
		render("/admin/video_collection/list.html");
	}

	public void add() {
		_form();
	}

	private void _form() {
		render("/admin/video_collection/videoCollection.html");
	}

	public void edit() {
		setAttr("videoCollection", VideoCollection.me.findById(getParaToInt()));
		_form();

	}

	public void save() {
		VideoCollection model = getModel(VideoCollection.class);
		model.set("enabled", 1).set("videoNum", 0);
		model.updateVideoNum();
		if (null != model.get("id")) {
			model.update();
		} else {
			model.save();
		}
		redirect("/admin/videoCollection");
	}

	public void delete() {
		VideoCollection model = VideoCollection.me.findById(getParaToInt());
		if (null != model) {
			model.set("enabled", 0).update();
		}
		redirect("/admin/videoCollection");
	}

}
