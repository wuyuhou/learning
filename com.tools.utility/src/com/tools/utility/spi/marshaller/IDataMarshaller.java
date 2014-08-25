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
package com.tools.utility.spi.marshaller;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 数据序列化接口
 *
 * @author wuyuhou
 *
 */
public interface IDataMarshaller {

	/**
	 * 序列化
	 *
	 * @param data 数据对象，不可以为空
	 * @param out 序列化目标，不可以为空
	 * @param additional 额外附属信息，如果没有用，可以为空
	 */
	void marshal(Object data, OutputStream out, Object additional) throws Exception;

	/**
	 * 反序列化
	 *
	 * @param in 输入，不可以为空
	 * @param additional 额外附属信息，如果没有用，可以为空
	 * @return 数据对象
	 */
	<T> T unmarshal(InputStream in, Object additional) throws Exception;

	/**
	 * 序列化
	 *
	 * @param data 数据对象，不可以为空
	 */
	byte[] marshal(Object data) throws Exception;

	/**
	 * 反序列化
	 *
	 * @param bytes 输入，不可以为空
	 * @return 数据对象
	 */
	<T> T unmarshal(byte[] bytes) throws Exception;

	/**
	 * 序列化
	 *
	 * @param data 数据对象，不可以为空
	 * @param encoding 编码格式，可以为空
	 */
	String marshal(Object data, String encoding) throws Exception;

	/**
	 * 反序列化
	 *
	 * @param in 输入，不可以为空
	 * @param encoding 编码格式，可以为空
	 * @return 数据对象
	 */
	<T> T unmarshal(String in, String encoding) throws Exception;

	/**
	 * 序列化
	 *
	 * @param data 数据对象，不可以为空
	 * @param outFile 序列化目标文件，不可以为空
	 * @param encoding 编码格式，可以为空
	 */
	void marshal(Object data, File outFile, String encoding) throws Exception;

	/**
	 * 反序列化
	 *
	 * @param inFile 输入文件，不可以为空
	 * @param encoding 编码格式，可以为空
	 * @return 数据对象
	 */
	<T> T unmarshal(File inFile, String encoding) throws Exception;
}