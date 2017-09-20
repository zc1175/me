package com.ollearning.common.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

public class CodeGenerator {

	public static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

	public static String getOrderCode() {
		String code = "";
		for (int i = 0; i < 2; i++) {
			code += getUniqueString();
		}
		return code;
	}

	public static String getCouponCode() {
		return getOrderCode();
	}

	public static String getToken() {
		return MD5.GetMD5Code(getUniqueString() + UUID.randomUUID().toString()
				+ getUniqueString());
	}

	public static String getLoginToken() {
		return MD5.GetMD5Code(new Date().getTime() + getUniqueString()
				+ getUniqueString());
	}

	public static String getPayCode() {
		SimpleDateFormat format = new SimpleDateFormat("MMddHHmmss");
		Date date = new Date();
		String key = format.format(date);

		java.util.Random r = new java.util.Random();
		key += r.nextInt();
		key = key.substring(0, 15);
		return key;
	}

	public static String getUniqueString() {
		char[] array = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
		Random rand = new Random();
		for (int i = 10; i > 1; i--) {
			int index = rand.nextInt(i);
			char tmp = array[index];
			array[index] = array[i - 1];
			array[i - 1] = tmp;
		}
		String result = "";
		for (int i = 0; i < 6; i++)
			result += array[i];
		return String.valueOf(result);
	}

}
