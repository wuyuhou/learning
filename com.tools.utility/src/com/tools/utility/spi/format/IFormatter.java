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
package com.tools.utility.spi.format;

/**
 * 格式化器接口
 *
 * @author wuyuhou
 *
 */
public interface IFormatter {
	
	/**
	 * 格式化
	 * 
	 * @param data 需要格式化的数据
	 * @return 格式化结果
	 */
	<S> S format(Object data);
	
	/**
	 * 反格式化（还原回最初的形式）
	 * 
	 * @param data 需要反格式化的数据
	 * @return 反格式化结果
	 */
	<T> T unformat(Object data);
}