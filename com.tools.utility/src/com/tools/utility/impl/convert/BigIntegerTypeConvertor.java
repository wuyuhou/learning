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

import java.math.BigDecimal;
import java.math.BigInteger;

import com.tools.utility.spi.convert.AbstractTypeConvertor;

/**
 * BigInteger转换器
 *
 * @author wuyuhou
 *
 */
public class BigIntegerTypeConvertor extends AbstractTypeConvertor<BigInteger> {

	@Override
	protected boolean doCanConvert(Object value, Class toClass) {
		return BigInteger.class.isAssignableFrom(toClass);
	}

	@Override
	protected BigInteger doConvert(Object value, Class toClass, BigInteger defaultValue, BigInteger overrideValue) {
		if (value == null) {
			if (overrideValue != null) {
				return overrideValue;
			}
			if (defaultValue != null) {
				return defaultValue;
			}
			return BigInteger.ZERO;
		}
		
		if (value instanceof BigInteger) {
			return (BigInteger) value;
		}

		if (value instanceof BigDecimal) {
			return ((BigDecimal) value).toBigInteger();
		}

		if (value instanceof Number) {
			return BigInteger.valueOf(((Number) value).longValue());
		}
		
		if (value instanceof byte[]) {
			return new BigInteger((byte[]) value);
		}

		if (value instanceof String) {
			String s = ((String) value).trim();
			if (s.length() == 0) {
				return BigInteger.ZERO;
			} else {
				return new BigInteger(s);
			}
		}
		
		throw new IllegalArgumentException("Does not recognize the data:" + value);
	}
}