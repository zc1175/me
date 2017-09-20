package com.ollearning.common.util;

import com.jfinal.kit.PathKit;
import com.ollearning.global.BIConfig;

public class PDF2HtmlUtil {

	private static final String CMD_WINDOWS = "c:/pdf2htmlex/pdf2htmlEX.exe";
	private static final String CMD_LINUX = "/usr/bin/pdf2htmlex";

	public static boolean pdf2html(String pdfFilePath, String pdfName,
			String htmlName) {
		Runtime rt = Runtime.getRuntime();
		try {
			String command = isWindows() ? CMD_WINDOWS : CMD_LINUX;
			command += " --dest-dir ";
			command += PathKit.getWebRootPath() + BIConfig.HTML_DIR;
			command += " ";
			command += pdfFilePath;
			command += " ";
			command += htmlName;
			Process p = rt.exec(command);
			StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(),
					"ERROR");
			// kick off stderr
			errorGobbler.start();
			StreamGobbler outGobbler = new StreamGobbler(p.getInputStream(),
					"STDOUT");
			// kick off stdout
			outGobbler.start();
			int w = p.waitFor();
			System.out.println(w);
			int v = p.exitValue();
			System.out.println(v);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private static boolean isWindows() {
		String osName = System.getProperty("os.name");
		return osName.indexOf("Windows") != -1;
	}

	public static void main(String[] args) {
		pdf2html("/usr/local/pdf2htmlex /Users/xingry/Desktop/1.pdf 1.html",
				"1.pdf", "1.html");
	}

}
