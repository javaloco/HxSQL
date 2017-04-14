/**
 * 
 */
package com.vitamin.db.sql.expression.syntax.handler;

import java.math.BigDecimal;

import com.vitamin.db.sql.expression.syntax.Ctx;

/**
 * @author huxiao
 *
 */
public class EqualToHandler extends CommonHandler implements IOperatorHandler {

	@Override
	public boolean handle(Ctx ctx, Object leftOperand, Object rightOperand) {
		return super.excCal(ctx, leftOperand, rightOperand);		
	}

	@Override
	public boolean calIfLeftValueIsNull(Object value, Object rightOperand) {
		String right = (String)rightOperand;
		if("null".equals(right)) {
			return true;
		}
		return false;
	}
	
	@Override
	public boolean calIfRightValueIsNull(Object value, Object rightOperand) {
		if(value == null) return true;
		return false;
	}

	@Override
	public boolean calIfTypeIsString(Object value, Object rightOperand) {
		String left = (String)value;
		String right = (String)rightOperand;
		return right.equals(left);
	}

	@Override
	public boolean calIfTypeIsInteger(Object value, Object rightOperand) {
		Integer left = (Integer)value;
		Integer right = Integer.parseInt((String)rightOperand);
		return left == right;
	}

	@Override
	public boolean calIfTypeIsFloat(Object value, Object rightOperand) {
		Float left = (Float)value;
		Float right = Float.parseFloat((String)rightOperand);
		return left == right;
	}

	@Override
	public boolean calIfTypeIsLong(Object value, Object rightOperand) {
		Long left = (Long)value;
		Long right = Long.parseLong((String)rightOperand);
		return left == right;
	}

	@Override
	public boolean calIfTypeIsDouble(Object value, Object rightOperand) {
		Double left = (Double)value;
		Double right = Double.parseDouble((String)rightOperand);
		return left == right;
	}

	@Override
	public boolean calIfTypeIsBoolean(Object value, Object rightOperand) {
		Boolean left = (Boolean)value;
		Boolean right = Boolean.parseBoolean((String)rightOperand);
		return left == right;
	}

	@Override
	public boolean calIfTypeIsBigDecimal(Object value, Object rightOperand) {
		BigDecimal lv = new BigDecimal((String)value);
	    BigDecimal rv = new BigDecimal((String)rightOperand);
	    
	    if(lv.compareTo(rv) == 0) {
	    	return true;
	    }
		return false;
	}

	@Override
	public boolean calIfTypeIsDate(Object value, Object rightOperand) {
		throw new UnsupportedOperationException("equal comparison can't be used to date value.");
	}
	
	
}
