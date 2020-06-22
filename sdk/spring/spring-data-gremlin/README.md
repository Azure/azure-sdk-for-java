[![MIT License](http://img.shields.io/badge/license-MIT-green.svg) ](https://github.com/Microsoft/spring-data-gremlin/blob/master/LICENSE)
[![Build Status](https://travis-ci.org/Microsoft/spring-data-gremlin.svg?branch=master)](https://travis-ci.org/Microsoft/spring-data-gremlin)
[![codecov](https://codecov.io/gh/Microsoft/spring-data-gremlin/branch/master/graph/badge.svg)](https://codecov.io/gh/Microsoft/spring-data-gremlin) 

# Spring Data Gremlin 

**Spring Data Gremlin** provides initial Spring Data support for those databases using Gremlin query language. With annotation oriented programming model, it simplified the mapping to the database entity. It also provides supports for basic and custom query. 

This project works with *any Gremlin-compatible* data store, and also with [Azure Cosmos DB](https://docs.microsoft.com/en-us/azure/cosmos-db/introduction). Cosmos is a globally-distributed database service that allows developers to work with data using a variety of standard APIs, such as Graph, MongoDB, and SQL. Spring Data Gremlin provides a delightful experience to interact with Azure Cosmos DB Graph API. 

## Spring Data Version Support
Version mapping between spring boot and spring-data-gremlin: 

| Spring boot version                                         | spring-data-gremlin version                                                                                                                                                                                                                   |
|:-----------------------------------------------------------:|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|
| ![version](https://img.shields.io/badge/version-2.3.x-blue) | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.spring.data.gremlin/spring-data-gremlin/2.3.svg)](https://search.maven.org/search?q=g:com.microsoft.spring.data.gremlin%20AND%20a:spring-data-gremlin%20AND%20v:2.3.*) |
| ![version](https://img.shields.io/badge/version-2.2.x-blue) | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.spring.data.gremlin/spring-data-gremlin/2.2.svg)](https://search.maven.org/search?q=g:com.microsoft.spring.data.gremlin%20AND%20a:spring-data-gremlin%20AND%20v:2.2.*) |
| ![version](https://img.shields.io/badge/version-2.1.x-blue) | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.spring.data.gremlin/spring-data-gremlin/2.1.svg)](https://search.maven.org/search?q=g:com.microsoft.spring.data.gremlin%20AND%20a:spring-data-gremlin%20AND%20v:2.1.*) |
| ![version](https://img.shields.io/badge/version-2.0.x-blue) | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.spring.data.gremlin/spring-data-gremlin/2.0.svg)](https://search.maven.org/search?q=g:com.microsoft.spring.data.gremlin%20AND%20a:spring-data-gremlin%20AND%20v:2.0.*) |

## TOC

* [Welcome to Contribute](#welcome-to-contribute)
* [Sample Code](#sample-code)
* [Spring data version support](#spring-data-version-support)
* [Feature List](#feature-list)
* [Quick Start](#quick-start)
* [Filing Issues](#filing-issues)
* [Code of Conduct](#code-of-conduct)

## Welcome To Contribute

Contribution is welcome. Please follow [this instruction](./CONTRIBUTING.md) to contribute code.

## Sample Code
Please refer to [sample project here](./examples/example/).

## Spring data version support
This repository only supports Spring Data 2.x. 

## Feature List
- Spring Data CRUDRepository basic CRUD functionality
    - save
    - findAll
    - findById
    - deleteAll
    - deleteById
- Spring Data [@Id](https://github.com/spring-projects/spring-data-commons/blob/db62390de90c93a78743c97cc2cc9ccd964994a5/src/main/java/org/springframework/data/annotation/Id.java) annotation.
  There're 2 ways to map a field in domain class to `id` field of a database entity.
  - annotate a field in domain class with `@Id` 
  - set name of this field to `id`
- Default annotaion
  - ```@Vertex``` maps an ```Object``` to a ```Vertex```
  - ```@VertexSet``` maps a set of ```Vertex```
  - ```@Edge``` maps an ```Object``` to an ```Edge```
  - ```@EdgeSet``` maps to a set of ```Edge```
  - ```@EdgeFrom``` maps to the head ```Vertex``` of an ```Edge```
  - ```@EdgeTo``` maps to the tail ```Vertex``` of an ```Edge```
  - ```@Graph``` maps to an ```Object``` to a ```Graph```
- Supports advanced operations 
  - ```<T> T findVertexById(Object id, Class<T> domainClass);```
  - ```<T> T findEdgeById(Object id, Class<T> domainClass);```
  - ```<T> boolean isEmptyGraph(T object)```
  - ```long vertexCount()```
  - ```long edgeCount()```
- Supports [Spring Data custom query](https://docs.spring.io/spring-data/commons/docs/current/reference/html/#repositories.query-methods.details) find operation, e.g.,  `findByAFieldAndBField`
- Supports any class type in domain class including collection and nested type.

  

## Quick Start

### Add the dependency
`spring-data-gremlin` is published on Maven Central Repository.  
If you are using Maven, add the following dependency.  

```xml
<dependency>
    <groupId>com.microsoft.spring.data.gremlin</groupId>
    <artifactId>spring-data-gremlin</artifactId>
    <version>2.1.7</version>
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
Extends DocumentDbRepository interface, which provides Spring Data repository support.

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
## Filing Issues

If you encounter any bug, please file an issue [here](https://github.com/Microsoft/spring-data-gremlin/issues/new?template=custom.md).

To suggest a new feature or changes that could be made, file an issue the same way you would for a bug.


## Code of Conduct

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.

## Data/Telemetry

This project collects usage data and sends it to Microsoft to help improve our products and services. Read our [privacy](https://privacy.microsoft.com/en-us/privacystatement) statement to learn more.

