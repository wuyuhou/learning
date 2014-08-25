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
 * 类型转换器接口定义
 *
 * @author wuyuhou
 *
 */
public interface ITypeConvertor<T> {
	
	/**
	 * 是否可以转换
	 * 
	 * @param value 值
	 * @param toClass 指定类型
	 * @return true：可以转换
	 */
	boolean canConvert(Object value, Class toClass);
	
	/**
	 * 把value转换为指定的对象
	 * 
	 * @param value 值
	 * @param toClass 指定类型
	 * @param defaultValue 默认值，如果value为空时的默认值，可以为空（系统会自动指定默认值）
	 * @param overrideValue 填充到某个值中，如果不为空，并且不是原始类型，则会被修改，主要指JavaBena、Map、Collection、数组等
	 * @return 转换后的值
	 */
	T convert(Object value, Class toClass, T defaultValue, T overrideValue);
}