
## Azure App Configuration Samples client library for Java
This document explains samples and how to use them.

## Key concepts
Key concepts are explained in detail [here][SDK_README_KEY_CONCEPTS].

# Samples Azure App Configuration APIs
This document describes how to use samples and what is done in each sample.

## Getting started
Getting started explained in detail [here][SDK_README_GETTING_STARTED].
 
### Adding the package to your project

Maven dependency for Azure app configuration Client library. Add it to your project's pom file.

[//]: # ({x-version-update-start;com.azure:azure-data-appconfiguration;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-data-appconfiguration</artifactId>
    <version>1.0.0-preview.6</version>
</dependency>
```
[//]: # ({x-version-update-end})

## How to run
These sample can be run in your IDE with default JDK.

## Examples
The following sections provide several code snippets covering some of the most common configuration service tasks, including:

- [Create a configuration setting][sample_hello_world]
- [Retrieve a configuration setting][sample_hello_world]
- [Update an existing configuration setting][sample_hello_world]
- [Delete a configuration setting][sample_hello_world]
- [List configuration settings with multiple keys][sample_list_configuration_settings]
- [List revisions of multiple configuration settings][sample_read_revision_history]
- [Set a configuration setting to read only][sample_read_only]
- [Clear read only from a configuration setting][sample_read_only]
- [Conditional request a configuration setting][sample_conditional_request]
- [AAD Authentication][sample_aad]

## Troubleshooting
Troubleshooting steps can be found [here][SDK_README_TROUBLESHOOTING].

## Next steps
Start using KeyVault Java SDK in your solutions. Our SDK documentation could be found at [SDK Documentation][azconfig_docs]. 

###  Additional Documentation
For more extensive documentation , see the [API reference documentation][azkeyvault_rest].

## Contributing
This project welcomes contributions and suggestions. Find [more contributing][SDK_README_CONTRIBUTING] details here.

<!-- LINKS -->
[KEYS_SDK_README]: ../../README.md
[SDK_README_CONTRIBUTING]: ../../README.md#contributing
[SDK_README_GETTING_STARTED]: ../../README.md#getting-started
[SDK_README_TROUBLESHOOTING]: ../../README.md#troubleshooting
[SDK_README_KEY_CONCEPTS]: ../../README.md#key-concepts
[SDK_README_DEPENDENCY]: ../../README.md#adding-the-package-to-your-product
[azconfig_docs]: https://docs.microsoft.com/azure/azure-app-configuration
[sample_hello_world]: java/com/azure/data/appconfiguration/HelloWorld.java
[sample_list_configuration_settings]: java/com/azure/data/appconfiguration/ConfigurationSets.java
[sample_conditional_request]: java/com/azure/data/appconfiguration/ConditionalRequest.java
[sample_read_only]: java/com/azure/data/appconfiguration/ReadOnlySample.java
[sample_read_revision_history]: com/azure/data/appconfiguration/ReadRevisionHistory.java
[sample_aad]: java/com/azure/data/appconfiguration/AadAuthentication.java

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fappconfiguration%2Fazure-data-appconfiguration%2Fsrc%2Fsamples%2FREADME.png)
