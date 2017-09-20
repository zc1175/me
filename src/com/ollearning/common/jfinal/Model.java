package com.ollearning.common.jfinal;

import java.util.List;

import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.ehcache.CacheKit;
import com.jfinal.plugin.ehcache.IDataLoader;
import com.ollearning.common.kit.HtmlKit;

public class Model<M extends com.jfinal.plugin.activerecord.Model> extends
		com.jfinal.plugin.activerecord.Model<M> {
	private static final long serialVersionUID = -9219932823106293234L;
	private String cacheNameForModel;

	public Model(String cacheNameForModel) {
		this.cacheNameForModel = cacheNameForModel;
	}

	public Page<M> loadModelPage(Page<M> page) {
		List<M> modelList = page.getList();
		for (int i = 0; i < modelList.size(); i++) {
			com.jfinal.plugin.activerecord.Model model = modelList.get(i);
			M topic = loadModel(model.getInt("id"));
			modelList.set(i, topic);
		}
		return page;
	}

	public M loadModel(int id) {
		final Object ID = id;
		return (M) CacheKit.get(cacheNameForModel, ID, new IDataLoader() {
			@Override
			public Object load() {
				return findById(ID);
			}
		});
	}

	public Model<M> filterText(String... attrNames) {
		for (String attrName : attrNames) {
			this.set(attrName, HtmlKit.getText(this.getStr(attrName)));
		}
		return this;
	}

	public Model<M> filterBasicHtmlAndImage(String... attrNames) {
		for (String attrName : attrNames) {
			this.set(attrName,
					HtmlKit.getBasicHtmlAndImage(this.getStr(attrName)));
		}
		return this;
	}

	public boolean update() {
		boolean ret = super.update();
		CacheKit.put(cacheNameForModel, get("id"), this);
		return ret;
	}

	public boolean delete() {
		boolean ret = super.delete();
		CacheKit.remove(cacheNameForModel, get("id"));
		return ret;
	}

	public void removeAllPageCache(String... cacheNames) {
		for (String s : cacheNames) {
			CacheKit.removeAll(s);
		}
	}

}
