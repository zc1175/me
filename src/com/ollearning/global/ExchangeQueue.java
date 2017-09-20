package com.ollearning.global;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import com.ollearning.common.util.DateUtil;
import com.ollearning.common.util.Office2PDF;
import com.ollearning.common.util.PDF2SWF;
import com.ollearning.docfile.model.DocFile;

/**
 * 文档转换队列
 * 
 * @author xingry
 * 
 */
public class ExchangeQueue {

	private static LinkedBlockingQueue<DocFile> docFileQueue = new LinkedBlockingQueue<DocFile>();
	// 文档处理线程,只支持单线程处理文档转换
	private static Thread thread = null;
	// 线程扫描队列的时间间隔，单位为毫秒
	private static long intervalTime = 5000;

	private static boolean isRunning = true;

	/**
	 * 启动转换线程
	 */
	private static void startThread() {
		if (null == thread) {
			thread = new Thread(new Runnable() {
				@Override
				public void run() {
					for (; isRunning;) {
						System.out.println("[转换队列]["
								+ DateUtil.now("yyyy-MM-dd HH:mm:ss")
								+ "]当前队列大小:" + docFileQueue.size());
						if (docFileQueue.size() == 0) {
							try {
								Thread.currentThread().sleep(intervalTime);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							continue;
						}

						DocFile docFile = docFileQueue.poll();
						if (null == docFile) {
							try {
								Thread.currentThread().sleep(intervalTime);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							continue;
						}

						String pdfPath = docFile.getStr("basePath")
								+ "/pdf_files/" + docFile.getStr("fileName")
								+ ".pdf";
						String swfPath = docFile.getStr("basePath")
								+ "/swf_files/" + docFile.getStr("fileName")
								+ ".swf";

						// 如果当前上传的文件是OFFICE文档，则需转换为PDF
						if (docFile.getStr("docFilePath").lastIndexOf(".pdf") == -1) {
							new Office2PDF().office2pdf(
									docFile.getStr("docFilePath"), pdfPath);
						}
						try {
							System.out.println(pdfPath);
							PDF2SWF.pdf2swf(pdfPath, swfPath);
						} catch (Exception ex) {
							System.out.println("PDF文件不存在: " + pdfPath);
							ex.printStackTrace();
						}

						docFile.set(
								"pdfFilePath",
								"/pdf_files/" + docFile.getStr("fileName")
										+ ".pdf")
								.set("swfFilePath",
										"/swf_files/"
												+ docFile.getStr("fileName")
												+ ".swf").update();

						try {
							Thread.currentThread().sleep(intervalTime);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			});
			thread.start();
		}
	}

	// 停止转换线程
	private static void stopThread() {
		isRunning = false;
		thread = null;

	}

	/**
	 * 将文档加入队列
	 * 
	 * @param docFile
	 */
	public static boolean addQueue(DocFile docFile) {
		boolean ret = false;
		try {
			docFileQueue.add(docFile);
			ret = true;
		} catch (Exception ex) {
			;
		}
		if (ret) {
			if (null == thread) {
				startThread();
			}
		}
		return ret;
	}

	/**
	 * 获取队列中未处理文档列表
	 * 
	 * @return
	 */
	public static List<DocFile> getQueueList() {
		ArrayList<DocFile> fileList = new ArrayList<DocFile>();
		DocFile[] arr = (DocFile[]) docFileQueue.toArray();
		for (DocFile file : arr) {
			fileList.add(file);
		}
		return fileList;
	}

}
