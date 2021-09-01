# Azure Cosmos DB Spring Boot Starter client library for Java

[Azure Cosmos DB](https://azure.microsoft.com/services/cosmos-db/) is a globally-distributed database service that allows developers to work with data using a variety of standard APIs, such as SQL, MongoDB, Graph, and Azure Table storage. 

[Package (Maven)][package] | [API reference documentation][refdocs] | [Product documentation][docs] | [Samples][sample]

## Getting started

### Prerequisites
- [Environment checklist][environment_checklist]

### Include the package
1. [Add azure-spring-boot-bom].
1. Add dependency. `<version>` can be skipped because we already add `azure-spring-boot-bom`.
```xml
<dependency>
  <groupId>com.azure.spring</groupId>
  <artifactId>azure-spring-boot-starter-cosmos</artifactId>
</dependency>
```

## Key concepts
- Spring Data ReactiveCrudRepository basic CRUD functionality
    - save
    - findAll
    - findOne by Id
    - deleteAll
    - delete by Id
    - delete entity
- Spring Data [@Id](https://github.com/spring-projects/spring-data-commons/blob/db62390de90c93a78743c97cc2cc9ccd964994a5/src/main/java/org/springframework/data/annotation/Id.java) annotation.
  There're 2 ways to map a field in domain class to `id` of Azure Cosmos DB document.
  - annotate a field in domain class with @Id, this field will be mapped to document `id` in Cosmos DB. 
  - set name of this field to `id`, this field will be mapped to document `id` in Cosmos DB.
    [Note] if both way applied,    
- Custom collection Name.
   By default, collection name will be class name of user domain class. To customize it, add annotation `@Document(collection="myCustomCollectionName")` to your domain class, that's all.
- Supports [Azure Cosmos DB partition](https://docs.microsoft.com/azure/cosmos-db/partition-data). To specify a field of your domain class to be partition key field, just annotate it with `@PartitionKey`. When you do CRUD operation, please specify your partition value. For more sample on partition CRUD, please refer to [test here](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/cosmos/azure-spring-data-cosmos-test/src/test/java/com/azure/spring/data/cosmos/repository/integration/AddressRepositoryIT.java)
- Supports [Spring Data custom query](https://docs.spring.io/spring-data/commons/docs/current/reference/html/#repositories.query-methods.details) find operation.
- Supports [spring-boot-starter-data-rest](https://projects.spring.io/spring-data-rest/).
- Supports List and nested type in domain class.

## Examples
### Add the property setting

Open `application.properties` file and add below properties with your Cosmos DB credentials.

```properties
azure.cosmos.uri=your-cosmos-uri
azure.cosmos.key=your-cosmos-key
azure.cosmos.database=your-cosmos-databasename
azure.cosmos.populateQueryMetrics=true
secondary-key=put-your-cosmos-secondary-key-here
```

Property `azure.cosmos.consistency-level` is also supported.

AzureKeyCredential feature provides capability to rotate keys on the fly. You can use `AzureKeyCredential.update()` methods to rotate key. For more information on this, see the [Sample Application][sample_cosmos_switch_key] code.

#### (Optional) Add Spring Boot Actuator
If you choose to add Spring Boot Actuator for Cosmos DB, add `management.health.azure-cosmos.enabled=true` to application.properties.
```properties
management.health.azure-cosmos.enabled=true
```
Include actuator dependencies.
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

Call `http://{hostname}:{port}/actuator/health/cosmos` to get the Cosmos DB health info. **Please note**: it will calculate [RUs](https://docs.microsoft.com/azure/cosmos-db/request-units).

### Define an entity
Define a simple entity as Document in Cosmos DB.
<!-- embedme ../azure-spring-boot/src/samples/java/com/azure/spring/cosmos/User.java#L10-L65 -->
```java
@Container(containerName = "mycollection")
public class User {
    @Id
    private String id;
    private String firstName;
    @PartitionKey
    private String lastName;
    private String address;

    public User() {
    }

    public User(String id, String firstName, String lastName, String address) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return String.format("%s %s, %s", firstName, lastName, address);
    }
}
```
`id` field will be used as document `id` in Azure Cosmos DB. Or you can annotate any field with `@Id` to map it to document `id`.

Annotation `@Container(containerName = "mycollection")` is used to specify the collection name of your document in Azure Cosmos DB.

### Create repositories
Extends ReactiveCosmosRepository interface, which provides Spring Data repository support.
<!-- embedme ../azure-spring-boot/src/samples/java/com/azure/spring/cosmos/UserRepository.java#L10-L14 -->
```java
@Repository
public interface UserRepository extends ReactiveCosmosRepository<User, String> {

    Flux<User> findByFirstName(String firstName);
}
```

So far ReactiveCosmosRepository provides basic save, delete and find operations. More operations will be supported later.

### Create an Application class
Here create an application class with all the components
<!-- embedme ../azure-spring-boot/src/samples/java/com/azure/spring/cosmos/CosmosSampleApplication.java#L21-L116 -->
```java
@SpringBootApplication
public class CosmosSampleApplication implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(CosmosSampleApplication.class);

    @Autowired
    private UserRepository repository;

    @Autowired
    private AzureKeyCredential azureKeyCredential;

    @Autowired
    private CosmosProperties properties;

    /**
     * The secondaryKey is used to rotate key for authorizing request.
     */
    @Value("${secondary-key}")
    private String secondaryKey;

    public static void main(String[] args) {
        SpringApplication.run(CosmosSampleApplication.class, args);
    }

    public void run(String... var1) {
        final User testUser = new User("testId", "testFirstName",
            "testLastName", "test address line one");

        // Save the User class to Azure Cosmos DB database.
        final Mono<User> saveUserMono = repository.save(testUser);

        final Flux<User> firstNameUserFlux = repository.findByFirstName("testFirstName");

        //  Nothing happens until we subscribe to these Monos.
        //  findById will not return the user as user is not present.
        final Mono<User> findByIdMono = repository.findById(testUser.getId());
        final User findByIdUser = findByIdMono.block();
        Assert.isNull(findByIdUser, "User must be null");

        final User savedUser = saveUserMono.block();
        Assert.state(savedUser != null, "Saved user must not be null");
        Assert.state(savedUser.getFirstName().equals(testUser.getFirstName()),
            "Saved user first name doesn't match");

        firstNameUserFlux.collectList().block();

        final Optional<User> optionalUserResult = repository.findById(testUser.getId()).blockOptional();
        Assert.isTrue(optionalUserResult.isPresent(), "Cannot find user.");

        final User result = optionalUserResult.get();
        Assert.state(result.getFirstName().equals(testUser.getFirstName()),
            "query result firstName doesn't match!");
        Assert.state(result.getLastName().equals(testUser.getLastName()),
            "query result lastName doesn't match!");
        LOGGER.info("findOne in User collection get result: {}", result.toString());

        switchKey();
    }

    /**
     * Switch cosmos authorization key
     */
    private void switchKey() {
        azureKeyCredential.update(secondaryKey);
        LOGGER.info("Switch to secondary key.");

        final User testUserUpdated = new User("testIdUpdated", "testFirstNameUpdated",
            "testLastNameUpdated", "test address Updated line one");
        final User saveUserUpdated = repository.save(testUserUpdated).block();
        Assert.state(saveUserUpdated != null, "Saved updated user must not be null");
        Assert.state(saveUserUpdated.getFirstName().equals(testUserUpdated.getFirstName()),
            "Saved updated user first name doesn't match");

        final Optional<User> optionalUserUpdatedResult = repository.findById(testUserUpdated.getId()).blockOptional();
        Assert.isTrue(optionalUserUpdatedResult.isPresent(), "Cannot find updated user.");
        final User updatedResult = optionalUserUpdatedResult.get();
        Assert.state(updatedResult.getFirstName().equals(testUserUpdated.getFirstName()),
            "query updated result firstName doesn't match!");
        Assert.state(updatedResult.getLastName().equals(testUserUpdated.getLastName()),
            "query updated result lastName doesn't match!");

        azureKeyCredential.update(properties.getKey());
        LOGGER.info("Switch back to key.");
        final Optional<User> userOptional = repository.findById(testUserUpdated.getId()).blockOptional();
        Assert.isTrue(userOptional.isPresent(), "Cannot find updated user.");
        Assert.state(updatedResult.getFirstName().equals(testUserUpdated.getFirstName()),
            "query updated result firstName doesn't match!");
        LOGGER.info("Finished key switch.");
    }

    @PostConstruct
    public void setup() {
        // For this example, remove all of the existing records.
        this.repository.deleteAll().block();
    }
}
```
Autowired UserRepository interface, then can do save, delete and find operations.

## Troubleshooting
### Enable client logging
Azure SDKs for Java offer a consistent logging story to help aid in troubleshooting application errors and expedite their resolution. The logs produced will capture the flow of an application before reaching the terminal state to help locate the root issue. View the [logging][logging] doc for guidance about enabling logging.

### Enable Spring logging
Spring allow all the supported logging systems to set logger levels set in the Spring Environment (for example, in application.properties) by using `logging.level.<logger-name>=<level>` where level is one of TRACE, DEBUG, INFO, WARN, ERROR, FATAL, or OFF. The root logger can be configured by using logging.level.root.

The following example shows potential logging settings in `application.properties`:

```properties
logging.level.root=WARN
logging.level.org.springframework.web=DEBUG
logging.level.org.hibernate=ERROR
```

For more information about setting loging in pring, please refer to the [official doc](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#boot-features-logging).
 

## Next steps

Besides using this Azure Cosmos DB Spring Boot Starter, you can directly use Spring Data for Azure Cosmos DB package for more complex scenarios. Please refer to [Spring Data for Azure Cosmos DB](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/cosmos/azure-spring-data-cosmos) for more details.

The following section provide a sample project illustrating how to use the starter.
### More sample code
- [Cosmos DB SQL API](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/tag_azure-spring-boot_3.6.0/cosmos/azure-spring-boot-sample-cosmos)

## Contributing
This project welcomes contributions and suggestions.  Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

Please follow [instructions here](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/CONTRIBUTING.md) to build from source or contribute.

<!-- LINKS -->
[docs]: https://docs.microsoft.com/azure/developer/java/spring-framework/configure-spring-boot-starter-java-app-with-cosmos-db
[refdocs]: https://azure.github.io/azure-sdk-for-java/springboot.html#azure-spring-boot
[package]: https://mvnrepository.com/artifact/com.azure.spring/azure-spring-boot-starter-cosmos
[sample]: https://github.com/Azure-Samples/azure-spring-boot-samples/tree/tag_azure-spring-boot_3.6.0/cosmos/azure-spring-boot-sample-cosmos
[logging]: https://docs.microsoft.com/en-us/azure/developer/java/sdk/logging-overview
[sample_cosmos_switch_key]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/cosmos/azure-spring-data-cosmos/src/samples/java/com/azure/spring/data/cosmos/SampleApplication.java
[environment_checklist]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/ENVIRONMENT_CHECKLIST.md#ready-to-run-checklist
[Add azure-spring-boot-bom]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/AZURE_SPRING_BOMS_USAGE.md#add-azure-spring-boot-bom
