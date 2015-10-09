package com.huawei.vtm.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class SHA256_BASE64 {
	/**
	 * 加密
	 * @param btData
	 *            数据
	 * @param iLen
	 *            数据的长度，当为 0 时，取 btData 数组的大小
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static String execute(byte[] btData, int iLen)
			throws NoSuchAlgorithmException {
		MessageDigest l_oSHA = null;
		byte[] l_btHash = null;

		if (btData == null) {
			return null;
		}

		if ((iLen <= 0) || (iLen > btData.length)) {
			iLen = btData.length;
		}

		l_oSHA = MessageDigest.getInstance("SHA-256");

		l_oSHA.update(btData, 0, iLen);

		l_btHash = l_oSHA.digest();

		l_oSHA = null;
		
		return BASE64.encode(l_btHash, l_btHash.length);
	}

	private SHA256_BASE64() {
	}
}
