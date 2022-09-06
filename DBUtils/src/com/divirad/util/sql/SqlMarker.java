package com.divirad.util.sql;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class SqlMarker {
    private SqlMarker() {} // no instance allowed

    /**
     * Marks class as table view.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface TableView {
        /**
         *  table name
         */
        String tableName();
        /**
         *  true if all columns are in this class as field
         */
        boolean isWholeTable();
    }

    /**
     * Marks a field as primary key.
     * Field will be used in WHERE conditions
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface PrimaryKey {}

    /**
     * Marks a field as automatic value like auto_increment.
     * Field will not be used in insert commands
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface AutomaticValue {}

    /**
     * Marks a field as ignored.
     * Field will not be used in any sql commands
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface IgnoreField {}
}