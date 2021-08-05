# Azure Quantum Jobs client library for Java

Azure Quantum is a Microsoft Azure service that you can use to run quantum computing programs or solve optimization problems in the cloud.  Using the Azure Quantum tools and SDKs, you can create quantum programs and run them against different quantum simulators and machines.  You can use the Azure.Quantum.Jobs client library to:
- Create, enumerate, and cancel quantum jobs
- Enumerate provider status and quotas


[Source code][source] | [API reference documentation](https://docs.microsoft.com/qsharp/api/) | [Product documentation](https://docs.microsoft.com/azure/quantum/)

## Getting started

This section should include everything a developer needs to do to install and create their first client connection *very quickly*.

### Install the package

Install the Azure Quantum Jobs client library for Java by adding the following to your pom.xml file:

[//]: # ({x-version-update-start;com.azure:azure-quantum-jobs;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-quantum-jobs</artifactId>
    <version>1.0.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

**Prerequisites**: You must have an [Azure subscription][azure_subscription], [Azure Quantum workspace][azure_quantum_workspaces], [Azure storage account][azure_storage], and a [Java Development Kit (JDK)][jdk_link] of version 8 or later.

### Authenticate the client

To authenticate with the service, you will have to pass a [`TokenCredential`][token-credential] to the client builder as described [below](#create-the-client).

`TokenCredential` is the default Authentication mechanism used by Azure SDKs.

## Key concepts

`QuantumJobClient` is the root class to be used to authenticate and create, enumerate, and cancel jobs.

`JobDetails` contains all the properties of a job.

`ProviderStatus` contains status information for a provider.

`QuantumJobQuota` contains quota properties.

## Examples

* [Get Container SAS URI](#get-container-sas-uri)
* [Upload Input Data](#upload-input-data)
* [Create The Job](#create-the-job)
* [Get Job](#get-job)
* [List Jobs](#list-jobs)

### Create the client

Create an instance of the client of your choice by passing the following values to `QuantumClientBuilder` and then calling the appropriate build method.
- [Subscription][subscriptions] - looks like XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX and can be found in your list of subscriptions on azure
- [Resource Group][resource-groups] - a container that holds related resources for an Azure solution
- [Workspace][workspaces] - a collection of assets associated with running quantum or optimization applications
- [Host][location] - the host endpoint is "https://{location}.quantum.azure.com". Choose the best data center location by geographical region
- [StorageContainerName][blob-storage] - your blob storage
- [Credential][credentials] - used to authenticate


<!-- embedme ./src/samples/java/com/azure/quantum/jobs/ReadmeSamples.java#L37-L51 -->
```java
JobsClient jobsClient = new QuantumClientBuilder()
    .credential(new DefaultAzureCredentialBuilder().build())
    .host("{endpoint}")
    .subscriptionId("{subscriptionId}")
    .resourceGroupName("{resourceGroup}")
    .workspaceName("{workspaceName}")
    .buildJobsClient();

StorageClient storageClient = new QuantumClientBuilder()
    .credential(new DefaultAzureCredentialBuilder().build())
    .host("{endpoint}")
    .subscriptionId("{subscriptionId}")
    .resourceGroupName("{resourceGroup}")
    .workspaceName("{workspaceName}")
    .buildStorageClient();
```

### Get Container SAS URI

Create a storage container to put your data in.

<!-- embedme ./src/samples/java/com/azure/quantum/jobs/ReadmeSamples.java#L58-L73 -->
```java
// Get container URI with SAS key
String containerName = "{storageContainerName}";

// Create container if it doesn't already exist
BlobContainerClient containerClient = new BlobContainerClientBuilder()
    .containerName(containerName)
    .endpoint(containerUri)
    .buildClient();
if (!containerClient.exists()) {
    containerClient.create();
}

// Get connection string to the container
String containerUri = storageClient.sasUri(
    new BlobDetails().setContainerName(containerName)
).getSasUri();
```

### Upload Input Data

Using the SAS URI, upload the json input data to the blob client.
This contains the parameters to be used with [Quantum Inspired Optimizations](https://docs.microsoft.com/azure/quantum/optimization-overview-introduction)

<!-- embedme ./src/samples/java/com/azure/quantum/jobs/ReadmeSamples.java#L80-L92 -->
```java
// Get input data blob Uri with SAS key
String blobName = "{blobName}";
BlobDetails blobDetails = new BlobDetails()
    .setContainerName(containerName)
    .setBlobName(blobName);
String inputDataUri = storageClient.sasUri(blobDetails).getSasUri();

// Upload input data to blob
BlobClient blobClient = new BlobClientBuilder()
    .endpoint(inputDataUri)
    .buildClient();
String problemFilePath = FileSystems.getDefault().getPath("src/samples/resources/problem.json").toString();
blobClient.uploadFromFile(problemFilePath);
```
### Create The Job

Now that you've uploaded your problem definition to Azure Storage, you can use the `create()` method in `JobsClient` or `JobsAsyncClient`, or the `createWithResponse()` method in `JobsAsyncClient` to define an Azure Quantum job.

<!-- embedme ./src/samples/java/com/azure/quantum/jobs/ReadmeSamples.java#L99-L108 -->
```java
String jobId = String.format("job-%s", UUID.randomUUID());
JobDetails createJobDetails = new JobDetails()
    .setContainerUri(containerUri)
    .setId(jobId)
    .setInputDataFormat("microsoft.qio.v2")
    .setOutputDataFormat("microsoft.qio-results.v2")
    .setProviderId("microsoft")
    .setTarget("microsoft.paralleltempering-parameterfree.cpu")
    .setName("{jobName}");
JobDetails jobDetails = jobsClient.create(jobId, createJobDetails);
```

### Get Job

To retrieve a specific job by its ID, you can use `get()` from `JobsClient` or `JobsAsyncClient`, or `getWithResponse()` in `JobsAsyncClient`.

<!-- embedme ./src/samples/java/com/azure/quantum/jobs/ReadmeSamples.java#L115-L116 -->
```java
// Get the job that we've just created based on its jobId
JobDetails myJob = jobsClient.get(jobId);
```

### List Jobs

To enumerate all the jobs in the workspace, use the `list()` method from `JobClient` or `JobAsyncClient`, or from `JobAsyncClient` use `listSinglePage()` or `listNextPage()`.

<!-- embedme ./src/samples/java/com/azure/quantum/jobs/ReadmeSamples.java#L123-L126 -->
```java
PagedIterable<JobDetails> jobs = jobsClient.list();
jobs.forEach(job -> {
    System.out.println(job.getName());
});
```

## Troubleshooting

All Quantum Jobs service operations will throw a RequestFailedException on failure with helpful ErrorCodes. Many of these errors are recoverable.

## Next steps

*  Visit our [Product documentation](https://docs.microsoft.com/azure/quantum/) to learn more about Azure Quantum.

## Contributing

See the [CONTRIBUTING.md][contributing] for details on building,
testing, and contributing to this library.

This project welcomes contributions and suggestions.  Most contributions require
you to agree to a Contributor License Agreement (CLA) declaring that you have
the right to, and actually do, grant us the rights to use your contribution. For
details, visit [cla.microsoft.com][cla].

This project has adopted the [Microsoft Open Source Code of Conduct][coc].
For more information see the [Code of Conduct FAQ][coc_faq]
or contact [opencode@microsoft.com][coc_contact] with any
additional questions or comments.

<!-- LINKS -->
[source]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/quantum/azure-quantum-jobs/src
[style-guide-msft]: https://docs.microsoft.com/style-guide/capitalization
[token-credential]: https://docs.microsoft.com/dotnet/api/azure.core.tokencredential?view=azure-dotnet
[resource-groups]: https://docs.microsoft.com/azure/azure-resource-manager/management/manage-resource-groups-portal
[workspaces]: https://docs.microsoft.com/azure/quantum/how-to-create-quantum-workspaces-with-the-azure-portal
[location]: https://azure.microsoft.com/global-infrastructure/services/?products=quantum
[blob-storage]: https://docs.microsoft.com/azure/storage/blobs/storage-blobs-introduction
[contributing]: https://github.com/Azure/azure-sdk-for-net/tree/main/CONTRIBUTING.md
[subscriptions]: https://ms.portal.azure.com/#blade/Microsoft_Azure_Billing/SubscriptionsBlade
[credentials]: https://docs.microsoft.com/dotnet/api/overview/azure/identity-readme#credentials
[style-guide-msft]: https://docs.microsoft.com/style-guide/capitalization
[style-guide-cloud]: https://aka.ms/azsdk/cloud-style-guide
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
[azure_subscription]: https://azure.microsoft.com/free
[azure_quantum]: https://azure.microsoft.com/services/quantum/
[azure_quantum_workspaces]: https://docs.microsoft.com/azure/quantum/how-to-create-quantum-workspaces-with-the-azure-portal
[azure_storage]: https://azure.microsoft.com/free/storage/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_contact]: mailto:opencode@microsoft.com

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Ftemplate%2Fazure-sdk-template%2FREADME.png)
