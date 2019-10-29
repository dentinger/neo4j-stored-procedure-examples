# Neo4j Procedure Template

This project is an example you can use to build Procedures in Neo4j.
It contains a handful of procedures and shows how to unit test them also.

Other examples of Neo4j Stored Procedures can be found  in the [Neo4j GitHub](https://github.com/neo4j-examples/neo4j-procedure-template/). 
 
**Note** 

This project requires a Neo4j 3.0.0 snapshot or milestone dependency.

## Building

This project uses maven, to build a jar-file with the procedure in this
project, simply package the project with gradle:

    ./gradlew clean build shadowJar

This will produce a jar-file,`build/libs/sample-neo4j-procedures-1.0.0.jar`,
that can be deployed in the `plugin` directory of your Neo4j instance.
