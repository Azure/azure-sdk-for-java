## Azure Cosmos DB Spring Boot Starter client library for Java

[Azure Cosmos DB](https://azure.microsoft.com/services/cosmos-db/) is a globally-distributed database service that allows developers to work with data using a variety of standard APIs, such as SQL, MongoDB, Graph, and Azure Table storage. 

## TOC

* [Key concepts](#key-concepts)
* [Examples](#examples)
* [Getting started](#getting-started)

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
- Supports [Azure Cosmos DB partition](https://docs.microsoft.com/azure/cosmos-db/partition-data). To specify a field of your domain class to be partition key field, just annotate it with `@PartitionKey`. When you do CRUD operation, please specify your partition value. For more sample on partition CRUD, please refer to [test here](./test/java/com/microsoft/azure/spring/data/cosmosdb/repository/AddressRepositoryIT.java)   
- Supports [Spring Data custom query](https://docs.spring.io/spring-data/commons/docs/current/reference/html/#repositories.query-methods.details) find operation.
- Supports [spring-boot-starter-data-rest](https://projects.spring.io/spring-data-rest/).
- Supports List and nested type in domain class.

## Examples
Please refer to [sample project here](../azure-spring-boot-samples/azure-spring-boot-sample-cosmosdb).

## Getting started

### Add the dependency

`azure-cosmosdb-spring-boot-starter` is published on Maven Central Repository.  
If you are using Maven, add the following dependency.  

[//]: # ({x-version-update-start;com.azure:azure-cosmosdb-spring-boot-starter;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-cosmosdb-spring-boot-starter</artifactId>
    <version>2.3.3-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Add the property setting

Open `application.properties` file and add below properties with your Cosmos DB credentials.

```properties
azure.cosmosdb.uri=your-cosmosdb-uri
azure.cosmosdb.key=your-cosmosdb-key
azure.cosmosdb.database=your-cosmosdb-databasename
```

Property `azure.cosmosdb.consistency-level` is also supported.

Property `azure.cosmosdb.cosmosKeyCredential` is also supported. CosmosKeyCredential feature provides capability to 
rotate keys on the fly. You can switch keys using switchToSecondaryKey(). For more information on this, see the Sample 
Application code.

### Define an entity
Define a simple entity as Document in Cosmos DB.
<!-- embedme ../azure-spring-boot/src/samples/java/com/azure/spring/cosmosdb/User.java#L10-L65 -->
```java
@Document(collection = "mycollection")
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

Annotation `@Document(collection="mycollection")` is used to specify the collection name of your document in Azure Cosmos DB.

### Create repositories
Extends ReactiveCosmosRepository interface, which provides Spring Data repository support.
<!-- embedme ../azure-spring-boot/src/samples/java/com/azure/spring/cosmosdb/UserRepository.java#L10-L14 -->
```java
@Repository
public interface UserRepository extends ReactiveCosmosRepository<User, String> {

    Flux<User> findByFirstName(String firstName);
}
```

So far ReactiveCosmosRepository provides basic save, delete and find operations. More operations will be supported later.

### Create an Application class
Here create an application class with all the components
<!-- embedme ../azure-spring-boot/src/samples/java/com/azure/spring/cosmosdb/CosmosSampleApplication.java#L18-L65 -->
```java
@SpringBootApplication
public class CosmosSampleApplication implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(CosmosSampleApplication.class);

    @Autowired
    private UserRepository repository;

    public static void main(String[] args) {
        SpringApplication.run(CosmosSampleApplication.class, args);
    }

    public void run(String... var1) {
        final User testUser = new User("testId", "testFirstName", "testLastName", "test address line one");

        // Save the User class to Azure CosmosDB database.
        final Mono<User> saveUserMono = repository.save(testUser);

        final Flux<User> firstNameUserFlux = repository.findByFirstName("testFirstName");

        //  Nothing happens until we subscribe to these Monos.
        //  findById will not return the user as user is not present.
        final Mono<User> findByIdMono = repository.findById(testUser.getId());
        final User findByIdUser = findByIdMono.block();
        Assert.isNull(findByIdUser, "User must be null");

        final User savedUser = saveUserMono.block();
        Assert.state(savedUser != null, "Saved user must not be null");
        Assert.state(savedUser.getFirstName().equals(testUser.getFirstName()), "Saved user first name doesn't match");

        firstNameUserFlux.collectList().block();

        final Optional<User> optionalUserResult = repository.findById(testUser.getId()).blockOptional();
        Assert.isTrue(optionalUserResult.isPresent(), "Cannot find user.");

        final User result = optionalUserResult.get();
        Assert.state(result.getFirstName().equals(testUser.getFirstName()), "query result firstName doesn't match!");
        Assert.state(result.getLastName().equals(testUser.getLastName()), "query result lastName doesn't match!");

        LOGGER.info("findOne in User collection get result: {}", result.toString());
    }

    @PostConstruct
    public void setup() {
        // For this example, remove all of the existing records.
        this.repository.deleteAll().block();
    }
}
```
Autowired UserRepository interface, then can do save, delete and find operations.

### Allow telemetry
Microsoft would like to collect data about how users use this Spring boot starter. Microsoft uses this information to improve our tooling experience. Participation is voluntary. If you don't want to participate, just simply disable it by setting below configuration in `application.properties`.
```properties
azure.cosmosdb.allow-telemetry=false
```
When telemetry is enabled, an HTTP request will be sent to URL `https://dc.services.visualstudio.com/v2/track`. So please make sure it's not blocked by your firewall.  
Find more information about Azure Service Privacy Statement, please check [Microsoft Online Services Privacy Statement](https://www.microsoft.com/privacystatement/OnlineServices/Default.aspx). 

## Troubleshooting

## Next steps

Besides using this Azure CosmosDb Spring Boot Starter, you can directly use Spring Data for Azure CosmosDb package for more complex scenarios. Please refer to [Spring Data for Azure CosmosDB](https://github.com/Microsoft/spring-data-cosmosdb) for more details.

## Contributing
