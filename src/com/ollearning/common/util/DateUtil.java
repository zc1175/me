package com.ollearning.common.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public final class DateUtil {

	private static SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
	private static SimpleDateFormat sdfTime = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	public static String curTime() {
		return sdfTime.format(new Date());
	}

	public static String curDate() {
		return sdfDate.format(new Date());
	}

	public static String now(String format) {
		return new SimpleDateFormat(format).format(new Date());
	}

	public static Date dateAdd(Date fromDate, long millseconds) {
		long timestamp = fromDate.getTime() + millseconds;
		Date date = new Date(timestamp);
		return date;
	}

	public static String format(Date date, String format) {
		return new SimpleDateFormat(format).format(date);
	}

}
