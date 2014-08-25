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
package com.tools.utility.api;

import java.util.Date;

import com.tools.utility.spi.format.DateFormatter;
import com.tools.utility.spi.format.NumberFormatter;

/**
 * 格式化工具类
 *
 * @author wuyuhou
 *
 */
public class FormatUtil {
	
	public static final String YYYYMMDD = "yyyy/MM/dd";
    public static final String YYYYMMDD0 = "yyyyMMdd";
    public static final String YYYYMMDD1 = "yyyy-MM-dd";
    
    public static final String YYYYMMDDHHMMSSSSS = "yyyy/MM/dd HH:mm:ss SSS";
    public static final String YYYYMMDDHHMMSSAA = "yyyy/MM/dd hh:mm:ss aa";
    public static final String YYYYMMDDHHMMSS = "yyyy/MM/dd HH:mm:ss";
    public static final String YYYYMMDDHHMMSS0 = "yyyyMMddHHmmss";    
    public static final String YYYYMMDDHHMMSS1 = "yyyy-MM-dd HH:mm:ss";    
    public static final String YYYYMMDDHHMMSS2 = "yyyy-MM-dd HH:mm:ss SSS";    
    public static final String YYYYMMDDHHMMSS3 = "yyyy-MM-dd HH:mm:ss aa";
    public static final String YYYYMMDDHHMMSS4 = "yyyyMMddHHmmssSSS";

    public static final String YYMMDD = "yy/MM/dd";
    public static final String YYMMDD0 = "yyMMdd";

    public static final String YYYYMMMDD = "yyyy,MMM dd";
    public static final String YYMMMDD = "yy,MMM dd";
    
    public static final String HHMMSS = "HH:mm:ss";
    public static final String HHMM = "HH:mm";
    public static final String HHMM0 = "h:m aa";

    public static final String CHINESEDATE = "yyyy年MM月dd日";   
    public static final String CHINESEDATE0 = "yyyy年M月d日";
    public static final String CHINESEDATE1 = "yyyy年MM月dd日 HH时mm分ss秒";
    public static final String CHINESEDATE2 = "yyyy年M月d日 H时m分s秒";
    public static final String CHINESEDATE3 = "yyyy年M月d日 h时m分s秒 aa";
    public static final String CHINESEDATE4 = "yyyy年M月d日 h时m分 aa";

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
	
	/**
	 * 日期格式化
	 * 
	 * @param date 日期
	 * @param pattern 模式串
	 * @return 格式化后的字符串
	 */
	public static String formatDate(Date date, String pattern) {
		DateFormatter formatter = new DateFormatter().setPattern(pattern);
		return formatter.format(date);
	}
	
	/**
	 * 日期反格式化
	 * 
	 * @param dateString 日期字符串
	 * @param pattern 模式串
	 * @return 反格式化后的日期
	 */
	public static Date unformatDate(String dateString, String pattern) {
		DateFormatter formatter = new DateFormatter().setPattern(pattern);
		return formatter.unformat(dateString);
	}
	
	/**
	 * 数字格式化
	 * 
	 * @param number 数字
	 * @param pattern 模式串
	 * @return 格式化后的字符串
	 */
	public static String formatNumber(Number number, String pattern) {
		NumberFormatter formatter = new NumberFormatter().setPattern(pattern);
		return (String)formatter.format(number);
	}
	
	/**
	 * 数字反格式化
	 * 
	 * @param dateString 日期字符串
	 * @param pattern 模式串
	 * @return 反格式化后的日期
	 */
	public static <D> D unformatNumber(String numberString, String pattern, Class<D> clazz) {
		NumberFormatter formatter = new NumberFormatter().setPattern(pattern);
		return (D)formatter.unformat(numberString, clazz);
	}
}