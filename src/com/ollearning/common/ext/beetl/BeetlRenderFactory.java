package com.ollearning.common.ext.beetl;

import java.io.IOException;

import org.beetl.core.Configuration;
import org.beetl.core.GroupTemplate;
import org.beetl.core.resource.WebAppResourceLoader;

import com.jfinal.kit.PropKit;
import com.jfinal.log.Logger;
import com.jfinal.render.IMainRenderFactory;
import com.jfinal.render.Render;

public class BeetlRenderFactory implements IMainRenderFactory {
	private static final Logger logger = Logger
			.getLogger(BeetlRenderFactory.class);

	public static String viewExtension = ".html";
	public static GroupTemplate groupTemplate = null;

	static {
		try {

			Configuration cfg = Configuration.defaultConfiguration();
			WebAppResourceLoader resourceLoader = new WebAppResourceLoader();
			String root = PropKit.use("config.properties").get("templatePath");
			logger.info("###Beetl template path : " + root);
			resourceLoader.setRoot(root);
			groupTemplate = new GroupTemplate(resourceLoader, cfg);

			groupTemplate.registerFunction("isSame", new IsSameFunction());
			groupTemplate
					.registerFunction("printTime", new PrintTimeFunction());
			groupTemplate.registerTag("includeJSP",
					org.beetl.ext.jsp.IncludeJSPTag.class);

		} catch (IOException e) {
			throw new RuntimeException("加载GroupTemplate失败", e);
		}
	}

	public Render getRender(String view) {
		return new BeetlRender(groupTemplate, view);
	}

	public String getViewExtension() {
		return viewExtension;
	}

	// public static String viewExtension = ".html";
	// public static GroupTemplate gt = null;
	//
	// public BeetlRenderFactory(boolean isLocal) {
	// File file = new File(PathKit.getWebRootPath() + Const.BEETL_ROOT_DIR);
	// Configuration cfg = Configuration.defaultConfiguration();
	// if (isLocal){
	// cfg.enableChecker(2);
	// } else {
	// cfg.enableChecker(0);
	// }
	// cfg.setCharset("UTF-8");
	// cfg.registerFunction("isSame", new IsSameFunction());
	// cfg.registerFunction("printTime", new PrintTimeFunction());
	// cfg.setStatementStart("@");
	// cfg.setStatementEnd(null);
	//
	// WebAppResourceLoader resourceLoader = new WebAppResourceLoader();
	// gt = new GroupTemplate(resourceLoader, cfg);
	// }
	//
	// public Render getRender(String view) {
	// return new BeetlRender(gt, view);
	// }
	//
	// public String getViewExtension() {
	// return viewExtension;
	// }

}
