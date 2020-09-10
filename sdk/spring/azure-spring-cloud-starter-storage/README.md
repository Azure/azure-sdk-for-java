# Azure Spring Cloud Starter Storage client library for Java

The project provides a Spring Boot Starter `spring-cloud-starter-azure-storage` to auto-configure [Azure Blob storage](https://docs.microsoft.com/en-us/azure/storage/blobs/storage-blobs-introduction) in your Spring projects. It implements Spring Resource abstraction for Azure Storage service which allows you to interact with Azure Blob storage using Spring programming model.

## Key concepts
## Getting started
## Examples

Please use this `sample` as a reference for how to use **Spring Cloud Azure Storage** in your projects. 

### Feature List 

- [Auto-configuration for Azure Blob storage](#auto-configuration-for-azure-blob-storage)
- [Autowire a resource](#autowire-a-resource)
- [Read and write to a resource](#read-and-write-to-a-resource)
- [Other operations](#other-operations) 

#### Auto-configuration for Azure Blob storage

Please use `spring-cloud-starter-azure-storage` to auto-configure Azure Storge in your project. 

**Maven Coordinates** 
```
<dependency>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>spring-cloud-starter-azure-storage</artifactId>
</dependency>
```
**Gradle Coordinates** 
```
dependencies {
    compile group: 'com.microsoft.azure', name: 'spring-cloud-starter-azure-storage'
}
```

The `spring-cloud-starter-azure-storage` provides the following configuration options in `application.properties`.

Name | Description | Required | Default 
---|---|---|---
 spring.cloud.azure.credential-file-path | Location of azure credential file | Yes |
 spring.cloud.azure.resource-group | Name of Azure resource group | Yes |
 spring.cloud.azure.region | Region name of the Azure resource group, e.g. westus | Yes | 
 spring.cloud.azure.storage.account | Name of the Azure Storage Account. Will create a new one if not existing | Yes |
 spring.cloud.azure.storage.enabled | Turn on or off functionalities of Spring Cloud Azure Storage | No | true

#### Autowire a resource 
You can use the annotation of `@Value("blob://{containerName}/{blobName}")` to autowire a `Resource` with that in [Azure Blob storage](https://azure.microsoft.com/en-us/services/storage/blobs/).

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
The Spring Resource abstraction for Azure Storage also supports [other operations](https://docs.spring.io/spring/docs/current/spring-framework-reference/core.html#resources) defined in Spring's `Resource` and `WritableResource` interface. 


## Troubleshooting
## Next steps
## Contributing
