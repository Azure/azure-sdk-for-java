## Azure Batch Libraries for Java

This README is based on the latest released version Azure Batch SDK (7.0.0). If you are looking for other releases, see the [More Information](#more-information) section below.

The Azure Batch Libraries for Java is a higher-level, object-oriented API for interacting with the Azure Batch service.


> **7.0.0** is a release that supports all features of Azure Batch service with API version "2019-08-01.10.0". We will be adding support for more new features and tweaking the API associated with Azure Batch service newer release.

**Azure Batch Authentication**

You need to create a Batch account through the [Azure portal](https://portal.azure.com) or Azure cli.

* Use the account name, key, and URL to create a `BatchSharedKeyCredentials` instance for authentication with the Azure Batch service.
The `BatchClient` class is the simplest entry point for creating and interacting with Azure Batch objects.

```java
BatchSharedKeyCredentials cred = new BatchSharedKeyCredentials(batchUri, batchAccount, batchKey);
BatchClient client = BatchClient.open(cred);
```

* The other way is using AAD (Azure Active Directory) authentication to create the client. See this [document](https://docs.microsoft.com/azure/batch/batch-aad-auth) for detail.

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

[//]: # ({x-version-update-start;com.microsoft.azure:azure-batch;current})
```xml
<dependency>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>azure-batch</artifactId>
    <version>8.1.0</version>
</dependency>
```
[//]: # ({x-version-update-end})

# Pre-requisites

- [A Java Developer Kit (JDK)](https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable), v 1.7 or later
- [Maven](https://search.maven.org/artifact/com.microsoft.azure/azure-batch)
- Azure Service Principal - see [how to create authentication info](https://docs.microsoft.com/azure/batch/batch-aad-auth#use-a-service-principal).


## Help

If you encounter any bugs with these libraries, please file issues via [Issues](https://github.com/Azure/azure-sdk-for-java) or checkout [StackOverflow for Azure Java SDK](https://stackoverflow.com/questions/tagged/azure-java-sdk).

# Contribute Code

For details on contributing to this repository, see the [contributing guide](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md).

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

All tests are run from the `sdk/batch` directory. They can be run either on the command line or from a Java IDE, such as Eclipse.

## Step 1: Run tests in Record mode

1. Deploy test resources in Azure and set the following environment variables:

    * APPLICATION_SECRET
    * AZURE_BATCH_ACCESS_KEY
    * AZURE_BATCH_ACCOUNT
    * AZURE_BATCH_ENDPOINT
    * AZURE_BATCH_REGION
    * AZURE_VNET
    * AZURE_VNET_ADDRESS_SPACE
    * AZURE_VNET_RESOURCE_GROUP
    * AZURE_VNET_SUBNET
    * AZURE_VNET_SUBNET_ADDRESS_SPACE
    * CLIENT_ID
    * STORAGE_ACCOUNT_KEY
    * STORAGE_ACCOUNT_NAME
    * SUBSCRIPTION_ID

1. Set `AZURE_TEST_MODE` to `Record`
1. Run the tests in `src/test/java`
    1. From the command-line, run `mvn test` (can also supply `-DAZURE_TEST_MODE=Record` instead of setting environment variable)
1. Test recordings will be created in `microsoft-azure-batch/target/test-classes/session-records`
1. Copy these recordings to `microsoft-azure-batch/src/test/resources/test-recordings`

## Step 2: Run tests in Playback mode

1. Set `AZURE_TEST_MODE` to `Playback`
1. Run the Jetty test server
    1. CLI: `mvn jetty:start`
    1. Eclipse: Install Jetty plugin for Eclipse from marketplace and create two run configurations (one for 11080 and one for 11081)
1. Run the tests
    1. CLI: `mvn test -DAZURE_TEST_MODE=Playback`

# More Information

* [Javadoc](https://docs.microsoft.com/java/api/overview/azure/batch?view=azure-java-stable)
* [https://azure.com/java](https://azure.com/java)
* If you don't have a Microsoft Azure subscription you can get a FREE trial account [here](https://go.microsoft.com/fwlink/?LinkId=330212)

---

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fbatch%2Fmicrosoft-azure-batch%2FREADME.png)
