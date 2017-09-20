package com.ollearning.docfile.model;

import java.io.File;

import com.jfinal.plugin.activerecord.Page;
import com.ollearning.common.jfinal.Const;
import com.ollearning.common.jfinal.Model;
import com.ollearning.common.util.FileUtil;

public class DocFile extends Model<DocFile> {

	private static final long serialVersionUID = -502258400105599254L;

	public static DocFile dao = new DocFile();

	public DocFile() {
		super("docFile");
	}

	public Page<DocFile> getPageForAdmin(Integer pageNumber) {
		String sqlSelect = " from doc_files where 1=1 and isDelete=0";
		sqlSelect += " order by  id desc";
		Page<DocFile> page = super.paginate(pageNumber,
				Const.PAGE_SIZE_FOR_ADMIN, "select *", sqlSelect);
		return loadModelPage(page);
	}

	public String getFormatSize() {
		Integer size = getInt("fileSize");
		if (null != size) {
			return FileUtil.formatSize(size);
		}
		return "未知";
	}

	public DocFile findByPid(String pid) {
		String sql = "select * from doc_files where pid='" + pid + "'";
		return findFirst(sql);
	}

	// 根据文档编号查询附件
	public DocFile findByDocId(Integer docId) {
		return findFirst(
				"select * from doc_files where docId=? order by id desc", docId);
	}

	public void deleteDocFile(Integer docFileId) {
		DocFile docfile = DocFile.dao.findById(docFileId);
		try {
			new File(docfile.getStr("docFilePath")).delete();
			new File(docfile.getStr("basePath") + docfile.getStr("pdfFilePath"))
					.delete();
			new File(docfile.getStr("basePath") + docfile.getStr("swfFilePath"))
					.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
		docfile.delete();
	}

}
