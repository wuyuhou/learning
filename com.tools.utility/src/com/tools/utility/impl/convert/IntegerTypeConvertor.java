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
 * Integer转换器
 *
 * @author wuyuhou
 *
 */
public class IntegerTypeConvertor extends AbstractTypeConvertor<Integer> {

	@Override
	protected boolean doCanConvert(Object value, Class toClass) {
		return int.class == toClass || Integer.class == toClass;
	}

	@Override
	protected Integer doConvert(Object value, Class toClass, Integer defaultValue, Integer overrideValue) {
		if (value == null) {
			if (overrideValue != null) {
				return overrideValue;
			}
			if (defaultValue != null) {
				return defaultValue;
			}
			return 0;
		}
		
		if (value instanceof Number) {
			return ((Number) value).intValue();
		}

		if (value instanceof Character) {
			return (int)((Character) value).charValue();
		}
		
		if (value instanceof byte[]) {
			byte[] bytes = (byte[])value;
			if (bytes.length == 4) {
				return(int)((bytes[0] << 24) + (bytes[1] << 16) + (bytes[2] << 8) + (bytes[3] << 0));
			}			
		}
		
		if (value instanceof Byte[]) {
			Byte[] bytes = (Byte[])value;
			if (bytes.length == 4) {
				return(int)((bytes[0] << 24) + (bytes[1] << 16) + (bytes[2] << 8) + (bytes[3] << 0));
			}
		}

		if (value instanceof String) {
			String s = ((String) value).trim();
			if (s.length() == 0) {
				return 0;
			} else {
				return Integer.parseInt(s);
			}
		}
		
		throw new IllegalArgumentException("Does not recognize the data:" + value);
	}
}