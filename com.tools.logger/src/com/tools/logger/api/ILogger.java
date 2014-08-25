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

/**
 * 日志记录器接口
 *
 * @author wuyuhou
 */
public interface ILogger {
	
	/**
	 * 是否可记录debug级别的日志
	 * 
	 * @return true：可以记录
	 */
	boolean isDebugEnabled();
	
	/**
	 * 是否可记录info级别的日志
	 * 
	 * @return true：可以记录
	 */
	boolean isInfoEnabled();
	
	/**
	 * 是否可记录warn级别的日志
	 * 
	 * @return true：可以记录
	 */
	boolean isWarnEnabled();

	/**
	 * 是否可记录error级别的日志
	 * 
	 * @return true：可以记录
	 */
	boolean isErrorEnabled();


	/**
	 * 记录debug级别的日志
	 * 
	 * @param message 日志信息
	 */
	void debug(Object message);
	
	/**
	 * 记录debug级别的日志
	 * 
	 * @param message 日志信息
	 * @param params 参数信息，支持格式化参数能力
	 */
	void debug(Object message, Object[] params);
	
	/**
	 * 记录debug级别的日志
	 * 
	 * @param t 异常实例
	 */
	void debug(Throwable t);

	/**
	 * 记录debug级别的日志
	 * 
	 * @param message 日志信息
	 * @param t 异常实例
	 */
	void debug(Object message, Throwable t);

	/**
	 * 记录debug级别的日志
	 * 
	 * @param message 日志信息
	 * @param params 参数信息，支持格式化参数能力
	 * @param t 异常实例
	 */
	void debug(Object message, Object[] params, Throwable t);


	/**
	 * 记录info级别的日志
	 * 
	 * @param message 日志信息
	 */
	void info(Object message);

	/**
	 * 记录info级别的日志
	 * 
	 * @param message 日志信息
	 * @param params 参数信息，支持格式化参数能力
	 */
	void info(Object message, Object[] params);

	/**
	 * 记录info级别的日志
	 * 
	 * @param t 异常实例
	 */
	void info(Throwable t);

	/**
	 * 记录info级别的日志
	 * 
	 * @param message 日志信息
	 * @param t 异常实例
	 */
	void info(Object message, Throwable t);

	/**
	 * 记录info级别的日志
	 * 
	 * @param message 日志信息
	 * @param params 参数信息，支持格式化参数能力
	 * @param t 异常实例
	 */
	void info(Object message, Object[] params, Throwable t);


	/**
	 * 记录warn级别的日志
	 * 
	 * @param message 日志信息
	 */
	void warn(Object message);

	/**
	 * 记录warn级别的日志
	 * 
	 * @param message 日志信息
	 * @param params 参数信息，支持格式化参数能力
	 */
	void warn(Object message, Object[] params);

	/**
	 * 记录warn级别的日志
	 * 
	 * @param t 异常实例
	 */
	void warn(Throwable t);

	/**
	 * 记录warn级别的日志
	 * 
	 * @param message 日志信息
	 * @param t 异常实例
	 */
	void warn(Object message, Throwable t);

	/**
	 * 记录warn级别的日志
	 * 
	 * @param message 日志信息
	 * @param params 参数信息，支持格式化参数能力
	 * @param t 异常实例
	 */
	void warn(Object message, Object[] params, Throwable t);


	/**
	 * 记录error级别的日志
	 * 
	 * @param message 日志信息
	 */
	void error(Object message);

	/**
	 * 记录error级别的日志
	 * 
	 * @param message 日志信息
	 * @param params 参数信息，支持格式化参数能力
	 */
	void error(Object message, Object[] params);

	/**
	 * 记录error级别的日志
	 * 
	 * @param t 异常实例
	 */
	void error(Throwable t);

	/**
	 * 记录error级别的日志
	 * 
	 * @param message 日志信息
	 * @param t 异常实例
	 */
	void error(Object message, Throwable t);

	/**
	 * 记录error级别的日志
	 * 
	 * @param message 日志信息
	 * @param params 参数信息，支持格式化参数能力
	 * @param t 异常实例
	 */
	void error(Object message, Object[] params, Throwable t);
}