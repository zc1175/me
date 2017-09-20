package com.ollearning.videofile.model;

import java.util.List;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.ollearning.common.jfinal.Model;

public class VideoCollection extends Model<VideoCollection> {

	private static final long serialVersionUID = 3174400924303194095L;
	private static final String cacheItem = "videoCollection";
	public static final VideoCollection me = new VideoCollection();

	public VideoCollection() {
		super(cacheItem);
	}

	public Page<VideoCollection> findByPage(int pageNumber) {
		return paginate(pageNumber,
				com.ollearning.common.jfinal.Const.PAGE_SIZE_FOR_ADMIN,
				"select *",
				"from video_collection where enabled=1 order by id desc");
	}

	public List<VideoCollection> findAll() {
		return find("select * from video_collection where enabled=1 order by id desc");
	}

	public void updateVideoNum() {
		int row = 0;
		try {
			row = Db.findFirst(
					"select count(*) as cnt from videos where videoCollectionId=?",
					getLong("id").intValue()).getLong("cnt").intValue();
		} catch (Exception e) {
			;
		}
		set("videoNum", row);
	}

	public Video getFirstVideo() {
		Page<Video> page = Video.dao.findByVideoCollectionId(1, getLong("id")
				.intValue());
		if (null != page && page.getList().size() > 0) {
			return page.getList().get(0);
		}
		return null;
	}

}
