/*******************************************************************************
 * $Header: /cvsroot/PSRV/develop/core/psrv-common/com.primeton.psrv.commons/src/com/primeton/psrv/commons/spi/datasource/IC3P0DataSourceConfigNames.java,v 1.3 2012/08/10 05:04:11 wangwb Exp $
 * $Revision: 1.3 $
 * $Date: 2012/08/10 05:04:11 $
 *
 *==============================================================================
 *
 * Copyright (c) 2001-2012 Primeton Technologies, Ltd.
 * All rights reserved.
 * 
 * Created on 2012-8-9
 *******************************************************************************/

package com.tools.db.api;

/**
 * TODO 此处填写 class 信息
 *
 * @author wangwb (mailto:wangwb@primeton.com)
 */

public interface IC3P0DataSourceConfigNames {
	public static final String INITIAL_POOL_SIZE = "c3p0.initialPoolSize";
	public static final String MIN_POOL_SIZE = "c3p0.minPoolSize";
	public static final String MAX_POOL_SIZE = "c3p0.maxPoolSize";
	public static final String IDLE_CONNECTION_TEST_PERIOD = "c3p0.idleConnectionTestPeriod";
	public static final String MAX_IDLE_TIME = "c3p0.maxIdleTime";
	public static final String PROPERTY_CYCLE = "c3p0.propertyCycle";
	public static final String MAX_STATEMENTS = "c3p0.maxStatements";
	public static final String MAX_STATEMENTS_PER_CONNECTION = "c3p0.maxStatementsPerConnection";
	public static final String CHECKOUT_TIMEOUT = "c3p0.checkoutTimeout";
	public static final String ACQUIRE_INCREMENT = "c3p0.acquireIncrement";
	public static final String ACQUIRE_RETRY_ATTEMPTS = "c3p0.acquireRetryAttempts";
	public static final String ACQUIRE_RETRY_DELAY = "c3p0.acquireRetryDelay";
	public static final String BREAK_AFTER_ACQUIRE_FAILURE = "c3p0.breakAfterAcquireFailure";
	public static final String USES_TRADITIONAL_REFLECTIVE_PROXIES = "c3p0.usesTraditionalReflectiveProxies";
	public static final String TEST_CONNECTION_ON_CHECKOUT = "c3p0.testConnectionOnCheckout";
	public static final String TEST_CONNECTION_ON_CHECKIN = "c3p0.testConnectionOnCheckin";
	public static final String CONNECTION_TESTER_CLASS_NAME = "c3p0.connectionTesterClassName";
	public static final String AUTOMATIC_TEST_TABLE = "c3p0.automaticTestTable";
	public static final String AUTO_COMMIT_ON_CLOSE = "c3p0.autoCommitOnClose";
	public static final String FORCE_IGNORE_UNRESOLVED_TRANSACTIONS = "c3p0.forceIgnoreUnresolvedTransactions";
	public static final String NUM_HELPER_THREADS = "c3p0.numHelperThreads";
	public static final String PREFERRED_TEST_QUERY = "c3p0.preferredTestQuery";
	public static final String FACTORY_CLASS_LOCATION = "c3p0.factoryClassLocation";
	public static final String DRIVER_CLASS = "c3p0.driverClass";
	public static final String JDBC_URL = "c3p0.jdbcUrl";
	public static final String USER = "c3p0.user";
	public static final String PASSWORD = "c3p0.password";
}