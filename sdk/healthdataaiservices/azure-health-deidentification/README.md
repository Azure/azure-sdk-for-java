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

```java com.azure.health.deidentification.helloworld
```

### Calling `Deidentification` endpoint

Calling the realtime endpoint with an input.

```java com.azure.health.deidentification.helloworld
```
### Creating Deidentification Job

Creating a Deidentification Job using `STORAGE_ACCOUNT_SAS_URI` environment variable.

```java com.azure.health.deidentification.sync.createjob
```
### Process Deidentification Job

Create and poll job until it is completed.

```java com.azure.health.deidentification.sync.createjob
```

### List Deidentification Jobs

List and process deidentification jobs

```java com.azure.health.deidentification.sync.listjobs
```

### List completed files

List the files which are completed by a job.

```java com.azure.health.deidentification.sync.listcompletedfiles
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
