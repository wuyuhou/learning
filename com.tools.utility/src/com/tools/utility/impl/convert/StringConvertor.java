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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


import com.tools.utility.api.IOUtil;
import com.tools.utility.api.ReflectUtil;
import com.tools.utility.api.SdoUtil;
import com.tools.utility.spi.convert.AbstractTypeConvertor;


/**
 * 字符串转换器
 *
 * @author wuyuhou
 *
 */
public class StringConvertor extends AbstractTypeConvertor<String> {
	
	@Override
	protected boolean doCanConvert(Object value, Class toClass) {
		return String.class == toClass;
	}
	
	protected String doConvert(final Object value, Class toClass, String defaultValue, String overrideValue) {
		return format(doConvertWithoutFormat(value, toClass, defaultValue, overrideValue));
	}

	@SuppressWarnings("unchecked")
	protected String doConvertWithoutFormat(final Object value, Class toClass, String defaultValue, String overrideValue) {
		if (value == null) {
			if (overrideValue != null) {
				return overrideValue;
			}
			if (defaultValue != null) {
				return defaultValue;
			}
			return "";
		}

		if (value instanceof byte[]) {
			return new String((byte[]) value);
		}
		
		if (value instanceof ByteArrayOutputStream) {
			return new String(((ByteArrayOutputStream)value).toByteArray());
		}
		
		if (value instanceof Throwable) {
			StringWriter strWriter = new StringWriter();
			((Throwable)value).printStackTrace(new PrintWriter(strWriter));
			return strWriter.toString();
		}
		
		if (value instanceof InputStream) {
			try {
				return new String(IOUtil.read((InputStream)value));
			} catch (Exception e) {
				return String.valueOf(value);
			}
		}
		
		if (value instanceof Date) {
			return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").format((Date) value);
		}
		
		if (value instanceof Number
				|| value instanceof OutputStream
				|| value.getClass().isPrimitive()
				|| ReflectUtil.primitiveWrapperTypeMap.get(value.getClass()) != null
				|| value instanceof String
				|| value instanceof BigInteger
				|| value instanceof BigDecimal) {
			return String.valueOf(value);
		} else if (value.getClass().isArray()) {//array
			int length = Array.getLength(value);
			StringBuilder buf = new StringBuilder();
			buf.append("<array class='").append(getClassName(value.getClass())).append("'");
			buf.append(" length='").append(length).append("'>");			
			for (int i = 0; i < length; i++) {
				Object item = Array.get(value, i);				
				buf.append("<item index='").append(i + 1).append("'");
				buf.append(" class='").append(item == null ? null : item.getClass().getName()).append("'>");
				String str = (item == value ? "(this array)" : doConvertWithoutFormat(item, String.class, null, null));
				buf.append(str);
				buf.append("</item>");
			}
			buf.append("</array>");
			return buf.toString();
		} else if (value instanceof Collection) {//collection
			StringBuilder buf = new StringBuilder();
			buf.append("<collection class='").append(value.getClass().getName()).append("'");
			buf.append(" length='").append(((Collection)value).size()).append("'>");
			int i = 1;
			for (Object item : (Collection)value) {
				buf.append("<item index='").append(i++).append("'");
				buf.append(" class='").append(getClassName(item.getClass())).append("'>");
				buf.append(item == value ? "(this collection)" : doConvertWithoutFormat(item, String.class, null, null));
				buf.append("</item>");
			}
			buf.append("</collection>");
			return buf.toString();
		} else if (value instanceof Map) { //map
			StringBuilder buf = new StringBuilder();
			buf.append("<map class='").append(value.getClass().getName()).append("'");
			buf.append(" length='").append(((Map)value).size()).append("'>");
			int i = 1;
			for (Object entry : ((Map)value).entrySet()) {
				Object entryKey = ((Entry)entry).getKey();
				Object entryValue = ((Entry)entry).getValue();
				buf.append("<entry index='").append(i++).append("'>");
				buf.append("<key");
				buf.append(" class='").append(getClassName(entryKey.getClass())).append("'>");
				buf.append(entryKey == value ? "(this map)" : doConvertWithoutFormat(entryKey, String.class, null, null));
				buf.append("</key>");
				buf.append("<value");
				buf.append(" class='").append(getClassName(entryValue.getClass())).append("'>");
				buf.append(entryValue == value ? "(this map)" : doConvertWithoutFormat(entryValue, String.class, null, null));
				buf.append("</value>");
				buf.append("</entry>");				
			}
			buf.append("</map>");			
			return buf.toString();
		} else if (SdoUtil.isSdoType(value.getClass())) {//sdo
			StringBuilder buf = new StringBuilder();
			buf.append("<sdo class='").append(value.getClass().getName()).append("'>");			
			try {
				List propertyList = SdoUtil.getInstanceProperties(value);
				for (Object property : propertyList) {
					String key = SdoUtil.getPropertyName(property);
					Object propertyValue = SdoUtil.getPropertyValue(value, key);
					buf.append("<property name='").append(key).append("'");
					buf.append(" class='").append(getClassName(propertyValue.getClass())).append("'>");
					if (propertyValue == value) {
						buf.append("(this sdo)");
					} else {
						buf.append(doConvertWithoutFormat(propertyValue, String.class, null, null));
					}
					buf.append("</property>");					
				}
			} catch (Throwable e) {
				buf.append(value);
			}
			buf.append("</sdo>");			
			return buf.toString();
		} else {//javabean
			StringBuilder buf = new StringBuilder();
			buf.append("<javabean class='").append(value.getClass().getName()).append("'>");
			try {
				Map<String, Field> fieldMap = ReflectUtil.getAllField(value.getClass());
				for (Entry<String, Field> entry : fieldMap.entrySet()) {
					String key = entry.getKey();
					final Field field = entry.getValue();
					Object fieldValue = AccessController.doPrivileged(new PrivilegedExceptionAction() {
						public Object run() throws Exception{
							boolean access = field.isAccessible();
							try {
								field.setAccessible(true);
								return field.get(value);
							} finally {
								field.setAccessible(access);
							}
						}					
					});
					buf.append("<property name='").append(key).append("'");
					buf.append(" class='").append(getClassName(fieldValue.getClass())).append("'>");
					if (fieldValue == value) {
						buf.append("(this javabean)");
					} else {
						buf.append(doConvertWithoutFormat(fieldValue, String.class, null, null));
					}
					buf.append("</property>");
				}
			} catch (Exception e) {
				buf.append(value);
			}
			buf.append("</javabean>");
			return buf.toString();
		}		
	}
	
	private String getClassName(Class clazz) {
		if (clazz == null) {
			return null;
		}
		if (clazz.isArray()) {
			return getClassName(clazz.getComponentType()) + "[]";
		} else {
			return clazz.getName();
		}
	}
	
	private String format(String str) {
		StringBuilder buf = new StringBuilder();
		boolean isInIngo1 = false;
		boolean isInIngo2 = false;
		int cycleCount = 0;
		StringBuilder token1 = new StringBuilder();
		boolean isToken1 = false;
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);			
			switch (c) {
			case ' ':
				isToken1 = false;
				break;
			case '\'':
				isToken1 = false;
				if (isInIngo2) {
					break;
				}	
				isInIngo1 = !isInIngo1;
				break;
			case '"':
				isToken1 = false;
				if (isInIngo1) {
					break;
				}
				isInIngo2 = !isInIngo2;
				break;
			case '<':
				if (isInIngo1 || isInIngo2) {
					break;
				}
				isToken1 = false;
				
				if (str.charAt(i + 1) == '/') {
					cycleCount--;
					StringBuilder token2 = new StringBuilder();
					for (int k = i + 2; k < str.length(); k++) {
						char ch = str.charAt(k);
						if (ch == '>') {
							break;
						}
						if (Character.isJavaIdentifierPart(ch)) {
							token2.append(ch);
						}
					}
					if (token2.toString().trim().equals(token1.toString().trim())) {
						token1 = new StringBuilder();
						break;
					}
					buf.append("\n");
					for (int j = 0; j < cycleCount; j++) {
						buf.append("    ");
					}
				} else {
					token1 = new StringBuilder();
					isToken1 = true;
					if (cycleCount > 0) {
						buf.append("\n");
					}
					
					for (int j = 0; j < cycleCount; j++) {
						buf.append("    ");
					}
					cycleCount++;
				}
				break;
			default:				
				break;
			}
			if (isToken1) {
				if (Character.isJavaIdentifierPart(c)) {
					token1.append(c);
				}				
			}
			
			buf.append(c);
		}
		return buf.toString();
	}
}