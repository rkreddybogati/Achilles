 For bean mapping, only **field-based access type** is supported by **Achilles**. Furthermore, there is no default mapping for fields. If you want a field to be mapped, you must annotate it with `@Column`/`@Id`/`@EmbeddedId`. All fields that are not annotated by either annotations are considered transient by **Achilles**
 
 Below is a list of all JPA annotations supported by **Achilles**.    

<br/> 
#### @Entity

 Indicates that an entity is candidate for persistence. By default **Achilles** creates a column family whose name is the **class name** of the entity. If you want to specify a specific column family name, add the *@Table* annotation with the *name* attribute (see below).
 
 Example:
```java
@Entity
public class User 
{
	...
	...
}
```
<br/>
#### @Table
When then *name* attribute is filled, it indicates the name of the column family used by by the **Cassandra** engine to store this entity.

 Example:

```java
@Entity
@Table(name = "users_column_family")
public class User 
{
	...
	...
}
```
 A class annotated either by @Entity or by @Table annotation is an entity candidate for **Achilles**. Thus the @Entity annotation is not strictly required. This constraint relaxing is necessary because when using **Achilles** along-side with a JPA implementation like Hibernate, you don't want it to detect your **Achilles** entity as a JPA entity.
 
<br/>
>	**Please note that Cassandra limits the column family name to 48 characters max.**

<br/>	
#### @Id

Indicates a field to be mapped as the primary key for the entity. The primary key can be of any type, even a plain POJO.

 Under the hood, the primary key will be serialized to bytes array and  used as row key (partition key) by the **Cassandra** engine.

<br/>   
#### @EmbeddedId

Indicates a field to be mapped as a compound primary key for an **clustered** entity. A valid compound primary key should be a POJO having at least 2 fields representing primary key components.

 The first component is the partition key, the remaining components are the clustering keys.

 For more details, see **[Clustered Entity]** 

<br/>
#### @Column

Indicates a field to be mapped by **Achilles**. When the *name* attribute of *@Column* is given, the field
 will be mapped to this name. Otherwise the field name will be used.

 Example:

```java
@Column(name = "age_in_years")
private Long age; 
```

[Clustered Entity]: https://github.com/doanduyhai/Achilles/wiki/Entity-Mapping#clustered-entity
