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
 * Char转换器
 *
 * @author wuyuhou
 *
 */
public class CharTypeConvertor extends AbstractTypeConvertor<Character> {

	@Override
	protected boolean doCanConvert(Object value, Class toClass) {
		return char.class == toClass || Character.class == toClass;
	}

	@Override
	protected Character doConvert(Object value, Class toClass, Character defaultValue, Character overrideValue) {
		if (value == null) {
			if (overrideValue != null) {
				return overrideValue;
			}
			if (defaultValue != null) {
				return defaultValue;
			}
			return Character.MIN_VALUE;
		}
		
		if (value instanceof Character) {
			return (Character) value;
		}
		
		if (value instanceof Integer){
			return (char)((Integer)value).intValue();
		}
		
		if (value instanceof Byte){
			return (char)((Byte)value).byteValue();
		}
		
		if (value instanceof byte[]) {
			byte[] bytes = (byte[])value;
			if (bytes.length == 2) {
				return(char)((bytes[0] << 8) + (bytes[1] << 0));
			}			
		}
		
		if (value instanceof Byte[]) {
			Byte[] bytes = (Byte[])value;
			if (bytes.length == 2) {
				return(char)((bytes[0] << 8) + (bytes[1] << 0));
			}
		}

		if (value instanceof String) {
			String s = ((String) value).trim();
			if (s.length() == 0) {
				return Character.MIN_VALUE;
			} else {
				return s.charAt(0);
			}
		}
		
		throw new IllegalArgumentException("Does not recognize the data:" + value);
	}
}