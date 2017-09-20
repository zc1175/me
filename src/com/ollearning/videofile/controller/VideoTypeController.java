package com.ollearning.videofile.controller;

import java.util.List;

import com.jfinal.aop.Before;
import com.ollearning.common.controller.BaseController;
import com.ollearning.docfile.model.DocType;
import com.ollearning.interceptors.AdminACLInterceptor;
import com.ollearning.sys.model.Dict;
import com.ollearning.sys.model.Operator;
import com.ollearning.videofile.model.VideoType;

@Before(AdminACLInterceptor.class)
public class VideoTypeController extends BaseController {

	public void index() {
		setAttr("videoPage", VideoType.dao.getParents(getParaToInt(1, 1)));
		setAttr("actionUrl", "/admin/videoType/index-");
		render("/admin/videoType/list.html");
	}

	public void child() {
		int parentId = getParaToInt();
		setAttr("videoType", VideoType.dao.findById(parentId));
		setAttr("videoList", VideoType.dao.getListByParentId(parentId,
				getLoginOperator().getInt("id")));
		setAttr("parentId", parentId);
		render("/admin/videoType/child.html");
	}

	public void add() {
		int parentId = getParaToInt(0, -1);
		setAttr("videoType", new VideoType().set("parentId", parentId));
		List<Operator> managerList = Operator.dao.findByRoleId(Integer
				.parseInt(Dict.dao.getValue("SYS_VALUE", "ROLE_EDITOR_ID")));
		setAttr("managerList", managerList);
		_form();
	}

	public void edit() {
		setAttr("videoType", VideoType.dao.findById(getParaToInt()));
		List<Operator> managerList = Operator.dao.findByRoleId(Integer
				.parseInt(Dict.dao.getValue("SYS_VALUE", "ROLE_EDITOR_ID")));
		List<Operator> checkedManagerList = VideoType.dao.findById(
				getParaToInt()).getManagerList();
		for (Operator op : managerList) {
			if (checkedManagerList.contains(op)) {
				op.setChecked(true);
			}
		}
		setAttr("managerList", managerList);
		_form();
	}

	public void save() {
		VideoType videoType = getModel(VideoType.class);
		String[] managerIds = getParaValues("managerId");

		videoType.set("deleted", 0);
		if (null != videoType.getInt("id")) {
			videoType.update();
		} else {
			try {
				if (null == videoType.getInt("parentId")
						|| -1 == videoType.getInt("parentId")) {
					videoType.set("level", 1).set("childNum", 0);
				} else {
					videoType.set("level",
							DocType.dao.findById(videoType.getInt("parentId"))
									.getInt("level") + 1);
					VideoType parent = VideoType.dao.findById(videoType
							.getInt("parentId"));
					parent.set("childNum", parent.getInt("childNum") + 1)
							.update();
				}
			} catch (Exception e) {
			}
			videoType.set("videoNum", 0).save();
		}
		videoType.clearManager();
		if (null != managerIds) {
			for (String managerId : managerIds) {
				videoType.addManager(Integer.parseInt(managerId));
			}
		}

		if (-1 == videoType.getInt("parentId")) {
			redirect("/admin/videoType");
		} else {
			redirect("/admin/videoType/child/" + videoType.getInt("parentId"));
		}

	}

	public void delete() {
		VideoType.dao.findById(getParaToInt()).delete();
		redirect("/admin/videoType");
	}

	private void _form() {
		setAttr("parentList", VideoType.dao.getParentList());
		render("/admin/videoType/videoType.html");
	}

	// 异步获取子分类列表
	public void getChildJson() {
		renderJson(VideoType.dao.getListByParentId(getParaToInt(0),
				getLoginOperator().getInt("id")));
	}
}
