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
package com.tools.utility.spi.crypto;

import java.security.SecureRandom;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/**
 * 加解密实现类
 *
 * @author wuyuhou
 *
 */
public class Cipher extends AbstractCryptor {
	
	public final static String AES_ALGORITHM = "AES";
	
	public final static String BLOWFISH_ALGORITHM = "Blowfish";
	
	public final static String DES_ALGORITHM = "DES";
	
	public final static String DESEDE_ALGORITHM = "DESede";
	
	private final static String DEFAULT_ALGORITHM = DES_ALGORITHM;
	
	private static ConcurrentHashMap<String, SecretKey> secretKeyMap = new ConcurrentHashMap<String, SecretKey>();
	
	//密钥
	private SecretKey secretKey = null;
	
	private boolean isNeedBase64Coder = true;

	public SecretKey getSecretKey() {
		return secretKey;
	}
	
	public void setSecretKey(SecretKey secretKey) {
		this.secretKey = secretKey;
	}
	
	public boolean isNeedBase64Coder() {
		return isNeedBase64Coder;
	}

	public void setNeedBase64Coder(boolean isBase64Coder) {
		this.isNeedBase64Coder = isBase64Coder;
	}

	//初始化
	private void init() throws Exception {
		if (getAlgorithm() == null || getAlgorithm().trim().length() == 0) {
			setAlgorithm(DEFAULT_ALGORITHM);
		}
		if (getSecretKey() == null) {	
			if (secretKeyMap.get(getAlgorithm()) == null) {
				synchronized(Cipher.class) {
					if (secretKeyMap.get(getAlgorithm()) == null) {
						KeyGenerator keygen = KeyGenerator.getInstance(getAlgorithm());
						keygen.init(new SecureRandom());
						secretKeyMap.put(getAlgorithm(), keygen.generateKey());
					}
				}
			}
			setSecretKey(secretKeyMap.get(getAlgorithm()));			
		}
	}
	
	/**
	 * 加密
	 * 
	 * @param dataBytes 需要加密的原始数据
	 * @return 加密后的数据
	 */
	public byte[] encrypt(byte[] dataBytes) {
		if (dataBytes == null || dataBytes.length == 0) {
			throw new IllegalArgumentException("dataBytes is null!");
		}
		try {
			init();
			javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance(getAlgorithm());
			cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, getSecretKey());
			byte[] result = cipher.doFinal(dataBytes);
			if (isNeedBase64Coder) {
				result = Base64Coder.encode(result);
			}
			return result;
		} catch (Throwable t) {
			throw new CryptoRuntimeException("encrypt error!", t);
		}		
	}
	
	/**
	 * 解密
	 * 
	 * @param dataBytes 需要解密的数据
	 * @return 原始数据
	 * 
	 */
	public byte[] decrypt(byte[] dataBytes) {
		if (dataBytes == null || dataBytes.length == 0) {
			throw new IllegalArgumentException("dataBytes is null!");
		}
		try {
			init();
			javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance(getAlgorithm());
			cipher.init(javax.crypto.Cipher.DECRYPT_MODE, getSecretKey());
			if (isNeedBase64Coder) {
				dataBytes = Base64Coder.decode(dataBytes);
			}
			return cipher.doFinal(dataBytes);
		} catch (Throwable t) {
			throw new CryptoRuntimeException("decrypt error!", t);
		}
	}
}