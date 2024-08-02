module DBUtils {
	requires transitive java.sql;
	requires org.apache.commons.dbcp2;
	
	exports com.divirad.util.sql;
}