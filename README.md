#Windows Azure SDK for Java

This SDK allows you to build Windows Azure applications in Java that allow
you to take advantage of Azure scalable cloud computing resources: table and blob
storage, messaging through Service Bus.

For documentation please see the [Windows Azure Java Developer Center](http://www.windowsazure.com/en-us/develop/java/)

#Features
* Blob
  * Create/Read/Update/Delete Blobs
* Queue
  * Create/Delete Queues
  * Insert/Peek Queue Messages
  * Advanced Queue Operations
* Service Bus
  * Use either the Queue or Topic/Subscription Model
* Service Runtime
  * Retrieve information about the state of your Azure Compute instances
* Table
  * Manage Tables
  * Work with Table Entities (CRUD)
  * Entity Group Transactions (Batch)

#Getting Started

##Download
###Option 1: Via Git

To get the source code of the SDK via git just type:

    git clone git://github.com/WindowsAzure/azure-sdk-for-java.git
    cd ./azure-sdk-for-java
    mvn compile

###Option 2: Via Maven

To get the binaries of this library as distributed by Microsoft, ready for use
within your project you can also have them installed by the Java package manager Maven.

```xml
<dependency>
    <groupId>com.microsoft.windowsazure</groupId>
    <artifactId>microsoft-windowsazure-api</artifactId>
    <version>0.3.0</version>
</dependency>
```

##Minimum Requirements

* Java 1.6
* (Optional) Maven
 

##Usage

To use this SDK to call Windows Azure services, you need to first create an
account.  To host your Java code in Windows Azure, you additionally need to download
the full Windows Azure SDK for Java - which includes packaging, emulation, and
deployment tools.

##Code Samples

The following is a quick example on how to set up a Azure blob using the API
and uploading a file to it.  For additional information on using the client libraries to access Azure services see the How To guides listed [here](http://www.windowsazure.com/en-us/develop/java/).

```java
import com.microsoft.windowsazure.services.core.storage.*;
import com.microsoft.windowsazure.services.blob.client.*;

public class BlobSample {

    public static final String storageConnectionString = 
            "DefaultEndpointsProtocol=http;" + 
            "AccountName=your_account_name;" + 
            "AccountKey= your_account_name"; 

    public static void main(String[] args) 
    {
        try
        {
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

            // Upload an image file.
            blob = container.getBlockBlobReference("image1.jpg");
            File fileReference = new File ("c:\\myimages\\image1.jpg");
            blob.upload(new FileInputStream(fileReference), fileReference.length());
        } 
        catch (FileNotFoundException fileNotFoundException)
        {
            System.out.print("FileNotFoundException encountered: ");
            System.out.println(fileNotFoundException.getMessage());
            System.exit(-1);
        }
        catch (StorageException storageException)
        {
            System.out.print("StorageException encountered: ");
            System.out.println(storageException.getMessage());
            System.exit(-1);
        }
        catch (Exception e)
        {
            System.out.print("Exception encountered: ");
            System.out.println(e.getMessage());
            System.exit(-1);
        }
    
    }
}
```

#Need Help?

Be sure to check out the Windows Azure [Developer Forums on Stack Overflow](http://go.microsoft.com/fwlink/?LinkId=234489) if you have trouble with the provided code.

#Contribute Code or Provide Feedback

If you would like to become an active contributor to this project please follow the instructions provided in [Windows Azure Projects Contribution Guidelines](http://windowsazure.github.com/guidelines.html).

If you encounter any bugs with the library please file an issue in the [Issues](https://github.com/WindowsAzure/azure-sdk-for-java/issues) section of the project.

#Learn More

* [Windows Azure Java Developer Center](http://www.windowsazure.com/en-us/develop/java/)
* [JavaDocs](http://dl.windowsazure.com/javadoc/)

