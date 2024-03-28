# Azure Batch client library for Java

This README is based on the latest released version of the Azure Compute Batch SDK, otherwise known as the track 2 Azure Batch Data Plane SDK.

> The SDK supports features of the Azure Batch service starting from API version **2023-05-01.16.0**. We will be adding support for more new features and tweaking the API associated with Azure Batch service newer release.

## Documentation

Various documentation is available to help you get started

- [API reference documentation][docs]
- [Product documentation][product_documentation]

## Getting started

### Prerequisites

- [Java Development Kit (JDK)][jdk] with version 8 or above
- [Azure Subscription][azure_subscription]

### Adding the package to your product

[//]: # ({x-version-update-start;com.azure:azure-compute-batch;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-compute-batch</artifactId>
    <version>1.0.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Authentication

[Azure Identity][azure_identity] package provides the default implementation for authenticating the client.

## Key concepts
**Azure Batch Authentication**

You need to create a Batch account through the [Azure portal](https://portal.azure.com) or Azure cli.

* Use the account name, key, and URL to create a `BatchSharedKeyCredentials` instance for authentication with the Azure Batch service.
  The `BatchServiceClientBuilder` class is the simplest entry point for creating and interacting with Azure Batch objects.

```
BatchServiceClientBuilder batchClientBuilder = new BatchServiceClientBuilder()
        .endpoint(batchEndpoint)
        .httpClient(HttpClient.createDefault());

BatchSharedKeyCredentials sharedKeyCred = new BatchSharedKeyCredentials(batchEndpoint, accountName, accountKey);

batchClientBuilder.credential(sharedKeyCred);
```

* The other way is using AAD (Azure Active Directory) authentication to create the client. See this [document](https://docs.microsoft.com/azure/batch/batch-aad-auth) for details on authenticating to Batch with AAD and this [document](azure_identity) for understanding how the Azure Identity library supports AAD token authentication in Java.
For example:

```
batchClientBuilder.credential(new DefaultAzureCredentialBuilder().build());
```

**Create a pool using an Azure Marketplace image**

You can create a pool of Azure virtual machines which can be used to execute tasks.

```
System.out.println("Created a pool using an Azure Marketplace image.");

ImageReference imgRef = new ImageReference().setPublisher("Canonical").setOffer("UbuntuServer")
                    .setSku("18.04-LTS").setVersion("latest");

String poolVmSize = "STANDARD_D1_V2";
VirtualMachineConfiguration configuration = new VirtualMachineConfiguration(imgRef, "batch.node.ubuntu 18.04");

BatchPoolCreateParameters poolCreateParameters = new BatchPoolCreateParameters(poolId, poolVmSize);
poolCreateParameters.setTargetDedicatedNodes(1)
        .setVirtualMachineConfiguration(configuration)
        .setUserAccounts(userList)
        .setNetworkConfiguration(networkConfiguration);

batchClientBuilder.buildPoolClient().create(poolCreateParameters);

System.out.println("Created a Pool: " + poolId);
```

**Create a Job**

You can create a job by using the recently created pool.

```
PoolInformation poolInfo = new PoolInformation();
poolInfo.setPoolId(poolId);
BatchJobCreateParameters jobCreateParameters = new BatchJobCreateParameters(jobId, poolInfo);
batchClientBuilder.buildJobClient().create(jobCreateParameters);
```
# Sample Code
//TODO

You can find sample code that illustrates Batch usage scenarios in https://github.com/azure/azure-batch-samples


## Examples
TODO?
```java com.azure.compute.batch.readme
```

## Help

If you encounter any bugs with these libraries, please file issues via [Issues](https://github.com/Azure/azure-sdk-for-java) or checkout [StackOverflow for Azure Java SDK](https://stackoverflow.com/questions/tagged/azure-java-sdk).

## Next steps

## Contributing

For details on contributing to this repository, see the [contributing guide](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md).

1. Fork it
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new Pull Request

# Build Code
To build the code open a console, navigate to the project subdirectory (sdk/batch/azure-compute-batch/), and run
```
mvn install -f pom.xml
```
For more information about building the client library including installing the associated build tools, please see the [Azure Java SDK Building wiki][java_building_wiki]  

# Test Code

All tests are run from the `sdk/batch/azure-compute-batch` directory. They can be run either on the command line or from a Java IDE, such as IntelliJ as Junit (Note that if you wish to run the tests within IntelliJ, you will need to temporarily delete the module-info.java file). 
If you are working on either the src or test code within an IDE, be sure you are also building the client library with Maven commands in the CLI as the build system is configured to target both JDK 8 and 11. Please see the [Build Code section](#build-code).

Tests are run in two phases: Record and Playback. During the first Record phase, integration tests create real Batch resources on Azure using the Batch API, and JSON files are created locally to capture the response from Azure. In the second Playback phase, the integrations tests only exercise assertions against the JSON files themselves. To record sessions locally, several resources need to already exist in Azure:

- A valid Azure subscription that can create resources
- A service principal with contributor access to the subscription. If not already available, create an app registration in "Azure Active Directory". Generate a client secret for this principal
- A clean Batch account
- A storage account
- A virtual network

## Step 1: Run tests in Record mode

1. Deploy test resources in Azure and set the following environment variables:

  * AZURE_CLIENT_SECRET
  * AZURE_TENANT_ID
  * AZURE_BATCH_ACCESS_KEY
  * AZURE_BATCH_ACCOUNT
  * AZURE_BATCH_ENDPOINT
  * AZURE_BATCH_REGION
  * AZURE_VNET
  * AZURE_VNET_ADDRESS_SPACE
  * AZURE_VNET_RESOURCE_GROUP
  * AZURE_VNET_SUBNET
  * AZURE_VNET_SUBNET_ADDRESS_SPACE
  * AZURE_CLIENT_ID
  * STORAGE_ACCOUNT_KEY
  * STORAGE_ACCOUNT_NAME
  * AZURE_SUBSCRIPTION_ID

2. If running as Junit in an IDE, Set the `AZURE_TEST_MODE` environment variable to `Record`, then run the tests in `src/test/java`
3. If running from the command-line, run `mvn test` (can also supply `-DAZURE_TEST_MODE=Record` instead of setting environment variable)
4. Test recordings will be created/modified in `azure-compute-batch/src/test/resources/session-records`

Note: Whether you are running in record or playback mode through mvn, you can also run a specific test file i.e. PoolsTests, JobScheduleTests, etc. by passing the -Dtest parameter such as:
```
mvn test -DAZURE_TEST_MODE=Playback -Dtest=JobScheduleTests
```


## Step 2: Run tests in Playback mode

1. If running as Junit in an IDE, Set the `AZURE_TEST_MODE` environment variable to `Playback`, then run the tests in `src/test/java`
2. If running from the command-line, run `mvn test -DAZURE_TEST_MODE=Playback`

<!-- LINKS -->
[product_documentation]: https://azure.microsoft.com/services/
[docs]: https://azure.github.io/azure-sdk-for-java/
[jdk]: https://docs.microsoft.com/java/azure/jdk/
[azure_subscription]: https://azure.microsoft.com/free/
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity
[java_building_wiki]: https://github.com/Azure/azure-sdk-for-java/wiki/Building
