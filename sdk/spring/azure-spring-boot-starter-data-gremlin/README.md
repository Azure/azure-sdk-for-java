# Azure Gremlin Spring Boot Starter client library for Java
The Spring Data Gremlin Starter provides Spring Data support for the Gremlin query language from Apache, which developers can use with any Gremlin-compatible data store.

[Package (Maven)][package] | [API reference documentation][refdocs] | [Product documentation][docs] | [Samples][sample]

## Getting started
### Prerequisites
- Java Development Kit (JDK) with version 8 or above
- [Azure Subscription][azure_subscription]
- [Maven](http://maven.apache.org/) 3.0 and above

### Include the package
[//]: # ({x-version-update-start;com.microsoft.azure:azure-data-gremlin-spring-boot-starter;current})
```xml
<dependency>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>azure-data-gremlin-spring-boot-starter</artifactId>
    <version>2.3.3</version>
</dependency>
```
[//]: # ({x-version-update-end})

## Key concepts

**Spring Data Gremlin** provides initial Spring Data support for those databases using Gremlin query language. With annotation oriented programming model, it simplified the mapping to the database entity. It also provides supports for basic and custom query. 

This project works with *any Gremlin-compatible* data store, and also with [Azure Cosmos DB](https://docs.microsoft.com/azure/cosmos-db/introduction). Cosmos is a globally-distributed database service that allows developers to work with data using a variety of standard APIs, such as Graph, MongoDB, and SQL. Spring Data Gremlin provides a delightful experience to interact with Azure Cosmos DB Graph API. 

## Examples

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

## Troubleshooting
### Enable client logging
Azure SDKs for Java offer a consistent logging story to help aid in troubleshooting application errors and expedite their resolution. The logs produced will capture the flow of an application before reaching the terminal state to help locate the root issue. View the [logging][logging] wiki for guidance about enabling logging.

### Enable Spring logging
Spring allow all the supported logging systems to set logger levels set in the Spring Environment (for example, in application.properties) by using `logging.level.<logger-name>=<level>` where level is one of TRACE, DEBUG, INFO, WARN, ERROR, FATAL, or OFF. The root logger can be configured by using logging.level.root.

The following example shows potential logging settings in `application.properties`:

```properties
logging.level.root=WARN
logging.level.org.springframework.web=DEBUG
logging.level.org.hibernate=ERROR
```

For more information about setting loging in pring, please refer to the [official doc](https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-logging).
 

## Next steps

The following section provide a sample project illustrating how to use the starter.
### More sample code
- [Gremlin SQL API](../azure-spring-boot-samples/azure-spring-boot-sample-data-gremlin)

## Contributing
This project welcomes contributions and suggestions.  Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

Please follow [instructions here](../CONTRIBUTING.md) to build from source or contribute.

<!-- LINKS -->
[docs]: https://docs.microsoft.com/azure/developer/java/spring-framework/configure-spring-data-gremlin-java-app-with-cosmos-db
[refdocs]: https://azure.github.io/azure-sdk-for-java/spring.html#azure-data-gremlin-spring-boot-starter
[package]: https://mvnrepository.com/artifact/com.microsoft.azure/spring-data-gremlin-boot-starter
[sample]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/spring/azure-spring-boot-samples/azure-spring-boot-sample-data-gremlin
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-with-Azure-SDK#use-logback-logging-framework-in-a-spring-boot-application
[azure_subscription]: https://azure.microsoft.com/free
