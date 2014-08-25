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
 * JavaBean转换器
 *
 * @author wuyuhou
 *
 */
public class ObjectTypeConvertor extends AbstractTypeConvertor<Object> {
	
	private static final TypeConvertorManager convertorManager = TypeConvertorManager.getInstance();

	@Override
	protected boolean doCanConvert(Object value, Class toClass) {
		return !(InputStream.class.isAssignableFrom(toClass) || OutputStream.class.isAssignableFrom(toClass));
	}
	
	@SuppressWarnings("unchecked")
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
				return ReflectUtil.newInstance(toClass);
			} catch (Throwable t) {
				throw new IllegalArgumentException("Does not recognize the toClass:" + toClass, t);
			}
		}
		
		 if (!value.getClass().isArray() 
				 && !(value instanceof Collection)
				 && !(value instanceof InputStream)
				 && !(value instanceof OutputStream)) {
			try {
				Object newretValue = null;
				if (overrideValue != null) {
					newretValue = overrideValue;
				}
				if (newretValue == null && defaultValue != null) {
					newretValue = defaultValue;
				}
				if (newretValue == null) {
					newretValue = ReflectUtil.newInstance(toClass);
				}
				final Object retValue = newretValue;
				Map<String, Field> retfieldMap = ReflectUtil.getAllField(toClass);
				
				if (SdoUtil.isSdoType(value.getClass())) {//sdo -> JavaBean
					for (Entry<String, Field> entry : retfieldMap.entrySet()) {
						String name = entry.getKey();
						final Field field = entry.getValue();
						try {
							final Object propertyValue = SdoUtil.getPropertyValue(value, name);
							AccessController.doPrivileged(new PrivilegedExceptionAction() {
								public Object run() throws Exception {
									boolean access = field.isAccessible();
									try {
										field.setAccessible(true);
										field.set(retValue, convertorManager.convert(propertyValue, field.getType(), null, null));
										return null;
									} finally {
										field.setAccessible(access);
									}
								}					
							});
						} catch (Throwable t) {
							throw new ConvertRuntimeException(t.getMessage(), t);
						}				
					}
				} else if (value instanceof Map) {//Map -> Javabean
					for (Object elem : ((Map)value).entrySet()) {
						final Entry entry = (Entry)elem;
						String name = convertorManager.convert(entry.getKey(), String.class, null, null);
						final Field retfield = retfieldMap.get(name);
						if (retfield != null) {
							try {
								AccessController.doPrivileged(new PrivilegedExceptionAction() {
									public Object run() throws Exception {
										boolean access = retfield.isAccessible();
										try {
											retfield.setAccessible(true);
											retfield.set(retValue, convertorManager.convert(entry.getValue(), retfield.getType(), null, null));
											return null;
										} finally {
											retfield.setAccessible(access);
										}
									}					
								});
							} catch (Throwable t) {
								throw new ConvertRuntimeException(t.getMessage(), t);
							}	
						}	
					}
				} else {//javaBean->JavaBean
					Map<String, Field> fieldMap = ReflectUtil.getAllField(value.getClass());
					for (Entry<String, Field> entry : retfieldMap.entrySet()) {
						String name = entry.getKey();
						final Field retfield = entry.getValue();			
						final Field field = fieldMap.get(name);
						if (field != null) {
							try {
								AccessController.doPrivileged(new PrivilegedExceptionAction() {
									public Object run() throws Exception {
										boolean retaccess = retfield.isAccessible();
										boolean access = field.isAccessible();
										try {
											retfield.setAccessible(true);
											field.setAccessible(true);
											retfield.set(retValue, convertorManager.convert(field.get(value), retfield.getType(), null, null));
											return null;
										} finally {
											retfield.setAccessible(retaccess);
											field.setAccessible(access);
										}
									}					
								});
							} catch (Throwable t) {
								throw new ConvertRuntimeException(t.getMessage(), t);
							}	
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