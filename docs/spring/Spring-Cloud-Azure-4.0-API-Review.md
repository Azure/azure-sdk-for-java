- [Why do we need spring-cloud-azure-core, spring-cloud-azure-service, and spring-cloud-azure-resourcemanager](#why-do-we-need-spring-cloud-azure-core-spring-cloud-azure-service-and-spring-cloud-azure-resourcemanager)
  * [Why do we need spring-cloud-azure-core, spring-cloud-azure-service](#why-do-we-need-spring-cloud-azure-core-spring-cloud-azure-service)
  * [Why do we need those ClientBuilderFactories](#why-do-we-need-those-clientbuilderfactories)
  * [Why spring-cloud-azure-core depends on `storage-blob` and `storage-file-share`](#why-spring-cloud-azure-core-depends-on-storage-blob-and-storage-file-share)
  * [What does spring-cloud-azure-resourcemanager provide](#what-does-spring-cloud-azure-resourcemanager-provide)
- [spring-cloud-azure-core](#spring-cloud-azure-core)
  * [Realistic app example](#realistic-app-example)
  * [Config file example](#config-file-example)
  * [Dependencies](#dependencies)
  * [Value it provides (over plain Azure SDK usage)](#value-it-provides-over-plain-azure-sdk-usage)
  * [API](#api)
  * [Misc](#misc)
    + [Perf and scalability](#perf-and-scalability)
- [spring-cloud-azure-service](#spring-cloud-azure-service)
  * [Realistic app example](#realistic-app-example-1)
  * [Config file example](#config-file-example-1)
  * [Dependencies](#dependencies-1)
  * [Value it provides (over plain Azure SDK usage)](#value-it-provides-over-plain-azure-sdk-usage-1)
  * [API](#api-1)
  * [Misc](#misc-1)
    + [Perf and scalability](#perf-and-scalability-1)
- [spring-cloud-azure-resourcemanager](#spring-cloud-azure-resourcemanager)
  * [Realistic app example](#realistic-app-example-2)
  * [Config file example](#config-file-example-2)
  * [Dependencies](#dependencies-2)
  * [Value it provides (over plain Azure SDK usage)](#value-it-provides-over-plain-azure-sdk-usage-2)
  * [API](#api-2)
  * [Misc](#misc-2)
    + [Perf and scalability](#perf-and-scalability-2)


## Why do we need spring-cloud-azure-core, spring-cloud-azure-service, and spring-cloud-azure-resourcemanager
### Why do we need spring-cloud-azure-core, spring-cloud-azure-service
There are multiple Spring projects/abstractions we are implementing, such as Spring Data, Spring Boot, Spring Integration, Spring Cloud Stream and etc.  In 3.x we don't have a `spring-cloud-azure-core`, `spring-cloud-azure-service`, or `spring-cloud-azure-resourcemanager` artifact. Version 3.x has multiple issues, such as: 
- Not supporting all the options an Azure SDK client offers.
- Not supporting all the authentication methods an Azure SDK client supports.
- Doesn't support the `DefaultAzureCredential`.
- ...

Take [how we auto-configure](https://github.com/Azure/azure-sdk-for-java/blob/96c98c3cb11c64f8e6b66446f440afbf11d8b167/sdk/spring/azure-spring-boot/src/main/java/com/azure/spring/autoconfigure/storage/StorageAutoConfiguration.java#L45-L48) the `BlobServiceClient` for example: 

```java
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty("azure.storage.blob-endpoint")
    public BlobServiceClientBuilder blobServiceClientBuilder(StorageProperties storageProperties) {
        final String accountName = storageProperties.getAccountName();
        final String accountKey = storageProperties.getAccountKey();

        return new BlobServiceClientBuilder()
            .endpoint(storageProperties.getBlobEndpoint())
            .credential(new StorageSharedKeyCredential(accountName, accountKey))
            .httpLogOptions(new HttpLogOptions().setApplicationId(ApplicationId.AZURE_SPRING_STORAGE_BLOB));
    }
```
The `BlobServiceClientBuilder` we create above has many problems, it only supports the `Shared Key` credential and it only configures **3** options, but the `BlobServiceClientBuilder` has **17** methods to configure a client. Of course, we can change the code here in the `spring-cloud-azure-autoconfigure` module. But what if we want to `construct a BlobServiceClientBuilder` in Spring modules other than the `spring-cloud-azure-autoconfigure` module? We definitely don't want to write such code twice or many more times. So there has to be a module to put such `construct an Azure service client builder` operations, it could be in `spring-cloud-azure-core` or `spring-cloud-azure-service`. To keep our `spring-cloud-azure-core` as thin as possible, we chose to put them in the `spring-cloud-azure-service` module.

### Why do we need those ClientBuilderFactories
The SDK clients can be categorized into three types, at least the ones we're supporting, the HTTP-based, the AMQP-based, the other. `azure-core` abstracts the common options that could be configured to SDK clients, such as `Configuration`, `ClientOptions`, `HttpPipelinePolicy`, and etc. But each SDK client builder doesn't have such a common pattern, which suits the builders themselves but is not very handy for a framework user like us. For example, the `TokenCredential` is supposed to support by all Azure SDK clients, even if they are not now; the `HttpClient` is also configurable for all HTTP-based clients. 

In a Spring Boot application, it's natural for users to want to apply the same `TokenCredential` or `HttpClient` objects to some or all Azure SDK clients. If we extract the `construct an Azure service client builder` to a method, the method would have to take such parameters. Such as:
```java
public BlobServiceClientBuilder buildBlobServiceClientBuilder(StorageProperties storageProperties, TokenCredential defaultTokenCredential, HttpClient httpClient) {
        
}
```

And the methods are not convenient for us to use extension points provided by the Spring framework, such as `BeanPostProcessor` and `BeanFactoryPostProcessor`, when extending them we'll be able to configure the Spring beans we create in an easy and consistent way. Just like what we do to inject the default `TokenCredential` to all *ClientBuilderFactories:
```java
static class AzureServiceClientBuilderFactoryPostProcessor implements BeanPostProcessor, BeanFactoryAware {

        private BeanFactory beanFactory;

        @SuppressWarnings("rawtypes")
        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            if (bean instanceof AbstractAzureCredentialBuilderFactory) {
                return bean;
            }
            if (bean instanceof AbstractAzureServiceClientBuilderFactory
                && beanFactory.containsBean(DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME)) {

                ((AbstractAzureServiceClientBuilderFactory) bean).setDefaultTokenCredential(
                    (TokenCredential) beanFactory.getBean(DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME));
            }
            return bean;
        }

        @Override
        public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
            this.beanFactory = beanFactory;
        }
    }
```

So the *ClientBuilderFactory are created for two main purposes:
- Provide methods for us to pass in the `shared objects` users want to configure for many or all Azure SDK clients.
- Extract the common building logic based on the client type, HTTP, or AMQP. 

❗❗❗ These *ClientBuilderFactories are **not APIs** and won't be directly used by the users. 

### Why spring-cloud-azure-core depends on `storage-blob` and `storage-file-share`
Quote @Strong Liu: 
> Using "optional" scope to delay the function in runtime is very common design in spring, for example 
> https://github.com/spring-projects/spring-framework/blob/main/spring-core/spring-core.gradle
> And the "resource" we're extending here is at the core level of spring framework, and our "core" lib is built on top of spring-framework and azure-core, so I think it is proper place to have it here, and AWS is doing so as well, https://github.com/awspring/spring-cloud-aws/blob/2.3.x/spring-cloud-aws-core/pom.xml

### What does spring-cloud-azure-resourcemanager provide
The `Azure Resource Manager` can help provide connection information for Azure services. But it should not be a hard dependency in our `spring-cloud-azure-autoconfigure` module. In the `spring-cloud-azure-resourcemanager` we provide two kinds of APIs, the first kind is to retrieve connection string for Azure services, and the second kind is to provision resources. 


## spring-cloud-azure-core
Most of classes in this module are SPIs instead of APIs, and they won't be directly used by application users. 
### Realistic app example

https://gist.github.com/saragluna/562dde2a6b6b0e73a707333faf85bc82

### Config file example

User applications won't directly depend on this artifact, and the Spring Boot config file won't work only with this artifact. The config file will only function when the `spring-cloud-azure-autoconfigure` is on the classpath.

### Dependencies

- spring-context
- azure-identity
- azure-core
- azure-core-amqp
- azure-core-management
- azure-storage-blob (**Optional**)
- azure-storage-fileshare (**Optional**)

### Value it provides (over plain Azure SDK usage)

Explained in [this section](#why-do-we-need-spring-cloud-azure-core-spring-cloud-azure-service-and-spring-cloud-azure-resourcemanager).

### API

```java
// 1. com.azure.spring.core.connectionstring.ConnectionStringProvider
public interface ConnectionStringProvider<T> {
  String getConnectionString();
	T getServiceType();
}

// 2. com.azure.spring.core.connectionstring.StaticConnectionStringProvider
public final class StaticConnectionStringProvider<T> implements ConnectionStringProvider<T> {

  public StaticConnectionStringProvider(T serviceType, String connectionString) {
  }
}

// 3. com.azure.spring.core.customizer.AzureServiceClientBuilderCustomizer
public interface AzureServiceClientBuilderCustomizer<T> {
    void customize(T builder);
}

// 4. com.azure.spring.core.resource.AzureStorageFileProtocolResolver
This is a Azure Storage File Share implementation of Spring ProtocolResolver, ResourcePatternResolver

// 5. com.azure.spring.core.resource.AzureStorageBlobProtocolResolver
This is a Azure Storage Blob implementation of Spring ProtocolResolver, ResourcePatternResolver

```

### Misc

#### Perf and scalability 

`AzureStorageFileProtocolResolver` and `AzureStorageBlobProtocolResolver` are the two APIs that could have performance issue, tracked by 				this issue https://github.com/Azure/azure-sdk-for-java/issues/25916 and it's resolved now.

## spring-cloud-azure-service

❗❗❗ These *ClientBuilderFactories are **not APIs** and won't be directly used by the users. 

Most of classes in this module are **SPIs** instead of APIs, and they won't be directly used by application users. 

### Realistic app example

https://gist.github.com/saragluna/6c005fab01097f7495174e1c436d492d

### Config file example

User applications won't directly depend on this artifact, and the Spring Boot config file won't work only with this artifact. The config file will only function when the `spring-cloud-azure-autoconfigure` is on the classpath.

### Dependencies

- spring-cloud-azure-core
- azure-cosmos (**Optional**)
- azure-data-appconfiguration (**Optional**)
- azure-messaging-eventhubs (**Optional**)
- azure-messaging-servicebus (**Optional**)
- azure-security-keyvault-certificates  (**Optional**)
- azure-security-keyvault-secrets  (**Optional**)
- azure-storage-blob (**Optional**)
- azure-storage-fileshare (**Optional**)
- azure-storage-queue (**Optional**)

### Value it provides (over plain Azure SDK usage)

Explained in [this section](#why-do-we-need-spring-cloud-azure-core-spring-cloud-azure-service-and-spring-cloud-azure-resourcemanager).

### API

```java
// 1. com.azure.spring.service.eventhubs.processor.EventProcessingListener
public interface EventProcessingListener {

    default EventHubsInitializationContextConsumer getInitializationContextConsumer() {
        return initializationContextConsumer -> { };
    }
  
    default EventHubsCloseContextConsumer getCloseContextConsumer() {
        return closeContext -> { };
    }

    default EventHubsErrorContextConsumer getErrorContextConsumer() {
        return errorContext -> { };
    }

}

// 2. com.azure.spring.service.eventhubs.processor.BatchEventProcessingListener
public interface BatchEventProcessingListener extends EventProcessingListener {
    void onEventBatch(EventBatchContext eventBatchContext);
}

// 3. com.azure.spring.service.eventhubs.processor.RecordEventProcessingListener
public interface RecordEventProcessingListener extends EventProcessingListener {
    void onEvent(EventContext eventContext);
}

// 4. com.azure.spring.service.servicebus.processor.MessageProcessingListener
public interface MessageProcessingListener {
    default ServiceBusErrorContextConsumer getErrorContextConsumer() {
        return errorContext -> { };
    }
}

// 5. com.azure.spring.service.servicebus.processor.RecordMessageProcessingListener
public interface RecordMessageProcessingListener extends MessageProcessingListener {
    void onMessage(ServiceBusReceivedMessageContext messageContext);
}

```

### Misc

#### Perf and scalability 

Since this module only contains the *ClientBuilderFactories, so there should not be performance or scalability issues. 

## spring-cloud-azure-resourcemanager

### Realistic app example

https://gist.github.com/saragluna/dec50631b1ffd0125a477b6565b205f3

### Config file example

User applications won't directly depend on this artifact, and the Spring Boot config file won't work only with this artifact. The config file will only function when the `spring-cloud-azure-autoconfigure` is on the classpath.

### Dependencies

- spring-cloud-azure-core
- azure-resourcemanager

### Value it provides (over plain Azure SDK usage)

Explained in [this section](#what-does-spring-cloud-azure-resourcemanager-provide).

### API

```java
// 1. com.azure.spring.resourcemanager.provisioner.eventhubs.EventHubsProvisioner
public interface EventHubsProvisioner {

    void provisionNamespace(String namespace);

    void provisionEventHub(String namespace, String eventHub);

    void provisionConsumerGroup(String namespace, String eventHub, String consumerGroup);

}

// 2. com.azure.spring.resourcemanager.provisioner.servicebus.ServiceBusProvisioner
public interface ServiceBusProvisioner {

    void provisionQueue(String namespace, String queue);

    void provisionTopic(String namespace, String topic);

    void provisionSubscription(String namespace, String topic, String subscription);

}

// 3. com.azure.spring.resourcemanager.connectionstring.EventHubsArmConnectionStringProvider
public class EventHubsArmConnectionStringProvider extends ArmConnectionStringProvider<AzureServiceType.EventHubs> {
  public EventHubsArmConnectionStringProvider(AzureResourceManager resourceManager,
                                              AzureResourceMetadata resourceMetadata,
                                              String namespace) {
  }
  public String getConnectionString() {}
  public AzureServiceType.EventHubs getServiceType() {}
}

// 4. com.azure.spring.resourcemanager.connectionstring.ServiceBusArmConnectionStringProvider
public class ServiceBusArmConnectionStringProvider extends ArmConnectionStringProvider<AzureServiceType.ServiceBus> {
  public ServiceBusArmConnectionStringProvider(AzureResourceManager resourceManager,
                                               AzureResourceMetadata resourceMetadata,
                                               String namespace) {
  }
  public String getConnectionString() {}
  public AzureServiceType.ServiceBus getServiceType() {}
}

// 5. com.azure.spring.resourcemanager.connectionstring.StorageQueueArmConnectionStringProvider
public class StorageQueueArmConnectionStringProvider extends ArmConnectionStringProvider<AzureServiceType.StorageQueue> {
  public StorageQueueArmConnectionStringProvider(AzureResourceManager resourceManager,
                                                 AzureResourceMetadata resourceMetadata,
                                                 String accountName) {
  }
  public String getConnectionString() {}
  public AzureServiceType.StorageQueue getServiceType() {}
}

```

### Misc

#### Perf and scalability 

This module will call the resource manager to provision resources, which will take some time.