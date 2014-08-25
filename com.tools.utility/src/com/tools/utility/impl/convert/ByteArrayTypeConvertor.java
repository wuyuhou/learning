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

import com.tools.utility.spi.convert.AbstractTypeConvertor;



/**
 * Byte数组转换器
 *
 * @author wuyuhou
 *
 */
public class ByteArrayTypeConvertor extends AbstractTypeConvertor<Byte[]> {
	
	public ByteArrayTypeConvertor() {
		super(new PrimitiveByteArrayTypeConvertor(), null, new ArrayTypeConvertor());
	}
	
	@Override
	protected boolean doCanConvert(Object value, Class toClass) {
		return Byte[].class == toClass;
	}

	@Override
	protected Byte[] doConvert(Object value, Class toClass, Byte[] defaultValue, Byte[] overrideValue) {
		if (value instanceof byte[]) {
			byte[] bytes = (byte[])value;
			Byte[] byteArray = new Byte[bytes.length];
			for (int i = 0; i < bytes.length; i++) {
				byteArray[i] = bytes[i];
			}
			return byteArray;
		}		
		throw new IllegalArgumentException("Does not recognize the data:" + value);
	}
}