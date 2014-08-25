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

import java.text.DecimalFormat;
import java.text.ParseException;

import com.tools.utility.api.ReflectUtil;

/**
 * 数字类型的格式化器<br>
 * 
 * 
 * 可以是下列数据类型：Long，Integer，Short，Byte，BigInteger，BigDecimal，double，Number <br>
 * 
 * 注意：凡格式化默认为Long或者Double类型
 * 
 * Format 模式包含正数和负数子模式，例如 "#,##0.00;(#,##0.00)"。每个子模式都有前缀、数字部分和后缀。 <br>
 * 负数子模式是可选的；如果存在，则将用已本地化的减号（在多数语言环境中是 '-'）作为前缀的正数子模式用作负数子模式。也就是说，单独的 "0.00" 等效于 "0.00;-0.00"。<br>
 * 如果存在显式的负数子模式，则它仅指定负数前缀和后缀；数字位数、最小位数，其他特征都与正数模式相同。<br>
 * 这意味着 "#,##0.0#;(#)" 的行为与 "#,##0.0#;(#,##0.0#)" 完全相同。<br>
 * 
 * <pre>
 * 下列字符用在非本地化的模式中。
 * 已本地化的模式使用从此 formatter 的 DecimalFormatSymbols 对象中获得的相应字符，这些字符已失去其特殊状态。两种例外是货币符号和引号，不将其本地化。

	符号	        位置	        本地化？	含义
	0	        数字	        是	    阿拉伯数字
	#	        数字 	    是	    阿拉伯数字，如果不存在则显示为 0
	.	        数字	        是	    小数分隔符或货币小数分隔符
	-	        数字	        是	    负号
	,	        数字	        是	    分组分隔符
	E	        数字	        是	    分隔科学计数法中的尾数和指数。在前缀或后缀中无需加引号。
	;	        子模式边界	是	    分隔正数和负数子模式
	%	        前缀或后缀	是	    乘以 100 并显示为百分数
	\u2030	    前缀或后缀	是	    乘以 1000 并显示为千分数
	¤ (\u00A4)	前缀或后缀	否	    货币记号，由货币符号替换。如果两个同时出现，则用国际货币符号替换。如果出现在某个模式中，则使用货币小数分隔符，而不使用小数分隔符。
	'	        前缀或后缀	否	    用于在前缀或或后缀中为特殊字符加引号，例如 "'#'#" 将 123 格式化为 "#123"。要创建单引号本身，请连续使用两个单引号："# o''clock"。
 * </pre>
 * 
 * @author wuyuhou
 *
 */
@SuppressWarnings("unchecked")
public class NumberFormatter<N extends Number> extends AbstractFormatter<N, String> {
	
	// 12.23454->12.23
	// 12.2 -> 12.20
	public static final String N00 = "#.00";
	
	// 12.23454->12
	public static final String N0 = "#";
	
	// 1223454->1, 223, 454
	public static final String NNN = "###, ###, ###, ###";	
	// 1223454->000, 001, 223, 454
	public static final String NNN000 = "000, 000, 000, 000";
	public static final String E000 = "0.###E0";
	
	// 0.2345 -> 23%
	public static final String P0 = "#%";
	//0.2345 -> 23.5%
	public static final String P00 = "#.0%";
	
	//0.2345 -> 235‰
	public static final String Q0 = "#\u2030";
	//0.2345 -> 234.5‰
	public static final String Q000 = "#.0\u2030";
	
	// 123.5->￥124
	public static final String C0 = "¤#";	
	// 123.5->￥123.50
	public static final String C00 = "¤#.00";

    //模式，比如"0.###E0" 将数字 1234 格式化为 "1.234E3"
	private String pattern = null;
	
	private DecimalFormat numberFormat = null;
	
	public NumberFormatter() {
		super();
		numberFormat = new DecimalFormat();
	}
	
	public NumberFormatter(AbstractFormatter formatter) {
		super(formatter);
		numberFormat = new DecimalFormat();
	}

	public String getPattern() {		
		return pattern;
	}

	public NumberFormatter setPattern(String pattern) {
		if (pattern == null || pattern.trim().length() == 0) {
			throw new IllegalArgumentException("pattern is null!");
		}
		this.pattern = pattern.trim();
		numberFormat.applyPattern(this.pattern);
		return this;
	}

	@Override
	protected void doCheck() {
		if (pattern == null || pattern.trim().length() == 0) {
			throw new FormatRuntimeException("Pattern is null, please set it!");
		}
	}

	@Override
	protected String doFormat(N data) {
		return numberFormat.format(data);
	}

	@Override
	protected N doUnformat(String data) {
		try {
			return (N)numberFormat.parse(data);
		} catch (ParseException e) {
			throw new FormatRuntimeException("[{0}] cannot unformat by pattern[{1}]!", new Object[]{data, pattern}, e);
		}
	}
	
	/**
	 * 指定返回数据类型
	 * 
	 * @param data
	 * @param toClass
	 * @return
	 */
	public <D> D unformat(Object data, Class<D> toClass) {
		return ReflectUtil.cast(unformat(data), toClass);
	}
}