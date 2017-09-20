package com.ollearning.common.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import com.jfinal.core.Controller;
import com.jfinal.kit.PropKit;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.upload.UploadFile;
import com.ollearning.sys.model.Operator;
import com.ollearning.user.model.User;

public class BaseController<T> extends Controller {

	public BaseController() {
	}

	protected String getBasePath() {
		return PropKit.use("config.properties").get("httpPath");
	}

	protected String getCtxPath() {
		return getRequest().getContextPath();
	}

	protected boolean isNullOrEmpty(String str) {
		return null == str || str.trim().length() == 0;
	}

	public User getLoginCust() {
		User cust = getSessionAttr("loginUser");
		// if(null == cust) throw new RuntimeException("登录超时，请重新登录!");
		return cust;
	}

	public Operator getLoginOperator() {
		if (null == getSessionAttr("loginOperator")) {
			renderHtml("<script>alert('登录超时！请重新登录');parent.parent.location.href='"
					+ getBasePath() + "/admin/login';</script>");
			return null;
		}
		return getSessionAttr("loginOperator");
	}

	@SuppressWarnings("deprecation")
	private String uploadFile(String formFileName, String saveDir, int maxSize)
			throws RuntimeException {
		try {
			System.out.println("uploadFile===>" + formFileName);
			String savePath = getRequest().getRealPath("/") + "/" + saveDir;
			UploadFile upFile = getFile(formFileName, savePath, maxSize,
					"utf-8");
			if (null == upFile)
				return null;
			File file = upFile.getFile();
			String fileExt = upFile.getOriginalFileName().substring(
					upFile.getOriginalFileName().lastIndexOf("."));
			String newFileName = System.currentTimeMillis() + fileExt;
			file.renameTo(new File(savePath + newFileName));
			return newFileName;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unused")
	public String getReturnUrl() {
		String returnUrl = getPara("returnUrl");
		if (null == returnUrl) {
			returnUrl = getAttr("returnUrl");
		} else if (null == returnUrl) {
			returnUrl = getSessionAttr("returnUrl");
		}
		return returnUrl;
	}

	public String getRealIpAddr() {
		HttpServletRequest req = getRequest();
		String ip = req.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip))
			ip = req.getHeader("Proxy-Client-IP");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip))
			ip = req.getHeader("WL-Proxy-Client-IP");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip))
			ip = req.getRemoteAddr();
		return ip;
	}

	/**
	 * 获取以modelName开头的参数，并自动赋值给record对象
	 * 
	 * @param modelName
	 * @return
	 */
	public Record getRecord(String modelName) {
		String modelNameAndDot = modelName + ".";
		Record model = new Record();
		boolean exist = false;
		Map<String, String[]> parasMap = getRequest().getParameterMap();
		for (Entry<String, String[]> e : parasMap.entrySet()) {
			String paraKey = e.getKey();
			if (paraKey.startsWith(modelNameAndDot)) {
				String paraName = paraKey.substring(modelNameAndDot.length());
				String[] paraValue = e.getValue();
				Object value = paraValue[0] != null ? (paraValue.length == 1 ? paraValue[0]
						: array2str(paraValue, ","))
						: null;
				model.set(paraName, value);
				exist = true;
			}
		}
		if (exist) {
			return model;
		} else {
			return null;
		}
	}

	private String array2str(String[] arr, String split) {
		String ret = "";
		for (int i = 0; i < arr.length; i++) {
			ret += arr[i];
			if (i < arr.length - 1) {
				ret += split;
			}
		}
		return ret;
	}

	/**
	 * 获取前端传来的数组对象并响应成Record列表
	 * 
	 * @param modelName
	 * @return
	 */
	public List<Record> getRecords(String modelName) {
		List<String> nos = getModelsNoList(modelName);
		List<Record> list = new ArrayList<Record>();
		for (String no : nos) {
			Record r = getRecord(modelName + "[" + no + "]");
			if (r != null) {
				list.add(r);
			}
		}
		return list;
	}

	/**
	 * 获取前端传来的数组对象并响应成Model列表
	 * 
	 * @param modelClass
	 * @param modelName
	 * @return
	 */
	public List<T> getModels(Class<T> modelClass, String modelName) {
		List<String> nos = getModelsNoList(modelName);
		List<T> list = new ArrayList<T>();
		for (String no : nos) {
			T m = getModel(modelClass, modelName + "[" + no + "]");
			if (m != null) {
				list.add(m);
			}
		}
		return list;
	}

	/**
	 * 提取model对象数组的标号
	 * 
	 * @param modelName
	 * @return
	 */
	private List<String> getModelsNoList(String modelName) {
		// 提取标号
		List<String> list = new ArrayList<String>();
		String modelNameAndLeft = modelName + "[";
		Map<String, String[]> parasMap = getRequest().getParameterMap();
		for (Entry<String, String[]> e : parasMap.entrySet()) {
			String paraKey = e.getKey();
			if (paraKey.startsWith(modelNameAndLeft)) {
				String no = paraKey.substring(paraKey.indexOf("[") + 1,
						paraKey.indexOf("]"));
				if (!list.contains(no)) {
					list.add(no);
				}
			}
		}
		return list;
	}

	public void redirect(String path) {
		if (StrKit.notBlank(path)) {
			if (path.startsWith(getBasePath())) {
				super.redirect(path);
			} else {
				super.redirect(getBasePath() + path);
			}
		}
	}

}
