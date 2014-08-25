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

import java.lang.reflect.Array;
import java.util.Collection;

import com.tools.utility.spi.convert.AbstractTypeConvertor;
import com.tools.utility.spi.convert.TypeConvertorManager;

/**
 * 数组类型转换器
 *
 * @author wuyuhou
 *
 */
public class ArrayTypeConvertor extends AbstractTypeConvertor<Object> {
	
	private static TypeConvertorManager convertorManager = TypeConvertorManager.getInstance();

	@Override
	protected boolean doCanConvert(Object value, Class toClass) {
		return toClass.isArray();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Object doConvert(Object value, Class toClass, Object defaultValue, Object overrideValue) {
		Class compnentType = toClass.getComponentType();
		if (value == null) {
			if (overrideValue != null) {
				return overrideValue;
			}
			if (defaultValue != null) {
				return defaultValue;
			}
			return Array.newInstance(compnentType, 0);
		}
		
		Object retArray = null;
		if (overrideValue != null) {
			retArray = overrideValue;
		}
		if (retArray == null && defaultValue != null) {
			retArray = defaultValue;
		}
		if (value.getClass().isArray()) {
			int length = Array.getLength(value);
			if (retArray == null) {
				retArray = Array.newInstance(compnentType, length);
			}			
			int retLength = Array.getLength(retArray);
			for (int i = 0; i < length && i < retLength; i++) {
				Object elemValue = convertorManager.convert(Array.get(value, i), compnentType, null, null);
				Array.set(retArray, i, elemValue);
			}
		} else if (value instanceof Collection) {
			Object[] valueArray = ((Collection)value).toArray();
			int length = valueArray.length;
			if (retArray == null) {
				retArray = Array.newInstance(compnentType, length);
			}			
			int retLength = Array.getLength(retArray);						
			for (int i = 0; i < length && i < retLength; i++) {
				Object elemValue = convertorManager.convert(valueArray[i], compnentType, null, null);
				Array.set(retArray, i, elemValue);
			}
		} else {
			if (retArray == null) {
				retArray = Array.newInstance(compnentType, 1);
			}
			int retLength = Array.getLength(retArray);
			if (retLength > 0) {
				Object elemValue = convertorManager.convert(value, compnentType, null, null);
				Array.set(retArray, 0, elemValue);
			}			
		}
		return retArray;
	}
}