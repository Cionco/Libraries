package com.divirad.util.sql;

public class EngineSpecifics {

	public static String getLeadingIdentifierSign(String engine) {
		if(engine.equals("mysql")) return "`";
		else if(engine.equals("sqlserver")) return "";
		else throw new IllegalArgumentException("Not defined for engine " + engine);
	}
	
	public static String getTrailingIdentifierSign(String engine) {
		if(engine.equals("mysql")) return "`";
		else if(engine.equals("sqlserver")) return "";
		else throw new IllegalArgumentException("Not defined for engine " + engine);
	}
}
