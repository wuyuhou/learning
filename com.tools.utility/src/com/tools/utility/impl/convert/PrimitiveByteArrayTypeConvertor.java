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

import java.math.BigInteger;

import com.tools.utility.spi.convert.AbstractTypeConvertor;

/**
 * PrimitiveByte数组转换器
 *
 * @author wuyuhou
 *
 */
public class PrimitiveByteArrayTypeConvertor extends AbstractTypeConvertor<byte[]> {
	
	public PrimitiveByteArrayTypeConvertor() {
		super(null, null, new ArrayTypeConvertor());
	}

	@Override
	protected boolean doCanConvert(Object value, Class toClass) {
		return byte[].class == toClass;
	}

	@Override
	protected byte[] doConvert(Object value, Class toClass, byte[] defaultValue, byte[] overrideValue) {
		if (value == null) {
			if (overrideValue != null) {
				return overrideValue;
			}
			if (defaultValue != null) {
				return defaultValue;
			}
			return new byte[0];
		}
		
		if (value instanceof Byte[]) {
			Byte[] byteArray = (Byte[])value;
			byte[] retValue = null;
			if (overrideValue != null) {
				retValue = overrideValue;
			}
			if (retValue == null && defaultValue != null) {
				retValue = defaultValue;
			}
			if (retValue == null) {
				retValue = new byte[byteArray.length];
			}
			
			for (int i = 0; i < retValue.length && i < byteArray.length; i++) {
				retValue[i] = byteArray[i];
			}
			return retValue;
		}
		
		if (value instanceof Boolean) {
			return new byte[]{((Boolean) value) == true ? (byte)1 : (byte)0};
		}
		
		if (value instanceof Byte) {
			return new byte[]{(Byte) value};
		}
		
		if (value instanceof Short) {
			short v = (Short)value;
			return new byte[]{
					(byte)((v >>> 8) & 0xFF), 
					(byte)((v >>> 0) & 0xFF)};
		}
		
		if (value instanceof Character) {
			char v = (Character)value;
			return new byte[]{					
					(byte)((v >>> 8) & 0xFF), 
					(byte)((v >>> 0) & 0xFF)};
		}
		
		if (value instanceof Integer) {
			int v = (Integer)value;
			return new byte[]{
					(byte)((v >>> 24) & 0xFF), 
					(byte)((v >>> 16) & 0xFF), 
					(byte)((v >>> 8) & 0xFF), 
					(byte)((v >>> 0) & 0xFF)};
		}
		
		if (value instanceof Long) {
			long v = (Long)value;
			return new byte[]{
					(byte)((v >>> 56) & 0xFF), 
					(byte)((v >>> 48) & 0xFF), 
					(byte)((v >>> 40) & 0xFF), 
					(byte)((v >>> 32) & 0xFF), 
					(byte)((v >>> 24) & 0xFF), 
					(byte)((v >>> 16) & 0xFF), 
					(byte)((v >>> 8) & 0xFF), 
					(byte)((v >>> 0) & 0xFF)};
		}
		
		if (value instanceof Float) {
			int v = Float.floatToIntBits((Float)value);
			return new byte[]{
					(byte)((v >>> 24) & 0xFF), 
					(byte)((v >>> 16) & 0xFF), 
					(byte)((v >>> 8) & 0xFF), 
					(byte)((v >>> 0) & 0xFF)};
		}
		
		if (value instanceof Double) {
			long v = Double.doubleToLongBits((Double)value);
			return new byte[]{
					(byte)((v >>> 56) & 0xFF), 
					(byte)((v >>> 48) & 0xFF), 
					(byte)((v >>> 40) & 0xFF), 
					(byte)((v >>> 32) & 0xFF), 
					(byte)((v >>> 24) & 0xFF), 
					(byte)((v >>> 16) & 0xFF), 
					(byte)((v >>> 8) & 0xFF), 
					(byte)((v >>> 0) & 0xFF)};
		}
		
		if (value instanceof BigInteger) {
			return ((BigInteger) value).toByteArray();
		}

		if (value instanceof String) {
			return ((String) value).getBytes();
		}
		
		throw new IllegalArgumentException("Does not recognize the data:" + value);
	}
}