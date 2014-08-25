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
 * Double转换器
 *
 * @author wuyuhou
 *
 */
public class DoubleTypeConvertor extends AbstractTypeConvertor<Double> {

	@Override
	protected boolean doCanConvert(Object value, Class toClass) {
		return double.class == toClass || Double.class == toClass;
	}

	@Override
	protected Double doConvert(Object value, Class toClass, Double defaultValue, Double overrideValue) {
		if (value == null) {
			if (overrideValue != null) {
				return overrideValue;
			}
			if (defaultValue != null) {
				return defaultValue;
			}
			return 0d;
		}
		
		if (value instanceof Number) {
			return ((Number) value).doubleValue();
		}
		
		if (value instanceof byte[]) {
			byte[] bytes = (byte[])value;
			if (bytes.length == 8) {
				return Double.longBitsToDouble((((long)bytes[0] << 56) +
		                ((long)(bytes[1] & 255) << 48) +
		                ((long)(bytes[2] & 255) << 40) +
		                ((long)(bytes[3] & 255) << 32) +
		                ((long)(bytes[4] & 255) << 24) +
		                ((bytes[5] & 255) << 16) +
		                ((bytes[6] & 255) <<  8) +
		                ((bytes[7] & 255) <<  0)));
			}			
		}
		
		if (value instanceof Byte[]) {
			Byte[] bytes = (Byte[])value;
			if (bytes.length == 8) {
				return Double.longBitsToDouble((((long)bytes[0] << 56) +
		                ((long)(bytes[1] & 255) << 48) +
		                ((long)(bytes[2] & 255) << 40) +
		                ((long)(bytes[3] & 255) << 32) +
		                ((long)(bytes[4] & 255) << 24) +
		                ((bytes[5] & 255) << 16) +
		                ((bytes[6] & 255) <<  8) +
		                ((bytes[7] & 255) <<  0)));
			}
		}

		if (value instanceof String) {
			String s = ((String) value).trim();
			if (s.length() == 0) {
				return 0d;
			} else {
				return Double.parseDouble(s);
			}
		}
		
		throw new IllegalArgumentException("Does not recognize the data:" + value);
	}
}