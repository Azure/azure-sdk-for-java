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

You can authenticate with Microsoft Entra ID authentication.

## Key concepts

### Azure Batch Authentication

You need to create a Batch account through the [Azure portal](https://portal.azure.com) or Azure cli.

- Use the account name, key, and URL to create a `BatchSharedKeyCredentials` instance for authentication with the Azure Batch service.
  The `BatchServiceClientBuilder` class is the simplest entry point for creating and interacting with Azure Batch objects.

```java
BatchServiceClientBuilder batchClientBuilder = new BatchServiceClientBuilder()
        .endpoint(batchEndpoint)
        .httpClient(HttpClient.createDefault());

BatchSharedKeyCredentials sharedKeyCred = new BatchSharedKeyCredentials(batchEndpoint, accountName, accountKey);

batchClientBuilder.credential(sharedKeyCred);
```

- The other way is using Entra ID authentication to create the client. See this [document](https://docs.microsoft.com/azure/batch/batch-aad-auth) for details on authenticating to Batch with Entra ID.
For example:

```java
batchClientBuilder.credential(new DefaultAzureCredentialBuilder().build());
```

### Create a pool using an Azure Marketplace image

You can create a pool of Azure virtual machines which can be used to execute tasks.

```java
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

### Create a Job

You can create a job by using the recently created pool.

```java
PoolInformation poolInfo = new PoolInformation();
poolInfo.setPoolId(poolId);
BatchJobCreateParameters jobCreateParameters = new BatchJobCreateParameters(jobId, poolInfo);
batchClientBuilder.buildJobClient().create(jobCreateParameters);
```

## Sample Code

You can find sample code that illustrates Batch usage scenarios in <https://github.com/azure/azure-batch-samples>

## Examples

Create a pool with 3 Small VMs

```java
String poolId = "ExamplePoolId";

String poolVmSize = "STANDARD_D1_V2";
int poolVmCount = 2;
int poolLowPriVmCount = 2;

// 10 minutes
long poolSteadyTimeoutInMilliseconds = 10 * 60 * 1000;

// Create pool if it doesn't exist
if (!batchClient.poolExists(poolId)) {
    ImageReference imgRef = new ImageReference().setPublisher("Canonical").setOffer("UbuntuServer")
        .setSku("18.04-LTS").setVersion("latest");

    VirtualMachineConfiguration configuration = new VirtualMachineConfiguration(imgRef, "batch.node.ubuntu 18.04");

    NetworkConfiguration netConfig = createNetworkConfiguration();
    List<InboundNATPool> inbounds = new ArrayList<>();
    inbounds.add(new InboundNATPool("testinbound", InboundEndpointProtocol.TCP, 5000, 60000, 60040));

    BatchPoolEndpointConfiguration endpointConfig = new BatchPoolEndpointConfiguration(inbounds);
    netConfig.setEndpointConfiguration(endpointConfig);

    BatchPoolCreateContent poolToCreate = new BatchPoolCreateContent(poolId, poolVmSize);
    poolToCreate.setTargetDedicatedNodes(poolVmCount)
        .setTargetLowPriorityNodes(poolLowPriVmCount)
        .setVirtualMachineConfiguration(configuration).setNetworkConfiguration(netConfig)
        .setTargetNodeCommunicationMode(BatchNodeCommunicationMode.DEFAULT);

    batchClient.createPool(poolToCreate);
}
```

Create a job

```java
String jobId = "ExampleJobId";

BatchPoolInfo poolInfo = new BatchPoolInfo();
poolInfo.setPoolId(poolId);
BatchJobCreateContent jobToCreate = new BatchJobCreateContent(jobId, poolInfo);

batchClient.createJob(jobToCreate);
```

Create a task

```java
String taskId = "ExampleTaskId";
BatchTaskCreateContent taskToCreate = new BatchTaskCreateContent(taskId, "echo hello world");
batchClient.createTask(jobId, taskToCreate);
```

## Help

If you encounter any bugs with these libraries, please file issues via [Issues](https://github.com/Azure/azure-sdk-for-java) or check out [StackOverflow for Azure Java SDK](https://stackoverflow.com/questions/tagged/azure-java-sdk).

## Troubleshooting

Consult the Full Documentation: The full documentation is available at <https://learn.microsoft.com/azure/batch/>.

Check the Error Code and Consult Documentation: The Batch service utilizes specific error codes that may be returned in the error response when a request fails due to various reasons. For a comprehensive list of error codes, their meanings, and detailed troubleshooting steps, refer to the Azure Batch service error codes documentation: <https://learn.microsoft.com/rest/api/batchservice/batch-status-and-error-codes>

Review Your Request Parameters: Errors such as InvalidPropertyValue and MissingRequiredProperty indicate that there might be a mistake in the request payload. Review your parameters to ensure they meet the API specifications.

Manage Resources: For errors related to limits and quotas (like AccountCoreQuotaReached), consider scaling down your usage or requesting an increase in your quota.

Check Azure Service Health: Sometimes, the issue may be with Azure services rather than your application. Check the Azure Status Dashboard for any ongoing issues that might be affecting Batch services.

Handle Transient Errors: Implement retry logic in your application to handle transient failures in Batch.

## Next steps

## Contributing

For details on contributing to this repository, see the [contributing guide](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md).

1. Fork it
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new Pull Request

## Build Code

To build the code open a console, navigate to the project subdirectory (sdk/batch/azure-compute-batch/), and run

```java
mvn install -f pom.xml
```

For more information about building the client library including installing the associated build tools, please see the [Azure Java SDK Building wiki][java_building_wiki]  

## Test Code

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

    - AZURE_CLIENT_SECRET
    - AZURE_TENANT_ID
    - AZURE_BATCH_ACCESS_KEY
    - AZURE_BATCH_ACCOUNT
    - AZURE_BATCH_ENDPOINT
    - AZURE_BATCH_REGION
    - AZURE_VNET
    - AZURE_VNET_ADDRESS_SPACE
    - AZURE_VNET_RESOURCE_GROUP
    - AZURE_VNET_SUBNET
    - AZURE_VNET_SUBNET_ADDRESS_SPACE
    - AZURE_CLIENT_ID
    - STORAGE_ACCOUNT_KEY
    - STORAGE_ACCOUNT_NAME
    - AZURE_SUBSCRIPTION_ID

2. If running as Junit in an IDE, Set the `AZURE_TEST_MODE` environment variable to `Record`, then run the tests in `src/test/java`
3. If running from the command-line, run `mvn test` (can also supply `-DAZURE_TEST_MODE=Record` instead of setting environment variable)
4. Test recordings will be created/modified in `azure-compute-batch/src/test/resources/session-records`

Note: Whether you are running in record or playback mode through mvn, you can also run a specific test file i.e. PoolsTests, JobScheduleTests, etc. by passing the -Dtest parameter such as:

```java
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
[java_building_wiki]: https://github.com/Azure/azure-sdk-for-java/wiki/Building
