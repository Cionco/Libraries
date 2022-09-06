package com.divirad.util.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {
	private ConnectionFactory() {}
	
	public static Connection createConnection(String engine, String hostname, String database, String username, String password, String params) throws SQLException {
		if(engine.equals("mysql")) {
			return DriverManager.getConnection("jdbc:" + engine + "://" + hostname + "/" + database + "?" + params, username, password);
		} else if(engine.equals("sqlserver")) {
			return DriverManager.getConnection("jdbc:" + engine + "://" + hostname 
					+ ";database=" + database 
					+ ";user=" + username 
					+ ";password=" + password
					+ params);
		} else throw new IllegalArgumentException("Not defined for engine " + engine);
	}
}