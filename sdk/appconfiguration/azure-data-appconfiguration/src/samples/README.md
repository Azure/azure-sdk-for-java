---
page_type: sample
languages:
  - java
products:
  - azure
  - azure-app-configuration
urlFragment: appconfiguration-samples
---

# Azure App Configuration Samples client library for Java
This document explains samples and how to use them.

## Key concepts
Key concepts are explained in detail [here][SDK_README_KEY_CONCEPTS].

## Getting started
Getting started explained in detail [here][SDK_README_GETTING_STARTED].
 
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
- [HTTP client with proxy option][proxy_option]

## Troubleshooting
Troubleshooting steps can be found [here][SDK_README_TROUBLESHOOTING].

## Next steps
Start using App Configuration Java SDK in your solutions. Our SDK documentation could be found at [SDK Documentation][azconfig_docs]. 

## Contributing
This project welcomes contributions and suggestions. Find [more contributing][SDK_README_CONTRIBUTING] details here.

<!-- LINKS -->
[KEYS_SDK_README]: ../../README.md
[SDK_README_CONTRIBUTING]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/appconfiguration/azure-data-appconfiguration/README.md#contributing
[SDK_README_GETTING_STARTED]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/appconfiguration/azure-data-appconfiguration/README.md#getting-started
[SDK_README_TROUBLESHOOTING]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/appconfiguration/azure-data-appconfiguration/README.md#troubleshooting
[SDK_README_KEY_CONCEPTS]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/appconfiguration/azure-data-appconfiguration/README.md#key-concepts
[azconfig_docs]: https://docs.microsoft.com/azure/azure-app-configuration
[proxy_option]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/appconfiguration/azure-data-appconfiguration/src/samples/java/com/azure/data/appconfiguration/ProxyOptionsSample.java
[sample_hello_world]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/appconfiguration/azure-data-appconfiguration/src/samples/java/com/azure/data/appconfiguration/HelloWorld.java
[sample_list_configuration_settings]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/appconfiguration/azure-data-appconfiguration/src/samples/java/com/azure/data/appconfiguration/ConfigurationSets.java
[sample_conditional_request]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/appconfiguration/azure-data-appconfiguration/src/samples/java/com/azure/data/appconfiguration/ConditionalRequest.java
[sample_read_only]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/appconfiguration/azure-data-appconfiguration/src/samples/java/com/azure/data/appconfiguration/ReadOnlySample.java
[sample_read_revision_history]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/appconfiguration/azure-data-appconfiguration/src/samples/java/com/azure/data/appconfiguration/ReadRevisionHistory.java
[sample_aad]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/appconfiguration/azure-data-appconfiguration/src/samples/java/com/azure/data/appconfiguration/AadAuthentication.java

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fappconfiguration%2Fazure-data-appconfiguration%2Fsrc%2Fsamples%2FREADME.png)
