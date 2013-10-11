### Un-wrapping

 Sometimes you may want to pass the entity managed by **Achilles** to a client over the wire. In this case 
 you need to rely on some serializer like **Jackson** or **JAXB** to name the few.
 
 The issue is that the *managed* entity is indeed a proxy object you don't want to pass to the serializer.
 The good old solution for this is creating DTO objects and some mappers to copy data back and forth.
 
 To save you this hassle, **Achilles** provides some useful methods to unwrap an *managed* 
 entity or a collection/list/set of *managed* entities:

```java
public <T> T unwrap(T proxy)
	
public <T> Collection<T> unwrap(Collection<T> proxies)
	
public <T> List<T> unwrap(List<T> proxies)
	
public <T> Set<T> unwrap(Set<T> proxies)
```

 This way you can pass the **real** object to your favorite serializer and avoid the DTO/VO pattern.

Example:
```java
@RequestMapping(value = "/timeline", method = RequestMethod.GET, produces = "application/json")
@ResponseBody
public List<Tweet> getTimeline(@RequestParam String userLogin, @RequestParam int length)
{
	return manager.unwrap(userService.getTimeline(userLogin, length));
}  
```

 In the above code, since the returned list of **Tweet** entities will be JSON serialized and returned to 
 the client, it's better to return the real object rather that the proxy one. 
  
### Forced initialization

 In the same manner as`Hibernate.initialize()`, **Achilles** provides a method to force initialization
 of all **lazy** fields on your entity:  `PersistenceManager.initialize()`.

```java
public <T> void initialize(T entity)
	
public <T> void initialize(Collection<T> entities)
	
public <T> void initialize(List<T> entities)
	
public <T> void initialize(Set<T> entities)
```
 This proves to be useful when you want to pass the entity to a serializer. You'd better initialize them before unwrapping.
