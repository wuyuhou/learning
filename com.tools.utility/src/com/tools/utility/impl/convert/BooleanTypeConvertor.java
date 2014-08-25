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
package com.tools.utility.impl.convert;

import com.tools.utility.spi.convert.AbstractTypeConvertor;



/**
 * 布尔类型转换器
 *
 * @author wuyuhou
 *
 */
public class BooleanTypeConvertor extends AbstractTypeConvertor<Boolean> {

	@Override
	protected boolean doCanConvert(Object value, Class toClass) {
		return boolean.class == toClass || Boolean.class == toClass;
	}

	@Override
	protected Boolean doConvert(Object value, Class toClass, Boolean defaultValue, Boolean overrideValue) {
		if (value == null) {
			if (overrideValue != null) {
				return overrideValue;
			}
			if (defaultValue != null) {
				return defaultValue;
			}
			return false;
		}
		
		if (value instanceof Boolean) {
			return ((Boolean) value).booleanValue();
		}
		
		if (value instanceof Number) {
			return ((Number) value).intValue() == 0 ? false : true;
		}
		
		if (value instanceof byte[]) {
			byte[] bytes = (byte[])value;
			if (bytes.length == 1) {
				return bytes[0] == 0 ? false : true;
			}			
		}
		
		if (value instanceof Byte[]) {
			Byte[] bytes = (Byte[])value;
			if (bytes.length == 1) {
				return bytes[0] == 0 ? false : true;
			}			
		}

		if (value instanceof String) {
			String s = ((String) value).trim().toLowerCase();
			if (s.equals("true") || s.equals("1")) {
				return true;
			}				
			if (s.equals("false") || s.equals("0")) {
				return false;
			}
			if (s.length() == 0) {
				if (defaultValue != null) {
					return defaultValue;
				}
				return false;
			}
		}
		
		throw new IllegalArgumentException("Does not recognize the data:" + value);
	}
}