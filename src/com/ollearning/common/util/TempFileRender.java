package com.ollearning.common.util;

import java.io.File;

import com.jfinal.render.FileRender;

public class TempFileRender extends FileRender {

	private static final long serialVersionUID = 6872938146617148564L;
	private String fileName;
	private File file;

	public TempFileRender(String fileName) {
		super(fileName);
		this.fileName = fileName;
	}

	public TempFileRender(File file) {
		super(file);
		this.file = file;
	}

	@Override
	public void render() {
		try {
			super.render();
		} finally {
			if (null != fileName) {
				file = new File(fileName);
			}
			if (null != file) {
				try {
					file.delete();
					file.deleteOnExit();
				} catch (Exception ex) {
					;
				}
			}
		}
	}

}
