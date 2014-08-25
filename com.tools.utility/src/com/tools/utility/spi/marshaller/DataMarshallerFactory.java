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

import com.tools.utility.impl.marshaller.JdkDataMarshaller;

/**
 * 数据序列化工厂
 *
 * @author wuyuhou
 *
 */
public class DataMarshallerFactory {
	private static IDataMarshaller marshaller = null;
	public static IDataMarshaller getDataMarshaller() {
		//不需要做并发控制
		if (marshaller == null) {
			marshaller = new JdkDataMarshaller();
		}
		return marshaller;
	}

	public static void setDataMarshaller(IDataMarshaller _marshaller) {
		if (_marshaller != null) {
			marshaller = _marshaller;
		}
	}
}