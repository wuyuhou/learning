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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日期数据类型的格式化器<br>
 * 
 * 日期和时间格式由日期和时间模式 字符串指定。
 * 
 * 在日期和时间模式字符串中，未加引号的字母 'A' 到 'Z' 和 'a' 到 'z' 被解释为模式字母，
 * 用来表示日期或时间字符串元素。文本可以使用单引号 (') 引起来，以免进行解释。"''" 表示单引号。
 * 
 * 所有其他字符均不解释；只是在格式化时将它们简单复制到输出字符串，或者在分析时与输入字符串进行匹配。
 * 
 * 格式说明如下：（模式字母通常是重复的，其数量确定其精确表示）
 * <pre>
 * 
    字母     日期或时间元素              表示                  示例                                             
    G        Era 标志符                Text                  AD                                                
    y        年                       Year                  1996; 96                                          
    M        年中的月份                 Month                 July; Jul; 07                                     
    w        年中的周数                 Number                27                                                
    W        月份中的周数               Number                2                                                
    D        年中的天数                 Number                189                                               
    d        月份中的天数               Number                10                                               
    F        月份中的星期               Number                2                                                
    E        星期中的天数               Text                  Tuesday; Tue                                     
    a        Am/pm 标记               Text                  PM                                                
    H        一天中的小时数（0-23）       Number                0                                                
    k        一天中的小时数（1-24）       Number                24                                               
    K        am/pm 中的小时数（0-11）    Number                0                                                
    h        am/pm 中的小时数（1-12）    Number                12                                               
    m        小时中的分钟数             Number                30                                               
    s        分钟中的秒数               Number                55                                               
    S        毫秒数                    Number                978                                              
    z        时区                     General time zone     Pacific Standard Time; PST; GMT-08:00            
    Z        时区                     RFC 822 time zone     -0800    
 *	
 * </pre>
 *
 * @author wuyuhou
 *
 */
@SuppressWarnings("unchecked")
public class DateFormatter extends AbstractFormatter<Date, String> {
	
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
    
	
    //模式，比如yyyy-MM-dd HH:mm:ss SSS
	private String pattern = null;
	
	private SimpleDateFormat dateFormat = null;
	
	public DateFormatter() {
		super();
		this.dateFormat = new SimpleDateFormat();
	}
	
	public DateFormatter(AbstractFormatter formatter) {
		super(formatter);
		this.dateFormat = new SimpleDateFormat();
	}

	public String getPattern() {		
		return pattern;
	}

	public DateFormatter setPattern(String pattern) {
		if (pattern == null || pattern.trim().length() == 0) {
			throw new IllegalArgumentException("pattern is null!");
		}
		this.pattern = pattern.trim();
		dateFormat.applyPattern(this.pattern);
		return this;
	}

	@Override
	protected void doCheck() {
		if (pattern == null || pattern.trim().length() == 0) {
			throw new FormatRuntimeException("Pattern is null, please set it!");
		}
	}

	@Override
	protected String doFormat(Date data) {
		return dateFormat.format(data);
	}

	@Override
	protected Date doUnformat(String data) {
		try {
			return dateFormat.parse(data);
		} catch (ParseException e) {
			throw new FormatRuntimeException("[{0}] cannot unformat by pattern[{1}]!", new Object[]{data, pattern}, e);
		}
	}
}