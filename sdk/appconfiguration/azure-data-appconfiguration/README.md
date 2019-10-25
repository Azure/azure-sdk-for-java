# Azure App Configuration client library for Java
Azure App Configuration is a managed service that helps developers centralize their application configurations simply and securely.

Modern programs, especially programs running in a cloud, generally have many components that are distributed in nature. Spreading configuration settings across these components can lead to hard-to-troubleshoot errors during an application deployment. Use App Configuration to store all the settings for your application and secure their accesses in one place.

Use the client library for App Configuration to create and manage application configuration settings.

[Source code][source_code] | [Package (Maven)][package] | [API reference documentation][api_documentation]
| [Product documentation][azconfig_docs] | [Samples][samples]

## Getting started

### Prerequisites

- Java Development Kit (JDK) with version 8 or above
- [Azure Subscription][azure_subscription]
- [App Configuration Store][app_config_store]

### Adding the package to your product

[//]: # ({x-version-update-start;com.azure:azure-data-appconfiguration;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-data-appconfiguration</artifactId>
    <version>1.0.0-preview.6</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Default HTTP Client
All client libraries, by default, use Netty HTTP client. Adding the above dependency will automatically configure 
AppConfiguration to use Netty HTTP client. 

[//]: # ({x-version-update-start;com.azure:azure-core-http-netty;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-core-http-netty</artifactId>
    <version>1.0.0-preview.7</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Alternate HTTP client
If, instead of Netty it is preferable to use OkHTTP, there is a HTTP client available for that too. Exclude the default
Netty and include OkHTTP client in your pom.xml.

[//]: # ({x-version-update-start;com.azure:azure-data-appconfiguration;current})
```xml
<!-- Add AppConfiguration dependency without Netty HTTP client -->
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-data-appconfiguration</artifactId>
    <version>1.0.0-preview.6</version>
    <exclusions>
      <exclusion>
        <groupId>com.azure</groupId>
        <artifactId>azure-core-http-netty</artifactId>
      </exclusion>
    </exclusions>
</dependency>
```
[//]: # ({x-version-update-start;com.azure:azure-core-http-okhttp;current})
```xml
<!-- Add OkHTTP client to use with AppConfiguration -->
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-core-http-okhttp</artifactId>
  <version>1.0.0-preview.7</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Configuring HTTP Clients
When an HTTP client is included on the classpath, as shown above, it is not necessary to specify it in the client library [builders](#create-client), unless you want to customize the HTTP client in some fashion. If this is desired, the `httpClient` builder method is often available to achieve just this, by allowing users to provide a custom (or customized) `com.azure.core.http.HttpClient` instances.

For starters, by having the Netty or OkHTTP dependencies on your classpath, as shown above, you can create new instances of these `HttpClient` types using their builder APIs. For example, here is how you would create a Netty HttpClient instance:

```java
HttpClient client = new NettyAsyncHttpClientBuilder()
    .port(8080)
    .wiretap(true)
    .build();
```

### Create an App Configuration Store

To create a Configuration Store you can use the Azure Portal or [Azure CLI][azure_cli].

You need to install the Azure App Configuration CLI extension first by executing the following command:
```Powershell
az extension add -n appconfig
```

After that, create the Configuration Store:
```Powershell
az appconfig create --name <config-store-name> --resource-group <resource-group-name> --location eastus
```

### Authenticate the client

In order to interact with the App Configuration service you'll need to create an instance of the Configuration Client class. To make this possible you'll need the connection string of the Configuration Store.

#### Get Credentials

Use the [Azure CLI][azure_cli] snippet below to get the connection string from the Configuration Store.

```Powershell
az appconfig credential list --name <config-store-name>
```

Alternatively, get the connection string from the Azure Portal.

#### Create Client

Once you have the value of the connection string you can create the configuration client:

```Java
ConfigurationClient client = new ConfigurationClientBuilder()
        .connectionString(connectionString)
        .buildClient();
```

or

```Java
ConfigurationAsyncClient client = new ConfigurationClientBuilder()
        .connectionString(connectionString)
        .buildAsyncClient();
```

## Key concepts

### Configuration Setting

A configuration setting is the fundamental resource within a configuration store. In its simplest form it is a key and a value. However, there are additional properties such as the modifiable content type and tags fields that allow the value to be interpreted or associated in different ways.

The Label property of a configuration setting provides a way to separate configuration settings into different dimensions. These dimensions are user defined and can take any form. Some common examples of dimensions to use for a label include regions, semantic versions, or environments. Many applications have a required set of configuration keys that have varying values as the application exists across different dimensions. For example, MaxRequests may be 100 in "NorthAmerica", and 200 in "WestEurope". By creating a configuration setting named MaxRequests with a label of "NorthAmerica" and another, only with a different value, in the "WestEurope" label, a solution can be achieved that allows the application to seamlessly retrieve Configuration Settings as it runs in these two dimensions.

### Configuration Client

The client performs the interactions with the App Configuration service, getting, setting, deleting, and selecting configuration settings. An asynchronous, `ConfigurationAsyncClient`, and synchronous, `ConfigurationClient`, client exists in the SDK allowing for selection of a client based on an application's use case.

An application that needs to retrieve startup configurations is better suited using the synchronous client, for example setting up a SQL connection.

```Java
ConfigurationClient client = new ConfigurationClient()
        .connectionString(appConfigConnectionString)
        .buildClient();

// urlLabel is optional
String url = client.getConfigurationSetting(urlKey, urlLabel).getValue();
Connection conn;
try {
    conn = DriverManager.getConnection(url);
} catch (SQLException ex) {
    System.out.printf("Failed to get connection using url %s", url);
}

```

An application that has a large set of configurations that it needs to periodically update is be better suited using the asynchronous client, for example all settings with a specific label are periodically updated.

```Java
ConfigurationAsyncClient client = new ConfigurationClientBuilder()
        .connectionString(appConfigConnectionString)
        .buildAsyncClient();

client.listConfigurationSettings(new SettingSelection().label(periodicUpdateLabel))
    .subscribe(setting -> updateConfiguration(setting));
```

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

### Create a Configuration Client
Create a configuration client by using `ConfigurationClientBuilder` by passing connection string.
```Java
ConfigurationClient client = new ConfigurationClientBuilder()
        .connectionString(connectionString)
        .buildClient();
```

### Create a Configuration Setting

Create a configuration setting to be stored in the configuration store. There are two ways to store a configuration setting:
- `addConfigurationSetting` creates a setting only if the setting does not already exist in the store.
- `setConfigurationSetting` creates a setting if it doesn't exist or overrides an existing setting.

```Java
ConfigurationSetting setting = client.setConfigurationSetting("some_key", "some_label", "some_value");
```

### Retrieve a Configuration Setting

Retrieve a previously stored configuration setting by calling `getConfigurationSetting`.
```Java
ConfigurationSetting setting = client.setConfigurationSetting("some_key", "some_label", "some_value");
ConfigurationSetting retrievedSetting = client.getConfigurationSetting("some_key", "some_label");
```
For conditional request,
```Java
Response<ConfigurationSetting> settingResponse = client.getConfigurationSettingWithResponse(setting, null, true, Context.NONE);
```

### Update an existing Configuration Setting

Update an existing configuration setting by calling `setConfigurationSetting`.
```Java
ConfigurationSetting setting = client.setConfigurationSetting("some_key", "some_label", "some_value");
ConfigurationSetting updatedSetting = client.setConfigurationSetting("some_key", "some_label", "new_value");
```
For conditional request,
```Java
Response<ConfigurationSetting> settingResponse = client.setConfigurationSettingWithResponse(setting, true, Context.NONE);
```

### Delete a Configuration Setting

Delete an existing configuration setting by calling `deleteConfigurationSetting`.
```Java
ConfigurationSetting setting = client.setConfigurationSetting("some_key", "some_label", "some_value");
ConfigurationSetting deletedSetting = client.deleteConfigurationSetting("some_key", "some_label");
```
For conditional request,
```Java
Response<ConfigurationSetting> settingResponse = client.deleteConfigurationSettingWithResponse(setting, true, Context.NONE);
```

### List Configuration Settings with Multiple Keys

List multiple configuration settings by calling `listConfigurationSettings`.
Pass null `SettingSelector` into the method If you want to fetch all configuration settings.
```Java
String key = "some_key";
String key2 = "new_key";
client.setConfigurationSetting(key, "some_label", "some_value");
client.setConfigurationSetting(key2, "new_label", "new_value");
SettingSelector selector = new SettingSelector().setKeys(key, key2);
PagedIterable<ConfigurationSetting> settings = client.listConfigurationSettings(selector);
```

### List Revisions of Multiple Configuration Settings

List all revision of a configuration settings by calling `listRevisions`.
```Java
String key = "revisionKey";
String Key2 = "newRevisionKey";
client.setConfigurationSetting(key, "some_label", "some_value");
client.setConfigurationSetting(key, "new_label", "new_value");
client.setConfigurationSetting(key2, "some_label", "some_value");
client.setConfigurationSetting(key2, "new_label", "new_value");
SettingSelector selector = new SettingSelector().setKeys(key, key2);
PagedIterable<ConfigurationSetting> settings = client.listRevisions(selector);
``` 

### Set a Configuration Setting to Read Only

Set a configuration setting to read-only status.
```Java
client.setConfigurationSetting("some_key", "some_label", "some_value");
ConfigurationSetting setting = client.setReadOnly("some_key", "some_label");
```
### Clear Read Only from a Configuration Setting

Clear read-only from a configuration setting.
```Java
ConfigurationSetting setting = client.clearReadOnly("some_key", "some_label");
```

## Troubleshooting

### General

When you interact with App Configuration using this Java client library, errors returned by the service correspond to the same HTTP status codes returned for [REST API][rest_api] requests. For example, if you try to retrieve a configuration setting that doesn't exist in your configuration store, a `404` error is returned, indicating `Not Found`.

## Next steps

[Quickstart: Create a Java Spring app with App Configuration][spring_quickstart]

## Contributing

If you would like to become an active contributor to this project please follow the instructions provided in [Microsoft Azure Projects Contribution Guidelines](http://azure.github.io/guidelines.html).

1. Fork it
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new Pull Request

<!-- LINKS -->
[api_documentation]: https://aka.ms/java-docs
[app_config_store]: https://docs.microsoft.com/azure/azure-app-configuration/quickstart-dotnet-core-app#create-an-app-configuration-store
[azconfig_docs]: https://docs.microsoft.com/azure/azure-app-configuration
[azure_cli]: https://docs.microsoft.com/cli/azure
[azure_subscription]: https://azure.microsoft.com/free
[maven]: https://maven.apache.org/
[package]: https://search.maven.org/artifact/com.azure/azure-data-appconfiguration
[rest_api]: https://github.com/Azure/AppConfiguration#rest-api-reference
[samples]: src/samples/java/com/azure/data/appconfiguration
[sample_hello_world]: src/samples/java/com/azure/data/appconfiguration/HelloWorld.java
[sample_list_configuration_settings]: src/samples/java/com/azure/data/appconfiguration/ConfigurationSets.java
[sample_conditional_request]: src/samples/java/com/azure/data/appconfiguration/ConditionalRequest.java
[sample_read_only]: src/samples/java/com/azure/data/appconfiguration/ReadOnlySample.java
[sample_read_revision_history]: src/samples/java/com/azure/data/appconfiguration/ReadRevisionHistory.java
[source_code]: src
[spring_quickstart]: https://docs.microsoft.com/azure/azure-app-configuration/quickstart-java-spring-app

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java/sdk/appconfiguration/azure-data-appconfiguration/README.png)
