package com.ollearning.front.controller;

import java.io.File;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.jfinal.kit.PathKit;
import com.jfinal.plugin.activerecord.Page;
import com.ollearning.common.controller.BaseController;
import com.ollearning.docfile.model.Doc;
import com.ollearning.docfile.model.DocType;
import com.ollearning.sys.model.Operator;
import com.ollearning.user.model.User;

public class FrontDocFileController extends BaseController {

	public void index() {
		Integer typeId = getParaToInt("typeId", -1);
		Integer time = getParaToInt("time", -1);
		String orderBy = getPara("orderBy", "-1");
		Integer pageNo = getParaToInt("pageNo", 1);

		setAttr("typeId", typeId);
		setAttr("time", time);
		setAttr("orderBy", orderBy);

		Doc.QueryBean qb = Doc.dao.newQueryBean();
		qb.typeId = typeId;
		qb.time = String.valueOf(time);
		qb.orderBy = orderBy;

		Page<Doc> docPage = Doc.dao.getPageByBean(pageNo, qb);
		setAttr("docPage", docPage);

		List<DocType> typeList = DocType.dao.getParentList();
		setAttr("typeList", typeList);

		if (-1 != typeId) {
			DocType docType = DocType.dao.findById(typeId);
			setAttr("docType", docType);
			if (null != docType && docType.getInt("parentId") != -1) {
				setAttr("parent", docType.getParent());
				setAttr("parentId", docType.getInt("parentId"));
				if (null != docType.getParent()
						&& -1 != docType.getParent().getInt("parentId")) {
					setAttr("pparent",
							DocType.dao.findById(docType.getParent().getInt(
									"parentId")));
					setAttr("ppId",
							DocType.dao.findById(docType.getInt("parentId"))
									.getInt("parentId"));
				}
			}
		}

		render("/front/document/index.html");
	}

	public void paper() {
		String pid = getPara();
		Doc doc = Doc.dao.findByPid(pid);
		if (null == doc) {
			renderHtml("<p>无效访问</p>");
			return;
		}
		int clickNum = (null == doc.getInt("clickNum")) ? 0 : doc
				.getInt("clickNum");
		doc.set("clickNum", clickNum + 1).update();
		setAttr("doc", doc);
		setAttr("relatedDocs", Doc.dao.getRelatedDocs(doc.getInt("id")));

		setAttr("basePath", getBasePath());
		render("/front/document/paper.html");
	}

	public void dl() {
		Doc doc = Doc.dao.findByPid(getPara());
		if (null == doc) {
			renderHtml("<script>alert('无效访问');");
			return;
		}
		User loginUser = getLoginCust();
		Operator op = getLoginOperator();

		if (null == op) {
			if (null != loginUser
					&& loginUser.getInt("score") < doc.getInt("score")) {
				renderHtml("<script>alert('积分不足！')</script>");
				return;
			}
		}
		doc.set("downloadNum", doc.getInt("downloadNum") + 1).update();
		String filePath = doc.getDocFile().getStr("pdfFilePath");
		renderFile(new File(PathKit.getWebRootPath() + filePath));
	}

}
