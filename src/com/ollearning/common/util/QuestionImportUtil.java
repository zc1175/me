package com.ollearning.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import com.jfinal.kit.PathKit;
import com.jfinal.kit.StrKit;
import com.ollearning.global.BIConfig;
import com.ollearning.testing.model.ExamType;
import com.ollearning.testing.model.Question;
import com.ollearning.testing.model.QuestionSub;
import com.ollearning.testing.model.QuestionType;
import com.ollearning.testing.model.Subject;

public class QuestionImportUtil {

	// 导入EXCEL
	public static Map<String, Integer> doImportExcel(String filePath,
			int examTypeId) {
		// 保存导入错误的试题
		// String wrongFilePath = PathKit.getWebRootPath() +
		// BIConfig.QUESTION_DIR + DateUtil.now("yyyyMMddHHmmss")+".xls";
		// File wrongFile = new File(wrongFilePath);
		// if(!wrongFile.exists())
		// try {
		// wrongFile.createNewFile();
		// } catch (IOException e1) {
		// e1.printStackTrace();
		// }

		Map<String, Integer> ret = new HashMap<String, Integer>();
		File file = new File(filePath);
		if (!file.exists())
			return ret;

		int choiceNum = 0; // 选择题正确导入数量
		HSSFWorkbook workbook = null;
		InputStream is = null;
		try {
			is = new FileInputStream(file);
			workbook = new HSSFWorkbook(is);
			try {
				// 选择题
				HSSFSheet sheet = workbook.getSheetAt(1);

				Iterator<Row> rows = sheet.rowIterator();
				HSSFRow row = null;
				while (rows.hasNext()) {
					try {
						row = (HSSFRow) rows.next();
						if (0 == row.getRowNum())
							continue;

						String subjectName = "";
						if (null != row.getCell(1)) {
							try {
								row.getCell(1).setCellType(
										Cell.CELL_TYPE_STRING);
								subjectName = row.getCell(1)
										.getStringCellValue();
							} catch (Exception e) {
							}
						}
						int subjectId = -1;
						if (StrKit.notBlank(subjectName)) {
							Subject subject = Subject.dao
									.findByName(subjectName);
							if (null != subject)
								subjectId = subject.getInt("id");
						}

						String diffcult = "";
						if (null != row.getCell(2)) {
							try {
								row.getCell(2).setCellType(
										Cell.CELL_TYPE_STRING);
								diffcult = row.getCell(2).getStringCellValue();
							} catch (Exception e) {
							}
						}
						String knowleges = "";
						if (null != row.getCell(3)) {
							try {
								row.getCell(3).setCellType(
										Cell.CELL_TYPE_STRING);
								knowleges = row.getCell(3).getStringCellValue();
							} catch (Exception e) {
								;
							}
						}
						String keywords = "";
						if (null != row.getCell(4)) {
							row.getCell(4).setCellType(Cell.CELL_TYPE_STRING);
							keywords = row.getCell(4).getStringCellValue();
						}
						String title = "";
						if (null != row.getCell(5)) {
							try {
								row.getCell(5).setCellType(
										Cell.CELL_TYPE_STRING);
								title = row.getCell(5).getStringCellValue();
							} catch (Exception e) {
								;
							}
						}
						String optiona = "";
						if (null != row.getCell(6)) {
							try {
								row.getCell(6).setCellType(
										Cell.CELL_TYPE_STRING);
								optiona = row.getCell(6).getStringCellValue();
							} catch (Exception e) {
							}
						}
						String optionb = "";
						if (null != row.getCell(7)) {
							try {
								row.getCell(7).setCellType(
										Cell.CELL_TYPE_STRING);
								optionb = row.getCell(7).getStringCellValue();
							} catch (Exception e) {
								;
							}
						}
						String optionc = "";
						if (null != row.getCell(8)) {
							try {
								row.getCell(8).setCellType(
										Cell.CELL_TYPE_STRING);
								optionc = row.getCell(8).getStringCellValue();
							} catch (Exception e) {
								;
							}
						}
						String optiond = "";
						if (null != row.getCell(9)) {
							try {
								row.getCell(9).setCellType(
										Cell.CELL_TYPE_STRING);
								optiond = row.getCell(9).getStringCellValue();
							} catch (Exception e) {
								;
							}
						}
						String optione = "";
						if (null != row.getCell(10)) {
							try {
								row.getCell(10).setCellType(
										Cell.CELL_TYPE_STRING);
								optione = row.getCell(10).getStringCellValue();
							} catch (Exception e) {
								;
							}
						}
						String optionf = "";
						if (null != row.getCell(11)) {
							try {
								row.getCell(11).setCellType(
										Cell.CELL_TYPE_STRING);
								optionf = row.getCell(11).getStringCellValue();
							} catch (Exception e) {
								;
							}
						}
						String answer = "";
						if (null != row.getCell(12)) {
							try {
								row.getCell(12).setCellType(
										Cell.CELL_TYPE_STRING);
								answer = row.getCell(12).getStringCellValue();
							} catch (Exception e) {
								;
							}
						}

						String description = "";
						if (null != row.getCell(13)) {
							try {
								row.getCell(13).setCellType(
										Cell.CELL_TYPE_STRING);
								description = row.getCell(13)
										.getStringCellValue();
							} catch (Exception e) {
								;
							}
						}

						int questionTypeId = -1;
						String questionTypeName = "";
						QuestionType questionType = null;
						if (answer.trim().length() == 1) {
							questionType = QuestionType.dao.findByName("单选");
						} else {
							questionType = QuestionType.dao.findByName("多选");
						}

						if (null != questionType) {
							questionTypeId = questionType.getInt("id");
							questionTypeName = questionType.getStr("name");
						}

						try {
							Question question = new Question();
							question.set("examTypeId", examTypeId)
									.set("typeName",
											ExamType.dao.loadModel(examTypeId)
													.getStr("name"))
									.set("diffcult", diffcult)
									.set("keywords", keywords)
									.set("knowledges", knowleges)
									.set("questionTypeId", questionTypeId)
									.set("questionTypeName", questionTypeName)
									.set("subjectId", subjectId)
									.set("title", title).set("childNum", 1)
									.set("deleted", 0)
									.set("description", description).save();

							new QuestionSub()
									.set("questionId", question.getInt("id"))
									.set("optiona", optiona)
									.set("optionb", optionb)
									.set("optionc", optionc)
									.set("optiond", optiond)
									.set("optione", optione)
									.set("optionf", optionf)
									.set("answer", answer).save();

						} catch (Exception ex) {
							ex.printStackTrace();
						}

						choiceNum++;

					} catch (Exception e) {
						System.out.println("XuanZe import failure rownum："
								+ row.getRowNum());
						e.printStackTrace();
					}
				}
				ret.put("单选题", choiceNum);
			} catch (Exception e) {
				e.printStackTrace();
			}

			int judgeNum = 0;
			try {
				// 判断题
				HSSFSheet sheet = workbook.getSheetAt(2);

				Iterator<Row> rows = sheet.rowIterator();
				HSSFRow row = null;
				while (rows.hasNext()) {
					try {
						row = (HSSFRow) rows.next();
						if (0 == row.getRowNum())
							continue;

						String subjectName = "";
						if (null != row.getCell(0)) {
							try {
								row.getCell(0).setCellType(
										Cell.CELL_TYPE_STRING);
								subjectName = row.getCell(0)
										.getStringCellValue();
							} catch (Exception e) {
								;
							}
						}
						int subjectId = -1;
						if (StrKit.notBlank(subjectName)) {
							try {
								Subject subject = Subject.dao
										.findByName(subjectName);
								if (null != subject)
									subjectId = subject.getInt("id");
							} catch (Exception e) {
								;
							}
						}
						String diffcult = "";
						if (null != row.getCell(1)) {
							try {
								row.getCell(1).setCellType(
										Cell.CELL_TYPE_STRING);
								diffcult = row.getCell(1).getStringCellValue();
							} catch (Exception e) {
								;
							}
						}
						String knowleges = "";
						if (null != row.getCell(2)) {
							try {
								row.getCell(2).setCellType(
										Cell.CELL_TYPE_STRING);
								knowleges = row.getCell(2).getStringCellValue();
							} catch (Exception e) {
								;
							}
						}
						String keywords = "";
						if (null != row.getCell(3)) {
							try {
								row.getCell(3).setCellType(
										Cell.CELL_TYPE_STRING);
								keywords = row.getCell(3).getStringCellValue();
							} catch (Exception e) {
								;
							}
						}
						String title = "";
						if (null != row.getCell(4)) {
							try {
								row.getCell(4).setCellType(
										Cell.CELL_TYPE_STRING);
								title = row.getCell(4).getStringCellValue();
							} catch (Exception e) {
								;
							}
						}
						String answer = "";
						if (null != row.getCell(5)) {
							row.getCell(5).setCellType(Cell.CELL_TYPE_STRING);
							answer = row.getCell(5).getStringCellValue();
						}

						String description = "";
						if (null != row.getCell(6)) {
							try {
								row.getCell(6).setCellType(
										Cell.CELL_TYPE_STRING);
								description = row.getCell(6)
										.getStringCellValue();
							} catch (Exception e) {
								;
							}

						}

						if (answer.trim().indexOf("对") != -1
								|| answer.trim().indexOf("正确") != -1) {
							answer = "A";
						} else {
							answer = "B";
						}

						int questionTypeId = -1;
						String questionTypeName = "";
						QuestionType questionType = QuestionType.dao
								.findByName("判断");
						if (null != questionType) {
							questionTypeId = questionType.getInt("id");
							questionTypeName = questionType.getStr("name");
						}

						try {
							Question question = new Question();
							question.set("examTypeId", examTypeId)
									.set("typeName",
											ExamType.dao.loadModel(examTypeId)
													.getStr("name"))
									.set("diffcult", diffcult)
									.set("keywords", keywords)
									.set("knowledges", knowleges)
									.set("questionTypeId", questionTypeId)
									.set("questionTypeName", questionTypeName)
									.set("subjectId", subjectId)
									.set("title", title)
									.set("description", description)
									.set("childNum", 1).set("deleted", 0)
									.save();

							new QuestionSub()
									.set("questionId", question.getInt("id"))
									.set("optiona", "对").set("optionb", "错")
									.set("answer", answer).save();

							judgeNum++;

						} catch (Exception ex) {
							ex.printStackTrace();
						}

					} catch (Exception e) {
						System.out.println("PanDuan import failure rownum："
								+ row.getRowNum());
						e.printStackTrace();
					}
				}
				ret.put("判断题", judgeNum);
			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				workbook.close();
				workbook = null;
				is.close();
				is = null;
			} catch (Exception ex) {
				;
			}
		}

		return ret;

	}

	public static Map<String, Integer> doImportWord(String filePath,
			int examTypeId) {
		Map<String, Integer> ret = new HashMap<String, Integer>();
		return ret;
	}
}
