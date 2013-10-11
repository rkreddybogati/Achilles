For **CQL** implementation, it is possible to display CQL statements in the log. For this you need to activate the `ACHILLES_DML_STATEMENT` logger.

Sample **log4j.xml** config file
```xml
<logger name="ACHILLES_DML_STATEMENT">
	<level value="DEBUG" />
</logger>
```

Expected log messages:

<pre>
Simple query : [SELECT id,count,name,value FROM clustered WHERE id=8013513484507194368 ORDER BY count ASC LIMIT 3;] with CONSISTENCY LEVEL [ONE] 
Prepared statement : [INSERT INTO clustered(id,count,name,value) VALUES (?,?,?,?);] with CONSISTENCY LEVEL [ONE] 
	 bound values: [4369116603855882908, 583011159, name, clustered_value] 
Prepared statement : [UPDATE clustered SET value=? WHERE id=? AND count=? AND name=?;] with CONSISTENCY LEVEL [ONE] 
 	 bound values: [new_clustered_value, 4945333445409206272, 1627329099, name]
</pre>



