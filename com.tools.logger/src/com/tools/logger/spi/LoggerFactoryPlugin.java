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
package com.tools.logger.spi;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;


import com.tools.logger.api.ILogger;
import com.tools.logger.impl.LoggerWrapper;


/**
 * 日志工厂实现
 *
 * @author wuyuhou
 *
 */
public class LoggerFactoryPlugin {
	
	private ILoggerProvider _provider = null;
	
	private ConcurrentHashMap<String, LoggerWrapper> logMap = new ConcurrentHashMap<String, LoggerWrapper>();
	
	private Object lock = new Object();

	public LoggerFactoryPlugin() {
		
	}
	
	public void setLoggerProvider(ILoggerProvider provider) {
		this._provider = provider;
	}
	
	public void refresh() {
		for (Entry<String, LoggerWrapper> entry : logMap.entrySet()) {
			entry.getValue().setLogger(doGetLogger(entry.getKey()).getLogger());
		}
	}
	
	public void destroy() {
		logMap.clear();
	}
	
	/**
	 * 取得日志记录器
	 *
	 * @param clazz
	 * @return 日志记录器
	 */
	public ILogger getLogger(Class clazz) {
		return getLogger(clazz == null ? "null" : clazz.getName());
	}

	/**
	 * 取得日志记录器
	 *
	 * @param loggerName 日志名称
	 * @return 日志记录器
	 */
	public ILogger getLogger(String loggerName) {
		if (loggerName == null) {
			loggerName = "null";
		}
		LoggerWrapper log = logMap.get(loggerName);
		if (log == null) {
			synchronized (lock) {
				log = logMap.get(loggerName);
				if (log == null) {
					log = doGetLogger(loggerName);
					logMap.put(loggerName, log);
				}
			}
		}
			
		return log;
	}
	
	private LoggerWrapper doGetLogger(String loggerName) {
		
		ILogger log = _provider != null ? _provider.createLogger(loggerName) : null;
		if (!(log instanceof LoggerWrapper)) {
			log = new LoggerWrapper(loggerName, log);
		}
		return (LoggerWrapper)log;
	}
}