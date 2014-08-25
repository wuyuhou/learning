package com.tools.db.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.tools.logger.api.ILogger;
import com.tools.logger.api.LoggerFactory;

public abstract class SqlScript {

	static ILogger logger = LoggerFactory.getLogger(SqlScript.class);

	public void run(InputStream stream, String charset, Connection conn, StringBuffer resultBuf) {

		String sqlStatement = null;
		Statement stmt = null;
		try {
			BufferedReader r = new BufferedReader(new InputStreamReader(stream, charset));
			
			stmt = conn.createStatement();

			boolean isFinish = false;// 是否完成标志位
			while (!isFinish) {
				sqlStatement = getNextStatement(r);

				if (sqlStatement != null) {
					// SQL 长度是否非法
					if (sqlStatement.length() < 5) {
						continue;
					}

					// SQL 是否为空
					if (sqlStatement.length() == 0) {
						continue;
					}

					execStmt(sqlStatement, stmt, resultBuf);
				} else {
					isFinish = true;
				}
			}
		} catch (Exception e) {
			if (logger.isWarnEnabled()) {
				logger.warn(null, e);
			}
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException sqle) {
				}
			}
		}
	}

	private void execStmt(String sql, Statement stmt, StringBuffer resultBuf) {
		try {
			stmt.execute(sql);
		} catch (SQLException sqle) {
			if (sql != null && sql.trim().toUpperCase().startsWith("DROP")) {
				return;
			}
			resultBuf.append(sqle.getMessage()).append("\n");

			if (logger.isWarnEnabled()) {
				logger.warn("SQL Error:[" + sql + "]" + sqle.getMessage(), sqle);
			}
			return;
		}
	}

	protected abstract String getNextStatement(BufferedReader r) throws IOException;

}
