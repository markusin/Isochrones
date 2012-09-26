Project MIERWINE: 
Multimodal Incremental Network Expansion keeps in main memory only a small part of the network
and loads the network incrementally in main memory. Expired network elements are removed.
Therefore only a small part of the isochrone is keept in main memory which is sub-linear 
to the size of the isochrone. 
	
Required software: 
1) Java 6: as compiler
2) Oracle server (configured in etc/config.xml) with spatial extension
3) Apache Ant: to run the application from the shell

How to launch the application:
0) Open a shell
1) go into folder ant
2) type "ant compile" to compile the source
3) type "ant runTests" to run the tests of Bozen and San Francisco (isochrone will be written into the database)
4) type "ant runExperiments" to run the benchmark tests of Bozen and San Francisco

Only typing ant it shows the possible commands to be launched.
