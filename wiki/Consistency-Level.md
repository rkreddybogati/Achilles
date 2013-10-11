## Static definition

Consistency levels can be defined statically with entity mapping using the **@Consistency** annotation. Please refer to **[Achilles Custom annotations]** for more details.

<br/>
## Runtime setting

### On PersistenceManager

For each operations on the PersistenceManager, **Achilles** extends it to support options at runtime:

```java
public <T> T find(Class<T> entityClass, Object primaryKey, Options options);

public <T> T getReference(Class<T> entityClass, Object primaryKey, Options options);

public void persist(Object entity, Options options);

public <T> T merge(T entity, Options options);

public void refresh(Object entity, Options options);

public void remove(Object entity, Options options);
```

 For more details, please refer to **[Options]**

<br/>
### On Counter type

The **Counter** API exposes methods to increment and decrement value using tunable consistency level:

```java

public Long get(ConsistencyLevel readLevel);


public void incr(ConsistencyLevel writeLevel);

public void incr(Long increment, ConsistencyLevel writeLevel);


public void decr(ConsistencyLevel writeLevel);

public void decr(Long decrement, ConsistencyLevel writeLevel);

```

<br/>
### On Batch level

It is possible to start a batch with a custom write consistency level using the **`startBatch(ConsistencyLevel consistencyLevel)`** method. Check **[Batch Mode]** for more details.

<br/>
## Settings priority

 Consistency levels can be defined at different places. Below is a summary of all type of consistency levels and their respective priority

<table border="1">
	<thead>
		<tr>
			<th>Priority</th>
			<th>Normal field</th>
			<th>Counter type</th>
		</tr>
	</thead>
	<tbody>
		<tr>
			<td>Lowest</td>
			<td>Default read/write consistency level defined by configuration</td>
			<td>Default read/write consistency level defined by configuration</td>
		</tr>
		<tr>
			<td>Lower</td>
			<td><strong>@Consistency</strong> annotation on class</td>
			<td><strong>@Consistency</strong> annotation on class</td>
		</tr>
		<tr>
			<td>Normal</td>
			<td>N/A</td>
			<td><strong>@Consistency</strong> annotation on field</td>
		</tr>
		<tr>
			<td>Higher</td>
			<td>Defined by <strong>PersistenceManager</strong>/by <strong>BatchMode</strong></td>
			<td>Defined by <strong>PersistenceManager</strong>/by <strong>BatchMode</strong></td>
		</tr>
		<tr>
			<td>Highest</td>
			<td>N/A</td>
			<td>Defined by <strong>Counter</strong> API</td>
		</tr>
	</body>
</table>

[Options]: https://github.com/doanduyhai/Achilles/wiki/Achilles-Custom-Types#options
[Achilles Custom annotations]: https://github.com/doanduyhai/Achilles/wiki/Achilles-Custom-Annotations
[Batch Mode]: https://github.com/doanduyhai/Achilles/wiki/Batch-Mode
