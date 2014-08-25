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
 * 布尔数据类型的格式化器<br>
 * 
 * 默认是true和fasle
 *
 * @author wuyuhou
 *
 */
@SuppressWarnings("unchecked")
public class BooleanFormatter extends AbstractFormatter<Boolean, String> {
	
	//true标志
	private String trueSymbol = "true";
	
	//false标志
	private String falseSymbol = "false";
	
	//大小写是否敏感
	private boolean isCaseSensitive = true;
	
	public BooleanFormatter() {
		super();
	}
	
	public BooleanFormatter(AbstractFormatter formatter) {
		super(formatter);
	}
	
	public boolean isCaseSensitive() {
		return isCaseSensitive;
	}

	public BooleanFormatter setCaseSensitive(boolean isCaseSensitive) {
		this.isCaseSensitive = isCaseSensitive;
		return this;
	}

	public BooleanFormatter setSymbol(String trueSymbol, String falseSymbol) {
		if (trueSymbol == null || trueSymbol.trim().length() == 0) {
			throw new IllegalArgumentException("True symbol is null!");
		}
		if (falseSymbol == null || falseSymbol.trim().length() == 0) {
			throw new IllegalArgumentException("Fasle symbol is null!");
		}
		this.trueSymbol = trueSymbol.trim();
		this.falseSymbol = falseSymbol.trim();
		return this;
	}
	
	/**
	 * 设置标志
	 * 
	 * @param symbol 标志字符串，以","或者"|"或者空格分隔；代表true的在前面
	 */
	public BooleanFormatter setSymbol(String symbol) {
		if (symbol == null || symbol.trim().length() == 0) {
			throw new IllegalArgumentException("Symbol is null!");
		}
		String[] symbols = parseSymbol(symbol);
		if (symbols.length != 2) {
			throw new IllegalArgumentException("Symbol'" + symbol + "' is incorrect format!");
		}
		this.trueSymbol = symbols[0].trim();
		this.falseSymbol = symbols[1].trim();
		return this;
	}
	
	private String[] parseSymbol(String symbol) {
		int index = symbol.indexOf(',');
		if (index != -1) {
			return symbol.split(",");
		}
		index = symbol.indexOf('|');
		if (index != -1) {
			return symbol.split("|");
		}
		index = symbol.indexOf(' ');
		if (index != -1) {
			return symbol.split(" ");
		}
		return new String[]{symbol};
	}

	public String getFalseSymbol() {
		return falseSymbol;
	}

	public String getTrueSymbol() {
		return trueSymbol;
	}

	@Override
	protected void doCheck() {
		if (trueSymbol == null || trueSymbol.trim().length() == 0 
				|| falseSymbol == null || falseSymbol.trim().length() == 0) {
			throw new FormatRuntimeException("Symbol is null, please set it!");
		}		
	}

	@Override
	protected String doFormat(Boolean data) {
		if (data) {
			return trueSymbol;
		} else {
			return falseSymbol;
		}
	}

	@Override
	protected Boolean doUnformat(String data) {
		data = data.trim();
		if (data.length() == 0) {
			return Boolean.FALSE;
		}
		if (isCaseSensitive) {
			if (trueSymbol.equals(data)) {
				return Boolean.TRUE;
			} else if (falseSymbol.equals(data)) {
				return Boolean.FALSE;
			}
		} else {
			if (trueSymbol.equalsIgnoreCase(data)) {
				return Boolean.TRUE;
			} else if (falseSymbol.equalsIgnoreCase(data)) {
				return Boolean.FALSE;
			}
		}
		throw new FormatRuntimeException("Does not recognize data[{0}]!", new Object[]{data});
	}
}