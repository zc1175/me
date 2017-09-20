package com.ollearning.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

public class FileUtil {

	public static Integer getFileSize(File file) {
		Integer size = 0;
		try {
			if (file.exists()) {
				FileInputStream fis = null;
				fis = new FileInputStream(file);
				size = fis.available();
			}
		} catch (Exception e) {
			;
		}
		return size;

	}

	public static String formatSize(Integer fileS) {
		DecimalFormat df = new DecimalFormat("#.00");
		String fileSizeString = "";
		if (fileS < 1024) {
			fileSizeString = df.format((double) fileS) + "B";
		} else if (fileS < 1048576) {
			fileSizeString = df.format((double) fileS / 1024) + "K";
		} else if (fileS < 1073741824) {
			fileSizeString = df.format((double) fileS / 1048576) + "M";
		} else {
			fileSizeString = df.format((double) fileS / 1073741824) + "G";
		}
		return fileSizeString;
	}

	public static String invaildChars[] = { "\\", "/", ":", "*", "?", "\"",
			"<", ">", "|" };

	public static ArrayList listFromDir(String FilePath, int list_type) {
		File path = new File(FilePath);
		String[] list1;
		ArrayList list2 = new ArrayList();
		String separator = File.separator;
		if (path.isDirectory()) {
			list1 = path.list();
			for (int i = 0; i < list1.length; i++) {
				File f = new File(FilePath + separator + list1[i]);
				if (list_type == 0) {
					if (f.isDirectory()) {
						list2.add(list1[i]);
					}
				} else if (list_type == 1) {
					if (f.isFile()) {
						list2.add(list1[i]);
					}
				} else
					list2.add(list1[i]);
			}

		}
		return list2;
	}

	public static long getDirSize(String FilePath) {
		long total_size = 0;
		String s_dir = FilePath;
		String separator = File.separator;
		try {
			File path = new File(s_dir);
			if (path.isDirectory()) {
				String[] list;
				list = path.list();
				for (int i = 0; i < list.length; i++) {
					File f = new File(s_dir + separator + list[i]);
					if (f.isFile()) {
						total_size += f.length();
					} else if (f.isDirectory()) {
						total_size += getDirSize(f.getAbsolutePath());
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return total_size;

	}

	public static long getDirSizeRemain(String FilePath, int total_size_M) {
		int total_size_B = total_size_M * 1024 * 1024;
		return total_size_B - getDirSize(FilePath);
	}

	/**
	 * 例如:/usr/local/tomcat/webapps/demosite/index.jsp 返回:index.jsp
	 */
	public static String getFileName(String file_path) {
		if (file_path == null || file_path.length() == 0) {
			return null;
		}
		String separator = File.separator;
		separator = "/";
		if (file_path.length() < 1)
			return "";
		String f_name = separator + file_path;
		int pos = f_name.lastIndexOf(separator);
		return f_name.substring(pos + 1);
	}

	/**
	 * ͨ�����ļ�path�õ�通过给定的文件path得到路径名称
	 * 例如:/usr/local/tomcat/webapps/demosite/index.jsp
	 * 返回:/usr/local/tomcat/webapps/demosite/
	 */
	public static final String getPath(String file_path) {
		if (file_path == null || file_path.length() == 0) {
			return null;
		}
		String separator = File.separator;
		separator = "/";
		int endIndex = file_path.lastIndexOf(separator);
		if (endIndex > -1) {
			file_path = file_path.substring(0, endIndex + 1);
		} else {
			file_path = "";
		}
		return file_path;
	}

	public static long getFileSize(String file_path) {
		if (file_path == null || file_path.length() == 0) {
			return -1;
		}
		File myFile = new File(file_path);
		if (myFile.exists() && myFile.isFile())
			return myFile.length();
		else
			return -1;
	}

	public static boolean isDirectory(String file_path) {
		if (file_path == null || file_path.length() == 0) {
			return false;
		}
		File myFile = new File(file_path);
		if (myFile.exists() && myFile.isDirectory())
			return true;
		else
			return false;
	}

	public static boolean isFile(String file_path) {
		if (file_path == null || file_path.length() == 0) {
			return false;
		}
		File myFile = new File(file_path);
		if (myFile.exists() && myFile.isFile())
			return true;
		else
			return false;
	}

	public static int getFileNum(String file_path) {
		if (file_path == null || file_path.length() == 0) {
			return -1;
		}
		int result = 0;
		File path = new File(file_path);
		String[] list1;
		String separator = File.separator;
		if (path.isDirectory()) {
			list1 = path.list();
			for (int i = 0; i < list1.length; i++) {
				File f = new File(file_path + separator + list1[i]);
				if (f.isFile()) {
					result++;
				}
			}
		}
		return result;
	}

	public static int getAllFileNum(String file_path) {
		if (file_path == null || file_path.length() == 0) {
			return -1;
		}
		int result = 0;
		File path = new File(file_path);
		String[] list1;
		String separator = File.separator;
		if (path.isDirectory()) {
			list1 = path.list();
			for (int i = 0; i < list1.length; i++) {
				File f = new File(file_path + separator + list1[i]);
				if (f.isFile()) {
					result++;
				} else {
					result = result
							+ getAllFileNum(file_path + separator + list1[i]);
				}
			}
		}
		return result;
	}

	public static ArrayList fileSearch(String file_path, String str_search) {
		return fileSearch(file_path, str_search, 0);
	}

	public static ArrayList fileSearch(String file_path, String str_search,
			int fileviewType) {
		if (file_path == null || file_path.length() == 0) {
			return null;
		}
		ArrayList searchResult = new ArrayList();
		File path = new File(file_path);
		String[] list1;
		// int list2_length = 0;
		String separator = File.separator;
		if (path.isDirectory()) {
			list1 = path.list();
			if (list1 != null) {
				for (int i = 0; i < list1.length; i++) {
					File f = new File(file_path + separator + list1[i]);
					if (f.isDirectory()) {
						ArrayList tempResult = fileSearch(f.getPath(),
								str_search, fileviewType);
						if (!tempResult.isEmpty()) {
							for (int j = 0; j < tempResult.size(); j++) {
								searchResult.add(tempResult.get(j));
							}
						}
					}
				}
			}
			if (list1 != null) {
				for (int i = 0; i < list1.length; i++) {
					File f = new File(file_path + separator + list1[i]);
					if (f.isFile()) {
						if (list1[i].toLowerCase().indexOf(
								str_search.toLowerCase()) > -1) {
							if (fileviewType == 0) {
								searchResult.add(f.getName());
							} else {
								searchResult.add(f.getPath());
							}
						}

					}
				}
			}

		}
		return searchResult;
	}

	public static void mkDir(String FilePath) {
		if (FilePath == null || FilePath.length() == 0) {
			return;
		}
		try {
			File file = new File(FilePath);
			if (!file.exists()) {
				file.mkdirs();
				System.out.println("Sueecss making dir");
			}
		} catch (Exception exception) {
			System.out.println("Error making dir");
		}
	}

	/**
	 * 强制复制单个文件，如果目标文件已存在，则覆
	 */
	public static void forceCopyFile(String file_path_orig,
			String file_path_dest) {
		try {
			int bytesum = 0;
			int byteread = 0;
			File oldfile = new File(file_path_orig);
			if (oldfile.exists()) { // 文件存在�?
				InputStream inStream = new FileInputStream(file_path_orig); // 读入原文�?
				FileOutputStream fs = new FileOutputStream(file_path_dest);
				byte[] buffer = new byte[1444];
				while ((byteread = inStream.read(buffer)) != -1) {
					bytesum += byteread; // 字节�? 文件大小
					// System.out.println(bytesum);
					fs.write(buffer, 0, byteread);
				}
				inStream.close();
			}
		} catch (Exception e) {
			System.out.println("复制单个文件操作出错");
			e.printStackTrace();
		}
	}

	public static void copyFile(String file_path_orig, String file_path_dest) {
		if (!isFile(file_path_dest)) {
			forceCopyFile(file_path_orig, file_path_dest);
		}
	}

	/**
	 * 强制移动文件，如果目标文件已存在，则覆盖
	 */
	public static boolean forceMoveFile(String file_path_orig,
			String file_path_dest) {
		delFile(file_path_dest);
		return moveFile(file_path_orig, file_path_dest);
	}

	/**
	 * 移动文件，如果目标文件已存在，则不移动操�?
	 */
	public static boolean moveFile(String file_path_orig, String file_path_dest) {
		if (file_path_orig == null || file_path_orig.length() == 0
				|| file_path_dest == null || file_path_dest.length() == 0) {
			return false;
		}
		File file_orig = new File(file_path_orig);
		File path_dest = new File(file_path_dest);
		return file_orig.renameTo(path_dest);
	}

	/** 移动目录 */
	public static boolean moveDir(String file_path, String file_path_dest) {
		if (file_path == null || file_path.length() == 0) {
			return false;
		}
		// renameTo(File dest)
		if (isDirectory(file_path)) {
			// 创建目标目录����Ŀ��Ŀ¼
			mkDir(file_path_dest);
			delDir(file_path_dest);
			return moveFile(file_path, file_path_dest);
		}
		return false;
	}

	/**
	 * 删除文件
	 */
	public static boolean delFile(String file_path) {
		if (file_path == null || file_path.length() == 0) {
			return false;
		}
		boolean result = false;
		File path = new File(file_path);
		if (path.isFile()) {
			if (path.delete())
				result = true;
		}
		return result;
	}

	public static boolean delFiles(String filedir) {
		File path = new File(filedir);
		String[] list1;
		String separator = File.separator;
		if (path.isDirectory()) {
			list1 = path.list();
			File f;
			for (int i = 0; i < list1.length; i++) {
				f = new File(filedir + separator + list1[i]);
				if (f.isFile()) {
					// filapath = filedir + separator + list1[i];
					// delFile(filapath);
					f.delete();
				}

			}
			return true;

		} else {
			return false;
		}
	}

	/**
	 * 删除目录
	 */
	public static boolean delDir(String file_path) {
		boolean result = false;
		File path = new File(file_path);
		if (path.isDirectory()) {
			if (path.delete())
				result = true;
		}

		return result;
	}

	public static String getSeparator() {
		return File.separator;
	}

	private static boolean isVaildFilename(String filename) {
		if (filename == null || filename.equals(""))
			return false;
		for (int i = 0; i < invaildChars.length; i++) {
			if (filename.indexOf(invaildChars[i]) > -1)
				return false;
		}
		return true;
	}

	public static void uploadFile(HttpServletRequest request, String filePath,
			String fileName, long maxSize, String[] allowFormats)
			throws Exception {
		try {
			if (!isDirectory(filePath)) {
				mkDir(filePath);
			}
			DiskFileItemFactory factory = new DiskFileItemFactory();
			factory.setSizeThreshold(4096); // ���û������С��������4kb
			factory.setRepository(new File(System.getProperty("user.dir")));// ���û�����Ŀ¼
			// Create a new file upload handler
			ServletFileUpload upload = new ServletFileUpload(factory);

			// Set overall request size constraint
			upload.setSizeMax(maxSize); // ��������ļ��ߴ�

			List items = upload.parseRequest(request);// �õ����е��ļ�
			Iterator i = items.iterator();
			while (i.hasNext()) {
				FileItem fi = (FileItem) i.next();
				String fName = fi.getName();
				if (fName != null) {
					File savedFile = new File(filePath, fileName);
					fi.write(savedFile);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}
	}
}
