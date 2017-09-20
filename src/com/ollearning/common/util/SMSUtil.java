package com.ollearning.common.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * ���Žӿڹ�����
 * 
 * @author XINGRY
 * 
 */
public final class SMSUtil {

	private static String URL = "http://58.251.49.114/sendSMS.action";
	private static String ENTERPRICE_ID = "15635";
	private static String USERNAME = "admin";
	private static String PWD = "1602fsx";

	private static Map getRequestParam() {
		Map<String, String> ret = new HashMap<String, String>();
		ret.put("enterpriseID", ENTERPRICE_ID);
		ret.put("loginName", USERNAME);
		ret.put("password", MD5.GetMD5Code(PWD).toLowerCase());
		return ret;
	}

	/**
	 * ���Ͷ���
	 * 
	 * @param phone
	 * @param content
	 *            ����
	 * @return boolean ���ͳɹ���ʧ��
	 */
	public static boolean sendSMS(String phone, String content) {
		Map<String, String> param = getRequestParam();
		param.put("content", content);
		param.put("mobiles", phone);
		String result = sendPostHttp(URL, param);
		System.out.println("SMS Send Response:" + result);
		if (result.indexOf("<Result>0</Result>") != -1) {
			return true;
		}
		return false;
	}

	public static String sendPostHttp(String url, Map<String, String> params) {
		URL u = null;
		HttpURLConnection con = null;
		// �����������
		StringBuffer sb = new StringBuffer();
		if (params != null) {
			for (Entry<String, String> e : params.entrySet()) {
				sb.append(e.getKey());
				sb.append("=");
				sb.append(e.getValue());
				sb.append("&");
			}
			sb.substring(0, sb.length() - 1);
		}
		System.out.println("send_url:" + url);
		System.out.println("send_data:" + sb.toString());
		// ���Է�������
		try {
			u = new URL(url);
			con = (HttpURLConnection) u.openConnection();
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			con.setDoInput(true);
			con.setUseCaches(false);
			con.setConnectTimeout(3000);
			con.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			OutputStreamWriter osw = new OutputStreamWriter(
					con.getOutputStream(), "UTF-8");
			osw.write(sb.toString());
			osw.flush();
			osw.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (con != null) {
				con.disconnect();
			}
		}

		// ��ȡ��������
		StringBuffer buffer = new StringBuffer();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					con.getInputStream(), "UTF-8"));
			String temp;
			while ((temp = br.readLine()) != null) {
				buffer.append(temp);
				buffer.append("\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return buffer.toString();
	}

}
