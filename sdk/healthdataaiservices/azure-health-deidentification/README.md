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
    <version>1.0.0</version>
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
- [Call the deidentification endpoint](#calling-deidentification-endpoint)
- [Create a Deidentification Job](#creating-deidentification-job)
- [Process a Deidentification Job](#process-deidentification-job)
- [List all Deidentification Jobs](#list-deidentification-jobs)
- [List completed files within a Deidentification Job](#list-completed-files)

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

Calling the realtime endpoint with some input text.

```java com.azure.health.deidentification.sync.helloworld
String inputText = "Hello, my name is John Smith.";
DeidentificationContent content = new DeidentificationContent(inputText);

DeidentificationResult result = deidentificationClient.deidentifyText(content);

System.out.println("Deidentified output: " + result.getOutputText());
// Deidentified output: Hello, my name is Harley Billiard.
```
### Creating Deidentification Job

Create a Deidentification Job using `STORAGE_ACCOUNT_NAME` and `STORAGE_CONTAINER_NAME` environment variables to 
deidentify all files in the storage container.

```java com.azure.health.deidentification.sync.createjob.create
String storageLocation = "https://" + Configuration.getGlobalConfiguration().get("STORAGE_ACCOUNT_NAME") + ".blob.core.windows.net/" + Configuration.getGlobalConfiguration().get("STORAGE_CONTAINER_NAME");
String jobName = "MyJob-" + Instant.now().toEpochMilli();
String outputFolder = "output_patient_1/";
String inputPrefix = "example_patient_1/";
SourceStorageLocation sourceStorageLocation = new SourceStorageLocation(storageLocation, inputPrefix);

DeidentificationJob job = new DeidentificationJob(sourceStorageLocation, new TargetStorageLocation(storageLocation, outputFolder));
job.setOperation(DeidentificationOperationType.SURROGATE);

```
### Process Deidentification Job

Create and poll job until it is completed.

```java com.azure.health.deidentification.sync.createjob.process
DeidentificationJob result = deidentificationClient.beginDeidentifyDocuments(jobName, job)
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
PagedIterable<DeidentificationDocumentDetails> reports = deidentificationClient.listJobDocuments(jobName);

for (DeidentificationDocumentDetails currentFile : reports) {
    System.out.println(currentFile.getId() + " - " + currentFile.getOutput().getLocation());
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
