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
package com.tools.utility.impl.marshaller;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;


import com.tools.utility.api.IOUtil;
import com.tools.utility.api.JsonUtil;
import com.tools.utility.spi.marshaller.AbstractDataMarshaller;
import com.tools.utility.spi.marshaller.DataWrapper;


/**
 * 使用Json数据类型的序列化，反序列化机制实现的数据序列化，会比较慢
 *
 * @author wuyuhou
 *
 */
public class JsonDataMarshaller extends AbstractDataMarshaller {

	@Override
	protected void doMarshal(Object data, OutputStream out, Object additional) throws Exception {
		out.write(JsonUtil.toJsonString(data).getBytes());
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <T> T doUnmarshal(InputStream in, Object additional) throws Exception {
		Map<String, Class> fieldTypeMap = new HashMap<String, Class>();
		if (additional instanceof Class) {
			fieldTypeMap.put("/data", (Class)additional);
		}		
		return (T)JsonUtil.toJavaObject(new String(IOUtil.read(in)), DataWrapper.class, fieldTypeMap);
	}

}