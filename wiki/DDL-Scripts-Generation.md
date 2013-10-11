If you want to create manually all the column families instead of letting **Achilles** do it for you, you can active the log level of `ACHILLES_DDL_SCRIPT` to **DEBUG**. The creation script will be displayed

Sample **log4j.xml** config file
```xml
	<logger name="ACHILLES_DDL_SCRIPT">
		<level value="DEBUG" />
	</logger>
```

Example of entity:

```java
@Entity
public class UserEntity
{

    @Id
    private Long id;

    @Column
    private String name;

    @Lazy
    @Column
    private String label;

    @Column(name = "age_in_years")
    private Long age;

    @Lazy
    @Column
    private List<String> friends;

    @Column
    private Set<String> followers;

    @Column
    private Map<Integer, String> preferences;

    @Column
    private Counter version;
}
```

### CQL
<pre>
CREATE TABLE UserEntity(
	age_in_years bigint,
	name text,
	label text,
	id bigint,
	friends list<text>,
	followers set<text>,
	preferences map<int,text>,
	PRIMARY KEY(id)
) WITH COMMENT = 'Create table for entity "info.archinnov.achilles.test.integration.entity.UserEntity"' 
</pre>

### Thrift

<pre> 
create column family UserEntity
	with key_validation_class = LongType
	and comparator = 'CompositeType(BytesType,UTF8Type,Int32Type)'
	and default_validation_class = UTF8Type
	and comment = 'Column family for entity info.archinnov.achilles.test.integration.entity.UserEntity'
</pre>			
