[![Build Status](https://travis-ci.org/Azure/azure-batch-sdk-for-java.svg?style=flat-square&label=build)](https://travis-ci.org/Azure/azure-batch-sdk-for-java)

#Azure Batch Libraries for Java

This README is based on the latest released preview version Azure Batch SDK (1.0.0-beta2). If you are looking for other releases, see [More Information](#more-information)

The Azure Batch Libraries for Java is a higher-level, object-oriented API for interacting with the Azure Batch service.


> **1.0.0-beta2** is a developer preview that supports major parts of Azure Batch. The next preview version of the Azure Batch Libraries for Java is a work in-progress. We will be adding support for more new featuresand tweaking the API over the next few months.

**Azure Batch Authentication**

You need to create a Batch account through the [Azure portal](https://portal.azure.com) or Azure cli. Use the account name, key, and URL to create a `BatchSharedKeyCredentials` instance for authentication with the Azure Batch service.
The `BatchClient` class is the simplest entry point for creating and interacting with Azure Batch objects.

```java
BatchSharedKeyCredentials cred = new BatchSharedKeyCredentials(batchUri, batchAccount, batchKey);
BatchClient client = BatchClient.Open(cred);
```
 
**Create a pool using an Azure Marketplace image**

You can create a pool of Azure virtual machines which can be used to execute tasks.

```java
System.out.println("Created a pool using an Azure Marketplace image.");

VirtualMachineConfiguration configuration = new VirtualMachineConfiguration();
configuration.setNodeAgentSKUId(skuId).setImageReference(imageRef);
client.getPoolOperations().createPool(poolId, poolVMSize, configuration, poolVMCount);

System.out.println("Created a Pool: " + poolId);
```

**Create a Job**

You can create a job by using the recently created pool.

```java
PoolInformation poolInfo = new PoolInformation();
poolInfo.setPoolId(poolId);
client.getJobOperations().createJob(jobId, poolInfo);
```

#Sample Code

You can find sample code that illustrates Batch usage scenarios in https://github.com/azure/azure-batch-samples


# Download


**1.0.0-beta2**

If you are using released builds from 1.0.0-beta2, add the following to your POM file:

```xml
<dependency>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>azure-batch</artifactId>
    <version>1.0.0-beta2</version>
</dependency>
<dependency>
    <groupId>com.microsoft.rest</groupId>
    <artifactId>client-runtime</artifactId>
    <version>1.0.0-beta2</version>
</dependency>
<dependency>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>azure-client-runtime</artifactId>
    <version>1.0.0-beta2</version>
</dependency>
```

#Pre-requisites

- A Java Developer Kit (JDK), v 1.7 or later
- Maven
- Azure Service Principal - see [how to create authentication info](./AUTH.md).


## Help

If you encounter any bugs with these libraries, please file issues via [Issues](https://github.com/Azure/azure-batch-sdk-for-java/issues) or checkout [StackOverflow for Azure Java SDK](http://stackoverflow.com/questions/tagged/azure-java-sdk).

#Contribute Code

If you would like to become an active contributor to this project please follow the instructions provided in [Microsoft Azure Projects Contribution Guidelines](http://azure.github.io/guidelines.html).

1. Fork it
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new Pull Request

#More Information
* [Javadoc](http://azure.github.io/azure-sdk-for-java)
* [http://azure.com/java](http://azure.com/java)
* If you don't have a Microsoft Azure subscription you can get a FREE trial account [here](http://go.microsoft.com/fwlink/?LinkId=330212)

**Previous Releases and Corresponding Repo Branches**

| Version           | SHA1                                                                                      | Remarks                                               |
|-------------------|-------------------------------------------------------------------------------------------|-------------------------------------------------------|
| 1.0.0-beta2       | [1.0.0-beta2](https://github.com/Azure/azure-sdk-for-java/tree/1.0.0-beta2)               | Tagged release for 1.0.0-beta2 version of Azure management libraries |
| 1.0.0-beta1       | [1.0.0-beta1](https://github.com/Azure/azure-sdk-for-java/tree/1.0.0-beta1)               | Maintenance branch for AutoRest generated raw clients |
| 1.0.0-beta1+fixes | [v1.0.0-beta1+fixes](https://github.com/Azure/azure-sdk-for-java/tree/v1.0.0-beta1+fixes) | Stable build for AutoRest generated raw clients       |
| 0.9.x-SNAPSHOTS   | [0.9](https://github.com/Azure/azure-sdk-for-java/tree/0.9)                               | Maintenance branch for service management libraries   |
| 0.9.3             | [v0.9.3](https://github.com/Azure/azure-sdk-for-java/tree/v0.9.3)                         | Latest release for service management libraries       |

---

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.
