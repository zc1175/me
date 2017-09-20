package com.ollearning.site.model;

import com.jfinal.plugin.ehcache.CacheKit;
import com.ollearning.common.jfinal.Model;

public class SiteInfo extends Model<SiteInfo> {
	private static final long serialVersionUID = 2084983498832012965L;
	private static final String SITE_INFO_CACHE = "siteInfo";

	// name , domain,title,description , keywords ,

	public static SiteInfo dao = new SiteInfo();

	public SiteInfo() {
		super(SITE_INFO_CACHE);
	}

	public boolean update() {
		CacheKit.remove(SITE_INFO_CACHE, 1);
		return super.update();
	}

}
