/*******************************************************************************
 * $Header: /cvsroot/PSRV/develop/core/psrv-common/com.primeton.psrv.commons/src/com/primeton/psrv/commons/spi/datasource/C3P0DataSourceFactory.java,v 1.3 2012/08/09 02:44:55 wangwb Exp $
 * $Revision: 1.3 $
 * $Date: 2012/08/09 02:44:55 $
 *
 *==============================================================================
 *
 * Copyright (c) 2001-2012 Primeton Technologies, Ltd.
 * All rights reserved.
 * 
 * Created on 2012-7-23
 *******************************************************************************/

package com.tools.db.api;

import java.util.Properties;

import javax.sql.DataSource;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DataSources;
import com.mchange.v2.c3p0.PoolConfig;
import com.tools.logger.api.ILogger;
import com.tools.logger.api.LoggerFactory;

/**
 * TODO 此处填写 class 信息
 *
 * @author wangwb (mailto:wangwb@primeton.com)
 */

public final class C3P0DataSourceFactory implements IC3P0DataSourceConfigNames {
	private static final ILogger logger = LoggerFactory.getLogger(C3P0DataSourceFactory.class);

	private C3P0DataSourceFactory() {

	}
	
	/**
	 * 创建数据源.<br>
	 * 
	 * @author properties 连接池属性
	 * @return 数据源
	 */
	public static DataSource createDataSource(Properties properties) {
		if (properties==null) {
			throw new NullPointerException("The properties is null.");
		}
		try {
			PoolConfig poolConfig = new PoolConfig(properties);
			ComboPooledDataSource ds = new ComboPooledDataSource();
			ds.setDriverClass(properties.getProperty(DRIVER_CLASS));
			ds.setJdbcUrl(properties.getProperty(JDBC_URL));
			ds.setUser(properties.getProperty(USER));
			ds.setPassword(properties.getProperty(PASSWORD));
			ds.setAcquireIncrement(poolConfig.getAcquireIncrement());
			ds.setAcquireRetryAttempts(poolConfig.getAcquireRetryAttempts());
			ds.setAcquireRetryDelay(poolConfig.getAcquireRetryDelay());
			ds.setCheckoutTimeout(poolConfig.getCheckoutTimeout());
			ds.setConnectionTesterClassName(poolConfig.getConnectionTesterClassName());
			ds.setFactoryClassLocation(poolConfig.getFactoryClassLocation());
			ds.setIdleConnectionTestPeriod(poolConfig.getIdleConnectionTestPeriod());
			ds.setInitialPoolSize(poolConfig.getInitialPoolSize());
			ds.setMaxIdleTime(poolConfig.getMaxIdleTime());
			ds.setMaxPoolSize(poolConfig.getMaxPoolSize());
			ds.setMaxStatements(poolConfig.getMaxStatements());
			ds.setMaxStatementsPerConnection(poolConfig.getMaxStatementsPerConnection());
			ds.setMinPoolSize(poolConfig.getMinPoolSize());
			ds.setNumHelperThreads(poolConfig.getNumHelperThreads());
			ds.setPreferredTestQuery(poolConfig.getPreferredTestQuery());
			ds.setPropertyCycle(poolConfig.getPropertyCycle());
			ds.setAutoCommitOnClose(poolConfig.isAutoCommitOnClose());
			ds.setBreakAfterAcquireFailure(poolConfig.isBreakAfterAcquireFailure());
			ds.setForceIgnoreUnresolvedTransactions(poolConfig.isForceIgnoreUnresolvedTransactions());
			ds.setTestConnectionOnCheckin(poolConfig.isTestConnectionOnCheckin());
			ds.setTestConnectionOnCheckout(poolConfig.isTestConnectionOnCheckout());
			ds.setUsesTraditionalReflectiveProxies(poolConfig.isUsesTraditionalReflectiveProxies());
			return ds;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
		
	}

	/**
	 * 销毁数据源.<br>
	 * 
	 * @param dataSource 数据源
	 */
	public static void destroyDataSource(DataSource datasource) {
		if (datasource == null) {
			return;
		}
		try {
			DataSources.destroy(datasource);
		} catch (Throwable e) {
			logger.error(e);
		}
	}
}