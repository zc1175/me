package com.ollearning.common.util;

import java.util.Arrays;

public final class StringUtil {

	public static boolean isEqualsIgnoreCase(String s1, String s2) {
		if (s1 == null || s2 == null)
			return false;
		if (s1.length() != s2.length())
			return false;
		s1 = s1.toLowerCase();
		s2 = s2.toLowerCase();
		String[] arr1 = s1.split("");
		String[] arr2 = s2.split("");
		Arrays.sort(arr1);
		;
		Arrays.sort(arr2);
		for (int i = 0; i < arr1.length; i++) {
			if (arr1[i].equals(arr2[i]) == false) {
				return false;
			}
		}
		return true;
	}

	public static void main(String[] args) {
		System.out.println(isEqualsIgnoreCase("ABC", "BCD"));
	}

}
