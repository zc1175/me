package com.ollearning.videofile.model;

import com.jfinal.plugin.activerecord.Page;
import com.ollearning.common.jfinal.Const;
import com.ollearning.common.jfinal.Model;
import com.ollearning.common.util.FileUtil;

public class VideoFile extends Model<VideoFile> {

	private static final long serialVersionUID = 270836461800592337L;

	public static VideoFile dao = new VideoFile();

	public VideoFile() {
		super("videoFile");
	}

	public Page<VideoFile> getPageForAdmin(Integer pageNumber) {
		String sqlSelect = " from video_files where 1=1 and isDelete=0";
		sqlSelect += " order by  id desc";
		Page<VideoFile> page = super.paginate(pageNumber,
				Const.PAGE_SIZE_FOR_ADMIN, "select *", sqlSelect);
		return loadModelPage(page);
	}

	public String getFormatSize() {
		return FileUtil.formatSize(getInt("fileSize"));
	}

	public VideoFile findByPid(String pid) {
		String sql = "select * from video_files where pid='" + pid + "'";
		return findFirst(sql);
	}

	public VideoFile findByVideoId(Integer videoId) {
		return findFirst("select * from video_files where videoId=?", videoId);
	}

	public String getVideoTypeImg() {
		String fileType = getStr("fileType");
		if (null == fileType)
			return "";
		if (null != fileType && fileType.indexOf("mp4") != -1) {
			return "movies.png";
		}
		if (fileType.indexOf("flv") != -1) {
			return "movies.png";
		}
		return "movies.png";
	}

	public static String getFileType(String fileName) {
		if (fileName.endsWith(".mp4")) {
			return "video/mp4";
		} else if (fileName.endsWith(".flv")) {
			return "video/x-flv";
		} else if (fileName.endsWith(".ogv")) {
			return "video/ogg";
		} else if (fileName.endsWith(".webm")) {
			return "video/webm";
		} else if (fileName.endsWith(".wmv")) {
			return "video/mp4";
		}
		return "video/mp4";
	}
}
