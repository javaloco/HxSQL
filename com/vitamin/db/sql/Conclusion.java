/**
 * 
 */
package com.vitamin.db.sql;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author huxiao
 *
 */
public class Conclusion {

	private String sql = null;
	private List<String> argsKeys = null;
	private List<Object> argsValuesBeta = null ;
	
	public String getSql() {
		return sql;
	}
	public void setSql(String sql) {
		this.sql = sql;
	}
	public List<String> getArgsKeys() {
		return argsKeys;
	}
	public void setArgsKeys(List<String> args) {
		this.argsKeys = args;
	}
	
	public Object[] getArgsValues(Map<String, Object> argsMap) {
		List<String> argsKeys = this.getArgsKeys();
		List<Object> argsValues = new ArrayList<Object>();
		for(String key : argsKeys) {
			Object v = argsMap.get(key);
			if(v != null) {
				if(v instanceof List<?>) {
					// 针对 in 语句
					List<?> vArray = (List<?>)v;
					for(Object a : vArray) {
						argsValues.add(a);
					}
				} 
				else {
					argsValues.add(v);
				}
			}
		}
		
		if(argsValues.isEmpty()) {
			return new Object[0];
		}
		
		return argsValues.toArray();/*如果list为空List，则toArray出来变成[null]*/
	}
	
	public Object[] getArgsValuesBeta() {
		if(argsValuesBeta.isEmpty()) {
			return new Object[0];
		}
		
		return argsValuesBeta.toArray(); /*如果list为空List，则toArray出来变成[null]*/
	}
	
	public void setArgsValuesBeta(List<Object> argsValuesBeta) {
		this.argsValuesBeta = argsValuesBeta;
	}
}
