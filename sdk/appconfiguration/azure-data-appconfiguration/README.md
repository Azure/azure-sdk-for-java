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

### Include the Package

[//]: # ({x-version-update-start;com.azure:azure-data-appconfiguration;current})
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-data-appconfiguration</artifactId>
  <version>1.1.4</version>
</dependency>
```
[//]: # ({x-version-update-end})

#### Create an App Configuration Store

To create a Configuration Store you can use the Azure Portal or [Azure CLI][azure_cli].

You need to install the Azure App Configuration CLI extension first by executing the following command:

```bash
az extension add -n appconfig
```

After that, create the Configuration Store:

```bash
az appconfig create --name <config-store-name> --resource-group <resource-group-name> --location eastus
```

### Authenticate the client

In order to interact with the App Configuration service you'll need to create an instance of the Configuration Client 
class. To make this possible you'll need the connection string of the Configuration Store. Alternatively, use AAD token
to connect to the service.

#### Use connection string

##### Get credentials

Use the [Azure CLI][azure_cli] snippet below to get the connection string from the Configuration Store.

```bash
az appconfig credential list --name <config-store-name>
```

Alternatively, get the connection string from the Azure Portal.

##### Create a Configuration Client

Once you have the value of the connection string you can create the configuration client:

<!-- embedme ./src/samples/java/com/azure/data/appconfiguration/ReadmeSamples.java#L46-L48 -->
```Java
ConfigurationClient configurationClient = new ConfigurationClientBuilder()
    .connectionString(connectionString)
    .buildClient();
```

or

<!-- embedme ./src/samples/java/com/azure/data/appconfiguration/ReadmeSamples.java#L52-L54 -->
```Java
ConfigurationAsyncClient configurationClient = new ConfigurationClientBuilder()
    .connectionString(connectionString)
    .buildAsyncClient();
```

#### Use AAD token

Here we demonstrate using [DefaultAzureCredential][default_cred_ref]
to authenticate as a service principal. However, the configuration client
accepts any [azure-identity][azure_identity] credential. See the
[azure-identity][azure_identity] documentation for more information about other
credentials.

##### Create a service principal (optional)
This [Azure CLI][azure_cli] snippet shows how to create a
new service principal. Before using it, replace "your-application-name" with
the appropriate name for your service principal.

Create a service principal:
```Bash
az ad sp create-for-rbac --name http://my-application --skip-assignment
```

Output:
```json
 {
     "appId": "generated app id",
     "displayName": "my-application",
     "name": "http://my-application",
     "password": "random password",
     "tenant": "tenant id"
 }
```

Use the output to set **AZURE_CLIENT_ID** ("appId" above), **AZURE_CLIENT_SECRET**
("password" above) and **AZURE_TENANT_ID** ("tenant" above) environment variables.
The following example shows a way to do this in Bash:
```Bash
export AZURE_CLIENT_ID="generated app id"
export AZURE_CLIENT_SECRET="random password"
export AZURE_TENANT_ID="tenant id"
```

Assign one of the applicable [App Configuration roles][app_config_role] to the service principal.

##### Create a client
Once the **AZURE_CLIENT_ID**, **AZURE_CLIENT_SECRET** and
**AZURE_TENANT_ID** environment variables are set,
[DefaultAzureCredential][default_cred_ref] will be able to authenticate the
configuration client.

Constructing the client also requires your configuration store's URL, which you can
get from the Azure CLI or the Azure Portal. In the Azure Portal, the URL can be found listed as the service "Endpoint".

<!-- embedme ./src/samples/java/com/azure/data/appconfiguration/ReadmeSamples.java#L58-L62 -->
```Java
DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
ConfigurationClient configurationClient = new ConfigurationClientBuilder()
    .credential(credential)
    .endpoint(endpoint)
    .buildClient();
```

## Key concepts

### Configuration Setting

A configuration setting is the fundamental resource within a configuration store. In its simplest form it is a key and a value. However, there are additional properties such as the modifiable content type and tags fields that allow the value to be interpreted or associated in different ways.

The Label property of a configuration setting provides a way to separate configuration settings into different dimensions. These dimensions are user defined and can take any form. Some common examples of dimensions to use for a label include regions, semantic versions, or environments. Many applications have a required set of configuration keys that have varying values as the application exists across different dimensions. For example, MaxRequests may be 100 in "NorthAmerica", and 200 in "WestEurope". By creating a configuration setting named MaxRequests with a label of "NorthAmerica" and another, only with a different value, in the "WestEurope" label, a solution can be achieved that allows the application to seamlessly retrieve Configuration Settings as it runs in these two dimensions.

### Configuration Client

The client performs the interactions with the App Configuration service, getting, setting, deleting, and selecting configuration settings. An asynchronous, `ConfigurationAsyncClient`, and synchronous, `ConfigurationClient`, client exists in the SDK allowing for selection of a client based on an application's use case.

An application that needs to retrieve startup configurations is better suited using the synchronous client, for example setting up a SQL connection.

<!-- embedme ./src/samples/java/com/azure/data/appconfiguration/ReadmeSamples.java#L66-L85 -->
```Java
ConfigurationClient configurationClient = new ConfigurationClientBuilder()
    .connectionString(connectionString)
    .buildClient();

// urlLabel is optional
String url = configurationClient.getConfigurationSetting(urlKey, urlLabel).getValue();
Connection conn = null;
try {
    conn = DriverManager.getConnection(url);
} catch (SQLException ex) {
    System.out.printf("Failed to get connection using url %s", url);
} finally {
    if (conn != null) {
        try {
            conn.close();
        } catch (SQLException ex) {
            System.out.printf("Failed to close connection, url %s", url);
        }
    }
}
```

An application that has a large set of configurations that it needs to periodically update is be better suited using the asynchronous client, for example all settings with a specific label are periodically updated.

<!-- embedme ./src/samples/java/com/azure/data/appconfiguration/ReadmeSamples.java#L89-L94 -->
```Java
ConfigurationAsyncClient configurationClient = new ConfigurationClientBuilder()
    .connectionString(connectionString)
    .buildAsyncClient();

configurationClient.listConfigurationSettings(new SettingSelector().setLabelFilter(periodicUpdateLabel))
    .subscribe(setting -> updateConfiguration(setting));
```

## Examples

The following sections provide several code snippets covering some of the most common configuration service tasks, including:

### Create a Configuration Client

Create a configuration client by using `ConfigurationClientBuilder` by passing connection string.

<!-- embedme ./src/samples/java/com/azure/data/appconfiguration/ReadmeSamples.java#L46-L48 -->
```Java
ConfigurationClient configurationClient = new ConfigurationClientBuilder()
    .connectionString(connectionString)
    .buildClient();
```

### Create a Configuration Setting

Create a configuration setting to be stored in the configuration store. There are two ways to store a configuration setting:

- `addConfigurationSetting` creates a setting only if the setting does not already exist in the store.

<!-- embedme ./src/samples/java/com/azure/data/appconfiguration/ReadmeSamples.java#L98-L98 -->
```Java
ConfigurationSetting setting = configurationClient.addConfigurationSetting("new_key", "new_label", "new_value");
```

Or

- `setConfigurationSetting` creates a setting if it doesn't exist or overrides an existing setting.

<!-- embedme ./src/samples/java/com/azure/data/appconfiguration/ReadmeSamples.java#L102-L102 -->
```Java
ConfigurationSetting setting = configurationClient.setConfigurationSetting("some_key", "some_label", "some_value");
```

### Retrieve a Configuration Setting

Retrieve a previously stored configuration setting by calling `getConfigurationSetting`.

<!-- embedme ./src/samples/java/com/azure/data/appconfiguration/ReadmeSamples.java#L106-L107 -->
```Java
ConfigurationSetting setting = configurationClient.setConfigurationSetting("some_key", "some_label", "some_value");
ConfigurationSetting retrievedSetting = configurationClient.getConfigurationSetting("some_key", "some_label");
```

For conditional request, if you want to conditionally fetch a configuration setting, set `ifChanged` to true. 
When `ifChanged` is true, the configuration setting is only retrieved if it is different than the given `setting`. 
This is determined by comparing the ETag of the `setting` to the one in the service to see if they are the same or not.
If the ETags are not the same, it means the configuration setting is different, and its value is retrieved.

<!-- embedme ./src/samples/java/com/azure/data/appconfiguration/ReadmeSamples.java#L111-L112 -->
```Java
ConfigurationSetting setting = configurationClient.setConfigurationSetting("some_key", "some_label", "some_value");
Response<ConfigurationSetting> settingResponse = configurationClient.getConfigurationSettingWithResponse(setting, null, true, Context.NONE);
```

### Update an existing Configuration Setting

Update an existing configuration setting by calling `setConfigurationSetting`.

<!-- embedme ./src/samples/java/com/azure/data/appconfiguration/ReadmeSamples.java#L116-L117 -->
```Java
ConfigurationSetting setting = configurationClient.setConfigurationSetting("some_key", "some_label", "some_value");
ConfigurationSetting updatedSetting = configurationClient.setConfigurationSetting("some_key", "some_label", "new_value");
```

For conditional request, if you want to conditionally update a configuration setting, set the `ifUnchanged` parameter to
true. When `ifUnchanged` is true, the configuration setting is only updated if it is same as the given `setting`.
This is determined by comparing the ETag of the `setting` to the one in the service to see if they are the same or not.
If the ETag are the same, it means the configuration setting is same, and its value is updated.

<!-- embedme ./src/samples/java/com/azure/data/appconfiguration/ReadmeSamples.java#L121-L122 -->
```Java
ConfigurationSetting setting = configurationClient.setConfigurationSetting("some_key", "some_label", "some_value");
Response<ConfigurationSetting> settingResponse = configurationClient.setConfigurationSettingWithResponse(setting, true, Context.NONE);
```

### Delete a Configuration Setting

Delete an existing configuration setting by calling `deleteConfigurationSetting`.

<!-- embedme ./src/samples/java/com/azure/data/appconfiguration/ReadmeSamples.java#L126-L127 -->
```Java
ConfigurationSetting setting = configurationClient.setConfigurationSetting("some_key", "some_label", "some_value");
ConfigurationSetting deletedSetting = configurationClient.deleteConfigurationSetting("some_key", "some_label");
```
For conditional request, if you want to conditionally delete a configuration setting, set the `ifUnchanged` parameter 
to true. When `ifUnchanged` parameter to true. When `ifUnchanged` is true, the configuration setting is only deleted if 
it is same as the given `setting`. This is determined by comparing the ETag of the `setting` to the one in the service 
to see if they are the same or not. If the ETag are same, it means the configuration setting is same, and its value is deleted.

<!-- embedme ./src/samples/java/com/azure/data/appconfiguration/ReadmeSamples.java#L131-L132 -->
```Java
ConfigurationSetting setting = configurationClient.setConfigurationSetting("some_key", "some_label", "some_value");
Response<ConfigurationSetting> settingResponse = configurationClient.deleteConfigurationSettingWithResponse(setting, true, Context.NONE);
```

### List Configuration Settings with multiple keys

List multiple configuration settings by calling `listConfigurationSettings`.
Pass a null `SettingSelector` into the method if you want to fetch all the configuration settings and their fields.

<!-- embedme ./src/samples/java/com/azure/data/appconfiguration/ReadmeSamples.java#L136-L141 -->
```Java
String key = "some_key";
String key2 = "new_key";
configurationClient.setConfigurationSetting(key, "some_label", "some_value");
configurationClient.setConfigurationSetting(key2, "new_label", "new_value");
SettingSelector selector = new SettingSelector().setKeyFilter(key + "," + key2);
PagedIterable<ConfigurationSetting> settings = configurationClient.listConfigurationSettings(selector);
```

### List revisions of multiple Configuration Settings

List all revisions of a configuration setting by calling `listRevisions`.

<!-- embedme ./src/samples/java/com/azure/data/appconfiguration/ReadmeSamples.java#L145-L149 -->
```Java
String key = "revisionKey";
configurationClient.setConfigurationSetting(key, "some_label", "some_value");
configurationClient.setConfigurationSetting(key, "new_label", "new_value");
SettingSelector selector = new SettingSelector().setKeyFilter(key);
PagedIterable<ConfigurationSetting> settings = configurationClient.listRevisions(selector);
``` 

### Set a Configuration Setting to read only

Set a configuration setting to read-only status.

<!-- embedme ./src/samples/java/com/azure/data/appconfiguration/ReadmeSamples.java#L153-L154 -->
```Java
configurationClient.setConfigurationSetting("some_key", "some_label", "some_value");
ConfigurationSetting setting = configurationClient.setReadOnly("some_key", "some_label", true);
```
### Clear read only from a Configuration Setting

Clear read-only from a configuration setting.

<!-- embedme ./src/samples/java/com/azure/data/appconfiguration/ReadmeSamples.java#L158-L158 -->
```Java
ConfigurationSetting setting = configurationClient.setReadOnly("some_key", "some_label", false);
```

### Create a client with Proxy Options

Create a configuration client with proxy options.

<!-- embedme ./src/samples/java/com/azure/data/appconfiguration/ReadmeSamples.java#L175-L187 -->
```Java
// Proxy options
final String hostname = "{your-host-name}";
final int port = 447; // your port number

ProxyOptions proxyOptions = new ProxyOptions(ProxyOptions.Type.HTTP,
    new InetSocketAddress(hostname, port));
HttpClient httpClient = new NettyAsyncHttpClientBuilder()
    .proxy(proxyOptions)
    .build();
ConfigurationAsyncClient configurationAsyncClient = new ConfigurationClientBuilder()
    .connectionString("{your_connection_string}")
    .httpClient(httpClient)
    .buildAsyncClient();
```

## Troubleshooting

### General

When you interact with App Configuration using this Java client library, errors returned by the service correspond to the same HTTP status codes returned for [REST API][rest_api] requests. For example, if you try to retrieve a configuration setting that doesn't exist in your configuration store, a `404` error is returned, indicating `Not Found`.

App Configuration provides a way to define customized headers through `Context` object in the public API. 

<!-- embedme ./src/samples/java/com/azure/data/appconfiguration/ReadmeSamples.java#L162-L171 -->
```java
// Add your headers
HttpHeaders headers = new HttpHeaders();
headers.put("my-header1", "my-header1-value");
headers.put("my-header2", "my-header2-value");
headers.put("my-header3", "my-header3-value");
// Call API by passing headers in Context.
configurationClient.addConfigurationSettingWithResponse(
    new ConfigurationSetting().setKey("key").setValue("value"),
    new Context(AddHeadersFromContextPolicy.AZURE_REQUEST_HTTP_HEADERS_KEY, headers));
// Above three HttpHeader will be added in outgoing HttpRequest.
```
For more detail information, check out the [AddHeadersFromContextPolicy][add_headers_from_context_policy]

### Default HTTP Client
All client libraries by default use the Netty HTTP client. Adding the above dependency will automatically configure 
the client library to use the Netty HTTP client. Configuring or changing the HTTP client is detailed in the
[HTTP clients wiki](https://github.com/Azure/azure-sdk-for-java/wiki/HTTP-clients).

### Default SSL library
All client libraries, by default, use the Tomcat-native Boring SSL library to enable native-level performance for SSL 
operations. The Boring SSL library is an uber jar containing native libraries for Linux / macOS / Windows, and provides 
better performance compared to the default SSL implementation within the JDK. For more information, including how to 
reduce the dependency size, refer to the [performance tuning][performance_tuning] section of the wiki.

## Next steps

- Samples are explained in detail [here][samples_readme].
- [Quickstart: Create a Java Spring app with App Configuration][spring_quickstart]

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
[samples]: src/samples/java/com/azure/data/appconfiguration
[samples_readme]: src/samples/README.md
[source_code]: src
[spring_quickstart]: https://docs.microsoft.com/azure/azure-app-configuration/quickstart-java-spring-app

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fappconfiguration%2Fazure-data-appconfiguration%2FREADME.png)
