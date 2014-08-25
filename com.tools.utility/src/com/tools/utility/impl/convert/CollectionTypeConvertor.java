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

import java.util.ArrayList;
import java.util.Collection;


import com.tools.utility.api.ReflectUtil;
import com.tools.utility.spi.convert.AbstractTypeConvertor;


/**
 * Collection类型转换器
 *
 * @author wuyuhou
 *
 */
public class CollectionTypeConvertor extends AbstractTypeConvertor<Collection> {
	
	@Override
	protected boolean doCanConvert(Object value, Class toClass) {
		return Collection.class.isAssignableFrom(toClass);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Collection doConvert(Object value, Class toClass, Collection defaultValue, Collection overrideValue) {
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
				return new ArrayList();
			}			
		}
		
		Collection retCollection = null;
		if (overrideValue != null) {
			retCollection = overrideValue;
		}
		if (retCollection == null && defaultValue != null) {
			retCollection = defaultValue;
		}
		if (retCollection == null) {
			try {
				retCollection = ReflectUtil.newInstance(toClass);
			} catch (Throwable t) {
				retCollection = new ArrayList();
			}
		}
		
		if (value.getClass().isArray()) {
			Object[] valueArray = (Object[])value;
			for (int i = 0; i < valueArray.length; i++) {
				retCollection.remove(valueArray[i]);
				retCollection.add(valueArray[i]);
			}
		} else if (value instanceof Collection) {
			Object[] valueArray = ((Collection)value).toArray();
			for (int i = 0; i < valueArray.length; i++) {
				retCollection.remove(valueArray[i]);
				retCollection.add(valueArray[i]);
			}
		} else {
			retCollection.remove(value);
			retCollection.add(value);
		}
		return retCollection;
	}
}