### Test Resources

For TDD lovers, **Achilles** comes along with a **JUnit** rule to start an embedded Cassandra server in memory and bootstrap the framework.

 The rule extends JUnit `ExternalResource` and exposes the following methods:

```java
public class AchillesCQLResource
{
	public AchillesCQLResource(String entityPackages, String... tables){...}

	public AchillesCQLResource(String entityPackages, Steps cleanUpSteps, String... tables){...}

	public Session getNativeSession();

	public CQLPersistenceManagerFactory getPersistenceManagerFactory();

	public CQLPersistenceManager getPersistenceManager();
} 

public class AchillesThriftResource
{
	public AchillesThriftResource(String entityPackages, String... tables){...}

	public AchillesThriftResource(String entityPackages, Steps cleanUpSteps, String... tables){...}

	public Cluster getCluster();

	public Keyspace getKeyspace();

	public ThriftPersistenceManagerFactory getPersistenceManagerFactory();

	public ThriftPersistenceManager getPersistenceManager();
}
```

As exposed above, there is a one test resource for each implementation (CQL3/Thrift). The constructor accepts:

1. **entityPackages**: list of packages, separated by comma, for entity discovery.
    Example: `com.myproject.entity,com.myproject.core.entity`

> this parameter is not mandatory for **CQL3** implementation. You can bootstrap **Achilles** without any mapped entity and use it as a plain data mapper for your native CQL3 queries

2. **cleanUpSteps**: enum class indicating when tables truncate should be performed. 3 possible values `BEFORE_TEST`,`AFTER_TEST`,`BOTH`. Default value = `BOTH`

3. **tables**: list of tables/column families to be truncated before/after each test

<br/>
### Usage 

 Below is a sample code to demonstrate how to use **Achilles** rules
```java
public class MyTest
{

	@Rule
	public AchillesCQLResource resource = new AchillesCQLResource
	("com.project.entity",Steps.BOTH, "table1","anotherTable");

	private CQLPersistenceManager manager = resource.getPersistenceManager();

	@Test
	public void should_test_something() throws Exception
	{
		...
		manager.persist(myEntity);
		...
	}
}
```

 In the log file, if the logger `ACHILLES_DML_STATEMENT` is activated, we can see:

<pre>
DEBUG ACHILLES_DML_STATEMENT@:truncateTable Simple query : [TRUNCATE table1] with CONSISTENCY LEVEL [ALL] 

DEBUG ACHILLES_DML_STATEMENT@:logDMLStatement Prepared statement : [INSERT INTO table1(id,age_in_years,name,label,preferences) VALUES (?,?,?,?,?);] with CONSISTENCY LEVEL [ONE] 
DEBUG ACHILLES_DML_STATEMENT@:logDMLStatement Prepared statement : [UPDATE achilles_counter_table SET counter_value = counter_value + ? WHERE fqcn = ? AND primary_key = ? AND property_name = ?] with CONSISTENCY LEVEL [ONE] 

DEBUG ACHILLES_DML_STATEMENT@:truncateTable Simple query : [TRUNCATE table1] with CONSISTENCY LEVEL [ALL]
</pre>

Please notice the `TRUNCATE table1` query issued **before** and **after** the test for clean up.

