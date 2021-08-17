# Azure Spring Boot Starter Storage client library for Java

The project provides a Spring Boot Starter `azure-spring-boot-starter-storage` to auto-configure [Azure Blob storage][azure_blob_storage] in your Spring projects. It implements Spring Resource abstraction for Azure Storage service which allows you to interact with Azure Blob storage using Spring programming model.

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
  <artifactId>azure-spring-boot-starter-storage</artifactId>
</dependency>
```

## Key concepts
The [Azure Storage platform] is Microsoft's cloud storage solution for modern data storage scenarios. Core storage services offer a massively scalable object store for data objects, disk storage for Azure virtual machines (VMs), a file system service for the cloud, a messaging store for reliable messaging, and a NoSQL store.

### Configuration Options
Azure Storage Spring Boot Starter deprecates all legacy properties of which the prefix is `azure.storage` and uses `spring.cloud.azure.storage` instead.
When a deprecated property is detected while its active property is not found, the active property will be configured into the application environment with the value from the deprecated property.

If you load configuration properties from Azure Key Vault, the preceding detection and replacement are also applicable for Key Vault property sources. Replaced properties from Key Vault have a higher priority than local ones.
Note that replaced properties will not be refreshed as common properties from Key Vault property source.
#### Active Properties

|Name|Description|Comment|
|:---|:---|:---
spring.cloud.azure.storage.account-name | The name of the Azure Storage account. | | 
spring.cloud.azure.storage.account-key | The access key of the Azure Storage account. | | 
spring.cloud.azure.storage.blob-endpoint | The blob endpoint of the Azure Storage account. | Optional when storage blob resource is used. |
spring.cloud.azure.storage.file-endpoint | The file endpoint of the Azure Storage account. | Optional when storage file resource is used |

#### Deprecated Properties
|Obsolete Type|Description|Replacement Type|
|:---|:---|:---
azure.storage.account-name | The name of the Azure Storage account. |**spring.cloud.azure.storage.account-name**  |
azure.storage.account-key | The access key of the Azure Storage account. |**spring.cloud.azure.storage.account-key** |
azure.storage.blob-endpoint | The blob endpoint of the Azure Storage account. |**spring.cloud.azure.storage.blob-endpoint** |
azure.storage.file-endpoint | The file endpoint of the Azure Storage account. |**spring.cloud.azure.storage.file-endpoint** |

## Examples

Please use this [sample] as a reference for how to use **Azure Spring Boot Storage Starter** in your projects. 

#### Autowire a resource 
You can use the annotation of `@Value("azure-blob://[your-container-name]/[your-blob-name]")` to autowire a **blob** `Resource`.

```java
@Value("azure-blob://[your-container-name]/[your-blob-name]")
private Resource storageResource;
```

You can use the annotation of `azure-file://[your-fileshare-name]/[your-file-name]")` to autowire a **file** `Resource`.

```java
@Value("azure-file://[your-fileshare-name]/[your-file-name]")
private Resource storageResource;
```

#### Read and write to a resource 
 You can read a resource from Azure Blob storage with `getInputStream()` method.

```java
 this.storageResource.getInputStream();
```
You can write to a resource in Azure Blob storage by casting the Spring `Resource` to `WritableResource`. 

```java
 ((WritableResource) this.azureBlobResource).getOutputStream();
```

#### Other operations 
The Spring Resource abstraction for Azure Storage also supports [other operations][other_operation] defined in Spring's `Resource` and `WritableResource` interface. 

#### Autowire the BlobServiceClientBuilder
You can autowire the `BlobServiceClientBuilder` and create a client using:
```java
@Autowire
private BlobServiceClientBuilder blobServiceClientBuilder;

private final BlobServiceAsyncClient blobServiceAsyncClient = blobServiceClientBuilder.buildAsyncClient();

```

#### Search for resources
You can use implementation class `AzureStorageResourcePatternResolver` of `ResourcePatternResolver` to search resource, it supports `blob` or `file` type.
* Pattern search, the **searchPattern** should start with `azure-blob://` or `azure-file://`. Such as `azure-blob://*/*`, it means list all blobs in all containers; `azure-blob://demo-container/**`, it means list all blobs in the demo-container container, including any sub-folder.
* Location search, the **searchLocation** should start with `azure-blob://` or `azure-file://`, the remaining file path should exist, otherwise an exception will be thrown.

```java
AzureStorageResourcePatternResolver storageResourcePatternResolver = new AzureStorageResourcePatternResolver(blobServiceClientBuilder.buildClient());

Resource[] resources = storageResourcePatternResolver.getResources(searchPattern);
Resource resource = storageResourcePatternResolver.getResource(searchLocation);
```

#### Multipart upload
Files larger than 4 MiB will be uploaded to Azure Storage in parallel.

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
[docs]: https://docs.microsoft.com/azure/developer/java/spring-framework/configure-spring-boot-starter-java-app-with-azure-storage
[package]: https://mvnrepository.com/artifact/com.azure.spring/azure-spring-boot-starter-storage
[refdocs]: https://azure.github.io/azure-sdk-for-java/springboot.html#azure-spring-boot
[src]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/spring/azure-spring-boot-starter-storage
[sample]: https://github.com/Azure-Samples/azure-spring-boot-samples/tree/tag_azure-spring-boot_3.6.0/storage/azure-spring-boot-sample-storage-resource
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-with-Azure-SDK#use-logback-logging-framework-in-a-spring-boot-application
[logging_doc]: https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#boot-features-logging
[contributing_md]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/spring/CONTRIBUTING.md
[maven]: https://maven.apache.org/
[azure_blob_storage]: https://docs.microsoft.com/azure/storage/blobs/storage-blobs-introduction
[Azure Storage platform]: https://azure.microsoft.com/services/storage/
[other_operation]: https://docs.spring.io/spring/docs/current/spring-framework-reference/core.html#resources
[environment_checklist]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/ENVIRONMENT_CHECKLIST.md#ready-to-run-checklist
[Add azure-spring-boot-bom]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/spring/AZURE_SPRING_BOMS_USAGE.md#add-azure-spring-boot-bom
