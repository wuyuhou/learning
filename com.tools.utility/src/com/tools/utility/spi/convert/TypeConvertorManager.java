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
package com.tools.utility.spi.convert;

import java.util.ArrayList;
import java.util.List;

import com.tools.utility.api.ServiceExtensionLoader;
import com.tools.utility.api.ServiceExtensionLoader.Extension;



/**
 * 类型转换器管理者
 *
 * @author wuyuhou
 *
 */
public class TypeConvertorManager {
	
	private static TypeConvertorManager INSTANCE = new TypeConvertorManager();
	
	private TypeConvertorManager() {
		ServiceExtensionLoader<ITypeConvertor> extensionLoader = ServiceExtensionLoader.load(ITypeConvertor.class);
		List<Extension<ITypeConvertor>> extensionList = extensionLoader.getExtensionObjects();
		for (Extension<ITypeConvertor> extension : extensionList) {
			register(extension.getExtension());
		}
	}
	
	public static TypeConvertorManager getInstance() {
		if (INSTANCE == null) {
			synchronized (TypeConvertorManager.class) {
				if (INSTANCE == null) {
					INSTANCE = new TypeConvertorManager();
				}
			}			
		}
		return INSTANCE;
	}
	
	private ArrayList<ITypeConvertor> convertorList = new ArrayList<ITypeConvertor>();
	
	public void register(ITypeConvertor convertor) {
		if (convertor == null) {
			throw new IllegalArgumentException("convertor is null!");
		}
		convertorList.add(convertor);
	}
	
	public void unregister(ITypeConvertor convertor) {
		if (convertor == null) {
			throw new IllegalArgumentException("convertor is null!");
		}
		convertorList.remove(convertor);
	}
	
	public ITypeConvertor[] getAllConvertors() {
		return convertorList.toArray(new ITypeConvertor[0]);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T convert(Object value, Class toClass, T defaultValue, T overrideValue) {
		for (ITypeConvertor convertor : getAllConvertors()) {
			if (convertor != null && convertor.canConvert(value, toClass)) {
				return (T)convertor.convert(value, toClass, defaultValue, overrideValue);
			}
		}
		if (value == null && toClass == null) {
			if (overrideValue != null) {
				return overrideValue;
			}
			if (defaultValue != null) {
				return defaultValue;
			}
			return (T)null;
		}
		if (toClass == null) {
			return (T)value;
		}
		throw new UnsupportedOperationException("The type of conversion is not supported:" + value + "," + toClass);
	}
}