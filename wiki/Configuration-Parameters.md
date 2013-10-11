# Parameters
**Achilles** comes with some configuration parameters:
## Common
##### Entity Packages

* **achilles.entity.packages** (MANDATORY): list of java packages for entity scanning, separated by comma.
  
  Example: _my.project.entity,another.project.entity_ 

##### DDL

* **achilles.ddl.force.column.family.creation** (OPTIONAL): create missing column families for entities if they are not found. **Default = 'false'**.

  If set to **false** and no column family is found for any entity, **Achilles** will raise an **AchillesInvalidColumnFamilyException**

##### JSON Serialization

* **achilles.json.object.mapper.factory** (OPTIONAL): an implementation of the _info.archinnov.achilles.json.ObjectMapperFactory_ interface to build custom Jackson **ObjectMapper** based on entity class
* **achilles.json.object.mapper** (OPTIONAL): default Jackson **ObjectMapper** to use for serializing entities

If both **achilles.json.object.mapper.factory** and **achilles.json.object.mapper** parameters are provided, **Achilles** will ignore the **achilles.json.object.mapper** parameter and use **achilles.json.object.mapper.factory**

If none is provided, **Achilles** will use a default Jackson **ObjectMapper** with the following configuration:

1. SerializationInclusion = Inclusion.NON_NULL 
1. DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES = false 
1. AnnotationIntrospector pair : primary = JacksonAnnotationIntrospector, secondary = JaxbAnnotationIntrospector 

##### Consistency Level


* **achilles.default.consistency.read** (OPTIONAL): default read consistency level for all entities
* **achilles.default.consistency.write** (OPTIONAL): default write consistency level for all entities
* **achilles.consistency.read.map** (OPTIONAL): map(String,String) of read consistency levels for column families/tables

  Example:

    "columnFamily1" -> "ONE" <br/>
    "columnFamily2" -> "QUORUM"
    ...
* **achilles.consistency.write.map** (OPTIONAL): map(String,String) of write consistency levels for column families

  Example:

    "columnFamily1" -> "ALL" <br/>
    "columnFamily2" -> "EACH_QUORUM"
    ...

## CQL parameters
##### Cluster and Keyspace

* **achilles.cassandra.connection.contactPoints**: comma separated list of contact points/hosts of the cluster 
* **achilles.cassandra.connection.port**: port for native protocole. **Default = 9042** 
* **achilles.cassandra.keyspace.name**: keyspace name

* **achilles.cassandra.cluster**: pre-built java driver core `Cluster` object
* **achilles.cassandra.native.session**: pre-built java driver core `Session` object

Either **achilles.cassandra.cluster/achilles.cassandra.native.session** or **achilles.cassandra.connection.contactPoints/achilles.cassandra.connection.port/achilles.cassandra.keyspace.name** parameters should be provided 

##### Compression

* **achilles.cassandra.compression.type** (OPTIONAL): `Compression.SNAPPY` or `Compression.NONE`. Default = `Compression.SNAPPY`

##### Policies

* **achilles.cassandra.retry.policy** (OPTIONAL): retry policy. Default = `Policies.defaultRetryPolicy()` of java driver core
* **achilles.cassandra.load.balancing.policy** (OPTIONAL): retry policy. Default = `Policies.defaultLoadBalancingPolicy()` of java driver core
* **achilles.cassandra.reconnection.policy** (OPTIONAL): retry policy. Default = `Policies.defaultReconnectionPolicy()` of java driver core

##### Credentials

* **achilles.cassandra.username** (OPTIONAL): user login
* **achilles.cassandra.password** (OPTIONAL): user password

##### Metrics

* **achilles.cassandra.disable.jmx** (OPTIONAL): disable JMX. **Default = false**
* **achilles.cassandra.disable.metrics** (OPTIONAL): disable metrics. **Default = false**

##### SSL

* **achilles.cassandra.ssl.enabled** (OPTIONAL): enable SSL **Default = false**
* **achilles.cassandra.ssl.options** (OPTIONAL): Instance of `com.datastax.driver.core.SSLOptions`. Should be set if the property  **achilles.cassandra.ssl.enabled** is true



## Thrift parameters
##### Cluster and Keyspace

* **achilles.cassandra.host**: hostname/port of the Cassandra cluster 

  Example: _localhost:9160_ 

* **achilles.cassandra.cluster.name**: Cassandra cluster name 
* **achilles.cassandra.keyspace.name**: Cassandra keyspace name 
* **achilles.cassandra.cluster**: instance of pre-configured me.prettyprint.hector.api.Cluster object from Hector API 
* **achilles.cassandra.keyspace**: instance of pre-configured me.prettyprint.hector.api.Keyspace object from Hector API 

Either **achilles.cassandra.cluster** or **achilles.cassandra.host/achilles.cassandra.cluster.name** parameters should be provided 

Either **achilles.cassandra.keyspace** or **achilles.cassandra.keyspace.name** parameters should be provided 

***


# Configuration
## CQL

 To configure **Achilles** with the above parameters, you need to provide a Map(String,Object) of key/value as constructor argument to the `CQLPersistenceManagerFactory` class.

Example:

```java
	Map<String,Object> configMap = new HashMap<String,Object>();
	
	configMap.put("achilles.entity.packages","my.package1,my.package2");
	configMap.put("achilles.cassandra.connection.contactPoints","localhost");
	configMap.put("achilles.cassandra.connection.port",9042);
	configMap.put("achilles.cassandra.keyspace.name","Test Keyspace");
	configMap.put("achilles.ddl.force.column.family.creation",true);

	CQLPersistenceManagerFactory pmf = new CQLPersistenceManagerFactory (configMap);
```
 For **Spring** users:
```xml
<bean id="achillesPersistenceManagerFactory" 
 	class="info.archinnov.achilles.integration.spring.CQLPersistenceManagerFactoryBean"
	init-method="initialize">
	<property name="entityPackages" value="my.package1,my.package2"/>
	<property name="contactPoints" value="localhost"/>
	<property name="port" value="9042"/>
	<property name="keyspaceName" value="achilles"/>
	<property name="objectMapper" ref="objectMapperFactoryBean"/>
	<property name="consistencyLevelReadDefault" value="ONE"/>
	<property name="consistencyLevelWriteDefault" value="ONE"/>
	<property name="forceColumnFamilyCreation" value="true" />
</bean>	
```

## Thrift 
Example:

```java
	Map<String,Object> configMap = new HashMap<String,Object>();
	
	configMap.put("achilles.entity.packages","my.package1,my.package2");
	configMap.put("achilles.cassandra.host","localhost:9160");
	configMap.put("achilles.cassandra.cluster.name","Test Cluster");
	configMap.put("achilles.cassandra.keyspace.name","Test Keyspace");
	configMap.put("achilles.ddl.force.column.family.creation",true);

	ThriftPersistenceManagerFactory pmf = new ThriftPersistenceManagerFactoryImpl(configMap);
```
 For **Spring** users:
```xml
<bean id="achillesPersistenceManagerFactory" 
	class="info.archinnov.achilles.integration.spring.ThriftPersistenceManagerFactoryBean"
	init-method="initialize">
	<property name="entityPackages" value="my.package1,my.package2"/>
	<property name="cassandraHost" value="localhost:9160"/>
	<property name="clusterName" value="Test Cluster"/>
	<property name="keyspaceName" value="achilles"/>
	<property name="objectMapper" ref="objectMapperFactoryBean"/>
	<property name="consistencyLevelReadDefault" value="ONE"/>
	<property name="consistencyLevelWriteDefault" value="ONE"/>
	<property name="forceColumnFamilyCreation" value="true" />
</bean>	
```
