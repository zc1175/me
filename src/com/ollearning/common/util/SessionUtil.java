package com.ollearning.common.util;

import java.util.Date;
import java.util.LinkedList;

import javax.servlet.http.HttpSession;

import com.ollearning.common.jfinal.Const;
import com.ollearning.user.model.User;

public final class SessionUtil {

	// 添加访问用户列表
	public static void addSession(HttpSession session) {
		boolean isExists = false;
		LinkedList<HttpSession> list = Const.ONLINE_USERS;
		for (HttpSession s : list) {
			if (session.getId().equals(s.getId())) {
				isExists = true;
				break;
			}
		}
		if (!isExists) {
			list.add(session);
		}
	}

	// 注销指定用户
	public static void killUserByName(String loginName) {
		LinkedList<HttpSession> list = Const.ONLINE_USERS;
		for (HttpSession s : list) {
			User user = (User) s.getAttribute(Const.USER_SESSION_KEY);
			if (null != user && user.getStr("loginName").equals(loginName)) {
				s.removeAttribute(Const.USER_SESSION_KEY);
				s.invalidate();
				break;
			}
		}
	}

	public static void killUserBySessionId(String sessionId) {
		removeSessionById(sessionId);
	}

	// 移除
	public static void removeSession(HttpSession session) {
		removeSessionById(session.getId());
	}

	public static void removeSessionById(String sessionId) {
		LinkedList<HttpSession> list = Const.ONLINE_USERS;
		for (int i = 0; i < list.size(); i++) {
			HttpSession s = list.get(i);
			if (sessionId.equals(s.getId())) {
				list.remove(i);
				break;
			}
		}
	}

	public static String getLastAccessTime(HttpSession s) {
		try {
			Date date = new Date(s.getLastAccessedTime());
			return DateUtil.format(date, "MM-dd HH:mm:ss");
		} catch (Exception e) {
			;
		}
		return "";
	}

	public static void setActPath(String sessionId, String path) {
		Const.USER_ACT.put(sessionId, path);
	}

	public static String getActPath(HttpSession session) {
		String act = "";
		try {
			act = Const.USER_ACT.get(session.getId());
		} catch (Exception e) {
			;
		}
		return act;
	}

	public static String getLoginIp(HttpSession session) {
		try {
			return (String) session.getAttribute("loginIp");
		} catch (Exception e) {
		}
		return "";
	}

	public static String getLoginName(HttpSession session) {
		try {
			User user = (User) session.getAttribute(Const.USER_SESSION_KEY);
			if (null != user) {
				return user.getStr("loginName") + "-" + user.getStr("name");
			}
		} catch (Exception e) {
			;
		}
		return "未登录";
	}

}
