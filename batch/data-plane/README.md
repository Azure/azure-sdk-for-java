﻿[![Build Status](https://travis-ci.org/Azure/azure-batch-sdk-for-java.svg?style=flat-square&label=build)](https://travis-ci.org/Azure/azure-batch-sdk-for-java)

## Azure Batch Libraries for Java

This README is based on the latest released version Azure Batch SDK (5.0.0). If you are looking for other releases, see the [More Information](#more-information) section below.

The Azure Batch Libraries for Java is a higher-level, object-oriented API for interacting with the Azure Batch service.


> **5.0.0** is a release that supports all features of Azure Batch service with API version "2018-12-01.8.1". We will be adding support for more new features and tweaking the API associated with Azure Batch service newer release.

**Azure Batch Authentication**

You need to create a Batch account through the [Azure portal](https://portal.azure.com) or Azure cli. 

* Use the account name, key, and URL to create a `BatchSharedKeyCredentials` instance for authentication with the Azure Batch service.
The `BatchClient` class is the simplest entry point for creating and interacting with Azure Batch objects.

```java
BatchSharedKeyCredentials cred = new BatchSharedKeyCredentials(batchUri, batchAccount, batchKey);
BatchClient client = BatchClient.open(cred);
```

* The other way is using AAD (Azure Active Directory) authentication to create the client. See this [document](https://docs.microsoft.com/en-us/azure/batch/batch-aad-auth) for detail.

```java
BatchApplicationTokenCredentials cred = new BatchApplicationTokenCredentials(batchEndpoint, clientId, applicationSecret, applicationDomain, null, null);
BatchClient client = BatchClient.open(cred);
```

**Create a pool using an Azure Marketplace image**

You can create a pool of Azure virtual machines which can be used to execute tasks.

```java
System.out.println("Created a pool using an Azure Marketplace image.");

VirtualMachineConfiguration configuration = new VirtualMachineConfiguration();
configuration.withNodeAgentSKUId(skuId).withImageReference(imageRef);
client.poolOperations().createPool(poolId, poolVMSize, configuration, poolVMCount);

System.out.println("Created a Pool: " + poolId);
```

**Create a Job**

You can create a job by using the recently created pool.

```java
PoolInformation poolInfo = new PoolInformation();
poolInfo.withPoolId(poolId);
client.jobOperations().createJob(jobId, poolInfo);
```

# Sample Code

You can find sample code that illustrates Batch usage scenarios in https://github.com/azure/azure-batch-samples


# Download


**5.0.0**

If you are using released builds from 5.0.0, add the following to your POM file:

```xml
<dependency>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>azure-batch</artifactId>
    <version>5.0.0</version>
</dependency>
<dependency>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>azure-client-runtime</artifactId>
    <version>1.6.4</version>
</dependency>
```

# Pre-requisites

- A Java Developer Kit (JDK), v 1.7 or later
- Maven
- Azure Service Principal - see [how to create authentication info](./AUTH.md).


## Help

If you encounter any bugs with these libraries, please file issues via [Issues](https://github.com/Azure/azure-batch-sdk-for-java/issues) or checkout [StackOverflow for Azure Java SDK](http://stackoverflow.com/questions/tagged/azure-java-sdk).

# Contribute Code

If you would like to become an active contributor to this project please follow the instructions provided in [Microsoft Azure Projects Contribution Guidelines](http://azure.github.io/guidelines.html).

1. Fork it
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new Pull Request

# Build Code
To build the code open a console, navigate to the git repository, and run
```
maven build
```

# Test Code
To run tests, set the following environment variables:
* AZURE_BATCH_ENDPOINT
* CLIENT_ID
* APPLICATION_SECRET
* AZURE_BATCH_ACCOUNT
* AZURE_BATCH_ACCESS_KEY
* STORAGE_ACCOUNT_NAME
* STORAGE_ACCOUNT_KEY
Then run any test in src/test/java directory.

# More Information
* [Javadoc](http://azure.github.io/azure-sdk-for-java)
* [http://azure.com/java](http://azure.com/java)
* If you don't have a Microsoft Azure subscription you can get a FREE trial account [here](http://go.microsoft.com/fwlink/?LinkId=330212)

**Previous Releases and Corresponding Repo Branches**

| Version           | SHA1                                                                                      | Remarks                                               |
|-------------------|-------------------------------------------------------------------------------------------|-------------------------------------------------------|
| 5.0.0             | [5.0.0](https://github.com/Azure/azure-batch-sdk-for-java/tree/master)                    | Tagged release for 5.0.0 version of Azure Batch libraries |
| 4.0.1             | [4.0.1](https://github.com/Azure/azure-batch-sdk-for-java/releases/tag/v4.0.1)            | Tagged release for 4.0.1 version of Azure Batch libraries |
| 4.0.0             | [4.0.0](https://github.com/Azure/azure-batch-sdk-for-java/releases/tag/v4.0.0)            | Tagged release for 4.0.0 version of Azure Batch libraries |
| 3.2.0             | [3.2.0](https://github.com/Azure/azure-batch-sdk-for-java/releases/tag/v3.2.0)            | Tagged release for 3.2.0 version of Azure Batch libraries |
| 3.1.0             | [3.1.0](https://github.com/Azure/azure-batch-sdk-for-java/releases/tag/v3.1.0)            | Tagged release for 3.1.0 version of Azure Batch libraries |
| 3.0.0             | [3.0.0](https://github.com/Azure/azure-batch-sdk-for-java/releases/tag/v3.0.0)            | Tagged release for 3.0.0 version of Azure Batch libraries |
| 2.1.0             | [2.1.0](https://github.com/Azure/azure-batch-sdk-for-java/releases/tag/v2.1.0)            | Tagged release for 2.1.0 version of Azure Batch libraries |
| 2.0.0             | [2.0.0](https://github.com/Azure/azure-batch-sdk-for-java/releases/tag/v2.0.0)            | Tagged release for 2.0.0 version of Azure Batch libraries |
| 1.0.0             | [1.0.0](https://github.com/Azure/azure-batch-sdk-for-java/releases/tag/v1.0.0)            | Tagged release for 1.0.0 version of Azure Batch libraries |
| 1.0.0-beta2       | [1.0.0-beta2](https://github.com/Azure/azure-sdk-for-java/tree/1.0.0-beta2)               | Tagged release for 1.0.0-beta2 version of Azure Batch libraries |

---

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.
