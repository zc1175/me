package com.ollearning.h2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.h2.jdbcx.JdbcConnectionPool;

public class H2Util {

	private static String jdbcUrl = "jdbc:h2:tcp://localhost/mem:sysdb";
	private static String userName = "system";
	private static String pwd = "Flzx3000$1sYhL9t";

	private static JdbcConnectionPool pool;

	static {
		try {
			pool = JdbcConnectionPool.create(jdbcUrl, userName, pwd);
		} catch (Exception ex) {
			System.err.println("h2 connection pool create error: "
					+ ex.getMessage());
			ex.printStackTrace();
		}
	}

	private static Connection getConnection() throws SQLException {
		return pool.getConnection();
	}

	public static void execute(String sql) {
		Connection conn = null;
		Statement stmt = null;
		try {
			conn = getConnection();
			stmt = conn.createStatement();
			stmt.execute(sql);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (null != conn) {
				try {
					conn.close();
				} catch (SQLException e) {
					;
				}
			}
		}
	}

	public static int executeUpdate(String sql, Object[] params) {
		PreparedStatement pstmt = null;
		Connection conn = null;
		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(sql);
			if (null != params && params.length > 0) {
				int index = 1;
				for (Object param : params) {
					pstmt.setObject(index, param);
					index++;
				}
			}
			return pstmt.executeUpdate();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (null != conn) {
				try {
					conn.close();
				} catch (SQLException e) {
					;
				}
			}
		}
		return 0;
	}

	public static List<List<Object>> executeQuery(String sql, String[] columns,
			Object[] params) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<List<Object>> rows = new ArrayList<List<Object>>();
		try {
			conn = getConnection();
			pstmt = conn.prepareStatement(sql);
			int index = 1;
			for (Object param : params) {
				pstmt.setObject(index, param);
				index++;
			}
			rs = pstmt.executeQuery();
			while (rs.next()) {
				List<Object> row = new ArrayList<Object>();
				for (int i = 0; i < columns.length; i++) {
					Object obj = rs.getObject(i + 1);
					row.add(obj);
				}
				rows.add(row);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (null != rs) {
				try {
					rs.close();
				} catch (SQLException e) {
					;
				}
			}
			if (null != conn) {
				try {
					conn.close();
				} catch (SQLException e) {
					;
				}
			}
		}
		return rows;
	}

}
