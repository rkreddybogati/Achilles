## API

To support **Cassandra** distributed counters, **Achilles** introduces the **Counter** type with the following API:

```java
	public Long get();

	public Long get(ConsistencyLevel readLevel);

	public void incr();

	public void incr(ConsistencyLevel writeLevel);

	public void incr(Long increment);

	public void incr(Long increment, ConsistencyLevel writeLevel);

	public void decr();

	public void decr(ConsistencyLevel writeLevel);

	public void decr(Long decrement);

	public void decr(Long decrement, ConsistencyLevel writeLevel);
```

 The **Counter** type is a proxy object. Consequently:

 * a **Counter** type can only be created from a _managed_ entity
 * it is not sensible to invoke setter on a **Counter** field on a _managed_ entity
 * to persist a transient entity with initial counter value, use **[Counter Builder]**  

Example:
```java
// Bean mapping
@Entity
public class UserEntity {

	...

	@Column
	private Counter tweetsCount;

	...
}

//Usage
...
UserEntity user = manager.find(UserEntity.class,10L);

Counter tweetsCount = user.getTweetCounts();

// Increment the tweet counter
tweetsCount.incr();

// Get the current count for user tweets
Long currentTweetsCount = tweetsCount.get();
...
```

<br/>
## Implementation

 There are two types of Counter proxy supported by **Achilles** : simple counter and clustered counters.

### Simple counters mapping

##### CQL
For simple counters, **Achilles** creates an unique counter table named **achilles_counter_table** (or **achillesCounterCF** for the **Thrift** version) to store counter values. The structure of this table is:
<pre>
CREATE TABLE achilles_counter_table (
	fqcn text,
	primary key text,
	property_name text,
	counter_value counter,
	PRIMARY KEY ((fqcn,primary_key),property_name)
)
</pre>

The compound primary key has 3 components:

* the first component is the fully qualified class name (FQCN) of the entity
* the second component is the primary key of the entity, serialized with **Jackson** mapper to **String** type 
* the third component is the property name of the counter field in the entity. Thus for a same entity instance, **Achilles** can store many distinct counters
* the partition key is a composite with the **fqcn** and entity **primary key** serialized in **JSON**


##### Thrift

For **Thrift** implementation, the column family design is:

 1. Key validation class of type **Composite(UTF8Type,UTF8Type)**
 2. Comparator of type **Composite(UTF8Type)** 
 3. Validation class of type **CounterType** 

The partition key is a composite with **fqcn** and **primary key** in **JSON** format as components.

The column comparator has only one String component and stores the **field name** of the counter field in the entity.

The validation class is of course the native **CounterType**

##### Example

Example is better than words, let's consider the following mapping:

```java
package com.achilles.entity;

public class StockEntity {

	@Id
	private Long itemId;

	@Column
	private Counter stockCount;

	...
}

public class RatingEntity {

	@Id
	private Long itempId;
 
	@Column
	private Counter likesCount;

	...
}

// Increment the stock for itemId 10 of 5 units
StockEntity stock = manager.find(StockEntity.class,10L);
stock.getStockCount().incr(5L);

// Add a new like on this item
RatingEntity rating = manager.find(RatingEntity.class,23L);
rating.getLikesCount().incr();

```

 The above examples introduces 2 entities, one representing the stock state for an item and the other representing the popularity of this itmanager.

 Assuming that the initial value is 0 for both counters, below is the representation of the _stockCount_ and _likesCount_ counters in **Cassandra** storage engine:

<br/>
<table border="1">
	<thead>
		<tr>
			<th>Partition key (row key)</th>
			<th>Colum name/Value</th>
		</tr>
	</thead>
	<tbody>
		<tr>
			<td>"com.achilles.entity.StockEntity":"10"</td>
			<td align="center">"stockCount"/5</td>
		</tr>
		<tr>		
			<td>"com.achilles.entity.RatingEntity":"23"</td>
			<td align="center">"likesCount"/1</td>
		</tr>
	</tbody>
</table>
 
### Clustered Counter mapping

 For clustered counters **Achilles** creates a dedicated counter column family to store the counter values.
 A clustered counter entity is defined as an entity

 1. Having an `@EmbeddedID`
 2. Having one unique `@Column` of **Counter** type

Example:

```java
// Bean Mapping
@Entity
@Table(name="tweet_counters")
public class TweetCountersEntity {

	@EmbeddedId
	private CounterKey id;

	@Column
	private Counter value;

	...

	public static class CounterKey
	{
		@Order(1)
		private Long userId;

		@Order(2)
		private String counterType;
	}
}

// Usage
TweetCountersEntity  timelineCounter = manager.find(TweetCountersEntity.class,new CounterKey(10L,"TIMELINE"));
TweetCountersEntity  userlineCounter = manager.find(TweetCountersEntity.class,new CounterKey(10L,"USERLINE"));
 

// Increment the tweet count for the timeline of 2 units
timelineCounter.getValue().incr(13L);

// Increment the tweet count for the user tweet of 2 units
userlineCounter.getValue().incr(2L);

```
 
 Assuming that the initial value is 0 for both counters, below is the representation of the clustered counters in **Cassandra** storage engine:

<table border="1">
	<thead>
		<tr>
			<th>Partition key (row key)</th>
			<th>Colum name/Value</th>
			<th>Colum name/Value</th>
		</tr>
	</thead>
	<tbody>
		<tr>
			<td>10</td>
			<td align="center">"TIMELINE"/13</td>
			<td align="center">"USERLINE"/2</td>
		</tr>
	</tbody>
</table>

<br/>


<br/>
## Immediate flushing

 Since distributed counters have been created to have real-time update on counters values across many nodes, there is no sense in delaying the flushing of counter values. With this in mind, all operations on counter type will be flushed immediately to **Cassandra** by **Achilles**, even in **[Batch mode]**.

<br/>
## Technical limitation

 The technical limitations on **Counter** type are bound to their current implementation in **Cassandra**, namely:

 * it is not possible to set a **[TTL]** on a counter value, because counter type has no tombstone
 * it is not possible to set a counter value to an arbitrary value. Only increment and decrement operations are allowed
 * it is not possible to **delete/reset** a counter value. However, it is possible to remove the entire counter column, though it is not really recommended. While removing a counter column, you may get non null value if you performs a read immediately after the removal

[Batch mode]: https://github.com/doanduyhai/Achilles/wiki/Batch-Mode#batch-mutations
[TTL]: https://github.com/doanduyhai/Achilles/wiki/Quick-Reference#setting-ttl-on-an-entity
[Counter Builder]: https://github.com/doanduyhai/Achilles/wiki/Achilles-Custom-Types#counterbuilder
