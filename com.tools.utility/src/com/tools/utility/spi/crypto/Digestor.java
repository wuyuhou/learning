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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 消息摘要实现类
 *
 * @author wuyuhou
 *
 */
public class Digestor extends AbstractCryptor {	
	
	public final static String MD5_ALGORITHM = "MD5";
	
	public final static String SHA_ALGORITHM = "SHA";
	
	public final static String SHA1_ALGORITHM = "SHA-1";
	
	private final static String DEFAULT_ALGORITHM = MD5_ALGORITHM;
	
	private boolean isNeedBase64Coder = true;

	public boolean isNeedBase64Coder() {
		return isNeedBase64Coder;
	}

	public void setNeedBase64Coder(boolean isBase64Coder) {
		this.isNeedBase64Coder = isBase64Coder;
	}

	//初始化
	private void init() throws NoSuchAlgorithmException {
		if (getAlgorithm() == null || getAlgorithm().trim().length() == 0) {
			setAlgorithm(DEFAULT_ALGORITHM);
		}		
	}
	
	/**
	 * 生成消息摘要
	 * 
	 * @param dataBytes 需要生成摘要的原始数据
	 * @return 消息摘要数据
	 */
	public byte[] digest(byte[] dataBytes) {
		if (dataBytes == null || dataBytes.length == 0) {
			throw new IllegalArgumentException("dataBytes is null!");
		}
		try {
			byte[] result = doDigest(dataBytes);	
			if (isNeedBase64Coder) {
				result = Base64Coder.encode(result);
			}
			return result;
		} catch (Throwable t) {
			throw new CryptoRuntimeException("digest error!", t);
		}		
	}
	
	private byte[] doDigest(byte[] dataBytes) throws Throwable {
		init();
		MessageDigest md = MessageDigest.getInstance(getAlgorithm());
		md.update(dataBytes);
		return md.digest();	
	}
	
	/**
	 * 验证消息摘要值
	 * 
	 * @param dataBytes 需要生成摘要的原始数据
	 * @param digestedBytes 需要验证的摘要数据
	 * @return true：是相同的详细摘要
	 */
	public boolean verify(byte[] dataBytes, byte[] digestedBytes) {
		if (dataBytes == null || dataBytes.length == 0) {
			throw new IllegalArgumentException("dataBytes is null!");
		}
		if (digestedBytes == null || digestedBytes.length == 0) {
			throw new IllegalArgumentException("digestedBytes is null!");
		}
		try {
			if (isNeedBase64Coder) {
				digestedBytes = Base64Coder.decode(digestedBytes);
			}
			byte[] newDegest = doDigest(dataBytes);	
			return MessageDigest.isEqual(newDegest, digestedBytes);
		} catch (Throwable t) {
			throw new CryptoRuntimeException("verify digest error!", t);
		}		
	}
}