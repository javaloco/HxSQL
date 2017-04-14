package com.vitamin.db.sql.expression.syntax;

import java.util.HashMap;
import java.util.Map;

import com.vitamin.db.sql.SQLConfigLoader;

public class Ctx {

	String namespace = null;
	Map<String, Object> paramMapping = new HashMap<String, Object>();
	
	public void initParam(String namespace, Map<String, Object> paramMapping) {
		this.namespace = namespace;
		this.paramMapping.putAll(paramMapping);
	}
	
	public Object getParamValue(String key) {
		return paramMapping.get(key);
	}
	
	public String getNamespace() {
		return namespace;
	}
	
	public String getParamType(String key) {
		String type = SQLConfigLoader.getInstance().getParamType(namespace + "." + key);
		if(type == null) {
			throw new IllegalStateException("The key in namespace does't exist. key with namespace is:" + namespace + "." + key);
		}
		return type;
	}
}
