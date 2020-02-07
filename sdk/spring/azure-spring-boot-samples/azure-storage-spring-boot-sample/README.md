## Overview
This sample project demonstrates how to use Azure Storage via Spring Boot Starter `azure-storage-spring-boot-starter`. 

## Prerequisites

* An Azure subscription; if you don't already have an Azure subscription, you can activate your [MSDN subscriber benefits](https://azure.microsoft.com/en-us/pricing/member-offers/msdn-benefits-details/) or sign up for a [free Azure account](https://azure.microsoft.com/en-us/free/).

* A [Java Development Kit (JDK)](http://www.oracle.com/technetwork/java/javase/downloads/), version 1.8.

* [Apache Maven](http://maven.apache.org/), version 3.0 or later.

## Quick Start

### Create storage account on Azure

1. Go to [Azure portal](https://portal.azure.com/) and create the account by following this [link](https://docs.microsoft.com/en-us/azure/storage/storage-create-storage-account). 
2. In the `Access keys` blade, mark down the `CONNECTION STRING`.
                                                                                                                                  
### Config the sample

1. Navigate to `src/main/resources` and open `application.properties`.
2. Fill in the `connection-string`. 

### Run the sample

1. Change directory to folder `azure-storage-spring-boot-sample`.
2. Run below commands. 

   - Use Maven 

     ```
     mvn package
     java -jar target/azure-storage-spring-boot-sample-0.0.1-SNAPSHOT.jar
     ```

   - Use Gradle 
   
     ```
     gradle bootRepackage
     java -jar build/libs/azure-storage-spring-boot-sample-0.0.1-SNAPSHOT.jar
     ```

## Sample usage 

### List the blobs in a container

1. Navigate to `src/main/java/com/microsoft/azure` and open `StorageSampleApplication.java`
2. Add below method

```java
private void listBlobInContainer(String containerName) throws StorageException, URISyntaxException {

        // Create the blob client.
        final CloudBlobClient blobClient = cloudStorageAccount.createCloudBlobClient();

        // Retrieve reference to a previously created container.
        final CloudBlobContainer container = blobClient.getContainerReference(containerName);

        // Loop over blobs within the container and output the URI to each of them.
        for (final ListBlobItem blobItem : container.listBlobs()) {
            System.out.println(blobItem.getUri());
        }
    }
```

3. Import `ListBlobItem`

```
import com.microsoft.azure.storage.blob.ListBlobItem;
```

4. Update `run` method and save

```java
public void run(String... var1) throws URISyntaxException, StorageException, IOException {
        createContainerIfNotExists("mycontainer");
        
        // List the blobs in a container
        listBlobInContainer("mycontainer");
    }
```

5. Run below commands. 

```
mvn package
java -jar target/azure-storage-spring-boot-sample-0.0.1-SNAPSHOT.jar
```

### More usage

Please check the following table for reference links of detailed Storage usage. 

Storage Type | Reference Link
--- | ---
`Blob Storage` | [https://docs.microsoft.com/en-us/azure/storage/storage-java-how-to-use-blob-storage](https://docs.microsoft.com/en-us/azure/storage/storage-java-how-to-use-blob-storage)
`Queue Storage` | [https://docs.microsoft.com/en-us/azure/storage/storage-java-how-to-use-queue-storage](https://docs.microsoft.com/en-us/azure/storage/storage-java-how-to-use-queue-storage)
`Table Storage` | [https://docs.microsoft.com/en-us/azure/storage/storage-java-how-to-use-table-storage](https://docs.microsoft.com/en-us/azure/storage/storage-java-how-to-use-table-storage)
`File Storage` | [https://docs.microsoft.com/en-us/azure/storage/storage-java-how-to-use-file-storage](https://docs.microsoft.com/en-us/azure/storage/storage-java-how-to-use-file-storage)








