/*
 * 
 *  @(#)StringUtil.java	Created on 2014年9月2日
 *
 * Copyright 2004-2005 Huawei Tech. Co. Ltd. All Rights Reserved.
 * 
 * Description 
 * 
 * CopyrightVersion 
 *
 */
package com.huawei.vtm;

import java.io.IOException;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.huawei.vtm.service.VTMApp;

/**
 * <p>
 * Title: 字符串处理工具类(String Processing Tools)
 * </p>
 * <p>
 * Description: 字符串处理工具类(String Processing Tools)
 * </p>
 * 
 * <pre> </pre>
 * <p>
 * Copyright: Copyright (c) 2005
 * </p>
 * <p>
 * Company: Huawei Technologies Co.
 * </p>
 * 
 * @author j00204006
 * @version V1.0 2014年9月2日
 * @since
 */
public class StringUtils
{
	/**
	 * 判断字符串是否为null或者空字符串（不含空格）。 Determine whether the string is null or empty
	 * string (no spaces).
	 * 
	 * @param str
	 *            字符串变量(String input)
	 * @return true/false
	 */
	public static boolean isNullOrEmpty(String str)
	{
		return str == null || str.isEmpty();
	}

	/**
	 * 判断字符串是否为null或者为空字符串（含空格）。 Determine whether the string is null or empty
	 * string (including spaces).
	 * 
	 * @param str
	 *            字符串变量(String Input)
	 * @return true/false
	 */
	public static boolean isNullOrBlank(String str)
	{
		return str == null || str.trim().isEmpty();
	}

//	/**
//	 * 对象转json字符串 Object to json
//	 * 
//	 * @param object
//	 *            对象(object)
//	 * @return json json字符串(Json String)
//	 * @throws IOException
//	 */
//	public static String beanToJson(Object object)
//	{
//		ObjectMapper mapper = new ObjectMapper();
//		StringWriter writer = new StringWriter();
//		JsonGenerator gen = null;
//		String json = null;
//		try
//		{
//			gen = new JsonFactory().createJsonGenerator(writer);
//
//			gen.setPrettyPrinter(new DefaultPrettyPrinter());
//
//			mapper.writeValue(gen, object);
//
//			json = writer.toString();
//
//			writer.close();
//			gen.close();
//
//		} catch (IOException e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} finally
//		{
//			try
//			{
//				if (gen != null && !gen.isClosed())
//				{
//					gen.close();
//				}
//			} catch (Exception e2)
//			{
//				// TODO: handle exception
//			}
//
//		}
//
//		return json;
//
//	}

	public static boolean isStringEmpty(String source)
	{
		return source == null || "".equals(source);
	}

	public static int stringToInt(String str)
	{
		return stringToInt(str, -1);
	}

	public static int stringToInt(String str, int defaultValue)
	{
		if (isStringEmpty(str))
		{
			return defaultValue;
		}
		try
		{
			return Integer.parseInt(str);
		} catch (NumberFormatException e)
		{
			return defaultValue;
		}
	}

	/**
	 * 方法名称：fintElement 作者：wangjian 方法描述：查找xml中的字段 输入参数：@param source
	 * 输入参数：@param startTag 输入参数：@param endTag 输入参数：@return 返回类型：String： 备注：
	 */
	public static String findStringElement(String source, String startTag, String endTag)
	{
		return findStringElement(source, startTag, endTag, null);
	}

	/**
	 * 从字符串中查找第一个子串
	 *
	 * @param source
	 *            原字符串
	 * @param startTag
	 *            开始子串
	 * @param endTag
	 *            结束子串
	 * @param defaultValue
	 *            默认返回值
	 * @return String
	 */
	public static String findStringElement(String source, String startTag, String endTag, String defaultValue)
	{
		if (source == null)
		{
			return defaultValue;
		}
		int i = source.indexOf(startTag);
		int j = source.indexOf(endTag, i);
		if ((i != -1) && (j != -1) && j > i)
		{
			return source.substring(i + startTag.length(), j);
		}
		return defaultValue;
	}

	public static long findLongElement(String source, String startTag, String endTag)
	{
		return stringToLong(findStringElement(source, startTag, endTag));
	}

	public static long stringToLong(String str)
	{
		if (isStringEmpty(str))
		{
			return -1;
		}

		try
		{
			return Long.parseLong(str);
		} catch (NumberFormatException e)
		{
			return -1;
		}
	}

	public static int findIntElement(String source, String startTag, String endTag, int defaultValue)
	{
		String temp = findStringElement(source, startTag, endTag);
		return stringToInt(temp, defaultValue);
	}

	public static String filterLog(String s)
	{
		if (!isStringEmpty(s))
		{
			return String.valueOf(s.charAt(s.length() - 1));
		}

		return null;
	}

	/**
	 * 删除指定位置的字符
	 *
	 * @param source
	 *            原字符串
	 * @param pos
	 *            删除位置
	 * @param c
	 *            字符
	 * @return 删除后的字符串
	 */
	public static String remove(String source, int pos, char c)
	{
		String result = source;

		if ((source == null) || "".equals(source))
		{
			return "";
		}

		if ((pos < 0) || (pos >= source.length()))
		{
			return result;
		}

		if (c == source.charAt(pos))
		{
			result = source.substring(pos + 1);
		}

		return result;
	}

	/**
	 * 判断是否是3G登录
	 * 
	 * @return boolean
	 */
	public static boolean is3GConnect()
	{
		// 0623
		ConnectivityManager cm = (ConnectivityManager) (VTMApp.getInstances().getApplication().getSystemService(Context.CONNECTIVITY_SERVICE));
		NetworkInfo info = cm.getActiveNetworkInfo();
		if (info == null)
		{
			return false;
		}

		// "LTE".equalsIgnoreCase(info.getTypeName()) 对LTE专网，需要进行此项设置。
		// eLTE集群终端EP820
		if (info.getType() == ConnectivityManager.TYPE_MOBILE || "LTE".equalsIgnoreCase(info.getTypeName()))
		{
			return info.isConnected();
		}

		return false;
	}

	/**
	 * Function: 检查是否存在 HW_VIRTUAL_CARD 三层SVN的虚拟IP
	 * 
	 * @author luotianjia 00186254/huawei
	 * @return String
	 */
	public static String getIdeskIP()
	{
		String ip = null;
		List<String> infoList = new ArrayList<String>();
		try
		{
			Enumeration<NetworkInterface> networkInfo = NetworkInterface.getNetworkInterfaces();
			if (networkInfo == null)
			{
				System.out.println("ggg StringUtils - get getIdeskIP address Error , return null value");
				return ip;
			}
			NetworkInterface intf = null;
			for (Enumeration<NetworkInterface> en = networkInfo; en.hasMoreElements();)
			{
				intf = en.nextElement();
				infoList.add(intf.toString());
			}
		} catch (SocketException e)
		{
			System.out.println("ggg StringUtils - " + e.toString());
		}

		for (String address : infoList)
		{
			if (address.contains("HW_VIRTUAL_CARD"))
			{
				ip = address.substring(address.lastIndexOf("[/") + 2, address.lastIndexOf("]"));
				System.out.println("ggg StringUtils - HW_VIRTUAL_CARD ip  == " + ip);
			}
		}
		return ip;
	}

	/**
	 * Function: 获取当前的IP地址.
	 * 
	 * @author luotianjia 00186254/huawei
	 * @return String
	 */
	public static String getIpAddress()
	{
	//	WifiManager wifiManager = (WifiManager) com.huawei.esdk.vtm.demo.VTMApp.getApp().getSystemService(Context.WIFI_SERVICE);
		WifiManager wifiManager = (WifiManager) VTMApp.getInstances().getApplication().getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		if (wifiInfo == null)
		{
			return "";
		}
		int ipAddress = wifiInfo.getIpAddress();
		String ip = intToIp(ipAddress);
		System.out.println("ggg StringUtils - wifi: intAddr[" + ipAddress + "],stringAddr[" + ip + "]");
		if (0 != ipAddress)
		{
			return ip;
		}
		try
		{
			Enumeration<NetworkInterface> networkInfo = NetworkInterface.getNetworkInterfaces();
			NetworkInterface intf = null;
			Enumeration<InetAddress> intfAddress = null;
			InetAddress inetAddress = null;
			if (networkInfo == null)
			{
				System.out.println("ggg StringUtils - get getIdeskIP address Error , return null value");
				return "";
			}
			for (Enumeration<NetworkInterface> en = networkInfo; en.hasMoreElements();)
			{
				intf = en.nextElement();
				intfAddress = intf.getInetAddresses();
				for (Enumeration<InetAddress> enumIpAddr = intfAddress; enumIpAddr.hasMoreElements();)
				{
					inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress())
					{
						ip = inetAddress.getHostAddress();
						if (isIPV4Addr(ip))
						{
							if ("10.0.2.15".equals(ip))
							{
								continue;
							} else
							{
								System.out.println("ggg StringUtils - ip is " + ip);
								return ip;
							}
						}
					}
				}
			}
		} catch (SocketException e)
		{
			System.out.println("ggg StringUtils - getIpAddress caught exception: "+ e.toString());
		}
		return ip;
	}
	
    /**
     * 方法名称：getLocalIpAddress
     * 作者：wangjian
     * 方法描述：获取本机IP
     * 输入参数：@return
     * 返回类型：String：
     * 备注：
     */
    public static String intToIp(int i)
    {
        return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF)
                + "." + ((i >> 24) & 0xFF);
    }

    /**
     * 判断是否是ipv4地址
     * @param ipAddr
     * @return
     */
    public static boolean isIPV4Addr(String ipAddr)
    {
        Pattern p = Pattern
                .compile("^((25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])\\.){3}"
                        + "(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])$");
        return p.matcher(ipAddr).matches();
        
    }
    
}
