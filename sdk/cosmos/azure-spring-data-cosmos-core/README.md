# Azure Spring Data Cosmos Core client library for Java

**Azure Spring Data Cosmos** provides core Spring Data support for Azure Cosmos DB using the [SQL API][sql_api_query], based on Spring Data framework.
[Azure Cosmos DB][cosmos_introduction] is a globally-distributed database service which allows developers to work with data using a variety of standard APIs, such as SQL, MongoDB, Cassandra, Graph, and Table.

## Spring data version support
This project supports both [spring-data-commons 2.2.x][azure_spring_data_2_2_cosmos] and [spring-data-commons 2.3.x][azure_spring_data_2_3_cosmos] versions.

## Getting started

### Include the package
Please refer to [azure-spring-data-2-2-cosmos readme][azure_spring_data_2_2_cosmos_readme] or [azure-spring-data-2-3-cosmos readme][azure_spring_data_2_3_cosmos_readme] on how to include the individual packages.

### Prerequisites

- Java Development Kit 8
- An active Azure account. If you don't have one, you can sign up for a [free account][azure_subscription]. Alternatively, you can use the [Azure Cosmos DB Emulator][local_emulator] for development and testing. As emulator https certificate is self signed, you need to import its certificate to java trusted cert store, [explained here][local_emulator_export_ssl_certificates]
- (Optional) SLF4J is a logging facade.
- (Optional) [SLF4J binding](http://www.slf4j.org/manual.html) is used to associate a specific logging framework with SLF4J.
- (Optional) Maven

SLF4J is only needed if you plan to use logging, please also download an SLF4J binding which will link the SLF4J API with the logging implementation of your choice. See the [SLF4J user manual](http://www.slf4j.org/manual.html) for more information.

### Setup Configuration Class
- In order to set up configuration class, you'll need to extend `AbstractCosmosConfiguration`

- Azure-spring-data-cosmos also supports `Response Diagnostics String` and `Query Metrics`.
Set `queryMetricsEnabled` flag to true in application.properties to enable query metrics.
In addition to setting the flag, implement `ResponseDiagnosticsProcessor` to log diagnostics information.
<!-- embedme src/samples/java/com/azure/cosmos/AppConfiguration.java#L23-L82 -->

```java
@Configuration
@EnableCosmosRepositories
public class AppConfiguration extends AbstractCosmosConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(AppConfiguration.class);

    @Value("${azure.cosmos.uri}")
    private String uri;

    @Value("${azure.cosmos.key}")
    private String key;

    @Value("${azure.cosmos.secondaryKey}")
    private String secondaryKey;

    @Value("${azure.cosmos.database}")
    private String dbName;

    @Value("${azure.cosmos.queryMetricsEnabled}")
    private boolean queryMetricsEnabled;

    private AzureKeyCredential azureKeyCredential;

    @Bean
    public CosmosClientBuilder getCosmosClientBuilder() {
        this.azureKeyCredential = new AzureKeyCredential(key);
        DirectConnectionConfig directConnectionConfig = new DirectConnectionConfig();
        GatewayConnectionConfig gatewayConnectionConfig = new GatewayConnectionConfig();
        return new CosmosClientBuilder()
            .endpoint(uri)
            .credential(azureKeyCredential)
            .directMode(directConnectionConfig, gatewayConnectionConfig);
    }

    @Override
    public CosmosConfig cosmosConfig() {
        return CosmosConfig.builder()
                           .enableQueryMetrics(queryMetricsEnabled)
                           .responseDiagnosticsProcessor(new ResponseDiagnosticsProcessorImplementation())
                           .build();
    }

    public void switchToSecondaryKey() {
        this.azureKeyCredential.update(secondaryKey);
    }

    @Override
    protected String getDatabaseName() {
        return "testdb";
    }

    private static class ResponseDiagnosticsProcessorImplementation implements ResponseDiagnosticsProcessor {

        @Override
        public void processResponseDiagnostics(@Nullable ResponseDiagnostics responseDiagnostics) {
            logger.info("Response Diagnostics {}", responseDiagnostics);
        }
    }

}
```
### Customizing Configuration
You can customize `DirectConnectionConfig` or `GatewayConnectionConfig` or both and provide them to `CosmosClientBuilder` bean to customize `CosmosAsyncClient`
<!-- embedme src/samples/java/com/azure/cosmos/AppConfigurationCodeSnippet.java#L45-L61 -->

```java
@Bean
public CosmosClientBuilder getCosmosClientBuilder() {

    DirectConnectionConfig directConnectionConfig = new DirectConnectionConfig();
    GatewayConnectionConfig gatewayConnectionConfig = new GatewayConnectionConfig();
    return new CosmosClientBuilder()
        .endpoint(uri)
        .directMode(directConnectionConfig, gatewayConnectionConfig);
}

@Override
public CosmosConfig cosmosConfig() {
    return CosmosConfig.builder()
                       .enableQueryMetrics(queryMetricsEnabled)
                       .responseDiagnosticsProcessor(new ResponseDiagnosticsProcessorImplementation())
                       .build();
}
```

By default, `@EnableCosmosRepositories` will scan the current package for any interfaces that extend one of Spring Data's repository interfaces.
Use it to annotate your Configuration class to scan a different root package by `@EnableCosmosRepositories(basePackageClass=UserRepository.class)` if your project layout has multiple projects.

### Define an entity
- Define a simple entity as item in Azure Cosmos DB.

- You can define entities by adding the `@Container` annotation and specifying properties related to the container, such as the container name, request units (RUs), time to live, and auto-create container.

- Containers will be created automatically unless you don't want them to. Set `autoCreateContainer` to false in `@Container` annotation to disable auto creation of containers.

- Note: By default request units assigned to newly created containers is 400. Specify different ru value to customize request units for the container created by the SDK (minimum RU value is 400).
<!-- embedme src/samples/java/com/azure/cosmos/User.java#L14-L62 -->
```java
@Container(containerName = "myContainer", ru = "400")
public class User {
    private String id;
    private String firstName;


    @PartitionKey
    private String lastName;

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
```
- `id` field will be used as Item id in Azure Cosmos DB. If you want use another field like `firstName` as item `id`, just annotate that field with `@Id` annotation.

- Annotation `@Container(containerName="myContainer")` specifies container name in Azure Cosmos DB.
- Annotation `@PartitionKey` on `lastName` field specifies this field as partition key in Azure Cosmos DB.
<!-- embedme src/samples/java/com/azure/cosmos/UserSample.java#L14-L19 -->
```java
@Container(containerName = "myContainer")
public class UserSample {
    @Id
    private String emailAddress;

}
```

### Create repositories
Extends CosmosRepository interface, which provides Spring Data repository support.
<!-- embedme src/samples/java/com/azure/cosmos/UserRepository.java#L15-L19 -->

```java
@Repository
public interface UserRepository extends CosmosRepository<User, String> {
    Iterable<User> findByFirstName(String firstName);
    User findOne(String id, String lastName);
}
```

- `findByFirstName` method is custom query method, it will find items per firstName.

### Create an Application class
Here create an application class with all the components
<!-- embedme src/samples/java/com/azure/cosmos/SampleApplication.java#L17-L49 -->

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

    public void run(String... var1) {

        final User testUser = new User("testId", "testFirstName", "testLastName");

        repository.deleteAll();
        repository.save(testUser);

        // to find by Id, please specify partition key value if collection is partitioned
        final User result = repository.findOne(testUser.getId(), testUser.getLastName());

        //  Switch to secondary key
        UserRepositoryConfiguration bean =
            applicationContext.getBean(UserRepositoryConfiguration.class);
        bean.switchToSecondaryKey();

        //  Now repository will use secondary key
        repository.save(testUser);

    }
}
```
- Autowire UserRepository interface, to perform operations like save, delete, find, etc.
- Spring Data Azure Cosmos DB uses the `CosmosTemplate` and `ReactiveCosmosTemplate` to execute the queries behind *find*, *save* methods. You can use the template yourself for more complex queries.

## Key concepts

### CrudRepository and ReactiveCrudRepository
- Azure Spring Data Cosmos supports ReactiveCrudRepository and CrudRepository which provides basic CRUD functionality
    - save
    - findAll
    - findOne by Id
    - deleteAll
    - delete by Id
    - delete entity

### Spring Data Annotations
- Spring Data [@Id annotation][spring_data_commons_id_annotation].
  There are 2 ways to map a field in domain class to `id` field of Azure Cosmos DB Item.
  - annotate a field in domain class with `@Id`, this field will be mapped to Item `id` in Cosmos DB.
  - set name of this field to `id`, this field will be mapped to Item `id` in Azure Cosmos DB.
- Supports auto generation of string type UUIDs using the @GeneratedValue annotation. The id field of an entity with a string
 type id can be annotated with `@GeneratedValue` to automatically generate a random UUID prior to insertion.
 <!-- embedme src/samples/java/com/azure/cosmos/GeneratedIdEntity.java#L8-L14 -->
 ```java
 public class GeneratedIdEntity {
 
     @Id
     @GeneratedValue
     private String id;
 
 }
 ```
- Custom container Name.
  By default, container name will be class name of user domain class. To customize it, add the `@Container(containerName="myCustomContainerName")` annotation to the domain class. The container field also supports SpEL expressions (eg. `container = "${dynamic.container.name}"` or `container = "#{@someBean.getContainerName()}"`) in order to provide container names programmatically/via configuration properties.
- Custom IndexingPolicy
  By default, IndexingPolicy will be set by azure service. To customize it add annotation `@CosmosIndexingPolicy` to domain class. This annotation has 4 attributes to customize, see following:
<!-- embedme src/samples/java/com/azure/cosmos/CosmosIndexingPolicyCodeSnippet.java#L16-L27 -->
```java
// Indicate if indexing policy use automatic or not
// Default value is true
boolean automatic() default Constants.DEFAULT_INDEXING_POLICY_AUTOMATIC;

// Indexing policy mode, option Consistent.
IndexingMode mode() default IndexingMode.CONSISTENT;

// Included paths for indexing
String[] includePaths() default {};

// Excluded paths for indexing
String[] excludePaths() default {};
```

### Azure Cosmos DB Partition
- Azure-spring-data-cosmos supports [Azure Cosmos DB partition][azure_cosmos_db_partition].
- To specify a field of domain class to be partition key field, just annotate it with `@PartitionKey`.
- When you perform CRUD operation, specify your partition value.
- For more sample on partition CRUD, please refer [test here][address_repository_it_test]

### Optimistic Locking
- Azure-spring-data-cosmos supports Optimistic Locking for specific containers, which means upserts/deletes by item will fail with an exception in case the item is modified by another process in the meanwhile.
- To enable Optimistic Locking for a container, just create a string `_etag` field and mark it with the `@Version` annotation. See the following:

<!-- embedme src/samples/java/com/azure/cosmos/MyItem.java#L14-L20 -->
```java
@Container(containerName = "myContainer")
public class MyItem {
    String id;
    String data;
    @Version
    String _etag;
}
```
### Spring Data custom query, pageable and sorting
- Azure-spring-data-cosmos supports [spring data custom queries][spring_data_custom_query]
- Example, find operation, e.g., `findByAFieldAndBField`
- Supports [Spring Data pageable and sort](https://docs.spring.io/spring-data/commons/docs/current/reference/html/#repositories.special-parameters).
  - Based on available RUs on the database account, cosmosDB can return items less than or equal to the requested size.
  - Due to this variable number of returned items in every iteration, user should not rely on the totalPageSize, and instead iterating over pageable should be done in this way.
<!-- embedme src/samples/java/com/azure/cosmos/PageableRepositoryCodeSnippet.java#L24-L35 -->
```java
private List<T> findAllWithPageSize(int pageSize) {

    final CosmosPageRequest pageRequest = new CosmosPageRequest(0, pageSize, null);
    Page<T> page = repository.findAll(pageRequest);
    List<T> pageContent = page.getContent();
    while (page.hasNext()) {
        Pageable nextPageable = page.nextPageable();
        page = repository.findAll(nextPageable);
        pageContent = page.getContent();
    }
    return pageContent;
}
```

### Spring Boot Starter Data Rest
- Azure-spring-data-cosmos supports [spring-boot-starter-data-rest](https://projects.spring.io/spring-data-rest/).
- Supports List and nested type in domain class.
- Configurable ObjectMapper bean with unique name `cosmosObjectMapper`, only configure customized ObjectMapper if you really need to. e.g.,
<!-- embedme src/samples/java/com/azure/cosmos/ObjectMapperConfigurationCodeSnippet.java#L17-L20 -->
```java
@Bean(name = "cosmosObjectMapper")
public ObjectMapper objectMapper() {
    return new ObjectMapper(); // Do configuration to the ObjectMapper if required
}
```

### Auditing
- Azure-spring-data-cosmos supports auditing fields on database entities using standard spring-data annotations.
- This feature can be enabled by adding `@EnableCosmosAuditing` annotation to your application configuration.
- Entities can annotate fields using `@CreatedBy`, `@CreatedDate`, `@LastModifiedBy` and `@LastModifiedDate`. These fields will be updated automatically.
<!-- embedme src/samples/java/com/azure/cosmos/AuditableUser.java#L13-L25 -->
```java
@Container(containerName = "myContainer")
public class AuditableUser {
    private String id;
    private String firstName;
    @CreatedBy
    private String createdBy;
    @CreatedDate
    private OffsetDateTime createdDate;
    @LastModifiedBy
    private String lastModifiedBy;
    @LastModifiedDate
    private OffsetDateTime lastModifiedByDate;
}
```

### Multi-database configuration
- Azure-spring-data-cosmos supports multi-database configuration, including "multiple database accounts" and "single account, with multiple databases".

The example uses the `application.properties` file
```properties
# primary account cosmos config
azure.cosmos.primary.uri=your-primary-cosmosDb-uri
azure.cosmos.primary.key=your-primary-cosmosDb-key
azure.cosmos.primary.secondaryKey=your-primary-cosmosDb-secondary-key
azure.cosmos.primary.database=your-primary-cosmosDb-dbName
azure.cosmos.primary.populateQueryMetrics=if-populate-query-metrics

# secondary account cosmos config
azure.cosmos.secondary.uri=your-secondary-cosmosDb-uri
azure.cosmos.secondary.key=your-secondary-cosmosDb-key
azure.cosmos.secondary.secondaryKey=your-secondary-cosmosDb-secondary-key
azure.cosmos.secondary.database=your-secondary-cosmosDb-dbName
azure.cosmos.secondary.populateQueryMetrics=if-populate-query-metrics
```

- The [Entity](https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/cosmos/azure-spring-data-cosmos-core#define-an-entity) and [Repository](https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/cosmos/azure-spring-data-cosmos-core#create-repositories) definition is similar as above. You can put different database entities into different packages.

- The `@EnableReactiveCosmosRepositories` or `@EnableCosmosRepositories` support user-define the cosmos template, use `reactiveCosmosTemplateRef` or `cosmosTemplateRef` to config the name of the `ReactiveCosmosTemplate` or `CosmosTemplate` bean to be used with the repositories detected.
- If you have multiple cosmos database accounts, you can define multiple `CosmosAsyncClient`. If the single cosmos account has multiple databases, you can use the same `CosmosAsyncClient` to initialize the cosmos template.
<!-- embedme src/samples/java/com/azure/cosmos/multidatasource/DatabaseConfiguration.java#L34-L131 -->

```java
@Configuration
@EnableCosmosAuditing
@PropertySource("classpath:application.properties")
public class DatabaseConfiguration extends AbstractCosmosConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseConfiguration.class);

    @Bean
    @ConfigurationProperties(prefix = "azure.cosmos.primary")
    public CosmosProperties primaryDataSourceConfiguration() {
        return new CosmosProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "azure.cosmos.secondary")
    public CosmosProperties secondaryDataSourceConfiguration() {
        return new CosmosProperties();
    }

    @Autowired
    @Qualifier("primaryDataSourceConfiguration")
    CosmosProperties primaryProperties;

    @Autowired
    @Qualifier("secondaryDataSourceConfiguration")
    CosmosProperties secondaryProperties;

    @Autowired(required = false)
    private IsNewAwareAuditingHandler cosmosAuditingHandler;

    @Bean
    public CosmosClientBuilder cosmosClientBuilder() {
        return new CosmosClientBuilder()
            .key(primaryProperties.getKey())
            .endpoint(primaryProperties.getUri());
    }

    @Bean
    public CosmosClientBuilder secondaryCosmosClientBuilder() {
        return new CosmosClientBuilder()
            .key(secondaryProperties.getKey())
            .endpoint(secondaryProperties.getUri());
    }
    // -------------------------First Cosmos Client for First Cosmos Account---------------------------
    @EnableReactiveCosmosRepositories(basePackages = "com.azure.cosmos.multidatasource.primarydatasource.first")
    public class PrimaryDataSourceConfiguration {

    }
    @EnableReactiveCosmosRepositories(basePackages = "com.azure.cosmos.multidatasource.primarydatasource.second", reactiveCosmosTemplateRef = "primaryReactiveCosmosTemplate")
    public class PrimaryDataSourceConfiguration2 {
        @Bean
        public ReactiveCosmosTemplate primaryReactiveCosmosTemplate(CosmosAsyncClient cosmosAsyncClient, CosmosConfig cosmosConfig, MappingCosmosConverter mappingCosmosConverter) {
            return new ReactiveCosmosTemplate(cosmosAsyncClient, "test1_2", cosmosConfig, mappingCosmosConverter, cosmosAuditingHandler);
        }
    }

    // -------------------------Second Cosmos Client for Secondary Cosmos Account---------------------------
    @Bean("secondaryCosmosAsyncClient")
    public CosmosAsyncClient getCosmosAsyncClient(CosmosClientBuilder secondaryCosmosClientBuilder) {
        return CosmosFactory.createCosmosAsyncClient(secondaryCosmosClientBuilder);
    }

    @Bean("secondaryCosmosConfig")
    public CosmosConfig getCosmosConfig() {
        return CosmosConfig.builder()
                           .enableQueryMetrics(true)
                           .responseDiagnosticsProcessor(new ResponseDiagnosticsProcessorImplementation())
                           .build();
    }

    @EnableReactiveCosmosRepositories(basePackages = "com.azure.cosmos.multidatasource.secondarydatasource.first", reactiveCosmosTemplateRef = "secondaryReactiveCosmosTemplate")
    public class SecondaryDataSourceConfiguration {
        @Bean
        public CosmosTemplate secondaryReactiveCosmosTemplate(@Qualifier("secondaryCosmosAsyncClient") CosmosAsyncClient client, @Qualifier("secondaryCosmosConfig") CosmosConfig cosmosConfig, MappingCosmosConverter mappingCosmosConverter) {
            return new CosmosTemplate(client, "test2_1", cosmosConfig, mappingCosmosConverter, cosmosAuditingHandler);
        }
    }
    @EnableReactiveCosmosRepositories(basePackages = "com.azure.cosmos.multidatasource.secondarydatasource.second", reactiveCosmosTemplateRef = "secondaryReactiveCosmosTemplate1")
    public class SecondaryDataSourceConfiguration1 {
        @Bean
        public CosmosTemplate secondaryReactiveCosmosTemplate1(@Qualifier("secondaryCosmosAsyncClient") CosmosAsyncClient client, @Qualifier("secondaryCosmosConfig") CosmosConfig cosmosConfig, MappingCosmosConverter mappingCosmosConverter) {
            return new CosmosTemplate(client, "test2_2", cosmosConfig, mappingCosmosConverter, cosmosAuditingHandler);
        }
    }

    private static class ResponseDiagnosticsProcessorImplementation implements ResponseDiagnosticsProcessor {

        @Override
        public void processResponseDiagnostics(@Nullable ResponseDiagnostics responseDiagnostics) {
            LOGGER.info("Response Diagnostics {}", responseDiagnostics);
        }
    }

    @Override
    protected String getDatabaseName() {
        return "test1_1";
    }
}
```

- In the above example, we have two cosmos account, each account has two databases. For each account, we can use the same Cosmos Client. You can create the `CosmosAsyncClient` like this:

<!-- embedme src/samples/java/com/azure/cosmos/multidatasource/DatabaseConfiguration.java#L91-L94 -->

```java
@Bean("secondaryCosmosAsyncClient")
public CosmosAsyncClient getCosmosAsyncClient(CosmosClientBuilder secondaryCosmosClientBuilder) {
    return CosmosFactory.createCosmosAsyncClient(secondaryCosmosClientBuilder);
}
```

- Besides, if you want to define `queryMetricsEnabled` or `ResponseDiagnosticsProcessor` , you can create the `CosmosConfig` for your cosmos template.

<!-- embedme src/samples/java/com/azure/cosmos/multidatasource/DatabaseConfiguration.java#L96-L102-->

```java
@Bean("secondaryCosmosConfig")
public CosmosConfig getCosmosConfig() {
    return CosmosConfig.builder()
                       .enableQueryMetrics(true)
                       .responseDiagnosticsProcessor(new ResponseDiagnosticsProcessorImplementation())
                       .build();
}
```

- Create an Application class

<!-- embedme src/samples/java/com/azure/cosmos/multidatasource/MultiDatasourceApplication.java#L23-L61 -->

```java
@SpringBootApplication
public class MultiDatasourceApplication implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;


    private final User user = new User("1024", "1024@geek.com", "1k", "Mars");
    private final Book book = new Book("9780792745488", "Zen and the Art of Motorcycle Maintenance", "Robert M. Pirsig");


    public static void main(String[] args) {
        SpringApplication.run(MultiDatasourceApplication.class, args);
    }

    @Override
    public void run(String... args) {
        final List<User> users = this.userRepository.findByEmailOrName(this.user.getEmail(), this.user.getName()).collectList().block();
        users.forEach(System.out::println);
        final Book book = this.bookRepository.findById("9780792745488").block();
        System.out.println(book);
    }

    @PostConstruct
    public void setup() {
        this.userRepository.save(user).block();
        this.bookRepository.save(book).block();

    }

    @PreDestroy
    public void cleanup() {
        this.userRepository.deleteAll().block();
        this.bookRepository.deleteAll().block();
    }
}
```

## Beta version package

Beta version built from `master` branch are available, you can refer to the [instruction](https://github.com/Azure/azure-sdk-for-java/blob/master/CONTRIBUTING.md#nightly-package-builds) to use beta version packages.

## Troubleshooting

### General

If you encounter any bug, please file an issue [here](https://github.com/Azure/azure-sdk-for-java/issues/new).

To suggest a new feature or changes that could be made, file an issue the same way you would for a bug.

### Enable Client Logging
- Azure-spring-data-cosmos uses SLF4j as the logging facade that supports logging into popular logging frameworks such as log4j and logback.
For example, if you want to use spring logback as logging framework, add the following xml to resources folder.

```xml
<configuration>
  <include resource="/org/springframework/boot/logging/logback/base.xml"/>
  <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
    <encoder>
      <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n
      </pattern>
    </encoder>
  </appender>
  <root level="info">
    <appender-ref ref="STDOUT"/>
  </root>
  <logger name="com.azure.cosmos" level="error"/>
  <logger name="org.springframework" level="error"/>
  <logger name="io.netty" level="error"/>
</configuration>
```

## Examples
- Please refer to [sample project here][samples].

## Next steps
- Read more about azure spring data cosmos [here][azure_spring_data_cosmos_docs].
- [Read more about Azure CosmosDB Service][cosmos_docs]

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a
[Contributor License Agreement (CLA)][cla] declaring that you have the right to, and actually do, grant us the rights
to use your contribution.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate
the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to
do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][coc]. For more information see the [Code of Conduct FAQ][coc_faq]
or contact [opencode@microsoft.com][coc_contact] with any additional questions or comments.

<!-- LINKS -->
[source_code]: src
[cosmos_introduction]: https://docs.microsoft.com/azure/cosmos-db/
[cosmos_docs]: https://docs.microsoft.com/azure/cosmos-db/introduction
[jdk]: https://docs.microsoft.com/java/azure/java-supported-jdk-runtime?view=azure-java-stable
[maven]: https://maven.apache.org/
[cla]: https://cla.microsoft.com
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com
[azure_subscription]: https://azure.microsoft.com/free/
[samples]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/cosmos/azure-spring-data-cosmos-core/src/samples/java/com/azure/cosmos
[sql_api_query]: https://docs.microsoft.com/azure/cosmos-db/sql-api-sql-query
[local_emulator]: https://docs.microsoft.com/azure/cosmos-db/local-emulator
[local_emulator_export_ssl_certificates]: https://docs.microsoft.com/azure/cosmos-db/local-emulator-export-ssl-certificates
[azure_spring_data_2_2_cosmos]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/cosmos/azure-spring-data-2-2-cosmos
[azure_spring_data_2_3_cosmos]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/cosmos/azure-spring-data-2-3-cosmos
[azure_spring_data_2_2_cosmos_readme]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/cosmos/azure-spring-data-2-2-cosmos/README.md
[azure_spring_data_2_3_cosmos_readme]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/cosmos/azure-spring-data-2-3-cosmos/README.md
[spring_data_commons_id_annotation]: https://github.com/spring-projects/spring-data-commons/blob/master/src/main/java/org/springframework/data/annotation/Id.java
[azure_cosmos_db_partition]: https://docs.microsoft.com/azure/cosmos-db/partition-data
[address_repository_it_test]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/cosmos/azure-spring-data-cosmos-test/src/test/java/com/azure/spring/data/cosmos/repository/integration/AddressRepositoryIT.java
[azure_spring_data_cosmos_docs]: https://docs.microsoft.com/azure/cosmos-db/sql-api-sdk-java-spring-v3
[spring_data_custom_query]: https://docs.spring.io/spring-data/commons/docs/current/reference/html/#repositories.query-methods.details

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fcosmos%2F%2Fazure-spring-data-cosmos-core%2FREADME.png)
