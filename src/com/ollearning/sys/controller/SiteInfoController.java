package com.ollearning.sys.controller;

import java.io.File;
import java.util.Date;

import com.jfinal.aop.Before;
import com.jfinal.upload.UploadFile;
import com.ollearning.common.controller.BaseController;
import com.ollearning.interceptors.AdminACLInterceptor;
import com.ollearning.site.model.SiteInfo;

@Before(AdminACLInterceptor.class)
public class SiteInfoController extends BaseController {

	public void index() {
		SiteInfo siteInfo = SiteInfo.dao.findById(1);
		setAttr("siteInfo", siteInfo);
		render("/admin/siteInfo/siteInfo.html");
	}

	public void save() {

		String[] pics = new String[5];
		for (int i = 0; i < 5; i++) {
			String picPath = uploadFile("file" + (i + 1));
			if (null != picPath) {
				pics[i] = picPath;
			}
		}

		SiteInfo info = getModel(SiteInfo.class);
		for (int i = 0; i < pics.length; i++) {
			if (null != pics[i]) {
				info.set("indexAd" + (i + 1), pics[i]);
			}
		}

		info.update();
		redirect("/admin/siteInfo?t=" + new Date().getTime());

	}

	private String uploadFile(String formFileName) {
		try {
			System.out.println("uploadFile===>" + formFileName);
			String savePath = getRequest().getRealPath("/") + "/upload_pics/";
			int maxSize = 5 * 1024 * 1024;// 5M
			UploadFile upFile = getFile(formFileName, savePath, maxSize,
					"utf-8");
			if (null == upFile)
				return null;
			File file = upFile.getFile();
			String fileExt = upFile.getOriginalFileName().substring(
					upFile.getOriginalFileName().lastIndexOf("."));
			String newFileName = System.currentTimeMillis() + fileExt;
			file.renameTo(new File(savePath + newFileName));
			return "/upload_pics/" + newFileName;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
