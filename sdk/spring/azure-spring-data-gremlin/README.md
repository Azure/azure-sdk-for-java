# Azure Spring Data Gremlin client library for Java

## Key concepts

**Spring Data Gremlin** provides initial Spring Data support for those databases using Gremlin query language. With annotation oriented programming model, it simplified the mapping to the database entity. It also provides supports for basic and custom query. 

This project works with *any Gremlin-compatible* data store, and also with [Azure Cosmos DB](https://docs.microsoft.com/azure/cosmos-db/introduction). Cosmos is a globally-distributed database service that allows developers to work with data using a variety of standard APIs, such as Graph, MongoDB, and SQL. Spring Data Gremlin provides a delightful experience to interact with Azure Cosmos DB Graph API. 

### Feature List
- Spring Data CRUDRepository basic CRUD functionality
    - save
    - findAll
    - findById
    - deleteAll
    - deleteById
- Spring Data [@Id](https://github.com/spring-projects/spring-data-commons/blob/master/src/main/java/org/springframework/data/annotation/Id.java) annotation.
  There're 2 ways to map a field in domain class to `id` field of a database entity.
  - annotate a field in domain class with `@Id` 
  - set name of this field to `id`
- Default annotation
  - `@Vertex` maps an `Object` to a `Vertex`
  - `@VertexSet` maps a set of `Vertex`
  - `@Edge` maps an `Object` to an `Edge`
  - `@EdgeSet` maps to a set of `Edge`
  - `@EdgeFrom` maps to the head `Vertex` of an `Edge`
  - `@EdgeTo` maps to the tail `Vertex` of an `Edge`
  - `@Graph` maps to an `Object` to a `Graph`
- Supports advanced operations 
  - `<T> T findVertexById(Object id, Class<T> domainClass);`
  - `<T> T findEdgeById(Object id, Class<T> domainClass);`
  - `<T> boolean isEmptyGraph(T object)`
  - `long vertexCount()`
  - `long edgeCount()`
- Supports [Spring Data custom query](https://docs.spring.io/spring-data/commons/docs/current/reference/html/#repositories.query-methods.details) find operation, e.g.,  `findByAFieldAndBField`
- Supports any class type in domain class including collection and nested type.


### Spring Data Version Support
This repository only supports Spring Data 2.x. Version mapping between spring boot and spring-data-gremlin: 

| Spring boot version                                         | spring-data-gremlin version                                                                                                                                                                                                                   |
|:-----------------------------------------------------------:|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|
| ![version](https://img.shields.io/badge/version-2.3.x-blue) | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.spring.data.gremlin/spring-data-gremlin/2.3.svg)](https://search.maven.org/search?q=g:com.microsoft.spring.data.gremlin%20AND%20a:spring-data-gremlin%20AND%20v:2.3.*) |
| ![version](https://img.shields.io/badge/version-2.2.x-blue) | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.spring.data.gremlin/spring-data-gremlin/2.2.svg)](https://search.maven.org/search?q=g:com.microsoft.spring.data.gremlin%20AND%20a:spring-data-gremlin%20AND%20v:2.2.*) |
| ![version](https://img.shields.io/badge/version-2.1.x-blue) | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.spring.data.gremlin/spring-data-gremlin/2.1.svg)](https://search.maven.org/search?q=g:com.microsoft.spring.data.gremlin%20AND%20a:spring-data-gremlin%20AND%20v:2.1.*) |
| ![version](https://img.shields.io/badge/version-2.0.x-blue) | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.spring.data.gremlin/spring-data-gremlin/2.0.svg)](https://search.maven.org/search?q=g:com.microsoft.spring.data.gremlin%20AND%20a:spring-data-gremlin%20AND%20v:2.0.*) |

## Getting started

### Add the dependency
`spring-data-gremlin` is published on Maven Central Repository.  
If you are using Maven, add the following dependency.  

[//]: # ({x-version-update-start;com.azure:azure-spring-data-gremlin;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-spring-data-gremlin</artifactId>
    <version>2.3.1-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Setup Configuration
Setup `application.yml` file.(Use Azure Cosmos DB Graph as an example.)

```yml
gremlin:
  endpoint: url-of-endpoint 
  port: 443
  username: /dbs/your-db-name/colls/your-collection-name
  password: your-password
  telemetryAllowed: true # set false to disable telemetry

```

### Define an entity
Define a simple Vertex entity with `@Vertex`.

<!-- embedme /src/samples/java/com/azure/spring/data/gremlin/Person.java#L16-L35 -->
```java
@Vertex
public class Person {

    @Id
    private String id;

    private String name;

    private String age;

    public Person() {

    }

    public Person(String id, String name, String age) {
        this.id = id;
        this.name = name;
        this.age = age;
    }
}
```

Define a simple Edge entity with `@Edge`.
<!-- embedme /src/samples/java/com/azure/spring/data/gremlin/Relation.java#L18-L32 -->
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
Define a simple Graph entity with `@Graph`.
<!-- embedme /src/samples/java/com/azure/spring/data/gremlin/Network.java#L21-L38 -->
```java
@Graph
public class Network {

    @Id
    private String id;

    public Network() {
        this.edges = new ArrayList<>();
        this.vertexes = new ArrayList<>();
    }

    @EdgeSet
    private List<Object> edges;

    @VertexSet
    private List<Object> vertexes;

}
```

### Create repositories
Extends GremlinRepository interface, which provides Spring Data repository support.
<!-- embedme /src/samples/java/com/azure/spring/data/gremlin/PersonRepository.java#L18-L23 -->
```java
@Repository
public interface PersonRepository extends GremlinRepository<Person, String> {

    List<Person> findByName(String name);

}
```
`findByName` method is custom query method, it will find the person with the `name` property.

### Create an application
Here create an application class with all the components
<!-- embedme /src/samples/java/com/azure/spring/data/gremlin/SampleApplication.java#L18-L35 -->
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
Autowired UserRepository interface, then can do save, delete and find operations. 

## Examples
Please refer to [sample project](../azure-spring-boot-samples/azure-spring-data-sample-gremlin/) and [web sample project](../azure-spring-boot-samples/azure-spring-data-sample-gremlin-web-service).

## Data / Telemetry

This project collects usage data and sends it to Microsoft to help improve our products and services. Read our [privacy](https://privacy.microsoft.com/privacystatement) statement to learn more.

## Contributing
## Troubleshooting
## Next steps
