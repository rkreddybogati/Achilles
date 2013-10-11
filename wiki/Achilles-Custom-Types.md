To support all **Cassandra** specific but powerful features such as tunable consistency levels or counters, **Achilles** introduces custom Java types:

<br/>
#### ConsistencyLevel
It is an enum exposing all existing consistency levels in Cassandra:

* ANY
* ONE
* TWO
* THREE
* QUORUM
* EACH_QUORUM
* LOCAL_QUORUM
* ALL

Because **Achilles** supports both **CQL** & **Thrift** implementations, it has to define a custom enum for Consistency instead of re-using the enums provided by **Hector** or **Java Driver Core** libraries.
 
 See **[Consistency Level]** for more details

<br/>
#### Counter
This type represents a **Cassandra** counter column. It exposes the following methods:

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

 For more details on counters, see **[Counter type]**

<br/>
#### CounterBuilder

 The above `Counter` type can be gotten only from a 'managed' entity. If you want to persist a transient entity having counter fields, you should use the `CounterBuilder` type provided by **Achilles**

 The builder exposes the following static methods:

```java
    public static Counter incr() 

    public static Counter incr(Long incr) 

    public static Counter decr() 

    public static Counter decr(Long decr) 
```

 The only sensible usage for `CounterBuilder` is for transient entity persistence. Example:

```java
@Entity
public class UserEntity
{
    @Id
    private Long userId;

    @Column
    private Counter 

    private UserEntity(Long userId,.....,Counter popularity)
    {
        this.userId = userId;
        ...
        this.popularity = popularity;
    }
    ...
}

 // Creating a new user with initial popularity value set to 100
 manager.persist(new UserEntity(10L,....,CounterBuilder.incr(100L));
```

<br/>
#### Options

 An `Options` is just a holder object for Cassandra specific TTL, Timestampand and Consistency level parameters

```java
public class Options {

    ConsistencyLevel consistency;

    Integer ttl;

    Long timestamp;

    public Optional<ConsistencyLevel> getConsistencyLevel() {
        return Optional.fromNullable(consistency);
    }

    public Optional<Integer> getTtl() {
        return Optional.fromNullable(ttl);
    }

    public Optional<Long> getTimestamp() {
        return Optional.fromNullable(timestamp);
    }
}
```
 
 Options are used in conjunction with common Persistence Manager operations `persist()`, `merge()`, `find()`, `getReference()` and `remove()`.

 Normally you cannot instantiate an `Options` object yourself, you need to use the `OptionsBuilder` instead, check below.

<br/>
#### OptionsBuilder

 Main builder to create `Options`.

 The exposed methods are:

```java
Options options;
// Consistency, TTL and timstamp
options = OptionsBuilder.withConsistency(QUORUM)
                .ttl(10)
                .timestamp(100L);

// Consistency and TTL only
options = OptionsBuilder.withConsistency(QUORUM)
                .ttl(10);

// Consistency and Timestamp only
options = OptionsBuilder.withConsistency(QUORUM)
                .timestamp(100L);

//TTL, Consistency and Timestamp
options = OptionsBuilder.withTtl(11)
                .consistency(ANY)
                .timestamp(111L);

// TTL and Consistency only
options = OptionsBuilder.withTtl(11)
                .consistency(ANY);

// TTL and Timestamp only
options = OptionsBuilder.withTtl(11)
                .timestamp(111L);

// Timestamp, Consistency and TTL 
options = OptionsBuilder.withTimestamp(122L)
                .consistency(ONE)
                .ttl(12);

// Timestamp and Consistency only
options = OptionsBuilder.withTimestamp(122L)
                .consistency(ONE);

// Timestamp and TTL only
options = OptionsBuilder.withTimestamp(122L)
                .ttl(12);
```


[Consistency Level]: https://github.com/doanduyhai/Achilles/wiki/Consistency-Level
[Counter type]:https://github.com/doanduyhai/Achilles/wiki/Entity-Mapping#counter-type
