# Azure Deidentification client library for Java

Azure Deidentification client library for Java.

This package contains Microsoft Azure Deidentification client library which is a managed service that enables users to tag, redact, or surrogate health data.

## Documentation

Various documentation is available to help you get started

- [API reference documentation][docs]
- [Product documentation][product_documentation]

## Getting started

### Prerequisites

- [Java Development Kit (JDK)][jdk] with version 8 or above
- [Azure Subscription][azure_subscription]

### Adding the package to your product

[//]: # ({x-version-update-start;com.azure:azure-health-deidentification;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-health-deidentification</artifactId>
    <version>1.0.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Authentication

[Azure Identity][azure_identity] package provides the default implementation for authenticating the client.

## Key concepts
### Operation Modes:

- Tag: Will return a structure of offset and length with the PHI category of the related text spans.
- Redact: Will return output text with placeholder stubbed text. ex. `[name]`
- Surrogate: Will return output text with synthetic replacements.
    - `My name is John Smith`
    - `My name is Tom Jones`

## Examples

The following sections provide several code snippets covering some of the most common Azure Deidentification client use cases, including:

- [Create a `DeidentificationClient`](#create-a-deidentificationclient)
- [Calling deidentification endpoint](#calling-deidentification-endpoint)
- [Creating deidentification Job](#creating-deidentification-job)
- [Process deidentification Job](#process-deidentification-job)
- [List deidentification Jobs](#list-deidentification-jobs)
- [List completed files](#list-completed-files)

### Create a `DeidentificationClient`

Create a `DeidentificationClient` using the `DEID_SERVICE_ENDPOINT` environment variable.

```java com.azure.health.deidentification.readme
DeidentificationClientBuilder deidentificationClientbuilder = new DeidentificationClientBuilder()
    .endpoint(Configuration.getGlobalConfiguration().get("DEID_SERVICE_ENDPOINT", "endpoint"))
    .httpClient(HttpClient.createDefault())
    .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC));

DeidentificationClient deidentificationClient = deidentificationClientbuilder.buildClient();
```

### Calling `Deidentification` endpoint

Calling the realtime endpoint with an input.

```java com.azure.health.deidentification.sync.helloworld
String inputText = "Hello, my name is John Smith.";

DeidentificationContent content = new DeidentificationContent(inputText);

DeidentificationResult result = deidentificationClient.deidentify(content);

System.out.println("Deidentified output: " + result.getOutputText());
// Deidentified output: Hello, my name is Harley Billiard.
```
### Creating Deidentification Job

Creating a Deidentification Job using `STORAGE_ACCOUNT_NAME` and `STORAGE_CONTAINER_NAME` environment variables.

```java com.azure.health.deidentification.sync.createjob.create
String storageLocation = "https://" + Configuration.getGlobalConfiguration().get("STORAGE_ACCOUNT_NAME") + ".blob.core.windows.net/" + Configuration.getGlobalConfiguration().get("STORAGE_CONTAINER_NAME");
String jobName = "MyJob-" + Instant.now().toEpochMilli();
String outputFolder = "_output";
String inputPrefix = "example_patient_1";
SourceStorageLocation sourceStorageLocation = new SourceStorageLocation(storageLocation, inputPrefix);

DeidentificationJob job = new DeidentificationJob(sourceStorageLocation, new TargetStorageLocation(storageLocation, outputFolder));
job.setOperation(OperationType.SURROGATE);
job.setDataType(DocumentDataType.PLAINTEXT);

```
### Process Deidentification Job

Create and poll job until it is completed.

```java com.azure.health.deidentification.sync.createjob.process
DeidentificationJob result = deidentificationClient.beginCreateJob(jobName, job)
    .waitForCompletion()
    .getValue();
System.out.println(jobName + " - " + result.getStatus());
// MyJob-1719953889301 - Succeeded
```

### List Deidentification Jobs

List and process deidentification jobs

```java com.azure.health.deidentification.sync.listjobs
PagedIterable<DeidentificationJob> jobs = deidentificationClient.listJobs();
for (DeidentificationJob currentJob : jobs) {
    System.out.println(currentJob.getName() + " - " + currentJob.getStatus());
    // MyJob-1719953889301 - Succeeded
}
```

### List completed files

List the files which are completed by a job.

```java com.azure.health.deidentification.sync.listcompletedfiles
PagedIterable<DocumentDetails> reports = deidentificationClient.listJobDocuments(jobName);

for (DocumentDetails currentFile : reports) {
    System.out.println(currentFile.getId() + " - " + currentFile.getOutput().getPath());
    // c45dcd5e-e3ce-4ff2-80b6-a8bbeb47f878 - _output/MyJob-1719954393623/example_patient_1/visit_summary.txt
    // e55a1aa2-8eba-4515-b070-1fd3d005008b - _output/MyJob-1719954393623/example_patient_1/doctor_dictation.txt
}
```



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
[product_documentation]: https://azure.microsoft.com/services/
[docs]: https://azure.github.io/azure-sdk-for-java/
[jdk]: https://learn.microsoft.com/azure/developer/java/fundamentals/
[azure_subscription]: https://azure.microsoft.com/free/
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fhealthdataaiservices%2Fazure-health-deidentification%2FREADME.png)
