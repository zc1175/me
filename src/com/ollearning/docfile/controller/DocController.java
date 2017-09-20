package com.ollearning.docfile.controller;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jfinal.aop.Before;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.upload.UploadFile;
import com.ollearning.common.controller.BaseController;
import com.ollearning.common.util.CodeGenerator;
import com.ollearning.common.util.DateUtil;
import com.ollearning.common.util.FileUtil;
import com.ollearning.common.util.Office2PDF;
import com.ollearning.common.util.PDF2HtmlUtil;
import com.ollearning.common.util.PDF2SWF;
import com.ollearning.docfile.model.Doc;
import com.ollearning.docfile.model.DocFile;
import com.ollearning.docfile.model.DocType;
import com.ollearning.global.BIConfig;
import com.ollearning.global.ExchangeQueue;
import com.ollearning.interceptors.AdminACLInterceptor;
import com.ollearning.sys.model.Dict;
import com.ollearning.testing.model.ExamType;

@Before(AdminACLInterceptor.class)
public class DocController extends BaseController {

	private int maxSize = 50 * 1000 * 1000;
	private String basePath = null;

	public void index() {
		Page<Doc> docPage = Doc.dao.getPage(getParaToInt(1, 1),
				getLoginOperator().getInt("id"));
		setAttr("adminId", Dict.dao.getValue("SYS_VALUE", "ROLE_ADMIN_ID"));
		setAttr("docPage", docPage);
		setAttr("basePath", getBasePath());
		setAttr("actionUrl", "/admin/docfile/index-");
		setAttr("pageNo", getParaToInt(1, 1));
		render("/admin/docfile/list.html");
	}

	public void add() {
		_form();
	}

	@SuppressWarnings("deprecation")
	public void save() {
		this.basePath = getRequest().getRealPath("/");
		String saveDocDir = basePath + BIConfig.DOC_DIR;
		String savePdfDir = basePath + BIConfig.PDF_DIR;
		String saveSwfDir = basePath + BIConfig.SWF_DIR;

		String newFileName = "";
		String fileExt = "";
		String filePath = "";
		String pdfPath = "";
		String swfPath = "";

		UploadFile upFile = getFile("file1", saveDocDir, maxSize, "utf-8");
		if (null != upFile) {
			File file = upFile.getFile();
			fileExt = upFile.getOriginalFileName().substring(
					upFile.getOriginalFileName().lastIndexOf("."));
			newFileName = System.currentTimeMillis() + "";
			filePath = saveDocDir + newFileName + fileExt;
			pdfPath = savePdfDir + newFileName + ".pdf";
			swfPath = saveSwfDir + newFileName + ".swf";

			file.renameTo(new File(filePath));
			if (fileExt.equals(".pdf")) {
				FileUtil.forceCopyFile(filePath, pdfPath);
			}
		}

		Doc doc = getModel(Doc.class);
		if (null == doc.getInt("id")) {
			doc.set("creatorId", getLoginOperator().getInt("id"))
					.set("creatorName", getLoginOperator().getStr("name"))
					.set("createTime", DateUtil.curTime()).set("clickNum", 0)
					.set("downloadNum", 0).set("collectNum", 0)
					.set("commentNum", 0);
			if (getLoginOperator().getInt("roleId").intValue() == Integer
					.parseInt(Dict.dao.getValue("SYS_VALUE", "ROLE_ADMIN_ID"))) {
				doc.set("checked", 1);
			}
			doc.set("pid", CodeGenerator.getToken()).save();
			if (null != doc.getInt("checked") && doc.getInt("checked") == 1)
				doc.updateTypeNum(1); // 增加1
		} else {
			doc.update();
		}

		// 保存关键字
		doc.addKeywords(getPara("keywords", ""));

		DocFile docFile = new DocFile();
		if (null != upFile) {
			docFile.set("pid", CodeGenerator.getToken())
					.set("basePath", basePath).set("docId", doc.getInt("id"));
			docFile.set("uploadTime", DateUtil.curTime())
					.set("oriFileName", upFile.getOriginalFileName())
					.set("docType", upFile.getContentType())
					.set("fileSize",
							FileUtil.getFileSize(new File(saveDocDir
									+ newFileName + fileExt)))
					.set("fileName", newFileName)
					.set("docFilePath",
							BIConfig.DOC_DIR + newFileName + fileExt).save();

			String isCreateSwf = getPara("isCreateSwf");
			if ("1".equals(isCreateSwf) && StrKit.notBlank(filePath)) {
				// 如果当前上传的文件是OFFICE文档，则需转换为PDF
				if (filePath.lastIndexOf(".pdf") == -1) {
					new Office2PDF().office2pdf(filePath, pdfPath);
				}
				PDF2SWF.pdf2swf(pdfPath, swfPath);

				docFile.set("pdfFilePath",
						BIConfig.PDF_DIR + newFileName + ".pdf")
						.set("swfFilePath",
								BIConfig.SWF_DIR + newFileName + ".swf")
						.update();
			}

			// PDF2HtmlUtil.pdf2html(pdfPath,
			// newFileName+".pdf",newFileName+".html");
		}

		redirect("/admin/docfile");
	}

	// AJAX生成SWF
	public void ajaxCreate() {
		Map<String, String> ret = new HashMap<String, String>();
		try {
			String id = getPara("id");
			if (null == id)
				return;

			DocFile docFile = DocFile.dao.findById(new Integer(id));
			String pdfPath = docFile.getStr("basePath") + BIConfig.PDF_DIR
					+ docFile.getStr("fileName") + ".pdf";
			String swfPath = docFile.getStr("basePath") + BIConfig.SWF_DIR
					+ docFile.getStr("fileName") + ".swf";

			// 如果当前上传的文件是OFFICE文档，则需转换为PDF
			if (docFile.getStr("docFilePath").lastIndexOf(".pdf") == -1) {
				new Office2PDF().office2pdf(docFile.getStr("docFilePath"),
						pdfPath);
			}
			try {
				PDF2SWF.pdf2swf(pdfPath, swfPath);
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			docFile.set("pdfFilePath",
					BIConfig.PDF_DIR + docFile.getStr("fileName") + ".pdf")
					.set("swfFilePath",
							BIConfig.SWF_DIR + docFile.getStr("fileName")
									+ ".swf").update();

			ret.put("result", "success");
		} catch (Exception ex) {
			ret.put("result", "error");
		}
		renderJson(ret);
	}

	public void delete() {
		Doc doc = Doc.dao.findById(getParaToInt());
		if (null == doc)
			return;
		DocFile docfile = DocFile.dao.findByDocId(doc.getInt("id"));
		try {
			new File(docfile.getStr("basePath") + docfile.getStr("docFilePath"))
					.delete();
		} catch (Exception ex) {
			;
		}
		try {
			new File(docfile.getStr("basePath") + docfile.getStr("pdfFilePath"))
					.delete();
		} catch (Exception ex) {
			;
		}
		try {
			new File(docfile.getStr("basePath") + docfile.getStr("swfFilePath"))
					.delete();
		} catch (Exception e) {
			;
		}
		if (null != docfile)
			docfile.delete();
		doc.clearKeywords();
		if (null != doc.getBoolean("checked") && doc.getBoolean("checked"))
			doc.updateTypeNum(-1); // 分类中文档 数量减一
		doc.delete();
		redirect("/admin/docfile/index-" + getPara("pageNo", "1"));
	}

	public void edit() {
		Doc doc = Doc.dao.findById(getParaToInt());
		setAttr("doc", doc);
		setAttr("keywords", doc.getKeywords());
		_form();
	}

	private void _form() {
		setAttr("examTypeList", ExamType.dao.getTopList());
		int adminId = Integer.parseInt(Dict.dao.getValue("SYS_VALUE",
				"ROLE_ADMIN_ID"));
		setAttr("adminId", adminId);
		setAttr("docTypeList",
				DocType.dao.getParentList(getLoginOperator().getInt("id")));
		render("/admin/docfile/docfile.html");
	}

	/**
	 * 获取队列中正在处理文档
	 */
	public void getQueueList() {
		List<DocFile> list = ExchangeQueue.getQueueList();
		renderJson(list);
	}

	/**
	 * 批量转换
	 */
	public void batchCreateSwf() {
		Map<String, String> ret = new HashMap<String, String>();
		String ids = getPara("docIds", "");
		String[] idArray = ids.split(",");
		int successNum = 0;
		for (String id : idArray) {
			Doc doc = Doc.dao.findById(id);
			if (null == doc)
				continue;
			DocFile file = doc.getDocFile();
			boolean isSuccess = ExchangeQueue.addQueue(file);
			if (isSuccess)
				successNum++;
		}

		ret.put("msg", "向队列中添加任务：成功" + successNum + "个，失败"
				+ (idArray.length - successNum) + "个。稍后刷新本页面即可！");
		renderJson(ret);
	}

	public void doBatchDel() {
		Map<String, Object> ret = new HashMap<String, Object>();
		String[] idArray = getParaValues("docIds");
		int successNum = 0;
		for (String id : idArray) {
			Doc doc = Doc.dao.findById(id);
			if (null == doc)
				continue;
			DocFile docfile = doc.getDocFile();
			try {
				new File(docfile.getStr("basePath")
						+ docfile.getStr("docFilePath")).delete();
			} catch (Exception ex) {
				;
			}
			try {
				new File(docfile.getStr("basePath")
						+ docfile.getStr("pdfFilePath")).delete();
			} catch (Exception ex) {
				;
			}
			try {
				new File(docfile.getStr("basePath")
						+ docfile.getStr("swfFilePath")).delete();
			} catch (Exception e) {
				;
			}
			if (null != docfile)
				docfile.delete();
			doc.delete();
			if (null != doc.getBoolean("checked")
					&& doc.getBoolean("checked") == true)
				doc.updateTypeNum(-1); // 分类中文档 数量减一
		}
		ret.put("result", true);
		redirect("/admin/docfile/index-" + getPara("pageNo", "1"));
	}

	/**
	 * 批量审核
	 */
	public void doCheck() {
		String[] docIds = getParaValues("docIds");
		Integer chk = getParaToInt("chk");
		if (null != docIds) {
			for (String docId : docIds) {
				Doc doc = Doc.dao.findById(docId);
				if (null != doc) {
					doc.set("checked", chk == 1 ? true : false).update();
					if (chk == 1) {
						// 审核通过，则更新当前类别文档数量
						doc.updateTypeNum(1);
					}
				}
			}
		}
		redirect("/admin/docfile/index-" + getParaToInt("pageNo", 1));
	}
}
