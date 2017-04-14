/**
 * 
 */
package com.vitamin.db.sql.expression.syntax.handler;

import com.vitamin.db.sql.expression.syntax.Ctx;

/**
 * @author huxiao
 *
 */
public interface IOperatorHandler {

	public boolean handle(Ctx ctx, Object leftOperand, Object rightOperand);
}
