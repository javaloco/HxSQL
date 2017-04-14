/**
 * 
 */
package com.vitamin.db.sql.expression.syntax.handler;

import com.vitamin.db.sql.expression.syntax.Ctx;

/**
 * @author huxiao
 *
 */
public class OrHandler implements IOperatorHandler {

	@Override
	public boolean handle(Ctx ctx, Object leftOperand, Object rightOperand) {
		//逆波兰式中，左右操作符号要么全是字符串，要么全是boolean型
		Boolean left = (Boolean)leftOperand;
		Boolean right = (Boolean)rightOperand;;
		return left || right;
	}
}
