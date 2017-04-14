/**
 * 
 */
package com.vitamin.db.sql;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.velocity.app.VelocityEngine;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

import com.vitamin.db.sql.expression.Expression;

/**
 * 使用单例的原因是不想耦合到jfinal的框架之中。
 * 这样的实现方式有助于防便的嵌入到不同的框架之中。
 * 
 * @author Administrator
 *
 */
public class SQLConfigLoader {
	static Logger log = Logger.getLogger(SQLConfigLoader.class);
	private static boolean isDebug = false;
	
	public static void debug(Object o) {
		if(isDebug) {
			System.out.println(o);
		}
	}
	
	private static class SingletonHolder { 
		public static SQLConfigLoader resource = new SQLConfigLoader(); 
	} 
	
	public static SQLConfigLoader getInstance() { 
		return  SingletonHolder.resource ; 
	} 
	       
	private SQLConfigLoader(){
		ve.init();  
	} 
	
	Expression expression = new Expression();
	
	VelocityEngine ve = new VelocityEngine();

	private static final int BSIZE = 1024;
	
	private final Map<String, Map<String, String>> namespaceSqlMapping = new HashMap<String, Map<String, String>>();
	private final Map<String, String> typeMapping = new HashMap<String, String>();
	
	
//	static String regex = "#\\{[A-z_]+\\}";
//	static Pattern pattern = Pattern.compile(regex);
	
	Pattern patternForPrepareStatement = Pattern.compile("\\$\\{.+\\}");
	Pattern patternForSqlParam = Pattern.compile("\\$\\{(.*)\\}");
	//<include refid="xxx_xxx" />
	Pattern patternForIncludeTag = Pattern.compile("<include\\s+refid\\s*=\\s*\"([A-Za-z_]+)\"\\s*/\\s*>");
	/**
	 * 合并读取并解析多个配置文件
	 * @param xmlSetPath
	 * @return
	 */
	public void loadSQLXmlSet(String xmlSetPath) {
		String xml = readFile(xmlSetPath) ;
		Document document = parseXML(xml);
		List<?> elements = document.selectNodes("/set/sqlXml"); 

		for (Object obj : elements) {  
            Element element = (Element) obj;  
            String name = element.attributeValue("name");
            loadXML(name);
		}
	}
	
	/**
	 * 加载SQL文件
	 * @param xmlPath
	 */
	private void loadXML(String xmlPath) {
		String SQL_XML = null;
		
		try {
			SQL_XML = readFile(xmlPath) ;
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		Document document = parseXML(SQL_XML);
		
		Element root = (Element)document.selectSingleNode("/root");
		String namespace = root.attributeValue("namespace");
		
		/**
		 * /root//*  表示 root 下的所有子元素
		 * /root/*  表示 root 下一级的子元素
		 */
		List<?> elements = document.selectNodes("/root/sql");  
		Map<String, String> sqlMapping = new HashMap<String, String>();
		/**
		 * 先剥下来第一层
		 */
		for (Object obj : elements) { 
            Element element = (Element) obj;  
            
            String key = element.attributeValue("id");
            //String value = element.getText().trim();
            
            /* 为什么要包含外部的xml标签，因为内部解析还需要再次调用DocumentHelper.parseText
             * <sql id="select_all_field">
             * select * 
             * </sql>
             */
            String xml = element.asXML();
             
            // getName()是元素名,getText()是元素值  
            debug(key + ": " + xml);  
            if(sqlMapping.containsKey(key)) throw new RuntimeException("SQL xml exist same id.");
            sqlMapping.put(key, xml);
        }  
		solveIncludeTag(sqlMapping);
		namespaceSqlMapping.put(namespace, sqlMapping);
		
		buildTypeMapping(namespace, document);
	}
	
	/**
	 * 获得各个字段的相应的类型。
	 * @param namespace
	 * @param document
	 */
	private void buildTypeMapping(String namespace, Document document){
		List<?> typeElements = document.selectNodes("/root/type/property");  
		for (Object obj : typeElements) {  
            Element element = (Element) obj;  
            
            String name = namespace + "." + element.attributeValue("name");
            String type = element.attributeValue("type");
             
            // getName()是元素名,getText()是元素值  
            debug(name + ": " + type);  
            if(typeMapping.containsKey(name)) {
            	throw new RuntimeException("SQL xml exist same property.");
            }
            typeMapping.put(name, type);
        }  
	}
	
	/**
	 * 替换完所有的<include refid="xxx_xxx" />
	 * 如果map中仍然有<include refid="xxx_xxx" />，则不间断的进行替换。
	 * @param sqlMapping
	 */
	private void solveIncludeTag(Map<String, String> sqlMapping) {
		boolean isFinish = false;
		while(!isFinish) {
			isFinish = true;
			for (Map.Entry<String, String> entry : sqlMapping.entrySet()) {
				Matcher matcher = patternForIncludeTag.matcher(entry.getValue());
				if(matcher.find()){
					String matcherStr = matcher.group();
					String refid = matcher.group(1).trim();
					debug("refid : " + refid);
					isFinish = false;
					
					/**
					 * <sql id="xxx">select ...</sql>
					 * 变成：
					 * select ...
					 */
					String xml = sqlMapping.get(refid);
					debug("xml : " + xml);
					Document oDocument = parseXML(xml);//通过parse重新格式化xml,方便后面的正则表达式替换
					Node sqlContent = oDocument.selectSingleNode("/sql");
					
					String nodeName = sqlContent.getName(); //确定节点名
					String outerXML = sqlContent.asXML();
					
					String nestedXML = outerXML.replaceAll("^<"+nodeName+".*?>|</"+nodeName+">$", "");
					debug("nestedXML : " + nestedXML);
					
					/**
					 * 替换
					 */
					String newXML = entry.getValue().replace(matcherStr, nestedXML);
					debug("newXML : " + newXML);
					sqlMapping.put(entry.getKey(), newXML);
				}
	        }  
		}
	}
	
	/**
	 * 读取文件内容
	 * @param xmlPath
	 * @return
	 */
	private String readFile(String xmlPath) {
		URL url = this.getClass().getResource(xmlPath);
		if(url == null) {
			throw new IllegalArgumentException("Sql xml does't exist. xml is:" + xmlPath);
		}
		File file = new File(url.getFile());
		
		StringBuffer sb = new StringBuffer();
		FileInputStream fs = null;
		FileChannel fc = null;
		try {
			fs = new FileInputStream(file);
			fc = fs.getChannel();
			ByteBuffer buff = ByteBuffer.allocate(BSIZE);
			while(fc.read(buff) != -1) {
				buff.flip();
				while (buff.hasRemaining()) {
					sb.append((char) buff.get());
			    }
				buff.clear();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new IllegalStateException(e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalStateException(e);
		} finally {
			try {
			if(fs != null) fs.close();
			if(fc != null) fc.close();
			} catch(Exception ee) {
				fs = null;
				fc = null;
			}
		}
		return sb.toString();
	}
	
	/**
	 * 解析xml文件
	 * @return
	 */
	private Document parseXML(String sqlXml) {
		Document document = null;
		try {
			document = DocumentHelper.parseText(sqlXml);
		} catch (DocumentException e) {
			e.printStackTrace();
			throw new IllegalStateException(e);
		}
		return document;
	}
	
	/**
	 * 为了jfinal使用sql方法时有准确的参数顺序
	 * 
	 * java只有值传递，但是能改变对象内的参数状态，变相实现引用传递
	 * 
	 * @param content
	 * @param keySet
	 * @return
	 */
	private void analyseForPrepareStatement(List<String> args, List<Object> argsValues, StringBuffer content, Map<String, Object> argsMap) {
		Matcher matcher = patternForPrepareStatement.matcher(content);
		if(matcher.find()) {
			String paramKey = getSqlXmlParamKey(matcher.group());
			
			Object value = argsMap.get(paramKey);
			String paramHole = "?";
			if(value instanceof List<?>) {
				List<?> vas = (List<?>)value;
				StringBuffer paramHoleBuffer = new StringBuffer();
				for(Object o : vas) {
					paramHoleBuffer.append(",").append("?");
					args.add("[" + paramKey + "]");
					argsValues.add(o);
				}
				paramHole = paramHoleBuffer.substring(1);
			} 
			else {
				args.add(paramKey);// matcher.replaceFirst 和 matcher.group() 有先后顺序
				argsValues.add(value);
			}
			
			String newContent = matcher.replaceFirst(paramHole);

			//KIT.debug(matcher.group());
			
			content.setLength(0);
			content.append(newContent);
			//KIT.debug("matcher.replaceFirst(\"?\") => " + content);
			analyseForPrepareStatement(args, argsValues, content, argsMap);
		}
	}
	
	private String getSqlXmlParamKey(String paramKey) {
		Matcher matcher = patternForSqlParam.matcher(paramKey);
		if(matcher.find()){
			return matcher.group(1).trim();
		}
		return null;
	}
	
	
	/**
	 * 开始将参数键替换成？
	 * @param content
	 * @param keySet
	 * @return
	 */
//	private String exportFixedVelocity(String content, Set<String> keySet) {   
//		KIT.debug("source content:" + content);
//        try {      
//            // 设置初始化数据  
//            VelocityContext context = new VelocityContext();  
//            for(String key : keySet) {
//            	context.put(key, "?");  
//            }
//              
//            // 设置输出  
//            StringWriter writer = new StringWriter();  
//            // 将环境数据转化输出  
//            ve.evaluate(context, writer, "", content); // 关键方法
//            KIT.debug("output:" + writer.toString());
//            return writer.toString();  
//  
//        } catch (Exception e) {  
//        	e.printStackTrace();
//            throw new RuntimeException("模版转化错误!");  
//        }  
//    }  
	
	/**
	 * 获取从xml解析出来的sql的语句
	 * @param namespace
	 * @param id
	 * @param argsMap
	 * @return
	 */
	public Conclusion getSql(String namespace, String id, Map<String, Object> argsMap) {
		Map<String, String> sqlMapping = namespaceSqlMapping.get(namespace);
		if(sqlMapping == null) {
			throw new IllegalArgumentException("The namespace doesn't exist. namespace is:" + namespace);
		}
		String sqlXml = sqlMapping.get(id);
		String docXml = analyse(namespace, sqlXml, argsMap);
		Set<String> keySet = argsMap.keySet();
		
		StringBuffer sql = new StringBuffer(docXml);
		List<String> args = new ArrayList<String>();
		List<Object> argsValues = new ArrayList<Object>();
		analyseForPrepareStatement(args, argsValues, sql, argsMap);
		debug("analyseForPrepareStatement return => " + sql);
		debug("analyseForPrepareStatement args return => " + args);
		debug("analyseForPrepareStatement keySet return => " + keySet);
		
		Conclusion oConclusion = new Conclusion();
		oConclusion.setArgsKeys(args);
		oConclusion.setArgsValuesBeta(argsValues);
		oConclusion.setSql(sql.toString());
		return oConclusion;
	}
	
	public String getSql(String namespace, String id) {
		Map<String, String> sqlMapping = namespaceSqlMapping.get(namespace);
		if(sqlMapping == null) {
			throw new IllegalArgumentException("The namespace doesn't exist. namespace is:" + namespace);
		}
		String sqlXml = sqlMapping.get(id);
		
		Document sqlWrapper = parseXML(sqlXml);
		Node sqlContent = sqlWrapper.selectSingleNode("/sql");
		return sqlContent.getText();
	}
	
	/**
	 * 得到SQL.xml中的指定参数的类型
	 * @param keyWithNamespace
	 * @return
	 */
	public String getParamType(String keyWithNamespace) {
		return typeMapping.get(keyWithNamespace);
	}
	
	/**
	 * 
	 *  <sql id="opm_car_usual_condition">
		from opm_cars 
	   where fleet_id = #{fleet_id}
	     and is_deleted = 'n' 
	     <if test="bizDomain != null">
	     	and biz_domain <> 1
	     	<if test="bizDomain == 'ddddd'">
	     	and biz_domain = #{biz_domain}
	     	</if>
	     </if>
	     and dddd
	     <if test="name != null">
	     	and name = #{name}
	     </if>
	</sql>
	 * 
	 * 
	 * 1. 通过某种方式获得<sql id="opm_car_usual_condition"></sql> 标签内的正文
	 * 2. 通过字符串indexOf获得下一级的子标签的起始位置
	 * 3. 获得下级子标签的元素数组，然后再对子元素数组中的元素进行如上解析
	 * 
	 * 
	 * @param sqlXml
	 * @return
	 */
	private String analyse(String namespace, String sqlXml, Map<String, Object> argsMap) {
		Document document = parseXML(sqlXml);
		
		//Element root = document.getRootElement();
		String docXml = document.asXML();
	
		//    /root/*  表示 root 下一级的子元素
		List<?> elements = document.selectNodes("/sql/*"); 
		
		/**
		 * 从xml标签的最外层往里面剥，如果解析一层后还有if标签，则继续往里面剥
		 * 
		 * <sql id="opm_car_usual_condition">
				from opm_cars 
	   			where fleet_id = #{fleet_id}
	     		and is_deleted = 'n' 
	     		
	     		<if test="bizDomain != null">
	     			and biz_domain <> 1
	     			<if test="bizDomain == 'ddddd'">
	     				and biz_domain = #{biz_domain}
	     			</if>
	     		</if>
	     		and dddd
	     		<if test="name != null">
	     			and name = #{name}
	     		</if>
			</sql>
		 * 
		 *  解析第一次后变成：
		 * <sql id="opm_car_usual_condition">
				from opm_cars 
	   			where fleet_id = #{fleet_id}
	     		and is_deleted = 'n' 
	     		
	     		
	     			and biz_domain <> 1
	     			<if test="bizDomain == 'ddddd'">
	     				and biz_domain = #{biz_domain}
	     			</if>
	     			
	     		and dddd
	     		
	     			and name = #{name}
	     		
			</sql>
		 *  
		 *  解析第二次后变成：
		 * <sql id="opm_car_usual_condition">
				from opm_cars 
	   			where fleet_id = #{fleet_id}
	     		and is_deleted = 'n' 
	     		
	     		
	     			and biz_domain <> 1
	     			
	     				and biz_domain = #{biz_domain}
	     			
	     			
	     		and dddd
	     		
	     			and name = #{name}
	     		
			</sql>
		 * 
		 */
		while(!elements.isEmpty()) {
			for (Object obj : elements) {  
				Element element = (Element) obj;  
				String nodeType = element.getName().toLowerCase();
				// 开始分析xml中的sql
				String sqlStr = expression.handle(namespace, nodeType, element, argsMap);
				debug("sqlStr : " + sqlStr);  
				int index = docXml.indexOf(element.asXML());
				if(index == -1) continue;
				debug("docXml before replace : " + docXml); 
				//replace的参数是字符串，replaceFirst及replaceAll则是正则表达式
				docXml = docXml.replace(element.asXML(), "");
				String firstHalf = docXml.substring(0, index);
				String lastHalf = docXml.substring(index, docXml.length());
				docXml = firstHalf + sqlStr + lastHalf;
				debug("docXml : " + docXml);  
			}
			
			document = parseXML(docXml);
			elements = document.selectNodes("/sql//*");  
		}

		Document sqlWrapper = parseXML(docXml);
		Node sqlContent = sqlWrapper.selectSingleNode("/sql");
		return sqlContent.getText();
	}

	
	/**
	 * 1.先解析SQL.xml中的sql，替换#AAA为?号，重新生成带?号的conditionSql。
	 * 
	 * @param model
	 * @param selectFieldSqlId
	 * @param conditionSqlId
	 * @param argsMap
	 * @param page
	 * @param pageSize
	 * @return
	 */
	/*public static<M> Page<M> paginate(M model, String namespace, String selectFieldSqlId, String conditionSqlId, Map<String, Object> argsMap, int page, int pageSize) {
		String conditionSql = SQLConfigLoader.getInstance().getSql(namespace, conditionSqlId);
		String selectFieldSql = SQLConfigLoader.getInstance().getSql(namespace, selectFieldSqlId);
		
        Matcher matcher = pattern.matcher(conditionSql);
        
        List<String> fields = new ArrayList<String> ();
        while(matcher.find()) {
        	String argMask = matcher.group(0);
        	fields.add(argMask.substring(2, argMask.length() - 1));
        }
        System.out.println(fields);	
        conditionSql = conditionSql.replaceAll(regex, "?");
        System.out.println(conditionSql);
        
        List<Object> args = new ArrayList<Object> ();
        for(String key : fields) {
        	args.add(argsMap.get(key));
        }
		
        return (Page<M>)com.vitamin.jfinal.enhance.PaginateInvoker.invoke((Model<?>)model, selectFieldSql, conditionSql, args.toArray(), page, pageSize);
	}*/
}
