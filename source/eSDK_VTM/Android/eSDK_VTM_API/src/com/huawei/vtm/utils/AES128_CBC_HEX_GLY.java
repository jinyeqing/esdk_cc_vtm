package com.huawei.vtm.utils;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public final class AES128_CBC_HEX_GLY
{
    public static String encode(byte[] btPlain, /* 明文 */
            int iLen, /* 明文的长度，当为 0 时，取 btPlain 数组的大小 */
            byte[] btKey, /* 密钥 */
            int iKeyLen, /* 密钥的长度，当为 0 时，取 btKey 数组的大小 */
            byte[] btIV, /* 初始向量 */
            int iIVLen /* 初始向量的长度，当为 0 时，取 btIV 数组的大小 */
    ) throws BadPaddingException, IllegalBlockSizeException,
            InvalidAlgorithmParameterException, InvalidKeyException,
            NoSuchAlgorithmException, NoSuchPaddingException
    {
        return HEX_GLY.encode(AES128_CBC_GLY.encode(btPlain, iLen, btKey,
                iKeyLen, btIV, iIVLen), 0);
    }

    public static byte[] decode(String stHex, /* 16 进制编码的密文 */
            byte[] btKey, /* 密钥 */
            int iKeyLen, /* 密钥的长度，当为 0 时，取 btKey 数组的大小 */
            byte[] btIV, /* 初始向量 */
            int iIVLen /* 初始向量的长度，当为 0 时，取 btIV 数组的大小 */
    ) throws BadPaddingException, IllegalBlockSizeException,
            InvalidAlgorithmParameterException, InvalidKeyException,
            NoSuchAlgorithmException, NoSuchPaddingException
    {
        return AES128_CBC_GLY.decode(HEX_GLY.decode(stHex), 0, btKey, iKeyLen,
                btIV, iIVLen);
    }

    private AES128_CBC_HEX_GLY()
    {
    }
}
