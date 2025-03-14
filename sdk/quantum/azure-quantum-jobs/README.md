# Azure Quantum Jobs client library for Java

Azure Quantum is a Microsoft Azure service that you can use to run quantum computing programs in the cloud.  Using the Azure Quantum tools and SDKs, you can create quantum programs and run them against different quantum simulators and machines.  You can use the Azure.Quantum.Jobs client library to:
- Create, enumerate, and cancel quantum jobs
- Enumerate provider status and quotas


[Source code][source] | [API reference documentation](https://azure.github.io/azure-sdk-for-java/) | [Product documentation](https://learn.microsoft.com/azure/quantum/) | [Samples][samples]

## Getting started

This section should include everything a developer needs to do to install and create their first client connection *very quickly*.

### Install the package

Install the Azure Quantum Jobs client library for Java by adding the following to your pom.xml file:

[//]: # ({x-version-update-start;com.azure:azure-quantum-jobs;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-quantum-jobs</artifactId>
    <version>1.0.0-beta.2</version>
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

* [Create the client](#create-the-client)
* [Get Container SAS URI](#get-container-sas-uri)
* [Upload Input Data](#upload-input-data)
* [Create The Job](#create-the-job)
* [Get Job](#get-job)
* [List Jobs](#list-jobs)

### Create the client

Create an instance of the client of your choice by passing the following values to `QuantumClientBuilder` and then calling the appropriate build method.
- [Subscription][subscriptions] - looks like XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX and can be found in your list of subscriptions on azure
- [Resource Group][resource-groups] - a container that holds related resources for an Azure solution
- [Workspace][workspaces] - a collection of assets associated with running quantum
- [Host][location] - the host endpoint is "https://{location}.quantum.azure.com". Choose the best data center location by geographical region
- [StorageContainerName][blob-storage] - your blob storage
- [Credential][credentials] - used to authenticate

```java readme-sample-getClients
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

```java readme-sample-getContainerSasUri
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

### Compile your quantum program into QIR

This step can be done in multiple ways and it is not in scope for this sample.

[Quantum Intermediate Representation (QIR)](https://github.com/qir-alliance/qir-spec) is a [QIR Alliance](https://www.qir-alliance.org/) specification to represent quantum programs within the [LLVM](https://llvm.org/) Intermediate Representation (IR).

A few methods to compile or generate a quantum program into QIR:
- [Q# compiler](https://github.com/microsoft/qsharp-compiler/): Can be used to [compile Q# Code into QIR](https://github.com/microsoft/qsharp-compiler/tree/main/src/QsCompiler/QirGeneration).
- [PyQIR](https://github.com/qir-alliance/pyqir): PyQIR is a set of APIs for generating, parsing, and evaluating Quantum Intermediate Representation (QIR).
- [IQ#](https://github.com/microsoft/iqsharp): Can be used to compile a Q# program into QIR with the [%qir](https://learn.microsoft.com/qsharp/api/iqsharp-magic/qir) magic command.

In this sample, we assume you already have a file with the QIR bitcode and you know the method name that you want to execute (entry point).

We will use the QIR bitcode sample (`BellState.bc` in the samples folde), compiled a Q# code (`BellState.qs` in the samples folder) targeting the `quantinuum.sim.h1-1e` target, with `AdaptiveExecution` target capability.

### Upload Input Data

Using the SAS URI, upload the QIR bitcode input data to the blob client.

```java readme-sample-uploadInputData
// Get input data blob Uri with SAS key
String blobName = "{blobName}";
BlobDetails blobDetails = new BlobDetails()
    .setContainerName(containerName)
    .setBlobName(blobName);
BlobHttpHeaders blobHttpHeaders = new BlobHttpHeaders()
    .setContentType("qir.v1");
String inputDataUri = storageClient.sasUri(blobDetails).getSasUri();

// Upload input data to blob
BlobClient blobClient = new BlobClientBuilder()
    .endpoint(inputDataUri)
    .buildClient();
String qirFilePath = FileSystems.getDefault().getPath("src/samples/java/com/azure/quantum/jobs/BellState.bc").toString();
blobClient.uploadFromFile(qirFilePath, null, blobHttpHeaders, null, null, null, null);
```

### Create The Job

Now that you've uploaded your QIR program bitcode to Azure Storage, you can use the `create()` method in `JobsClient` or `JobsAsyncClient`, or the `createWithResponse()` method in `JobsAsyncClient` to submit an Azure Quantum job.

```java readme-sample-createTheJob
String jobId = String.format("job-%s", UUID.randomUUID());
Map<String, Object> inputParams = new HashMap<String, Object>();
inputParams.put("entryPoint", "ENTRYPOINT__BellState");
inputParams.put("arguments", new ArrayList<String>());
inputParams.put("targetCapability", "AdaptiveExecution");
JobDetails createJobDetails = new JobDetails()
    .setContainerUri(containerUri)
    .setId(jobId)
    .setInputDataFormat("qir.v1")
    .setOutputDataFormat("microsoft.quantum-results.v1")
    .setProviderId("quantinuum")
    .setTarget("quantinuum.sim.h1-1e")
    .setName("{jobName}")
    .setInputParams(inputParams);
JobDetails jobDetails = jobsClient.create(jobId, createJobDetails);
```

### Get Job

To retrieve a specific job by its ID, you can use `get()` from `JobsClient` or `JobsAsyncClient`, or `getWithResponse()` in `JobsAsyncClient`.

```java readme-sample-getJob
// Get the job that we've just created based on its jobId
JobDetails myJob = jobsClient.get(jobId);
```

### List Jobs

To enumerate all the jobs in the workspace, use the `list()` method from `JobClient` or `JobAsyncClient`, or from `JobAsyncClient` use `listSinglePage()` or `listNextPage()`.

```java readme-sample-listJobs
PagedIterable<JobDetails> jobs = jobsClient.list();
jobs.forEach(job -> System.out.println(job.getName()));
```

## Troubleshooting

All Quantum Jobs service operations will throw a RequestFailedException on failure with helpful ErrorCodes. Many of these errors are recoverable.

## Next steps

*  Visit our [Product documentation](https://learn.microsoft.com/azure/quantum/) to learn more about Azure Quantum.

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
[style-guide-msft]: https://learn.microsoft.com/style-guide/capitalization
[token-credential]: https://learn.microsoft.com/dotnet/api/azure.core.tokencredential?view=azure-dotnet
[resource-groups]: https://learn.microsoft.com/azure/azure-resource-manager/management/manage-resource-groups-portal
[workspaces]: https://learn.microsoft.com/azure/quantum/how-to-create-quantum-workspaces-with-the-azure-portal
[location]: https://azure.microsoft.com/global-infrastructure/services/?products=quantum
[blob-storage]: https://learn.microsoft.com/azure/storage/blobs/storage-blobs-introduction
[contributing]: https://github.com/Azure/azure-sdk-for-java/tree/main/CONTRIBUTING.md
[subscriptions]: https://ms.portal.azure.com/#blade/Microsoft_Azure_Billing/SubscriptionsBlade
[credentials]: https://learn.microsoft.com/dotnet/api/overview/azure/identity-readme#credentials
[style-guide-msft]: https://learn.microsoft.com/style-guide/capitalization
[style-guide-cloud]: https://aka.ms/azsdk/cloud-style-guide
[jdk_link]: https://learn.microsoft.com/java/azure/jdk/?view=azure-java-stable
[azure_subscription]: https://azure.microsoft.com/free
[azure_quantum]: https://azure.microsoft.com/services/quantum/
[azure_quantum_workspaces]: https://learn.microsoft.com/azure/quantum/how-to-create-quantum-workspaces-with-the-azure-portal
[azure_storage]: https://azure.microsoft.com/free/storage/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_contact]: mailto:opencode@microsoft.com
[samples]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/quantum/azure-quantum-jobs/src/samples/java/com/azure/quantum/jobs


