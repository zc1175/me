package com.ollearning.common.util;

import java.math.BigDecimal;
import java.text.NumberFormat;

public class NumberUtil {

	/**
	 * �������룬����С��
	 * 
	 * @param v
	 * @param scale
	 * @return
	 */
	public static String round(double v, int scale) {
		if (scale < 0) {
			throw new IllegalArgumentException(
					"The scale must be a positive integer or zero");
		}
		BigDecimal b = new BigDecimal(Double.toString(v));
		BigDecimal one = new BigDecimal("1");
		return String.valueOf(b.divide(one, scale, BigDecimal.ROUND_HALF_UP)
				.doubleValue());
	}

	/**
	 * ֱ����ȥ������С��
	 * 
	 * @param value
	 * @param max
	 * @return
	 */
	public static double ceil(double value, int max) {
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(max);
		nf.setMinimumFractionDigits(max);
		nf.setGroupingUsed(false);

		return Double.parseDouble(nf.format(value));
	}
}
