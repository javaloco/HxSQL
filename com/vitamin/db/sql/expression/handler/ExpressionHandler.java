/**
 * 
 */
package com.vitamin.db.sql.expression.handler;

import java.util.Map;

import org.dom4j.Element;

/**
 * @author huxiao
 * 
 * 
 *
 */
public interface ExpressionHandler {
	
	public String handle(String namespace, Element element, Map<String, Object> paramMapping) ;

}

