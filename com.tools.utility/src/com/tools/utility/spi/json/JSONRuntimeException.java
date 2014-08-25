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
package com.tools.utility.spi.json;

import com.tools.exception.api.CommonRuntimeException;

/**
 * The JSONException is thrown by the JSON.org classes then things are amiss.
 * 
 * @author wuyuhou
 */
public class JSONRuntimeException extends CommonRuntimeException {
	
	private static final long serialVersionUID = 341214587119169384L;
	
	public JSONRuntimeException(String message) {
		super(message);
	}
	
	public JSONRuntimeException(String message, Object[] param) {
		super(message, param);
	}
	
	public JSONRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public JSONRuntimeException(Throwable cause) {
		super(cause);
	}
	
	public JSONRuntimeException(String message, Object[] param, Throwable cause) {
		super(message, param, cause);
	}
}