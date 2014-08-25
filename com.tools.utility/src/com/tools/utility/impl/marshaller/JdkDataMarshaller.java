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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import com.tools.utility.spi.marshaller.AbstractDataMarshaller;

/**
 * 使用JDK的序列化，反序列化机制实现的数据序列化
 *
 * @author wuyuhou
 *
 */
public class JdkDataMarshaller extends AbstractDataMarshaller {

	@Override
	protected void doMarshal(Object data, OutputStream out, Object additional) throws Exception {
		ObjectOutputStream outObj = out instanceof ObjectOutputStream ? (ObjectOutputStream)out : new ObjectOutputStream(out);
		outObj.writeObject(data);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <T> T doUnmarshal(InputStream in, Object additional) throws Exception {
		ObjectInputStream objIn = in instanceof ObjectInputStream ? (ObjectInputStream)in : new ObjectInputStream(in);
		return (T)objIn.readObject();
	}

}