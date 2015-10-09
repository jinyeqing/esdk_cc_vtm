package com.huawei.vtm.utils;

import java.util.Locale;

public class HEX_GLY
{

    public static String encode(byte[] btData, /* 数据 */
            int iLen /* 数据的长度，当为 0 时，取 btData 数组的大小 */
    )
    {
        StringBuffer l_stTmp = null;
        StringBuffer l_stHex = new StringBuffer("");
        int ii;

        if (btData == null)
        {
            return null;
        }

        if ((iLen <= 0) || (iLen > btData.length))
        {
            iLen = btData.length;
        }

        for (ii = 0; ii < iLen; ii++)
        {
            l_stTmp = new StringBuffer(Integer.toHexString(btData[ii] & 0xFF));

            if (l_stTmp.length() == 1)
            {
                l_stTmp.append("0");
                l_stTmp.append(l_stTmp);
            }

            l_stHex.append(l_stTmp.toString().toUpperCase(Locale.getDefault()));
        }

        l_stTmp = null;

        return l_stHex.toString();
    }

    public static byte[] decode(String stData) // 转换失败返回 null
    {
        byte[] l_btData = null;
        byte[] l_btTmp = null;
        String l_stData = null;
        int l_iLen;
        int ii;
        int jj;
        int kk;
        char l_cTmp;

        if (stData == null)
        {
            return new byte[0];
        }

        l_iLen = stData.length();

        if ((l_iLen % 2) != 0)
        {
            return new byte[0];
        }

        l_stData = stData.toUpperCase(Locale.getDefault());

        for (ii = 0; ii < l_iLen; ii++)
        {
            l_cTmp = l_stData.charAt(ii);

            if (!((('0' <= l_cTmp) && (l_cTmp <= '9')) || (('A' <= l_cTmp) && (l_cTmp <= 'F'))))
            {
                return new byte[0];
            }
        }

        l_iLen /= 2;

        l_btData = new byte[l_iLen];

        l_btTmp = new byte[2];

        for (ii = 0, jj = 0, kk = 0; ii < l_iLen; ii++)
        {
            l_btTmp[0] = (byte) (l_stData.charAt(jj++));
            l_btTmp[1] = (byte) (l_stData.charAt(jj++));

            for (kk = 0; kk < 2; kk++)
            {
                if (('A' <= l_btTmp[kk]) && (l_btTmp[kk] <= 'F'))
                {
                    l_btTmp[kk] -= 55;
                }
                else
                {
                    l_btTmp[kk] -= 48;
                }
            }

            l_btData[ii] = (byte) ((l_btTmp[0] << 4) | l_btTmp[1]);
        }

        l_btTmp = null;
        l_stData = null;

        return l_btData;
    }

    private HEX_GLY()
    {
    }
}
