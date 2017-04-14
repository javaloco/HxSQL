/**
 * 
 */
package com.vitamin.db.sql.expression.syntax;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * @author huxiao
 *
 */
public class Words {
	
	private static final char SPACE = 32;
	//private static final char SLASH = 92; //"\"
	private static final char SINGLE_QUOTES = 39; // 单引号
	
	class STATUS {
		public static final int INIT = 0;
		public static final int SPACE = 1;
		public static final int OPERATOR = 2;
		public static final int LEFT_BRACKET = 3;
		public static final int RIGHT_BRACKET = 4;
		public static final int SINGLE_QUOTES_STRING = 5;
		public static final int STRING = 6;
	}
	
	private static final Map<Character, Integer> nature = new HashMap<Character, Integer> ();
	static {
		Set<Character> operatorKeyWordSet = Operator.getOperatorKeyWordSet();
		for(Character c : operatorKeyWordSet) {
			nature.put(c, STATUS.OPERATOR);
		}
		
		nature.put(SINGLE_QUOTES, STATUS.SINGLE_QUOTES_STRING);
		nature.put(SPACE, STATUS.SPACE);
		nature.put('(', STATUS.LEFT_BRACKET);
		nature.put(')', STATUS.RIGHT_BRACKET);
	}

	List<String> list = new ArrayList<String>();
	
	StringBuffer wordBuffer = new StringBuffer(); 
	Stack<Character> stack = new Stack<Character>();
	
	public List<String> scan(String testExpressionStr) {
		list.clear();
		char[] characters = testExpressionStr.toCharArray();
		for (char c : characters) {
			if(isDifferent(c)){
				list.add(getWord());
			}
			// 第一个字符一定不会是空格
			if( (sizeOfWordBuffer() == 0) && (c == Words.SPACE) ) {
				continue;
			}
			
			appendToWordBuffer(c);
		}
		list.add(getWord());
		return list;
	}
	
	private void appendToWordBuffer(char c) {
		wordBuffer.append(c);
	}
	
	private String getWord() {
		if( (wordBuffer.length() > 1) 
				&& (wordBuffer.charAt(0) == Words.SINGLE_QUOTES) 
				&& (wordBuffer.charAt(wordBuffer.length() - 1) == Words.SINGLE_QUOTES) ) {
			// 去掉 'abc' 的双引号变成 abc
			wordBuffer.deleteCharAt(0);
			wordBuffer.deleteCharAt(wordBuffer.length() - 1);
		}
		String word = wordBuffer.toString();
		wordBuffer.setLength(0);
		return word;
	}
	
	private int sizeOfWordBuffer() {
		return wordBuffer.length();
	}
	
	/**
	 * 用第一个字符的属性来判断当前wordbuffer的状态
	 * 比如wordbuffer内容为：['adc]  ;则状态为SINGLE_QUOTES_STRING, 但是实际代码没有使用状态判断这种方式
	 * 比如wordbuffer内容为：[(]  ; 则状态为STATUS.LEFT_BRACKET
	 * @return
	 */
	private int getWordBufferStatus() {
		char[] characters = wordBuffer.toString().toCharArray();
		if(characters.length == 0) return STATUS.INIT;
		Integer status = nature.get(characters[0]);
		if(status == null) return STATUS.STRING;
		return status;
	}
	
	private int getCharactorStatus(char c) {
		Integer status = nature.get(c);
		if(status == null) return STATUS.STRING;
		return status;
	}
	
	/**
	 * 利用有效空格来获取操作数及操作符
	 * 必须配合isValueStrInAppending使用
	 * @param c
	 * @return
	 */
	private boolean isActiveSPACE(char c) {
		return (c == Words.SPACE);
	}
	
	/**
	 * 当wordbuffer为abc，并且下一个字符为空格时，我们可认为是different，从而可以使abc成为一个元素
	 * 当wordbuffer为abc，并且下一个字符为操作符时，我们可认为是different，从而可以使abc成为一个元素
	 * 当wordbuffer为==，并且下一个字符为空格时，我们可认为是different，从而可以使==成为一个元素
	 * 当wordbuffer为==，并且下一个字符为a时，我们可认为是different，从而可以使==成为一个元素
	 * 
	 * 也就是如果下一个字符和前一个字符性质不一样，那么就是isDifferent
	 * @param c
	 * @return
	 */
	private boolean isDifferent(char c) {
		if(sizeOfWordBuffer() == 0) return false;
		if(sizeOfWordBuffer() > 0) {
			/*if(isActiveSPACE(c)) return true;
			if(isValueStrInAppending()) return false;
			if(isBracket(c)) return false;*/
			if(isValueStrInAppending()) return false;
			if(isActiveSPACE(c)) return true;
			
			//用于判断 == <> 这种双操作符组成的操作符号，避免==拆分成两个=号
			//因为ab遇到=号就必须组词，而=号再遇到=号不应该让=号单独组词
			if ((Operator.isKeyWord(c)) != isOperatorStrInAppending()) return true;
			return isBracketAffectDifferent(c);
		}
		return false;
	}
	
	/**
	 * 用第一个字符的属性来判断当前wordbuffer的状态
	 * 比如wordbuffer内容为：[(]  ; 则状态为STATUS.LEFT_BRACKET，如果下一个字符为a，则两个状态不一致，导致(被输出
	 * 比如wordbuffer内容为：[a]  ; 则状态为STATUS.STRING，如果下一个字符为a，则两个状态一致，则返回false
	 * 比如wordbuffer内容为：[a]  ; 则状态为STATUS.STRING，如果下一个字符为(，则两个状态一致，则返回true
	 * @param c
	 * @return
	 */
	private boolean isBracketAffectDifferent(char c) {
		int status = getCharactorStatus(c);
		if(getWordBufferStatus() == status) return false;
		return true;
	}
	
	/**
	 * 如果 == 这种操作符位于'' 之内，则只能认为是字符串而不是操作符
	 * 判断是否处于单引号字符串组装过程中
	 * @return
	 */
	private boolean isValueStrInAppending() {
		char[] characters = wordBuffer.toString().toCharArray();
		//System.out.println("characters:" + wordBuffer);
		if(characters.length == 0) return false;
		if(characters[0] != '\'') return false;
		for (char c : characters) {
			if('\'' == c) { // 如果遇到了 ' 号
				if(stack.size() == 0) { // 如果stack内什么都没有，就可以放心的塞进去
					stack.push(c);
				} else { // 如果stack内已经有内容了，那遇到单引号有两种情况，即这个单引号的前一个字符是不是转义符号
					if(stack.peek() == '\\') { // 判断是否组成 \' 转义
						stack.push(c);
					} else {
						stack.clear();
						return false;
					}
				}
			}
			else {
				stack.push(c);
				//System.out.println(stack);
			}
		}
		stack.clear();// 判断完要清除，避免残余数据影响下次判断
		return true;
	}
	
	/**
	 * 当下一个字符是“=”号时，如果前一个字符也是操作符号时,则返回true
	 * @return
	 */
	private boolean isOperatorStrInAppending() {
		char[] characters = wordBuffer.toString().toCharArray();
		for (char c : characters) {
			if(!Operator.isKeyWord(c)) return false;
		}
		return true;
	}
	
	public static void main(String[] args) {
		String testStr = "#name=='huxiao' and(#age <> '12' or #money< 100000 ) and #line=zhoushan ()";
		test(testStr);
		
		testStr = " b== 'dd\\'d d==d' and avc>= 1 or cccc !=null";
		test(testStr);
		
		testStr = " b== 'dd\\'d d==d'";
		test(testStr);
		
		testStr = " b== 'dd\\'d d==d'";
		test(testStr);
	}
	
	public static void test(String testStr) {
		System.out.println(testStr);
		Words oWords = new Words();
		List<String> list = oWords.scan(testStr);
		System.out.println(list);
		System.out.println("\r\n==================\r\n");
	}
}
