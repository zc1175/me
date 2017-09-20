package com.ollearning.front.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Page;
import com.ollearning.common.controller.BaseController;
import com.ollearning.common.jfinal.Const;
import com.ollearning.docfile.model.Doc;
import com.ollearning.jna.JnaUtil.hello;
import com.ollearning.news.model.News;
import com.ollearning.news.model.NewsCategory;
import com.ollearning.sys.model.Dict;
import com.ollearning.testing.model.Exam;
import com.ollearning.testing.model.ExamType;
import com.ollearning.testing.model.UserAnswer;
import com.ollearning.videofile.model.Video;
import com.sun.jna.Native;

public class FrontIndexController extends BaseController {

	public void index() {
		setAttr("docList", Doc.dao.getDocListForIndex());
		setAttr("examList", Exam.dao.getExamListForIndex());
		setAttr("newsList", News.dao.getPageForFront(new String[] {}, 1, 5)
				.getList());
		setAttr("videoList", Video.dao.getVideoListForIndex());
		render("/front/index.html");
	}

	public void dl() {

		String newsIds = Dict.dao.getValue("SYS_VALUE", "FRONT_DL_NEWS_ID");
		List<NewsCategory> categoryList = new ArrayList();

		if (StrKit.notBlank(newsIds)) {
			String[] newsIdArray = newsIds.split(",");
			for (String newsId : newsIdArray) {
				categoryList.add(NewsCategory.dao.findById(newsId));
			}
			String categoryId = getPara("typeId");
			setAttr("typeId", categoryId);
			if (null != categoryId) {
				newsIdArray = new String[] { categoryId };
				setAttr("categoryId", categoryId);
				NewsCategory category = NewsCategory.dao.loadModel(new Integer(
						categoryId));
				setAttr("category", category);
				if (null != category && -1 != category.getInt("parentId")) {
					NewsCategory categoryParent = NewsCategory.dao
							.loadModel(category.getInt("parentId"));
					setAttr("parentId", categoryParent.getInt("id"));
					setAttr("categoryParent", categoryParent);
					if (-1 != categoryParent.getInt("parentId")) {
						setAttr("categoryPParent",
								NewsCategory.dao.loadModel(categoryParent
										.getInt("parentId")));
						setAttr("ppId", categoryParent.getInt("parentId"));
					}
				}
			}
			setAttr("newsPage", News.dao.getPageForFront(newsIdArray,
					getParaToInt("pageNo", 1), Const.PAGE_SIZE_FOR_FRONT));
			setAttr("categoryList", categoryList);
		}
		render("/front/dl/index.html");
	}

	/**
	 * 在线练习
	 */
	public void practice() {
		Integer examTypeId = getParaToInt("examTypeId", -1);
		String time = getPara("time", "-1");
		String type = getPara("type", "-1");
		Integer pageNo = getParaToInt("pageNo", 1);
		setAttr("examTypeId", examTypeId);
		setAttr("time", time);

		type = "practice"; // 在线练习
		setAttr("type", type);

		Exam.QueryBean qb = Exam.dao.newQueryBean();
		qb.examTypeId = examTypeId;
		qb.time = time;
		qb.type = type;

		// 查询已经发布且生成的，在时间范围内试卷列表
		Page<Exam> examPage = Exam.dao.getExamListForFront(pageNo, qb);
		UserAnswer.dao.checkUserAnswered(examPage.getList(), getLoginCust());
		setAttr("examPage", examPage);
		setAttr("examTypeList", ExamType.dao.getTopList());
		setAttr("actionUrl", "/front/testing/index-");
		render("/front/practice/index.html");
	}

	// 仿真
	public void simulation() {
		Map<String, String> maps = new HashMap<String, String>();

		Prop prop = PropKit.use("simulation.txt");
		int totalNum = prop.getInt("totalNum");
		for (int i = 1; i <= totalNum; i++) {
			String item = prop.get("s" + i);
			String[] arr = item.split("\\|");
			String title = arr[0];
			String path = arr[1];
			maps.put(title, path);
		}

		setAttr("maps", maps);
		setAttr("basePath", getBasePath());
		render("/front/simulation.html");

	}

}
