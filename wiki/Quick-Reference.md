Let's consider the following entity for all the below examples:

```java
@Entity
public class User
{
	@Id
	private Long userId;

	@Column
	private String firstname;

	@Column
	private String lastname;

	@Column
	private Counter tweetCount;

	public User(){}

	public User(Long userId, String firstname, String lastname){...}

	public User(Long userId, String firstname, String lastname, Counter tweetCount){...}
}
```

 For all following example, the `manager` represents an instance of `CQLPersistenceManager` or `ThriftPersistenceManager`

<br/>
## Persisting a transient entity

```java
	manager.persist(new User(10L,"John","DOE"));
```

or

```java
	manager.merge(new User(10L,"John","DOE"));
```

Warning! Theoretically nothing prevents you from persisting twice different users with the same primary key. In this case, **all data from previous user will be wiped out and data from new user inserted**.

 **Achilles** `persist()` doest not work like an upsert. 

 The CQL implementation achieves it by a single INSERT statement, null values from the entity will be translated into column removal by the Cassandra engine. The Thrift implementation removes first the whole row before inserting new data, which is less efficient.

<br/>
## Updating an entity
### Updating a managed entity

```java
	User user = manager.find(User.class,10L);
	user.setFirstname("Jonathan");
	manager.merge(user);
```
 
 The drawback of the above update is the need to load the entity before updating it (the dreadful **read-before-write** pattern). However it has the benefit to guarantee you that **the entity you are updating does really exist**.

 To avoid reading before update, use **Direct Update** as described below

### Direct update

```java
	User user = manager.getReference(User.class,10L);
	user.setFirstname("Jonathan");
	manager.merge(user);
```

 Unlike `find()`, `getReference()` does not hit the database. **Achilles** simply instanciates a new `User` object, sets the primary key and returns a proxified version of the entity to you. Upon call to `merge()`, **Achilles** updates the firstname.

 If you are sure that you entity does exist in Cassandra, prefer `getReference()` to `find()`

<br/>
## Removing an entity
### Removing a managed entity
```java
	User user = manager.find(User.class,10L);
	manager.remove(user);
```
### Direct removal
```java
	manager.removeById(User.class,10L);
```

<br/>
## Finding clustered entities

 For all examples in this section, let's consider the following clustered entity representing a tweet line

```java
public class TweetLine
{
	@EmbeddedId
	private TweetKey id;

	@Column
	private String content;

	public static class TweetKey
	{
		@Order(1)
		private Long userId;

		@Order(2)
		LineType type;

		@Order(3)
		UUID tweetId;
	}

	public static enum LineType
	{ USERLINE, TIMELINE, FAVORITELINE, MENTIONLINE}
}
```

### Find by partition key and clustering keys

 Get the last 10 tweets from timeline, starting from tweet with lastUUID

```java
List<TweetLine> tweets = manager.sliceQuery(TweetLine.class)
			.partitionKey(10L)
			.fromClusterings(LineType.TIMELINE,lastUUID)
			.toClusterings(LineType.TIMELINE)
			.orderind(OrderingMode.DESCENDING)
			.limit(10)
			.get();
```
### Find by embedded ids

 Same as above but using `TweetKey` instead of clustering components

```java
List<TweetLine> tweets = manager.sliceQuery(TweetLine.class)
			.fromEmbededId(new TweetKey(10L,LineType.TIMELINE,lastUUID))
			.toClusterings(new TweetKey(10L,LineType.TIMELINE))
			.orderind(OrderingMode.DESCENDING)
			.limit(10)
			.get();
```

### Iterating through a large set of entities

 Fetch all timeline tweets by batch of 100 tweets
```java
Iterator<TweetLine> iterator = manager.sliceQuery(TweetLine.class)
			.partitionKey(10L)
			.fromClusterings(LineType.TIMELINE,lastUUID)
			.toClusterings(LineType.TIMELINE)
			.orderind(OrderingMode.DESCENDING)
			.iterator(100);

while(iterator.hasNext())
{
	TweetLine timelineTweet = iterator.next();
	...
}		
```

### Removing clustered entities  

Removing all timeline tweets
```java
	manager.sliceQuery(TweetLine.class)
		.partitionKey(10L)
		.fromClusterings(LineType.TIMELINE)
		.toClusterings(LineType.TIMELINE)
		.remove();
```

> Right now, due some technical limitation on the protocol side (internally it's feasible) it is not possible to perform range deletion. This limitation may be lifted in a near future when the protocol (native or Thrift) will support range deletion.


<br/>
## Querying Cassandra
### Native query
```java
	List<Map<String,Object>> rows = manager.nativeQuery(
					"SELECT firstname,lastname FROM user LIMIT 100");
	
	for(Map<String,Object> row : rows)
	{
		String firstname = row.get("firstname");
		String lastname = row.get("lastname");
		...
	}
```

 Please note that the returned Map structure is indeed a `LinkedHashMap` which preserves the insertion order, which is the order of the columns declared in the query string (here _firstname_ then _lastname_ )

### Typed query
```java
	List<User> users = manager.typedQuery(User.class,
				"SELECT userId,firstname,lastname FROM user LIMIT 100");
	
	for(User user : user)
	{
		...
	}
```
 Please note that the **Typed Query** returned _managed_ instanced of the entities. Thus the **primary key/compound primary keys/select * should be present in the query string**.

### Raw typed query
```java
	List<User> users = manager.rawTypedQuery(User.class,
					"SELECT firstname,lastname FROM user LIMIT 100");
	
	for(User user : user)
	{
		...
	}
```
Similar to **Typed Query**, except that the resuls are transient entities. Consequently the restriction on mandatory primary key columns in the query string is lifted.

<br/>
## Working with consistency
### Defining consistency statically

```java
@Entity
@Consistency(read=ConsistecyLevel.ONE,write=ConsistencyLevel.QUORUM)
public class User
{
	...
}
```
### Setting consistency level at runtime

**Write consistency**
```java
	manager.persist(new User(10L,"John","DOE"), 
		OptionsBuilder.withConsistency(ConsistencyLevel.QUORUM));
```

or

```java
	manager.merge(new User(10L,"John","DOE"),
		OptionsBuilder.withConsistency(ConsistencyLevel.QUORUM));
```
<br/>
**Read consistency**
```java
	User user = manager.find(User.class,10L,
		OptionsBuilder.withConsistency(ConsistencyLevel.QUORUM));
```

or

```java
	User user = manager.getReference(User.class,10L,
		OptionsBuilder.withConsistency(ConsistencyLevel.QUORUM));
```

> The default consistency level is ONE when not set

Check **[OptionsBuilder]** for more details on available settings.

<br/>
## Working with TTL
### Setting TTL on an entity
```java
	manager.persist(new User(10L,"John","DOE"),
		OptionsBuilder.withTtl(150)); // Expire in 150 secs
```

or

```java
	manager.merge(new User(10L,"John","DOE"),
		OptionsBuilder.withTtl(150)); // Expire in 150 secs
```

### Setting TTL on individual field
```java
	User user = manager.getReference(User.class,10L);
	user.setFirstname("temporary firstname");

	/* Firstname value will expire in 150 secs, 
	 * leaving the user with only userId and
	 * lastname
	 */	
	manager.merge(user,OptionsBuilder.withTtl(150));
```

Please notice the usage of `getReference()` to save a read from Cassandra

Check **[OptionsBuilder]** for more details on available settings.

<br/>
## Working with Timestamp
### Setting Timestamp on an entity
```java
	// Set timestamp value on the all fields of User entity
	manager.persist(new User(10L,"John","DOE"), 
			OptionsBuilder.withTimestamp(1357949499999L)); 
```

or

```java
	// Set timestamp value on the all fields of User entity
	manager.merge(new User(10L,"John","DOE"),
			OptionsBuilder.withTimestamp(1357949499999L));
```

### Setting Timestamp on individual field
```java
	User user = manager.getReference(User.class,10L);
	user.setFirstname("temporary firstname");

	/* Only firstname value will have timestamp set 
	 * to 1357949499999L
	 */	
	manager.merge(user,OptionsBuilder.withTimestamp(1357949499999L));
```

Check **[OptionsBuilder]** for more details on available settings.

<br/>
## Setting multiple options
Of course it is possible to specify the TTL value, timestamp and/or consistency level at the same time:

```java
	manager.persist(new User(10L,"John","DOE"), OptionsBuilder
                .withConsistency(QUORUM)
                .ttl(10)
                .timestamp(1357949499999L)); 
```
or

```java
	manager.merge(new User(10L,"John","DOE"), OptionsBuilder
                .withConsistency(QUORUM)
                .ttl(10)
                .timestamp(1357949499999L)); 
```

Check **[OptionsBuilder]** for more details on available settings.

<br/>
## Using counter type
### Persisting new counter value

```java
	// Creating new user John DOE with 13 tweets
	manager.persist(new User(10L,"John","DOE",CounterBuilder.incr(13L)));
```

### Updating counter value from managed entity
```java
	User user = manager.find(User.class,10L);

	// Increment tweet count by 2
	user.getTweetCount().incr(2L);
```

### Direct update of counter value
```java
	User user = manager.getReference(User.class,10L);

	// Increment tweet count by 2
	user.getTweetCount().incr(2L);
```
 Again, direct update using `getReference()` is much more performant than the `find()` version

### Updating counter value with Consistency Level
```java
	User user = manager.getReference(User.class,10L);

	// Increment tweet count by 2 with consistency QUORUM
	user.getTweetCount().incr(2L,ConsistencyLevel.QUORUM);
```

<br/>
## Unwrapping entities
```java
	User user = manager.find(User.class,10L);

	// Transient instance of User entity
	User transient = manager.unwrap(user);
```

 The `unwrap()` method also accept `List` or `Set` of entities as argument

[OptionsBuilder]: https://github.com/doanduyhai/Achilles/wiki/Achilles-Custom-Types#optionsbuilder
