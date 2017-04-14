/**
 * 
 */
package com.vitamin.db.sql.expression;

import java.util.HashMap;
import java.util.Map;

import org.dom4j.Element;

import com.vitamin.db.sql.expression.handler.ExpressionHandler;
import com.vitamin.db.sql.expression.handler.IF;

/**
 * @author huxiao
 *
 */
public class Expression {
	private final Map<String, ExpressionHandler> expressionHandlerMapping = new HashMap<String, ExpressionHandler>();
	
	public Expression() {
		expressionHandlerMapping.put("if", new IF());
	}
	
	/**
	 * 返回的数值要么是 "" 要么是 去掉最外层标签的动态语句
	 * 
	 * @param elementName
	 * @param element
	 * @param paramMapping
	 * @return
	 */
	public String handle(String namespace, String elementName, Element element, Map<String, Object> paramMapping) {
		ExpressionHandler expressionHandler = expressionHandlerMapping.get(elementName);
		if(expressionHandler == null) return null;
		return expressionHandler.handle(namespace, element, paramMapping);
	}
}
