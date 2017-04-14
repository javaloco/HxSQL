/**
 * 
 */
package com.vitamin.db.sql.expression.syntax;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author huxiao
 *
 */
public class Operator {
	
	public static Integer HIGH_PRIORITY = 2;
	public static Integer LOW_PRIORITY = 1;
	public static Integer MIN_PRIORITY = 0;
	
	public static String AND = "and";
	public static String OR = "or";
	public static String LEFT_BRACKET = "(";
	public static String RIGHT_BRACKET = ")";
	
	private static Set<String> operatorSet = new HashSet<String>();
	private static Set<Character> operatorKeyWordSet = new HashSet<Character>();
	private static Map<String, Integer> priority = new HashMap<String, Integer> ();
	
	public static final String NOT_EQUAL_TO = "!=";
	public static final String EQUAL_TO = "==";
	public static final String LESS_THAN = "<";
	public static final String BE_SMALLER_OR_EQUAL_TO = "<=";
	public static final String GREATER_THAN = ">";
	public static final String BE_MORE_THAN_OR_EQUAL_TO = ">=";
	static {
		operatorSet.add(NOT_EQUAL_TO);
		operatorSet.add(EQUAL_TO);
		operatorSet.add(LESS_THAN);
		operatorSet.add(BE_SMALLER_OR_EQUAL_TO);
		operatorSet.add(GREATER_THAN);
		operatorSet.add(BE_MORE_THAN_OR_EQUAL_TO);
		//operatorSet.add("&&");
		//operatorSet.add("||");
		
		/**
		 * 将 != 拆成 ! 和 = 形成运算符关键字，用于解析表达式时作为识别某个关键词的分割依据
		 * 比如  name!='123'   需要通过运算符识别出 name 和 '123'
		 */
		for(String s : operatorSet) {
			priority.put(s, HIGH_PRIORITY);
			char[] cs = s.toCharArray();
			for(char c : cs) {
				operatorKeyWordSet.add(c);
			}
		}
		
		priority.put(AND, LOW_PRIORITY);
		priority.put(OR, LOW_PRIORITY);
		priority.put(LEFT_BRACKET, MIN_PRIORITY);
	}
	
	
	public static boolean has(String str) {
		if (operatorSet.contains(str)) {
			return true;
		} 
		else if(AND.equals(str)) {
			return true;
		}
		else if(OR.equals(str)) {
			return true;
		}
		return false;
	}
	
	public static boolean isKeyWord(char c) {
		return operatorKeyWordSet.contains(c);
	}
	
	public static int getPriority(String s) {
		return priority.get(s);
	}
	
	public static Set<Character> getOperatorKeyWordSet() {
		return operatorKeyWordSet;
	}
}
