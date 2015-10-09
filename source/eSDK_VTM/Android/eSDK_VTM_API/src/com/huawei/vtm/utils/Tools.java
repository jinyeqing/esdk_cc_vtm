package com.huawei.vtm.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.huawei.vtm.common.Constants;
import com.huawei.vtm.common.SystemSetting;
import com.huawei.vtm.service.VTMApp;

public class Tools
{

    /**
     * 字符串转换为数字。 <br>
     * 如果抛出NumberFormatException异常，则返回-1.
     * 
     */
    public static int parseInt(String source)
    {
        try
        {
            int target = Integer.parseInt(source);
            return target;
        }
        catch (NumberFormatException e)
        {
            LogUtils.e(e.toString());

            return -1;
        }
    }

    public static boolean isEmpty(String string)
    {
        if (null == string || "".equals(string))
        {
            return true;
        }

        return false;
    }

    public static boolean checkIP()
    {
        String ip = SystemSetting.getInstance().getServerIp();
        if (null == ip)
        {
            return false;
        }
        String regex = "^(2[0-4]\\d|25[0-5]|1\\d{2}|[1-9]\\d|[1-9])\\."
                + "(2[0-4]\\d|25[0-5]|1\\d{2}|[1-9]\\d|\\d)\\."
                + "(2[0-4]\\d|25[0-5]|1\\d{2}|[1-9]\\d|\\d)\\."
                + "(2[0-4]\\d|25[0-5]|1\\d{2}|[1-9]\\d|\\d)$";

        return Pattern.matches(regex, ip);
    }

    public static boolean checkPort()
    {
        int port = SystemSetting.getInstance().getServerPort();
        if (1 < port && port < 65535)
        {
            return true;
        }

        return false;
    }

    public static void deleteFile(File file)
    {
        if ((file == null) || (!file.exists()))
        {
            return;
        }

        if (file.isFile())
        {
            boolean success = file.delete();

            if (!success)
            {
                LogUtils.e("deleteFile", "delete file error");
            }

            return;
        }

        if (file.isDirectory())
        {
            File[] files = file.listFiles();

            if (files != null)
            {
                for (int i = 0; i < files.length; i++)
                {
                    deleteFile(files[i]);
                }
            }

            boolean success = file.delete();

            if (!success)
            {
                LogUtils.e("deleteFile", "delete file error");
            }
        }
    }

    public static void unZipFile()
    {
        if (VTMApp.getInstances().getApplication() == null)
        {
            return;
        }

        String path = Constants.ANNORESPATH;
        File file = new File(path);

        File[] files = file.listFiles();

        if (file.exists())
        {
            if (files != null && files.length == 7)
            {
                return;
            }
            else
            {
                Tools.deleteFile(file);
            }
        }
        else
        {
            if (!file.mkdirs())
            {
                return;
            }
        }
        ZipInputStream zipInputStream = null;
        FileOutputStream fileOutputStream = null;

        List<OutputStream> list = new ArrayList<OutputStream>();

        ZipEntry zipEntry = null;
        try
        {
            zipInputStream = new ZipInputStream(VTMApp.getInstances()
                    .getApplication().getAssets().open("AnnoRes.zip"));

            zipEntry = zipInputStream.getNextEntry();

            byte[] buffer = new byte[1048576];

            int count = 0;

            while (zipEntry != null)
            {
                if (zipEntry.isDirectory())
                {
                    file = new File(path + File.separator + zipEntry.getName());
                    if (!file.mkdirs())
                    {
                        break;
                    }
                }
                else
                {
                    file = new File(path + File.separator + zipEntry.getName());

                    boolean isCreate = file.createNewFile();
                    LogUtils.d("unZipFile", "==isDel==" + isCreate);
                    fileOutputStream = new FileOutputStream(file);
                    while ((count = zipInputStream.read(buffer)) > 0)
                    {
                        fileOutputStream.write(buffer, 0, count);
                    }
                    list.add(fileOutputStream);
                }

                zipEntry = zipInputStream.getNextEntry();
            }
        }
        catch (IOException e)
        {
            LogUtils.e("unZipFile", "close...Exception->e" + e.toString());
        }
        finally
        {
            if (zipInputStream != null)
            {
                try
                {
                    zipInputStream.close();
                }
                catch (IOException e2)
                {
                    LogUtils.e("closeInputStream",
                            "close try catch...Exception->e2" + e2.toString());
                }
                finally
                {
                    zipInputStream = null;
                }
            }

            try
            {
                for (int i = 0; list.size() > 0 && i < list.size(); i++)
                {
                    list.get(i).close();
                }
            }
            catch (IOException e2)
            {
                LogUtils.e("closeInputStream",
                        "close try catch...Exception->e2" + e2.toString());
            }
            finally
            {
                list.clear();
            }

            // if (fileOutputStream != null)
            // {
            // try
            // {
            // fileOutputStream.close();
            // }
            // catch (IOException e2)
            // {
            // LogUtils.e("closeInputStream",
            // "close try catch...Exception->e2" + e2.toString());
            // }
            // finally
            // {
            // fileOutputStream = null;
            // }
            // }
        }
    }

    /**
     * Function: 获取当前的IP地址.
     * @author luotianjia 00186254/huawei
     * @return String
     */
    public static String getLocalIpAddress()
    {
        String ip = "";
        try
        {
            Enumeration<NetworkInterface> networkInfo = NetworkInterface
                    .getNetworkInterfaces();
            NetworkInterface intf = null;
            Enumeration<InetAddress> intfAddress = null;
            InetAddress inetAddress = null;
            if (networkInfo == null)
            {
                LogUtils.e("getLocalIpAddress",
                        "get LocalIp address Error , return null value ");
                return "";
            }
            for (Enumeration<NetworkInterface> en = networkInfo; en
                    .hasMoreElements();)
            {
                intf = en.nextElement();
                intfAddress = intf.getInetAddresses();
                for (Enumeration<InetAddress> enumIpAddr = intfAddress; enumIpAddr
                        .hasMoreElements();)
                {
                    inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress())
                    {
                        ip = inetAddress.getHostAddress();
                        if (isIPV4Addr(ip))
                        {
                            LogUtils.i("getLocalIpAddress", "ip is " + ip);
                            return ip;
                        }
                    }
                }
            }
        }
        catch (SocketException e)
        {
            LogUtils.e("getLocalIpAddress", "SocketException | " + e.toString());
        }
        return ip;
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
