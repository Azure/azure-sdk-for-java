#Windows Azure Storage SDK for Java

This project provides a client library in Java that makes it easy to consume Windows Azure Storage services. For documentation please see the [Windows Azure Java Developer Center](http://www.windowsazure.com/en-us/develop/java/) and the [JavaDocs](http://dl.windowsazure.com/storage/javadoc).

#Features
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

#Getting Started

##Download
###Option 1: Via Git

To get the source code of the SDK via git just type:

    git clone git://github.com/WindowsAzure/azure-storage-java.git
    cd ./azure-storage-java/microsoft-azure-storage
    mvn compile

###Option 2: Via Maven

To get the binaries of this library as distributed by Microsoft, ready for use
within your project you can also have them installed by the Java package manager Maven.

```xml
<dependency>
	<groupId>com.microsoft.windowsazure.storage</groupId>
	<artifactId>microsoft-windowsazure-storage-sdk</artifactId>
	<version>0.5.0</version>
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

##Code Sample

The following is a quick example on how to set up a Azure blob using the API and uploading a file to it.  For additional information on using the client libraries to access Azure services see the How To guides for [blobs](http://www.windowsazure.com/en-us/develop/java/how-to-guides/blob-storage/), [queues](http://www.windowsazure.com/en-us/develop/java/how-to-guides/queue-service/) and [tables](http://www.windowsazure.com/en-us/develop/java/how-to-guides/table-service/) and the [general documentation](http://www.windowsazure.com/en-us/develop/java/).

```java
import java.io.*;

import com.microsoft.windowsazure.storage.*;
import com.microsoft.windowsazure.storage.blob.*;

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
			container.createIfNotExists();

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

Be sure to check out the Windows Azure [Developer Forums on MSDN](http://social.msdn.microsoft.com/Forums/windowsazure/en-US/home?forum=windowsazuredata) or the [Developer Forums on Stack Overflow](http://stackoverflow.com/questions/tagged/azure+windows-azure-storage) if you have trouble with the provided code.

#Contribute Code or Provide Feedback

If you would like to become an active contributor to this project please follow the instructions provided in [Windows Azure Projects Contribution Guidelines](http://windowsazure.github.com/guidelines.html).

If you encounter any bugs with the library please file an issue in the [Issues](https://github.com/WindowsAzure/azure-storage-java/issues) section of the project.

#Learn More

* [Windows Azure Java Developer Center](http://www.windowsazure.com/en-us/develop/java/)
* [Windows Azure Storage Service](http://www.windowsazure.com/en-us/documentation/services/storage/)
* [JavaDocs](http://dl.windowsazure.com/storage/javadoc)
