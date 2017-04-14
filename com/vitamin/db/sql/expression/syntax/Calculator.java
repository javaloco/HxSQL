/**
 * 
 */
package com.vitamin.db.sql.expression.syntax;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.vitamin.db.sql.SQLConfigLoader;
import com.vitamin.db.sql.expression.syntax.handler.OperatorHandlerDistribution;

/**
 * @author huxiao
 *
 */
public class Calculator {
	private List<String> reversePolishList = new ArrayList<String>();
	private Stack<String> operatorStack = new Stack<String>();
	private Stack<Object> calStack = new Stack<Object>();
	
	private Words oWords = new Words();  
	
	public static boolean cal(Ctx ctx, String expression) {
		Calculator o = new Calculator();
		return o.calculate(ctx, expression);
	}
	
	public boolean calculate(Ctx ctx, String expression) {
		List<String> words = oWords.scan(expression);
		SQLConfigLoader.debug("words:" + words);
		reversePolish(words);
		SQLConfigLoader.debug("reversePolishList:" + reversePolishList);
		return calReversePolish(ctx);
	}
	
	private void reversePolish(List<String> words) {
		reversePolishList.clear();
		for(String e : words) {
			if(Operator.LEFT_BRACKET.equals(e)) {
				operatorStack.push(e);
			}
			else if (Operator.RIGHT_BRACKET.equals(e)) {
				// 当遇到右括号时，依次pop出符号堆栈中的元素直到出现左括号，左括号最后需要直接丢弃
				while(!Operator.LEFT_BRACKET.equals(operatorStack.peek())) {
					reversePolishList.add(operatorStack.pop());
				}
				operatorStack.pop();
			}
			else if(Operator.has(e)) {
				if(operatorStack.isEmpty()) {
					operatorStack.push(e);
					continue;
				}
				
				String top = operatorStack.peek();
				int topPriority = Operator.getPriority(top);
				int ePriority = Operator.getPriority(e);
				if(ePriority > topPriority) {
					// 如果新的操作符优先级大于栈顶符号，则直接把新操作符压入堆栈中
					operatorStack.push(e);
				} else {
					// 如果新的操作符优先级小于或者等于栈顶符号，则把栈顶运算符输出，然后再压入新运算符
					reversePolishList.add(operatorStack.pop());
					operatorStack.push(e);
				}
			} 
			else {
				reversePolishList.add(e);
			}
		}
		
		while(!operatorStack.isEmpty()) {
			reversePolishList.add(operatorStack.pop());
		}
	}
	
	private boolean calReversePolish(Ctx ctx) {
		for(String e : reversePolishList) {
			if(Operator.has(e)) {
				SQLConfigLoader.debug("calStack:" + calStack);
				Object firstTop = calStack.pop();// 第一个弹出的是右侧操作数, ｛要么是boolean型，要么是字符串｝。
				Object secondTop = calStack.pop();// 第二个弹出的是左侧操作数
				// 根据操作符选择相应的handler来计算结果
				boolean result = OperatorHandlerDistribution.handle(ctx, secondTop, e, firstTop);
				calStack.push(result);
			}
			else {
				calStack.push(e);
			}
		}
		return (Boolean) calStack.pop();
	}
}


/**

4利用堆栈解析算术表达式的过程

中缀表达式翻译成后缀表达式的方法如下：

（1）从左向右依次取得数据ch。

（2）如果ch是操作数，直接输出。

（3）如果ch是运算符（含左右括号），则：
a：如果ch = '('，放入堆栈。
b：如果ch = ')'，依次输出堆栈中的运算符，直到遇到'('为止。
c：如果ch不是')'或者'('，那么就和堆栈顶点位置的运算符top做优先级比较。
1：如果ch优先级比top高，那么将ch放入堆栈。
2：如果ch优先级低于或者等于top，那么输出top，然后将ch放入堆栈。

（4）如果表达式已经读取完成，而堆栈中还有运算符时，依次由顶端输出。

如果我们有表达式(A-B)*C+D-E/F，要翻译成后缀表达式，并且把后缀表达式存储在一个名叫output的字符串中，可以用下面的步骤。

（1）读取'('，压入堆栈，output为空
（2）读取A，是运算数，直接输出到output字符串，output = A
（3）读取'-'，此时栈里面只有一个'('，因此将'-'压入栈，output = A
（4）读取B，是运算数，直接输出到output字符串，output = AB
（5）读取')'，这时候依次输出栈里面的运算符'-'，然后就是'('，直接弹出，output = AB-
（6）读取'*'，是运算符，由于此时栈为空，因此直接压入栈，output = AB-
（7）读取C，是运算数，直接输出到output字符串，output = AB-C
（8）读取'+'，是运算符，它的优先级比'*'低，那么弹出'*'，压入'+"，output = AB-C*
（9）读取D，是运算数，直接输出到output字符串，output = AB-C*D
（10）读取'-'，是运算符，和'+'的优先级一样，因此弹出'+'，然后压入'-'，output = AB-C*D+
（11）读取E，是运算数，直接输出到output字符串，output = AB-C*D+E
（12）读取'/'，是运算符，比'-'的优先级高，因此压入栈，output = AB-C*D+E
（13）读取F，是运算数，直接输出到output字符串，output = AB-C*D+EF
（14）原始字符串已经读取完毕，将栈里面剩余的运算符依次弹出，output = AB-C*D+EF/-

5计算算术表达式

当有了后缀表达式以后，运算表达式的值就非常容易了。可以按照下面的流程来计算。

（1）从左向右扫描表达式，一个取出一个数据data
（2）如果data是操作数，就压入堆栈
（3）如果data是操作符，就从堆栈中弹出此操作符需要用到的数据的个数，进行运算，然后把结果压入堆栈
（4）如果数据处理完毕，堆栈中最后剩余的数据就是最终结果。

比如我们要处理一个后缀表达式1234+*+65/-，那么具体的步骤如下。

（1）首先1，2，3，4都是操作数，将它们都压入堆栈
（2）取得'+'，为运算符，弹出数据3，4，得到结果7，然后将7压入堆栈
（3）取得'*'，为运算符，弹出数据7，2，得到数据14，然后将14压入堆栈
（4）取得'+'，为运算符，弹出数据14，1，得到结果15，然后将15压入堆栈
（5）6，5都是数据，都压入堆栈
（6）取得'/'，为运算符，弹出数据6，5，得到结果1.2，然后将1.2压入堆栈
（7）取得'-'，为运算符，弹出数据15，1.2，得到数据13.8，这就是最后的运算结果


*/