package com.divirad.util.sql;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.sql.ResultSet;
import java.util.ArrayList;

/**
 * Exact same thing as {@link DaoBase} just with public CRUD methods
 * @author nkepperadm
 *
 * @param <T>
 */
public abstract class OpenDaoBase<T> {

	protected Class<T> cls;
    protected Constructor<T> constructor;
    
    public OpenDaoBase(Class<T> cls) {
    	this.cls = cls;
    	
    	if (!Modifier.isFinal(cls.getModifiers()))
            throw new IllegalArgumentException("Can't use class: must be final");
        if (cls.getSuperclass() != Object.class)
            throw new IllegalArgumentException("Can't use class: must not extend another class");
    	
    	try {
            this.constructor = cls.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Can't use class: No default constructor");
        }
    }
    
    protected abstract T convFirstInResultSet(ResultSet rs) throws Exception;
    
    protected abstract ArrayList<T> convAllInResultSet(ResultSet rs) throws Exception;
    
    protected abstract T convertCurrentFromResultSet(ResultSet rs) throws Exception;
    
    public abstract T select(T data) throws Exception;
    
    public abstract ArrayList<T> selectAll() throws Exception;
    
    public abstract void insert(T data) throws Exception;
    
    public abstract void insertAll(ArrayList<T> data) throws Exception;
    
    public abstract void update(T data) throws Exception;
    
    public abstract void replace(T data) throws Exception;
    
    public abstract void delete(T data) throws Exception;
}
