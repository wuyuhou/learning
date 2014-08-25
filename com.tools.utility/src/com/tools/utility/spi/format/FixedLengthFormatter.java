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

import java.io.UnsupportedEncodingException;

/**
 * 固定长度格式化器
 * 
 * @author wuyuhou
 * 
 */
@SuppressWarnings("unchecked")
public class FixedLengthFormatter extends AbstractFormatter<String, String> {
	
	//对齐方式
	public static enum Alignment {
		LEFT,//左对齐
		LCENTER,//靠左居中，如缺5字节，则左补2字节，右补3字节
		RCENTER,//靠右居中，如缺5字节，则左补3字节，右补2字
		RIGHT//右对齐
	}
	
	//对齐方式，默认左对齐
	private Alignment alignment = Alignment.LEFT;
	
	//长度
	private int fixedLength = -1;
	
	//填充符号
	private String fillSymbol = null;
	
	//是否以字节长度计算，默认是以字符长度计算
	private boolean isByteMode = false;
	
	//编码方式，字节模式时有意义
	private String encoding = null;
	
	public FixedLengthFormatter() {
		super();
	}

	public FixedLengthFormatter(AbstractFormatter formatter) {
		super(formatter);
	}

	public Alignment getAlignment() {
		return alignment;
	}

	public FixedLengthFormatter setAlignment(Alignment alignment) {
		if (alignment == null) {
			throw new IllegalArgumentException("Alignment is null!");
		}
		this.alignment = alignment;
		return this;
	}

	public String getFillSymbol() {
		return fillSymbol;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public FixedLengthFormatter setFillSymbol(String fillSymbol) {
		if (fillSymbol == null) {
			throw new IllegalArgumentException("fillSymbol is null!");
		}
		if (fillSymbol.length() != 1) {
			throw new IllegalArgumentException("fillSymbol'" + fillSymbol +"' length != 1");
		}
		this.fillSymbol = fillSymbol;
		return this;
	}
	
	public FixedLengthFormatter setFillSymbol(int fillSymbolAscii) {
		char c = (char)fillSymbolAscii;
		this.fillSymbol = c + "";
		return this;
	}

	public int getFixedLength() {
		return fixedLength;
	}

	public FixedLengthFormatter setFixedLength(int fixedLength) {
		if (fixedLength < 0) {
			throw new IllegalArgumentException("fixedLength < 0");
		}
		this.fixedLength = fixedLength;
		return this;
	}

	public boolean isByteMode() {
		return isByteMode;
	}

	public FixedLengthFormatter setByteMode(boolean isByteMode) {
		this.isByteMode = isByteMode;
		return this;
	}

	@Override
	protected void doCheck() {
		if (fixedLength < 0) {
			throw new FormatRuntimeException("FixedLength is less than zero, please set it!");
		}
		if (fillSymbol == null) {
			throw new FormatRuntimeException("FillSymbol is null, please set it!");
		}
		if (fillSymbol.length() != 1) {
			throw new IllegalArgumentException("fillSymbol'" + fillSymbol +"' length != 1");
		}
	}
	
	private byte[] getBytes(String str) {
		if (encoding == null || encoding.trim().length() == 0) {
			return str.getBytes();
		} else {
			try {
				return str.getBytes(encoding);
			} catch (UnsupportedEncodingException e) {
				return str.getBytes();
			}
		}	
	}

	@Override
	protected String doFormat(String data) {
		int actualLength = 0;
		if (isByteMode) {
			actualLength = getBytes(data).length;		
		} else {
			actualLength = data.length();
		}
		
		//需要填充的长度
		int fillLength = fixedLength - actualLength;
		
		if (fillLength == 0) {
			return data;
		} else if (fillLength < 0) {
			//截取
			if (isByteMode) {
				if (actualLength / 2 > fixedLength) {
					for (int i = 1; i < data.length(); i++) {
						if (getBytes(data.substring(0, i)).length > fixedLength) {
							return data.substring(0, i - 1);
						}
					}
				} else {
					for (int i = data.length() - 2; i >= 0; i--) {
						String result = data.substring(0, i);
						if (getBytes(result).length <= fixedLength) {
							return result;
						}
					}
				}
			} else {
				return data.substring(0, fixedLength);
			}
		} else {			
			int fillSymbolByteLength = getBytes(fillSymbol).length;
			int halfFillLength = fillLength / 2;
			if (Alignment.RCENTER.equals(alignment)) {
				if (fillLength % 2 != 0) {
					halfFillLength++;
				}				
			}
			//取左边的一半
			StringBuilder bufLeftHalf = new StringBuilder();
			for (int i = 0; i < halfFillLength; i++) {
				if (isByteMode) {
					if (fillSymbolByteLength * (i + 1) > halfFillLength) {
						break;
					}
				} 
				bufLeftHalf.append(fillSymbol);
			}
			int leftByteLength = getBytes(bufLeftHalf.toString()).length;
			
			//右边的一半
			StringBuilder bufRightHalf = new StringBuilder();
			for (int i = halfFillLength; i < fillLength; i++) {
				if (isByteMode) {
					if (fillSymbolByteLength * (i - halfFillLength + 1) + leftByteLength > fillLength) {
						break;
					}
				} 
				bufRightHalf.append(fillSymbol);
			}
			//补齐
			if (Alignment.LEFT.equals(alignment)) {
				return data + bufLeftHalf.append(bufRightHalf.toString()).toString();
			} else if (Alignment.RIGHT.equals(alignment)) {
				return bufLeftHalf.append(bufRightHalf.toString()).append(data).toString();
			} else {
				return bufLeftHalf.append(data).append(bufRightHalf.toString()).toString();
			}	
		}
		throw new FormatRuntimeException("Does not format data[{0}]!", new Object[]{data});
	}

	@Override
	protected String doUnformat(String data) {
		int actualLength = 0;
		if (isByteMode) {
			actualLength = getBytes(data).length;
		} else {
			actualLength = data.length();
		}
		if (actualLength > fixedLength) {
			throw new FormatRuntimeException("Data[{0}] length[{1}] is greater than fixedLength[{2}]!", 
					new Object[]{data, actualLength, fixedLength});
		}
		if (Alignment.LEFT.equals(alignment)) {
			int index = data.length() - 1;
			for (; index >= 0; index--) {
				if (!fillSymbol.equals(data.substring(index, index + 1))) {
					break;
				}
			}
			return data.substring(0, index + 1);
		} else if (Alignment.RIGHT.equals(alignment)) {
			int index = 0;
			for (; index < data.length(); index++) {
				if (!fillSymbol.equals(data.substring(index, index + 1))) {
					break;
				}
			}
			return data.substring(index);
		} else {
			int index = 0;
			for (; index < data.length(); index++) {
				if (!fillSymbol.equals(data.substring(index, index + 1))) {
					break;
				}
			}
			data = data.substring(index);
			index = data.length() - 1;
			for (; index >= 0; index--) {
				if (!fillSymbol.equals(data.substring(index, index + 1))) {
					break;
				}
			}
			return data.substring(0, index + 1);
		}
	}
}