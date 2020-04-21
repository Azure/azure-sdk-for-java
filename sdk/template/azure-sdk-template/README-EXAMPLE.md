# Azure Template client library for Java

This is a template README for Azure Java libraries, this is used as a template for new libraries added into this repository.

[Source code][source_code] | [Package (Maven)][package] | [API reference documentation][api_documentation]| [Product documentation][search_docs] | [Samples][samples]

## Getting started

### Include the package

If your module is include in [azure-sdk-bom](azure_sdk_bom) use the BOM depencency management include.

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>com.azure</groupId>
      <artifactId>azure-sdk-bom</artifactId>
      <version>1.0.0</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>

<dependencies>
  <dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-template</artifactId>
  </dependency>
</dependencies>
```

Otherwise use the standalone dependency include.

```xml
<dependencies>
  <dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-template</artifactId>
    <version>1.0.0</version>
  </dependency>
</dependencies>
```

### Prerequisites

- Java version 8 or above
- [Azure subscription][azure_subscription]
- [Service resources][service_resources]

### Authenticate the client

This section is option, it is only required if the library needs to authenticate with Azure.

## Key concepts

Include explanations about high level concepts that a user should know about when using this library.

## Examples

Include champion scenario examples here, each champion scenario will have an H3 header. The codesnippets for these examples
should be injected using the embedme tool that the Java team leverages.

### Champion scenario 1

```java
// Code for scenario 1
```

### Champion scenario 2

```java
// Code for scenario 2
```

### Champion scenario N

```java
// Code for scenario N
```

## Troubleshooting

### General

Include general troubleshooting tips such as handling service exceptions.

### Enabling Logging

Azure SDKs for Java offer a consistent logging story to help aid in troubleshooting application errors and expedite
their resolution. The logs produced will capture the flow of an application before reaching the terminal state to help
locate the root issue. View the [logging][logging] wiki for guidance about enabling logging.

### Default HTTP Client

By default a Netty based HTTP client will be used, for more information on configuring or changing the HTTP client is
detailed in the [HTTP clients wiki](https://github.com/Azure/azure-sdk-for-java/wiki/HTTP-clients).

## Next steps

Include additional documentation that doesn't belong in the getting started README, such as links to in-depth samples, documentation about the service, etc.

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a
[Contributor License Agreement (CLA)][cla] declaring that you have the right to, and actually do, grant us the rights
to use your contribution.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate
the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to
do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][coc]. For more information see the [Code of Conduct FAQ][coc_faq]
or contact [opencode@microsoft.com][coc_contact] with any additional questions or comments.

<!-- LINKS -->
[cla]: https://cla.microsoft.com
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com

<!-- You'll want to add an impression to capture metrics around how often people are viewing the README. -->
<!-- The last sub path will need to be URL encoded. azure-sdk-for-java%2Fsdk%2F{service directory}%2F{module directory}%2FREADME.png -->
![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Ftemplate%2Fazure-sdk-template%2FREADME-EXAMPLE.png)
