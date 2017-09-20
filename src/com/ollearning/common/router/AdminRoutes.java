package com.ollearning.common.router;

import com.jfinal.config.Routes;
import com.ollearning.docfile.controller.DocController;
import com.ollearning.docfile.controller.DocTypeController;
import com.ollearning.monitor.MonitorController;
import com.ollearning.news.controller.NewsCategoryController;
import com.ollearning.news.controller.NewsController;
import com.ollearning.stat.StatController;
import com.ollearning.sys.controller.AdminController;
import com.ollearning.sys.controller.BroadcastController;
import com.ollearning.sys.controller.DictController;
import com.ollearning.sys.controller.LogController;
import com.ollearning.sys.controller.NavMenuController;
import com.ollearning.sys.controller.OperatorController;
import com.ollearning.sys.controller.RightController;
import com.ollearning.sys.controller.RoleController;
import com.ollearning.sys.controller.SessionController;
import com.ollearning.sys.controller.SiteInfoController;
import com.ollearning.testing.controller.ExamComboController;
import com.ollearning.testing.controller.ExamController;
import com.ollearning.testing.controller.ExamTypeController;
import com.ollearning.testing.controller.QuestionController;
import com.ollearning.testing.controller.QuestionTypeController;
import com.ollearning.testing.controller.SubjectController;
import com.ollearning.user.controller.UserController;
import com.ollearning.user.controller.UserLevelController;
import com.ollearning.user.controller.UserLogController;
import com.ollearning.videofile.controller.VideoCollectionController;
import com.ollearning.videofile.controller.VideoController;
import com.ollearning.videofile.controller.VideoTypeController;

public class AdminRoutes extends Routes {

	@Override
	public void config() {

		add("/admin", AdminController.class);
		add("/admin/role", RoleController.class);
		add("/admin/right", RightController.class);
		add("/admin/operator", OperatorController.class);
		add("/admin/log", LogController.class);
		add("/admin/newsCategory", NewsCategoryController.class);
		add("/admin/news", NewsController.class);
		add("/admin/navMenu", NavMenuController.class);
		add("/admin/siteInfo", SiteInfoController.class);
		add("/admin/docfile", DocController.class);
		add("/admin/videofile", VideoController.class);
		add("/admin/videoType", VideoTypeController.class);

		add("/admin/examType", ExamTypeController.class);
		add("/admin/subject", SubjectController.class);
		add("/admin/questiontype", QuestionTypeController.class);
		add("/admin/question", QuestionController.class);
		add("/admin/exam", ExamController.class);
		add("/admin/examCombo", ExamComboController.class);
		add("/admin/userLevel", UserLevelController.class);
		add("/admin/user", UserController.class);

		add("/admin/stat", StatController.class);
		add("/admin/monitor", MonitorController.class);

		add("/admin/docType", DocTypeController.class);
		add("/admin/dict", DictController.class);
		add("/admin/broadcast", BroadcastController.class);
		add("/admin/session", SessionController.class);

		add("/admin/videoCollection", VideoCollectionController.class);
		add("/admin/userLog", UserLogController.class);
	}

}
