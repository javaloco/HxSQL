/**
 * 
 */
package com.vitamin.db.sql;

import java.util.Map;

/**
 * @author huxiao
 *
 */
public class HxSQL {

	public static Conclusion getSql(String namespace, String id, Map<String, Object> argsMap) {
		return SQLConfigLoader.getInstance().getSql(namespace, id, argsMap);
	}
	
	public static String getSql(String namespace, String id) {
		return SQLConfigLoader.getInstance().getSql(namespace, id);
	}
}
