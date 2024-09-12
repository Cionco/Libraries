module DBUtils {
	requires transitive java.sql;
	requires org.apache.commons.dbcp2;
	requires org.slf4j;
	
	exports com.divirad.util.sql;
}