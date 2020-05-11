# Authentication in Azure Management Library for Java

To use the APIs in the Azure Management Library for Java, as the first step you need to create an authenticated client. This document is to introduce several possible approaches for authentication.

## Prerequisites

Before authenticating the Azure client, there are a few authentication classes you need to know before you decide which kind of authentication would meet your requirement.

* TokenCredential
  * The `TokenCredential` is an interface in the `azure-core` package for credentials that can provide a token. 
  * Azure Identity offers multiple implementations of the `TokenCredential` class in the `azure-identity` package. To learn more, see [credentials in Azure Identity](https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/identity/azure-identity#credentials).

Sample code to create a simple `ClientSecretCredential`:

```java
ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
    .clientId("<YOUR_CLIENT_ID>")
    .clientSecret("<YOUR_CLIENT_SECRET>")
    .tenantId("<YOUR_TENANT_ID>")
	.authorityHost("<AZURE_AUTHORITY_HOST>")
    .build();
```

Please note, most of credentials require a [service principal registration](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/management/samples/src/main/java/com/azure/management/graphrbac/samples/ManageServicePrincipalCredentials.java). Alternatively, you could also register a service principal in the [Azure portal](https://portal.azure.com/).

* AzureProfile
  * The `AzureProfile` is a class holding `AzureEnvironment`, `subscriptionId`, `tenantId` to configure the requests sending to wire. 
  * The `subscriptionId` is mandantory for most resource management while the `tenantId` would be required only for Graph RBAC. They can be set via environment variables.
  
|variable name|value
|-|-
|`AZURE_TENANT_ID`|id of the principal's Azure Active Directory tenant
|`AZURE_SUBSCRIPTION_ID`|id of the subscription for the Azure resources

Sample code to create a `AzureProfile`:

```java
//AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE, true);
AzureProfile profile = new AzureProfile("<YOUR_TENANT_ID>", "<YOUR_SUBSCRIPTION_ID>", AzureEnvironment.AZURE);
```

* HttpPipelinePolicy
  * The `HttpPipelinePolicy` is an interface that process provided request context and invokes the next policy. To learn more, see [policy in Azure Core](https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/core/azure-core/src/main/java/com/azure/core/http/policy) and [policy in the library](https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/resources/mgmt/src/main/java/com/azure/management/resources/fluentcore/policy).


* HttpClient
  * The `HttpClient` is a generic interface for sending HTTP requests and getting responses. 
  * [azure-core-http-netty](https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/core/azure-core-http-netty) provides a Netty derived HTTP client.
  * [azure-core-http-okhttp](https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/core/azure-core-http-okhttp) provides an OkHttp derived HTTP client.


* HttpPipeline
  * The `HttpPipeline` is a class that HTTP requests and responses will flow through. It is a construct that contains a list of `HttpPipelinePolicy` which are applied to a request sequentially to prepare it being sent by an `HttpClient`.

Sample code to create a `HttpPipeline`:

```java
HttpPipeline httpPipeline = new HttpPipelineBuilder()
    .policies(httpPipelinePolicies)
	.httpClient(httpClient)
	.build();
```

## Simple Authentication

If you want to authenticate as simple as possible, you only need to provide `TokenCredential` and `AzureProfile`. The library will help build http pipeline internally with [default configuration](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resources/mgmt/src/main/java/com/azure/management/resources/fluentcore/utils/HttpPipelineProvider.java#L43).

```java
Azure azure = Azure.authenticate(credential, profile).withDefaultSubscription();
```

The `Authenticated` class provides access to a subset of Azure APIs that do not require a specific subscription. If the profile does not contain a subscription, you can select a subscription via `Authenticated::subscriptions`.

```java
Azure.Authenticated authenticated = Azure.authenticate(credential, profile);
String subscriptionId = authenticated.subscriptions().list().iterator().next().subscriptionId();
Azure azure = authenticated.withSubscription(subscriptionId);
```

## Advanced Authentication

If you want to take full control for Azure client, you could build your own http pipeline for authentication.

```java
Azure azure = Azure.authenticate(httpPipeline, profile).withDefaultSubscription();
```

If you want to configure part of http pipeline instead of building new one, you may set via `Azure::configure`.

```java
Azure azure = Azure.configure()
    .withLogLevel(HttpLogDetailLevel.BASIC)
    .withPolicy(customPolicy)
    .withRetryPolicy(customRetryPolicy)
    .withHttpClient(httpClient)
    .authenticate(credential, profile)
    .withDefaultSubscription();
```


