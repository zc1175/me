package com.ollearning.docfile.model;

import java.util.List;

import com.ollearning.common.jfinal.Model;

public class DocKeywords extends Model<DocKeywords> {

	private static final long serialVersionUID = -6273627313595092707L;

	public static DocKeywords dao = new DocKeywords();

	public DocKeywords() {
		super(DocKeywords.class.getName());
	}

	public List<DocKeywords> getByDocId(Integer docId) {
		return find("select * from docs_keywords where docId=?", docId);
	}

}
