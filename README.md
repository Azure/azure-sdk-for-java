#Microsoft Azure SDK for Java

This project provides a client library in Java that makes it easy to consume Microsoft Azure services. For documentation please see the [Microsoft Azure Java Developer Center](http://azure.microsoft.com/en-us/develop/java/).

#Features

* Storage
    * Blob
        * Create/Read/Update/Delete containers
        * Create/Read/Update/Delete blobs
    * Queue
        * Create/Delete Queues
        * Insert/Peek Queue Messages
        * Advanced Queue Operations
    * Table
        * Create/Read/Update/Delete tables
        * Create/Read/Update/Delete entities
        * Batch operation
* Service Bus
    * Queues
        * Create/Read/Update/Delete queues
        * Send/Receive/Unlock/Delete messages
        * Renew message lock
        * Message forwarding
    * Topics
        * Create/Read/Update/Delete topics
        * Create/Read/Update/Delete subscriptions
        * Create/Read/Update/Delete rules
        * Send/Receive/Unlock/Delete messages
        * Renew message lock
        * Message forwarding
* Media Services
    * Create/Read/Update/Delete access policies
    * Create/Read/Update/Delete asset files
    * Create/Read/Update/Delete assets
    * Create/Read/Update/Delete/Rebind content keys
    * Create/Read/Update/Cancel/Delete jobs
    * Add/Get job notifications
    * Create/Read/Update/Delete notification endpoints
* Service Management
    * Compute Management
    * Web Site Management
    * Virtual Network Management
    * Storage Management
    * Sql Database Management
* Service Runtime
    * Retrieve information about the state of your Azure Compute instances


#Getting Started

##Download
###Option 1: Via Git

To get the source code of the SDK via git just type:

    git clone git://github.com/Azure/azure-sdk-for-java.git
    cd ./azure-sdk-for-java/microsoft-azure-api/
    mvn compile

###Option 2: Via Maven

To get the binaries of this library as distributed by Microsoft, ready for use
within your project you can also have them installed by the Java package manager Maven.

```xml
<dependency>
  <groupId>com.microsoft.windowsazure</groupId>
  <artifactId>microsoft-windowsazure-api</artifactId>
  <version>0.4.5</version>
</dependency>
```

##Minimum Requirements

* Java 1.6
* (Optional) Maven


##Usage

To use this SDK to call Microsoft Azure services, you need to first create an
account.  To host your Java code in Microsoft Azure, you additionally need to download
the full Microsoft Azure SDK for Java - which includes packaging, emulation, and
deployment tools.

##Code Sample

The following is a quick example on how to set up a Azure blob using the API
and uploading a file to it.  For additional information on using the client libraries to access Azure services see the How To guides listed [here](http://azure.microsoft.com/en-us/develop/java/).

```java
import java.io.*;

import com.microsoft.windowsazure.services.core.storage.*;
import com.microsoft.windowsazure.services.blob.client.*;

public class BlobSample {
    public static final String storageConnectionString =
            "DefaultEndpointsProtocol=http;"
            + "AccountName=your_account_name;"
            + "AccountKey= your_account_key";

    public static void main(String[] args) {
        try {
            CloudStorageAccount account;
            CloudBlobClient serviceClient;
            CloudBlobContainer container;
            CloudBlockBlob blob;

            account = CloudStorageAccount.parse(storageConnectionString);
            serviceClient = account.createCloudBlobClient();
            // Container name must be lower case.
            container = serviceClient.getContainerReference("blobsample");
            container.createIfNotExist();

            // Set anonymous access on the container.
            BlobContainerPermissions containerPermissions;
            containerPermissions = new BlobContainerPermissions();
            container.uploadPermissions(containerPermissions);

            // Upload an image file.
            blob = container.getBlockBlobReference("image1.jpg");
            File fileReference = new File("c:\\myimages\\image1.jpg");
            blob.upload(new FileInputStream(fileReference), fileReference.length());
        } catch (FileNotFoundException fileNotFoundException) {
            System.out.print("FileNotFoundException encountered: ");
            System.out.println(fileNotFoundException.getMessage());
            System.exit(-1);
        } catch (StorageException storageException) {
            System.out.print("StorageException encountered: ");
            System.out.println(storageException.getMessage());
            System.exit(-1);
        } catch (Exception e) {
            System.out.print("Exception encountered: ");
            System.out.println(e.getMessage());
            System.exit(-1);
        }

    }
}
```

#Need Help?

Be sure to check out the Microsoft Azure [Developer Forums on Stack Overflow](http://go.microsoft.com/fwlink/?LinkId=234489) if you have trouble with the provided code.

#Contribute Code or Provide Feedback

If you would like to become an active contributor to this project please follow the instructions provided in [Microsoft Azure Projects Contribution Guidelines](http://azure.github.com/guidelines.html).

If you encounter any bugs with the library please file an issue in the [Issues](https://github.com/Azure/azure-sdk-for-java/issues) section of the project.

#Learn More

* [Microsoft Azure Java Developer Center](http://azure.microsoft.com/en-us/develop/java/)
* [JavaDocs](http://dl.windowsazure.com/javadoc/)

