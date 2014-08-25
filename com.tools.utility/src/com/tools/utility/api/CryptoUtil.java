/*
 * Copyright 2013 Primeton.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tools.utility.api;

import javax.crypto.SecretKey;

import com.tools.utility.spi.crypto.Cipher;
import com.tools.utility.spi.crypto.Digestor;
import com.tools.utility.spi.crypto.Signaturer;

/**
 * 加解密工具类
 *
 * @author wuyuhou
 *
 */
public class CryptoUtil {
	
	/**
	 * 加密
	 * 
	 * @param dataString 需要加密的原始数据
	 * @param algorithm 加密算法，可以为空，默认为DES算法
	 * @param secretKey 加密密钥，可以为空
	 * @return 加密后的数据
	 */
	public static String encrypt(String dataString, String algorithm, SecretKey secretKey) {
		if (dataString == null) {
			throw new IllegalArgumentException("Encrypt dataString is null!");
		}
		Cipher cipher = new Cipher();
		cipher.setAlgorithm(algorithm);
		cipher.setSecretKey(secretKey);
		return new String(cipher.encrypt(dataString.getBytes()));
	}
	
	/**
	 * 解密
	 * 
	 * @param dataString 需要解密的数据
	 * @param algorithm 加密算法，可以为空，默认为DES算法
	 * @param secretKey 加密密钥，可以为空
	 * @return 解密后的数据
	 */
	public static String decrypt(String dataString, String algorithm, SecretKey secretKey) {
		if (dataString == null) {
			throw new IllegalArgumentException("Decrypt dataString is null!");
		}
		Cipher cipher = new Cipher();
		cipher.setAlgorithm(algorithm);
		cipher.setSecretKey(secretKey);
		return new String(cipher.decrypt(dataString.getBytes()));
	}
	
	/**
	 * 生成摘要
	 * 
	 * @param dataString 需要生成摘要的原始数据
	 * @param algorithm 摘要算法，可以为空，默认为MD5算法
	 * @return 摘要数据
	 */
	public static String digest(String dataString, String algorithm) {
		if (dataString == null) {
			throw new IllegalArgumentException("Digest dataString is null!");
		}
		Digestor digestor = new Digestor();
		digestor.setAlgorithm(algorithm);
		return new String(digestor.digest(dataString.getBytes()));
	}
	
	/**
	 * 验证摘要
	 * 
	 * @param dataString 生成摘要的原始数据
	 * @param digestedString 生成的摘要数据
	 * @param algorithm 摘要算法，可以为空，默认为MD5算法
	 * @return true：验证通过
	 */
	public static boolean verifyDigest(String dataString, String digestedString, String algorithm) {
		if (dataString == null) {
			throw new IllegalArgumentException("Very Digest dataString is null!");
		}
		if (digestedString == null) {
			throw new IllegalArgumentException("Very Digest digestedString is null!");
		}
		Digestor digestor = new Digestor();
		digestor.setAlgorithm(algorithm);
		return digestor.verify(dataString.getBytes(), digestedString.getBytes());
	}
	
	/**
	 * 生成数字签名
	 * 
	 * @param dataString 需要生成数字签名的原始数据
	 * @param algorithm 数字签名算法，可以为空，默认为DSA算法
	 * @return 数字签名
	 */
	public static String sign(String dataString, String algorithm) {
		if (dataString == null) {
			throw new IllegalArgumentException("Signaturer dataString is null!");
		}
		Signaturer signaturer = new Signaturer();
		signaturer.setAlgorithm(algorithm);
		return new String(signaturer.sign(dataString.getBytes()));
	}
	
	/**
	 * 验证数字签名
	 * 
	 * @param dataString 生成数字签名的原始数据
	 * @param signedString 生成的数字签名数据
	 * @param algorithm 数字签名算法，可以为空，默认为DSA算法
	 * @return true：验证通过
	 */
	public static boolean verifySign(String dataString, String signedString, String algorithm) {
		if (dataString == null) {
			throw new IllegalArgumentException("Signaturer dataString is null!");
		}
		if (signedString == null) {
			throw new IllegalArgumentException("Signaturer signedString is null!");
		}
		Signaturer signaturer = new Signaturer();
		signaturer.setAlgorithm(algorithm);
		return signaturer.verify(dataString.getBytes(), signedString.getBytes());
	}
}