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
 * 格式化器抽象基类
 *
 * @author wuyuhou
 *
 */
public abstract class AbstractFormatter<T, S> implements IFormatter {
	
	private AbstractFormatter formatter = null;
	private AbstractFormatter unformatter = null;
	
	public AbstractFormatter() {
		
	}
	
	@SuppressWarnings("unchecked")
	public AbstractFormatter(AbstractFormatter formatter) {
		if (formatter == this) {
			throw new IllegalArgumentException("Formatter can not own!");
		}
		this.formatter = formatter;
		formatter.unformatter = this;
	}
	
	/**
	 * 格式化
	 * 
	 * @param data 需要格式化的数据
	 * @return 格式化结果
	 */
	@SuppressWarnings("unchecked")
	public S format(Object data) {		
		check(data);
		if (formatter == null) {
			return doFormat((T)data);
		} else {
			return doFormat((T)formatter.format(data));
		}		
	}
	
	protected void check(Object data) {
		if (data == null) {
			throw new IllegalArgumentException("The data is null!");
		}		
		doCheck();
	}
	
	/**
	 * 反格式化（还原回最初的形式）
	 * 
	 * @param data 需要反格式化的数据
	 * @return 反格式化结果
	 */
	@SuppressWarnings("unchecked")
	public T unformat(Object data) {
		check(data);
		if (unformatter == null) {
			return doUnformat((S)data);
		} else {
			
			return doUnformat((S)unformatter.unformat(data));
		}	
	}
	
	/**
	 * 检查实现类内的属性，是否已经合理赋值，否则抛出FormatRuntimeException
	 * 
	 */
	abstract protected void doCheck();
	
	/**
	 * 格式化
	 * 
	 * @param data 需要格式化的数据
	 * @return 格式化结果
	 */
	abstract protected S doFormat(T data);
	
	/**
	 * 反格式化（还原回最初的形式）
	 * 
	 * @param data 需要反格式化的数据
	 * @return 反格式化结果
	 */
	abstract protected T doUnformat(S data);
	
}