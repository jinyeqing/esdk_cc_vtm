package com.huawei.vtm.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;

import com.huawei.vtm.common.Constants;

public class LogUtils
{

	private static double logFileSize = 1024.00 * 5;

	private static boolean islogOpen = true;

	private final static String format = "yyyy-MM-dd kk:mm:ss";

	public static void setLogSwitch(boolean isOpen)
	{
		islogOpen = isOpen;
	}

	public static int i(String msg)
	{
		return i(null, msg);
	}

	public static int i(String tag, String msg)
	{
		if (islogOpen)
		{
			writeLog("info" + "-" + getTagName(tag) + " : " + msg);
		}
		return Log.i(getTagName(tag), msg);
	}

	public static int d(String msg)
	{
		return d(null, msg);
	}

	public static int d(String tag, String msg)
	{
		if (islogOpen)
		{
			writeLog("debug" + "-" + getTagName(tag) + " : " + msg);
		}
		return Log.d(getTagName(tag), msg);
	}

	public static int e(String msg)
	{
		return e(null, msg);
	}

	public static int e(String tag, String msg)
	{
		if (islogOpen)
		{
			writeLog("error" + "-" + getTagName(tag) + " : " + msg);
		}
		return Log.e(getTagName(tag), msg);
	}

	private static String getTagName(String tag)
	{
		return tag == null ? Constants.VTM_LOG : tag;
	}

	private static void writeLog(String logText)
	{
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
		{
			return;
		}

		String nowTimeStr = String.format("[%s]", DateFormat.format(format, System.currentTimeMillis()).toString());
//		String nowTimeStr = "Time:" + Long.toString(System.currentTimeMillis());
		
		String toLogStr = nowTimeStr + " " + logText;
		toLogStr += "\r\n";

		FileOutputStream fileOutputStream = null;
		String logFile = Environment.getExternalStorageDirectory().toString() + "/" + Constants.VTM_LOG_FILE;
		String filename = Constants.VTM_LOG_FILE_NAME;
		try
		{

			File fileOld = new File(logFile + "/" + filename);
			if ((float) ((fileOld.length() + logText.length()) / 1024.00) > logFileSize)
			{
				File bakFile = new File(fileOld.getPath() + ".bak");
				if (bakFile.exists())
				{
					if (bakFile.delete())
					{
						Log.d("Write Log", "delete " + bakFile.getName());
					}
				}
				if (fileOld.renameTo(bakFile))
				{
					Log.d("Write Log", fileOld.getName() + " rename to " + bakFile.getName());
				}
			}

			File file = new File(logFile);
			if (!file.exists())
			{
				if (file.mkdir())
				{
					Log.d("Write Log", "create " + file.getName());
				}
			}

			File filepath = new File(logFile + "/" + filename);
			if (!filepath.exists())
			{
				if (filepath.createNewFile())
				{
					Log.d("Write Log", "create " + filepath.getName());
				}
			}
			fileOutputStream = new FileOutputStream(filepath, true);

			byte[] buffer = toLogStr.getBytes(Constants.CHARSET_UTF_8);

			fileOutputStream.write(buffer);
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		} finally
		{
			if (fileOutputStream != null)
			{
				try
				{
					fileOutputStream.close();
				} catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

}
