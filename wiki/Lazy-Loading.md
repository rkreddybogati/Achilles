Upon invocation of `find()` or `getReference()` on the PersistenceManager, **Achilles** will fetch the entity with eager columns from **Cassandra**.

Lazy fields are:

 * fields annotated with **@Lazy**
 * fields of **Counter** type

Collection and maps, if configured as eager, will be fetched **entirely** in memory.

For **Counter** type, it is not sensible to force eager fetching because it is lazy by design
 
