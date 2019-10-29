# Microsoft Azure Storage SDK v11 for Java

This project provides a client library in Java that makes it easy to consume Microsoft Azure Storage services. For documentation please see the [Storage API doc page](https://docs.microsoft.com/en-us/java/api/overview/azure/storage/client?view=azure-java-preview) and the [quick start document](https://docs.microsoft.com/en-us/azure/storage/blobs/storage-quickstart-blobs-java-v10).
Please note that this version of the library is a compete overhaul of the current Azure Storage Java Client Library, and is based on the new Storage SDK architecture, also referred to as V11.

| SDK Name      | Version       | Description | Maven/API Reference Links |
| ------------- | ------------- | ----------- | ----- |
| [Blob Storage SDK v11 for Java](https://github.com/Azure/azure-storage-java/)  | v11.0.1  | The next generation async Storage SDK | [Maven](https://mvnrepository.com/artifact/com.microsoft.azure/azure-storage-blob) - [Reference](https://docs.microsoft.com/en-us/java/api/overview/azure/storage/client?view=azure-java-stable) |
| [Queue Storage SDK v10 for Java](https://github.com/azure/azure-storage-java/tree/New-Storage-SDK-V10-Preview) | V10.0.0-Preview | The next generation async Storage SDK | [Maven](https://mvnrepository.com/artifact/com.microsoft.azure/azure-storage-queue) - [Reference](https://docs.microsoft.com/en-us/java/api/overview/azure/storage/queue?view=azure-java-preview)
| [Storage SDK v8 for Java](https://github.com/azure/azure-storage-java/tree/legacy-master)  | v8  | Legacy Storage SDK (sync only) | [Maven](https://mvnrepository.com/artifact/com.microsoft.azure/azure-storage) - [Reference](https://docs.microsoft.com/en-us/java/api/overview/azure/storage_stable?view=azure-java-legacy)|
| [Storage SDK for Android](https://github.com/Azure/azure-storage-android) | v2 | Storage SDK for Android | [Maven](https://mvnrepository.com/artifact/com.microsoft.azure.android/azure-storage-android) - [Reference](http://azure.github.io/azure-storage-android/)
| [Azure Management Libraries for Java](https://github.com/Azure/azure-libraries-for-java) | v1 | Management libraries including Storage Resource Provider APIs | [Maven](https://mvnrepository.com/artifact/com.microsoft.azure/azure-mgmt-resources) - [Reference](http://azure.github.io/azure-storage-android/)|

## Migrating to V11

Migrating to the newest version of the SDK will require a substantial rewrite of any component that interfaces with Azure Storage. Despite this, we feel the benefits offered by this new design are worth it, and we are happy to help with the transition! Please refer to the wiki for information on the core ideas behind the new design and best practices on how to use it effectively.

# Features
  * Blob
      * Create/Read/Update/Delete containers
      * Create/Read/Update/Delete blobs
      * Advanced Blob Operations wrapped in the TransferManager class
  * Features new to V11
      * Asynchronous I/O for all operations using the [ReactiveX](https://github.com/ReactiveX/RxJava) framework
      * HttpPipeline which enables a high degree of per-request configurability and guaranteed thread safety
          * Please see the wiki for more information
      * 1-to-1 correlation with the Storage REST API for clarity and simplicity

# Getting Started

## Download
### Option 1: Via Maven

To get the binaries of this library as distributed by Microsoft, ready for use within your project, you can use Maven.

[//]: # ({x-version-update-start;com.microsoft.azure:azure-storage-blob;current})
```xml
<dependency>
	<groupId>com.microsoft.azure</groupId>
	<artifactId>azure-storage-blob</artifactId>
	<version>11.0.2</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Option 2: Source Via Git

To get the source code of the SDK via git just type:

    git clone git://github.com/Azure/azure-storage-java.git
    cd ./azure-storage-java
    mvn compile

### Option 3: Source Zip

To download a copy of the source code, click "Download ZIP" on the right side of the page or click [here](https://github.com/Azure/azure-storage-java/archive/master.zip). Unzip and navigate to the microsoft-azure-storage folder.

## Minimum Requirements

* Java 1.8+
* [Jackson-Core](https://github.com/FasterXML/jackson-core) is used for JSON and XML parsing. 
* [ReactiveX](https://github.com/ReactiveX/RxJava) is used for reactive, asynchronous IO operations.
* [Autorest-runtime](https://github.com/Azure/autorest-clientruntime-for-java) is used to interact with auto-generated code.
* (Optional) Maven

The three dependencies, [Jackson-Core](https://github.com/FasterXML/jackson-core), [ReactiveX](https://github.com/ReactiveX/RxJava), and [Autorest-runtime](https://github.com/Azure/autorest-clientruntime-for-java), will be added automatically if Maven is used. Otherwise, please download the jars and add them to your build path. 

## Usage

To use this SDK to call Microsoft Azure storage services, you need to first [create an account](https://azure.microsoft.com/free).

Samples are provided in azure-storage/src/test/groovy/com/microsoft/azure/storage/Samples.java. The unit tests in the same directory can also be helpful.

## Code Sample

The following is a quick example on how to upload some data to an azure blob and download it back. You may also run the samples in azure-storage/src/test/groovy/com/microsoft/azure/storage/Samples.java. For additional information on using the client libraries to access Azure services see the How To guides for [blobs](http://azure.microsoft.com/en-us/documentation/articles/storage-java-how-to-use-blob-storage/) and the [general documentation](http://azure.microsoft.com/en-us/develop/java/).

```java
public class Sample {
    /**
     * This example shows how to start using the Azure Storage Blob SDK for Java.
     */
    public void basicExample() throws InvalidKeyException, MalformedURLException {
        // From the Azure portal, get your Storage account's name and account key.
        String accountName = getAccountName();
        String accountKey = getAccountKey();

        // Use your Storage account's name and key to create a credential object; this is used to access your account.
        SharedKeyCredentials credential = new SharedKeyCredentials(accountName, accountKey);

        /*
        Create a request pipeline that is used to process HTTP(S) requests and responses. It requires your accont
        credentials. In more advanced scenarios, you can configure telemetry, retry policies, logging, and other
        options. Also you can configure multiple pipelines for different scenarios.
         */
        HttpPipeline pipeline = StorageURL.createPipeline(credential, new PipelineOptions());

        /*
        From the Azure portal, get your Storage account blob service URL endpoint.
        The URL typically looks like this:
         */
        URL u = new URL(String.format(Locale.ROOT, "https://%s.blob.core.windows.net", accountName));

        // Create a ServiceURL objet that wraps the service URL and a request pipeline.
        ServiceURL serviceURL = new ServiceURL(u, pipeline);

        // Now you can use the ServiceURL to perform various container and blob operations.

        // This example shows several common operations just to get you started.

        /*
        Create a URL that references a to-be-created container in your Azure Storage account. This returns a
        ContainerURL object that wraps the container's URL and a request pipeline (inherited from serviceURL).
        Note that container names require lowercase.
         */
        ContainerURL containerURL = serviceURL.createContainerURL("myjavacontainerbasic");

        /*
        Create a URL that references a to-be-created blob in your Azure Storage account's container.
        This returns a BlockBlobURL object that wraps the blob's URl and a request pipeline
        (inherited from containerURL). Note that blob names can be mixed case.
         */
        BlockBlobURL blobURL = containerURL.createBlockBlobURL("HelloWorld.txt");

        String data = "Hello world!";

        // Create the container on the service (with no metadata and no public access)
        containerURL.create(null, null)
                .flatMap(containersCreateResponse ->
                        /*
                         Create the blob with string (plain text) content.
                         NOTE: It is imperative that the provided length matches the actual length exactly.
                         */
                        blobURL.upload(Flowable.just(ByteBuffer.wrap(data.getBytes())), data.length(),
                                null, null, null))
                .flatMap(blobsDownloadResponse ->
                        // Download the blob's content.
                        blobURL.download(null, null, false))
                .flatMap(blobsDownloadResponse ->
                        // Verify that the blob data round-tripped correctly.
                        FlowableUtil.collectBytesInBuffer(blobsDownloadResponse.body(null))
                                .doOnSuccess(byteBuffer -> {
                                    if (byteBuffer.compareTo(ByteBuffer.wrap(data.getBytes())) != 0) {
                                        throw new Exception("The downloaded data does not match the uploaded data.");
                                    }
                                }))
                .flatMap(byteBuffer ->
                        // Delete the blob we created earlier.
                        blobURL.delete(null, null))
                .flatMap(blobsDeleteResponse ->
                        // Delete the container we created earlier.
                        containerURL.delete(null))
                /*
                This will synchronize all the above operations. This is strongly discouraged for use in production as
                it eliminates the benefits of asynchronous IO. We use it here to enable the sample to complete and
                demonstrate its effectiveness.
                 */
                .blockingGet();
    }
}
```

## Building

If building from sources, run mvn compile to build. No build steps are necessary if including the package as a maven dependency.

## Running tests

Please refer to CONTRIBUTING.md for information on how to run the tests.

# Need Help?

Be sure to check out the Microsoft Azure [Developer Forums on MSDN](http://social.msdn.microsoft.com/Forums/windowsazure/en-US/home?forum=windowsazuredata) or the [Developer Forums on Stack Overflow](http://stackoverflow.com/questions/tagged/azure+windows-azure-storage) if you have trouble with the provided code.

# Contribute Code or Provide Feedback

If you would like to become an active contributor to this project please follow the instructions provided in [Azure Projects Contribution Guidelines](http://azure.github.io/guidelines/).

If you encounter any bugs with the library please file an issue in the [Issues](https://github.com/Azure/azure-storage-java/issues) section of the project.

When sending pull requests, please send non-breaking PRs to the dev branch and breaking changes to the dev_breaking branch. Do not make PRs against master.

# Learn More

* [Quick Start with the Azure Storage SDK v11 for Java](https://docs.microsoft.com/en-us/azure/storage/blobs/storage-quickstart-blobs-java-v10)
* [Java API Reference](https://docs.microsoft.com/en-us/java/api/overview/azure/storage/client?view=azure-java-preview)
* [Azure Storage Service](http://azure.microsoft.com/en-us/documentation/services/storage/)
* [Azure Storage Team Blog](http://blogs.msdn.com/b/windowsazurestorage/)
* [Javadoc](http://azure.github.io/azure-storage-java/)

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fstorage%2Fmicrosoft-azure-storage-blob%2FREADME.png)