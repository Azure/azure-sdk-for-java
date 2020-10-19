# Sample for Azure Blob storage Spring Boot client library for Java

## Key concepts
This sample project demonstrates how to use Azure Blob storage with Spring Boot. 

### Prerequisites

* An Azure subscription; if you don't already have an Azure subscription, you can activate your [MSDN subscriber benefits](https://azure.microsoft.com/pricing/member-offers/msdn-benefits-details/) or sign up for a [free Azure account](https://azure.microsoft.com/free/).

* A [Java Development Kit (JDK)][jdk_link], version 1.8.

* [Apache Maven](http://maven.apache.org/), version 3.0 or later.

## Getting started

### Create storage account on Azure

1. Go to [Azure portal](https://portal.azure.com/) and create the account by following this [link](https://docs.microsoft.com/azure/storage/storage-create-storage-account). 
2. In the `Access keys` blade, mark down the `Key`.

## Examples                                                           
### Config the sample

1. Navigate to `src/main/resources` and open `application.properties`.
2. Fill in the `account-name`, `account-key`, and the `container-name` you want to use. 

### Run with Maven
```
# Under sdk/spring project root directory
mvn clean install -DskipTests
cd azure-spring-boot-samples/azure-spring-boot-sample-storage-blob
mvn spring-boot:run
```

## Troubleshooting
## Next steps
Please check the following table for reference links of detailed Storage usage. 

Storage Type | Reference Link
--- | ---
`Blob storage` | [https://docs.microsoft.com/azure/storage/storage-java-how-to-use-blob-storage](https://docs.microsoft.com/azure/storage/storage-java-how-to-use-blob-storage)
`Queue storage` | [https://docs.microsoft.com/azure/storage/storage-java-how-to-use-queue-storage](https://docs.microsoft.com/azure/storage/storage-java-how-to-use-queue-storage)
`Table storage` | [https://docs.microsoft.com/azure/storage/storage-java-how-to-use-table-storage](https://docs.microsoft.com/azure/storage/storage-java-how-to-use-table-storage)
`File storage` | [https://docs.microsoft.com/azure/storage/storage-java-how-to-use-file-storage](https://docs.microsoft.com/azure/storage/storage-java-how-to-use-file-storage)

## Contributing

<!-- LINKS -->
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable







