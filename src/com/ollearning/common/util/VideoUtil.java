package com.ollearning.common.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class VideoUtil {

	private static final String CMD_WINDOWS = "c:/ffmpeg/bin/ffmpeg.exe";
	private static final String CMD_LINUX = "/usr/bin/ffmpeg";

	// private static final String CMD_WINDOWS_BAT = "c:/ffmpeg/bin/ffmpeg.bat";

	/**
	 * 对指定的视频 进行截图
	 * 
	 * @param secondsFrom
	 *            截取的时间（秒数）
	 * @param videoFilePath
	 *            源文件全路径
	 * @param outputFilePath
	 *            目标文件全路径
	 * @return
	 * @throws RuntimeException
	 */
	public static boolean createCapture(String videoFilePath,
			String outputFilePath) {
		String cmd = isWindows() ? CMD_WINDOWS : CMD_LINUX;

		File srcFile = new File(videoFilePath);

		if (!srcFile.exists()) {
			throw new RuntimeException("视频文件不存在！");
		}

		if (srcFile.exists()) {
			// String cmdPath = cmd + " -i " + videoFilePath
			// +" -y -f image2 -ss " + secondsFrom + " -vframes 1 " +
			// outputFilePath;
			String cmdPath = "";
			if (isWindows())
				cmdPath += "cmd /c start ";
			cmdPath += cmd + " -i " + videoFilePath
					+ " -y -f image2 -ss 3 -vframes 1 " + outputFilePath;
			try {
				Runtime.getRuntime().exec(cmdPath);
				System.out.println(cmdPath);
				return true;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}

		return false;
	}

	/**
	 * 转换为FLV
	 * 
	 * @param srcFilePath
	 * @param outputFilePath
	 * @return
	 */
	public static boolean convert2FLV(String srcFilePath, String outputFilePath) {
		List<String> commend = new ArrayList<String>();
		String cmd = isWindows() ? CMD_WINDOWS : CMD_LINUX;
		commend.add(cmd);
		commend.add("-i");
		commend.add(srcFilePath);
		commend.add("-ab");
		commend.add("56");
		commend.add("-ar");
		commend.add("22050");
		commend.add("-qscale");
		commend.add("8");
		commend.add("-r");
		commend.add("15");
		commend.add("-s");
		commend.add("600x500");
		commend.add(outputFilePath);

		try {
			ProcessBuilder builder = new ProcessBuilder(commend);
			System.out.println(commend);
			builder.command(commend);
			builder.start();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 获取文件类型
	 * 
	 * @param path
	 * @return
	 */
	private static int getContentType(String path) {
		String type = path.substring(path.lastIndexOf(".") + 1, path.length())
				.toLowerCase();
		// ffmpeg能解析的格式：（asx，asf，mpg，wmv，3gp，mp4，mov，avi，flv等）
		if (type.equals("avi")) {
			return 0;
		} else if (type.equals("mpg")) {
			return 0;
		} else if (type.equals("wmv")) {
			return 0;
		} else if (type.equals("3gp")) {
			return 0;
		} else if (type.equals("mov")) {
			return 0;
		} else if (type.equals("mp4")) {
			return 0;
		} else if (type.equals("asf")) {
			return 0;
		} else if (type.equals("asx")) {
			return 0;
		} else if (type.equals("flv")) {
			return 0;
		}
		// 对ffmpeg无法解析的文件格式(wmv9，rm，rmvb等),
		// 可以先用别的工具（mencoder）转换为avi(ffmpeg能解析的)格式.
		else if (type.equals("wmv9")) {
			return 1;
		} else if (type.equals("rm")) {
			return 1;
		} else if (type.equals("rmvb")) {
			return 1;
		}
		return 9;
	}

	private static boolean isWindows() {
		String osName = System.getProperty("os.name");
		return osName.indexOf("Windows") != -1;
	}

}
