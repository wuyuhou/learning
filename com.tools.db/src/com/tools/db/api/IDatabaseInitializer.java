
package com.tools.db.api;

import java.io.InputStream;
import java.sql.Connection;

/**
 * 数据库初始化接口
 *
 * @author yourname (mailto:yourname@primeton.com)
 */
public interface IDatabaseInitializer {
	/**
	 * 初始化
	 * 
	 * @param connection 连接
	 * @param sqlScriptIn sql脚本流
	 * param encoding sql脚本编码
	 * @return 初始化结果，如果为空，则初始化成功，否则为错误信息
	 */
	String initialize(Connection connection, InputStream sqlScriptIn, String encoding);
}
/*
 * 
 * $Log: IDatabaseInitializer.java,v $
 * Revision 1.1  2012/08/14 08:34:08  wuyh
 * Update:增加数据库初始化功能
 *
 * 
 */
