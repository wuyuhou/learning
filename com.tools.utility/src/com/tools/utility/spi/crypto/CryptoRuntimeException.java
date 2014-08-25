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

import com.tools.exception.api.CommonRuntimeException;

/**
 * 加解密过程中出错
 *
 * @author wuyuhou
 *
 */
public class CryptoRuntimeException extends CommonRuntimeException {

	private static final long serialVersionUID = -2168590357442517526L;
	
	public CryptoRuntimeException(String message) {
		super(message);
	}
	
	public CryptoRuntimeException(String message, Object[] param) {
		super(message, param);
	}
	
	public CryptoRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public CryptoRuntimeException(String message, Object[] param, Throwable cause) {
		super(message, param, cause);
	}
}