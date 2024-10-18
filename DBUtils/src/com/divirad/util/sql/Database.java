package com.divirad.util.sql;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Wrapper for all operations done on a database.
 * Loads database connection properties from a properties file in the application directory <code>properties.prop</code>
 * 
 * The properties needed are
 * - engine
 * - hostname
 * - database
 * - username
 * - password
 * - useIntegratedSecurity (if this is set, no need for username & password)
 * - params (any additional parameters that should be added to the end of the connection string)
 * Each of these properties uses the prefix <code>db.</code>
 * 
 * Multiple database profiles can be created by specifying a <code>profiles</code> property which lists the names of the profiles split by
 * a single comma (no space). 
 * Then, for each profile, instead of just the <code>db.</code> prefix, use <code>db.profilename.</code> prefix
 */
public class Database {
	
	private static class DBProfile {
		private String engine;   
		private String hostname; 
		private String database; 
		private String username; 
		private String password; 
		private String params;	 
		private boolean integratedSecurity;		
	}
	
	private static Map<String, DBProfile> dbprofiles = new HashMap<>();	
	private static String activeProfileName = "default";
	
	private static DBCPDataSource ds;
	
	private static List<PropertyChangeListener> listenerList = new ArrayList<>();
	
	static Logger log = LoggerFactory.getLogger(Database.class);
	
	static {
		loadProperties();
	}
	
	public static void loadProperties() {
		String path = "./properties.prop";
		try {
			FileInputStream fis = new FileInputStream(path);
			Properties props = new Properties();
			props.load(fis);
			
			String profiles = props.getProperty("db.profiles", null);
			
			if(profiles == null) {
				DBProfile p = new DBProfile();
				p.engine = props.getProperty("db.engine", "mysql");
				p.hostname = props.getProperty("db.hostname");
				p.database = props.getProperty("db.database");
			    p.username = props.getProperty("db.username", "");
			    p.password = props.getProperty("db.password", "");
			    p.integratedSecurity = Boolean.parseBoolean(props.getProperty("db.useIntegratedSecurity", "false"));
			    p.params = props.getProperty("db.params", "");
			    dbprofiles.put("default", p);
			} else {
				for(String pName : profiles.split(","))
					dbprofiles.put(pName, readProfile(props, pName));
			}
		} catch (FileNotFoundException e) {
			System.err.println("Database properties file not found");
			System.exit(1);
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}
	
	public static Set<String> getProfileNames() {
		return dbprofiles.keySet();
	}
	
	public static String getActiveProfileName() {
		return activeProfileName;
	}
	
	public static void addProfileListener(PropertyChangeListener l) {
		listenerList.add(l);
	}
	
	public static void removeProfileListener(PropertyChangeListener l) {
		listenerList.remove(l);
	}
	
	private static void fireProfileChanged(String oldProfileName, String newProfileName) {
		List<PropertyChangeListener> copy = new ArrayList<>(listenerList);
		
		PropertyChangeEvent e = new PropertyChangeEvent(null, "activeProfile", oldProfileName, newProfileName);
		
		for(PropertyChangeListener l : copy)
			l.propertyChange(e);
	}
	
	/**
	 * Reads a database profile set from a properties object
	 * @param props the properties object
	 * @param name the name of the database profile
	 * @return the new profile
	 */
	private static DBProfile readProfile(Properties props, String name) {
		DBProfile p = new DBProfile();
		p.engine = props.getProperty("db." + name + ".engine", "mysql");
		p.hostname = props.getProperty("db." + name + ".hostname");
		p.database = props.getProperty("db." + name + ".database");
	    p.username = props.getProperty("db." + name + ".username", "");
	    p.password = props.getProperty("db." + name + ".password", "");
	    p.integratedSecurity = Boolean.parseBoolean(props.getProperty("db." + name + ".useIntegratedSecurity", "false"));
	    p.params = props.getProperty("db." + name + ".params", "");
	    return p;
	}
	
	/**
	 * Loads a profile from the list <code>dbprofiles</code>
	 * 
	 * @param id the id in the list
	 */
	public static void loadProfile(String profileName) {
		try {
			if(ds != null) ds.shutdown();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		String oldProfileName = activeProfileName;
		activeProfileName = profileName;
		ds = new DBCPDataSource(dbprofiles.get(profileName));
		fireProfileChanged(oldProfileName, activeProfileName);
	}
	
	public static void openTransaction() {
		ds.openTransaction();
	}
	
	public static void commit() {
		ds.commitTransaction();
	}
	
	public static void rollback() {
		ds.rollbackTransaction();
	}
	
	public static void shutdown() throws SQLException {
		ds.shutdown();
	}
	
	public static String getLeadingIdentifierSign() {
		return EngineSpecifics.getLeadingIdentifierSign(dbprofiles.get(activeProfileName).engine);
	}
	
	public static String getTrailingIdentifierSign() {
		return EngineSpecifics.getTrailingIdentifierSign(dbprofiles.get(activeProfileName).engine);
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
    	Connection con = null;
    	try {
    		con = ds.getConnection();
    		try(PreparedStatement ps = con.prepareStatement(sql)) {
    			log.debug("Statement prepared");
    			setParams.run(ps);
    			try(ResultSet rs = ps.executeQuery()) {
    				log.debug("Execute");
    				return useResultSet.run(rs);
    			}
    		}
    	} catch(SQLException e) {
    		e.printStackTrace();
    		return null;
    	} finally {
    		try {
    			if(con.getAutoCommit())
    				con.close();
    		} catch(SQLException e) {
    			e.printStackTrace();
    		}
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
    	Connection con = null;
    	try {
    		con = ds.getConnection();
    		try(PreparedStatement ps = con.prepareStatement(sql)) {
    			log.debug("Statement prepared");
    			setParams.run(ps);
    			log.debug("Parameters set");
    			ps.execute();
    			log.debug("Executed");
    			updateCount = ps.getUpdateCount();
    		}
    	} catch(SQLException e) {
    		e.printStackTrace();
    	} finally {
    		try {
    			if(con.getAutoCommit())
    				con.close();
    		} catch(SQLException e) {
    			e.printStackTrace();
    		}
    	}
    	return updateCount;
    }

    public static int getLastID() {
        return query("SELECT LAST_INSERT_ID();", rs -> rs != null && rs.next() ? rs.getInt(1) : null);
    }
    
    private static class DBCPDataSource {
    	
    	private BasicDataSource ds;
    	
    	private Connection activeTransaction; 
    	
    	public DBCPDataSource(DBProfile p) {
    		ds = new BasicDataSource();
    		ds.setUrl(getConnectionString(p.engine, p.hostname, p.database, p.integratedSecurity, p.params));
    		if(!p.integratedSecurity) {
    			ds.setUsername(p.username);
    			ds.setPassword(p.password);
    		}
    		ds.setMinIdle(5);
    		ds.setMaxIdle(10);
    		ds.setMaxOpenPreparedStatements(100);
    	}    	
    	
    	public Connection getConnection() throws SQLException {
    		if(activeTransaction != null) {
    			log.debug("Using active transaction connection");
    			return activeTransaction;
    		}
    		log.debug("Establishing \"new\" connection");
    		return ds.getConnection();
    	}
    	
    	public void openTransaction() {
    		try {
    			activeTransaction = ds.getConnection();
    			activeTransaction.setAutoCommit(false);;
    		} catch(SQLException e) {
    			e.printStackTrace();
    		}
    	}
    	
    	public void commitTransaction() {
    		if(activeTransaction == null)
    			throw new IllegalStateException("Can't commit transaction when no transaction is open");
    		try {
    			activeTransaction.commit();
    			activeTransaction.setAutoCommit(true);
    			activeTransaction.close();
    			activeTransaction = null;
    		} catch(SQLException e) {
    			try {
    				activeTransaction.rollback();
    			} catch(SQLException e1) {
    				e1.printStackTrace();
    			}
    			e.printStackTrace();
    		}
    	}
    	
    	public void rollbackTransaction() {
    		if(activeTransaction == null)
    			throw new IllegalStateException("Can't rollback transaction when no transaction is open");
    		try {
    			activeTransaction.rollback();
    			activeTransaction.setAutoCommit(true);
    			activeTransaction.close();
    			activeTransaction = null;
    		} catch(SQLException e) {
    			e.printStackTrace();
    		}
    	}
    	
    	private String getConnectionString(String engine, String hostname, String database, 
				boolean useIntegratedSecurity, String params) {
    		if(engine.equals("mysql")) {
    			if(useIntegratedSecurity) return getBaseConnectionString(engine, hostname)
    					+ "/" + database
    					+ "?" + "IntegratedSecurity=yes" + params;
    			else return getBaseConnectionString(engine, hostname) 
    					+ "/" + database 
    					+ "?" + params;
    		} else if(engine.equals("sqlserver")) {
    			if(useIntegratedSecurity) 
    				return getBaseConnectionString(engine, hostname)
    					+ ";database=" + database 
    					+ ";integratedSecurity=true"
    					+ params;
    			else return getBaseConnectionString(engine, hostname)
    					+ ";database=" + database 
    					+ params;
    		} else throw new IllegalArgumentException("Not defined for engine " + engine);
    	}
    	
    	
    	private String getBaseConnectionString(String engine, String hostname) {
    		return "jdbc:" + engine + "://" + hostname;
    	}
    	
    	public void shutdown() throws SQLException {
    		ds.close();
    	}
    }
}