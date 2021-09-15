# Azure Resource Manager DataFactory client library for Java

Azure Resource Manager DataFactory client library for Java.

This package contains Microsoft Azure SDK for DataFactory Management SDK. The Azure Data Factory V2 management API provides a RESTful set of web services that interact with Azure Data Factory V2 services. Package tag package-2018-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

## We'd love to hear your feedback

We're always working on improving our products and the way we communicate with our users. So we'd love to learn what's working and how we can do better.

If you haven't already, please take a few minutes to [complete this short survey][survey] we have put together.

Thank you in advance for your collaboration. We really appreciate your time!

## Documentation

Various documentation is available to help you get started

- [API reference documentation][docs]

## Getting started

### Prerequisites

- [Java Development Kit (JDK)][jdk] with version 8 or above
- [Azure Subscription][azure_subscription]

### Adding the package to your product

[//]: # ({x-version-update-start;com.azure.resourcemanager:azure-resourcemanager-datafactory;current})
```xml
<dependency>
    <groupId>com.azure.resourcemanager</groupId>
    <artifactId>azure-resourcemanager-datafactory</artifactId>
    <version>1.0.0-beta.6</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Include the recommended packages

Azure Management Libraries require a `TokenCredential` implementation for authentication and an `HttpClient` implementation for HTTP client.

[Azure Identity][azure_identity] package and [Azure Core Netty HTTP][azure_core_http_netty] package provide the default implementation.

### Authentication

By default, Azure Active Directory token authentication depends on correct configure of following environment variables.

- `AZURE_CLIENT_ID` for Azure client ID.
- `AZURE_TENANT_ID` for Azure tenant ID.
- `AZURE_CLIENT_SECRET` or `AZURE_CLIENT_CERTIFICATE_PATH` for client secret or client certificate.

In addition, Azure subscription ID can be configured via environment variable `AZURE_SUBSCRIPTION_ID`.

With above configuration, `azure` client can be authenticated by following code:

```java
AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
TokenCredential credential = new DefaultAzureCredentialBuilder()
    .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
    .build();
DataFactoryManager manager = DataFactoryManager
    .authenticate(credential, profile);
```

The sample code assumes global Azure. Please change `AzureEnvironment.AZURE` variable if otherwise.

See [Authentication][authenticate] for more options.

## Key concepts

See [API design][design] for general introduction on design and key concepts on Azure Management Libraries.

## Examples

```java
// storage account
StorageAccount storageAccount = storageManager.storageAccounts().define(STORAGE_ACCOUNT)
    .withRegion(REGION)
    .withExistingResourceGroup(RESOURCE_GROUP)
    .create();
final String storageAccountKey = storageAccount.getKeys().iterator().next().value();
final String connectionString = getStorageConnectionString(STORAGE_ACCOUNT, storageAccountKey, storageManager.environment());

// container
final String containerName = "adf";
storageManager.blobContainers().defineContainer(containerName)
    .withExistingBlobService(RESOURCE_GROUP, STORAGE_ACCOUNT)
    .withPublicAccess(PublicAccess.NONE)
    .create();

// blob as input
BlobClient blobClient = new BlobClientBuilder()
    .connectionString(connectionString)
    .containerName(containerName)
    .blobName("input/data.txt")
    .buildClient();
blobClient.upload(BinaryData.fromString("data"));

// data factory
manager.factories().define(DATA_FACTORY)
    .withRegion(REGION)
    .withExistingResourceGroup(RESOURCE_GROUP)
    .create();

// linked service
final Map<String, String> connectionStringProperty = new HashMap<>();
connectionStringProperty.put("type", "SecureString");
connectionStringProperty.put("value", connectionString);

final String linkedServiceName = "LinkedService";
manager.linkedServices().define(linkedServiceName)
    .withExistingFactory(RESOURCE_GROUP, DATA_FACTORY)
    .withProperties(new AzureStorageLinkedService()
        .withConnectionString(connectionStringProperty))
    .create();

// input dataset
final String inputDatasetName = "InputDataset";
manager.datasets().define(inputDatasetName)
    .withExistingFactory(RESOURCE_GROUP, DATA_FACTORY)
    .withProperties(new AzureBlobDataset()
        .withLinkedServiceName(new LinkedServiceReference().withReferenceName(linkedServiceName))
        .withFolderPath(containerName)
        .withFileName("input/data.txt")
        .withFormat(new TextFormat()))
    .create();

// output dataset
final String outputDatasetName = "OutputDataset";
manager.datasets().define(outputDatasetName)
    .withExistingFactory(RESOURCE_GROUP, DATA_FACTORY)
    .withProperties(new AzureBlobDataset()
        .withLinkedServiceName(new LinkedServiceReference().withReferenceName(linkedServiceName))
        .withFolderPath(containerName)
        .withFileName("output/data.txt")
        .withFormat(new TextFormat()))
    .create();

// pipeline
PipelineResource pipeline = manager.pipelines().define("CopyBlobPipeline")
    .withExistingFactory(RESOURCE_GROUP, DATA_FACTORY)
    .withActivities(Collections.singletonList(new CopyActivity()
        .withName("CopyBlob")
        .withSource(new BlobSource())
        .withSink(new BlobSink())
        .withInputs(Collections.singletonList(new DatasetReference().withReferenceName(inputDatasetName)))
        .withOutputs(Collections.singletonList(new DatasetReference().withReferenceName(outputDatasetName)))))
    .create();

// run pipeline
CreateRunResponse createRun = pipeline.createRun();

// wait for completion
PipelineRun pipelineRun = manager.pipelineRuns().get(RESOURCE_GROUP, DATA_FACTORY, createRun.runId());
String runStatus = pipelineRun.status();
while ("InProgress".equals(runStatus)) {
    sleepIfRunningAgainstService(10 * 1000);    // wait 10 seconds
    pipelineRun = manager.pipelineRuns().get(RESOURCE_GROUP, DATA_FACTORY, createRun.runId());
    runStatus = pipelineRun.status();
}
```
[Code snippets and samples](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/datafactory/azure-resourcemanager-datafactory/SAMPLE.md)


## Troubleshooting

## Next steps

## Contributing

For details on contributing to this repository, see the [contributing guide](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md).

1. Fork it
1. Create your feature branch (`git checkout -b my-new-feature`)
1. Commit your changes (`git commit -am 'Add some feature'`)
1. Push to the branch (`git push origin my-new-feature`)
1. Create new Pull Request

<!-- LINKS -->
[survey]: https://microsoft.qualtrics.com/jfe/form/SV_ehN0lIk2FKEBkwd?Q_CHL=DOCS
[docs]: https://azure.github.io/azure-sdk-for-java/
[jdk]: https://docs.microsoft.com/java/azure/jdk/
[azure_subscription]: https://azure.microsoft.com/free/
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity
[azure_core_http_netty]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-http-netty
[authenticate]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/docs/AUTH.md
[design]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/docs/DESIGN.md
