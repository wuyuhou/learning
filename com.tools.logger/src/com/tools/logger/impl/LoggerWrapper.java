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
package com.tools.logger.impl;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;


import com.tools.logger.api.ILogger;
import com.tools.logger.spi.AbstractLogger;


/**
 * 日志包裹（防止记录日志时抛出异常）
 *
 * @author wuyuhou
 */
public class LoggerWrapper extends AbstractLogger {

	private ILogger logger = null;

	@SuppressWarnings("unused")
	private String name = null;

	public LoggerWrapper(String name, ILogger logger) {
		this.name = name;
		if (logger == null) {
			return;
		}
		if (logger instanceof LoggerWrapper) {
			this.logger = ((LoggerWrapper)logger).logger;
		} else {
			this.logger = logger;
		}
	}

	public ILogger getLogger() {
		return logger;
	}

	public void setLogger(ILogger logger) {
		this.logger = logger;
	}

	public boolean isDebugEnabled() {
		if (logger == null) {
			return true;
		}
		try {
			return logger.isDebugEnabled();
		} catch (Throwable t) {
			return false;
		}
	}

	public boolean isInfoEnabled() {
		if (logger == null) {
			return true;
		}
		try {
			return logger.isInfoEnabled();
		} catch (Throwable t) {
			return false;
		}
	}

	public boolean isWarnEnabled() {
		if (logger == null) {
			return true;
		}
		try {
			return logger.isWarnEnabled();
		} catch (Throwable t) {
			return false;
		}
	}

	public boolean isErrorEnabled() {
		if (logger == null) {
			return true;
		}
		try {
			return logger.isErrorEnabled();
		} catch (Throwable t) {
			return false;
		}
	}

	protected void doDebug(String message, Throwable t) {
		try {
			logger.debug(message, t);
		} catch (Throwable e) {
			try {
				System.out.println(convert("DEBUG", message, t));
			} catch (Throwable ignore) {

			}
		}
	}

	protected void doInfo(String message, Throwable t) {

		try {
			logger.info(message, t);
		} catch (Throwable e) {
			try {
				System.out.println(convert(" INFO", message, t));
			} catch (Throwable ignore) {

			}
		}
	}

	protected void doWarn(String message, Throwable t) {
		try {
			logger.warn(message, t);
		} catch (Throwable e) {
			try {
				System.err.println(convert(" WARN", message, t));
			} catch (Throwable ignore) {

			}
		}
	}

	protected void doError(String message, Throwable t) {
		try {
			logger.error(message, t);
		} catch (Throwable e) {
			try {
				System.err.println(convert("ERROR", message, t));
			} catch (Throwable ignore) {

			}
		}
	}

	private String convert(String level, Object message, Throwable t) {
		StringBuilder buf = new StringBuilder();
		buf.append("[").append(format(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss")).append("]");
		buf.append("[").append(level).append("]");
//		if (name != null && name.trim().length() > 0) {
//			buf.append("[").append(name).append("]");
//		}
		buf.append("[").append(message).append("]");

		if (t != null) {
			String error_msg = t.getMessage();
			if (error_msg != null) {
				buf.append("[").append(error_msg).append("]");
			}

			buf.append("\n");
			StringWriter sw = new StringWriter();
			t.printStackTrace(new PrintWriter(sw));
			buf.append(sw.toString());
		}
		return buf.toString();
	}

	private static String format(long time, String pattern) {
		SimpleDateFormat dateFormat = new SimpleDateFormat();
		dateFormat.applyPattern(pattern);
		return dateFormat.format(new Date(time));
	}
}