# HxSQL
Find sql in xml file, generate sql statement with parameter, like iBatis.

使用jfinal开发web项目时，希望sql语句能单独配置在xml文件中，并具有基本的、类似于iBatis的条件语句判断功能，于是开发了HxSQL。

## 加载HxSQL：
``` java
public void afterJFinalStart(){		
	SQLConfigLoader.getInstance().loadSQLXmlSet("/com/baymax/etc/db/xml/SQL_SET.xml");
}
```

## SQL_SET.xml 文件示例：
``` xml
<?xml version="1.0" encoding="UTF-8"?>
<set>
	<sqlXml name="/com/baymax/etc/db/xml/PvgUser.xml"/>
	<sqlXml name="/com/baymax/etc/db/xml/PvgRole.xml"/>
	<sqlXml name="/com/baymax/etc/db/xml/PvgPermission.xml"/>
	<sqlXml name="/com/baymax/etc/db/xml/PvgResource.xml"/>
	<sqlXml name="/com/baymax/etc/db/xml/PvgUserRole.xml"/>
	<sqlXml name="/com/baymax/etc/db/xml/PvgPermissionResource.xml"/>
</set>
```

## PvgUser.xml 文件示例：
``` xml
<?xml version="1.0" encoding="UTF-8" ?>
<root namespace="pvg_user">
	<type>
	    <property name="name" type="String" />
	    <property name="name_cn" type="String" />
	    <property name="employee_id_number" type="String" />
	    <property name="is_admin" type="String" />
	</type>

	<sql id="select_all_field">
		select *
	</sql>
	
	<sql id="select_for_show">
		select id, 
		       name, 
		       name_cn, 
		       employee_id_number,
		       is_admin
	</sql>
	
	<sql id="table">
		pvg_user
	</sql>
	
	<sql id="from_table">
		from <include refid="table" />
	</sql>
	
	<sql id="select_by_name">
	    <include refid="select_all_field" />
	    <include refid="from_table" />
	    where name = ${name}
	      and is_deleted = 'n'
	</sql>
	
	
	<sql id="filter_records">
		from <include refid="table" />
		where is_deleted = 'n'
		<if test="name != null">
			and name = ${name}
		</if>
		<if test="name_cn != null">
			and name_cn = ${name_cn}
		</if>
		<if test="employee_id_number != null">
			and employee_id_number = ${employee_id_number}
		</if>
		<if test="is_admin != null">
			and is_admin = ${is_admin}
		</if>
		
		order by id desc
	</sql>
	
	<sql id="update_by_id">
	    update <include refid="table" />
	       set modifier = ${modifier}
	       <if test="name != null">
			  ,name = ${name}
		   </if>
		   <if test="name_cn != null">
			  ,name_cn = ${name_cn}
		   </if>
		   <if test="employee_id_number != null">
			  ,employee_id_number = ${employee_id_number}
		   </if>
		   <if test="is_admin != null">
			  ,is_admin = ${is_admin}
		   </if>
	    where id = ${id}
	      and is_deleted = 'n'
	</sql>
	
	<sql id="delete_by_id">
	    update <include refid="table" />
	       set modifier = ${modifier}
	          ,is_deleted = 'y'
	    where id = ${id}
	      and is_deleted = 'n'
	</sql>
	
	<sql id="select_by_parentId_order_asc">
	    <include refid="select_all_field" />
	    <include refid="from_table" />
	    where parent_id = ${parent_id}
	      and is_deleted = 'n'
	 order by id asc; 
	</sql>
	
</root>
```

## 使用HxSQL：
``` java
public List<PvgUser> findByParentID(int id) {
	Map<String, Object> argsMap = new HashMap<String, Object>(1);
	argsMap.put(Const.pure(Const.USER.PARENT_ID), id);
	
	Conclusion oConclusion = HxSQL.getSql(NAME_SPACE, "select_by_parentId_order_asc", argsMap);
	String sql = oConclusion.getSql();
	Object[] argsValues = oConclusion.getArgsValuesBeta();
	
	return PvgUser.dao.find(sql, argsValues);
}
```

``` java
Map<String, Object> argsMap = new HashMap<String, Object>();
argsMap.put(key, value);
......

String sqlSelection = HxSQL.getSql(NAME_SPACE, "select_for_show");
Conclusion oConclusion = HxSQL.getSql(NAME_SPACE, "filter_records", argsMap);
String sqlCondition = oConclusion.getSql();
Object[] argsValues = oConclusion.getArgsValuesBeta();
List<PvgUser> list = PvgUser.dao.find(sqlSelection + " " + sqlCondition, argsValues);
```

### update的使用示例
``` java
<sql id="delete_by_id_with_version">
	update <include refid="table" /> 
	   set version = version + 1
               , is_deleted = 'y'
	       , modifier = ${modifier}
	       <if test="remark != null">
			, remark = ${remark}
	       </if>
	 where id =  ${id}
	   and is_deleted = 'n'
	   and version = ${version}
</sql>
``` 

``` java
public void delete(int id, int version) {		
	Map<String, Object> argsMap = new HashMap<String, Object>();
	argsMap.put(Const.ID, id);
	argsMap.put("modifier", Pvg.getPvgName());
	argsMap.put("version", version);
		
	Conclusion oConclusion = HxSQL.getSql(NS_WORK_SHEET, "delete_by_id_with_version", argsMap);
	String sql = oConclusion.getSql();
	Object[] argsValues = oConclusion.getArgsValues(argsMap);
		
	int count = Db.update(sql, argsValues);
	if(count == 0) {
		throw new IllegalStateException(I18nDelegate.get(I18nDelegate.KEYS.UPDATE_ERR)); 
	}
}
``` 


### ">"、"<"等符号使用示例
``` java
<?xml version="1.0" encoding="UTF-8" ?>
<root namespace="WorkSheet">
<type>
	<property name="id" type="Integer" />
	<property name="staff" type="String" />
	<property name="__start_time" type="Date" />
	<property name="__end_time" type="Date" />
</type>

<sql id="select_all_field">
	select *
</sql>
	
<sql id="from_table">
	from <include refid="table" />
</sql>
	
<sql id="table">
	`work_sheet`
</sql>

<sql id="filter_records_for_report">
	from <include refid="table" />
	where domain = 'fault' 
	and is_deleted = 'n'
	<if test="id != null">
		and id = ${id}
	</if>
	<if test="staff != null">
		and staff like ${staff}
	</if>
	<if test="__start_time != null">
		<![CDATA[
		and gmt_end >= ${__start_time}
		]]>
	</if>
	<if test="__end_time != null">
		<![CDATA[
		and gmt_end < ${__end_time}
		]]>
	</if>
</sql>
</root> 
```

``` java
public List<WorkSheet> findAllByGmtEndRange(int fleetId, Date gmtEnd_start, Date gmtEnd_end) {
	Map<String, Object> argsMap = new HashMap<String, Object>();
	argsMap.put(Const.WEB.WORK_SHEET.pure(Const.WEB.WORK_SHEET.FLEET_ID), fleetId);
	argsMap.put("__start_time", gmtEnd_start);
	argsMap.put("__end_time", gmtEnd_end);
		
	String sqlSelection = SQLConfigLoader.getInstance().getSql(NS_WORK_SHEET, "select_all_field");
	Conclusion oConclusion = SQLConfigLoader.getInstance().getSql(NS_WORK_SHEET, "filter_records_for_report", argsMap);
	String sqlCondition = oConclusion.getSql();
	Object[] argsValues = oConclusion.getArgsValuesBeta();
	return WorkSheet.dao.find(sqlSelection + sqlCondition, argsValues);
}
```


### "like"的使用示例
``` java
<?xml version="1.0" encoding="UTF-8" ?>
<root namespace="SIM">
	<type>
		<property name="sim_number" type="String" />
	</type>
	
	<sql id="select_all_field">
		select *
	</sql>
	
	<sql id="from_table">
		from SIM
	</sql>

	<sql id="filter_records">
		<include refid="from_table" />
		where is_deleted = 'n'
		<if test="sim_number != null">
			and sim_number like ${sim_number}
		</if>	
		order by id desc
	</sql>
</root>
```

``` java
public Page<SIM> find(int pageNumber, int pageSize) {
	Map<String, Object> argsMap = new HashMap<String, Object>();
	argsMap.put("sim_number", "%123%");

	String sqlSelection = HxSQL.getSql("SIM", "select_all_field");
	Conclusion oConclusion = HxSQL.getSql("SIM", "filter_records", argsMap);
	String sqlCondition = oConclusion.getSql();
	Object[] argsValues = oConclusion.getArgsValuesBeta();
	Page<SIM> pages = SIM.dao.paginate(pageNumber, pageSize, sqlSelection, sqlCondition, argsValues);
	return pages;
}
``` 

### "in"的使用示例
``` java
...
<type>
...
	<property name="sim_type" type="String" />
	<property name="oil_tank_id" type="Integer" />
</type>
...
<sql id="select_by_oil_tank_id_and_sim_type">
	<include refid="select_all_field" />
	<include refid="from_table" />
	where oil_tank_id = ${oil_tank_id}
	  and sim_type in (${sim_type})
	  and is_deleted = 'n'
</sql>
``` 

``` java
public List<SIM> findSIMID(int oilTankId, String domain) {
	List<Object> paramSIMTypes = new ArrayList<Object>();
	paramSIMTypes.add("WCDMA");
	paramSIMTypes.add("EVDO");
		
	Map<String, Object> argsMap = new HashMap<String, Object>();
	argsMap.put("oil_tank_id", oilTankId);
	argsMap.put("sim_type", paramSIMTypes);
		
	Conclusion oConclusion = HxSQL.getSql("SIM", "select_by_oil_tank_id_and_sim_type", argsMap);
		
	List<SIM> oSIMList = SIM.dao.find(oConclusion.getSql(), oConclusion.getArgsValuesBeta());
	return list;
}
``` 


### 判断条件中使用值判断的使用示例
``` java
<?xml version="1.0" encoding="UTF-8" ?>
<root namespace="SIM">
	<type>
		<property name="search__is_binded" type="String" />
	</type>
	
	<sql id="select_all_field">
		select *
	</sql>
	
	<sql id="from_table">
		from SIM
	</sql>

	<sql id="filter_records">
		<include refid="from_table" />
		where is_deleted = 'n'
		<if test="search__is_binded == 'y'">
			and oil_tank_id is not null
		</if>
		<if test="search__is_binded == 'n'">
			and oil_tank_id is null
		</if>	
		order by id desc
	</sql>
</root>
``` 

``` java
public Page<SIM> find(int pageNumber, int pageSize) {
	Map<String, Object> argsMap = new HashMap<String, Object>();
	argsMap.put("search__is_binded", "y");

	String sqlSelection = HxSQL.getSql("SIM", "select_all_field");
	Conclusion oConclusion = HxSQL.getSql("SIM", "filter_records", argsMap);
	String sqlCondition = oConclusion.getSql();
	Object[] argsValues = oConclusion.getArgsValuesBeta();
	Page<SIM> pages = SIM.dao.paginate(pageNumber, pageSize, sqlSelection, sqlCondition, argsValues);
	return pages;
}
``` 
