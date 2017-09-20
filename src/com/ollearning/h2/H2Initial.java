package com.ollearning.h2;

import java.sql.SQLException;

import org.h2.tools.Server;

public class H2Initial {

	private static Server server = null;

	public static void startup() {
		try {
			// default port is 8082
			server = Server.createTcpServer().start();
		} catch (SQLException e) {
			System.out.println("h2 startup error：" + e.toString());
			e.printStackTrace();
			throw new RuntimeException(e);
		}

	}

}
