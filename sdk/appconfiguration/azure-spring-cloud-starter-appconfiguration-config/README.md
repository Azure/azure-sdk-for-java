# Azure Spring cloud starter App Configuration client library for Java

This project allows Spring Application to load properties from Azure Configuration Store.

## Key concepts

## Getting started

## Examples

Please use this `sample` as a reference for how to use this starter.

### Dependency Management

There are two libraries that can be used spring-cloud-azure-appconfiguration-config and spring-cloud-azure-appconfiguration-config-web. There are two differences between them the first being the web version takes on spring-web as a dependency, and the web version will attempt a refresh when the application is active when the cache expires. For more information on refresh see the [Configuration Refresh](#Configuration-Refresh) section.

#### Maven Coordinates

```xml
<dependency>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>spring-cloud-azure-appconfiguration-config</artifactId>
    <version>{version}</version>
</dependency>
```

or

```xml
<dependency>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>spring-cloud-azure-appconfiguration-config-web</artifactId>
    <version>{version}</version>
</dependency>
```

#### Gradle Coordinates

```gradle
dependencies {
    compile group: 'com.microsoft.azure', name: 'spring-cloud-azure-appconfiguration-config', version: '{starter-version}'
}
```

or

```gradle
dependencies {
    compile group: 'com.microsoft.azure', name: 'spring-cloud-azure-appconfiguration-config-web', version: '{starter-version}'
}
```

### Supported properties

Name | Description | Required | Default
---|---|---|---
spring.cloud.azure.appconfiguration.stores | List of configuration stores from which to load configuration properties | Yes | true
spring.cloud.azure.appconfiguration.enabled | Whether enable spring-cloud-azure-appconfiguration-config or not | No | true
spring.cloud.azure.appconfiguration.default-context | Default context path to load properties from | No | application
spring.cloud.azure.appconfiguration.name | Alternative to Spring application name, if not configured, fallback to default Spring application name | No | ${spring.application.name}
spring.cloud.azure.appconfiguration.profile-separator | Profile separator for the key name, e.g., /foo-app_dev/db.connection.key, must follow format `^[a-zA-Z0-9_@]+$` | No | `_`
spring.cloud.azure.appconfiguration.cache-expiration | Amount of time, of type [Duration](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html#boot-features-external-config-conversion-duration), configurations are stored before a check can occur. | No | 30s
spring.cloud.azure.appconfiguration.managed-identity.client-id | Client id of the user assigned managed identity, only required when choosing to use user assigned managed identity on Azure | No | null

`spring.cloud.azure.appconfiguration.stores` is a List of stores, for each store should follow below format:

Name | Description | Required | Default
---|---|---|---
spring.cloud.azure.appconfiguration.stores[0].endpoint | When the endpoint of an App Configuration store is specified, a managed identity or a token credential provided using `AppConfigCredentialProvider` will be used to connect to the App Configuration service. An `IllegalArgumentException` will be thrown if the endpoint and connection-string are specified at the same time. | Conditional | null
spring.cloud.azure.appconfiguration.stores[0].prefix | The prefix of the key name in the configuration store, e.g., /my-prefix/application/key.name | No |  null
spring.cloud.azure.appconfiguration.stores[0].connection-string | When the connection-string of an App Configuration store is specified, HMAC authentication will be used to connect to the App Configuration service. An `IllegalArgumentException` will be thrown if the endpoint and connection-string are specified at the same time. | Conditional | null
spring.cloud.azure.appconfiguration.stores[0].label | Comma separated list of label values, by default will query *(No label)* labeled values. If you want to specify *(No label)* label explicitly, use `\0`, e.g., spring.cloud.azure.appconfiguration.stores[0].label=\0,v0 | No |  null
spring.cloud.azure.appconfiguration.stores[0].fail-fast | Whether throw `RuntimeException` or not when fail to read App Configuration during application start-up. If an exception does occur during startup when set to false the store is skipped. | No |  true
spring.cloud.azure.appconfiguration.stores[0].watched-key | The single watched key(or by default *) used to indicate configuration change.  | No | *

### Advanced usage

#### Load from multiple configuration stores

If the application needs to load configuration properties from multiple stores, following configuration sample describes how the bootstrap.properties(or .yaml) can be configured.

```properties
spring.cloud.azure.appconfiguration.stores[0].connection-string=[first-store-connection-string]
spring.cloud.azure.appconfiguration.stores[0].prefix=[my-prefix]
spring.cloud.azure.appconfiguration.stores[0].label=[my-label]
spring.cloud.azure.appconfiguration.stores[1].connection-string=[second-store-connection-string]
```

If duplicate keys exists for multiple stores, the last configuration store has the highest priority.

#### Load from multiple labels

If the application needs to load property values from multiple labels in the same configuration store, following configuration can be used:

```properties
spring.cloud.azure.appconfiguration.stores[0].connection-string=[first-store-connection-string]
spring.cloud.azure.appconfiguration.stores[0].label=[my-label1], [my-label2]
```

Multiple labels can be separated with comma, if duplicate keys exists for multiple labels, the last label has highest priority.

#### Spring Profiles

Spring Profiles are supported by setting labels on your configurations that match your profile. Then set your label on your config store:

```properties
spring.cloud.azure.appconfiguration.stores[0].label=${spring.profiles.active}
```

#### Configuration Refresh

Configuration Refresh feature allows the application to load the latest property value from configuration store automatically, without restarting the application.

Changing a property key in the configuration store on Azure Portal, e.g., /application/config.message, log similar with below will be printed on the console.

```console
INFO 17496 --- [TaskScheduler-1] o.s.c.e.event.RefreshEventListener       : Refresh keys changed: [config.message]
```

The application now will be using the updated properties. By default, `@ConfigurationProperties` annotated beans will be automatically refreshed. Use `@RefreshScope` on beans which are required to be refreshed when properties are changed.

By default, all the keys in a configuration store will be watched. To prevent configuration changes are picked up in the middle of an update of multiple keys, you are recommended to use the watched-key property to watch a specific key that signals the completion of your update so all configuration changes can be refreshed together.

```properties
spring.cloud.azure.appconfiguration.stores[0].watched-key=[my-watched-key]
```

When using the web library, applications will attempt a refresh whenever a servlet request occurs after the cache expiration time.

In the console library calling refreshConfiguration on `AzureCloudConfigRefresh` will result in a refresh if the cache has expired. The web library can also use this method along with servlet request method.

#### Push Based Refresh

The Web Provider can be connect to your Azure App Configuration store via an Azure Event Grid Web Hook to trigger a refresh event. By adding the Spring Actuator as a dependency you can add App Configuration Refresh as an exposed endpoint. There are two options appconfiguration-refresh and appconfiguration-refresh-bus. These endpoints work just like there counterparts refresh and refresh-bus, but have the required web hook authorization to work with Azure Event Grid.

```properties
management.endpoints.web.exposure.include= appconfiguration-refresh, appconfiguration-refresh-bus
```

In addition a required query parameter has been added for security. No token name or value is set by default, but setting one is required in order to use the endpoints. We suggest you set up your token value in Key Vault and add it to your store through a key vault reference. The values should be:

```properties
/application/spring.cloud.appconfiguration.token-name
/application/spring.cloud.appconfiguration.token-secret

 or

/YOUR_APPLICATION_NAME/spring.cloud.appconfiguration.token-name
/YOUR_APPLICATION_NAME/spring.cloud.appconfiguration.token-secret
```

To setup the webhook open your app store and open the events tab. Select "+ Event Subscription". Set the name of your Event and selent the Endpoint type of Web Hook. Select "Select an endpoint". You endpoint will be your look as following:

`http://myApplication.azurewebsites.net/actuator/appconfiguration-refresh?myTokenName=mySecret`

Your application will need to be up and running with token-name and token-secret set as Selecting Confirm Selection will validate the endpoint.

Note: This validation only happens on the creation/modification of the endpoint.

It is also highly recommended that filters are setup as otherwise a refresh will be triggered after every key creation and modification.

#### Failfast

Failfast feature decides whether throw RuntimeException or not when exception happens. If an exception does occur when false the store is skipped. Any store skipped on startup will be automatically skipped on Refresh. By default, failfast is enabled, it can be disabled with below configuration:

```properties
spring.cloud.azure.appconfiguration.stores[0].fail-fast=false
```

#### Placeholders in App Configuration

The values in App Configuration are filtered through the existing Environment when they are used. Placeholders can be used just like in `application.properties`, but with the added benefit of support for key vault references. Example with kafka:

```properties
/application/app.name=MyApp
/application/app.description=${app.name} is configured with Azure App Configuration
```

#### Use Managed Identity to access App Configuration

[Managed identity](https://docs.microsoft.com/azure/active-directory/managed-identities-azure-resources/overview) allows application to access [Azure Active Directory][azure_active_directory] protected resource on [Azure][azure].

In this library, [Azure Identity SDK][azure_identity_sdk] is used to access Azure App Configuration and optionally Azure Key Vault, for secrets. Only one method of authentication can be set at one time. When not using the AppConfigCredentialProvider and/or KeyVaultCredentialProvider the same authentication method is used for both App Configuration and Key Vault.

Follow the below steps to enable accessing App Configuration with managed identity:

1. [Enable managed identities](https://docs.microsoft.com/azure/active-directory/managed-identities-azure-resources/overview#how-can-i-use-managed-identities-for-azure-resources) for the [supported Azure services](https://docs.microsoft.com/azure/active-directory/managed-identities-azure-resources/services-support-managed-identities), for example, virtual machine or App Service, on which the application will be deployed.

1. Configure the [Azure RBAC][azure_rbac] of your App Configuration store to grant access to the Azure service where your application is running. Select the App Configuration Data Reader. The App Configuration Data Owner role is not required but can be used if needed.

1. Configure bootstrap.properties(or .yaml) in the Spring Boot application.

The configuration store endpoint must be configured when `connection-string` is empty. When using a User Assigned Id the value `spring.cloud.azure.appconfiguration.managed-identity.client-id=[client-id]` must be set.

##### bootstrap.application

```application
spring.cloud.azure.appconfiguration.stores[0].endpoint=[config-store-endpoint]

#If Using User Assigned Identity
spring.cloud.azure.appconfiguration.managed-identity.client-id=[client-id]
```

#### Token Credential Provider

Another method of authentication is using AppConfigCredentialProvider and/or KeyVaultCredentialProvider. By implementing either of these classes and providing and generating a @Bean of them will enable authentication through any method defined by the [Java Azure SDK][azure_identity_sdk]. The uri value is the endpoint/dns name of the connection service, so if needed different credentials can be used per config store/key vault.

```java
public class MyCredentials implements AppConfigCredentialProvider, KeyVaultCredentialProvider {

    @Override
    public TokenCredential getAppConfigCredential(String uri) {
            return buildCredential();
    }

    @Override
    public TokenCredential getKeyVaultCredential(String uri) {
            return buildCredential();
    }

    TokenCredential buildCredential() {
            return new DefaultAzureCredentialBuilder().build();
    }

}
```

#### Client Builder Customization

The service client builders used for connecting to App Configuration and Key Vault can be customized by implementing interfaces `ConfigurationClientBuilderSetup` and `SecretClientBuilderSetup` respectively. Generating and providing a `@Bean` of them will update the default service client builders used in [App Configuration SDK](https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/appconfiguration/azure-data-appconfiguration#key-concepts) and [Key Vault SDK](https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/keyvault/azure-security-keyvault-secrets#key-concepts). If necessary, the customization can be done per App Configuration store or Key Vault instance.

```java
public interface ConfigurationClientBuilderSetup {
    public void setup(ConfigurationClientBuilder builder, String endpoint);
}

public interface SecretClientBuilderSetup {
    public void setup(SecretClientBuilder builder, String uri);
}
```

For example, the following implementation of `MyClient` replaces the default `HttpClient` with one using a proxy for all traffic to App Configuration and Key Vault.

```java
public class MyClient implements ConfigurationClientBuilderSetup, SecretClientBuilderSetup {

    @Override
    public void setup(ConfigurationClientBuilder builder, String endpoint) {
        builder.httpClient(buildHttpClient());
    }

    @Override
    public void setup(SecretClientBuilder builder, String uri) {
        builder.httpClient(buildHttpClient());
    }

    private HttpClient buildHttpClient() {
        String hostname = System.getProperty("https.proxyHosts");
        String portString = System.getProperty("https.proxyPort");
        int port = Integer.valueOf(portString);

        ProxyOptions proxyOptions = new ProxyOptions(ProxyOptions.Type.HTTP,
                new InetSocketAddress(hostname, port));
        return new NettyAsyncHttpClientBuilder()
                .proxy(proxyOptions)
                .build();
    }

}
```

<!-- LINKS -->
[azure]: https://azure.microsoft.com
[azure_active_directory]: https://azure.microsoft.com/services/active-directory/
[azure_identity_sdk]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/identity/azure-identity
[azure_rbac]: https://docs.microsoft.com/azure/role-based-access-control/role-assignments-portal

## Troubleshooting
## Next steps
## Contributing
