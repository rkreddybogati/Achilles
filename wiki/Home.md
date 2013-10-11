[[assets/Achilles_New_Logo.png]]

<br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;[![Build Status](https://travis-ci.org/doanduyhai/Achilles.png?branch=master)](https://travis-ci.org/doanduyhai/Achilles)

# Presentation #

 Achilles is an open source Persistence Manager for Apache Cassandra. Among all the features:

 - 2 implementations: **Thrift** & **CQL3** 
 - Dirty check for simple and collection/map type properties
 - Lazy loading 
 - Collections and map support
 - Support for clustered entities in Thrift & CQL
 - Advanced queries (slice, native or typed) in CQL3
 - Support for counters
 - Support for runtime setting of consistency level, ttl and timestamp
 - Batch mode for atomic commits soon (atomicity only available for **CQL** impl)


# Installation #

 To use **Achilles**, just add the following dependency in your **pom.xml**:
 
 For **CQL** version:
 
	<dependency>	
		<groupId>info.archinnov</groupId>
		<artifactId>achilles-cql</artifactId>
		<version>2.0.7</version>
	</dependency>  
 
  For **Thrift** version:
 
	<dependency>	
		<groupId>info.archinnov</groupId>
		<artifactId>achilles-thrift</artifactId>
		<version>2.0.7</version>
	</dependency> 


 
 For now, **Achilles** depends on the following libraries:
 
 1. cassandra 1.2.8
 2. cassandra-driver-core 1.0.2 for the **CQL** version
 3. hector-core 1.1-4 for the **Thrift** version
 3. CGLIB nodep 2.2.2 for proxy building
 4. hibernate-jpa-2.0-api 1.0.1.Final (no reference jar for JPA 2, only vendor specific ones are available)
 5. Jackson asl, mapper & xc 1.9.3 
   
  
# 5 minutes tutorial

 To boostrap quickly with **Achilles**, you can check the **[5 minutes tutorial]**

# Quick Reference

 To be productive quickly with **Achilles**. Most of useful examples are given in the **[Quick Reference]**

# Advanced tutorial

 To get a deeper look on how you can use **Achilles Thrift version**, check out the **[Twitter Demo]** application and read the **[Advanced Tutorial]** section
 
# License
Copyright 2012 DuyHai DOAN

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this application except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

[5 minutes Tutorial]: https://github.com/doanduyhai/Achilles/wiki/5-minutes-Tutorial
[Quick Reference]: https://github.com/doanduyhai/Achilles/wiki/Quick-Reference
[Twitter Demo]: https://github.com/doanduyhai/Achilles-Twitter-Demo
[Advanced Tutorial]: https://github.com/doanduyhai/Achilles/wiki/Advanced-Tutorial:-Twitter-Demo
[Datastax Java Driver]: https://github.com/datastax/java-driver
