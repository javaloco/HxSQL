/**
 * 
 */
package com.vitamin.db.sql.expression.handler;

import java.util.Map;

import org.dom4j.Element;

/**
 * @author huxiao
 * 
 * <foreach collection="array" item="classIds"  open="(" separator="," close=")">  
        #{classIds}  
   </foreach>  
 *
 */
public class ForEach implements ExpressionHandler {

	@Override
	public String handle(String namespace, Element element, Map<String, Object> paramMapping) {
		// TODO Auto-generated method stub
		return null;
	}

}
