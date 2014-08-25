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

import java.util.HashMap;


/**
 * 人民币大写的格式化器<br>
 * 
 * 支持负数，支持double数据类型。<br>
 * 
 * 人民币大写包含的字： 人民币、负、零、壹、贰、叁、肆、伍、陆、柒、捌、玖、拾、佰、仟、万、亿、元（圆）、角、分、整（正）<br>
 * 比如：￥107000.53 -> 人民币壹拾万零柒仟元伍角叁分
 *
 * @author wuyuhou
 *
 */
@SuppressWarnings("unchecked")
public class RmbCapitalFormatter extends AbstractFormatter<Double, String> {
	
	//大写对应数组
	private static final String RMB_CAPITAL[] = {
			"零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖", "拾"
	};
	 
	private static final HashMap<String, Integer> RMB_CAPITAL_MAP = new HashMap<String, Integer>();
	static {
		RMB_CAPITAL_MAP.put("零", 0);
		RMB_CAPITAL_MAP.put("壹", 1);
		RMB_CAPITAL_MAP.put("贰", 2);
		RMB_CAPITAL_MAP.put("叁", 3);
		RMB_CAPITAL_MAP.put("肆", 4);
		RMB_CAPITAL_MAP.put("伍", 5);
		RMB_CAPITAL_MAP.put("陆", 6);
		RMB_CAPITAL_MAP.put("柒", 7);
		RMB_CAPITAL_MAP.put("捌", 8);
		RMB_CAPITAL_MAP.put("玖", 9);
		RMB_CAPITAL_MAP.put("拾", 10);
	}
	
	//是圆（复）还是元（简）
	private boolean isSimpleYuan = false;
	
	//是整（复）还是正（简）
	private boolean isSimpleZheng = false;

	public RmbCapitalFormatter() {
		super();
	}
	
	public RmbCapitalFormatter(AbstractFormatter formatter) {
		super(formatter);
	}

	public boolean isSimpleYuan() {
		return isSimpleYuan;
	}

	public RmbCapitalFormatter setSimpleYuan(boolean isSimpleYuan) {
		this.isSimpleYuan = isSimpleYuan;
		return this;
	}

	public boolean isSimpleZheng() {
		return isSimpleZheng;
	}

	public RmbCapitalFormatter setSimpleZheng(boolean isSimpleZheng) {
		this.isSimpleZheng = isSimpleZheng;
		return this;
	}

	@Override
	protected void doCheck() {
		
	}

	@Override
	protected String doFormat(Double data) {
		StringBuilder buf = new StringBuilder();
		boolean isNegative = false;
		if (data < 0) {
			isNegative = true;
			data = 0 - data;
		}
		//整数部分
		long intPart = data.longValue();
		//小数部分，只有前两位小数有意义
		int fractionPart = (int)((data - intPart) * 100);
		if (intPart == 0 && fractionPart == 0) {
			buf.append("人民币零");
			if (isSimpleYuan) {
				buf.append("元");
			} else {
				buf.append("圆");
			}
			if (isSimpleZheng) {
				buf.append("正");
			} else {
				buf.append("整");
			}
			return buf.toString();
		}
		
		String cur = "";		
		boolean needZero = false;
		//亿为一个大循环，四位数是一个小循环
		while(intPart > 0) {
			int lowestFourDigits = (int)(intPart % 10000);
			intPart = intPart / 10000;
			
			if (lowestFourDigits > 0) {
				if (needZero) {
					buf.insert(0, "零");
					needZero = false;
				}
				buf.insert(0, changeFourDigists(lowestFourDigits) + cur);
				if (lowestFourDigits < 1000) {
					needZero = true;
				}
			} else {
				if ("亿".equals(cur)) {
					buf.insert(0, cur);
				}
			}
			if (cur.length() == 0) {
				cur = "万";
			} else if ("万".equals(cur)) {
				cur = "亿";
			}  else if ("亿".equals(cur)) {
				cur = "万";
			}
		}
		if (data > 1) {
			if (isSimpleYuan) {
				buf.append("元");
			} else {
				buf.append("圆");
			}
		}		
		if (fractionPart > 0) {
			int jiaoPart = fractionPart / 10;
			int fenPart = fractionPart % 10;
			if (jiaoPart > 0) {
				buf.append(RMB_CAPITAL[jiaoPart]).append("角");
			}
			if (fenPart > 0) {
				if (data > 1 && jiaoPart == 0) {
					buf.append(RMB_CAPITAL[0]);// 零
				}
				buf.append(RMB_CAPITAL[fenPart]).append("分");
			}
		} else {
			if (isSimpleZheng) {
				buf.append("正");
			} else {
				buf.append("整");
			}
		}
		String result = buf.toString();
		if (isNegative) {
			return "人民币负" + result;
		} else {
			return "人民币" + result;
		}
	}

	//四位整数转换成大写金额
	private static String changeFourDigists(int number) {
		if (number == 0) {
			return "零";
		}
		
		StringBuilder buf = new StringBuilder();
		//千位数部分
		int qianPart = number / 1000;
		number = number % 1000;
		//百位数部分
		int baiPart = number / 100;
		number = number % 100;
		//十位数部分
		int shiPart = number / 10;
		number = number % 10;
		//个位数部分
		int gePart = number;
		
		//是否需要添零
		boolean needZero = false;
		//前面是否有值
		boolean havaValueBefore = false;
		if (qianPart > 0) {
			buf.append(RMB_CAPITAL[qianPart]).append("仟");
			havaValueBefore = true;
		} else {
			havaValueBefore = false;
		}

		if (baiPart > 0) {
			buf.append(RMB_CAPITAL[baiPart]).append("佰");
			havaValueBefore = true;
		} else {
			if (havaValueBefore) {
				needZero = true;
			}
		}

		if (shiPart > 0) {
			if (needZero) {
				buf.append("零");
				needZero = false;
			}
			buf.append(RMB_CAPITAL[shiPart]).append("拾");
			havaValueBefore = true;
		}  else {
			if (havaValueBefore) {
				needZero = true;
			}
		}
		
		if (gePart > 0) {
			if (needZero) {
				buf.append("零");
				needZero = false;
			}
			buf.append(RMB_CAPITAL[gePart]);
		}
		
		return buf.toString();
	}

	@Override
	protected Double doUnformat(String data) {
		boolean isNegative = false;
		if (data.startsWith("人民币")) {
			data = data.substring(3);
		}
		if (data.startsWith("负")) {
			isNegative = true;
			data = data.substring(1);
		}
		//整数部分
		long intPart = 0;
		//小数部分
		int fractionPart = 0;
		
		//千分位部分
		int qianPart = 0;
		//百分位部分
		int baiPart = 0;
		//十分位部分
		int shiPart = 0;
		//个位部分
		int gePart = 0;
		while(data.length() > 0) {
			String token = data.substring(0, 1);
			
			if (token.equals("零")) {
			} else if (token.equals("整") || token.equals("正")) {
				break;
			} else if (token.equals("角")) {
				shiPart = gePart * 10;
				gePart = 0;
			} else if (token.equals("分")) {
				fractionPart += shiPart + gePart;
				shiPart = 0;
				gePart = 0;
				break;
			} else if (token.equals("圆") || token.equals("元")) {
				intPart += qianPart + baiPart + shiPart + gePart;
				qianPart = 0;
				baiPart = 0;
				shiPart = 0;
				gePart = 0;
			} else if (token.equals("拾")) {
				shiPart = gePart * 10;
				gePart = 0;
			} else if (token.equals("佰")) {
				baiPart = gePart * 100;
				gePart = 0;
			} else if (token.equals("仟")) {
				qianPart = gePart * 1000;
				gePart = 0;			
			} else if (token.equals("万")) {
				intPart += (qianPart + baiPart + shiPart + gePart) * 10000;
				qianPart = 0;
				baiPart = 0;
				shiPart = 0;
				gePart = 0;
			} else if (token.equals("亿")) {
				intPart += qianPart + baiPart + shiPart + gePart;
				intPart = intPart * 100000000;
				qianPart = 0;
				baiPart = 0;
				shiPart = 0;
				gePart = 0;				
			} else {
				gePart = RMB_CAPITAL_MAP.get(token);
			}
			data = data.substring(1);			
		}
		double result = intPart + ((double)fractionPart) / 100;
		if (isNegative) {
			return 0 - result;
		} else {
			return result;
		}
	}
}