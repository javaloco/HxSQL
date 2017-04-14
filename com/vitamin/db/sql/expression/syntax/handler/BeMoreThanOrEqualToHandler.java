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
public class BeMoreThanOrEqualToHandler extends CommonHandler implements IOperatorHandler {
	
	@Override
	public boolean handle(Ctx ctx, Object leftOperand, Object rightOperand) {
		return super.excCal(ctx, leftOperand, rightOperand);
	}

	@Override
	public boolean calIfLeftValueIsNull(Object value, Object rightOperand) {
		return false;
	}
	
	@Override
	public boolean calIfRightValueIsNull(Object value, Object rightOperand) {
		return false;
	}

	@Override
	public boolean calIfTypeIsString(Object value, Object rightOperand) {
		throw new UnsupportedOperationException("More than or equal comparison can't be used to string value.");
	}

	@Override
	public boolean calIfTypeIsInteger(Object value, Object rightOperand) {
		Integer right = Integer.parseInt((String)rightOperand);
		Integer left = (Integer) value;
		return left >= right;
	}

	@Override
	public boolean calIfTypeIsFloat(Object value, Object rightOperand) {
		Float right = Float.parseFloat((String)rightOperand);
		Float left = (Float) value;
		return left >= right;
	}

	@Override
	public boolean calIfTypeIsLong(Object value, Object rightOperand) {
		Long right = Long.parseLong((String)rightOperand);
		Long left = (Long) value;
		return left >= right;
	}

	@Override
	public boolean calIfTypeIsDouble(Object value, Object rightOperand) {
		Double right = Double.parseDouble((String)rightOperand);
		Double left = (Double) value;
		return left >= right;
	}

	@Override
	public boolean calIfTypeIsBoolean(Object value, Object rightOperand) {
		throw new UnsupportedOperationException("More than or equal comparison can't be used to boolean value.");
	}

	@Override
	public boolean calIfTypeIsBigDecimal(Object value, Object rightOperand) {
		BigDecimal lv = new BigDecimal((String)value);
	    BigDecimal rv = new BigDecimal((String)rightOperand);
	    if(lv.compareTo(rv) == 1) {
	    	return true;
	    }
	    
	    if(lv.compareTo(rv) == 0) {
	    	return true;
	    }
		return false;
	}

	@Override
	public boolean calIfTypeIsDate(Object value, Object rightOperand) {
		throw new UnsupportedOperationException("More than or equal comparison can't be used to date value.");
	}



}
