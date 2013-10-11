### Bootstrap #

 First of all you need to initialize the **CQLPersistenceManagerFactory** (or **ThriftPersistenceManagerFactory**).

```java
Map<String,Object> configMap = new HashMap<String,Object>();
	
configMap.put("achilles.entity.packages","my.package1,my.package2");
configMap.put("achilles.cassandra.connection.contactPoints","localhost");
configMap.put("achilles.cassandra.connection.port","9041");
configMap.put("achilles.cassandra.keyspace.name","Test Keyspace");
configMap.put("achilles.ddl.force.column.family.creation",true);

CQLPersistenceManagerFactory persistenceManagerFactory = new CQLPersistenceManagerFactory(configMap);
```

 The *"achilles.ddl.force.column.family.creation"* parameter instructs **Achilles** to force the column families creation if they do not exist or not. 

 This flag should be set to **false** most of the time when going to Prod. Set it to **true** during development phase  because **Achilles** creates its own column family structure for persistence and it is very unlikely that your existing column families are compatible with **Achilles** structure (but it can be in some cases, check **[DDL Scripts Generation]**)


 For **Spring** users:
```xml
<bean id="achillesPersistenceManagerFactory" class="info.archinnov.achilles.integration.spring.CQLPersistenceManagerFactoryBean"
	init-method="initialize">
	<property name="entityPackages" value="my.package1,my.package2"/>
	<property name="contactPoints" value="localhost"/>
	<property name="port" value="9042"/>
	<property name="keyspaceName" value="Test Keyspace"/>
	<property name="forceColumnFamilyCreation" value="true" />
</bean>	
```
 For complete configuration of the factory bean, please refer to **[Spring Integration]**

 Once the factory bean is defined, you can just inject the PersistenceManager into any of your service

```java

	@Inject
	private CQLPersistenceManager manager;
	...
```

### Bean Mapping #

 Let's create an **User** bean in JPA style

```java
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import org.achilles.annotations.Lazy;
 
@Entity 
@Table(name="users")
public class User 
{
	@Id
	private Long id;

	@Column
	private String firstname;

	@Column
	private String lastname;
		
	@Column(name="age_in_year")
	private Integer age;
		
	@Column
	private Biography bio;
		
	@Lazy
	@Column
	private List<String> favoriteTags;
		
	@Column
	private Map<Integer,String> preferences;
		
	// Getters and setters ...
}

public class Biography 
{
	private String birthPlace;
	private List<String> diplomas;
	private String description; 
	
	//Getters and Setters
}
```
 All fields are eagerly fetched except the *favoriteTags* list annotated by **@Lazy**. 
 The list will be loaded **entirely** when calling *getFavoriteTags()*.
	
### Usage #

 First we create an **User** and persist it

```java

User user = new User();
user.setId(1L);
user.setFirstname("DuyHai");
user.setLastname("DOAN");
user.setAge(30);
	
// Biography
Biography bio = new Biography();
bio.setBirthPlace("VietNam");
bio.setDiplomas(Arrays.asList("Master of Science","Diplome d'ingenieur"));
bio.setDescription("Yet another framework developer");	
user.setBio(bio);

// Favorite Tags
Set<String> tags = new HashSet<String>();
tags.add("computing");
tags.add("java");
tags.add("cassandra");
user.setFavoriteTags(tags);
	
// Preferences
Map<Integer,String> preferences = new HashMap<Integer,String>();
preferences.put(1,"FR");
preferences.put(2,"French");
preferences.put(3,"Paris");
	
user.setPreferences(preferences);

// Save user
manager.persist(user);
```
 Then we can find it by id:

```java
User foundUser = manager.find(User.class,1L);
	
// Now add some new tags
foundUser.getFavoriteTags().add("achilles"); 
	
// Save it
foundUser = manager.merge(foundUser);

assertEquals("achilles",foundUser.getFavoriteTags().get(3));
```

### Clustered entities

 To use **Cassandra** native wide rows structure, you can define a **clustered entity**. An entity is **clustered** when it has an `@EmbeddedId` which is an object representing a compound primary key:

1. the first component represents the partition key (or row id in term of storage engine)
2. the following components are called **clustering** components and represent column name in the storage engine



```java
@Entity
@Table(name="timeline")
public class Timeline
{
	@EmbeddedId
	private TweetKey id;

	@Column
	private Tweet tweet;

	// Default constructor mandatory
	public Timeline(){}

	// Custom constructor for convenience
	public Timeline(Long userId,Tweet tweet)
	{
		this.id = new TweetKey(userId,tweet.getId());
		this.tweet = tweet;
	}


	public static class TweetKey 
	{
		@PartitionKey // optional annotation. 
		@Column @Order(1)
		private Long userId;

		// Clustering component
		@Column @Order(2)
		private UUID tweetId;

		public TweetKey(){}

		public TweetKey(Long userId, UUID tweetId)
		{
			this.userId = userId;
			this.tweetId = tweetId;
		}
	}
}
```

 **Tweet** here is a simple POJO.

 To insert new tweets to the user timeline:

```java

Long userId = user.getId();
	
Tweet tweet1 = new Tweet(), tweet2 = new Tweet(), 
      tweet3 = new Tweet(), tweet4 = new Tweet();
	
	
// Insert tweets
manager.persist(new Timeline(userid,tweet1));
manager.persist(new Timeline(userid,tweet2));
manager.persist(new Timeline(userid,tweet3));
	
// Insert with TTL
manager.persist(new Timeline(userid,tweet4),OptionsBuilder.withTtl(150));

...
...
```
	
 Later, you can retrieved the saved tweets using **SliceQueryBuilder** :
 
```java
// Should return tweet2 & tweet3
List<Timeline> foundRange = manager.sliceQuery(Timeline.class)
	.partitionKey(userId)
	.fromClustering(uuid2)
	.toClustering(uuid4)
	.limit(10)
	.boundind(BoundingMode.INCLUSIVE_START_BOUND_ONLY)
	.ordering(OrderingMode.ASCENDING)
	.get();
	
assertEquals(foundRange.size(),2);	
	
// Assuming that you implemented equals() & hashCode() for Tweet class
assertEquals(foundRange.get(0).getTweet(),tweet2); 
assertEquals(foundRange.get(1).getTweet(),tweet3);
```

 And that's it. To have more details on the advanced features, please check the **[Documentation]**.	

[Documentation]: https://github.com/doanduyhai/Achilles/wiki
[DDL Scripts Generation]: https://github.com/doanduyhai/Achilles/wiki/DDL-Scripts-Generation
[Spring Integration]: https://github.com/doanduyhai/Achilles/wiki/Spring-Integration
[WideMap API]: https://github.com/doanduyhai/Achilles/wiki/WideMap-API
