1. Can I use **Achilles** and **Hibernate** (or any JPA provider) at the same time ?

	Yes, but you need to remove all *@Entity* annotation on **Achilles** entities
	and use the *@Table* annotation instead. Indeed all **Achilles** entities
	having the *@Entity* annotation will be parsed by **Hibernate** and it will complain
	since it cannot find any SQL table
	that map to your entities.
	
	Alternatively if you're using **Spring**, you can give a list of explicit entities
	to be scanned by **Hibernate** so in this case no issue
	
	If you're using **Hibernate** HBM files for mapping instead of annotations, it's fine also
	

2. Will there be any support for secondary index ?

	Yes in a very near future (probable release 2.0.8)		 	  	

3.  Any support for property indexing & text search ?

	Technically possible by putting Lucence in the loop but lots of work to do. You're welcomed to help. 

	For those who have some money, Datastax offers a custom version of **[Apache Solr]** with Cassandra under the hood, which boost the performance. Check it out, it's quite nice





[Apache Solr]: http://www.datastax.com/what-we-offer/products-services/datastax-enterprise/apache-solr