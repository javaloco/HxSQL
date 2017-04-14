/**
 * 
 */
package com.vitamin.db.sql.expression.syntax.handler;

import com.vitamin.db.sql.expression.syntax.Ctx;

/**
 * @author huxiao
 *
 */
public abstract class CommonHandler {
	
	public boolean excCal(Ctx ctx, Object leftOperand, Object rightOperand) {
		//逆波兰式中，左右操作符号要么全是字符串，要么全是boolean型
		String key = (String)leftOperand;
		String type = ctx.getParamType(key);
		Object value = ctx.getParamValue(key);
		return cal(type, value, rightOperand);
	}

	/**
	 * 注意 MyBatis 中if test 标签也只能做简单的基础类型判断
	 * @param type
	 * @param value
	 * @param rightOperand
	 * @return
	 */
	public boolean cal(String type, Object value, Object rightOperand) {
		if(value == null) {
			return calIfLeftValueIsNull(value, rightOperand);
		}
		
		String right = (String)rightOperand;
		if("null".equals(right)) {
			return calIfRightValueIsNull(value, rightOperand);
		}
		
		if("String".equals(type)) {
			return calIfTypeIsString(value, rightOperand);
		}
		else if("Integer".equals(type)) {
			return calIfTypeIsInteger(value, rightOperand);
		}
		else if("Float".equals(type)) {
			return calIfTypeIsFloat(value, rightOperand);
		}
		else if("Long".equals(type)) {
			return calIfTypeIsLong(value, rightOperand);
		}
		else if("Double".equals(type)) {
			return calIfTypeIsLong(value, rightOperand);
		}
		else if("Boolean".equals(type)) {
			return calIfTypeIsBoolean(value, rightOperand);
		}
		else if("BigDecimal".equals(type)) {
			return calIfTypeIsBigDecimal(value, rightOperand);
		}
		else if("Date".equals(type)) {
			return calIfTypeIsDate(value, rightOperand);
		}
		
		return false;
	}
	
	public abstract boolean calIfLeftValueIsNull(Object value, Object rightOperand) ; 
	
	public abstract boolean calIfRightValueIsNull(Object value, Object rightOperand) ; 
	
	public abstract boolean calIfTypeIsString(Object value, Object rightOperand);
	
	public abstract boolean calIfTypeIsInteger(Object value, Object rightOperand);
	
	public abstract boolean calIfTypeIsFloat(Object value, Object rightOperand);
	
	public abstract boolean calIfTypeIsLong(Object value, Object rightOperand);
	
	public abstract boolean calIfTypeIsDouble(Object value, Object rightOperand);
	
	public abstract boolean calIfTypeIsBoolean(Object value, Object rightOperand);
	
	public abstract boolean calIfTypeIsBigDecimal(Object value, Object rightOperand);
	
	public abstract boolean calIfTypeIsDate(Object value, Object rightOperand);
}
