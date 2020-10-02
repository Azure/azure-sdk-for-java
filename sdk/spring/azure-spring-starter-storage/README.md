# Azure Spring Starter Storage client library for Java

The project provides a Spring Boot Starter `spring-starter-azure-storage` to auto-configure [Azure Blob storage][azure_blob_storage] in your Spring projects. It implements Spring Resource abstraction for Azure Storage service which allows you to interact with Azure Blob storage using Spring programming model.

## Getting started
### Prerequisites
- [Java Development Kit (JDK)][jdk_link] with version 8 or above
- [Azure Subscription][azure_subscription]
- [Maven][maven] 3.0 and above

### Include the package
[//]: # ({x-version-update-start;com.microsoft.azure:spring-starter-azure-storage;current})
```xml
<dependency>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>spring-starter-azure-storage</artifactId>
    <version>1.2.8</version>
</dependency>
```
[//]: # ({x-version-update-end})

## Key concepts
The Azure Storage platform is Microsoft's cloud storage solution for modern data storage scenarios. Core storage services offer a massively scalable object store for data objects, disk storage for Azure virtual machines (VMs), a file system service for the cloud, a messaging store for reliable messaging, and a NoSQL store.

## Examples

Please use this `sample` as a reference for how to use **Spring Cloud Azure Storage** in your projects. 

#### Auto-configuration for Azure Blob storage

The `spring-starter-azure-storage` provides the following configuration options in `application.properties`.

Name | Description | Required | Default 
---|---|---|---
 spring.cloud.azure.credential-file-path | Location of azure credential file | Yes |
 spring.cloud.azure.resource-group | Name of Azure resource group | Yes |
 spring.cloud.azure.region | Region name of the Azure resource group, e.g. westus | Yes | 
 spring.cloud.azure.storage.account | Name of the Azure Storage Account. Will create a new one if not existing | Yes |
 spring.cloud.azure.storage.enabled | Turn on or off functionalities of Spring Cloud Azure Storage | No | true

#### Autowire a resource 
You can use the annotation of `@Value("blob://{containerName}/{blobName}")` to autowire a `Resource` with that in [Azure Blob storage][azure_storage].

```
@Value("blob://{containerName}/{blobName}")
private Resource blobFile;
```

#### Read and write to a resource 
 You can read a resource from Azure Blob storage with `getInputStream()` method.

```
 this.blobFile.getInputStream();
```
You can write to a resource in Azure Blob storage by casting the Spring `Resource` to `WritableResource`. 

```
(WritableResource) this.blobFile).getOutputStream();
```

#### Other operations 
The Spring Resource abstraction for Azure Storage also supports [other operations][other_operation] defined in Spring's `Resource` and `WritableResource` interface. 


## Troubleshooting
### Enable client logging
Azure SDKs for Java offers a consistent logging story to help aid in troubleshooting application errors and expedite their resolution. The logs produced will capture the flow of an application before reaching the terminal state to help locate the root issue. View the [logging][logging] wiki for guidance about enabling logging.

### Enable Spring logging
Spring allow all the supported logging systems to set logger levels set in the Spring Environment (for example, in application.properties) by using `logging.level.<logger-name>=<level>` where level is one of TRACE, DEBUG, INFO, WARN, ERROR, FATAL, or OFF. The root logger can be configured by using logging.level.root.

The following example shows potential logging settings in `application.properties`:

```properties
logging.level.root=WARN
logging.level.org.springframework.web=DEBUG
logging.level.org.hibernate=ERROR
```

For more information about setting logging in spring, please refer to the [official doc][logging_doc].
 
## Next steps
The following section provide a sample project illustrating how to use the starter.
### More sample code
- [Azure Storage][sample]

## Contributing
This project welcomes contributions and suggestions.  Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

Please follow [instructions here][contributing_md] to build from source or contribute.

<!-- Link -->
[src]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/spring/azure-spring-starter-storage
[package]: https://mvnrepository.com/artifact/com.microsoft.azure/spring-starter-azure-storage
[refdocs]: https://azure.github.io/azure-sdk-for-java/spring.html#spring-starter-azure-storage
[sample]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/spring/azure-spring-boot-samples/azure-spring-cloud-storage-resource-sample
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-with-Azure-SDK#use-logback-logging-framework-in-a-spring-boot-application
[azure_subscription]: https://azure.microsoft.com/free
[logging_doc]: https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-logging
[contributing_md]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/spring/CONTRIBUTING.md
[maven]: http://maven.apache.org/
[azure_blob_storage]: https://docs.microsoft.com/azure/storage/blobs/storage-blobs-introduction
[azure_storage]: https://azure.microsoft.com/services/storage/blobs/
[other_operation]: https://docs.spring.io/spring/docs/current/spring-framework-reference/core.html#resources
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
