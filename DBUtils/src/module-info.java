module DBUtils {
	requires transitive java.sql;
	requires org.apache.commons.dbcp2;
	requires org.slf4j;
	requires transitive java.desktop;
	
	exports com.divirad.util.sql;
}