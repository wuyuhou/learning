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

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;


import com.tools.utility.api.ReflectUtil;
import com.tools.utility.api.SdoUtil;
import com.tools.utility.spi.convert.AbstractTypeConvertor;
import com.tools.utility.spi.convert.ConvertRuntimeException;
import com.tools.utility.spi.convert.TypeConvertorManager;


/**
 * Sdo类型转换器
 *
 * @author wuyuhou
 *
 */
public class SdoTypeConvertor extends AbstractTypeConvertor<Object> {
	
	private static final TypeConvertorManager convertorManager = TypeConvertorManager.getInstance();
	
	@Override
	protected boolean doCanConvert(Object value, Class toClass) {
		return SdoUtil.isSdoType(toClass);
	}

	@Override
	protected Object doConvert(final Object value, Class toClass, Object defaultValue, Object overrideValue) {
		if (value == null) {
			if (overrideValue != null) {
				return overrideValue;
			}
			if (defaultValue != null) {
				return defaultValue;
			}
			try {
				return SdoUtil.create(toClass);
			} catch (Throwable t) {
				throw new IllegalArgumentException("Does not recognize the toClass:" + toClass, t);
			}
		}

		if (!value.getClass().isArray() 
				 && !(value instanceof Collection)
				 && !(value instanceof InputStream)
				 && !(value instanceof OutputStream)) {
			try {
				Object retValue = null;
				if (overrideValue != null) {
					retValue = overrideValue;
				}
				if (retValue == null && defaultValue != null) {
					retValue = defaultValue;
				}
				if (retValue == null) {
					retValue = SdoUtil.create(toClass);
				}
				
				if (SdoUtil.isSdoType(value.getClass())) {//sdo -> sdo
					try {
						for (Object property : SdoUtil.getInstanceProperties(value)) {
							String propertyName = SdoUtil.getPropertyName(property);
							Object propertyValue = SdoUtil.getPropertyValue(value, propertyName);
							SdoUtil.setPropertyValue(retValue, propertyName, propertyValue);
						}
					} catch (Throwable t) {
						throw new ConvertRuntimeException(t.getMessage(), t);
					}	
				} else if (value instanceof Map) {//Map -> sdo
					for (Object elem : ((Map)value).entrySet()) {
						Entry entry = (Entry)elem;
						String name = convertorManager.convert(entry.getKey(), String.class, null, null);
						try {
							SdoUtil.setPropertyValue(retValue, name, entry.getValue());
						} catch (Throwable t) {
							throw new ConvertRuntimeException(t.getMessage(), t);
						}	
					}
				} else {//javaBean->sdo
					Map<String, Field> fieldMap = ReflectUtil.getAllField(value.getClass());
					for (Entry<String, Field> entry : fieldMap.entrySet()) {
						final String name = entry.getKey();
						final Field field = entry.getValue();
						try {
							final Class propertyTypeClass = SdoUtil.getPropertyInstanceClass(retValue, name);
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
							SdoUtil.setPropertyValue(retValue, name, convertorManager.convert(fieldValue, propertyTypeClass, null, null));
						} catch (Throwable t) {
							throw new ConvertRuntimeException(t.getMessage(), t);
						}					
					}
				}
				return retValue;
			} catch (Throwable t) {
				throw new ConvertRuntimeException(t.getMessage(), t);
			}
		}
		throw new IllegalArgumentException("Does not recognize the data:" + value);
	}
}