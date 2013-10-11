**Achilles** provides some end points to integrate easily with Spring.

## XML Config
For XML config lovers, **Achilles** exposes a `CQLPersistenceManagerFactoryBean` and `ThriftPersistenceManagerFactoryBean`. To use them, just declare the factory in your XML config file:

### CQL

```xml
<!-- Configuration with contactPoints,port and keyspace -->
<bean id="achillesPersistenceManagerFactoryBean" 	
class="info.archinnov.achilles.integration.spring.CQLPersistenceManagerFactoryBean">
	<property name="entityPackages" value="com.mypackage.entity"/>

	<property name="contactPoints" value="localhost"/>
	<property name="port" value="9042"/>
	<property name="keyspaceName" value="my_keyspace"/>

	<property name="consistencyLevelReadDefault" value="ONE"/>
	<property name="consistencyLevelWriteDefault" value="QUORUM"/>

	...
</bean>

<!-- Configuration with cluster and session -->
<bean id="achillesPersistenceManagerFactoryBean" 	
class="info.archinnov.achilles.integration.spring.CQLPersistenceManagerFactoryBean">
	<property name="entityPackages" value="com.mypackage.entity"/>

	<property name="cluster" ref="cluster"/>
	<property name="session" ref="session"/>

	<property name="consistencyLevelReadDefault" value="ONE"/>
	<property name="consistencyLevelWriteDefault" value="QUORUM"/>

	...
</bean>
```

### Thrift

```xml


<!-- Read Consistency Map -->
<map id="readConsistencyMap" key-type="java.lang.String" value-type="java.lang.String">
	<entry key="columnFamily1" value="ONE" />
	<entry key="columnFamily2" value="TWO" />
	...
</map>

<!-- Write Consistency Map -->
<map id="writeConsistencyMap" key-type="java.lang.String" value-type="java.lang.String">
	<entry key="columnFamily1" value="QUORUM" />
	<entry key="columnFamily2" value="ALL" />
	...
</map>

<!-- Configuration with HECTOR cluster and keyspace -->
<bean id="achillesPersistenceManagerFactoryBean" class="info.archinnov.achilles.integration.spring.ThriftPersistenceManagerFactoryBean">
	<property name="entityPackages" value="com.mypackage.entity"/>

	<property name="keyspace" ref="cassandraKeyspace"/>
	<property name="cluster" ref="cassandraCluster"/>

	<property name="objectMapper" ref="objectMapper" />

	<property name="consistencyLevelReadDefault" value="ONE"/>
	<property name="consistencyLevelWriteDefault" value="QUORUM"/>

	<property name="consistencyLevelReadMap" value="readConsistencyMap"/>
	<property name="consistencyLevelWriteMap" value="writeConsistencyMap"/>

	...
</bean>

<!-- Configuration with host and cluster/keyspace name -->
<bean id="achillesPersistenceManagerFactoryBean" class="info.archinnov.achilles.integration.spring.ThriftPersistenceManagerFactoryBean">
	<property name="entityPackages" value="com.mypackage.entity"/>

	<property name="cassandraHost" value="localhost:9160"/>
	<property name="clusterName" value="achillesCluster"/>
	<property name="keyspaceName" value="achilles"/>

	<property name="objectMapper" ref="objectMapper" />

	<property name="consistencyLevelReadDefault" value="ONE"/>
	<property name="consistencyLevelWriteDefault" value="QUORUM"/>

	<property name="consistencyLevelReadMap" value="readConsistencyMap"/>
	<property name="consistencyLevelWriteMap" value="writeConsistencyMap"/>

	...
</bean>
```

## Java Config

 For those who configure their beans through Java code, you need to configure the `CQLPersistenceManager` or `ThriftPersistenceManager` manually. Sample configuration files are provided: 

* `info.archinnov.achilles.integration.spring.CQLPersistenceManagerJavaConfigSample`
* `info.archinnov.achilles.integration.spring.ThriftPersistenceManagerJavaConfigSample`

### CQL

```java
import static info.archinnov.achilles.configuration.CQLConfigurationParameters.*;
import static info.archinnov.achilles.configuration.ConfigurationParameters.*;
...
@Configuration
public class CQLPersistenceManagerJavaConfigSample
{

    @Value("#{cassandraProperties['achilles.entity.packages']}")
    private String entityPackages;

    @Value("#{cassandraProperties['achilles.cassandra.connection.contactPoints']}")
    private String contactPoints;

    @Value("#{cassandraProperties['achilles.cassandra.connection.port']}")
    private Integer port;

    @Value("#{cassandraProperties['achilles.cassandra.keyspace.name']}")
    private String keyspaceName;

    @Autowired
    private RetryPolicy retryPolicy;

    @Autowired
    private LoadBalancingPolicy loadBalancingPolicy;

    @Autowired
    private ReconnectionPolicy reconnectionPolicy;

    @Value("#{cassandraProperties['achilles.cassandra.username']}")
    private String username;

    @Value("#{cassandraProperties['achilles.cassandra.password']}")
    private String password;

    @Value("#{cassandraProperties['achilles.cassandra.disable.jmx']}")
    private boolean disableJmx;

    @Value("#{cassandraProperties['achilles.cassandra.disable.metrics']}")
    private boolean disableMetrics;

    @Value("#{cassandraProperties['achilles.cassandra.ssl.enabled']}")
    private boolean sslEnabled;

    @Autowired
    private SSLOptions sslOptions;

    @Autowired
    private ObjectMapperFactory objecMapperFactory;

    @Value("#{cassandraProperties['achilles.consistency.read.default']}")
    private String consistencyLevelReadDefault;

    @Value("#{cassandraProperties['achilles.consistency.write.default']}")
    private String consistencyLevelWriteDefault;

    @Value("#{cassandraProperties['achilles.consistency.read.map']}")
    private String consistencyLevelReadMap;

    @Value("#{cassandraProperties['achilles.consistency.write.map']}")
    private String consistencyLevelWriteMap;

    @Value("#{cassandraProperties['achilles.ddl.force.column.family.creation']}")
    private String forceColumnFamilyCreation;

    private CQLPersistenceManagerFactory pmf;

    @PostConstruct
    public void initialize()
    {
        Map<String, Object> configMap = extractConfigParams();
        pmf = new CQLPersistenceManagerFactory(configMap);
    }

    @Bean
    public CQLPersistenceManager getPersistenceManager()
    {
        return pmf.createPersistenceManager();
    }

    private Map<String, Object> extractConfigParams()
    {
       ...
    }

    private Map<String, String> extractConsistencyMap(String consistencyMapProperty)
    {
        ...
    }
```

 The `cassandraProperties.properties` file looks like:

```text
# Entity packages
achilles.entity.packages = com.my.package1,com.my.package2

#Cluster
achilles.cassandra.connection.contactPoints = localhost
achilles.cassandra.connection.point = 9042
achilles.cassandra.keyspace.name = achilles

#Credentials
achilles.cassandra.username = xxxx
achilles.cassandra.password = xxx

#Metrics
achilles.cassandra.disable.jmx = true
achilles.cassandra.disable.metrics = true

#SSL
achilles.cassandra.ssl.enabled = true

# Consistency Levels
achilles.consistency.read.default = ONE
achilles.consistency.write.default = QUORUM

# DDL 
achilles.ddl.force.column.family.creation = true

```


### Thrift

```java
import static info.archinnov.achilles.configuration.ConfigurationParameters.*;
...

@Configuration
public class ThriftPersistenceManagerJavaConfigSample
{
	@Value("#{cassandraProperties['achilles.entity.packages']}")
	private String entityPackages;

	@Autowired(required = true)
	private Cluster cluster;

	@Autowired(required = true)
	private Keyspace keyspace;

	@Autowired
	private ObjectMapperFactory objecMapperFactory;

	@Value("#{cassandraProperties['achilles.consistency.read.default']}")
	private String consistencyLevelReadDefault;

	@Value("#{cassandraProperties['achilles.consistency.write.default']}")
	private String consistencyLevelWriteDefault;

	@Value("#{cassandraProperties['achilles.consistency.read.map']}")
	private String consistencyLevelReadMap;

	@Value("#{cassandraProperties['achilles.consistency.write.map']}")
	private String consistencyLevelWriteMap;

	@Value("#{cassandraProperties['achilles.ddl.force.column.family.creation']}")
	private String forceColumnFamilyCreation;

	private ThriftPersistenceManagerFactory pmf;

	@PostConstruct
	public void initialize()
	{
		Map<String, Object> configMap = extractConfigParams();
		pmf = new ThriftPersistenceManagerFactory(configMap);
	}

	@Bean
	public ThriftPersistenceManager getPersistenceManager()
	{
		return pmf.createPersistenceManager();
	}

	private Map<String, Object> extractConfigParams()
	{
		...
	}

	private Map<String, String> extractConsistencyMap(String consistencyMapProperty)
	{
		...
	}
```

 The `cassandraProperties.properties` file looks like:

```text
# Entity packages
achilles.entity.packages = com.my.package1,com.my.package2

# Consistency Levels
achilles.consistency.read.default = ONE
achilles.consistency.write.default = QUORUM

achilles.consistency.read.map = columnFamily1:ONE,columnFamily2:TWO
achilles.consistency.write.map = columnFamily1:QUORUM,columnFamily2:ALL

# DDL 
achilles.ddl.force.column.family.creation = true

```

 In the above configuration class, a new `ThriftPersistenceManager` is created upon each invocation of `getPersistenceManager()`. If you want to return the same instance reach time (Singleton), tune a little bit the configuration class:

```java
@Configuration
public class ThriftPersistenceManagerJavaConfigSample
{
	...
	private ThriftPersistenceManagerFactory pmf;
	private ThriftPersistenceManager manager;


	@PostConstruct
	public void initialize()
	{
		Map<String, Object> configMap = extractConfigParams();
		pmf = new ThriftPersistenceManagerFactory(configMap);
		manager = (ThriftPersistenceManager) pmf.createPersistenceManager();
	}

	@Bean
	public ThriftPersistenceManager getPersistenceManager()
	{
		return manager;
	}
}
```
## Usage

 Once the factory is configured, either with XML or java config, you just need to inject it in the target service:

```java
public class MyService {

	@Inject
	private CQLPersistenceManager manager;
	
	...
}
```
