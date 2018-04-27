# Microsoft Azure Storage SDK for Java

This project provides a client library in Java that makes it easy to consume Microsoft Azure Storage services. For documentation please see the [Storage API doc page](https://docs.microsoft.com/en-us/java/api/overview/azure/storage) and the generated [javadoc](http://azure.github.io/azure-storage-java/).

> If you are looking for the Azure Storage Android SDK, please visit [https://github.com/Azure/azure-storage-android](https://github.com/Azure/azure-storage-android).

# Features
  * Blob
      * Create/Read/Update/Delete containers
      * Create/Read/Update/Delete blobs
      * Advanced Blob Operations
  * Features new to V10
      * Asynchronous I/O for all operations
      * HttpPipeline which enables a high degree of per-request configurability and guaranteed thread safety
          * Please see the wiki for more information
      * 1-to-1 correlation with the Storage REST API for clarity and simplicity

# Getting Started

## Download
### Option 1: Via Maven

To get the binaries of this library as distributed by Microsoft, ready for use within your project, you can use Maven.

```xml
<dependency>
	<groupId>com.microsoft.azure</groupId>
	<artifactId>azure-storage-blob</artifactId>
	<version>10.0.0-Preview</version>
</dependency>
```

### Option 2: Source Via Git

To get the source code of the SDK via git just type:

    git clone git://github.com/Azure/azure-storage-java.git
    cd ./azure-storage-java
    git checkout New-Storage-SDK-V10-Preview
    mvn compile

### Option 3: Source Zip

To download a copy of the source code, use the drop down menu on the left to select the branch New-Storage-SDK-V10-Preview and click "Download ZIP" on the right side of the page or click [here](https://github.com/Azure/azure-storage-java/archive/master.zip). Unzip and navigate to the microsoft-azure-storage folder.

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

The following is a quick example on how to upload a file to azure blob and download it back. You may also run the samples in azure-storage/src/test/groovy/com/microsoft/azure/storage/Samples.java. For additional information on using the client libraries to access Azure services see the How To guides for [blobs](http://azure.microsoft.com/en-us/documentation/articles/storage-java-how-to-use-blob-storage/) and the [general documentation](http://azure.microsoft.com/en-us/develop/java/).

```java

```
## Building

If building from sources, run mvn compile to build. No build steps are necessary if including the package as a maven dependency.

## Running tests

Please refer to CONTRIBUTING.md for information on how to run the tests.

## Migrating to V10

Migrating to the newest version of the SDK will require a substantial rewrite of any component that interfaces with Azure Storage. Despite this, we feel the benefits offered by this new design are worth it, and we are happy to help with the transition!

# Need Help?

Be sure to check out the Microsoft Azure [Developer Forums on MSDN](http://social.msdn.microsoft.com/Forums/windowsazure/en-US/home?forum=windowsazuredata) or the [Developer Forums on Stack Overflow](http://stackoverflow.com/questions/tagged/azure+windows-azure-storage) if you have trouble with the provided code.

# Contribute Code or Provide Feedback

If you would like to become an active contributor to this project please follow the instructions provided in [Azure Projects Contribution Guidelines](http://azure.github.io/guidelines/).

If you encounter any bugs with the library please file an issue in the [Issues](https://github.com/Azure/azure-storage-java/issues) section of the project.

When sending pull requests, please send non-breaking PRs to the dev branch and breaking changes to the dev_breaking branch. Do not make PRs against master.

# Learn More

* [Java on Azure Developer Center](http://azure.microsoft.com/en-us/java/azure)
* [Azure Storage Service](http://azure.microsoft.com/en-us/documentation/services/storage/)
* [Azure Storage Team Blog](http://blogs.msdn.com/b/windowsazurestorage/)
* [Javadoc](http://azure.github.io/azure-storage-java/)