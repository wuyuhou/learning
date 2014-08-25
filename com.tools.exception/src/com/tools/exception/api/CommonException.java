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
package com.tools.exception.api;

import java.text.MessageFormat;

/**
 * 公共�?查异常类
 *
 * @author wuyuhou
 *
 */
public abstract class CommonException extends Exception {

	private static final long serialVersionUID = -6708888492895881522L;

	/**
	 * 构�?�方�?.<br>
	 * @param message 异常信息
	 */
	public CommonException(String message) {
		super(message);
	}
	
	/**
	 * 构�?�方�?.<br>
	 * @param message 异常的格式化信息
	 * @param param 异常格式化信息的参数
	 */
	public CommonException(String message, Object[] param) {
		super(format(message, param));
	}
	

	/**
	 * 构�?�方�?.<br>
	 * @param cause 异常原因
	 */
	public CommonException(Throwable cause) {
		super(cause);
	}
	
	/**
	 * 构�?�方�?.<br>
	 * @param message 异常信息
	 * @param cause 异常原因
	 */
	public CommonException(String message, Throwable cause) {
		super(message, cause);
	}
	
	/**
	 * 构�?�方�?.<br>
	 * @param message 异常的格式化信息
	 * @param param  异常格式化信息的参数
	 * @param cause 异常原因
	 */
	public CommonException(String message, Object[] param, Throwable cause) {
		super(format(message, param), cause);
	}
	
	static String format(String message, Object[] params) {
		if (message.trim().length() > 0) {
			if (params != null && params.length > 0) {
				try {
					return new MessageFormat(message).format(params);
				} catch (Throwable t) {
					return message;
				}
			}
		}
		return message;
	}
}