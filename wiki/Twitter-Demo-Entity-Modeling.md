 The entities has been designed to favor the reading tweets use case over deletion. Indeed, most of the time people create and read tweets, the tweet removal scenario is quite rare. This emphasises one of the most important requirement when working with **Cassandra**: **_you must adapt the entity modeling and design to your business use cases_** 


 With this in mind, below are the main use cases for a Twitter-like application, ordered by priority

1. **Read tweets**
2. **Write tweets**: there are fewer people writing than reading tweets
3. **Delete tweets**: very rare

With this in mind, we choose to duplicate as much as we can data for the read use-case to avoid multiple reads while fetching tweets in different lines.

 The write use-case is a little bit slower since we need to spread copies of tweets in different lines. But since **Cassandra** is very performant for write there will be no significant impact on perf.

 The delete use-case is the slowest in term of performance since we need to read indexes to remove each duplicated copies of the tweet being removed. However it is still acceptable because the removal use case is rare.


## Modeling Users

```java
@Entity
@Table(name = "user")
public class User {

    @Id
    private String login;

    @Column
    private String firstname;

    @Column
    private String lastname;

    @JsonDeserialize(using = JsonDateDeserializer.class)
    @JsonSerialize(using = JsonDateSerializer.class)
    @Column
    private Date accountCreationDate;

    @Column
    private Counter tweetsCounter;

    private long tweetsCount;

    @Column
    private Counter friendsCounter;

    private long friendsCount;

    @Column
    private Counter followersCounter;

    private long followersCount;

    @Column
    private Counter mentionsCounter;

    private long mentionsCount;

    ...
}
```

 The user entity exposes 4 counters:

 * **tweetsCounter**: number of tweets created by the user
 * **friendsCounter**: number of people the user is following
 * **followersCounter**: number of people following the user
 * **mentionsCounter**: number of tweet in which the user is mentioned (good popularity indicator)


There are also 4 primitive long fields to carry the counter values for serialization back to the front-end because the **Counter** type is a proxy.
 
 
## Modeling Tweets
```java
@Entity
@Table(name = "tweet")
public class Tweet {

    @Id
    private UUID id;

    @Column
    private Counter favoritesCount;

    @Column
    private TweetModel tweet;
   
}
```

 The **Tweet** entity exposes the `TweetModel` as a POJO and a dedicated counter `favoriteCount`.

<br/>
## Modeling User Relationships

 To persist user friends and followers, we need clustered entities with an underlying wide row structure in **Cassandra**. 

 The idea is to create a clustered entity `UserRelation` with compound primary key `UserKey` having components:

1. **userLogin**: partition key representing the current user login
2. **relationship**: an enum representing the kind of relationship: `FRIEND` or `FOLLOWER`
3. **login**: the login of the other user in relationship

```java
public class UserKey {

    @Column
    @Order(1)
    private String userLogin;

    @Column
    @Order(2)
    private Relationship relationship;

    @Column
    @Order(3)
    private String login;
}

public static enum Relationship
{
        FRIEND, FOLLOWER;
}
```

 The `UserRelation` entity is designed as:

```java
@Entity
@Table(name = "user_relation")
public class UserRelation {

    @EmbeddedId
    protected UserKey id;

    @JoinColumn
    @ManyToMany
    protected User user;
}
```

As value, we have a `@JoinColumn` to an `User` entity. Denormalization is not an option here because `User` entity is mutable (user details can change)

 Additionally we add a `FollowerLoginLine` which is a **value-less** clusterd entity to only index user followers login without pulling the whole `User` entity as join column.

```java
@Entity
@Table(name = "followers_login")
public class FollowerLoginLine {

    @EmbeddedId
    private UserKey id;
}
```



<br/>
## Modeling Tweet Lines

 We need an entity `TweetLine` to index tweets for _userline_, _timeline_, _favoriteline_ , _mentionline_ and _tagline_.

 The compound primary key `TweetKey` for all those lines have the following components:

 1. **loginOrTag**: partition key representing login of the current user for tweet lines and tag value for tagline
 2. **type** : enum representing the type of line. Possible values are `USERLINE`, `TIMELINE`, `FAVORITELINE`, `MENTIONLINE` and `TAGLINE`
 3. **tweetId**: self-explanatory

```java
public class TweetKey {

    @Column
    @Order(1)
    private String loginOrTag;

    @Column
    @Order(2)
    private LineType type;

    @Column
    @Order(3)
    private UUID tweetId;
}

public static enum LineType
{
    USERLINE, TIMELINE, FAVORITELINE, MENTIONLINE, TAGLINE;
}

@Entity
@Table(name = "tweet_line")
public class TweetLine {

    @EmbeddedId
    protected TweetKey id;

    @Column
    protected TweetModel tweetModel;
}
```

The value for the `TweetLine`entity is the `TweetModel` POJO.

<br/>
## Modeling Tweet Reverse Indexes

 Since we duplicate the `TweetModel` into each tweet line (_userline_, _timeline_, _favoriteline_, _mentionline_ & _tagline_) we need a way to find them back for tweet deletion.

 What we need is an inverse index which tells us **in which line a tweet has been spread**.

 The `TweetIndex` entity is designed for that purpose. It is a **value-less** clustered entity whose compound primary key `TweetIndexKey` components are:

 1. **tweetId**: partition key representing the id of the tweet that has been spread
 2. **type**: enum representing the type of the line the tweet has been spread to
 3. **loginOrTag**: representing an user login for all tweet lines and tag name for tagline

```java
public class TweetIndexKey {

    @Column
    @Order(1)
    private UUID tweetId;

    @Column
    @Order(2)
    private LineType type;

    @Column
    @Order(3)
    private String loginOrTag;
}

@Entity
@Table(name = "tweet_index")
public class TweetIndex {

    @EmbeddedId
    private TweetIndexKey id;
}
```

