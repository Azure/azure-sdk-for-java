## Overview

**Spring Data Gremlin** provides initial Spring Data support for those databases using Gremlin query language. With annotation oriented programming model, it simplified the mapping to the database entity. It also provides supports for basic and custom query. 

This project works with *any Gremlin-compatible* data store, and also with [Azure Cosmos DB](https://docs.microsoft.com/en-us/azure/cosmos-db/introduction). Cosmos is a globally-distributed database service that allows developers to work with data using a variety of standard APIs, such as Graph, MongoDB, and SQL. Spring Data Gremlin provides a delightful experience to interact with Azure Cosmos DB Graph API. 

## Usage

### Add the dependency
`spring-data-gremlin` is published on Maven Central Repository. If you are using Maven, add the following dependency.  

```xml
<dependency>
    <groupId>com.microsoft.spring.data.gremlin</groupId>
    <artifactId>spring-data-gremlin</artifactId>
    <version>2.2.0</version>
</dependency>
```

### Setup Configuration
Setup ```application.yml``` file.(Use Azure Cosmos DB Graph as an example.)

```
gremlin:
  endpoint: url-of-endpoint 
  port: 443
  username: /dbs/your-db-name/colls/your-collection-name
  password: your-password
  telemetryAllowed: true # set false to disable telemetry

```

### Define an entity
Define a simple Vertex entity with ```@Vertex```.

```
@Vertex
public class Person {

    @Id
    private String id;

    private String name;

    private String age;

    ...
}

```

Define a simple Edge entity with ```@Edge```.

```
@Edge
public class Relation {

    @Id
    private String id;

    private String name;

    @EdgeFrom
    private Person personFrom;

    @EdgeTo
    private Person personTo;

    ...
}
```
Define a simple Graph entity with ```@Graph```.

```
@Graph
public class Network {

    @Id
    private String id;

    public Network() {
        this.edges = new ArrayList<Object>();
        this.vertexes = new ArrayList<Object>();
    }

    @EdgeSet
    private List<Object> edges;

    @VertexSet
    private List<Object> vertexes;
    
    ...
}
```

### Create repositories
Extends CosmosRepository interface, which provides Spring Data repository support.

```
import GremlinRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonRepository extends GremlinRepository<Person, String> {
        List<Person> findByName(String name); 
}
```

`findByName` method is custom query method, it will find the person with the ```name``` property.

### Create an Application class
Here create an application class with all the components

```
@SpringBootApplication
public class SampleApplication implements CommandLineRunner {

    @Autowired
    private PersonRepository repository;

    public static void main(String[] args) {
        SpringApplication.run(SampleApplication.class, args);
    }

    public void run(String... var1) throws Exception {

        private final Person testUser = new Person("PERSON_ID", "PERSON_NAME", "PERSON_AGE");

        repository.deleteAll();
        repository.save(testUser);

        ... 
    }
}
```
Autowired UserRepository interface, then can do save, delete and find operations. Spring Data Azure Cosmos DB uses the DocumentTemplate to execute the queries behind *find*, *save* methods. You can use the template yourself for more complex queries.

## Allow telemetry
Microsoft would like to collect data about how users use this Spring boot starter. Microsoft uses this information to improve our tooling experience. Participation is voluntary. If you don't want to participate, just simply disable it by setting below configuration in `application.properties`.
```
gremlin.allow-telemetry=false
```
When telemetry is enabled, an HTTP request will be sent to URL `https://dc.services.visualstudio.com/v2/track`. So please make sure it's not blocked by your firewall.  
Find more information about Azure Service Privacy Statement, please check [Microsoft Online Services Privacy Statement](https://www.microsoft.com/en-us/privacystatement/OnlineServices/Default.aspx). 


## Filing Issues

If you encounter any bug, please file an issue [here](https://github.com/Microsoft/spring-data-gremlin/issues/new?template=custom.md).

To suggest a new feature or changes that could be made, file an issue the same way you would for a bug.
