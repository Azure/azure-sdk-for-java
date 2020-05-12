# Sample for Azure Gremlin Spring Boot Starter client library for Java

## Key concepts

**Spring Data Gremlin** provides initial Spring Data support for those databases using Gremlin query language. With annotation oriented programming model, it simplified the mapping to the database entity. It also provides supports for basic and custom query. 

This project works with *any Gremlin-compatible* data store, and also with [Azure Cosmos DB](https://docs.microsoft.com/azure/cosmos-db/introduction). Cosmos is a globally-distributed database service that allows developers to work with data using a variety of standard APIs, such as Graph, MongoDB, and SQL. Spring Data Gremlin provides a delightful experience to interact with Azure Cosmos DB Graph API. 

## Examples
Please refer to [sample project here](../azure-spring-boot-samples/azure-spring-boot-sample-data-gremlin).

## Getting started

### Add the dependency
`azure-data-gremlin-spring-boot-starter` is published on Maven Central Repository. If you are using Maven, add the following dependency.  

[//]: # ({x-version-update-start;com.azure:azure-data-gremlin-spring-boot-starter;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-data-gremlin-spring-boot-starter</artifactId>
    <version>2.2.5-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Setup Configuration
Setup ```application.yml``` file.(Use Azure Cosmos DB Graph as an example.)

```yaml
gremlin:
  endpoint: url-of-endpoint 
  port: 443
  username: /dbs/your-db-name/colls/your-collection-name
  password: your-password
  telemetryAllowed: true # set false to disable telemetry

```

### Define an entity
Define a simple Vertex entity with ```@Vertex```.
<!-- embedme ../azure-spring-boot/src/samples/java/com/azure/spring/gremlin/Person.java#L10-L80 -->
```java
@Vertex
public class Person {

    @Id
    private String id;

    private String name;

    private String level;

    public Person() {
    }

    public Person(String id, String name, String level) {
        this.id = id;
        this.name = name;
        this.level = level;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Person person = (Person) o;
        return Objects.equals(id, person.id)
                    && Objects.equals(name, person.name)
                    && Objects.equals(level, person.level);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, level);
    }

    @Override
    public String toString() {
        return "Person{"
                    + "id='" + id + '\''
                    + ", name='" + name + '\''
                    + ", level='" + level + '\''
                    + '}';
    }
}
```

Define a simple Edge entity with ```@Edge```.
<!-- embedme ../azure-spring-boot/src/samples/java/com/azure/spring/gremlin/Relation.java#L10-L23 -->

```java
@Edge
public class Relation {

    @Id
    private String id;

    private String name;

    @EdgeFrom
    private Person personFrom;

    @EdgeTo
    private Person personTo;
}
```
Define a simple Graph entity with ```@Graph```.
<!-- embedme ../azure-spring-boot/src/samples/java/com/azure/spring/gremlin/Network.java#L13-L29 -->
```java
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
}
```

### Create repositories
Extends CosmosRepository interface, which provides Spring Data repository support.
<!-- embedme ../azure-spring-boot/src/samples/java/com/azure/spring/gremlin/PersonRepository.java#L10-L13 -->
```java
@Repository
public interface PersonRepository extends GremlinRepository<Person, String> {
        List<Person> findByName(String name); 
}
```

`findByName` method is custom query method, it will find the person with the ```name``` property.

### Create an Application class
Here create an application class with all the components
<!-- embedme ../azure-spring-boot/src/samples/java/com/azure/spring/gremlin/SampleApplication.java#L10-L27 -->
```java
@SpringBootApplication
public class SampleApplication implements CommandLineRunner {

    @Autowired
    private PersonRepository repository;

    public static void main(String[] args) {
        SpringApplication.run(SampleApplication.class, args);
    }

    public void run(String... var1) {

        final Person testUser = new Person("PERSON_ID", "PERSON_NAME", "PERSON_AGE");

        repository.deleteAll();
        repository.save(testUser);
    }
}
```
Autowired UserRepository interface, then can do save, delete and find operations. Spring Data Azure Cosmos DB uses the DocumentTemplate to execute the queries behind *find*, *save* methods. You can use the template yourself for more complex queries.

## Allow telemetry
Microsoft would like to collect data about how users use this Spring boot starter. Microsoft uses this information to improve our tooling experience. Participation is voluntary. If you don't want to participate, just simply disable it by setting below configuration in `application.properties`.
```properties
gremlin.allow-telemetry=false
```
When telemetry is enabled, an HTTP request will be sent to URL `https://dc.services.visualstudio.com/v2/track`. So please make sure it's not blocked by your firewall.  
Find more information about Azure Service Privacy Statement, please check [Microsoft Online Services Privacy Statement](https://www.microsoft.com/privacystatement/OnlineServices/Default.aspx). 


## Troubleshooting

If you encounter any bug, please file an issue [here](https://github.com/Microsoft/spring-data-gremlin/issues/new?template=custom.md).

To suggest a new feature or changes that could be made, file an issue the same way you would for a bug.

## Next steps
## Contributing
