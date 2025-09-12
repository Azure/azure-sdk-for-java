# Azure Health Data Services de-identification service client library for Java

This package contains a client library for the de-identification service in Azure Health Data Services which 
enables users to tag, redact, or surrogate health data containing Protected Health Information (PHI).
For more on service functionality and important usage considerations, see [the de-identification service overview][product_documentation].

## Getting started

### Prerequisites

- Install the [Java Development Kit (JDK)][jdk] with version 8 or above.
- Have an [Azure Subscription][azure_subscription].
- [Deploy the de-identification service][deid_quickstart].
- [Configure Azure role-based access control (RBAC)][deid_rbac] for the operations you will perform.

### Adding the package to your product

[//]: # ({x-version-update-start;com.azure:azure-health-deidentification;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-health-deidentification</artifactId>
    <version>1.0.0</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Authentication
Both the asynchronous and synchronous clients can be created by using `DeidentificationClientBuilder`. Invoking `buildClient`
will create the synchronous client, while invoking `buildAsyncClient` will create its asynchronous counterpart.

You will need a **service URL** to instantiate a client object. You can find the service URL for a particular resource
in the [Azure portal][azure_portal], or using the [Azure CLI][azure_cli]:
```bash
# Get the service URL for the resource
az deidservice show --name "<resource-name>" --resource-group "<resource-group-name>" --query "properties.serviceUrl"
```

Optionally, save the service URL as an environment variable named `DEID_ENDPOINT` for the sample client initialization code.

The [Azure Identity][azure_identity] package provides the default implementation for authenticating the client.
You can use `DefaultAzureCredential` to automatically find the best credential to use at runtime.

```java readme-sample-create-client
DeidentificationClient deidentificationClient = new DeidentificationClientBuilder()
    .endpoint(Configuration.getGlobalConfiguration().get("DEID_ENDPOINT"))
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildClient();
```

## Key concepts
### De-identification operations:
Given an input text, the de-identification service can perform three main operations:
- `Tag` returns the category and location within the text of detected PHI entities.
- `Redact` returns output text where detected PHI entities are replaced with placeholder text. For example `John` replaced with `[name]`.
- `Surrogate` returns output text where detected PHI entities are replaced with realistic replacement values. For example, `My name is John Smith` could become `My name is Tom Jones`.

### String Encoding
When using the `Tag` operation, the service will return the locations of PHI entities in the input text. These locations will be represented as offsets and lengths, each of which is a [StringIndex][string_index] containing
three properties corresponding to three different text encodings. **Java applications should call `getUtf16()`.**

For more on text encoding, see [Character encoding in .NET][character_encoding].

### Available endpoints
There are two ways to interact with the de-identification service. You can send text directly, or you can create jobs 
to de-identify documents in Azure Storage.

You can de-identify text directly using the `DeidentificationClient`:
```java com.azure.health.deidentification.samples.deidentify_text
String inputText = "Hello, my name is John Smith.";

DeidentificationContent content = new DeidentificationContent(inputText);
content.setOperationType(DeidentificationOperationType.SURROGATE);

DeidentificationResult result = deidentificationClient.deidentifyText(content);
System.out.println("De-identified output: " + (result != null ? result.getOutputText() : null));
// De-identified output: Hello, my name is <synthetic name>.
```

To de-identify documents in Azure Storage, see [Tutorial: Configure Azure Storage to de-identify documents][deid_configure_storage]
for prerequisites and configuration options. In the sample code below, populate the `STORAGE_ACCOUNT_NAME` and `STORAGE_CONTAINER_NAME`
environment variables with your desired values. To refer to the same job between multiple examples, set the `DEID_JOB_NAME`
environment variable.
 
The client exposes a `beginDeidentifyDocuments` method that returns a `SyncPoller` or `PollerFlux` instance.
Callers should wait for the operation to be completed by calling `getFinalResult()`:

```java com.azure.health.deidentification.samples.begin_deidentify_documents
String storageLocation = "https://" + Configuration.getGlobalConfiguration().get("STORAGE_ACCOUNT_NAME") + ".blob.core.windows.net/" + Configuration.getGlobalConfiguration().get("STORAGE_CONTAINER_NAME");
DeidentificationJob job = new DeidentificationJob(
    new SourceStorageLocation(storageLocation, "data/example_patient_1"),
    new TargetStorageLocation(storageLocation, "_output")
        .setOverwrite(true)
);

job.setOperationType(DeidentificationOperationType.REDACT);

String jobName = Configuration.getGlobalConfiguration().get("DEID_JOB_NAME", "MyJob-" + Instant.now().toEpochMilli());
DeidentificationJob result = deidentificationClient.beginDeidentifyDocuments(jobName, job)
    .waitForCompletion()
    .getValue();
System.out.println(jobName + " - " + result.getStatus());
```

## Examples

The following sections provide several code snippets covering some of the most common client use cases, including:

- [Create a client](#create-a-deidentificationclient)
- [De-identify text](#de-identify-text)
- [Begin a job to de-identify documents in Azure Storage](#begin-a-job-to-de-identify-documents-in-azure-storage)
- [Get the status of a de-identification job](#get-the-status-of-a-de-identification-job)
- [List all de-identification jobs](#list-all-de-identification-jobs)
- [List all documents in a de-identification job](#list-all-documents-in-a-de-identification-job)

### Create a `DeidentificationClient`

```java readme-sample-create-client
DeidentificationClient deidentificationClient = new DeidentificationClientBuilder()
    .endpoint(Configuration.getGlobalConfiguration().get("DEID_ENDPOINT"))
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildClient();
```

### De-identify text

```java com.azure.health.deidentification.samples.deidentify_text
String inputText = "Hello, my name is John Smith.";

DeidentificationContent content = new DeidentificationContent(inputText);
content.setOperationType(DeidentificationOperationType.SURROGATE);

DeidentificationResult result = deidentificationClient.deidentifyText(content);
System.out.println("De-identified output: " + (result != null ? result.getOutputText() : null));
// De-identified output: Hello, my name is <synthetic name>.
```

### Begin a job to de-identify documents in Azure Storage

```java com.azure.health.deidentification.samples.begin_deidentify_documents
String storageLocation = "https://" + Configuration.getGlobalConfiguration().get("STORAGE_ACCOUNT_NAME") + ".blob.core.windows.net/" + Configuration.getGlobalConfiguration().get("STORAGE_CONTAINER_NAME");
DeidentificationJob job = new DeidentificationJob(
    new SourceStorageLocation(storageLocation, "data/example_patient_1"),
    new TargetStorageLocation(storageLocation, "_output")
        .setOverwrite(true)
);

job.setOperationType(DeidentificationOperationType.REDACT);

String jobName = Configuration.getGlobalConfiguration().get("DEID_JOB_NAME", "MyJob-" + Instant.now().toEpochMilli());
DeidentificationJob result = deidentificationClient.beginDeidentifyDocuments(jobName, job)
    .waitForCompletion()
    .getValue();
System.out.println(jobName + " - " + result.getStatus());
```

### Get the status of a de-identification job

```java com.azure.health.deidentification.samples.get_deidentification_job
String jobName = Configuration.getGlobalConfiguration().get("DEID_JOB_NAME");
DeidentificationJob result = deidentificationClient.getJob(jobName);
System.out.println(jobName + " - " + result.getStatus());
```

### List all de-identification jobs

```java com.azure.health.deidentification.samples.list_deidentification_jobs
PagedIterable<DeidentificationJob> result = deidentificationClient.listJobs();
for (DeidentificationJob job : result) {
    System.out.println(job.getJobName() + " - " + job.getStatus());
}
```

### List all documents in a de-identification job

```java com.azure.health.deidentification.samples.list_processed_documents_within_a_job
String jobName = Configuration.getGlobalConfiguration().get("DEID_JOB_NAME");
PagedIterable<DeidentificationDocumentDetails> result = deidentificationClient.listJobDocuments(jobName);
for (DeidentificationDocumentDetails documentDetails : result) {
    System.out.println(documentDetails.getInputLocation().getLocation() + " - " + documentDetails.getStatus());
}
```

## Troubleshooting
A `DeidentificationClient` raises `HttpResponseException` [exceptions][http_response_exception]. For example, if you
provide an invalid service URL an `HttpResponseException` would be raised with an error indicating the failure cause.
In the following code snippet, the error is handled
gracefully by catching the exception and display the additional information about the error.

```java readme-sample-handlingException
try {
    DeidentificationContent content = new DeidentificationContent("input text");
    deidentificationClient.deidentifyText(content);
} catch (HttpResponseException e) {
    System.out.println(e.getMessage());
    // Do something with the exception
}
```

## Next steps
See the [samples]<!--(https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/healthdataaiservices/azure-health-deidentification/src/samples/README.md)--> for several code snippets illustrating common patterns used in the de-identification service
Java SDK. For more extensive documentation, see the [de-identification service documentation][product_documentation].

## Contributing
For details on contributing to this repository, see the [contributing guide](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md).

<!-- LINKS -->
[product_documentation]: https://learn.microsoft.com/azure/healthcare-apis/deidentification/
[docs]: https://learn.microsoft.com/java/api/overview/azure/health-deidentification
[jdk]: https://learn.microsoft.com/azure/developer/java/fundamentals/
[azure_subscription]: https://azure.microsoft.com/free/
[deid_quickstart]: https://learn.microsoft.com/azure/healthcare-apis/deidentification/quickstart
[string_index]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/healthdataaiservices/azure-health-deidentification/src/main/java/com/azure/health/deidentification/models/StringIndex.java
[character_encoding]: https://learn.microsoft.com/dotnet/standard/base-types/character-encoding-introduction
[deid_rbac]: https://learn.microsoft.com/azure/healthcare-apis/deidentification/manage-access-rbac
[deid_configure_storage]: https://learn.microsoft.com/azure/healthcare-apis/deidentification/configure-storage
[azure_identity]: https://learn.microsoft.com/azure/developer/java/sdk/identity
[azure_cli]: https://learn.microsoft.com/cli/azure/healthcareapis/deidservice?view=azure-cli-latest
[azure_portal]: https://ms.portal.azure.com
[http_response_exception]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core/src/main/java/com/azure/core/exception/HttpResponseException.java
