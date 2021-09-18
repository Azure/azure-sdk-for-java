# Spring Cloud for Azure starter App Configuration client library for Java

This package helps Spring Application to load properties from Azure Configuration Store.

[Package (Maven)][package] | [Samples][app_configuration_sample]

## Getting started

### Prerequisites

- Java Development Kit (JDK) with version 8 or above
- [Azure Subscription][azure_subscription]
- [Maven][maven] 3.0 and above

### Include the package

There are two libraries that can be used azure-spring-cloud-appconfiguration-config and azure-spring-cloud-appconfiguration-config-web. There are two differences between them the first being the web version takes on spring-web as a dependency, and the web version has various methods for refreshing configurations on a watch interval when the application is active. For more information on refresh see the [Configuration Refresh](#configuration-refresh) section.

[//]: # ({x-version-update-start;com.azure.spring:azure-spring-cloud-appconfiguration-config;current})
```xml
<dependency>
    <groupId>com.azure.spring</groupId>
    <artifactId>azure-spring-cloud-appconfiguration-config</artifactId>
    <version>2.1.0</version>
</dependency>
```
[//]: # ({x-version-update-end})

or

[//]: # ({x-version-update-start;com.azure.spring:azure-spring-cloud-appconfiguration-config;current})
```xml
<dependency>
    <groupId>com.azure.spring</groupId>
    <artifactId>azure-spring-cloud-appconfiguration-config-web</artifactId>
    <version>2.1.0</version>
</dependency>
```
[//]: # ({x-version-update-end})

## Key concepts

Azure App Configuration provides a service to centrally manage application settings and feature flags. Modern programs, especially programs running in a cloud, generally have many components that are distributed in nature. Spreading configuration settings across these components can lead to hard-to-troubleshoot errors during an application deployment. Use App Configuration to store all the settings for your application and secure their accesses in one place.

## Examples

Please use this `sample` as a reference for how to use this starter.

### Supported properties

Name | Description | Required | Default
---|---|---|---
spring.cloud.azure.appconfiguration.stores | List of configuration stores from which to load configuration properties | Yes | true
spring.cloud.azure.appconfiguration.enabled | Whether enable spring-cloud-azure-appconfiguration-config or not | No | true

`spring.cloud.azure.appconfiguration.stores` is a list of stores, where each store follows the following format:

Name | Description | Required | Default
---|---|---|---
spring.cloud.azure.appconfiguration.stores[0].enabled | Whether the store will be loaded. | No | true
spring.cloud.azure.appconfiguration.stores[0].fail-fast | Whether to throw a `RuntimeException` or not when failing to read from App Configuration during application start-up. If an exception does occur during startup when set to false the store is skipped. | No |  true
spring.cloud.azure.appconfiguration.stores[0].selects[0].key-filter | The key pattern used to indicate which configuration(s) will be loaded.  | No | /application/*
spring.cloud.azure.appconfiguration.stores[0].selects[0].label-filter | The label used to indicate which configuration(s) will be loaded. | No | `${spring.profiles.active}` or if null `\0`

Configuration Store Authentication

Name | Description | Required | Default
---|---|---|---
spring.cloud.azure.appconfiguration.stores[0].endpoint | When the endpoint of an App Configuration store is specified, a managed identity or a token credential provided using `AppConfigCredentialProvider` will be used to connect to the App Configuration service. An `IllegalArgumentException` will be thrown if the endpoint and connection-string are specified at the same time. | Conditional | null
spring.cloud.azure.appconfiguration.stores[0].connection-string | When the connection-string of an App Configuration store is specified, HMAC authentication will be used to connect to the App Configuration service. An `IllegalArgumentException` will be thrown if the endpoint and connection-string are specified at the same time. | Conditional | null
spring.cloud.azure.appconfiguration.stores[0].managed-identity.client-id | Client id of the user assigned managed identity, only required when choosing to use user assigned managed identity on Azure | No | null

`spring.cloud.azure.appconfiguration.stores[0].monitoring` is a set of configurations dealing with refresh of configurations:

Name | Description | Required | Default
---|---|---|---
spring.cloud.azure.appconfiguration.stores[0].monitoring.enabled | Whether the configurations and feature flags will be re-loaded if a change is detected.  | No | false
spring.cloud.azure.appconfiguration.stores[0].monitoring.refresh-interval | Amount of time, of type Duration, configurations are stored before a check can occur. | No | 30s
spring.cloud.azure.appconfiguration.stores[0].monitoring.feature-flags.watch-interval | Amount of time, of type Duration, feature flags are stored before a check can occur. | No | 30s
spring.cloud.azure.appconfiguration.stores[0].monitoring.triggers[0].key | A key that is watched for change via etag. If a change is detected on the key then a refresh of all configurations will be triggered. | Yes (If monitoring enabled) | null
spring.cloud.azure.appconfiguration.stores[0].monitoring.triggers[0].label | The label of the key that is being watched for etag changes. | No | \0
spring.cloud.azure.appconfiguration.stores[0].monitoring.push-notification.primary-token.name | The name of a token used with Event Hub to trigger push based refresh. | No | null
spring.cloud.azure.appconfiguration.stores[0].monitoring.push-notification.primary-token.secret | The secret value of a token used with Event Hub to trigger push based refresh. | No | null
spring.cloud.azure.appconfiguration.stores[0].monitoring.push-notification.secondary-token.name | The name of a token used with Event Hub to trigger push based refresh. | No | null
spring.cloud.azure.appconfiguration.stores[0].monitoring.push-notification.secondary-token.secret | The secret value of a token used with Event Hub to trigger push based refresh. | No | null

`spring.cloud.azure.appconfiguration.stores[x].feature-flags` is a set of configurations for the feature flags of the store:

Name | Description | Required | Default
---|---|---|---
spring.cloud.azure.appconfiguration.stores[0].feature-flags.enabled | Whether feature flags are loaded from the config store.  | No | false
spring.cloud.azure.appconfiguration.stores[0].feature-flags.label-filter | The label used to indicate which feature flags will be loaded. | No | \0

### Advanced usage

#### Load from multiple configuration stores

If the application needs to load configuration properties from multiple stores, following configuration sample describes how the bootstrap.properties(or .yaml) can be configured.

```properties
spring.cloud.azure.appconfiguration.stores[0].connection-string=[first-store-connection-string]
spring.cloud.azure.appconfiguration.stores[0].selects[0].label-filter=[my-label]
spring.cloud.azure.appconfiguration.stores[1].connection-string=[second-store-connection-string]
```

If duplicate keys exists for multiple stores, the last configuration store has the highest priority.

#### Load from multiple labels

If the application needs to load property values from multiple labels in the same configuration store, following configuration can be used:

```properties
spring.cloud.azure.appconfiguration.stores[0].connection-string=[first-store-connection-string]
spring.cloud.azure.appconfiguration.stores[0].selects[0].label-filter=[my-label1]
spring.cloud.azure.appconfiguration.stores[0].selects[1].label-filter=[my-label2]
```

Multiple labels can be separated with comma, if duplicate keys exists for multiple labels, the last label has highest priority.

#### Spring Profiles

Spring Profiles are supported by automatically by being set as App Configuration Labels. Using the label filter configuration overrides profile use. To include Spring Profiles and labels:

```properties
spring.cloud.azure.appconfiguration.stores[0].selects[0].label-filter=${spring.profiles.active},v1
```

If you need to use `(No Label)` you need to do the following:

```properties
spring.cloud.azure.appconfiguration.stores[0].selects[0].label-filter=,${spring.profiles.active}
```

where the empty value before the comma equals the `\0` value.

and for yaml

```yaml
spring:
  cloud:
    azure:
      appconfiguration:
        stores:
         -
           selects:
             -
              label-filter: ',${spring.profiles.active}'
```

#### Configuration Refresh

Configuration Refresh feature allows the application to load the latest property value from configuration store automatically, without restarting the application.

Changing a property key in the configuration store on Azure Portal, e.g., /application/config.message, log similar with below will be printed on the console.

```console
INFO 17496 --- [TaskScheduler-1] o.s.c.e.event.RefreshEventListener       : Refresh keys changed: [config.message]
```

The application now will be using the updated properties. By default, `@ConfigurationProperties` annotated beans will be automatically refreshed. Use `@RefreshScope` on beans which are required to be refreshed when properties are changed.

By default, all the keys following the pattern `/application/*` with the label `${spring.profiles.active}` or when no Spring Profile is set `(No Label)` is used. At least one watch key is required when monitoring is enabled.

```properties
spring.cloud.azure.appconfiguration.stores[0].monitoring.enabled=true
spring.cloud.azure.appconfiguration.stores[0].monitoring.triggers[0].key=[my-watched-key]
spring.cloud.azure.appconfiguration.stores[0].monitoring.triggers[0].label=[my-watched-label]
```

When using the web library, applications will attempt a refresh whenever a servlet request occurs after the watch interval time when monitoring is enabled.

In the console library calling refreshConfiguration on `AzureCloudConfigRefresh` will result in a refresh if the watch interval has passed. The web library can also use this method along with servlet request method.

##### Push Based Refresh

The Web Provider can be connect to your Azure App Configuration store via an Azure Event Grid Web Hook to trigger a refresh event. By adding the Spring Actuator as a dependency you can add App Configuration Refresh as an exposed endpoint. There are two options appconfiguration-refresh and appconfiguration-refresh-bus. These endpoints work just like there counterparts refresh and refresh-bus, but have the required web hook authorization to work with Azure Event Grid. When needing to refresh multiple application instances `azure-servicebus-jms-spring-boot-starter` needs to be setup to have the refresh triggered in all instances.

```properties
management.endpoints.web.exposure.include= appconfiguration-refresh, appconfiguration-refresh-bus
```

In addition a required query parameter has been added for security. No token name or value is set by default, but setting one is required in order to use the endpoints. It is suggested you set up your token value in Key Vault and add it to your store through a key vault reference. The values should be:

```properties
/application/spring.cloud.appconfiguration.stores[0].monitoring.push-notification.primary-token.name=[primary-token-name]
/application/spring.cloud.appconfiguration.stores[0].monitoring.push-notification.primary-token.secret=[primary-token-secret]
/application/spring.cloud.appconfiguration.stores[0].monitoring.push-notification.secondary-token.name=[secondary-token-name]
/application/spring.cloud.appconfiguration.stores[0].monitoring.push-notification.secondary-token.secret=[secondary-token-secret]
```

To setup the webhook open your app store and open the events tab. Select "+ Event Subscription". Set the name of your Event and select the Endpoint type of Web Hook. Select "Select an endpoint". You endpoint will be your look as following:

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

[Managed identity][azure_managed_identity] allows application to access [Azure Active Directory][azure_active_directory] protected resource on [Azure][azure].

In this library, [Azure Identity SDK][azure_identity_sdk] is used to access Azure App Configuration and optionally Azure Key Vault, for secrets. Only one method of authentication can be set at one time. When not using the AppConfigCredentialProvider and/or KeyVaultCredentialProvider the same authentication method is used for both App Configuration and Key Vault.

Follow the below steps to enable accessing App Configuration with managed identity:

1. [Enable managed identities][enable_managed_identities] for the [supported Azure services][support_azure_services], for example, virtual machine or App Service, on which the application will be deployed.

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

The service client builders used for connecting to App Configuration and Key Vault can be customized by implementing interfaces `ConfigurationClientBuilderSetup` and `SecretClientBuilderSetup` respectively. Generating and providing a `@Bean` of them will update the default service client builders used in [App Configuration SDK][app_configuration_SDK] and [Key Vault SDK][key_vault_SDK]. If necessary, the customization can be done per App Configuration store or Key Vault instance.

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

## Troubleshooting
### Enable client logging
Azure SDKs for Java offers a consistent logging story to help aid in troubleshooting application errors and expedite their resolution. The logs produced will capture the flow of an application before reaching the terminal state to help locate the root issue. View the [logging][logging] wiki for guidance about enabling logging.

### Enable Spring logging
Spring allow all the supported logging systems to set logger levels set in the Spring Environment (for example, in application.properties) by using `logging.level.<logger-name>=<level>` where level is one of TRACE, DEBUG, INFO, WARN, ERROR, FATAL, or OFF. The root logger can be configured by using logging.level.root.

The following example shows potential logging settings in `application.properties`:

```properties
logging.level.root=WARN
logging.level.org.springframework.web=DEBUG
logging.level.org.hibernate=ERROR
```

For more information about setting logging in spring, please refer to the [official doc][logging_doc].
 

## Next steps

The following section provide a sample project illustrating how to use the starter.
### More sample code
- [Azure App Configuration][app_configuration_sample]
- [Azure App Configuration Conversation Complete][app_configuration_conversation_complete_sample]
- [Azure App Configuration Conversation Initial][app_configuration_conversation_initail_sample]

## Contributing
This project welcomes contributions and suggestions.  Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

Please follow [instructions here][contributing_md] to build from source or contribute.

<!-- Link -->
[package]: https://mvnrepository.com/artifact/com.microsoft.azure/spring-cloud-azure-appconfiguration-config
[app_configuration_sample]: https://github.com/Azure-Samples/azure-spring-boot-samples/tree/tag_azure-spring-boot_3.6.0/appconfiguration/azure-appconfiguration-sample
[app_configuration_conversation_complete_sample]: https://github.com/Azure-Samples/azure-spring-boot-samples/tree/tag_azure-spring-boot_3.6.0/appconfiguration/azure-appconfiguration-conversion-sample-complete
[app_configuration_conversation_initail_sample]: https://github.com/Azure-Samples/azure-spring-boot-samples/tree/tag_azure-spring-boot_3.6.0/appconfiguration/azure-appconfiguration-conversion-sample-initial
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-with-Azure-SDK#use-logback-logging-framework-in-a-spring-boot-application
[azure_subscription]: https://azure.microsoft.com/free
[logging_doc]: https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#boot-features-logging
[contributing_md]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/spring/CONTRIBUTING.md
[maven]: https://maven.apache.org/
[spring_conversion_duration]: https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config.typesafe-configuration-properties.conversion.durations
[azure_managed_identity]: https://docs.microsoft.com/azure/active-directory/managed-identities-azure-resources/overview
[enable_managed_identities]: https://docs.microsoft.com/azure/active-directory/managed-identities-azure-resources/overview#how-can-i-use-managed-identities-for-azure-resources
[support_azure_services]: https://docs.microsoft.com/azure/active-directory/managed-identities-azure-resources/services-support-managed-identities
[azure]: https://azure.microsoft.com
[azure_active_directory]: https://azure.microsoft.com/services/active-directory/
[azure_identity_sdk]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/identity/azure-identity
[azure_rbac]: https://docs.microsoft.com/azure/role-based-access-control/role-assignments-portal
[app_configuration_SDK]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/appconfiguration/azure-data-appconfiguration#key-concepts
[key_vault_SDK]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/keyvault/azure-security-keyvault-secrets#key-concepts
