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



/**
 * 类型转换器接口定义抽象实现
 *
 * @author wuyuhou
 *
 */
public abstract class AbstractTypeConvertor<T> implements ITypeConvertor<T> {
	
	private ITypeConvertor convertBefore = null;
	
	private ITypeConvertor convertAfter = null;
	
	private ITypeConvertor convertException = null;
	
	public AbstractTypeConvertor() {
		
	}
	
	public AbstractTypeConvertor(
			ITypeConvertor convertBefore, 
			ITypeConvertor convertAfter,
			ITypeConvertor convertException) {
		if (convertBefore == this) {
			throw new IllegalArgumentException("convertBefore can not own!");
		}
		if (convertAfter == this) {
			throw new IllegalArgumentException("convertAfter can not own!");
		}
		if (convertException == this) {
			throw new IllegalArgumentException("convertException can not own!");
		}
		this.convertBefore = convertBefore;
		this.convertAfter = convertAfter;
		this.convertException = convertException;
	}
	
	/**
	 * 是否可以转换
	 * 
	 * @param value 值
	 * @param toClass 指定类型
	 * @return true：可以转换
	 */
	public boolean canConvert(Object value, Class toClass) {
		if (toClass == null) {
			return false;
		}
		try {
			return doCanConvert(value, toClass);
		} catch (Throwable t) {
			return false;
		}
	}
	
	/**
	 * 把value转换转换为指定的对象
	 * 
	 * @param value 值
	 * @param toClass 指定类型
	 * @return 转换后的值
	 */
	@SuppressWarnings("unchecked")
	public T convert(Object value, Class toClass, T defaultValue, T overrideValue) {
		if (value != null) {
			//如果是同种类型，可以直接强转
			if (overrideValue == null && 
					toClass != null && 
					toClass.isAssignableFrom(value.getClass())) {
				return (T)value;
			}
		}
		try {
			if (convertBefore != null) {
				if (convertBefore.canConvert(value, toClass)) {
					value = convertBefore.convert(value, toClass, defaultValue, overrideValue);
				}
			}
			value = doConvert(value, toClass, defaultValue, overrideValue);
			if (convertAfter != null) {
				if (convertAfter.canConvert(value, toClass)) {
					value = convertAfter.convert(value, toClass, defaultValue, overrideValue);
				}
			}
			return (T)value;
		} catch (Throwable e) {
			if (convertException != null) {
				if (convertException.canConvert(value, toClass)) {
					return (T)convertException.convert(value, toClass, defaultValue, overrideValue);
				}
			}
			if (e instanceof RuntimeException) {
				throw (RuntimeException)e;
			} else {
				throw new IllegalArgumentException(e);
			}
		}
	}
	 
	protected abstract boolean doCanConvert(Object value, Class toClass);
	
	protected abstract T doConvert(Object value, Class toClass, T defaultValue, T overrideValue);
}