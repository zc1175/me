package com.ollearning.common.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class PDF2SWF {

	private static final String CMD_WINDOWS = "c:/pdf2swf.exe";
	private static final String CMD_LINUX = "/usr/bin/pdf2swf";

	public static void pdf2swf(String srcFilePath, String destFilePath)
			throws RuntimeException {

		String cmd = isWindows() ? CMD_WINDOWS : CMD_LINUX;

		File srcFile = new File(srcFilePath);
		File destFile = new File(destFilePath);

		if (!srcFile.exists()) {
			throw new RuntimeException("pdf2swf srcFile is not exits.");
		}

		if (!destFile.exists()) {
			try {
				destFile.createNewFile();
			} catch (IOException e) {
				throw new RuntimeException("pdf2swf create destFile error:"
						+ e.getMessage());
			}
		}

		if (destFile.exists()) {
			String cmdPath = cmd + " " + srcFilePath + " -o " + destFilePath
					+ " -f -T 9";
			try {
				Process pro = Runtime.getRuntime().exec(cmdPath);
				BufferedReader bufferedReader = new BufferedReader(
						new InputStreamReader(pro.getInputStream()));
				while (bufferedReader.readLine() != null)
					;
				try {
					pro.waitFor();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				pro.exitValue();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private static boolean isWindows() {
		String osName = System.getProperty("os.name");
		return osName.indexOf("Windows") != -1;
	}

}
