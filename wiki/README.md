[[assets/Achilles_Logo_Reversed.png]]

# Presentation #

 Achilles is an open source Persistence Manager for Apache Cassandra. Among all the features:

 - 2 implementations: Thrift & CQL3
 - Dirty check for simple and collection/map type properties
 - Lazy loading
 - Collections and map support
 - Support for clustered entities in Thrift & CQL3
 - Support for compound primary key and composite partition key
 - Advanced queries (slice, native or typed) in CQL3
 - Support for counters
 - Support for runtime setting of consistency level, ttl and timestamp
 - Batch mode for atomic commits soon (atomicity only available for **CQL3** impl)

# Installation #

 To use **Achilles**, just add the following dependency in your **pom.xml**:

```xml

	<dependency>	
		<groupId>info.archinnov</groupId>
		<artifactId>achilles-thrift</artifactId>
		<version>1.8.0</version>
	</dependency>  
```

Right now, only the **Thrift** implementation is available. The **CQL** version is in progress and relies on **[Datastax Java Driver]** which is still in beta. 

The framework has been released on **Sonatype OSS** repository so make sure you have the following
 entry in your **pom.xml**:

```xml
<repository>
	<id>Sonatype</id>
	<name>oss.sonatype.org</name>
	<url>http://oss.sonatype.org</url>
</repository>
```

 For now, **Achilles** depends on the following libraries:
 
 1. cassandra 1.2.8
 2. cassandra-driver-core 1.0.2 for the **CQL** version
 3. hector-core 1.1-4 for the **Thrift** version
 4. CGLIB nodep 2.2.2 for proxy building
 5. hibernate-jpa-2.0-api 1.0.1.Final (no reference jar for JPA 2, only vendor specific ones are available)
 6. Jackson asl, mapper & xc 1.9.3 

# Quick Starter#

 For a quick tutorial, please go to **[5 minutes Tutorial]**

# Advanced tutorial #

 To get a deeper look on how you can use **Achilles**, check out the **[Twitter Demo]** application and read the **[Advanced Tutorial]** section
    
# License #
Copyright 2012 DuyHai DOAN

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this application except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

[5 minutes Tutorial]: https://github.com/doanduyhai/Achilles/wiki/5-minutes-Tutorial
[Twitter Demo]: https://github.com/doanduyhai/Achilles-Twitter-Demo
[Advanced Tutorial]: https://github.com/doanduyhai/Achilles/wiki/Advanced-Tutorial:-Twitter-Demo
[Datastax Java Driver]: https://github.com/datastax/java-driver
