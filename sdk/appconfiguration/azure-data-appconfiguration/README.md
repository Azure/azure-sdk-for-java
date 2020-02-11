# Azure App Configuration client library for Java
Azure App Configuration is a managed service that helps developers centralize their application configurations simply and securely.

Modern programs, especially programs running in a cloud, generally have many components that are distributed in nature. Spreading configuration settings across these components can lead to hard-to-troubleshoot errors during an application deployment. Use App Configuration to store all the settings for your application and secure their accesses in one place.

Use the client library for App Configuration to create and manage application configuration settings.

[Source code][source_code] | [Package (Maven)][package] | [API reference documentation][api_documentation]
| [Product documentation][azconfig_docs] | [Samples][samples_readme] | [Wiki][wiki]

## Getting started

### Prerequisites

- Java Development Kit (JDK) with version 8 or above
- [Azure Subscription][azure_subscription]
- [App Configuration Store][app_config_store]

### Adding the package to your product

Java SDKs for Azure releases a BOM (Bill of Materials) which deals with inter-artifact dependencies and is the recommended way to consume artifacts. The BOM contains a set of artifacts that have been verified to share a common set of dependencies. The following is how to include App Configuration in your product when using the BOM.

[//]: # ({x-version-update-start;com.azure:azure-sdk-bom;current})
```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.azure</groupId>
            <artifactId>azure-sdk-bom</artifactId>
            <version>1.0.0-beta.1</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>com.azure</groupId>
        <artifactId>azure-data-appconfiguration</artifactId>
    </dependency>
</dependencies>
```
[//]: # ({x-version-update-end})

If you do not wish to make use of the Azure SDK BOM, you may retrieve the details of recent releases of `azure-data-appconfiguration` [here][package].

## Key concepts

### Configuration Setting

A configuration setting is the fundamental resource within a configuration store. In its simplest form it is a key and a value. However, there are additional properties such as the modifiable content type and tags fields that allow the value to be interpreted or associated in different ways.

The Label property of a configuration setting provides a way to separate configuration settings into different dimensions. These dimensions are user defined and can take any form. Some common examples of dimensions to use for a label include regions, semantic versions, or environments. Many applications have a required set of configuration keys that have varying values as the application exists across different dimensions. For example, MaxRequests may be 100 in "NorthAmerica", and 200 in "WestEurope". By creating a configuration setting named MaxRequests with a label of "NorthAmerica" and another, only with a different value, in the "WestEurope" label, a solution can be achieved that allows the application to seamlessly retrieve Configuration Settings as it runs in these two dimensions.

## Examples

The following sections provide several code snippets covering some of the most common configuration service tasks. Separately there is a [full list of samples][samples_readme] that demonstrates a number of important scenarios.

### Create a Configuration Client

Once you have a connection string you can create a configuration client with either synchronous or asynchronous APIs, as such:

<!-- embedme ./src/samples/java/com/azure/data/appconfiguration/ReadmeSamples.java#L44-L46 -->
```Java
ConfigurationClient configurationClient = new ConfigurationClientBuilder()
    .connectionString(connectionString)
    .buildClient();
```

or

<!-- embedme ./src/samples/java/com/azure/data/appconfiguration/ReadmeSamples.java#L50-L52 -->
```Java
ConfigurationAsyncClient configurationClient = new ConfigurationClientBuilder()
    .connectionString(connectionString)
    .buildAsyncClient();
```

### Create a Configuration Setting

Create a configuration setting to be stored in the configuration store.

Use `addConfigurationSetting` to create a setting only if the setting does not already exist in the store:

<!-- embedme ./src/samples/java/com/azure/data/appconfiguration/ReadmeSamples.java#L96-L96 -->
```Java
ConfigurationSetting setting = configurationClient.addConfigurationSetting("new_key", "new_label", "new_value");
```

Alternatively, use `setConfigurationSetting` to create a setting if it does not exist, or if you want to override an existing setting:

<!-- embedme ./src/samples/java/com/azure/data/appconfiguration/ReadmeSamples.java#L100-L100 -->
```Java
ConfigurationSetting setting = configurationClient.setConfigurationSetting("some_key", "some_label", "some_value");
```

### Retrieve a Configuration Setting

Retrieve a previously stored configuration setting by calling `getConfigurationSetting`.

<!-- embedme ./src/samples/java/com/azure/data/appconfiguration/ReadmeSamples.java#L104-L105 -->
```Java
ConfigurationSetting setting = configurationClient.setConfigurationSetting("some_key", "some_label", "some_value");
ConfigurationSetting retrievedSetting = configurationClient.getConfigurationSetting("some_key", "some_label");
```

### List Configuration Settings with multiple keys

List multiple configuration settings by calling `listConfigurationSettings`.
Pass a null `SettingSelector` into the method if you want to fetch all the configuration settings and their fields.

<!-- embedme ./src/samples/java/com/azure/data/appconfiguration/ReadmeSamples.java#L134-L139 -->
```Java
String key = "some_key";
String key2 = "new_key";
configurationClient.setConfigurationSetting(key, "some_label", "some_value");
configurationClient.setConfigurationSetting(key2, "new_label", "new_value");
SettingSelector selector = new SettingSelector().setKeyFilter(key + "," + key2);
PagedIterable<ConfigurationSetting> settings = configurationClient.listConfigurationSettings(selector);
```

## Next steps

- Samples are explained in detail [here][samples_readme].
- [Quickstart: Create a Java Spring app with App Configuration][spring_quickstart]
- The [Azure SDK for Java wiki][wiki] contains guidance on logging, performance tuning, identity / authentication, and much more.

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a [Contributor License Agreement (CLA)][cla] declaring that you have the right to, and actually do, grant us the rights to use your contribution.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][coc]. For more information see the [Code of Conduct FAQ][coc_faq] or contact [opencode@microsoft.com][coc_contact] with any additional questions or comments.

<!-- LINKS -->
[add_headers_from_context_policy]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/core/azure-core/src/main/java/com/azure/core/http/policy/AddHeadersFromContextPolicy.java
[api_documentation]: https://aka.ms/java-docs
[app_config_store]: https://docs.microsoft.com/azure/azure-app-configuration/quickstart-dotnet-core-app#create-an-app-configuration-store
[app_config_role]: https://github.com/Azure/AppConfiguration/blob/master/docs/REST/authorization/aad.md
[azconfig_docs]: https://docs.microsoft.com/azure/azure-app-configuration
[azure_cli]: https://docs.microsoft.com/cli/azure
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/identity/azure-identity
[azure_subscription]: https://azure.microsoft.com/free
[cla]: https://cla.microsoft.com
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com
[default_cred_ref]: https://azuresdkdocs.blob.core.windows.net/$web/java/azure-identity/1.0.1/com/azure/identity/DefaultAzureCredential.html
[maven]: https://maven.apache.org/
[package]: https://search.maven.org/artifact/com.azure/azure-data-appconfiguration
[performance_tuning]: https://github.com/Azure/azure-sdk-for-java/wiki/Performance-Tuning
[rest_api]: https://github.com/Azure/AppConfiguration#rest-api-reference
[samples_readme]: src/samples/README.md
[source_code]: src
[spring_quickstart]: https://docs.microsoft.com/azure/azure-app-configuration/quickstart-java-spring-app
[wiki]: https://github.com/Azure/azure-sdk-for-java/wiki

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fappconfiguration%2Fazure-data-appconfiguration%2FREADME.png)
