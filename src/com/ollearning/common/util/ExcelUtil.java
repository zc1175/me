package com.ollearning.common.util;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.util.CellRangeAddress;

import com.jfinal.kit.PathKit;

public class ExcelUtil {

	public static String createExcel(String companyName, String headers,
			List<List<Object>> rows, String fileName) {
		String filePath = "";
		String fileAbsPath = "";
		try {
			filePath = "/temp/" + new String(fileName.getBytes("utf-8"), "gbk");
			fileAbsPath = PathKit.getWebRootPath() + filePath;
			String[] headerArray = headers.split(",");

			HSSFWorkbook wb = new HSSFWorkbook();
			HSSFSheet sheet = wb.createSheet(fileName);

			HSSFCellStyle style = getCellStyle(wb);
			style.setWrapText(true);
			HSSFCellStyle titleStyle = getTitleCellStyle(wb);

			// 标题
			HSSFRow row1 = sheet.createRow(0);
			row1.setHeight((short) 300);
			HSSFCell cell1 = row1.createCell(0);
			sheet.addMergedRegion(new CellRangeAddress(0, 0, 0,
					headerArray.length - 1));
			cell1.setCellStyle(titleStyle);
			cell1.setCellValue(companyName);

			// 列头
			HSSFRow row2 = sheet.createRow(1);
			for (int i = 0; i < headerArray.length; i++) {
				HSSFCell cellTemp = row2.createCell(i);
				cellTemp.setCellStyle(style);
				cellTemp.setCellValue(headerArray[i]);
			}
			// 数据行
			int rowLength = rows.size();
			for (int i = 0; i < rowLength; i++) {
				HSSFRow rowTemp = sheet.createRow(2 + i);
				List<Object> rowData = rows.get(i);
				for (int j = 0; j < headerArray.length; j++) {
					HSSFCell cellTemp = rowTemp.createCell(j);
					cellTemp.setCellStyle(style);
					cellTemp.setCellValue(null == rowData.get(j) ? "" : rowData
							.get(j).toString());
				}
			}

			sheet.setColumnWidth((short) 1, 600);
			sheet.setColumnWidth((short) 1, 300);
			// sheet.autoSizeColumn((short)1); //调整第一列宽度
			// sheet.autoSizeColumn((short)2); //调整第二列宽度

			FileOutputStream fos = new FileOutputStream(fileAbsPath);
			wb.write(fos);
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileAbsPath;
	}

	private static HSSFCellStyle getTitleCellStyle(HSSFWorkbook wb) {
		HSSFFont font = wb.createFont();
		font.setFontName("宋体");
		font.setFontHeight((short) 300);
		font.setBoldweight((short) 100);
		HSSFCellStyle style = wb.createCellStyle();
		style.setAlignment(HSSFCellStyle.ALIGN_LEFT);
		style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		style.setFont(font);
		return style;
	}

	private static HSSFCellStyle getCellStyle(HSSFWorkbook wb) {
		HSSFFont font = wb.createFont();
		font.setFontName("宋体");
		HSSFCellStyle style = wb.createCellStyle();
		style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		style.setBottomBorderColor(HSSFColor.GREY_40_PERCENT.index);
		style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		style.setBorderRight(HSSFCellStyle.BORDER_THIN);
		style.setBorderTop(HSSFCellStyle.BORDER_THIN);
		style.setFont(font);
		return style;
	}

}
