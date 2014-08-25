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
 * BigDecimal转换器
 *
 * @author wuyuhou
 *
 */
public class BigDecimalTypeConvertor extends AbstractTypeConvertor<BigDecimal> {

	@Override
	protected boolean doCanConvert(Object value, Class toClass) {
		return BigDecimal.class.isAssignableFrom(toClass);
	}

	@Override
	protected BigDecimal doConvert(Object value, Class toClass, BigDecimal defaultValue, BigDecimal overrideValue) {
		if (value == null) {
			if (overrideValue != null) {
				return overrideValue;
			}
			if (defaultValue != null) {
				return defaultValue;
			}
			return BigDecimal.ZERO;
		}
		
		if (value instanceof BigDecimal) {
			return (BigDecimal) value;
		}

		if (value instanceof BigInteger) {
			return new BigDecimal((BigInteger) value);
		}

		if (value instanceof Number) {
			if (value instanceof Long) {
				return new BigDecimal(((Long) value).longValue());
			}
			return new BigDecimal(((Number) value).doubleValue());
		}

		if (value instanceof String) {
			String s = ((String) value).trim();
			if (s.length() == 0) {
				return BigDecimal.ZERO;
			} else {
				return new BigDecimal(s);
			}
		}
		
		throw new IllegalArgumentException("Does not recognize the data:" + value);
	}
}