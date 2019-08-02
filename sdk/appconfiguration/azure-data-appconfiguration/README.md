# Azure App Configuration client library for Java
Azure App Configuration is a managed service that helps developers centralize their application configurations simply and securely.

Modern programs, especially programs running in a cloud, generally have many components that are distributed in nature. Spreading configuration settings across these components can lead to hard-to-troubleshoot errors during an application deployment. Use App Configuration to store all the settings for your application and secure their accesses in one place.

Use the client library for App Configuration to create and manage application configuration settings.

[Source code][source_code] | [Package (Maven)][package] | [API reference documentation][api_documentation] | [Product documentation][azconfig_docs]

## Getting started

### Prerequisites

- Java Development Kit (JDK) with version 8 or above
- [Azure Subscription][azure_subscription]
- [App Configuration Store][app_config_store]

### Adding the package to your product

```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-data-appconfiguration</artifactId>
    <version>1.0.0-preview.2</version>
</dependency>
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
        .credential(new ConfigurationClientCredentials(connectionString))
        .buildClient();
```

or

```Java
ConfigurationAsyncClient client = new ConfigurationClientBuilder()
        .credential(new ConfigurationClientCredentials(connectionString))
        .buildAsyncClient();
```

## Key concepts

### Configuration Setting

A configuration setting is the fundamental resource within a Configuration Store. In its simplest form it is a key and a value. However, there are additional properties such as the modifiable content type and tags fields that allow the value to be interpreted or associated in different ways.

The Label property of a Configuration Setting provides a way to separate Configuration Settings into different dimensions. These dimensions are user defined and can take any form. Some common examples of dimensions to use for a label include regions, semantic versions, or environments. Many applications have a required set of configuration keys that have varying values as the application exists across different dimensions. For example, MaxRequests may be 100 in "NorthAmerica", and 200 in "WestEurope". By creating a Configuration Setting named MaxRequests with a label of "NorthAmerica" and another, only with a different value, in the "WestEurope" label, a solution can be achieved that allows the application to seamlessly retrieve Configuration Settings as it runs in these two dimensions.

### Configuration Client

The client performs the interactions with the App Configuration service, getting, setting, updating, deleting, and selecting configuration settings. An asynchronous, `ConfigurationAsyncClient`, and synchronous, `ConfigurationClient`, client exists in the SDK allowing for selection of a client based on an application's use case.

An application that needs to retrieve startup configurations is better suited using the synchronous client, for example setting up a SQL connection.

```Java
ConfigurationClient client = new ConfigurationClient()
        .credential(new ConfigurationClientCredentials(appConfigConnectionString))
        .buildClient();

String url = client.getSetting(urlKey).value();
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
        .credential(new ConfigurationClientCredentials(appConfigConnectionString))
        .buildAsyncClient();

client.listSettings(new SettingSelection().label(periodicUpdateLabel))
    .subscribe(setting -> updateConfiguration(setting));
```

## Examples

The following sections provide several code snippets covering some of the most common Configuration Service tasks, including:
- [Create a Configuration Setting](#create-a-Configuration-Setting)
- [Retrieve a Configuration Setting](#retrieve-a-Configuration-Setting)
- [Update an existing Configuration Setting](#update-an-existing-Configuration-Setting)
- [Delete a Configuration Setting](#delete-a-Configuration-Setting)

### Create a Configuration Setting

Create a Configuration Setting to be stored in the Configuration Store. There are two ways to store a Configuration Setting:
- addSetting creates a setting only if the setting does not already exist in the store.
- setSetting creates a setting if it doesn't exist or overrides an existing setting.
```Java
ConfigurationClient client = new ConfigurationClientBuilder()
        .credential(new ConfigurationClientCredentials(connectionString))
        .buildClient();
ConfigurationSetting setting = client.setSetting("some_key", "some_value");
```

### Retrieve a Configuration Setting

Retrieve a previously stored Configuration Setting by calling getSetting.
```Java
ConfigurationClient client = new ConfigurationClientBuilder()
        .credential(new ConfigurationClientCredentials(connectionString))
        .buildClient();
client.setSetting("some_key", "some_value");
ConfigurationSetting setting = client.getSetting("some_key");
```

### Update an existing Configuration Setting

Update an existing Configuration Setting by calling updateSetting.
```Java
ConfigurationClient client = new ConfigurationClientBuilder()
        .credential(new ConfigurationClientCredentials(connectionString))
        .buildClient();
client.setSetting("some_key", "some_value");
ConfigurationSetting setting = client.updateSetting("some_key", "new_value");
```

### Delete a Configuration Setting

Delete an existing Configuration Setting by calling deleteSetting.
```Java
ConfigurationClient client = new ConfigurationClientBuilder()
        .credential(new ConfigurationClientCredentials(connectionString))
        .buildClient();
client.setSetting("some_key", "some_value");
ConfigurationSetting setting = client.deleteSetting("some_key");
```

## Troubleshooting

### General

When you interact with App Configuration using this Java client library, errors returned by the service correspond to the same HTTP status codes returned for [REST API][rest_api] requests. For example, if you try to retrieve a Configuration Setting that doesn't exist in your Configuration Store, a `404` error is returned, indicating `Not Found`.

## Next steps

[Quickstart: Create a Java Spring app with App Configuration](https://docs.microsoft.com/en-us/azure/azure-app-configuration/quickstart-java-spring-app)

## Contributing

If you would like to become an active contributor to this project please follow the instructions provided in [Microsoft Azure Projects Contribution Guidelines](http://azure.github.io/guidelines.html).

1. Fork it
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new Pull Request

<!-- LINKS -->
[api_documentation]: https://aka.ms/java-docs
[app_config_store]: https://docs.microsoft.com/en-us/azure/azure-app-configuration/quickstart-dotnet-core-app#create-an-app-configuration-store
[azconfig_docs]: https://docs.microsoft.com/en-us/azure/azure-app-configuration/
[azure_cli]: https://docs.microsoft.com/cli/azure
[azure_subscription]: https://azure.microsoft.com/en-us/free/
[maven]: https://maven.apache.org/
[package]: https://search.maven.org/artifact/com.azure/azure-data-appconfiguration
[rest_api]: https://github.com/Azure/AppConfiguration#rest-api-reference
[source_code]: src
