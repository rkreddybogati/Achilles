#### @Lazy

 When put on a **@Column** field, this annotation makes it lazy. When an entity is loaded by **Achilles**, only eager fields are loaded. Lazy fields will be loaded at runtime when the getter is invoked.

  
 A *lazy* Collection or Map field will be loaded entirely when the getter is invoked.
 

<br/>
#### @Order

 This annotation is used to define components for compound primary keys. It should be used in a class which is an `@EmbeddedId` for an entity.
 
 The annotation requires the value attribute to indicates the order of the current component in the key. Example:

```java
@Entity
public class MyEntity 
{
	@EmbeddedId
	private CompoundKey id;
	
	...
		
	private static class CompoundKey 
	{
		@Order(1)
		private Long userId;

		@Order(2)
		private UUID tweetId;
		...
	}
	...
}
```
<br/> 
>	Unlike Java indexes, the ordering starts at 1. It is a design choice since it is more natural for human being to start counting at 1

 For more detail on this annotation and its usage, please refer to **[Clustered Entity]**

<br/>
#### @PartitionKey

This annotation indicates which component is part of the partition key for a compound primary key.

If you have a simple partition key, this annotation is optional. By default the component having `@Order(1)` is considered by **Achilles** as default partition key. If you have a composite partition key with many components, add this annotation on each of them.

```java
@Entity
public class MyEntity 
{
	@EmbeddedId
	private CompoundKey id;
	
	...
		
	private static class CompoundKey 
	{
		@PartitionKey
		@Order(1)
		private Long id;

		@PartitionKey
		@Order(2)
		private String type;

		@Order(3)
		private UUID date;
		...
	}
	...
}
```
<br/> 

 In the above example, `id` and `type` are part of the composite partition key. `date` is the clustering key.

> Remark: all fields annotated with `@PartitionKey` should be consecutive with respect to their ordering. Failing this condition will raise an exception during **Achilles** bootstrap

<br/>
#### @Consistency

 This annotation can be used on an entity or a **Counter** field 

 You need to specify the *read* and *write* attribute to define the corresponding consistency level.

 Example:

```java
@Entity
@Consistency(read=ConsistencyLevel.ONE,write=ConsistencyLevel.QUORUM)
public class MyBean 
{
	@Id
	private Long id;
	
	...
		
	@ConsistencyLevel(read=ConsistencyLevel.ONE,write=ConsistencyLevel.ONE)
	@Counter
	@Column
	private Long counter;

}
```
<br/>
#### @TimeUUID

This annotation tells **Achilles** to map a Java `UUID` field to **Cassandra** `timeuuid` type.

 Example:

```java
@Entity
@Consistency(read=ConsistencyLevel.ONE,write=ConsistencyLevel.QUORUM)
public class MyBean 
{
	@Id
	private Long id;
	
  @TimeUUID
  @Column 
  private UUID date;
	...
```

This is especially useful in **CQL3** to map to `timeuuid` type so you can use **[Timeuuid functions]** like `dateOf()`/`now()`/`minTimeuuid()`/`maxTimeuuid()` or `unixTimestampOf()` on native queries

[Clustered Entity]: https://github.com/doanduyhai/Achilles/wiki/Entity-Mapping#clustered-entity
[Timeuuid functions]: http://www.datastax.com/documentation/cql/3.0/webhelp/index.html#cql/cql_reference/cql_data_types_c.html#reference_ds_axc_xk5_yj
