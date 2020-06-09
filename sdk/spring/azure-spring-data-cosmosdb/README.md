[![Travis CI](https://travis-ci.org/Microsoft/spring-data-cosmosdb.svg?branch=master)](https://travis-ci.org/Microsoft/spring-data-cosmosdb)
[![codecov](https://codecov.io/gh/Microsoft/spring-data-cosmosdb/branch/master/graph/badge.svg)](https://codecov.io/gh/Microsoft/spring-data-cosmosdb)
[![MIT License](http://img.shields.io/badge/license-MIT-green.svg) ](https://github.com/Microsoft/spring-data-cosmosdb/blob/master/LICENSE)


# Spring Data for Azure Cosmos DB 

[Azure Cosmos DB](https://docs.microsoft.com/en-us/azure/cosmos-db/introduction) is a globally-distributed database service that allows developers to work with data using a variety of standard APIs, such as SQL, MongoDB, Cassandra, Graph, and Table.

**Spring Data Azure Cosmos DB** provides initial Spring Data support for Azure Cosmos DB using the [SQL API](https://docs.microsoft.com/en-us/azure/cosmos-db/sql-api-introduction), based on Spring Data framework. Currently it only supports SQL API, the other APIs are in the plan. 

## TOC

* [Sample Code](#sample-code)
* [Spring data version support](#spring-data-version-support)
* [Feature List](#feature-list)
* [Quick Start](#quick-start)
* [Query Partitioned Collection](QueryPartitionedCollection.md)
* [Snapshots](#snapshots)
* [Filing Issues](#filing-issues)
* [How to Contribute](#how-to-contribute)
* [Code of Conduct](#code-of-conduct)

## Sample Code
Please refer to [sample project here](./samplecode).

## Spring Data Version Support
Version mapping between spring boot and spring-data-cosmosdb:

| Spring boot version                                         | spring-data-cosmosdb version                                                                                                                                                                                        |
| :----------------------------------------------------------:|:-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|
| ![version](https://img.shields.io/badge/version-2.3.x-blue) | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/spring-data-cosmosdb/2.3.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:spring-data-cosmosdb%20AND%20v:2.3.*) |
| ![version](https://img.shields.io/badge/version-2.2.x-blue) | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/spring-data-cosmosdb/2.2.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:spring-data-cosmosdb%20AND%20v:2.2.*) |
| ![version](https://img.shields.io/badge/version-2.1.x-blue) | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/spring-data-cosmosdb/2.1.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:spring-data-cosmosdb%20AND%20v:2.1.*) |
| ![version](https://img.shields.io/badge/version-2.0.x-blue) | [![Maven Central](https://img.shields.io/maven-central/v/com.microsoft.azure/spring-data-cosmosdb/2.0.svg)](https://search.maven.org/search?q=g:com.microsoft.azure%20AND%20a:spring-data-cosmosdb%20AND%20v:2.0.*) |

## Feature List
- Spring Data ReactiveCrudRepository CrudRepository basic CRUD functionality
    - save
    - findAll
    - findOne by Id
    - deleteAll
    - delete by Id
    - delete entity
- Spring Data [@Id](https://github.com/spring-projects/spring-data-commons/blob/db62390de90c93a78743c97cc2cc9ccd964994a5/src/main/java/org/springframework/data/annotation/Id.java) annotation.
  There're 2 ways to map a field in domain class to `id` field of Azure Cosmos DB document.
  - annotate a field in domain class with `@Id`, this field will be mapped to document `id` in Cosmos DB. 
  - set name of this field to `id`, this field will be mapped to document `id` in Azure Cosmos DB.
- Custom collection Name.
  By default, collection name will be class name of user domain class. To customize it, add the `@Document(collection="myCustomCollectionName")` annotation to the domain class. The collection field also supports SpEL expressions (eg. `collection = "${dynamic.collection.name}"` or `collection = "#{@someBean.getContainerName()}"`) in order to provide collection names programmatically/via configuration properties.
- Custom IndexingPolicy
  By default, IndexingPolicy will be set by azure service. To customize it add annotation `@DocumentIndexingPolicy` to domain class. This annotation has 4 attributes to customize, see following:
```java
   boolean automatic;     // Indicate if indexing policy use automatic or not
   IndexingMode mode;     // Indexing policy mode, option Consistent|Lazy|None.
   String[] includePaths; // Included paths for indexing
   String[] excludePaths; // Excluded paths for indexing
```
- Supports Optimistic Locking for specific collections, which means upserts/deletes by document will fail with an exception in case the document was modified by another process in the meanwhile. To enable Optimistic Locking for a collection, just create a string `_etag` field and mark it with the `@Version` annotation. See the following:

```java
@Document(collection = "myCollection")
class MyDocument {
    String id;
    String data;
    @Version
    String _etag;
}
```
- Supports [Azure Cosmos DB partition](https://docs.microsoft.com/en-us/azure/cosmos-db/partition-data). To specify a field of domain class to be partition key field, just annotate it with `@PartitionKey`. When you do CRUD operation, pls specify your partition value. For more sample on partition CRUD, pls refer to [test here](./src/test/java/com/microsoft/azure/spring/data/cosmosdb/repository/integration/AddressRepositoryIT.java)
- Supports [Spring Data custom query](https://docs.spring.io/spring-data/commons/docs/current/reference/html/#repositories.query-methods.details) find operation, e.g., `findByAFieldAndBField`
- Supports [Spring Data pagable and sort](https://docs.spring.io/spring-data/commons/docs/current/reference/html/#repositories.special-parameters).
  - Based on available RUs on the database account, cosmosDB can return documents less than or equal to the requested size.
  - Due to this variable number of returned documents in every iteration, user should not rely on the totalPageSize, and instead iterating over pageable should be done in this way.  
```java
    final CosmosPageRequest pageRequest = new CosmosPageRequest(0, pageSize, null);
    Page<T> page = tRepository.findAll(pageRequest);
    List<T> pageContent = page.getContent();
    while(page.hasNext()) {
        Pageable nextPageable = page.nextPageable();
        page = repository.findAll(nextPageable);
        pageContent = page.getContent();
    }
```
- Supports [spring-boot-starter-data-rest](https://projects.spring.io/spring-data-rest/).
- Supports List and nested type in domain class.
- Configurable ObjectMapper bean with unique name `cosmosdbObjectMapper`, only configure customized ObjectMapper if you really need to. e.g.,
```java
   @Bean(name = "cosmosdbObjectMapper")
   public ObjectMapper objectMapper() {
      return new ObjectMapper(); // Do configuration to the ObjectMapper if required
   }
```

## Quick Start

### Add the dependency
`spring-data-cosmosdb` is published on Maven Central Repository.  
If you are using Maven, add the following dependency.  

```xml
<dependency>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>spring-data-cosmosdb</artifactId>
    <version>2.2.4</version>
</dependency>
```

### Setup Configuration
Setup configuration class.

CosmosKeyCredential feature provides capability to rotate keys on the fly. You can switch keys using switchToSecondaryKey(). 
For more information on this, see the Sample Application code.

### Sync and Reactive Repository support
2.2.x supports both sync and reactive repository support. 

Use `@EnableCosmosRepositories` to enable sync repository support. 

For reactive repository support, use `@EnableReactiveCosmosRepositories`

### Response Diagnostics String and Query Metrics
2.2.x supports Response Diagnostics String and Query Metrics. 
Set `populateQueryMetrics` flag to true in application.properties to enable query metrics.
In addition to setting the flag, implement `ResponseDiagnosticsProcessor` to log diagnostics information. 

```java
@Configuration
@EnableCosmosRepositories
@Slf4j
public class AppConfiguration extends AbstractCosmosConfiguration {

    @Value("${azure.cosmosdb.uri}")
    private String uri;

    @Value("${azure.cosmosdb.key}")
    private String key;

    @Value("${azure.cosmosdb.secondaryKey}")
    private String secondaryKey;

    @Value("${azure.cosmosdb.database}")
    private String dbName;

    @Value("${azure.cosmosdb.populateQueryMetrics}")
    private boolean populateQueryMetrics;
    
    private CosmosKeyCredential cosmosKeyCredential;

    public CosmosDBConfig getConfig() {
        this.cosmosKeyCredential = new CosmosKeyCredential(key);
        CosmosDbConfig cosmosdbConfig = CosmosDBConfig.builder(uri, 
            this.cosmosKeyCredential, dbName).build();
        cosmosdbConfig.setPopulateQueryMetrics(populateQueryMetrics);
        cosmosdbConfig.setResponseDiagnosticsProcessor(new ResponseDiagnosticsProcessorImplementation());
        return cosmosdbConfig;
    }
    
    public void switchToSecondaryKey() {
        this.cosmosKeyCredential.key(secondaryKey);
    }
    
    private static class ResponseDiagnosticsProcessorImplementation implements ResponseDiagnosticsProcessor {
    
        @Override
        public void processResponseDiagnostics(@Nullable ResponseDiagnostics responseDiagnostics) {
            log.info("Response Diagnostics {}", responseDiagnostics);
        }
    }

}
```
Or if you want to customize your config:
```java
public CosmosDBConfig getConfig() {
    this.cosmosKeyCredential = new CosmosKeyCredential(key);
    CosmosDBConfig cosmosDbConfig = CosmosDBConfig.builder(uri, this.cosmosKeyCredential, dbName).build();
    cosmosDbConfig.getConnectionPolicy().setConnectionMode(ConnectionMode.DIRECT);
    cosmosDbConfig.getConnectionPolicy().setMaxPoolSize(1000);
    return cosmosDbConfig;
}
```
By default, `@EnableCosmosRepositories` will scan the current package for any interfaces that extend one of Spring Data's repository interfaces. Using it to annotate your Configuration class to scan a different root package by type if your project layout has multiple projects and it's not finding your repositories.
```java
@Configuration
@EnableCosmosRepositories(basePackageClass=UserRepository.class)
public class AppConfiguration extends AbstractCosmosConfiguration {
    // configuration code
}
```


### Define an entity
Define a simple entity as Document in Azure Cosmos DB.

You can define entities by adding the `@Document` annotation and specifying properties related to the container, such as the container name, request units (RUs), time to live, and auto-create container. 

Containers are created automatically unless you don't want them to: Set `autoCreateCollection` to false in `@Document` annotation to disable auto creation of containers. 

Note: By default request units assigned to newly created containers is 4000. Specify different ru value to customize request units for container created by the SDK (minimum RU value is 400). 

```java
@Document(collection = "myCollection", ru = "400")
public class User {
    private String id;
    private String firstName;

    @PartitionKey
    private String lastName;
 
    ... // setters and getters
    
    public User() {
        // If you do not want to create a default constructor, 
        // use annotation @JsonCreator and @JsonProperty in the full args constructor
    }
    
    public User(String id, String firstName, String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @Override
    public String toString() {
        return String.format("User: %s %s, %s", firstName, lastName, id);
    }
}
```
`id` field will be used as document id in Azure Cosmos DB. If you want use another field like `emailAddress` as document `id`, just annotate that field with `@Id` annotation.

Annotation `@Document(collection="mycollection")` is used to specify collection name in Azure Cosmos DB.
Annotation `@PartitionKey` on `lastName` field is used to specify this field be partition key in Azure Cosmos DB.

```java
@Document(collection = "mycollection")
public class User {
    @Id
    private String emailAddress;

    ...
}
```

### Create repositories
Extends CosmosRepository interface, which provides Spring Data repository support.

```java
import CosmosRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CosmosRepository<User, String> {
    List<User> findByFirstName(String firstName); 
}
```

`findByFirstName` method is custom query method, it will find documents per FirstName.

### Create an Application class
Here create an application class with all the components

```java
@SpringBootApplication
public class SampleApplication implements CommandLineRunner {

    @Autowired
    private UserRepository repository;
    
    @Autowired
    private ApplicationContext applicationContext;

    public static void main(String[] args) {
        SpringApplication.run(SampleApplication.class, args);
    }

    public void run(String... var1) throws Exception {

        final User testUser = new User("testId", "testFirstName", "testLastName");

        repository.deleteAll();
        repository.save(testUser);

        // to find by Id, please specify partition key value if collection is partitioned
        final User result = repository.findOne(testUser.getId(), testUser.getLastName);
        // if emailAddress is mapped to id, then 
        // final User result = respository.findOne(testUser.getEmailAddress(), testUser.getLastName());
        
        //  Switch to secondary key
        UserRepositoryConfiguration bean = 
            applicationContext.getBean(UserRepositoryConfiguration.class);
        bean.switchToSecondaryKey();
        
        //  Now repository will use secondary key
        repository.save(testUser);

    }
}
```
Autowired UserRepository interface, then can do save, delete and find operations. Spring Data Azure Cosmos DB uses the CosmosTemplate to execute the queries behind *find*, *save* methods. You can use the template yourself for more complex queries.

## Snapshots
[![Nexus OSS](https://img.shields.io/nexus/snapshots/https/oss.sonatype.org/com.microsoft.azure/spring-data-cosmosdb.svg)](https://oss.sonatype.org/content/repositories/snapshots/com/microsoft/azure/spring-data-cosmosdb/)

Snapshots built from `master` branch are available, add [maven repositories](https://maven.apache.org/settings.html#Repositories) configuration to your pom file as below. 
```xml
<repositories>
  <repository>
    <id>nexus-snapshots</id>
    <url>https://oss.sonatype.org/content/repositories/snapshots/</url>
    <snapshots>
      <enabled>true</enabled>
      <updatePolicy>always</updatePolicy>
    </snapshots>
  </repository>
</repositories>
```

## Filing Issues

If you encounter any bug, please file an issue [here](https://github.com/Microsoft/spring-data-cosmosdb/issues/new).

To suggest a new feature or changes that could be made, file an issue the same way you would for a bug.

## How To Contribute

Contribution is welcome. Please follow [this instruction](./HowToContribute.md) to contribute code.

## Code of Conduct

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.

### Data/Telemetry

 This project collects usage data and sends it to Microsoft to help improve our products and services. Read our [privacy](https://privacy.microsoft.com/en-us/privacystatement) statement to learn more.
