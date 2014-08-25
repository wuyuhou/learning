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
package com.tools.logger.api;

import com.tools.logger.spi.ILoggerProvider;
import com.tools.logger.spi.LoggerFactoryPlugin;


/**
 * 鏃ュ織宸ュ巶<br><br>
 * 
 * <b>浣跨敤绀轰緥:</b>
 * <pre>
 * ILogger log = LoggerFactory.getLogger(ClassA.class);
 * if (log.isErrorEnabled()) {
 *     log.error("....error!");
 * }
 * </pre>
 *
 * @author wuyuhou
 */
public class LoggerFactory {

	private static LoggerFactoryPlugin logFactoryPlugin = new LoggerFactoryPlugin();
	
	public static void setLoggerProvider(ILoggerProvider provider) {
		synchronized (LoggerFactory.class) {
			logFactoryPlugin.setLoggerProvider(provider);
		}
	}
	/**
	 * 鍙栧緱鏃ュ織璁板綍鍣�
	 *
	 * @param clazz
	 * @return 鏃ュ織璁板綍鍣�
	 */
	public static ILogger getLogger(Class clazz) {
		return logFactoryPlugin.getLogger(clazz);
	}

	/**
	 * 鍙栧緱鏃ュ織璁板綍鍣�
	 *
	 * @param loggerName 鏃ュ織鍚嶇О
	 * @return 鏃ュ織璁板綍鍣�
	 */
	public static ILogger getLogger(String loggerName) {
		return logFactoryPlugin.getLogger(loggerName);
	}
	
	/**
	 * 鍒锋柊
	 *
	 */
	public static void refresh() {
		logFactoryPlugin.refresh();
	}
	
	/**
	 * 閿�姣�
	 *
	 */
	public static void destroy() {
		logFactoryPlugin.destroy();
	}
}