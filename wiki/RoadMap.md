Next features in the pipe are :

* Allow reverse ordering for clustering components, probably introducing an extra attribute on the `@Order` annotation

* Add support for secondary index with new annotation `@Index`

* Add support for parameterized CQL3 query coming with Java driver 2.0

* Add support for interceptors upon events:
    * PrePersist 
    * PostPersist
    * PreUpdate 
    * PostUpdate
    * PreRemove 
    * PostRemove 
    * PostLoad

 Interceptors can be declared using dedicated JPA annotations @EntityListeners. Internal callbacks can use corresponding JPA annotations (`@PrePersist`,`@PostPersist`...)

* Add support for bean validation (JSR-303). A bean validator is just a special entity interceptor on `@PrePersist` and `@PreUpdate` events

* Add Named queries and named variables for binding. Add `@Query` syntax on repository interfaces to define query methods à-la Spring Data (http://static.springsource.org/spring-data/data-jpa/docs/current/reference/html/jpa.repositories.html#jpa.named-parameters)

* Allow inheritance entity mapping with discriminator column

* Provide asynchronous executions for all operations with the CQL implementation

* Implement lightweight transactions using Options

* Allow list of partition keys in slice queries (to be translate into `IN(?)` clause in **CQL3**

* Add support for Cassandra notifications when available in Java driver core