 **Achilles** support mapping for Set, List and Map types. The underlying implementation returned by a *managed*
 entity is a HashSet, ArrayList and HashMap.
 
 Example:
```java
@Entity
public class PlayerStats 
{
	@Id
	private Long id;

	@Column
	private Set<Rank> earnedRanks;
		
	@Lazy
	@Column
	private List<String> favoriteItems;

	@Column
	private Map<Long,Integer> levelsScore;		
} 
```

 All collections and maps fields are eagerly fetched by default when the entity is loaded. However you can make them lazy with the  **@Lazy** annotation. A _lazy_ collection or map is not loaded when the entity is fetched but only upon getter invocation.
 
>	Even if set as _lazy_, collection or map values are loaded entirely in memory. Therefore we strongly advise to limit the use of collections and maps for **small number of items (<1000)**.
 

 
 
