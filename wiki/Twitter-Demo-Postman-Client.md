 Below is the procedure to install and use the Chrome Postman REST Client extension to interact with the demo

### Installing the client
 
 First you need to have a recent Google Chrome browser. Then install the **[Postman REST Client]** add-on, it's free.

### Importing scripts

 All REST requests have been prepared before hand and saved as scripts. They are saved under the **Postman_Client_Scripts** folder in the demo source:

* **AchillesDemo-environment.json**: script to load environment and variable. It define the variable **{{url}}** which default to _http://localhost:8080_. You need to import it ( _Manage environments/Import_ )
* **AchillesDemo_Users.json**: predefined collection of requests to interact with users, namely:
    * create user
    * follow/stop following an user
    * get information on an user
    * list user friends
    * list user followers
* **AchillesDemo_Tweets.json**: predefined collection of requests to interact with tweets, namely:
    * post a tweet
    * display a tweet
    * add/remove a tweet to/from favorite list
    * delete a tweet
* **AchillesDemo_Lines.json**: predefined collection of requests to interact with lines, namely:
    * list an user timeline
    * list all user tweets
    * list an user mentionline
    * list all tweets for a given tag

You can import all the scripts for scenarios in _Collections/Import collection_
### Playing scenarios

 The scenarios are there to illustrate the features of the twitter demo back-end and also serves as functional validation tests.

Scenario 1: 
 * create 3 users, **John Doe**, **Helen Sue** and **Richard Smith**
 * **Helen** and **Richard** follows **John**
 * When getting info on **John**, his followers count should be 2
 * **John** followers line should shows **Helen** and **Richard**

Scenario 2:
 * **John** posts 4 tweets.
    * _tweet1_ : " _#Achilles_ makes modeling with _#Cassandra_ much easier"
    * _tweet2_ : "This is a great live demo for _#Achilles_ "
    * _tweet3_ : "Have a look at _#Achilles_, an Entity Manager for _#Cassandra_ "
    * _tweet4_ : "When will _#Java_ 8 be out ?? Can't wait for it"
 * **John** tweets count should be updated to 4
 * Those 4 tweets should be visible in **John** userline and timeline
 * Those 4 tweets should be visible in **Helen** and **Richard** timeline since they follow **John**
 * _tweet1_, _tweet2_ and _tweet3_ should be visible in _#Achilles_ tagline
 * _tweet1_ and _tweet3_ should be visible in _#Cassandra_ tagline
 * _tweet4_ should be visible in _#Java_ tagline

Scenario 3:
 * **Richard** starts following **Helen**
 * **Helen** posts _tweet5_ : " _#Achilles_ is to _#Cassandra_ as _#Hibernate_ is to #SQL"
 * **Richard** should see _tweet5_ in his timeline
 * _tweet5_ should be visible in the _#Achilles_, _#Cassandra_, _#Hibernate_ and _#SQL_ taglines
 * **Helen** tweets count and followers count should be 1
 * **Helen** followers line should show **Richard**
 * **Richard** friends line should show **John** and **Helen**

Scenario 4: 
 * **Richard** stops following **Helen**
 * **Helen** posts _tweet6_ : "Yet another tweet"
 * _tweet6_ should NOT be visible in **Richard** timeline
 * **Helen** tweets count should be incremented to 2 and followers count decremented to 0
 * **Helen** followers line should be empty
 * **Richard** friends line should only show **John**
 
Scenario 5:
 * **Helen** adds _tweet2_ to her favorite list
 * **Richard** adds _tweet3_ to his favorite list
 * _tweet2_ should appear in **Helen** favorite line
 * _tweet3_ should appear in **Richard** favorite line
 * _tweet2_ and _tweet3_ should have 1 favorite count

Scenario 6:
 * **John** posts _tweet7_ : "@hsue, @rsmith and @batman, you should try #Achilles"
 * _tweet7_ should appear in **Helen** and **Richard** mention line
 * **Helen** and **Richard** mention count should be incremented to 1
 * **Batman** mention line should be empty since the user does not exists

Scenario 7:
 * **John** removes _tweet1_
 * _tweet1_ should no longer exist in **Helen** and **Richard** timeline
 * _tweet1_ should be removed from _#Achilles_ and _#Cassandra_ taglines
 * **John** tweets count should be decremented

Scenario 8:
 * **John** removes _tweet2_ and _tweet3_. These tweets were favorited by **Helen** and **Richard**
 * **Helen** and **Richard** favorite line should be now empty
 * **John** tweets count should be decremented by 2

Scenario 9:
 * **John** removes _tweet7_. This tweet was mentioning **Helen** and **Richard**
 * **Helen** and **Richard** mention line should be now empty
 * **John** tweets count should be decremented 
 


 



[Postman REST Client]: https://chrome.google.com/webstore/detail/postman-rest-client/fdmmgilgnpjigdojojpjoooidkmcomcm