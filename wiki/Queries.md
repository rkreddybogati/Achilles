## Slice Query

### Presentation
 Slice queries in **Achilles** are designed to work along side with **[Clustered Entity]**.

 You can retrieve a clustered entity calling the `find()` method on an `PersistenceManager`, providing the compound primary key.

 You can also retrieve a list of clustered entities using the **Slice Query Builder**. Example

```java
// Get all clustered entities having
// 1. partition key 10L
// 2. in descending order
// 3. from clustering component 100 to clustering component 25 as bounds
// 4. excluding start clustering component (100)
// 5. limiting the returned values to maximum 50 entities/rows

List<ClusteredEntity> entities = manager.sliceQuery(ClusteredEntity.class)
                .partitionKey(10L)
                .fromClusterings(100)
                .toClusterings(25)
                .bounding(BoundingMode.INCLUSIVE_END_BOUND_ONLY)
                .ordering(DESCENDING)
                .limit(50)
                .get();
```

 With the **Slice Query Builder** you can:

1. Retrieve a list of clustered entities
2. Get an iterator to iterate over a range of clustered entities, avoiding loading all of them at once in memory
3. Remove a range of clustered entities


 * All slice queries always start with a partition key/partition key components. 
 * Clustering keys are optional and can be skipped. 
 * Default ordering is ASCENDING
 * Default bounding mode is INCLUSIVE BOUNDS
 * Default limit is set to 100 rows (to avoid OutOfMemoryException). If you expect to fetch more than 100 rows, just set it explicitely

<br/>
### API
Below are different **methods trees** for slice query builder

 **Query by partition key and clustering keys** 

```java
sliceQuery(ClusteredEntity.class).
partitionKey(Object...partitionComponents)
      fromClusterings(Object...clusterings) ||  toClusterings(Object...clusterings)
            toClusterings(Object...clusterings) ||  fromClusterings(Object...clusterings)
            ordering(OrderingMode ordering) ||
            bounding(BoundingMode boundingMode) ||
            consistencyLevel(ConsistencyLevel consistencyLevel) ||
            limit(int limit) ||
            get() || // Retrieve a list of matching entities, limited by 'limit' or default 100 
            get(int n)||  // Retrieve first n matching entities
            iterator() || // Iterator on matching entities, loading by batch of 100 rows by default
            iterator(int batchSize) || // Iterator on matching entities, loading by batch of 'batchSize'
            remove() || // Remove matching entities
            remove(int n) || // Remove first n matching entities
```

 **Query by partition key and embedded ids**

```java
sliceQuery(ClusteredEntity.class).
partitionKey(Object pk)            
      fromEmbeddedId(Object fromEmbeddedId) || toEmbeddedId(Object toEmbeddedId) 
            toEmbeddedId(Object toEmbeddedId) || fromEmbeddedId(Object fromEmbeddedId)
            ordering(OrderingMode ordering) ||
            bounding(BoundingMode boundingMode) ||
            consistencyLevel(ConsistencyLevel consistencyLevel) ||
            limit(int limit) ||
            get() ||
            get(int n) || 
            iterator() ||
            iterator(int batchSize) ||
            remove() ||
            remove(int n) ||
```

 **Query by partition key only**
```java
sliceQuery(ClusteredEntity.class).
partitionKey(Object...partitionComponents) 
      consistencyLevel(ConsistencyLevel consistencyLevel) ||
      get() ||
      get(int n) ||
      getFirstOccurence(Object... clusteringComponents) || // Get first matching entity with clustering components
      getFirst(int n, Object... clusteringComponents) || // Get first n matching entities with clustering components
      getLastOccurence(Object... clusteringComponents) || // Get last matching entity with clustering components
      getLast(int n, Object... clusteringComponents) || // Get last n matching entities with clustering components
      iterator() ||
      iterator(int batchSize) ||
      iterator(Object... clusteringComponents) || // Iterator on matching entities with clustering components
      iterator(int batchSize, Object... clusteringComponents) || // Iterator on matching entities with clustering components, using 'batchSize'
      remove() ||
      remove(int n) ||
      removeFirstOccurence(Object... clusteringComponents) || // Remove first matching entity with clustering components
      removeFirst(int n, Object... clusteringComponents) || // Remove first n matching entities with clustering components
      removeLastOccurence(Object... clusteringComponents) || // Remove last matching entity with clustering components
      removeLast(int n, Object... clusteringComponents)|| // Remove last n matching entities with clustering components
```

<br/>
### Clustering components validation rules

 For a set of clustering components to be valid provided for slice query, they should:

1. be all null or 
2. have no "hole" (null) between components
3. be not complete

Let's consider the following compound primary key:

```java
public class MyKey 
{	
	@Order(1)
	private Date partitionKey;

	@Order(2)
	private Long id;

	@Order(3)
	private String name;
	
	@Order(4)
	private UUID uuid;
}
```

The following clustering components sets are valid:

- null
- **{id=1L,null,null}**
- **{id=1L,name="test",null}**
- **{id=1L,name="test",uuid='e39707f0-5a6c-11e2-ab85-685d43d1d7d3'}**

The following are not valid:

- **{null,name="test",null}**
- **{null,name="test",uuid='e39707f0-5a6c-11e2-ab85-685d43d1d7d3'}**
- **{id=1L,null,uuid='e39707f0-5a6c-11e2-ab85-685d43d1d7d3'}**

<br/>
### Clustering components ordering rules

When doing slice queries with starting and ending bounds in Cassandra, you must ensure that the starting bound is less than ending bound, with respect to the natural ordering of the type.

For simple types like `Integer` or `String`, the natural ordering is obvious. For compound keys, the ordering is obtained by comparing component by component with regard to their order in the key:

* first component of starting bound with the one of the ending bound
* second component of starting bound with the one of the ending bound
* etc..

For example:

1. **{id=10L,name="test",null} > {id=1L,name="test",null}** because 10L > 1L
2. **{id=10L,null,null} > {id=1L,name="test",null}** still because 10L > 1L. The name components do not matter as long as the id components are not equal
3. **{id=10L,name="test",null} = {id=10L,name="test",null}**
4. **{id=10L,name="test",null} > {id=10L,name="xxx",uuid='e39707f0-5a6c-11e2-ab85-685d43d1d7d3'}** because "test" > "xxx" 

<br/>
### Known limitations

 For **CQL** implementation,

1. It is not possible to have more than **one varying component** between `from` and `to` components sets. The varying component should be the highest one with respect to their index in the compound primary key.
  Example:
    - fromClustering **{id=10L,name="a"}**, toClustering **{id=10L,name="Z"}** : **Valid**
    - fromClustering **{id=10L,name="a",uuid='e39707f0-5a6c-11e2-ab85-685d43d1d7d3'}**, toClustering **{id=10L,name="Z",uuid='7a963fc0-0069-11e3-b778-0800200c9a66'}** : **Invalid**

2. It is not possible to have more than **2 different components** between `from` and `to` components sets
  Example:
    - fromClustering **{id=10L}**, toClustering **{id=10L,name="Z"}** : **Valid**
    - fromClustering **{id=10L}**, toClustering **{id=10L,name="Z",uuid='7a963fc0-0069-11e3-b778-0800200c9a66'}** : **Invalid**

3. Range delete is not supported right now for different clustering components, even if they respect the 2 rules above. 
4. Range delete does not support **LIMIT clause** in CQL3. Thus methods like `limit(int n)`, `removeFirst(...)`, `removeLast(...)` and `remove(int n)` are not supported by the **Slice Query Builder** for deletion

<br/>
## Native Query

### Usage

 It is possible to execute native CQL3 queries. The query execution is simply delegated by **Achilles** to the underlying Java driver and the returned rows are mapped into `List<Map<String,Object>>`.

* Each element of the list represent one CQL3 row.
* The map contains a collection of entries which represent the column name and column value for each row
* The map entries are sorted by the order in which columns are declared in the projection clause of the query

Example:

```java
        String nativeQuery = "SELECT name,age_in_years FROM UserEntity WHERE id IN(10,11)";

        List<Map<String, Object>> actual = manager.nativeQuery(nativeQuery).get();

        assertThat(actual).hasSize(2);

        Map<String, Object> row1 = actual.get(0);
        Map<String, Object> row2 = actual.get(1);

        assertThat(row1.get("name")).isEqualTo("Helen SUE");
        assertThat(row1.get("age_in_years")).isEqualTo(35L);

        assertThat(row2.get("name")).isEqualTo("John DOO");
        assertThat(row2.get("age_in_years")).isEqualTo(35L);
```


<br/>
### Mapping of CQL3 functions

 In CQL3, in the `SELECT` clause it is possible to use native functions. These are:

 1. `count(*)` or `count(1)`: counting the number of rows matching the selection predicate
 2. `ttl(column_name)`: time-to-live value of a given **column_name**. Value returned as `Long` type
 3. `writetime(column_name)`: timestamp value of a given **column_name**. Value returned as `Long` type
 4. `now()`: return current timestamp as version 1 `UUID` type
 5. `dateOf(column_name)`: date value of the given **column_name** column as `Date` type. The **column_name** type must be defined as `timeuuid` in Cassandra
 6. `unixTimestampOf(column_name)`: unix timestamp of the given **column_name** column as `Long` type. The **column_name** type must be defined as `timeuuid` in Cassandra

<br/>
> **All above functions cannot be applied on primary key/compound primary key column(s)**

<br/>
For each of the above function, the returned column name for native query is:

<center>
<table border="1">
	<thead>
		<tr>
			<th>CQL3 function</th>
			<th>Returned column name</th>
		</tr>
	</thead>
	<tbody>
		<tr>
			<td>`count(*)` or `count(1)`</td>
			<td>count</td>
		</tr>
		<tr>
			<td>`ttl(column_name)`</td>
			<td>ttl(column_name)</td>
		</tr>
		<tr>
			<td>`writetime(column_name)`</td>
			<td>writetime(column_name)</td>
		</tr>
		<tr>
			<td>`now()`</td>
			<td>now()</td>
		</tr>
		<tr>
			<td>`dateOf(column_name)`</td>
			<td>dateOf(column_name)</td>
		</tr>
		<tr>
			<td>`unixTimestampOf(column_name)`</td>
			<td>unixTimestampOf(column_name)</td>
		</tr>   
	</tbody>
</table>
</center>

<br/>
 Example:
```java

Long id = RandomUtils.nextLong();
UUID date = UUIDGen.getTimeUUID();

session.execute("CREATE TABLE test_functions(id bigint PRIMARY KEY,date timeuuid)");
        session.execute("INSERT INTO test_functions(id,date) VALUES(" + id + "," + date + ")");

Map<String, Object> result = manager.nativeQuery(
        "SELECT now(),dateOf(date),unixTimestampOf(date) FROM test_functions WHERE id="+ id).first();

session.execute("DROP TABLE test_functions");

assertThat(result.get("now()")).isNotNull().isInstanceOf(UUID.class);
assertThat(result.get("dateOf(date)")).isNotNull().isInstanceOf(Date.class);
assertThat(result.get("unixTimestampOf(date)")).isNotNull().isInstanceOf(Long.class);
```

<br/>
## Typed Query

### Normal Typed Query

 Unlike native queries, typed queries map returned rows into managed entities. This is called a **typed query** because the query is typed by the returned entity type. Example:

```java

        String queryString = "SELECT * FROM MyEntity LIMIT 3";
        List<MyEntity> actual = manager.typedQuery(MyEntity.class, queryString).get();

```
 **Achilles** is acting as a super data mapper for native CQL3 queries. 

> Please note that the returned entities are in _managed_ state, meaning that you can invoke `merge()`, `remove()` or `refresh()` on thmanager. All eager fields that are normally fetch using `find()` may not be present. Only fields matching declared columns in the request are hydrated into the entities

 There is a mandatory requirement though on typed queries compared to native queries: **the primary key or all components of compound primary key of the entity should be present in the projection clause of the query**. This requirement is necessary because returned entities are _managed_ by **Achilles**, thus needing the primary key to be present.

<br/>
### Raw Typed Query

 Unliked normal type queries, raw typed queries return **transient entities**. The requirement on primary key being present in the query is lifted. Consequently the returned entities are not fully hydrated. 


```java

        String queryString = "SELECT * FROM MyEntity LIMIT 3";
        List<MyEntity> actual = manager.rawTypedQuery(MyEntity.class, queryString).get();

```



[Clustered Entity]: https://github.com/doanduyhai/Achilles/wiki/Entity-Mapping#clustered-entity
