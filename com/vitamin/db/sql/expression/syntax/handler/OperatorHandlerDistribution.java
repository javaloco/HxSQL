/**
 * 
 */
package com.vitamin.db.sql.expression.syntax.handler;

import java.util.HashMap;
import java.util.Map;

import com.vitamin.db.sql.expression.syntax.Ctx;
import com.vitamin.db.sql.expression.syntax.Operator;

/**
 * @author huxiao
 *
 */
public class OperatorHandlerDistribution {

	private static Map<String, IOperatorHandler> distribution = new HashMap<String, IOperatorHandler>();
	static {
		distribution.put(Operator.EQUAL_TO, new EqualToHandler());
		distribution.put(Operator.NOT_EQUAL_TO, new NotEqualToHandlerHandler());
		distribution.put(Operator.LESS_THAN, new LessThanHandler());
		distribution.put(Operator.BE_SMALLER_OR_EQUAL_TO, new BeSmallerOrEqualToHandler());
		distribution.put(Operator.GREATER_THAN, new GreaterThanHandler());
		distribution.put(Operator.BE_MORE_THAN_OR_EQUAL_TO, new BeMoreThanOrEqualToHandler());
		distribution.put(Operator.AND, new AndHandler());
		distribution.put(Operator.OR, new OrHandler());
	}
	
	public static boolean handle(Ctx ctx, Object leftOperand, String operator, Object rightOperand) {
		IOperatorHandler op = distribution.get(operator);
		if(op == null) {
			throw new IllegalArgumentException("No handler can be used for the operator, operator is:" + operator);
		}
		return op.handle(ctx, leftOperand, rightOperand);
	}
}
