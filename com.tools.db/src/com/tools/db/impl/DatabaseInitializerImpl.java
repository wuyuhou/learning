/*******************************************************************************
 * $Header: /cvsroot/PSRV/develop/core/psrv-common/com.primeton.psrv.commons/src/com/primeton/psrv/commons/impl/db/DatabaseInitializerImpl.java,v 1.1 2012/08/14 08:34:08 wuyh Exp $
 * $Revision: 1.1 $
 * $Date: 2012/08/14 08:34:08 $
 *
 *==============================================================================
 *
 * Copyright (c) 2001-2006 Primeton Technologies, Ltd.
 * All rights reserved.
 * 
 * Created on 2010-5-28
 *******************************************************************************/


package com.tools.db.impl;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

import com.tools.db.api.IDatabaseInitializer;
import com.tools.logger.api.ILogger;
import com.tools.logger.api.LoggerFactory;

/**
 * 初始化实现类
 *
 * @author yourname (mailto:yourname@primeton.com)
 */
public class DatabaseInitializerImpl implements IDatabaseInitializer {
	
	static ILogger logger = LoggerFactory.getLogger(DatabaseInitializerImpl.class);
	
	/**
	 * 初始化
	 * 
	 * @param connection 连接
	 * @param componentNames 组件名称列表
	 * @return 初始化结果，如果为空，则初始化成功，否则为错误信息
	 */
	public String initialize(Connection connection, InputStream sqlScriptIn, String encoding) {
		if (connection == null) {
			return "Connection is null!";
		}
		if (sqlScriptIn == null) {
			return "SqlScript is null!";
		}
		try {
			DatabaseMetaData metaData = connection.getMetaData();
			String databaseProductName = metaData.getDatabaseProductName();
			SqlScript script = null;
			String dbType = null;
			if (databaseProductName.toLowerCase().indexOf("db2") != -1) {
				script = new DB2Script();
				dbType = "db2";
			} else if (databaseProductName.toLowerCase().indexOf("oracle") != -1) {
				script = new OracleScript();
				dbType = "oracle";
			} else if (databaseProductName.toLowerCase().indexOf("sql server") != -1) {
				script = new SQLServerScript();
				dbType = "sqlserver";
			} else if (databaseProductName.toLowerCase().indexOf("informix") != -1) {
				script = new InformixScript();
				dbType = "informix";
			} else if (databaseProductName.toLowerCase().indexOf("adaptive server enterprise") != -1
					|| databaseProductName.toLowerCase().indexOf("sybase adaptive server iq") != -1) {// sybase
				script = new SybaseScript();
				dbType = "sybase";
			} else if (databaseProductName.toLowerCase().indexOf("mysql") != -1) {
				script = new MySqlScript();
				dbType = "mysql";
			} else {
				return "Not support this database " + databaseProductName;
			}
			
			StringBuffer messageResultBuf = new StringBuffer();
			try {
				script.run(sqlScriptIn, encoding == null ? "UTF-8" : encoding, connection, messageResultBuf);
			} catch (Throwable e) {
				logger.debug(e);
			}
			
			return messageResultBuf.toString();			
		} catch (Throwable t) {
			logger.debug(t);
			return t.getMessage();
		}
	}
}
/*
 * $Log: DatabaseInitializerImpl.java,v $
 * Revision 1.1  2012/08/14 08:34:08  wuyh
 * Update:增加数据库初始化功能
 *
 */
