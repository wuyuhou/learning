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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 服务配置扩展点加载器<br>
 *
 * 支持下面格式的配置文件：<br>注意：默认以index排序（升序），或者priority排序（降序），如果没有这个字段，默认是100
 * com.primeton.XXXXListener;index=100;keyX=valueX;
 * com.primeton.XXXXListener;index=101;keyX=valueX;
 *
 * @author wuyuhou
 *
 */
public class ServiceExtensionLoader<X> {

	private static final String PREFIX1 = "META-INF/";
	
	private static final String PREFIX2 = "META-INF/services/";

	private String encoding = "UTF-8";

	private List<Extension<X>> extensionList = new ArrayList<Extension<X>>();
	
	private List<Throwable> errorList = new ArrayList<Throwable>();

	private ClassLoader classLoader = ServiceExtensionLoader.class.getClassLoader();

	public ClassLoader getClassLoader() {
		return classLoader;
	}

	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	@SuppressWarnings("unchecked")
	public ServiceExtensionLoader<X> load(InputStream in) {
		if (in == null) {
			throw new IllegalArgumentException("InputStream is null!");
		}
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(in, encoding));
			String line = null;
			while((line = reader.readLine()) != null) {
				try {
					//去掉注释
					int indexComment = line.indexOf("#");
					if (indexComment != -1) {
						line = line.substring(0, indexComment);
					}
					indexComment = line.indexOf("--");
					if (indexComment != -1) {
						line = line.substring(0, indexComment);
					}
					indexComment = line.indexOf("//");
					if (indexComment != -1) {
						line = line.substring(0, indexComment);
					}
					line = line.trim();
					if (line.length() == 0) {
						continue;
					}
					String[] eles = line.split(";");

					if (eles.length > 0) {
						Extension<X> extension = new Extension<X>();
						extension.setOriginalValue(eles[0]);
						try {
							Class<X> clazz = ReflectUtil.loadClass(getClassLoader(), eles[0]);
							extension.setType(clazz);
							String notNeedInstance = extension.getProperty("notNeedInstance", "false");
							if ("false".equalsIgnoreCase(notNeedInstance)) {
								extension.setExtension((X)ReflectUtil.newInstance(clazz));
							}	
						} catch (Throwable t) {
							errorList.add(new Throwable("Load class [" + eles[0] + "] error!", t));
						}

						for (int i = 1; i < eles.length; i++) {
							int index = eles[i].indexOf("=");
							if (index >= 0) {
								String key = eles[i].substring(0, index).trim();
								String value = eles[i].substring(index + 1).trim();
								if (key.equals("index")) {
									extension.setIndex(Integer.parseInt(value));
								} else if (key.equals("priority")) {
									extension.setIndex(0 - Integer.parseInt(value));
								} else {
									extension.setProperty(key, value);
								}
							}
						}					
						addListByIndex(extension);
					}
				} catch (Throwable e) {
					errorList.add(new Throwable("Parser [" + line + "] error!", e));
				}
			}
		} catch (Throwable e) {
			errorList.add(e);
		}
		return this;
	}

	private void addListByIndex(Extension<X> extension) {
		if (extensionList.size() == 0) {
			extensionList.add(extension);
			return;
		}
		int i = extensionList.size() - 1;
		for (; i >= 0; i--) {
			if (extensionList.get(i).getIndex() <= extension.getIndex()) {
				break;
			}
		}
		extensionList.add(++i, extension);
	}
	
	public static <X> ServiceExtensionLoader<X> load(Class<X> clazz) {
		ServiceExtensionLoader<X> loader = new ServiceExtensionLoader<X>();
		loader.classLoader = clazz.getClassLoader();
		loader.load(PREFIX1 + clazz.getName());
		return loader.load(PREFIX2 + clazz.getName());
	}

	public ServiceExtensionLoader<X> load(String resource) {
		for (URL url : IOUtil.getAllResources(null, resource)) {
			load(url);
		}
		return this;
	}

	public ServiceExtensionLoader<X> load(URL url) {
		if (url == null) {
			throw new IllegalArgumentException("url is null!");
		}
		InputStream in = null;
		try {
			in = url.openStream();
			load(in);
		} catch (Throwable e) {
			errorList.add(new Throwable("load url[" + url + "] error!", e));
		} finally {
			IOUtil.closeQuietly(in);
		}
		return this;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public List<X> getExtensions() {
		List<X> returnList = new ArrayList<X>();
		for (Extension<X> extension : extensionList) {
			returnList.add(extension.getExtension());
		}
		return returnList;
	}

	public List<Extension<X>> getExtensionObjects() {
		List<Extension<X>> returnList = new ArrayList<Extension<X>>();
		for (Extension<X> extension : extensionList) {
			returnList.add(extension);
		}
		return returnList;
	}
	
	public List<Throwable> getErrorList() {
		return errorList;
	}

	public Iterator<X> iterator() {
		return getExtensions().iterator();
	}

	public static class Extension<X> {

		private X extension = null;
		
		private Class<X> type = null;

		private int index = 100;

		private Map<String, String> extProperty = new HashMap<String, String>();
		
		private String originalValue = null;

		public String getOriginalValue() {
			return originalValue;
		}

		public void setOriginalValue(String originalValue) {
			this.originalValue = originalValue;
		}

		public X getExtension() {
			return extension;
		}

		public void setExtension(X extension) {
			this.extension = extension;
		}

		public Class<X> getType() {
			return type;
		}

		public void setType(Class<X> type) {
			this.type = type;
		}

		public int getIndex() {
			return index;
		}

		public void setIndex(int priority) {
			this.index = priority;
		}

		public void setProperty(String key, String value) {
			extProperty.put(key, value);
		}

		public String getProperty(String key) {
			return getProperty(key, (String)null);
		}

		public <T> T getProperty(String key, Class<T> toClass) {
			return ReflectUtil.cast(getProperty(key), toClass);
		}

		public String getProperty(String key, String defaultValue) {
			String value = extProperty.get(key);
			if (value == null) {
				return defaultValue;
			} else {
				return value;
			}
		}

		@Override
		public String toString() {
			StringBuffer buf = new StringBuffer();
			buf.append("{index=").append(index).append(", ").append(extension.getClass().getName()).append("}");
			return buf.toString();
		}

	}
}