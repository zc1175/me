package com.ollearning.videofile.controller;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.upload.UploadFile;
import com.ollearning.common.controller.BaseController;
import com.ollearning.common.util.CodeGenerator;
import com.ollearning.common.util.DateUtil;
import com.ollearning.common.util.FileUtil;
import com.ollearning.common.util.VideoUtil;
import com.ollearning.global.BIConfig;
import com.ollearning.interceptors.AdminACLInterceptor;
import com.ollearning.sys.model.Dict;
import com.ollearning.videofile.model.Video;
import com.ollearning.videofile.model.VideoCollection;
import com.ollearning.videofile.model.VideoFile;
import com.ollearning.videofile.model.VideoType;

@Before(AdminACLInterceptor.class)
public class VideoController extends BaseController {
	private int maxSize = 500 * 1000 * 1000;
	private String basePath = null;

	public void index() {
		VideoFile.dao.removeAllPageCache();
		Page<Video> videofilePage = Video.dao.getPage(getParaToInt(1, 1),
				getLoginOperator().getInt("id"));
		setAttr("videofilePage", videofilePage);
		setAttr("actionUrl", "/admin/videofile/index-");
		render("/admin/videofile/list.html");
	}

	public void add() {
		_form();
	}

	@SuppressWarnings("deprecation")
	public void save() {

		this.basePath = getRequest().getRealPath("/");

		// 上传文件
		String saveDir = basePath + BIConfig.VIDEO_DIR;
		String newFileName = "";
		String fileExt = "";
		String filePath = "";

		UploadFile upFile = getFile("file1", saveDir, maxSize, "utf-8");
		if (null != upFile) {
			File file = upFile.getFile();
			fileExt = upFile.getOriginalFileName().substring(
					upFile.getOriginalFileName().lastIndexOf("."));
			newFileName = System.currentTimeMillis() + "";
			filePath = saveDir + newFileName + fileExt;

			file.renameTo(new File(filePath));
		}
		Video video = getModel(Video.class);

		if (null == video.getInt("id")) {
			video.set("creatorId", getLoginOperator().getInt("id"))
					.set("creatorName", getLoginOperator().getStr("name"))
					.set("createTime", DateUtil.curTime()).set("clickNum", 0)
					.set("downloadNum", 0).set("collectNum", 0)
					.set("commentNum", 0);
			if (getLoginOperator().getInt("roleId").intValue() == Integer
					.parseInt(Dict.dao.getValue("SYS_VALUE", "ROLE_ADMIN_ID"))) {
				video.set("checked", 1);
			}
			video.set("pid", CodeGenerator.getToken()).save();
			video.updateTypeNum(1); // 增加1
		} else {
			video.update();
		}

		if (null != upFile) {
			VideoFile videoFile = new VideoFile();
			videoFile
					.set("pid", UUID.randomUUID().toString())
					.set("basePath", basePath)
					.set("filePath", BIConfig.VIDEO_DIR + newFileName + fileExt)
					.set("uploadTime", DateUtil.curTime())
					.set("oriFileName", upFile.getOriginalFileName())
					.set("fileType",
							VideoFile.getFileType(upFile.getOriginalFileName()))
					.set("fileSize",
							FileUtil.getFileSize(new File(saveDir + newFileName
									+ fileExt)))
					.set("videoId", video.getInt("id"))
					.set("fileName", newFileName).set("isDelete", 0).save();

			// 生成截图
			String fileName = System.currentTimeMillis() + ".jpg";
			String captureFile = videoFile.getStr("basePath")
					+ BIConfig.CAPTURE_DIR + fileName;
			boolean isCreate = VideoUtil.createCapture(filePath, captureFile);
			if (isCreate) {
				videoFile.set("capture", BIConfig.CAPTURE_DIR + fileName)
						.update();
			}

			String isCreateFlv = getPara("isCreateFLV");
			if (StrKit.notBlank(isCreateFlv) && "1".equals(isCreateFlv)) {
				VideoUtil.convert2FLV(
						videoFile.getStr("basePath")
								+ videoFile.getStr("filePath"),
						videoFile.getStr("basePath") + BIConfig.VIDEO_DIR
								+ newFileName + ".flv");
			}

		}

		try {
			if (null != video.get("videoCollectionId")) {
				VideoCollection vc = VideoCollection.me.findById(video
						.get("videoCollectionId"));
				vc.updateVideoNum();
				vc.update();
			}
		} catch (Exception e) {
		}

		redirect("/admin/videofile");
	}

	public void delete() {
		Video video = Video.dao.findById(getParaToInt());
		try {
			new File(video.getVideoFile().getStr("basePath")
					+ video.getVideoFile().getStr("filePath")).delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
		video.delete();
		index();
	}

	public void edit() {
		Video videofile = Video.dao.findById(getParaToInt(0));
		setAttr("video", videofile);
		_form();
	}

	private void _form() {
		setAttr("videoTypeList",
				VideoType.dao.getParentList(getLoginOperator().getInt("id")));
		setAttr("videoCollectionList", VideoCollection.me.findAll());
		render("/admin/videofile/videofile.html");
	}

	/**
	 * AJAX生成视频的截图
	 */
	public void ajaxCreateCapture() {
		Map<String, String> ret = new HashMap<String, String>();
		try {
			int id = getParaToInt("id");
			Video video = Video.dao.findById(id);
			if (null == video) {
				ret.put("result", "error");
				renderJson(ret);
				return;
			}
			VideoFile videoFile = video.getVideoFile();
			String fileName = System.currentTimeMillis() + ".jpg";
			String captureFile = videoFile.getStr("basePath")
					+ BIConfig.CAPTURE_DIR + fileName;
			boolean isCreate = VideoUtil
					.createCapture(
							videoFile.getStr("basePath")
									+ videoFile.getStr("filePath"), captureFile);
			if (isCreate) {
				videoFile.set("capture", BIConfig.CAPTURE_DIR + fileName)
						.update();
				ret.put("result", "success");
			} else {
				ret.put("result", "error");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			ret.put("result", "error");
		}
		renderJson(ret);

	}

	/**
	 * 转换为FLV
	 */
	public void ajaxCreateFLV() {
		Map<String, String> ret = new HashMap<String, String>();
		try {
			int id = getParaToInt("id");
			VideoFile videoFile = VideoFile.dao.findById(id);
			String fileName = System.currentTimeMillis() + ".flv";
			String captureFile = videoFile.getStr("basePath")
					+ BIConfig.CAPTURE_DIR + fileName;
			boolean isCreate = VideoUtil
					.convert2FLV(
							videoFile.getStr("basePath")
									+ videoFile.getStr("filePath"), captureFile);
			if (isCreate) {
				videoFile.set("capture", BIConfig.CAPTURE_DIR + fileName)
						.update();
				ret.put("result", "success");
			} else {
				ret.put("result", "error");
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			ret.put("result", "error");
		}
		renderJson(ret);

	}

}
