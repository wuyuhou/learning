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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * 系统属性助手类
 * 
 * @author wuyuhou
 */
public class SystemProperties {

	private static Properties properties = new Properties();

	/**
	 * 取得系统属性值.<br>
	 * 
	 * @param key key键
	 * @return 系统属性值
	 */
	public static String getProperty(String key) {
		if (key == null) {
			throw new IllegalArgumentException("key is null!");
		}
		String value = properties.getProperty(key);
		if (value == null) {
			value = System.getProperty(key);
		}
		return value;
	}

	/**
	 * 取得系统属性值.<br>
	 * 
	 * @param key key键
	 * @param defaultValue 默认值
	 * @return 系统属性值
	 */
	public static String getProperty(String key, String defaultValue) {
		String value = getProperty(key);
		if (value == null) {
			return defaultValue;
		}
		return value;
	}

	/**
	 * 取得系统属性值.<br>
	 * 
	 * @param key key键
	 * @param clazz 指定类型
	 * @return 系统属性值
	 */
	public static <V> V getProperty(String key, Class<V> clazz) {
		String value = getProperty(key, (String) null);
		return ReflectUtil.cast(value, clazz);
	}

	/**
	 * 取得系统属性值.<br>
	 * 
	 * @param key key键
	 * @param clazz 指定类型
	 * @return 系统属性值
	 */
	public static <V> V getProperty(String key, Class<V> clazz, V defaultValue) {
		String value = getProperty(key);
		if (value == null) {
			return defaultValue;
		}
		return ReflectUtil.cast(value, clazz);
	}

	/**
	 * 系统属性设置
	 * 
	 * @param key key键
	 * @param value value值
	 */
	public static void setProperty(String key, String value) {
		if (key == null) {
			throw new IllegalArgumentException("key is null!");
		}
		if (value == null) {
			throw new IllegalArgumentException("value is null!");
		}
		properties.setProperty(key, value);
	}

	/**
	 * 设置属性集
	 * 
	 * @param props 属性集
	 */
	public static void setProperties(Properties props) {
		if (props == null) {
			return;
		}
		properties.putAll(props);
	}
	
	/**
	 * 取得所有key
	 * 
	 * @return 所有key数组
	 */
	public static String[] getAllKeys() {
		HashSet<String> keys = new HashSet<String>();
		for (Object key : properties.keySet()) {
			keys.add((String)key);
		}
		for (Object key : System.getProperties().keySet()) {
			keys.add((String)key);
		}
		return keys.toArray(new String[0]);
	}

	/**
	 * 加载属性
	 * 
	 * @param inStream 输入流
	 * @throws IOException
	 */
	public static void load(InputStream inStream) throws IOException {
		if (inStream == null) {
			throw new IllegalArgumentException("inStream is null!");
		}
		properties.load(inStream);
	}

	/**
	 * 加载属性
	 * 
	 * @param file 属性文件
	 * @throws IOException
	 */
	public static void load(File file) throws IOException {
		if (file == null) {
			throw new IllegalArgumentException("file is null!");
		}
		if (!file.exists()) {
			throw new IllegalArgumentException("file '" + file.getAbsolutePath() + "' is not existed!");
		}
		if (file.isDirectory()) {
			throw new IllegalArgumentException("'" + file.getAbsolutePath() + "' is dir!");
		}
		FileInputStream in = null;
		try {
			in = new FileInputStream(file);
			load(in);
		} finally {
			IOUtil.closeQuietly(in);
		}
	}
	
	/**
	 * 打印属性
	 * 
	 * @param outSteam 输出对象
	 */
	public static void print(OutputStream outSteam) {
		PrintStream out = new PrintStream(outSteam);
		out.println("-- listing properties --");
		for (Entry e : properties.entrySet()) {
		    String key = (String)e.getKey();
		    String val = (String)e.getValue();
		    out.println(key + "=" + val);
		}
		out.println("-- listing system properties --");
		for (Entry e : System.getProperties().entrySet()) {
		    String key = (String)e.getKey();
		    String val = (String)e.getValue();
		    out.println(key + "=" + val);
		}
	}
}