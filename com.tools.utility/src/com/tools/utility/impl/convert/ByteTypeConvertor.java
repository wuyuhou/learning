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
 * Byte转换器
 *
 * @author wuyuhou
 *
 */
public class ByteTypeConvertor extends AbstractTypeConvertor<Byte> {

	@Override
	protected boolean doCanConvert(Object value, Class toClass) {
		return Byte.class == toClass || byte.class == toClass;
	}

	@Override
	protected Byte doConvert(Object value, Class toClass, Byte defaultValue, Byte overrideValue) {
		if (value == null) {
			if (overrideValue != null) {
				return overrideValue;
			}
			if (defaultValue != null) {
				return defaultValue;
			}
			return 0;
		}
		
		if (value instanceof Byte) {
			return (Byte) value;
		}
		
		if (value instanceof byte[]) {
			byte[] bytes = (byte[])value;
			if (bytes.length == 1) {
				return bytes[0];
			}			
		}
		
		if (value instanceof Byte[]) {
			Byte[] bytes = (Byte[])value;
			if (bytes.length == 1) {
				return bytes[0];
			}
		}
		
		if (value instanceof Number) {
			return ((Number) value).byteValue();
		}

		if (value instanceof String) {
			String s = ((String) value).trim();
			if (s.length() == 0) {
				return 0;
			} else {
				return Byte.parseByte(s);
			}
		}
		
		throw new IllegalArgumentException("Does not recognize the data:" + value);
	}
}