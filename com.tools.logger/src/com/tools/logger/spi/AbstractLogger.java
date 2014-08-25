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

import java.text.MessageFormat;

import com.tools.logger.api.ILogger;


/**
 * 日志抽象实现
 *
 * @author wuyuhou
 */
public abstract class AbstractLogger implements ILogger {

	public void debug(Object message) {
		if (message != null && message instanceof Throwable) {
			debug(null, null, (Throwable)message);
			return;
		}
		debug(message, null, null);
	}

	public void debug(Object message, Object[] params) {
		debug(message, params, null);
	}

	public void debug(Throwable t) {
		debug(null, null, t);
	}

	public void debug(Object message, Throwable t) {
		debug(message, null, t);
	}

	public void debug(Object message, Object[] params, Throwable t) {
		if (isDebugEnabled()) {
			doDebug(formatMessage(message, params), t);
		}
	}

	abstract protected void doDebug(String message, Throwable t);

	public void info(Object message) {
		if (message != null && message instanceof Throwable) {
			info(null, null, (Throwable)message);
			return;
		}
		info(message, null, null);
	}

	public void info(Object message, Object[] params) {
		info(message, params, null);
	}

	public void info(Throwable t) {
		info(null, null, t);
	}

	public void info(Object message, Throwable t) {
		info(message, null, t);
	}

	public void info(Object message, Object[] params, Throwable t) {
		if (isInfoEnabled()) {
			doInfo(formatMessage(message, params), t);
		}
	}

	abstract protected void doInfo(String message, Throwable t);

	public void warn(Object message) {
		if (message != null && message instanceof Throwable) {
			warn(null, null, (Throwable)message);
			return;
		}
		warn(message, null, null);
	}

	public void warn(Object message, Object[] params) {
		warn(message, params, null);
	}

	public void warn(Throwable t) {
		warn(null, null, t);
	}

	public void warn(Object message, Throwable t) {
		warn(message, null, t);
	}

	public void warn(Object message, Object[] params, Throwable t) {
		if (isWarnEnabled()) {
			doWarn(formatMessage(message, params), t);
		}
	}

	abstract protected void doWarn(String message, Throwable t);

	public void error(Object message) {
		if (message != null && message instanceof Throwable) {
			error(null, null, (Throwable)message);
			return;
		}
		error(message, null, null);
	}

	public void error(Object message, Object[] params) {
		error(message, params, null);
	}

	public void error(Throwable t) {
		error(null, null, t);
	}

	public void error(Object message, Throwable t) {
		error(message, null, t);
	}

	public void error(Object message, Object[] params, Throwable t) {
		if (isErrorEnabled()) {
			doError(formatMessage(message, params), t);
		}
	}

	abstract protected void doError(String message, Throwable t);
	
	protected String formatMessage(Object message, Object[] params) {
		StringBuilder buf = new StringBuilder();		
		if (message != null) {
			if (message instanceof String) {
				String msg = (String) message;
				if (msg.trim().length() > 0) {
					if (params != null && params.length > 0) {
						message = new MessageFormat(msg).format(params);
					}
				}
			}
			buf.append(message);
		}
		return buf.toString();
	}
}