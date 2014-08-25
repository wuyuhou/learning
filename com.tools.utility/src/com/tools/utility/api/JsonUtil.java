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
package com.tools.utility.api;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.tools.utility.spi.format.DateFormatter;
import com.tools.utility.spi.json.JSONArray;
import com.tools.utility.spi.json.JSONObject;
import com.tools.utility.spi.json.JSONTokener;


/**
 * Json工具包
 * 
 * @author wuyuhou
 * 
 */
public class JsonUtil {
	
	/**
	 * 取得指定Java对象
	 * 
	 * @param jsonString json字符串
	 * @param toClass 指定对象的Java类型，不能为空
	 * @param fieldTypeMap 指定对象字段的Java类型(一般是接口类型的属性字段)，可以为空，以xpath方式设置属性名称，比如：/data/user/name
	 * @return
	 */
	public static <T> T toJavaObject(String jsonString, Class<T> toClass, Map<String, Class> fieldTypeMap) {
		if (toClass == null) {
			throw new IllegalArgumentException("toClass is null!");
		}
		if (jsonString == null) {
			return ReflectUtil.cast(null, toClass);
		}
		
		/*
		 * json对象<pr>
		 * 
		 * 如果jsonString是以"{"开始，则返回JSONObject对象；<br/>
		 * 如果jsonString是以"["或者"("开始，则返回JSONArray对象；<br/>
		 * 上述之外，则为简单类型的字符串，直接返回相应值（Boolean, Double, Integer, Long, String；特别注意，如果是null或者空字符串，则返回JSONObject.NULL对象）；<br/>
		 * 
		 */
		Object json = new JSONTokener(jsonString).nextValue();
		
		return (T)doToJavaObject(json, toClass, fieldTypeMap, "");
	}
	
	@SuppressWarnings("unchecked")
	private static <T> T doToJavaObject(Object json, Class<T> toClass, Map<String, Class> fieldTypeMap, String prefix) {
		if (toClass == null || toClass.equals(Object.class)) {
			return (T)json;
		}
		if (json instanceof JSONObject) {
			if (toClass.isArray()) {
				throw new IllegalArgumentException("toClass'" + toClass.getName() + "' is array!");
			} else {
				Object returnObj = ReflectUtil.cast(null, toClass);	
				for (String name : JSONObject.getNames((JSONObject)json)) {
					try {
						Object sub = ((JSONObject)json).get(name);
						if (SdoUtil.isSdoType(toClass)) {
							sub = doToJavaObject(sub, SdoUtil.getPropertyInstanceClass(sub, name), fieldTypeMap, prefix + "/" + name);
						} else if (!Map.class.isAssignableFrom(toClass)) {
							Field field = ReflectUtil.getField(toClass, name, false);
							Class subToClass = field.getType();
							if (fieldTypeMap != null) {
								Class fieldClass = fieldTypeMap.get(prefix + "/" + name);
								if (fieldClass != null) {
									if (subToClass.isAssignableFrom(fieldClass)) {
										subToClass = fieldClass;
									}
								}
							}
							
							sub = doToJavaObject(sub, field.getType(), fieldTypeMap, prefix + "/" + name);
						}
						ReflectUtil.setValue(returnObj, name, sub);
					} catch (Throwable e) {
						throw new IllegalArgumentException("toClass'" + toClass.getName() + "' is not right class,  json is '" + json + "'!", e);
					}
				}
				return (T)returnObj;
			}
		} else if (json instanceof JSONArray) {
			if (toClass.isArray() || Collection.class.isAssignableFrom(toClass)) {
				JSONArray jsonArray = (JSONArray)json;
				Object returnObj = null;
				if (toClass.isArray()) {
					returnObj = Array.newInstance(toClass.getComponentType(), jsonArray.length());
				} else {
					returnObj = ReflectUtil.cast(null, toClass);
				}
				for (int i = 0; i < jsonArray.length(); i++) {
					if (toClass.isArray()) {
						Array.set(returnObj, i, doToJavaObject(jsonArray.get(i), toClass.getComponentType(), fieldTypeMap, prefix));
					} else {
						((Collection)returnObj).add(jsonArray.get(i));
					}
				}
				return (T)returnObj;
			} else {
				throw new IllegalArgumentException("toClass'" + toClass.getName() + "' is not array, but json'" + json + "' is array!");
			}
		} else if (json == JSONObject.NULL) {
			return ReflectUtil.cast(null, toClass);
		} else {
			return ReflectUtil.cast(json, toClass);
		}
	}
	
	private static DateFormatter DEFAULT_DATE_FORMATER = new DateFormatter().setPattern(DateFormatter.YYYYMMDDHHMMSS2);
	
	/**
	 * 把对象转化为Json字符串
	 * 
	 * @param obj
	 * @return
	 */
	public static String toJsonString(Object obj) {
		StringBuilder json = new StringBuilder();
		if (obj == null) {
			json.append("\"\"");
		} else if (obj instanceof Character || Character.TYPE.equals(obj.getClass())) {
			json.append("'").append(string2json(String.valueOf(obj))).append("'");
		} else if (obj instanceof String) {
			json.append("\"").append(string2json((String) obj)).append("\"");		
		} else if (obj instanceof Date) {// 默认时间格式
			json.append("\"").append(DEFAULT_DATE_FORMATER.format(obj)).append("\"");
		} else if (obj.getClass().isPrimitive() || Number.class.isAssignableFrom(obj.getClass()) || obj instanceof Boolean) {
			json.append(obj);
		} else if (obj.getClass().isArray()) {
			json.append(array2json((Object[]) obj));
		} else if (obj instanceof Collection) {
			json.append(array2json(((Collection<?>) obj).toArray()));
		} else if (obj instanceof Map) {
			json.append(map2json((Map<?, ?>) obj));
		} else if (SdoUtil.isSdoType(obj.getClass())) {//sdo
			json.append(sdo2json(obj));
		} else {
			json.append(bean2json(obj));
		}
		return json.toString();
	}

	// 把数组转化为JSON字符串
	private static String array2json(Object[] array) {
		StringBuilder json = new StringBuilder();
		json.append("[");
		int count = 0;
		for (Object obj : array) {
			if (count > 0) {
				json.append(",");
			}			
			json.append(toJsonString(obj));
			count++;
		}
		json.append("]");
		return json.toString();
	}

	// 把Map转化为JSON字符串
	private static String map2json(Map<?, ?> map) {
		StringBuilder json = new StringBuilder();
		json.append("{");
		int count = 0;
		for (Object key : map.keySet()) {
			if (count > 0) {
				json.append(",");
			}
			json.append(toJsonString(key));
			json.append(":");
			json.append(toJsonString(map.get(key)));
			count++;
		}
		json.append("}");
		
		return json.toString();
	}

	// 把sdo转化为JSON字符串
	private static String sdo2json(Object sdo) {
		StringBuilder json = new StringBuilder();
		json.append("{");
		int count = 0;
		try {
			List propertyList = SdoUtil.getInstanceProperties(sdo);
			for (Object property : propertyList) {
				String key = SdoUtil.getPropertyName(property);
				Object propertyValue = SdoUtil.getPropertyValue(sdo, key);
				if (count > 0) {
					json.append(",");
				}
				json.append(toJsonString(key));
				json.append(":");
				json.append(toJsonString(propertyValue));
				count++;
			}
		} catch (Throwable e) {				
		}
		json.append("}");
		
		return json.toString();
	}

	// 把JavaBean转化为JSON字符串
	private static String bean2json(final Object bean) {
		StringBuilder json = new StringBuilder();
		json.append("{");

		int count = 0;
		Map<String, Field> fieldMap = ReflectUtil.getAllField(bean.getClass());
		for (Entry<String, Field> entry : fieldMap.entrySet()) {
			String key = entry.getKey();
			final Field field = entry.getValue();
			Object fieldValue = null;
			try {
				fieldValue = AccessController.doPrivileged(new PrivilegedExceptionAction() {
					public Object run() throws Exception {
						boolean access = field.isAccessible();
						try {
							field.setAccessible(true);
							return field.get(bean);
						} finally {
							field.setAccessible(access);
						}
					}					
				});
			} catch (PrivilegedActionException e) {				
			}
			if (count > 0) {
				json.append(",");
			}
			json.append(toJsonString(key));
			json.append(":");
			json.append(toJsonString(fieldValue));
			count++;
		}
		json.append("}");
		return json.toString();
	}

	// 把字符串转化为JSON字符串
	private static String string2json(String s) {
		if (s == null)
			return "";
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			char ch = s.charAt(i);
			switch (ch) {
			case '"':
				sb.append("\\\"");
				break;
			case '\\':
				sb.append("\\\\");
				break;
			case '\b':
				sb.append("\\b");
				break;
			case '\f':
				sb.append("\\f");
				break;
			case '\n':
				sb.append("\\n");
				break;
			case '\r':
				sb.append("\\r");
				break;
			case '\t':
				sb.append("\\t");
				break;
			case '/':
				sb.append("\\/");
				break;
			default:
				if (ch >= '\u0000' && ch <= '\u001F') {
					String ss = Integer.toHexString(ch);
					sb.append("\\u");
					for (int k = 0; k < 4 - ss.length(); k++) {
						sb.append('0');
					}
					sb.append(ss.toUpperCase());
				} else {
					sb.append(ch);
				}
			}
		}
		return sb.toString();
	}
	
	/**
	 * json字符串的格式化
	 * 
	 * @param json
	 * @param fillStringUnit
	 * @return
	 */
	public static String formatJson(String json, String fillStringUnit) {
		if (json == null || json.trim().length() == 0) {
			return null;
		}
		
		int fixedLenth = 0;
		ArrayList<String> tokenList = new ArrayList<String>();
		{
			String jsonTemp = json;
			//预读取
			while (jsonTemp.length() > 0) {
				String token = getToken(jsonTemp);
				jsonTemp = jsonTemp.substring(token.length());
				token = token.trim();
				tokenList.add(token);
			}			
		}
		
		for (int i = 0; i < tokenList.size(); i++) {
			String token = tokenList.get(i);
			int length = token.getBytes().length;
			if (length > fixedLenth && i < tokenList.size() - 1 && tokenList.get(i + 1).equals(":")) {
				fixedLenth = length;
			}
		}
		
		StringBuilder buf = new StringBuilder();
		int count = 0;
		for (int i = 0; i < tokenList.size(); i++) {
			
			String token = tokenList.get(i);
			
			if (token.equals(",")) {
				buf.append(token);
				doFill(buf, count, fillStringUnit);
				continue;
			}
			if (token.equals(":")) {
				buf.append(" ").append(token).append(" ");
				continue;
			}
			if (token.equals("{")) {
				String nextToken = tokenList.get(i + 1);
				if (nextToken.equals("}")) {
					i++;
					buf.append("{ }");
				} else {
					count++;
					buf.append(token);
					doFill(buf, count, fillStringUnit);
				}
				continue;
			}
			if (token.equals("}")) {
				count--;
				doFill(buf, count, fillStringUnit);
				buf.append(token);
				continue;
			}
			if (token.equals("[")) {
				String nextToken = tokenList.get(i + 1);
				if (nextToken.equals("]")) {
					i++;
					buf.append("[ ]");
				} else {
					count++;
					buf.append(token);
					doFill(buf, count, fillStringUnit);
				}
				continue;
			}
			if (token.equals("]")) {
				count--;
				doFill(buf, count, fillStringUnit);
				buf.append(token);
				continue;
			}
			
			buf.append(token);
			//左对齐
			if (i < tokenList.size() - 1 && tokenList.get(i + 1).equals(":")) {
				int fillLength = fixedLenth - token.getBytes().length;
				if (fillLength > 0) {
					for(int j = 0; j < fillLength; j++) {
						buf.append(" ");
					}
				}
			}
		}
		return buf.toString();
	}
	
	private static String getToken(String json) {
		StringBuilder buf = new StringBuilder();
		boolean isInYinHao = false;
		while (json.length() > 0) {
			String token = json.substring(0, 1);
			json = json.substring(1);
			
			if (!isInYinHao && 
					(token.equals(":") || token.equals("{") || token.equals("}") 
							|| token.equals("[") || token.equals("]")
							|| token.equals(","))) {
				if (buf.toString().trim().length() == 0) {					
					buf.append(token);
				}
				
				break;
			}

			if (token.equals("\\")) {
				buf.append(token);
				buf.append(json.substring(0, 1));
				json = json.substring(1);
				continue;
			}
			if (token.equals("\"")) {
				buf.append(token);
				if (isInYinHao) {
					break;
				} else {
					isInYinHao = true;
					continue;
				}				
			}
			buf.append(token);
		}
		return buf.toString();
	}

	private static void doFill(StringBuilder buf, int count, String fillStringUnit) {
		buf.append("\n");
		for (int i = 0; i < count; i++) {
			buf.append(fillStringUnit);
		}
	}
}