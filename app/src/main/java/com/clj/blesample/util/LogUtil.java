package com.clj.blesample.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Environment;
import android.util.Log;

public class LogUtil {
	private static boolean mDebugFlag = true;
	private static final String DEBUG_TAG = "LogUtil";

	private static String LogFileName = "Log.txt";
	private static SimpleDateFormat LogSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 日志的输出格式
	private static SimpleDateFormat LogfileSdf = new SimpleDateFormat("yyyy-MM-dd-HH");// 日志文件格式

	public static void enableDebug(boolean pDebugFlag) {
		mDebugFlag = pDebugFlag;
	}

	public static void log(String pTag, String pMsg) {
		if (mDebugFlag) {
			Log.e(pTag, pMsg);
			writeLogtoFile(pTag, pMsg);
		}
	}

	public static void log(String pMsg) {
		if (mDebugFlag) {
			Log.e(DEBUG_TAG, pMsg);
			writeLogtoFile(DEBUG_TAG, pMsg);
		}
	}



	/**
	 * 打开日志文件并写入日志
	 *
	 * @return
	 **/
	private static void writeLogtoFile(String tag, String text) {// 新建或打开日志文件
		Date nowtime = new Date();
		String needWriteFiel = LogfileSdf.format(nowtime);
		String needWriteMessage = LogSdf.format(nowtime) + "    " + "    " + tag + "    " + text;
		File file = new File(Environment.getExternalStorageDirectory() + "/ShenDaBleLog", needWriteFiel + LogFileName);
		//Log.e(DEBUG_TAG, file.toString());
		File PushPath = new File(Environment.getExternalStorageDirectory(), "/ShenDaBleLog");
		//Log.e(DEBUG_TAG, PushPath.toString());
		if (!PushPath.exists()) {
			//Log.e(DEBUG_TAG, "PushPath.mkdirs();");
			PushPath.mkdirs();
		}

		try {
			FileWriter filerWriter = new FileWriter(file, true);//后面这个参数代表是不是要接上文件中原来的数据，不进行覆盖
			BufferedWriter bufWriter = new BufferedWriter(filerWriter);
			bufWriter.write(needWriteMessage);
			bufWriter.newLine();
			bufWriter.close();
			filerWriter.close();

			//删除不是今天的日志文件
			delFile(file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 删除制定的日志文件
	 */
	public static void delFile(File file) {// 删除日志文件

		File parentdir = new File(Environment.getExternalStorageDirectory() + "/ShenDaBleLog");
		Log.e(DEBUG_TAG, parentdir.toString());
		String[] children = parentdir.list();
		//递归删除目录中的子目录下
		for (int i = 0; i < children.length; i++) {
			File delFile = new File(parentdir, children[i]);
			if (!delFile.getAbsolutePath().equalsIgnoreCase(file.getAbsolutePath())) {
				delFile.delete();
			}
		}
	}
}
