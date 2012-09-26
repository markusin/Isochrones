Project MINEX: 
Multimodal Incremental Network Expansion with Expiration keeps in main memory only a small part of the network
and loads the network incrementally in main memory. Expired network elements are removed.
Therefore only a small part of the Isochrone is kept in main memory which is sub-linear 
to the size of the Isochrone. 
	
Required software: 
1) Java 6: as compiler
2) On of the following spatial DBMS 
   - Oracle server with spatial extension (Oracle Spatial)
   - PostgreSQL with spatial extension (PostGIS)
3) Apache Ant: to run the application from the shell

How to launch the application:
0) Open a shell
1) go into folder ant
2) type "ant setupDB" does a db setup
3) type "ant compile" to compile the source
4) type "ant runTests" to run the tests of Bozen and San Francisco (isochrone will be written into the database)
5) type "ant runExperiments" to run the benchmark tests of Bozen, South Tyrol, San Francisco, Washington DC

Only typing ant lists all possible execution tasks
