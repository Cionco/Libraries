package com.divirad.util.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {
	private ConnectionFactory() {}
	
	public static Connection createConnection(String engine, String hostname, String database, 
			String username, String password, String params) throws SQLException {
		return createConnection(engine, hostname, database, false, username, password, params);
	}
	
	public static Connection createConnection(String engine, String hostname, String database, 
												boolean useIntegratedSecurity, String username, 
												String password, String params) throws SQLException {
		if(engine.equals("mysql")) {
			if(useIntegratedSecurity) return DriverManager.getConnection(getBaseConnectionString(engine, hostname)
					+ "/" + database
					+ "?" + "IntegratedSecurity=yes" + params);
			else return DriverManager.getConnection(getBaseConnectionString(engine, hostname) 
					+ "/" + database 
					+ "?" + params, username, password);
		} else if(engine.equals("sqlserver")) {
			if(useIntegratedSecurity) return DriverManager.getConnection(getBaseConnectionString(engine, hostname)
					+ ";database=" + database 
					+ ";integratedSecurity=true"
					+ params);
			else return DriverManager.getConnection(getBaseConnectionString(engine, hostname)
					+ ";database=" + database 
					+ ";user=" + username 
					+ ";password=" + password
					+ params);
		} else throw new IllegalArgumentException("Not defined for engine " + engine);
	}
	
	private static String getBaseConnectionString(String engine, String hostname) {
		return "jdbc:" + engine + "://" + hostname;
	}
}