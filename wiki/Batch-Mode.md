## Batch Mutations

Sometimes it is more convenient to stack up all modifications and send them in one request to **Cassandra**. This is called batch mutations. 


 To support batching feature, **Achilles** provides a **CQLBatchingPersistenceManager** / **ThriftBatchingPersistenceManager**. This entity manager exposes the same methods than the normal `PersistenceManager` plus 4 new operations related to batch:

```java

public void startBatch();

public void startBatch(ConsistencyLevel consistencyLevel);

public void endBatch();

public void cleanBatch();
```

* `startBatch()`: start a new batch session
* `startBatch(ConsistencyLevel consistencyLevel)`: start a new batch session with custom consistency read/write level
* `endBatch()`: end an existing batch session and flush the pending mutation to **Cassandra**
* `cleanBatch()`: clean all pending mutations for the current batch session and reset the state

<br/>
## Implementation details

 The batch session is managed by a _batch flushing context_ in **Achilles**. Unlike an _immediate flushing context_, the latter stacks up modifications and only flush them to **Cassandra** when `endBatch()` is invoked.

 Insert and update operations are saved in a temporary map inside the _batch flushing context_. 


 For **CQL**, **Achilles** stacks up all statements (prepared or simple queries) in a list and execute them using **CQL3** native batch mode. Not much to say. For **Thrift** there is one **Hector** mutator object per column family. At flush time, all the mutators are sent to **Cassandra** and the mutator map is cleared.

- The **BatchingPersistenceManager** is stateful and not thread-safe by design because of the _batch flushing context_. 
- A **BatchingPersistenceManager** instance can be obtained by invoking `createBatchingPersistenceManager()` on the **PersistenceManagerFactory** 
- Any **BatchingPersistenceManager** instance should be discarded right after the end of the batch.
- Any _managed_ entity and **Counter** proxy created by a **BatchingPersistenceManager** is bound to the _batch flushing context_ and should be discarded at the end of the batch

<br/>
## Usage 

Let's consider the following **UserEntity**:

```java
@Entity 
public class UserEntity 
{
	@Id
	private Long id;

	@Column
	private String firstname;

	@Column
	private String lastname; 

	@Column
	private Counter tweetsCount; 

	public UserEntity(Long userId,String firstname,String lastname, Counter tweetsCount)
	{...}
}
```

 When the user create a new tweet message, we need to spread the tweet to all its followers. 

```java
// Start batch
CQLBatchingPersistenceManager batchManager = pmf.createBatchingPersistenceManager();
batchManager.startBatch();

UserEntity user = batchManager.find(UserEntity.class,10L);

user.setFirstname("new firstname");
user.setLastname("new lastname");

// Save name change. No flushing yet
batchManager.merge(user);

// Create new user. No flushing yet
batchManager.persist(new UserEntity(10L,"John","DOO",CounterBuilder.incr(10));


// Counter value increment, immediately flushed even if in Batch mode
user.getTweetsCount().incr(2L);

// Flush first user name change and new user creation to Cassandra
batchManager.endBatch();
```

 The above example illustrates how batching mode works. All dirty checking and state changes on the user entity is not flushed when `manager.merge()` is called. Similarly new entities insertion is not flushed until `endBatch()` is called

  However, the counter value increment is flushed immediately to **Cassandra** as per design of counter in **Achilles**

<br/>
## Exception and recovery

 As already mentioned, the **BatchingPersistenceManager** is  stateful so if any exception occurs at flush time, **Achilles** will try to recover by clearing the statement list/mutator map and cleaning up the _batch flushing context_. Theoretically you can re-use the same instance of **BatchingPersistenceManager** after the exception is caught.

 However it is strongly recommended to create a new **BatchingPersistenceManager** instance and not re-use the previous one because creating a new instance is a very cheap operation.

 All _managed_ entities created by the old **BatchingPersistenceManager** instance should also be discarded because they keep a reference on the (potentially) staled _batch flushing context_.

<br/>
## Batch consistency level

 It is possible to start a batch session with custom consistency levels:

```java
// Spawn new batchManager instance
CQLBatchingPersistenceManager batchManager = persistenceManagerFactory.createBatchingPersistenceManager();

// Start batch with consistency level QUORUM
batchManager.startBatch(ConsistencyLevel.QUORUM);

```

 In the above example, all operation will be done with consistency **QUORUM**.

 If a batch session is started with custom consistency levels:

 * Invoking common operations like `persist()`, `merge()` ... with custom consistency levels on the batchManager instance will raise an **AchillesException**
 * Invoking **Counter** operations with custom consistency levels on proxies created by the batchManager instance is allowed though and will override the consistency level defined by the batch


<br/>
## Atomicity

 For **CQL** version only, all batch operations are atomic, in the sense that either all upsert statements succeed or they fail. There is no risk having upsert statements partially failed leaving the database in an inconsistent state.

 Atomicity is not possible with **Thrift** because not supported at protocol level.
