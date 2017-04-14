/**
 * 
 */
package com.vitamin.db.sql.expression.handler;

import java.util.Map;

import org.dom4j.Element;

import com.vitamin.db.sql.expression.syntax.Calculator;
import com.vitamin.db.sql.expression.syntax.Ctx;

/**
 * @author huxiao
 *
 */
public class IF implements ExpressionHandler {

	private static char SPACE = 32;
	
	@Override
	public String handle(String namespace, Element element, Map<String, Object> paramMapping) {
		String testStr = element.attributeValue("test");
		//System.out.println("test : " + testStr);  
		Ctx ctx = new Ctx();
		ctx.initParam(namespace, paramMapping);
		boolean result = Calculator.cal(ctx, testStr);
		//KIT.debug("Calculator.cal(ctx, testStr) : " + result);
		if(result) {
			//如果if条件为true，则输出内部的sql
			//return SPACE + element.getStringValue() + SPACE;
			return SPACE + innerXML(element) + SPACE;
		}
		//如果if结果为false，则动态sql条件不成立，则只输出一个空格
		return String.valueOf(SPACE);
	}
	
	private String innerXML(Element element) {
		String nodeName = element.getName(); //确定节点名
        String outerXML = element.asXML(); //得到 outerXML

        //注意起始节点可能会带属性
        String innerXML = outerXML.replaceAll("^<"+nodeName+".*?>|</"+nodeName+">$", "");
        return innerXML;
	}
}
