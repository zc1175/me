package com.ollearning.common.jfinal;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.wall.WallFilter;
import com.jfinal.config.Constants;
import com.jfinal.config.Handlers;
import com.jfinal.config.Interceptors;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.ext.handler.ContextPathHandler;
import com.jfinal.ext.interceptor.SessionInViewInterceptor;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.CaseInsensitiveContainerFactory;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.plugin.druid.DruidStatViewHandler;
import com.jfinal.plugin.ehcache.EhCachePlugin;
import com.ollearning.common.ext.beetl.BeetlRenderFactory;
import com.ollearning.common.handler.JspSkipHandler;
import com.ollearning.common.router.AdminRoutes;
import com.ollearning.common.router.FrontRoutes;
import com.ollearning.docfile.model.Doc;
import com.ollearning.docfile.model.DocFile;
import com.ollearning.docfile.model.DocKeywords;
import com.ollearning.docfile.model.DocType;
import com.ollearning.interceptors.FrontInterceptor;
import com.ollearning.interceptors.LogInterceptor;
import com.ollearning.news.model.News;
import com.ollearning.news.model.NewsCategory;
import com.ollearning.site.model.NavMenu;
import com.ollearning.site.model.SiteInfo;
import com.ollearning.sys.SysLog;
import com.ollearning.sys.model.Broadcast;
import com.ollearning.sys.model.Dict;
import com.ollearning.sys.model.Log;
import com.ollearning.sys.model.Operator;
import com.ollearning.sys.model.Right;
import com.ollearning.sys.model.Role;
import com.ollearning.sys.model.RoleRight;
import com.ollearning.testing.model.Exam;
import com.ollearning.testing.model.ExamCombo;
import com.ollearning.testing.model.ExamComboDetail;
import com.ollearning.testing.model.ExamQuestion;
import com.ollearning.testing.model.ExamType;
import com.ollearning.testing.model.Question;
import com.ollearning.testing.model.QuestionSub;
import com.ollearning.testing.model.QuestionType;
import com.ollearning.testing.model.Subject;
import com.ollearning.testing.model.UserAnswer;
import com.ollearning.testing.model.UserAnswerDetail;
import com.ollearning.user.model.User;
import com.ollearning.user.model.UserLevel;
import com.ollearning.user.model.UserLog;
import com.ollearning.videofile.model.Video;
import com.ollearning.videofile.model.VideoCollection;
import com.ollearning.videofile.model.VideoFile;
import com.ollearning.videofile.model.VideoType;

public class Config extends JFinalConfig {
	private static Logger logger = Logger.getLogger(SysLog.class);

	public Config() {
	}
	
	//表和实体类进行映射
	private void addMappings(ActiveRecordPlugin arp) {
		arp.addMapping("sys_role", Role.class);
		arp.addMapping("sys_right", Right.class);
		arp.addMapping("sys_role_right", RoleRight.class);
		arp.addMapping("operator", Operator.class);
		arp.addMapping("sys_log", Log.class);
		arp.addMapping("base_dict", Dict.class);
		arp.addMapping("site_info", SiteInfo.class);
		arp.addMapping("nav_menu", NavMenu.class);
		arp.addMapping("news_categories", NewsCategory.class);
		arp.addMapping("news", News.class);
		arp.addMapping("doc_files", DocFile.class);
		arp.addMapping("docs_keywords", DocKeywords.class);

		arp.addMapping("videos", Video.class);
		arp.addMapping("video_type", VideoType.class);
		arp.addMapping("video_files", VideoFile.class);
		arp.addMapping("video_collection", VideoCollection.class);

		// testing

		arp.addMapping("exams", Exam.class);
		arp.addMapping("examquestion", ExamQuestion.class);
		arp.addMapping("examcombo", ExamCombo.class);
		arp.addMapping("examcombodetail", ExamComboDetail.class);

		arp.addMapping("exam_type", ExamType.class);
		arp.addMapping("subjects", Subject.class);
		arp.addMapping("questiontype", QuestionType.class);
		arp.addMapping("questions", Question.class);
		arp.addMapping("questionsubs", QuestionSub.class);

		arp.addMapping("users", User.class);
		arp.addMapping("user_level", UserLevel.class);
		arp.addMapping("useranswer", UserAnswer.class);
		arp.addMapping("useranswerdetail", UserAnswerDetail.class);

		arp.addMapping("doc_type", DocType.class);
		arp.addMapping("docs", Doc.class);
		arp.addMapping("broadcast", Broadcast.class);

		arp.addMapping("user_logs", UserLog.class);

	}

	/**
	 * 配置常量
	 */
	public void configConstant(Constants me) {
		loadPropertyFile("config.properties");

		me.setError404View("/common/404.html");
		me.setError500View("/common/500.html");
		me.setMainRenderFactory(new BeetlRenderFactory());
	}

	/**
	 * 配置路由
	 */
	public void configRoute(Routes me) {
		me.add(new FrontRoutes());
		me.add(new AdminRoutes());
	}

	/**
	 * 配置插件
	 */
	public void configPlugin(Plugins me) {
		String jdbcUrl, user, password;
		jdbcUrl = getProperty("jdbcUrl");
		user = getProperty("user");
		password = getProperty("password");
		DruidPlugin druidPlugin = new DruidPlugin(jdbcUrl, user, password);
		druidPlugin.setFilters("wall,stat,log4j");
		druidPlugin.addFilter(new StatFilter());
		WallFilter wall = new WallFilter();
		wall.setDbType(getProperty("dbType"));
		druidPlugin.addFilter(wall);
		druidPlugin
				.setTestOnBorrow(true)
				.setTestOnReturn(true)
				.setTestWhileIdle(true)
				.setMaxWait(getPropertyToInt("druid.maxWait"))
				.setMaxPoolPreparedStatementPerConnectionSize(
						getPropertyToInt("druid.maxPoolPreparedStatementPerConnectionSize"))
				.setInitialSize(getPropertyToInt("druid.initialSize"))
				.setMaxActive(getPropertyToInt("druid.maxActive"));
		me.add(druidPlugin);

		ActiveRecordPlugin arp = new ActiveRecordPlugin(druidPlugin);
		arp.setShowSql(true);
		// 列名大小写不敏感
		arp.setContainerFactory(new CaseInsensitiveContainerFactory(true));
		addMappings(arp);
		me.add(arp);
		me.add(new EhCachePlugin());
	}

	/**
	 * 配置全局拦截器
	 */
	public void configInterceptor(Interceptors me) {
		me.add(new SessionInViewInterceptor());
		me.add(new LogInterceptor());
		me.add(new FrontInterceptor());
	}

	/**
	 * 配置处理器
	 */
	public void configHandler(Handlers me) {
		me.add(new ContextPathHandler("base"));
		me.add(new DruidStatViewHandler("/druid"));
		me.add(new JspSkipHandler());
	}

	/**
	 * 初始化常量
	 */
	public void afterJFinalStart() {
		// 定义模板中使用全局变量
		org.beetl.core.GroupTemplate gt = BeetlRenderFactory.groupTemplate;
		Map<String, Object> shared = new HashMap<String, Object>();
		// 缓存站点信息
		shared.put("httpPath", getProperty("httpPath"));
		shared.put("basePath", getProperty("httpPath"));
		gt.setSharedVars(shared);

		// 启动后1分钟开始检查 ，然后每2分钟检查一次
		// new Timer().schedule(new SysValidTimerTask(), 1000*60,1000*60*2);

		// 检查考生试卷是否已到时间，并实现自动交卷,启动后5分钟开始检查，每隔2秒执行一次
		// new Timer().schedule(new ExamTimerTask(), 1000*60*5,1000*2);

	}
}
