package com.huawei.vtm.utils;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public final class AES128_CBC_GLY
{
    private static final int AES_128_KEY_LEN = 16; // 128 bit

    private static final int AES_IV_LEN = 16;

    public static byte[] encode(byte[] btPlain, /* 明文 */
            int iLen, /* 明文的长度，当为 0 时，取 btPlain 数组的大小 */
            byte[] btKey, /* 密钥 */
            int iKeyLen, /* 密钥的长度，当为 0 时，取 btKey 数组的大小 */
            byte[] btIV, /* 初始向量 */
            int iIVLen /* 初始向量的长度，当为 0 时，取 btIV 数组的大小 */
    ) throws BadPaddingException, IllegalBlockSizeException,
            InvalidAlgorithmParameterException, InvalidKeyException,
            NoSuchAlgorithmException, NoSuchPaddingException
    {
        return encode_decode(btPlain, /* 明文 */
                iLen, /* 明文的长度，当为 0 时，取 btPlain 数组的大小 */
                btKey, /* 密钥 */
                iKeyLen, /* 密钥的长度，当为 0 时，取 btKey 数组的大小 */
                btIV, /* 初始向量 */
                iIVLen, /* 初始向量的长度，当为 0 时，取 btIV 数组的大小 */
                0);
    }

    public static byte[] decode(byte[] btCipher, /* 密文 */
            int iLen, /* 密文的长度，当为 0 时，取 btCipher 数组的大小 */
            byte[] btKey, /* 密钥 */
            int iKeyLen, /* 密钥的长度，当为 0 时，取 btKey 数组的大小 */
            byte[] btIV, /* 初始向量 */
            int iIVLen /* 初始向量的长度，当为 0 时，取 btIV 数组的大小 */
    ) throws BadPaddingException, IllegalBlockSizeException,
            InvalidAlgorithmParameterException, InvalidKeyException,
            NoSuchAlgorithmException, NoSuchPaddingException
    {
        return encode_decode(btCipher, /* 密文 */
                iLen, /* 密文的长度，当为 0 时，取 btCipher 数组的大小 */
                btKey, /* 密钥 */
                iKeyLen, /* 密钥的长度，当为 0 时，取 btKey 数组的大小 */
                btIV, /* 初始向量 */
                iIVLen, /* 初始向量的长度，当为 0 时，取 btIV 数组的大小 */
                1);
    }

    private static byte[] encode_decode(byte[] btData, /* 数据 */
            int iLen, /* 数据的长度，当为 0 时，取 btCipher 数组的大小 */
            byte[] btKey, /* 密钥 */
            int iKeyLen, /* 密钥的长度，当为 0 时，取 btKey 数组的大小 */
            byte[] btIV, /* 初始向量 */
            int iIVLen, /* 初始向量的长度，当为 0 时，取 btIV 数组的大小 */
            int iFlag) throws BadPaddingException, IllegalBlockSizeException,
            InvalidAlgorithmParameterException, InvalidKeyException,
            NoSuchAlgorithmException, NoSuchPaddingException
    {
        int ii;
        int l_iMode;
        byte[] l_btKey = null;
        byte[] l_btIV = null;
        Cipher l_oCipher = null;

        if ((btData == null) || (btData.length == 0) || (btKey == null))
        {
            return new byte[0];
        }

        if ((iLen <= 0) || (iLen > btData.length))
        {
            iLen = btData.length;
        }

        if ((iKeyLen <= 0) || (iKeyLen > btKey.length))
        {
            iKeyLen = btKey.length;
        }

        if (iKeyLen > AES_128_KEY_LEN) // 16 Bytes
        {
            iKeyLen = AES_128_KEY_LEN; // 16 Bytes
        }

        l_btKey = new byte[AES_128_KEY_LEN]; // 16 Bytes

        for (ii = 0; ii < AES_128_KEY_LEN; ii++)
        {
            l_btKey[ii] = (byte) 0x00;
        }

        for (ii = 0; ii < iKeyLen; ii++)
        {
            l_btKey[ii] = btKey[ii];
        }

        if (((iIVLen <= 0) || (iIVLen > btIV.length)) && (btIV != null))
        {
            iIVLen = btIV.length;
        }

        l_btIV = new byte[AES_IV_LEN]; // 16 Bytes

        for (ii = 0; ii < AES_IV_LEN; ii++)
        {
            l_btIV[ii] = (byte) 0x00;
        }

        for (ii = 0; ii < ((iIVLen < AES_IV_LEN) ? iIVLen : AES_IV_LEN); ii++)
        {
            l_btIV[ii] = btIV[ii];
        }

        l_oCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        if (iFlag == 0)
        {
            l_iMode = Cipher.ENCRYPT_MODE;
        }
        else
        {
            l_iMode = Cipher.DECRYPT_MODE;
        }

        if (btIV == null)
        {
            l_oCipher.init(l_iMode, new SecretKeySpec(l_btKey, 0,
                    AES_128_KEY_LEN, "AES"));
        }
        else
        {
            l_oCipher.init(l_iMode, new SecretKeySpec(l_btKey, 0,
                    AES_128_KEY_LEN, "AES"), new IvParameterSpec(l_btIV, 0,
                    AES_IV_LEN));
        }

        l_btKey = null;
        l_btIV = null;

        return l_oCipher.doFinal(btData, 0, iLen);
    }

    private AES128_CBC_GLY()
    {
    }
}
