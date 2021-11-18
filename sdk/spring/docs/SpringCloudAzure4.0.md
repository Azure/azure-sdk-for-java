# Spring Cloud Azure 4.0



[TOC]



## Preface

Spring Cloud Azure offers a convenient way to interact with **Azure** provided services using well-known Spring idioms and APIs for Spring developers.

## What's new?

### What's New in 4.0 since 3.10.x

This section covers the changes made from version 3.10 to version 4.0.0-beta.1. 

#### Project Name Changes

There has never been a consistent or official name to call all the Spring Cloud Azure libraries, some of them were called `Azure Spring Boot` and some of them `Spring on Azure` , and all these names will make developer confused. Since 4.0, we began to use the project name `Spring Cloud Azure` to represent all the Azure Spring libraries.

#### Artifact Name Changes

Group ids are the same for modern and legacy Spring Cloud Azure libraries, they are all `com.azure.spring`. Artifact ids for the modern Spring Cloud Azure libraries have changed. And according to which Spring project it belongs, Spring Boot, Spring Integration or Spring Cloud Stream, the artifact ids pattern could be `spring-cloud-azure-starter-[service]` , `spring-integration-azure-[service]` and `spring-cloud-azure-stream-binder-[service]`. The legacy starters each has an artifact id following the pattern `azure-spring-*`. This provides a quick and accessible means to help understand, at a glance, whether you are using modern or legacy starters.

In the process of developing Spring Cloud Azure 4.0, we renamed some artifacts to make them follow the new naming conventions, deleted some artifacts for its functionality could be put in a more appropriate artifact, and added some new artifacts to better serve some scenarios.

#### BOM Changes

We used to ship two BOMs for our libraries, the `azure-spring-boot-bom` and `azure-spring-cloud-dependencies`, but we combined these two BOMs into one BOM since 4.0, the `spring-cloud-azure-dependencies`. Please add an entry in the dependencyManagement of your project to benefit from the dependency management.

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
        <groupId>com.azure.spring</groupId>
        <artifactId>spring-cloud-azure-dependencies</artifactId>
        <version>4.0.0-beta.1</version>
        <type>pom</type>
        <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```

#### Pakcage Changes

#### Configuration Properties Changes

##### Configuration Properties Prefix Unified

The configuration properties' prefixes have been unified to `spring.cloud.azure` namespace since Spring Cloud Azure 4.0, it will make configuration proeprties more consistent and configuring it in a more intuitive way. Here's a quick review of the prefixes of supported Azure services.

| Azure Service               | Configuration Property Prefix           |
| --------------------------- | --------------------------------------- |
| Azure App Configuration     | spring.cloud.azure.appconfiguration     |
| Azure Cosmos DB             | spring.cloud.azure.cosmos               |
| Azure Event Hubs            | spring.cloud.azure.eventhubs            |
| Azure Key Vault Certificate | spring.cloud.azure.keyvault.certificate |
| Azure Key Vault Secret      | spring.cloud.azure.keyvault.secret      |
| Azure Service Bus           | spring.cloud.azure.servicebus           |
| Azure Storage Blob          | spring.cloud.azure.storage.blob         |
| Azure Storage File Share    | spring.cloud.azure.storage.fileshare    |
| Azure Storage Queue         | spring.cloud.azure.storage.queue        |

##### Common Configuration Properties Categorized

Most of Azure Service SDKs could divided into two categories by transport type, HTTP-based and AMQP-based. There're properties that are common to all SDKs such as authentication principals and Azure environment settings. Or common to all HTTP-based clients, such as setting logging level to log http requests and responses. In Spring Cloud Azure 4.0 we added five common categories of configuration properties, which could be specified to each Azure service.

| Prefix                                           | Description                                                  |
| ------------------------------------------------ | ------------------------------------------------------------ |
| spring.cloud.azure.*\<azure-service>*.client     | To configure the transport clients underneath one Azure service SDK. |
| spring.cloud.azure.*\<azure-service>*.credential | To configure how to authenticate with Azure Active Directory for one Azure service SDK.. |
| spring.cloud.azure.*\<azure-service>*.profile    | To configure the Azure cloud environment one Azure service SDK. |
| spring.cloud.azure.*\<azure-service>*.proxy      | To configure the proxy options for one Azure service SDK.    |
| spring.cloud.azure.*\<azure-service>*.retry      | To configure the retry options apply to one Azure service SDK. |

##### Global Configuration Properties Added

There're some properties that could be shared among different Azure services, for example using the same service principal to access Azure Cosmos DB and Azure Event Hubs. Spring Cloud Azure 4.0 allows developers to define properties that apply to all Azure SDKs in the namespace `spring.cloud.azure`. 

| Prefix                        | Description                                                  |
| ----------------------------- | ------------------------------------------------------------ |
| spring.cloud.azure.client     | To configure the transport clients apply to all Azure SDKs by default. |
| spring.cloud.azure.credential | To configure how to authenticate with Azure Active Directory for all Azure SDKs by default. |
| spring.cloud.azure.profile    | To configure the Azure cloud environment for all Azure SDKs by default. |
| spring.cloud.azure.proxy      | To configure the proxy options apply to all Azure SDK clients by default. |
| spring.cloud.azure.retry      | To configure the retry options apply to all Azure SDK clients by default. |

Please note that propeties configured under each Azure service will override the global configurations.

##### Full Set of SDK Configurations Added

Now it's possible to configure the Azure SDK client's properties via configuration properties. 

##### Azure SDK Predefined Environment Variables Supported

There're a set of environment variables defined by Azure Core and Azure SDKs, now configuring these environment variables will also be effective in Spring Cloud Azure 4.0. 

#### Authentication Changes

##### All Authentication Methods Supported

Azure services use a variety of different authentication schemes to allow clients to access the service, such as authenticating with Azure Active Directory using service principal or user principal, authenticating using SAS token, etc. In Spring Cloud Azure 4.0 we supported all authentication methods for each Azure SDK client, just configuring corresponding properties will work.

##### Managed Identity Supported

Some of our libraries supported managed identity but not all of them. Since Spring Cloud Azure 4.0 they are all supporting it if the SDK clients themselves support it.

##### Default Token Credential Auto-Configured

When developing locally in an environment which could already have some credentials stored somewhere, like Azure CLI, Visual Studio Code, or Intellij IDEA, it would be more convenient if the application could pick up these credentials than configuring again in the application. In Spring Cloud Azure 4.0, a default token credential will be auto-configured and can leverage credentials stored in your environment.

##### Finer Control over Authenticating with Azure Resources Supported

For Azure services like Event Hubs and Service Bus, it's a common requirement to configure different access policies for differnt event hubs or service bus queues or topics. Since Spring Cloud Azure 4.0, the event hub level or service bus queue level configuration has been supported.

#### Auto-Configuration Changes

##### Auto-Configuration for Raw SDK Clients Added

Since Spring Cloud Azure 4.0, all supported Azure SDK raw clients will be auto-configured if corresponding properties configured. To be specific, a client builder, a synchronous cient and an ayschronous client will be auto-configured at the same time. Auto-configuration is non-invasive. At any point, you can start to define your own configuration to replace specific parts of the auto-configuration. For example, if you add your own `SecretClient` bean, the default secret client backs away.

##### Auto-Configuration for Azure Event Hubs Producers and Consumers Separately Supported 

There're cases when an application consumes events from one event hub, processes and then send the processed events to another one, and in such cases the two event hubs could be in different Event Hubs namespaces. So it's important if we can configure the producer and the consumer separately. Since Spring Cloud Azure 4.0, such configuration has been supported. 

##### Auto-Configuration for Azure Service Bus Producers and Consumers Separately Supported

There're cases when an application consumes messages from one Service Bus queue, processes and then send the processed messages to another one, and in such cases the two queues could be in different Service Bus namespaces. So it's important if we can configure the producer and the consumer separately. Since Spring Cloud Azure 4.0, such configuration has been supported. 

##### Checkpoint Store Not Required When Using Azure Event Hubs

The checkpoint store was required to configure when using Azure Event Hubs, even if it was only used as a producer. Since Spring Cloud Azure 4.0, the checkpoint store will only be required when you uses it as a processor and without providing your own checkpoint store implementation.

#### Dependency Changes

##### Azure Resource Manager Made as Optional

The Azure Resource Manager was added as a dependency in some starters, but now it's marked as optional. You can include it now only when you want to operate Azure resources or retrieve resource information with Azure Resource Manager. 

##### Unnecessary Dependencies Removed

Some of the dependencies were added accidentally to our libraries, but now they are removed. Please make sure add the removed dependencies manually to your project to prevent unintentionally crash.

#### Production Ready Changes

##### Spring Cloud Azure Actuator Added

A new artifact called spring-cloud-azure-actuator was added to support the production ready features, such as health indicators. 

##### Tracing with Sleuth Added

A new artifact called spring-cloud-azure-trace-sleuth was added to support integrating HTTP-based clients tracing with Spring Cloud Sleuth project.

#### Storage Resource Changes

##### Content Type Supported

// todo



#### Spring Integration Azure Changes

// todo

#### Spring Cloud Azure Stream Binder Changes

##### Service Bus Topic Binder and Queue Binder Merged

There were two binder types for Azure Service Bus, servicebus-queue and servicebus-topic, now they have been merged into one. Instead of including two binder libraries into your application, specifying the Service Bus entity type, Queue or Topic, in configuration files will work.

##### Full Set of Configurations of Binding Level Supported

For Service Bus and Event Hubs binder, it's now possible to configure the client at binding level.









## Introduction

This first part of the reference documentation is a high-level overview of Spring Cloud Azure and the underlying concepts and some code snippets that can help you get up and running as quickly as possible.

### Quick Tour



#### Compatibility

This quick tour works with the following versions:

- Spring Cloud Azure 4.0.0-beta.1
- Spring Boot 2.5.4+
- Minimum Java version: 8

#### Getting Started

##### Add BOM

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
        <groupId>com.azure.spring</groupId>
        <artifactId>spring-cloud-azure-dependencies</artifactId>
        <version>4.0.0-beta.1</version>
        <type>pom</type>
        <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```

#####  

## Reference

### Use MSI / Managed identities

#### Azure Spring Cloud Service 

Azure Spring Cloud Service supports system-assigned managed identity only at present. To use it for Azure Spring Cloud apps, add the below properties:

```
spring.cloud.azure.credential.managed-
```

#### App Services

To use managed identities for App Services - please refer to[How to use managed identities for App Service and Azure Functions].

To use it in an App Service, add the below properties:

```
azure.keyvault.enabled=true
azure.keyvault.uri=put-your-azure-keyvault-uri-here
```

#### VM

To use it for virtual machines, please refer to
[Azure AD managed identities for Azure resources documentation].

To use it in a VM, add the below properties:

```
azure.keyvault.enabled=true
azure.keyvault.uri=put-your-azure-keyvault-uri-here
azure.keyvault.client-id=put-your-azure-client-id-here
```

If you are using system assigned identity, you don't need to specify the client-id.

### Spring Cloud Azure Starter Data Cosmos DB

#### Key Concepts

- Spring Data ReactiveCrudRepository basic CRUD functionality
  - save
  - findAll
  - findOne by Id
  - deleteAll
  - delete by Id
  - delete entity
- Spring Data `@Id` annotation. There're 2 ways to map a field in domain class to `id` of Azure Cosmos DB document.
  - annotate a field in domain class with @Id, this field will be mapped to document `id` in Cosmos DB.
  - set name of this field to `id`, this field will be mapped to document `id` in Cosmos DB. [Note] if both way applied,
- Custom collection Name. By default, collection name will be class name of user domain class. To customize it, add annotation `@Document(collection="myCustomCollectionName")` to your domain class, that's all.
- Supports [Azure Cosmos DB partition](https://docs.microsoft.com/azure/cosmos-db/partition-data). To specify a field of your domain class to be partition key field, just annotate it with `@PartitionKey`. When you do CRUD operation, please specify your partition value. For more sample on partition CRUD, please refer to [test here](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/cosmos/azure-spring-data-cosmos-test/src/test/java/com/azure/spring/data/cosmos/repository/integration/AddressRepositoryIT.java)
- Supports [Spring Data custom query](https://docs.spring.io/spring-data/commons/docs/current/reference/html/#repositories.query-methods.details) find operation.
- Supports [spring-boot-starter-data-rest](https://projects.spring.io/spring-data-rest/).
- Supports List and nested type in domain class.

#### Examples

##### Add the property setting

Open `application.yaml` file and add below properties with your Cosmos DB credentials.

```yml
spring:
  cloud:
    azure:
      cosmos:
        endpoint: your-cosmos-endpoint
        key: your-cosmos-key
        database: your-cosmos-databasename
        populateQueryMetrics: true
				consistencyLevel: EVENTUAL
				secondary-key=put-your-cosmos-secondary-key-here // todo

```



AzureKeyCredential feature provides capability to rotate keys on the fly. You can use `AzureKeyCredential.update()` methods to rotate key. For more information on this, see the [Sample Application](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/cosmos/azure-spring-data-cosmos/src/samples/java/com/azure/spring/data/cosmos/SampleApplication.java) code.

##### (Optional) Add Spring Boot Actuator

If you choose to add Spring Boot Actuator for Cosmos DB, add `management.health.azure-cosmos.enabled=true` to application.yaml.

```yml
management.health.azure-cosmos.enabled: true
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

##### Define an entity

Define a simple entity as Document in Cosmos DB.

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

##### Create repositories

Extends ReactiveCosmosRepository interface, which provides Spring Data repository support.

```java
@Repository
public interface UserRepository extends ReactiveCosmosRepository<User, String> {

    Flux<User> findByFirstName(String firstName);
}
```

So far ReactiveCosmosRepository provides basic save, delete and find operations. More operations will be supported later.

##### Create an Application class

Here create an application class with all the components

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

### Spring Cloud Azure Starter Key Vault Secret

#### Key concepts

By adding [PropertySource] in [ConfigurableEnvironment], values saved in [Azure Key Vault Secrets]
can be resolved in `${...}` placeholder in `@Value` annotation.

#### Prerequisites

##### Include the package
```xml
<dependency>
  <groupId>com.azure.spring</groupId>
  <artifactId>spring-cloud-azure-starter-keyvault-secrets</artifactId>
</dependency>
```

##### Save secrets in Azure Key Vault
Save secrets in Azure Key Vault through Azure Portal or Azure CLI:
- [Set and retrieve a secret from Azure Key Vault using Azure CLI]. // todo
- [Set and retrieve a secret from Azure Key Vault using the Azure portal] // todo

#####  Configure necessary properties.
Configure these properties:
```yml
spring:
  cloud:
    azure:
      keyvault:
        secret:
          endpoint: put-your-azure-keyvault-endpoint-here 
          property-source-enabled: true
					credential:
					  client-id: put-your-azure-client-id-here
            client-secret: put-your-azure-client-secret-here
```

#####  Get Key Vault secret value as property
Now, you can get Azure Key Vault secret value as a configuration property.

<!-- embedme ../azure-spring-boot/src/samples/java/com/azure/spring/keyvault/KeyVaultSample.java#L18-L32 -->

```java
@SpringBootApplication
public class KeyVaultSample implements CommandLineRunner {

    @Value("${your-property-name}")
    private String mySecretProperty;

    public static void main(String[] args) {
        SpringApplication.run(KeyVaultSample.class, args);
    }

    @Override
    public void run(String... args) {
        System.out.println("property your-property-name value is: " + mySecretProperty);
    }
}
```

You can refer to [Key Vault Secrets Sample project] to get more information.



## Examples



### Use multiple Key Vault in one application

If you want to use multiple Key Vaults in one project, you need to define names for each of the
Key Vaults you want to use and in which order the Key Vaults should be consulted. If a property
exists in multiple Key Vaults, the order determines which value you will get back.

The example below shows a setup for 2 Key Vaults, named `keyvault1` and
`keyvault2`. The order specifies that `keyvault1` will be consulted first.

```
azure.keyvault.order=keyvault1,keyvault2
azure.keyvault.keyvault1.uri=put-a-azure-keyvault-uri-here
azure.keyvault.keyvault1.client-id=put-a-azure-client-id-here
azure.keyvault.keyvault1.client-key=put-a-azure-client-key-here
azure.keyvault.keyvault1.tenant-id=put-a-azure-tenant-id-here
azure.keyvault.keyvault2.uri=put-a-azure-keyvault-uri-here
azure.keyvault.keyvault2.client-id=put-a-azure-client-id-here
azure.keyvault.keyvault2.client-key=put-a-azure-client-key-here
azure.keyvault.keyvault2.tenant-id=put-a-azure-tenant-id-here
```
Note if you decide to use multiple Key Vault support, and you already have an
existing configuration, please make sure you migrate that configuration to the
multiple Key Vault variant. Mixing multiple Key Vaults with an existing single
Key Vault configuration is a non-supported scenario.

### Case-sensitive key mode

The new case-sensitive mode allows you to use case-sensitive Key Vault names. Note
that the Key Vault secret key still needs to honor the naming limitation as
described [Vault-name and Object-name].

To enable case-sensitive mode, you can set the following property in the `application.properties`:
```
azure.keyvault.case-sensitive-keys=true
```

If your Spring property is using a name that does not honor the Key Vault secret key limitation,
use placeholders in properties. An example of using a placeholder:
```
my.not.compliant.property=${myCompliantKeyVaultSecret}
```

The application will take care of getting the value that is backed by the 
`myCompliantKeyVaultSecret` key name and assign its value to the non-compliant
`my.not.compliant.property`.

### Handle special property name

Allowed secret name pattern in Azure Key Vault is `^[0-9a-zA-Z-]+$`. This section tells how to
handle special names.
 - When property name contains `.`

   `.` is not supported in secret name. If your application have property name which contain `.`,
like `spring.datasource.url`, just replace `.` to `-` when save secret in Azure Key Vault.
For example: Save `spring-datasource-url` in Azure Key Vault. In your application, you can still
use `spring.datasource.url` to retrieve property value.

 - Use [Property Placeholders] as a workaround.

### Custom settings
To use the custom configuration, open the `application.properties` file and add below properties to
specify your Azure Key Vault URI, Azure service principal client id and client key.
- `azure.keyvault.enabled` is used to turn on/off Azure Key Vault Secret as a Spring Boot property
  source, the default value is true.
- `azure.keyvault.token-acquiring-timeout-seconds` is optional. Its value is used to specify the
  timeout in seconds when acquiring a token from Azure AAD, the default value is 60 seconds.
- `azure.keyvault.refresh-interval` is optional. Its value is used to specify the period for
  PropertySource to refresh secret keys, the default value is 1800000(ms).
- `azure.keyvault.secret-keys` is used to indicate that if an application using specific secret keys
  and this property is set, the application will only load the keys in the property and won't load
  all the keys from Key Vault, that means if you want to update your secrets, you need to restart
  the application rather than only add secrets in the Key Vault.
- `azure.keyvault.authority-host` is the URL at which your identity provider can be reached.
    - If working with azure global, just left the property blank, and the value will be filled with
      the default value.
    - If working with azure stack, set the property with authority URI.
- `azure.keyvault.secret-service-version`
    - The valid values for this property can be found [SecretServiceVersion].
    - This property is optional. If not set, the property will be filled with the latest value.

```
azure.keyvault.enabled=true
azure.keyvault.uri=put-your-azure-keyvault-uri-here
azure.keyvault.client-id=put-your-azure-client-id-here
azure.keyvault.client-key=put-your-azure-client-key-here
azure.keyvault.tenant-id=put-your-azure-tenant-id-here
azure.keyvault.token-acquire-timeout-seconds=60
azure.keyvault.refresh-interval=1800000
azure.keyvault.secret-keys=key1,key2,key3
azure.keyvault.authority-host=put-your-own-authority-host-here(fill with default value if empty)
azure.keyvault.secret-service-version=specify secretServiceVersion value(fill with default value if empty)
```

## Known Issues

