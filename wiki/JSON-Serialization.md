## Default configuration
 
Internally, **Achilles** stores data in JSON format using the **Jackson Mapper** library.
 JSON serialization is way faster than the old plain Object serialization since only data are serialized,
 not class structure.
 
 By default, **Achilles** sets up an internal **Object Mapper** with the following feature config:
 
  1. Serialization Inclusion = NON NULL
  2. Deserialization FAIL_ON_UNKNOWN_PROPERTIES = false
  3. JacksonAnnotationIntrospector + JaxbAnnotationIntrospector
  
**Jackson** will serialize all your entities even if they do not have any JSON annotations. You can also 
 use **JAXB** annotations.

<br/> 
## Custom Object Mapper

It is possible to inject a pre-configured **Jackson Object Mapper** as configuration parameter to bootstrap the `PersistenceManagerFactory` class using the _**"achilles.json.object.mapper"**_ parameter.

```java
Map<String,Object> configMap = new HashMap<String,Object>();
configMap.put("achilles.json.object.mapper", preConfiguredObjectMapper);

...

CQLPersistenceManagerFactory emf = new CQLPersistenceManagerFactory(configMap);
```

<br/> 
## Custom Object Mapper Factory
 
Last but not least, it is possible to further custom JSON serialization using the `ObjectMapperFactory` interface using the _**"achilles.json.object.mapper.factory"**_ parameter:
```java
public interface ObjectMapperFactory
{
	public <T> ObjectMapper getMapper(Class<T> type);
} 


Map<String,Object> configMap = new HashMap<String,Object>();
configMap.put("achilles.json.object.mapper.factory", customObjectMapperFactoryImpl);

...

CQLPersistenceManagerFactory emf = new CQLPersistenceManagerFactory(configMap);
```

> When both _**"achilles.json.object.mapper.factory"**_ and _**"achilles.json.object.mapper"**_ params are provided for configuration, **Achilles** will ignore the _**"achilles.json.object.mapper"**_ param and only use the _**"achilles.json.object.mapper.factory"**_ one
