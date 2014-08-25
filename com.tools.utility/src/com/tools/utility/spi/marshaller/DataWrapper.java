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

/**
 * 数据对象包裹
 *
 * @author wuyuhou
 */
public class DataWrapper implements java.io.Serializable {

	private static final long serialVersionUID = 4453190795843175352L;

	private Object data = null;

	public Object getData() {
		return data;
	}

	public void setData(Object object) {
		this.data = object;
	}
}