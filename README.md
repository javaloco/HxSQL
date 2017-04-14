# HxSQL
Find sql in xml file, generate sql statement with parameter, like iBatis.

使用jfinal开发web项目时，希望sql语句能单独配置在xml文件中，并具有基本的、类似于iBatis的条件语句判断功能，于是开发了HxSQL。

加载HxSQL：

public void afterJFinalStart(){		
		SQLConfigLoader.getInstance().loadSQLXmlSet("/com/baymax/etc/db/xml/SQL_SET.xml");
}

SQL_SET.xml 文件示例：

<?xml version="1.0" encoding="UTF-8"?>
<set>
	<sqlXml name="/com/baymax/etc/db/xml/PvgUser.xml"/>
	<sqlXml name="/com/baymax/etc/db/xml/PvgRole.xml"/>
	<sqlXml name="/com/baymax/etc/db/xml/PvgPermission.xml"/>
	<sqlXml name="/com/baymax/etc/db/xml/PvgResource.xml"/>
	<sqlXml name="/com/baymax/etc/db/xml/PvgUserRole.xml"/>
	<sqlXml name="/com/baymax/etc/db/xml/PvgPermissionResource.xml"/>
</set>

PvgUser.xml 文件示例：

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

未完待续...
