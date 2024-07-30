# Azure App Configuration client library for Java
Azure App Configuration is a managed service that helps developers centralize their application configurations simply and securely.

Modern programs, especially programs running in a cloud, generally have many components that are distributed in nature. Spreading configuration settings across these components can lead to hard-to-troubleshoot errors during an application deployment. Use App Configuration to store all the settings for your application and secure their accesses in one place.

Use the client library for App Configuration to create and manage application configuration settings.

[Source code][source_code] | [Package (Maven)][package] | [API reference documentation][api_documentation]
| [Product documentation][app_config_docs] | [Samples][samples] | [Troubleshooting][troubleshooting]

## Getting started

### Prerequisites

- A [Java Development Kit (JDK)][jdk_link], version 8 or later.
  - Here are details about [Java 8 client compatibility with Azure Certificate Authority](https://learn.microsoft.com/azure/security/fundamentals/azure-ca-details?tabs=root-and-subordinate-cas-list#client-compatibility-for-public-pkis).
- [Azure Subscription][azure_subscription]
- [App Configuration Store][app_config_store]

### Include the Package
#### Include the BOM file

Please include the azure-sdk-bom to your project to take dependency on the General Availability (GA) version of the library. In the following snippet, replace the {bom_version_to_target} placeholder with the version number.
To learn more about the BOM, see the [AZURE SDK BOM README](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/boms/azure-sdk-bom/README.md).

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.azure</groupId>
            <artifactId>azure-sdk-bom</artifactId>
            <version>{bom_version_to_target}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```
and then include the direct dependency in the dependencies section without the version tag as shown below.

```xml
<dependencies>
  <dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-data-appconfiguration</artifactId>
  </dependency>
</dependencies>
```

#### Include direct dependency
If you want to take dependency on a particular version of the library that is not present in the BOM,
add the direct dependency to your project as follows.

[//]: # ({x-version-update-start;com.azure:azure-data-appconfiguration;current})
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-data-appconfiguration</artifactId>
  <version>1.6.3</version>
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

```java readme-sample-createClient
ConfigurationClient configurationClient = new ConfigurationClientBuilder()
    .connectionString(connectionString)
    .buildClient();
```

or

```java readme-sample-createAsyncClient
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
```bash
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
```bash
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

```java readme-sample-aadAuthentication
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

Azure App Configuration allows users to create a point-in-time snapshot of their configuration store, providing them with the ability to treat settings as one consistent version. This feature enables applications to hold a consistent view of configuration, ensuring that there are no version mismatches to individual settings due to reading as updates were made. Snapshots are immutable, ensuring that configuration can confidently be rolled back to a last-known-good configuration in the event of a problem.

### Configuration Client

The client performs the interactions with the App Configuration service, getting, setting, deleting, and selecting configuration settings. An asynchronous, `ConfigurationAsyncClient`, and synchronous, `ConfigurationClient`, client exists in the SDK allowing for selection of a client based on an application's use case.

An application that needs to retrieve startup configurations is better suited using the synchronous client, for example setting up a SQL connection.

```java readme-sample-sqlExample
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

```java readme-sample-listConfigurationsExample
ConfigurationAsyncClient configurationClient = new ConfigurationClientBuilder()
    .connectionString(connectionString)
    .buildAsyncClient();

configurationClient.listConfigurationSettings(new SettingSelector().setLabelFilter(periodicUpdateLabel))
    .subscribe(setting -> updateConfiguration(setting));
```

## Examples

The following sections provide several code snippets covering some of the most common configuration service tasks, including:
For "Feature Flag" and "Secret Reference" configuration settings, see [samples][samples_readme] for more detail.

* [Create a Configuration Client](#create-a-client)
* [Create a Configuration Setting](#create-a-configuration-setting)
* [Retrieve a Configuration Setting](#retrieve-a-configuration-setting)
* [Update an existing Configuration Setting](#update-an-existing-configuration-setting)
* [Delete a Configuration Setting](#delete-a-configuration-setting)
* [List Configuration Settings with multiple keys](#list-configuration-settings-with-multiple-keys)
* [List revisions of multiple Configuration Settings](#list-revisions-of-multiple-configuration-settings)
* [Set a Configuration Setting to read only](#set-a-configuration-setting-to-read-only)
* [Clear read only from a Configuration Setting](#clear-read-only-from-a-configuration-setting)
* [Create a client with Proxy Options](#create-a-client-with-proxy-options)
* [Create a Snapshot](#create-a-snapshot)
* [Retrieve a Snapshot](#retrieve-a-snapshot)
* [Archive a Snapshot](#archive-a-snapshot)
* [Recover a snapshot](#recover-a-snapshot)
* [Retrieve all Snapshots](#retrieve-all-snapshots)
* [Retrieve Configuration Settings in a Snapshot](#retrieve-configuration-settings-in-a-snapshot)

### Create a Configuration Client

Create a configuration client by using `ConfigurationClientBuilder` by passing connection string.

```java readme-sample-createClient
ConfigurationClient configurationClient = new ConfigurationClientBuilder()
    .connectionString(connectionString)
    .buildClient();
```

### Create a Configuration Setting

Create a configuration setting to be stored in the configuration store. There are two ways to store a configuration setting:

- `addConfigurationSetting` creates a setting only if the setting does not already exist in the store.

```java readme-sample-addConfigurationSetting
ConfigurationSetting setting = configurationClient.addConfigurationSetting("new_key", "new_label", "new_value");
```

Or

- `setConfigurationSetting` creates a setting if it doesn't exist or overrides an existing setting.

```java readme-sample-setConfigurationSetting
ConfigurationSetting setting = configurationClient.setConfigurationSetting("some_key", "some_label", "some_value");
```

Create a Feature Flag configuration setting or Secrete Reference configuration setting to be stored in the
configuration store. 

```java readme-sample-addFeatureFlagConfigurationSetting
String key = "some_key";
String filterName = "{filter_name}"; // such as "Microsoft.Percentage"
String filterParameterKey = "{filter_parameter_key}"; // "Value"
Object filterParameterValue = 30; // Any value. Could be String, primitive value, or Json Object
FeatureFlagFilter percentageFilter = new FeatureFlagFilter(filterName)
                                         .addParameter(filterParameterKey, filterParameterValue);
FeatureFlagConfigurationSetting featureFlagConfigurationSetting =
    new FeatureFlagConfigurationSetting(key, true)
        .setClientFilters(Arrays.asList(percentageFilter));

FeatureFlagConfigurationSetting setting = (FeatureFlagConfigurationSetting)
    configurationClient.addConfigurationSetting(featureFlagConfigurationSetting);
```
```java readme-sample-addSecretReferenceConfigurationSetting
String key = "{some_key}";
String keyVaultReference = "{key_vault_reference}";

SecretReferenceConfigurationSetting referenceConfigurationSetting =
    new SecretReferenceConfigurationSetting(key, keyVaultReference);

SecretReferenceConfigurationSetting setting = (SecretReferenceConfigurationSetting)
    configurationClient.addConfigurationSetting(referenceConfigurationSetting);
```

### Retrieve a Configuration Setting

Retrieve a previously stored configuration setting by calling `getConfigurationSetting`.

```java readme-sample-getConfigurationSetting
ConfigurationSetting setting = configurationClient.setConfigurationSetting("some_key", "some_label", "some_value");
ConfigurationSetting retrievedSetting = configurationClient.getConfigurationSetting("some_key", "some_label");
```

For conditional request, if you want to conditionally fetch a configuration setting, set `ifChanged` to true. 
When `ifChanged` is true, the configuration setting is only retrieved if it is different from the given `setting`. 
This is determined by comparing the ETag of the `setting` to the one in the service to see if they are the same or not.
If the ETags are not the same, it means the configuration setting is different, and its value is retrieved.

```java readme-sample-getConfigurationSettingConditionally
ConfigurationSetting setting = configurationClient.setConfigurationSetting("some_key", "some_label", "some_value");
Response<ConfigurationSetting> settingResponse = configurationClient.getConfigurationSettingWithResponse(setting, null, true, Context.NONE);
```

Retrieve a Feature Flag configuration setting or Secrete Reference configuration setting in the configuration store.

```java readme-sample-getFeatureFlagConfigurationSetting
FeatureFlagConfigurationSetting setting = (FeatureFlagConfigurationSetting)
    configurationClient.getConfigurationSetting(featureFlagConfigurationSetting);
```
```java readme-sample-getSecretReferenceConfigurationSetting
SecretReferenceConfigurationSetting setting = (SecretReferenceConfigurationSetting)
    configurationClient.getConfigurationSetting(referenceConfigurationSetting);
```

### Update an existing Configuration Setting

Update an existing configuration setting by calling `setConfigurationSetting`.

```java readme-sample-updateConfigurationSetting
ConfigurationSetting setting = configurationClient.setConfigurationSetting("some_key", "some_label", "some_value");
ConfigurationSetting updatedSetting = configurationClient.setConfigurationSetting("some_key", "some_label", "new_value");
```

For conditional request, if you want to conditionally update a configuration setting, set the `ifUnchanged` parameter to
true. When `ifUnchanged` is true, the configuration setting is only updated if it is same as the given `setting`.
This is determined by comparing the ETag of the `setting` to the one in the service to see if they are the same or not.
If the ETag are the same, it means the configuration setting is same, and its value is updated.

```java readme-sample-updateConfigurationSettingConditionally
ConfigurationSetting setting = configurationClient.setConfigurationSetting("some_key", "some_label", "some_value");
Response<ConfigurationSetting> settingResponse = configurationClient.setConfigurationSettingWithResponse(setting, true, Context.NONE);
```

Update a Feature Flag configuration setting or Secrete Reference configuration setting in the configuration store.

```java readme-sample-updateFeatureFlagConfigurationSetting
FeatureFlagConfigurationSetting setting = (FeatureFlagConfigurationSetting)
    configurationClient.setConfigurationSetting(featureFlagConfigurationSetting);
```
```java readme-sample-updateSecretReferenceConfigurationSetting
SecretReferenceConfigurationSetting setting = (SecretReferenceConfigurationSetting)
    configurationClient.setConfigurationSetting(referenceConfigurationSetting);
```

### Delete a Configuration Setting

Delete an existing configuration setting by calling `deleteConfigurationSetting`.

```java readme-sample-deleteConfigurationSetting
ConfigurationSetting setting = configurationClient.setConfigurationSetting("some_key", "some_label", "some_value");
ConfigurationSetting deletedSetting = configurationClient.deleteConfigurationSetting("some_key", "some_label");
```
For conditional request, if you want to conditionally delete a configuration setting, set the `ifUnchanged` parameter 
to true. When `ifUnchanged` parameter to true. When `ifUnchanged` is true, the configuration setting is only deleted if 
it is same as the given `setting`. This is determined by comparing the ETag of the `setting` to the one in the service 
to see if they are the same or not. If the ETag are same, it means the configuration setting is same, and its value is deleted.

```java readme-sample-deleteConfigurationSettingConditionally
ConfigurationSetting setting = configurationClient.setConfigurationSetting("some_key", "some_label", "some_value");
Response<ConfigurationSetting> settingResponse = configurationClient.deleteConfigurationSettingWithResponse(setting, true, Context.NONE);
```

Delete a Feature Flag configuration setting or Secrete Reference configuration setting in the configuration store.

```java readme-sample-deleteFeatureFlagConfigurationSetting
FeatureFlagConfigurationSetting setting = (FeatureFlagConfigurationSetting)
    configurationClient.deleteConfigurationSetting(featureFlagConfigurationSetting);
```
```java readme-sample-deleteSecretReferenceConfigurationSetting
SecretReferenceConfigurationSetting setting = (SecretReferenceConfigurationSetting)
    configurationClient.deleteConfigurationSetting(referenceConfigurationSetting);
```

### List Configuration Settings with multiple keys

List multiple configuration settings by calling `listConfigurationSettings`.
Pass a null `SettingSelector` into the method if you want to fetch all the configuration settings and their fields.

```java readme-sample-listConfigurationSetting
String key = "some_key";
String key2 = "new_key";
configurationClient.setConfigurationSetting(key, "some_label", "some_value");
configurationClient.setConfigurationSetting(key2, "new_label", "new_value");
SettingSelector selector = new SettingSelector().setKeyFilter(key + "," + key2);
PagedIterable<ConfigurationSetting> settings = configurationClient.listConfigurationSettings(selector);
```

### List revisions of multiple Configuration Settings

List all revisions of a configuration setting by calling `listRevisions`.

```java readme-sample-listRevisions
String key = "revisionKey";
configurationClient.setConfigurationSetting(key, "some_label", "some_value");
configurationClient.setConfigurationSetting(key, "new_label", "new_value");
SettingSelector selector = new SettingSelector().setKeyFilter(key);
PagedIterable<ConfigurationSetting> settings = configurationClient.listRevisions(selector);
```

### Set a Configuration Setting to read only

Set a configuration setting to read-only status.

```java readme-sample-setReadOnly
configurationClient.setConfigurationSetting("some_key", "some_label", "some_value");
ConfigurationSetting setting = configurationClient.setReadOnly("some_key", "some_label", true);
```

### Clear read only from a Configuration Setting

Clear read-only from a configuration setting.

```java readme-sample-clearReadOnly
ConfigurationSetting setting = configurationClient.setReadOnly("some_key", "some_label", false);
```

### Create a client with Proxy Options

Create a configuration client with proxy options.

```java readme-sample-createClientWithProxyOption
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

### Create a Snapshot

To create a snapshot, you need to instantiate the `ConfigurationSnapshot` class and specify filters to determine 
which configuration settings should be included. The creation process is a Long-Running Operation (LRO) and can be 
achieved by calling the `beginCreateSnapshot` method.

```java readme-sample-createSnapshot
String snapshotName = "{snapshotName}";
// Prepare the snapshot filters
List<ConfigurationSettingsFilter> filters = new ArrayList<>();
// Key Name also supports RegExp but only support prefix end with "*", such as "k*" and is case-sensitive.
filters.add(new ConfigurationSettingsFilter("Test*"));
SyncPoller<PollOperationDetails, ConfigurationSnapshot> poller =
    configurationClient.beginCreateSnapshot(snapshotName, new ConfigurationSnapshot(filters), Context.NONE);
poller.setPollInterval(Duration.ofSeconds(10));
poller.waitForCompletion();
ConfigurationSnapshot snapshot = poller.getFinalResult();
System.out.printf("Snapshot name=%s is created at %s, snapshot status is %s.%n",
    snapshot.getName(), snapshot.getCreatedAt(), snapshot.getStatus());
```

### Retrieve a Snapshot

Once a configuration setting snapshot is created, you can retrieve it using the `getSnapshot` method.

```java readme-sample-getSnapshot
String snapshotName = "{snapshotName}";
ConfigurationSnapshot getSnapshot = configurationClient.getSnapshot(snapshotName);
System.out.printf("Snapshot name=%s is created at %s, snapshot status is %s.%n",
    getSnapshot.getName(), getSnapshot.getCreatedAt(), getSnapshot.getStatus());
```

### Archive a Snapshot

To archive a snapshot, you can utilize the `archiveSnapshot` method. This operation updates the status of the snapshot 
to `archived`.

```java readme-sample-archiveSnapshot
String snapshotName = "{snapshotName}";
ConfigurationSnapshot archivedSnapshot = configurationClient.archiveSnapshot(snapshotName);
System.out.printf("Archived snapshot name=%s is created at %s, snapshot status is %s.%n",
    archivedSnapshot.getName(), archivedSnapshot.getCreatedAt(), archivedSnapshot.getStatus());
```

### Recover a snapshot

You can recover an archived snapshot by using the `recoverSnapshot` method. This operation updates the status of the 
snapshot to `ready`.

```java readme-sample-recoverSnapshot
String snapshotName = "{snapshotName}";
ConfigurationSnapshot recoveredSnapshot = configurationClient.recoverSnapshot(snapshotName);
System.out.printf("Recovered snapshot name=%s is created at %s, snapshot status is %s.%n",
    recoveredSnapshot.getName(), recoveredSnapshot.getCreatedAt(), recoveredSnapshot.getStatus());
```

### Retrieve all Snapshots

To retrieve all snapshots, you can use the `listSnapshots` method.

```java readme-sample-getAllSnapshots
String snapshotNameProduct = "{snapshotNameInProduct}";
SnapshotSelector snapshotSelector = new SnapshotSelector().setNameFilter(snapshotNameProduct);
PagedIterable<ConfigurationSnapshot> configurationSnapshots =
    configurationClient.listSnapshots(snapshotSelector);
for (ConfigurationSnapshot snapshot : configurationSnapshots) {
    System.out.printf("Listed Snapshot name = %s is created at %s, snapshot status is %s.%n",
        snapshot.getName(), snapshot.getCreatedAt(), snapshot.getStatus());
}
```

### Retrieve Configuration Settings in a Snapshot
List multiple configuration settings in a snapshot by calling `listConfigurationSettingsForSnapshot`.

```java readme-sample-listSettingsInSnapshot
String snapshotNameProduct = "{snapshotNameInProduct}";
PagedIterable<ConfigurationSetting> configurationSettings =
    configurationClient.listConfigurationSettingsForSnapshot(snapshotNameProduct);

for (ConfigurationSetting setting : configurationSettings) {
    System.out.printf("[ConfigurationSetting in snapshot] Key: %s, Value: %s%n",
        setting.getKey(), setting.getValue());
}
```

## Troubleshooting

### General

When you interact with App Configuration using this Java client library, errors returned by the service correspond to the same HTTP status codes returned for [REST API][rest_api] requests. For example, if you try to retrieve a configuration setting that doesn't exist in your configuration store, a `404` error is returned, indicating `Not Found`.

App Configuration provides a way to define customized headers through `Context` object in the public API. 

```java readme-sample-customHeaders
// Add your headers
HttpHeaders headers = new HttpHeaders();
headers.set("my-header1", "my-header1-value");
headers.set("my-header2", "my-header2-value");
headers.set("my-header3", "my-header3-value");
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
[add_headers_from_context_policy]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core/src/main/java/com/azure/core/http/policy/AddHeadersFromContextPolicy.java
[api_documentation]: https://aka.ms/java-docs
[app_config_store]: https://docs.microsoft.com/azure/azure-app-configuration/quickstart-dotnet-core-app#create-an-app-configuration-store
[app_config_role]: https://docs.microsoft.com/azure/azure-app-configuration/rest-api-authorization-azure-ad#roles
[app_config_docs]: https://docs.microsoft.com/azure/azure-app-configuration
[azure_cli]: https://docs.microsoft.com/cli/azure
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/identity/azure-identity
[azure_subscription]: https://azure.microsoft.com/free
[cla]: https://cla.microsoft.com
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com
[default_cred_ref]: https://azuresdkdocs.blob.core.windows.net/$web/java/azure-identity/1.0.1/com/azure/identity/DefaultAzureCredential.html
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
[maven]: https://maven.apache.org/
[package]: https://central.sonatype.com/artifact/com.azure/azure-data-appconfiguration
[performance_tuning]: https://github.com/Azure/azure-sdk-for-java/wiki/Performance-Tuning
[rest_api]: https://github.com/Azure/AppConfiguration#rest-api-reference
[samples]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/appconfiguration/azure-data-appconfiguration/src/samples/java/com/azure/data/appconfiguration
[samples_readme]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/appconfiguration/azure-data-appconfiguration/src/samples/README.md
[source_code]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/appconfiguration/azure-data-appconfiguration/src
[spring_quickstart]: https://docs.microsoft.com/azure/azure-app-configuration/quickstart-java-spring-app
[troubleshooting]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/appconfiguration/azure-data-appconfiguration/TROUBLESHOOTING.md
![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fappconfiguration%2Fazure-data-appconfiguration%2FREADME.png)
