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
package com.tools.utility.api.config;

import com.tools.exception.api.CommonRuntimeException;

/**
 * Configuration运行期异常
 *
 * @author wuyuhou 
 */

public class ConfigurationRuntimeException extends CommonRuntimeException {
	
	private static final long serialVersionUID = 4635225964480211291L;
	
	public ConfigurationRuntimeException(String message) {
		super(message);
	}
	
	public ConfigurationRuntimeException(String message, Object[] param) {
		super(message, param);
	}
	
	public ConfigurationRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public ConfigurationRuntimeException(String message, Object[] param, Throwable cause) {
		super(message, param, cause);
	}	
}