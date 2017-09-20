package com.ollearning.common.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;

import freemarker.template.Configuration;
import freemarker.template.Template;

public final class FreemarkerUtil {

	public static void exportWord(Map<String, Object> data,
			String templateName, String filePath) {
		try {
			Configuration conf = new Configuration();
			conf.setDefaultEncoding("utf-8");
			conf.setClassForTemplateLoading(FreemarkerUtil.class,
					"/com/ollearning/template/");
			Template template = conf.getTemplate(templateName);
			File outFile = new File(filePath);
			if (!outFile.getParentFile().exists()) {
				outFile.getParentFile().mkdirs();
			}
			Writer out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(outFile), "utf-8"));

			template.process(data, out);

			out.flush();
			out.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void main(String[] args) {
		exportWord(null, "ExamTemplate.ftl", "/opt/1.doc");
	}

}
