Below is a list of operations supported by **Achilles** PersistenceManager:

### Persist transient entity
`persist(Object entity)` : Persist an entity.

`persist(Object entity, Options options)` : persist an entity with the given **options**. More details on **[Options]**


> Remark: If you persist twice two instances of the same entity class and same primary key, all previous values will be removed or replaced by new ones. Example is better than words

```java
@Entity 
public class UserEntity 
{
    //First constructor with login
    public UserEntity(Long userId,String login)
    {
        ...
    }

    //Second constructore more comprehensive
    public UserEntity(Long userId,String login,String firstname,String lastname,Long age)
    {
        ...
    }
}    

manager.persist(new UserEntity(10L,"jdoe","John","Doe",32));
manager.persist(new UserEntity(10L,"jdoe"));
```

 After the second `manager.persist()`, the `UserEntity` table will contain only 2 values : `userId` and `login`. Setting a value to **null** is equivalent to removing the corresponding column in Cassandra.

<br/>
### Find entities & Get Reference on entities

`find(Class<T> clazz,Object primaryKey)`: find an entity by its primary key. Quite straightforward. The returned entity is in *managed* state

`find(Class<T> clazz,Object primaryKey, Options options)`: find an entity by its primary key using the given **options**. Only Consistency level is used. TTL and timestamp values are ignored if set. More details on **[Options]**

<br/>
<br/>
`T getReference(Class<T> entityClass, Object primaryKey)`: 

1. Create a new instance of type T
2. Assign the provided `primaryKey` to this instance
3. Return the **proxified** instance

Basically, calling `getReference()` will not hit the database thus there is **no guarantee** that the instance with `primaryKey` really exists in **Cassandra**. 

 The main use-cases for `getReference()` are:

- Update of entities for which you know the primary key and you are sure they exist. If `find()` is used
there will be an unnecessary read from **Cassandra** to load eager fields first
- Access **Counter** proxy fields. In this case you don't need loading eager fields of the entities

`T getReference(Class<T> entityClass, Object primaryKey, Options options)`: same as _T getReference(Class<T> entityClass, Object primaryKey)_ but using the given **options**. Only Consistency level is used. TTL and timestamp values are ignored if set. More details on **[Options]**

<br/>
### Merge modifications
`T merge(T entity)`: merge the state of a *managed* entity. Flush the changes to **Cassandra** and return a new *managed* entity. The returned entity is in *managed* state.

> **Achilles** implementation of *merge()* is a little bit different from the JPA version. The JPA version makes the entity passed as argument of *merge()*  detached. With **Achilles**, the entity passed as argument remains in *managed* state and is returned by the *merge()* operation.
 
Example:
```java
    User user = manager.find(User.class,1L);
    user.setFirstname("DuyHai");

    User mergedUser = manager.merge(user);

    // mergedUser and user are the same object
    assertTrue(mergedUser.equals(user));
```

<br/>
> **Calling `merge()` after setting a property of an entity to `null` will trigger removal of this entity from Cassandra**

<br/>
`T merge(T entity, Options options)`: same as _T merge(T entity)_ but using the given **options**. More details on **[Options]**

One common use case of this operation is to assign one particular field of an entity a TTL or timestamp.

In the below example, we want to add a TTL to the `firstname` property of an user

Example:
```java
    User user = new User();
    user.setLastname("DOAN");
    user.setFirstname("DuyHai");

    user = manager.merge(user);

    // Make firstname dirty by setting the same value
    user.setFirstname(user.getFirstname());

    // Apply change to firstname with ttl = 2 secs
    User mergedUser = manager.merge(user,OptionsBuilder.withTtl(2));

    // Wait 3 secs
    Thread.sleep(3000);

    // Reload user state from Cassandra
    manager.refresh(mergedUser);

    // The firstname property now is null because it has expired due to 2secs ttl
    assertTrue(mergedUser.getFirstname() == null);
```


<br/>
### Removing entities
`remove(T entity)`: remove a *managed* entity. All counters values related to this entity are also removed. If the entity is *transient*, the methods will raise an **IllegalStateException**.

`remove(T entity, Options options)`: same as _remove(T entity)_ but using the given **options**. Only Consistency level is used. TTL and timestamp values are ignored if set. More details on **[Options]**

<br/>
<br/>
`removeById(T entity, Object primaryKey)`: remove an entity by its primary key. This method proves to be quite convenient because you need not load the entity before removing it as with `remove()`

`removeById(T entity, Object primaryKey, Options options)`: same as _removeByUd(T entity,Object primaryKey)_ but using the given **options**.

<br/>
### Refreshing managed entities
`refresh(Object entity)`: refresh a *managed* etity. If the entity is *transient*, the methods will raise an **IllegalStateException**.
 
> Behind the scene, **Achilles** will load the entity from **Cassandra**. All *lazy* fields that have been previously loaded are cleared and will be re-loaded again upon getter invocation.


<br/>
`refresh(Object entity, Options options)`: same as _refresh(Object entity)_ but using the given **options**.

<br/>
### Initialization and unwrapping
 The following methods are useful for dealing with proxified entities

<br/>
`void initialize(Object entity)`: this operation is **Achilles** specific. It initializes all lazy associations for the provided entity (except **WideMap/Counter** fields of course). The entity should be in *managed* mode otherwise an **IllegalStateException** will be thrown.

<br/>
`void initialize(List<Object> entities)`: same as _initialize(Object entity)_ but for a list of *managed* entities

`void initialize(Set<Object> entities)`: same as _initialize(Object entity)_ but for a set of *managed* entities

`Object unwrap(Object proxy)`: return the **real** object behind the proxified entity. When invoked on a **non-managed** entity, Achilles simply returns the object itself.

> Please note that all lazy fields that were not loaded in the proxy won't be available on the real
object. A common pattern is to call `initialize(proxy)` first before unproxying. Refer to **[Working with proxified entities]** for more details

<br/>
`List<Object> unwrap(Collection<Object> proxies)`: same as _unwrap(Object proxy)_ but for a list of proxies. Retuns an `ArrayList` instance

`Set<Object> unwrap(Set<Object> proxies)`: same as _unwrap(Object proxy)_ but for a set of proxies. Retuns a `HashSet` instance

`T initAndUnwrap(T entity)`: short-hand for _initialize(entity)_ followed by _unwrap(entity)_

`List<T> initAndUnwrap(List<T> entity)`: same as _T initAndUnwrap(T entity)_ but for a `List`

`Set<T> initAndUnwrap(Set<T> entity)`: same as _T initAndUnwrap(T entity)_ but for a `Set`

<br/>
### Misc
`Object getDelegate()`: simply return the current PersistenceManager instance.

`Session getNativeSession()`: **CQL3** specific method which returns the underlying Java Driver core Session object

[Options]: https://github.com/doanduyhai/Achilles/wiki/Achilles-Custom-Types#options
[Working with proxified entities]: https://github.com/doanduyhai/Achilles/wiki/Working-with-proxified-entities
