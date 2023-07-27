package com.divirad.util.sql;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class Database {
	
	private static String engine;   
	private static String hostname; 
	private static String database; 
	private static String username; 
	private static String password; 
	private static String params;	 
	private static boolean integratedSecurity;
	
	static {
		loadProperties();
	}
	
	public static void loadProperties() {
		String path = "./properties.prop";
		try {
			FileInputStream fis = new FileInputStream(path);
			Properties props = new Properties();
			props.load(fis);
			
			engine = props.getProperty("db.engine", "mysql");
			hostname = props.getProperty("db.hostname");
			database = props.getProperty("db.database");
		    username = props.getProperty("db.username", "");
		    password = props.getProperty("db.password", "");
		    integratedSecurity = Boolean.parseBoolean(props.getProperty("db.useIntegratedSecurity", "false"));
		    params = props.getProperty("params", "");
		} catch (FileNotFoundException e) {
			System.err.println("Database properties file not found");
			System.exit(1);
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}
	
	public static String getLeadingIdentifierSign() {
		return EngineSpecifics.getLeadingIdentifierSign(engine);
	}
	
	public static String getTrailingIdentifierSign() {
		return EngineSpecifics.getTrailingIdentifierSign(engine);
	}
	
	
    public interface ISetParams {
        void run(PreparedStatement ps) throws SQLException;
    }
    public interface IUseResultSet <T> {
        T run(ResultSet ps) throws SQLException;
    }

    /**
     * Executes a sql query with no parameter
     * @param sql prepared sql string
     * @param useResultSet function using the ResultSet to create the return value
     * @param <T> return Type
     * @return the return value of useResultSet
     */
    public static <T> T query(String sql, IUseResultSet<T> useResultSet) {
        return query(sql, ps -> {}, useResultSet);
    }

    /**
     * Executes a sql query
     * @param sql prepared sql string
     * @param setParams function to set the parameters of the PreparedStatement
     * @param useResultSet function using the ResultSet to create the return value
     * @param <T> return Type
     * @return the return value of useResultSet
     */
    public static <T> T query(String sql, ISetParams setParams, IUseResultSet<T> useResultSet) {
    	try (Connection con = ConnectionFactory.createConnection(engine, hostname, database, integratedSecurity, username, password, params)) {
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                setParams.run(ps);
                try (ResultSet rs = ps.executeQuery()) {
                    return useResultSet.run(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Executes a sql command with no parameter
     * @param sql prepared sql string
     * 
     * @return count of rows affected
     */
    public static int execute(String sql) {
        return execute(sql, ps -> {});
    }

    /**
     * Executes a sql command
     * @param sql prepared sql string
     * @param setParams function to set the parameters of the PreparedStatement
     * 
     * @return count of rows affected
     */
    public static int execute(String sql, ISetParams setParams) {
    	int updateCount = -1;
    	try (Connection con = ConnectionFactory.createConnection(engine, hostname, database, integratedSecurity, username, password, params)) {
        		try (PreparedStatement ps = con.prepareStatement(sql)) {
                setParams.run(ps);
                ps.execute();
                updateCount = ps.getUpdateCount();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } 
    	return updateCount;
    }

    public static int getLastID() {
        return query("SELECT LAST_INSERT_ID();", rs -> rs != null && rs.next() ? rs.getInt(1) : null);
    }
}