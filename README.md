Welcome to TravelCompanion in Scala / Lift.
========

This project was created during the bachelor thesis by Ralf Muri and Daniel Hobi in spring 2010.

## The following technical aspects are implemented:
- JPA (based on lift-jpa-archetype)
- User Management (copy & paste from ProtoUser without mapper functionality)
- Validating (JSR 303)
- GUI Widgets (DistanceTraveler)
- Ajax & Comet (Blog as Single Page Application)
- REST

Please feel free to download & study our document in the doc directory (german!)
You will get some additional stuff like:
- Best practices in Lift & HowTos
- Working with IntelliJ IDEA & github.com
- General information about TravelCompanion
- Experience made by the authors

A running demo is deployed on stax.net:
http://travelcompanion.ralfmuri.staxapps.net

To run the app:
mvn install
cd web
mvn jetty:run

Then point your favorite browser to http://localhost:9090/

To create an offline version of the app (web/target/TravelCompanionScala-offline-1.0/):
mvn install
cd web
mvn package -Pjetty-offline

Notice: TravelCompanion is intended for demo purposes only.

## Update Rel. 1.1 Integration Solr 1.4.1 search engine

### Case
- The Solr search engine webapp shall run on the same Jetty Instance as the TravelCompanionScala webapp
- All the nessessary configurations (path to search index etc.) should be done inside the TravelCompanionScala webapp
- The TravelCompanionScala webapp does the search requests via the Solr XML-RPC API

### Realisation
- Embedded Solr webapp in TravelCompanionScala module web
- New Dir /solr/home in TravelCompanionScala for for Solr Search-Index and configuration
- Enhanced pom.xml of module web: new contextHandler and systemProperty
- Basic Search interface created
- New and updated Stages are updated in the index and deleted stages are removed from the index

### Issues
1. Because there are now two "memory-hungry" webapps, set your jetty run config in your IDE to:
   -XX:MaxPermSize=256m
   -Xms500m -Xmx500

2. The class TestTravelGenerator generates testdata for the TravelCompanionScala and the Solr Search-Index
   It's important that the DB and the Search-Index have the same Data - if the data is inconsistent:
- Delete DB: [UserHome]\pber\
- Delete Solr Index: [ProjectHome]\solr\home\data
- Jetty Run
- Run TestTravelGenerator.scala: the tours and stages are added
- Jetty restart: needs to be done, because the Test-Class uses another "JPA Session"

3. Does it deploy to stax ?
