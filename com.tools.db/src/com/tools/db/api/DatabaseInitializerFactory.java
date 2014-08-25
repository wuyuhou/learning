
package com.tools.db.api;

import com.tools.db.impl.DatabaseInitializerImpl;


/**
 * 数据库初始化接口工厂
 *
 * @author yourname (mailto:yourname@primeton.com)
 */
public class DatabaseInitializerFactory {
	public static IDatabaseInitializer getDatabaseInitializer() {
		return new DatabaseInitializerImpl();
	}
}
/*
 * 
 * $Log: DatabaseInitializerFactory.java,v $
 * Revision 1.1  2012/08/14 08:34:08  wuyh
 * Update:增加数据库初始化功能
 *
 * 
 */
