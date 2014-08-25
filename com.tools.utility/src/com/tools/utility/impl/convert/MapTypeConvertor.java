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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;


import com.tools.utility.api.ReflectUtil;
import com.tools.utility.api.SdoUtil;
import com.tools.utility.spi.convert.AbstractTypeConvertor;
import com.tools.utility.spi.convert.ConvertRuntimeException;


/**
 * Map类型转换器
 *
 * @author wuyuhou
 *
 */
public class MapTypeConvertor extends AbstractTypeConvertor<Map> {
	
	@Override
	protected boolean doCanConvert(Object value, Class toClass) {
		return Map.class.isAssignableFrom(toClass);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Map doConvert(final Object value, Class toClass, Map defaultValue, Map overrideValue) {
		if (value == null) {
			if (overrideValue != null) {
				return overrideValue;
			}
			if (defaultValue != null) {
				return defaultValue;
			}
			try {
				return ReflectUtil.newInstance(toClass);
			} catch (Throwable t) {
				return new HashMap();
			}
		}

		if (!value.getClass().isArray() && !(value instanceof Collection)) {
			Map retMap = null;
			if (retMap != null) {
				retMap = overrideValue;
			}
			if (retMap == null && defaultValue != null) {
				retMap = defaultValue;
			}
			if (retMap == null) {
				try {
					retMap = ReflectUtil.newInstance(toClass);
				} catch (Throwable t) {
					retMap = new HashMap();
				}
			}			
			
			//sdo->map
			if (SdoUtil.isSdoType(value.getClass())) {
				try {
					for (Object property : SdoUtil.getInstanceProperties(value)) {
						String propertyName = SdoUtil.getPropertyName(property);
						retMap.put(propertyName, SdoUtil.getPropertyValue(value, propertyName));
					}
				} catch (InvocationTargetException e) {
					throw new ConvertRuntimeException(e.getMessage(), e.getTargetException());
				} catch (Throwable t) {
					throw new ConvertRuntimeException(t.getMessage(), t);
				}				
			} else if (value instanceof Map) {
				retMap.putAll((Map)value);
			} else {
				//javaBean->Map
				Map<String, Field> fieldMap = ReflectUtil.getAllField(value.getClass());
				for (Entry<String, Field> entry : fieldMap.entrySet()) {
					final String name = entry.getKey();
					final Field field = entry.getValue();
					try {
						Object fieldValue = AccessController.doPrivileged(new PrivilegedExceptionAction() {
							public Object run() throws Exception {
								boolean access = field.isAccessible();
								try {
									field.setAccessible(true);
									return field.get(value);
								} finally {
									field.setAccessible(access);
								}
							}					
						});
						retMap.put(name, fieldValue);
					} catch (Throwable t) {
						throw new ConvertRuntimeException(t.getMessage(), t);
					}	
				}
			}
			return retMap;
		}
		throw new IllegalArgumentException("Does not recognize the data:" + value);
	}
}