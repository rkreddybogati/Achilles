 By design, **Achilles** `PersistenceManager` is stateless. It only contains references to meta data and pre-built DAOs/prepared statements. For common operations (`persist`, `merge`, `remove`...) it delegates the job to underlying implementation classes.

 Thus the `PersistenceManager` is **thread-safe** and can be injected as a singleton in any of your DAO/Services.

Example:

```java
	...
	@Inject
	private CQLPersistenceManager manager;
	...
	...
```
