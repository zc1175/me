package com.ollearning.common.jfinal;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.servlet.http.HttpSession;

import com.ollearning.testing.model.UserAnswerDetail;

public class Const {

	public static boolean SYS_VALID = true;

	// User Answer queue
	public static final ConcurrentLinkedQueue<UserAnswerDetail> USER_ANSWER_QUEUE = new ConcurrentLinkedQueue<UserAnswerDetail>();

	// online users
	public static final LinkedList<HttpSession> ONLINE_USERS = new LinkedList<HttpSession>();
	public static final HashMap<String, String> USER_ACT = new HashMap<String, String>();

	public static final String SYS_ERR_MSG = "系统处理失败，请稍后重试！";

	// keys
	public static String OPERATOR_SESSION_KEY = "loginOperator";
	public static String USER_SESSION_KEY = "loginUser";
	public static String COMPANY_SESSION_KEY = "loginCompany";

	// page size
	public static int PRODUCT_PAGE_SIZE = 20; // 首页帖子分页大小
	public static int PAGE_SIZE_FOR_ADMIN = 10; // 管理员后台 的分页大小
	public static int PAGE_SIZE_FOR_FRONT = 9; // 前台 的分页大小

	public static class ExamStatus {
		public static String START = "已开考";
		public static String SUBMIT = "已交卷";
	}

	public static String TIMESTAMP = System.currentTimeMillis() + "";

	public static class Exam {
		// 试卷生成方式
		public static class Create {
			public static final String NONE = "none"; // 实时随机抽题
			public static final String SAME = "createsame"; // 预先生成，每人题目相同，顺序相同
			public static final String DIFF = "creatediff"; // 预先生成，每人题目相同，顺序不同
		}

	}

}
