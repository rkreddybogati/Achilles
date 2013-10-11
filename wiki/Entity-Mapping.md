 For bean mapping, only **field-based access type** is supported by **Achilles**. Furthermore, there is no default mapping for fields. If you want a field to be mapped, you must annotate it with `@Column`/`@Id`/`@EmbeddedId`. All fields that are not annotated by either annotations is considered transient by **Achilles**
<br/>
## Common rules

 **Achilles** maps an entity into a **Cassandra** column family/table. Each row represents an instance of the entity and each column represents a field.

 For clustered entities, each tuple of (partition key, clustereting keys ...) represent a logical row although it translates into distinct columns by the underlying storage engine.
  
### Entity matching
* **Achilles** entities must conform to _JavaBean convention_ with respect to accessors.
* A Java bean is candidate to be an **Achilles** entity if it is annotated with `@Entity`/`@Table` or both
* All entities must have at least one field annotated with `@Id` or `@EmbeddedId`. This represents the entity primary key and maps to **Cassandra** partition key/compound primary key

<br/>
### Table name/Column family name

* If the attribute _name_ is provided by the **@Table** annotation, it will be used as table/column family name in **Cassandra** for the entity
* Otherwise, the table/column family name is derived from the entity class name (not fully qualified name) by replacing all **"."** characters by **"_"**

In any case, the provided or derived table/column family name should match the regexp pattern : `[a-zA-Z0-9_]{1,48}`

> The limitation to 48 characters comes from **Cassandra** restriction on column family name size

Examples:

```java
@Entity
public class MyEntity
{
	@Id
	private Long id;
	...
} 
```
Inferred table/column family name = **MyEntity**

```java
@Entity
@Table(name = "another_entity")
public class AnotherEntity 
{
	@Id
	private Long id;
	...
} 
```
Inferred table/column family name = **another_entity**
<br/>
### Field mapping

* A field is managed by **Achilles** if it is annotated with `@Column`/`@Id`/`@EmbeddedId`
* Below is the mapping between Java types and **Cassandra** serializers by **Achilles**:
<table border="1">
	<thead>
		<tr>
			<th>Java type</th>
			<th>Cassandra serializers</th>
		</tr>
	</thead>
	<tbody>

		<tr>
			<td>byte[]</td>
			<td>ByteArraySerializer</td>
		</tr>
		<tr>
			<td>ByteBuffer</td>
			<td>ByteBufferSerializer</td>
		</tr>
		<tr>
			<td>Boolean/boolean</td>
			<td>BooleanSerializer</td>
		</tr>
		<tr>
			<td>Date</td>
			<td>DateSerializer</td>
		</tr>
		<tr>
			<td>ByteBuffer</td>
			<td>ByteBufferSerializer</td>
		</tr>
		<tr>
			<td>Double/double</td>
			<td>DoubleSerializer</td>
		</tr>
		<tr>
			<td>Character</td>
			<td>CharSerializer</td>
		</tr>
		<tr>
			<td>Float/float</td>
			<td>FloatSerializer</td>
		</tr>
		<tr>
			<td>BigInteger</td>
			<td>BigIntegerSerializer</td>
		</tr>
		<tr>
			<td>Integer/int</td>
			<td>IntegerSerializer</td>
		</tr>
		<tr>
			<td>Long/long</td>
			<td>LongSerializer</td>
		</tr>
		<tr>
			<td>Short/short</td>
			<td>ShortSerializer</td>
		</tr>
		<tr>
			<td>String</td>
			<td>StringSerializer</td>
		</tr>
		<tr>
			<td>UUID</td>
			<td>UUIDSerializer</td>
		</tr>
		<tr>
			<td>enum</td>
			<td>StringSerializer</td>
		</tr>
		<tr>
			<td>Object</td>
			<td>StringSerializer</td>
		</tr>

	</tbody>
</table>

<br/>
Any object that does not belong to supported native types will be serialized as String by **Achilles** using Jackson Mapper library. For more details on Object serialization, see **[JSON Serialization]**

<br/>
##### Column name

* By default, the column name in **Cassandra** is the same as the entity field name.
* The column name in **Cassandra** can be overridden by providing the _name_ attribute on the **@Column** annotation. 

> On `@Id/@EmbeddedId` fiels, you can override the column name by adding a `@Column` annotation.

Examples:

```java
	...
	@Column
	private String name;
	...
```

Column name in **Cassandra** = **name**


```java
	...
	@Column(name="age_in_year")
	private Integer age;
	...
```
Column name in **Cassandra** = **age_in_year**
<br/>
<br/>
##### Lazy/Eager loading

* By default, all fields are fetched eagerly by **Achilles** 
* To enable lazy loading, you can annotated a field with the **@Lazy** annotation.

Example:

```java
	...
	@Lazy
	@Column
	private DomainModel model;
	...
```
 The domain model will be loaded by **Achilles** only upon getter invocation

<br/><br/>
### Un-mapped fields

 It is possible to have values stored in **Cassandra** that is not mapped to any field/property of the entity.
In this case **Achilles** will simply ignore them

<br/><br/>
## Collection and Map support

 For collection and maps, **Achilles** relies on Composite types to support them with the **Thrift** version. 

 With **CQL3**, collection and maps are supported natively so there is nothing special to do for **Achilles**

Example:

```java
	...
	@Column
	private List<String> addresses;
	...
```
<br/>
## Enum type

 **Achilles** does support enum types. **Enums** are serialized for now as String using the `name()` method. Consequently, the ordering for enum types is based on their name, and not their natural ordering in Java (based on declaration order).

 This is an important detail and can be a gotcha if you don't pay attention while working with **[Slice Queries]** and compound primary keys having enums as component.

<br/>
## Counter type

* All fields of **Counter** type are lazy by nature. In fact a **Counter** field should be used only as proxy object to interact with **Cassandra** counters. 
* **Achilles** raises an `UnsupportedOperationException` upon invocation of setter of **Counter** type on a proxified entity. Since this type is a proxy type, it is not sensible to invoke getter on it. 
* It is possible to initialize a **Counter** value on a transient entity using the provided **Counter Builder**. In this case, calling setting on Counter field is sensible.

Example:
```java
	...
	@Column
	private Counter tweetsCount;
	... 
```

For more details, see **[Counters]**

<br/>
## Consistency Level

* Consistency levels can be defined on the entity or a field using the **@Consistency** annotation
* Consistency levels defined on the entity apply to all fields 
* Consistency levels can be overriden on **Counter** fields and only those fields
* Consistency levels can be overriden at runtime

Example:

```java
@Entity
@Table(name="user_tweets")
@Consistency(read = QUORUM, write = QUORUM)
public class UseEntity {

	@Id
	private Long id;

	@Column
	private String name;

	@Column
	@Consistency(read = ONE, write = ONE)
	private Counter tweetsCount;
	...
}
```
 In the above example:

 * the entity is read using **QUORUM** and persisted with **QUORUM** consistency levels
 * the _tweetCount_ counter is read and persisted using **ONE** consistencyconsistency levels

<br/>
## Clustered Entity

 Apart from classic entity mapping, **Achilless** does also support **Cassandra** wide row structures. For this you need to define a clustered entity.

 A **clustered entity** is an entity having an `@EmbeddedId` field representing a compound primary key. The `@EmbeddedId` class should exposes at least 2 fields annotated with the `@Order` annotation, defining the respective clustering components and partition key.  


Example:
```java
@Entity
public class TimelineEntity {

	@EmbeddedId
	private CompoundKey id;

	@Column
	private String> tweetContent;

	public static class CompoundKey 
	{
		@Order(1)
		private Long userId;

		@Order(2)
		private UUID tweetId;

	}
}
```
In the above exaple, the TimelineEntity has:

1. A partition key `userId` of type `Long`
2. A clustering key `tweetId` of type `UUID`
3. A value `tweetContent` of type `String`

This entity definition will translate into **CQL** table definition as:

<pre>
CREATE TABLE TimeLineEntity (

	userid bigint,

	tweetid uuid,

	tweetcontent text,

	PRIMARY KEY (userid,tweetid)

)
</pre>

> The field annotated with `@Order(1)` is the **partition key** by default. You need not add any `@PartitionKey` annotation on it. Remaining fields annotated with `@Order(n)` are **clustering components**.

> The `@Order(n)` index *n* is 1-based (starts at 1) and not 0-based

> Please notice that column names are **case-insensitive** in CQL3

<br/>
For the **Thrift** version, the column family is
<pre>
create column family TimeLineEntity 
	with key_validation_class = 'org.apache.cassandra.db.marshal.LongType'
	and comparator = 'CompositeType(org.apache.cassandra.db.marshal.UUIDType)'
	and default_validation_class = UTF8Type
</pre>

> For the **Thrift** implementation, clustered entities cannot have more that one column except primary/compound primary key(s). **CQL** does not suffers such limitation because clustered entities are supported natively


<br/><br/>
## Composite Partition Key
It is possible to define composite partition key with **Achilles**. First you need to define a compound primary key class with `@EmbeddedId`, then use the `@PartitionKey` annotation on each component which is part of the composite partition key.

> It is possible to define a composite partition key on normal or clustered entity

<br/>
**Non clustered entity with composite partition key**
```java
@Entity
public class EntityWithCompositePartitionKey {

	@EmbeddedId
	private CompoundKey id;

	...

	public static class CompoundKey 
	{
		@PartitionKey
		@Order(1)
		private Long id;

		@PartitionKey
		@Order(2)
		private String type;
	}
}
```
<br/>
**Clustered entity with composite partition key**

```java
@Entity
public class EntityWithCompositePartitionKey {

	@EmbeddedId
	private CompoundKey id;

	...

	public static class CompoundKey 
	{
		@PartitionKey
		@Order(1)
		private Long id;

		@PartitionKey
		@Order(2)
		private String type;

		@Order(3)
		private UUID date; // this is the clustering component
	}
}
```

<br/>
## Value-less Entity

 **Achilles** does support entity with only `@Id`/`@EmbeddedId` fields. They are called **value-less** entities. A common use case for value-less entities is indexing data.

<br/>
## Time UUID

To map a Java `UUID` type to **Cassandra** `timeuuid` type, you need to add the `@TimeUUID` annotation on the target field.

[Slice Queries]: https://github.com/doanduyhai/Achilles/wiki/Queries#slice-query
[Join columns]: https://github.com/doanduyhai/Achilles/wiki/Join-Columns-&-Cascading
[Counters]: https://github.com/doanduyhai/Achilles/wiki/Counters
[JSON Serialization]: https://github.com/doanduyhai/Achilles/wiki/JSON-Serialization
