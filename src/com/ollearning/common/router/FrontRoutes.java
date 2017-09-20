package com.ollearning.common.router;

import com.jfinal.config.Routes;
import com.ollearning.front.controller.FrontDocFileController;
import com.ollearning.front.controller.FrontIndexController;
import com.ollearning.front.controller.FrontNewsController;
import com.ollearning.front.controller.FrontQuestionController;
import com.ollearning.front.controller.FrontTestingController;
import com.ollearning.front.controller.FrontUserController;
import com.ollearning.front.controller.FrontVideoCollectionController;
import com.ollearning.front.controller.FrontVideoFileController;

public class FrontRoutes extends Routes {

	@Override
	public void config() {

		add("/", FrontIndexController.class);
		add("/docs", FrontDocFileController.class);
		add("/video", FrontVideoFileController.class);
		add("/testing", FrontTestingController.class);
		add("/user", FrontUserController.class);
		add("/question", FrontQuestionController.class);
		add("/news", FrontNewsController.class);
		add("/videoCollection", FrontVideoCollectionController.class);

	}

}
