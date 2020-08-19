# Authentication in Azure Management Libraries for Java

To use the APIs in the Azure Management Libraries for Java, as the first step you need to create an authenticated client. This document is to introduce several possible approaches for authentication.

## Getting Started

* [Prerequisites](#prerequisites)
* [Simple Authentication](#simple-authentication)
  * [Preparing TokenCredential](#preparing-tokencredential)
  * [Preparing AzureProfile](#preparing-azureprofile)
  * [Authenticating with default HttpPipeline](#authenticating-with-default-httppipeline)
* [Advanced Authentication](#advanced-authentication)
  * [Preparing HttpPipelinePolicy](#preparing-httppipelinepolicy)
  * [Preparing HttpClient](#preparing-httpclient)
  * [Preparing HttpPipeline](#preparing-httppipeline)
  * [Authenticating with custom HttpPipeline](#authenticating-with-custom-httppipeline)

## Prerequisites

* An [Azure tenant](https://docs.microsoft.com/en-us/azure/active-directory/develop/quickstart-create-new-tenant) for Graph RBAC.
* An [Azure subscription](https://azure.microsoft.com/en-us/free/) for resource management.
* An Azure Active Directory service principal. You can create a service principal via [Azure Portal](https://docs.microsoft.com/en-us/azure/active-directory/develop/howto-create-service-principal-portal), [Azure CLI](https://docs.microsoft.com/en-us/cli/azure/create-an-azure-service-principal-azure-cli) or [Azure Powershell](https://docs.microsoft.com/en-us/azure/active-directory/develop/howto-authenticate-service-principal-powershell).

## Simple Authentication

If you want to authenticate as simple as possible, you need to prepare `TokenCredential` and `AzureProfile` as below.

### Preparing TokenCredential
  * The `TokenCredential` is an interface in the `azure-core` package for credentials that can provide a token. 
  * Azure Identity offers multiple implementations of the `TokenCredential` class in the `azure-identity` package. To learn more, see [credentials in Azure Identity](https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/identity/azure-identity#credentials).

Sample code to create a simple `ClientSecretCredential`:

```java
ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
    .clientId("<YOUR_CLIENT_ID>")
    .clientSecret("<YOUR_CLIENT_SECRET>")
    .tenantId("<YOUR_TENANT_ID>")
	// authority host is optional
	.authorityHost("<AZURE_AUTHORITY_HOST>")
    .build();
```

The value of `AZURE_AUTHORITY_HOST` can be set via [`AzureAuthorityHosts`](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/identity/azure-identity/src/main/java/com/azure/identity/AzureAuthorityHosts.java) or [`AzureEnvironment::getActiveDirectoryEndpoint`](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/core/azure-core-management/src/main/java/com/azure/core/management/AzureEnvironment.java#L192).

### Preparing AzureProfile
  * The `AzureProfile` is a class holding `AzureEnvironment`, `subscriptionId`, `tenantId` to configure the requests sending to wire. 
  * The `subscriptionId` is mandatory for most resource management while the `tenantId` would be required only for Graph RBAC. They can be set via environment variables.
  
|variable name|value
|-|-
|`AZURE_TENANT_ID`|id of the principal's Azure Active Directory tenant
|`AZURE_SUBSCRIPTION_ID`|id of the subscription for the Azure resources

Sample code to create a `AzureProfile`:

```java
//AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
AzureProfile profile = new AzureProfile("<YOUR_TENANT_ID>", "<YOUR_SUBSCRIPTION_ID>", AzureEnvironment.AZURE);
```

The sample code assumes global Azure. Please change `AzureEnvironment.AZURE` variable if otherwise.

Sample code for Azure Germany, with `EnvironmentCredential`:

```java
AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE_GERMANY);
EnvironmentCredential credential = new EnvironmentCredentialBuilder()
    .authorityHost(profile.environment().getActiveDirectoryEndpoint())
    .build();
```

### Authenticating with default HttpPipeline

Once the `TokenCredential` and `AzureProfile` are ready, you can move forward with below authenticating code. It helps build http pipeline internally with [default configuration](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager-resources/src/main/java/com/azure/resourcemanager/resources/fluentcore/utils/HttpPipelineProvider.java#L43).

```java
Azure azure = Azure.authenticate(credential, profile).withDefaultSubscription();
```

The `Authenticated` class provides access to a subset of Azure APIs that do not require a specific subscription. If the profile does not contain a subscription, you can select a subscription via [`Authenticated::subscriptions`](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager/src/main/java/com/azure/resourcemanager/Azure.java#L200). Similarly, you can select a tenant via [`Authenticated::tenants`](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/resourcemanager/azure-resourcemanager/src/main/java/com/azure/resourcemanager/Azure.java#L207).

```java
Azure.Authenticated authenticated = Azure.authenticate(credential, profile);
String subscriptionId = authenticated.subscriptions().list().iterator().next().subscriptionId();
Azure azure = authenticated.withSubscription(subscriptionId);
```

## Advanced Authentication

If you want to take full control of Azure client, you could build your own http pipeline for authentication.

### Preparing HttpPipelinePolicy
  * The `HttpPipelinePolicy` is an interface that process provided request context and invokes the next policy. To learn more, see [policies in Azure Core](https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/core/azure-core/src/main/java/com/azure/core/http/policy) and [policies in Azure Management Libraries for Java](https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/resourcemanager/azure-resourcemanager-resources/src/main/java/com/azure/resourcemanager/resources/fluentcore/policy).


### Preparing HttpClient
  * The `HttpClient` is a generic interface for sending HTTP requests and getting responses. 
  * [azure-core-http-netty](https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/core/azure-core-http-netty) provides a Netty derived HTTP client.
  * [azure-core-http-okhttp](https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/core/azure-core-http-okhttp) provides an OkHttp derived HTTP client.


### Preparing HttpPipeline
  * The `HttpPipeline` is a class that HTTP requests and responses will flow through. It is a construct that contains a list of `HttpPipelinePolicy` which are applied to a request sequentially to prepare it being sent by an `HttpClient`.

Sample code to create a `HttpPipeline`:

```java
HttpPipeline httpPipeline = new HttpPipelineBuilder()
    .policies(httpPipelinePolicies)
    .httpClient(httpClient)
    .build();
```

### Authenticating with custom HttpPipeline

Once your custom configurations are ready, you can move forward with below authenticating code. It would execute the settings you apply in the custom HttpPipeline.

```java
Azure azure = Azure.authenticate(httpPipeline, profile).withDefaultSubscription();
```

If you want to configure part of http pipeline instead of building new one, you may set via `Azure::configure`.

```java
Azure azure = Azure.configure()
    .withPolicy(customPolicy)
    .withRetryPolicy(customRetryPolicy)
    .withHttpClient(httpClient)
    .authenticate(credential, profile)
    .withDefaultSubscription();
```
