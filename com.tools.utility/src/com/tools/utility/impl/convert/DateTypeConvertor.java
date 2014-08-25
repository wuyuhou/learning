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

import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.tools.utility.spi.convert.AbstractTypeConvertor;

/**
 * Date转换器
 *
 * @author wuyuhou
 *
 */
public class DateTypeConvertor extends AbstractTypeConvertor<Date> {
	
	private static final SimpleDateFormat[] supportDateFormats = new SimpleDateFormat[] {
		new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS"),
		new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S"),
		new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"),
		new SimpleDateFormat("yyyyMMddHHmmss"),
		new SimpleDateFormat("yyyyMMdd"),
		new SimpleDateFormat("yyyy-MM-dd") };

	@Override
	protected boolean doCanConvert(Object value, Class toClass) {
		return Date.class.isAssignableFrom(toClass);
	}

	@Override
	protected Date doConvert(Object value, Class toClass, Date defaultValue, Date overrideValue) {
		if (value == null) {
			if (overrideValue != null) {
				return overrideValue;
			}
			if (defaultValue != null) {
				return defaultValue;
			}
			return new Date();
		}
		
		if (value instanceof Date) {
			if (overrideValue != null) {
				overrideValue.setTime(((Date) value).getTime());
				return overrideValue;
			}
			return (Date) value;
		}
		
		if (value instanceof Long) {
			if (overrideValue != null) {
				overrideValue.setTime(((Long) value));
				return overrideValue;
			}
			return new Date(((Long) value));
		}
		
		if (value instanceof Time) {
			if (overrideValue != null) {
				overrideValue.setTime(((Time) value).getTime());
				return overrideValue;
			}
			return new Date(((Time) value).getTime());
		}
		
		if (value instanceof Timestamp) {
			if (overrideValue != null) {
				overrideValue.setTime(((Timestamp) value).getTime());
				return overrideValue;
			}
			return new Date(((Timestamp) value).getTime());
		}
		
		if (value instanceof Calendar) {
			if (overrideValue != null) {
				overrideValue.setTime(((Calendar) value).getTimeInMillis());
				return overrideValue;
			}
			return ((Calendar) value).getTime();
		}

		if (value instanceof String) {
			String s = ((String) value).trim();
			if (s.length() == 0) {
				if (overrideValue != null) {
					return overrideValue;
				}
				if (defaultValue != null) {
					return defaultValue;
				}
				return new Date();
			} else {
				Date result = parseDate((String) value, supportDateFormats);
				if (result != null) {
					if (overrideValue != null) {
						overrideValue.setTime(result.getTime());
						return overrideValue;
					}
					return result;
				}
			}
		}
		
		throw new IllegalArgumentException("Does not recognize the data:" + value);
	}
	
	private static Date parseDate(String dateString,
			SimpleDateFormat[] format_array) {
		for (int i = 0; i < format_array.length; ++i) {
			try {
				format_array[i].setLenient(false);
				return format_array[i].parse(dateString);
			} catch (Exception parseException) {
			}
		}

		return null;
	}
}