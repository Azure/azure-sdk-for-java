# Authentication in Azure Management Libraries for Java

**If you are looking for general documentation on how to use the management libraries, please [visit here](https://aka.ms/azsdk/java/mgmt)**

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
  * [Authenticating with custom HttpPipeline](#authenticating-with-custom-httppipeline)

## Prerequisites

* An [Azure tenant](https://learn.microsoft.com/azure/active-directory/develop/quickstart-create-new-tenant) for Graph RBAC.
* An [Azure subscription](https://azure.microsoft.com/free/) for resource management.
* A Microsoft Entra service principal. You can create a service principal via [Azure Portal](https://learn.microsoft.com/azure/active-directory/develop/howto-create-service-principal-portal), [Azure CLI](https://learn.microsoft.com/cli/azure/azure-cli-sp-tutorial-1) or [Azure Powershell](https://learn.microsoft.com/azure/active-directory/develop/howto-authenticate-service-principal-powershell).

## Simple Authentication

If you want to authenticate as simple as possible, you need to prepare `TokenCredential` and `AzureProfile` as below.

### Preparing TokenCredential
  * The `TokenCredential` is an interface in the `azure-core` package for credentials that can provide a token. 
  * Azure Identity offers multiple implementations of the `TokenCredential` class in the `azure-identity` package. To learn more, see [credentials in Azure Identity](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/identity/azure-identity#credentials).

Sample code to create a simple `ManagedIdentityCredential`:

```java readme-sample-buildManagedIdentityCredential
ManagedIdentityCredential managedIdentityCredential = new ManagedIdentityCredentialBuilder()
    // client ID is optional
    .clientId("<YOUR_CLIENT_ID>")
    .build();
```

The value of `AZURE_AUTHORITY_HOST` can be set via [`AzureAuthorityHosts`](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity/src/main/java/com/azure/identity/AzureAuthorityHosts.java) or [`AzureEnvironment::getActiveDirectoryEndpoint`](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-management/src/main/java/com/azure/core/management/AzureEnvironment.java#L192).

### Preparing AzureProfile
  * The `AzureProfile` is a class holding `AzureEnvironment`, `subscriptionId`, `tenantId` to configure the requests sending to wire. 
  * The `subscriptionId` is mandatory for most resource management while the `tenantId` would be required only for Graph RBAC. They can be set via environment variables.
  
|variable name|value
|-|-
|`AZURE_TENANT_ID`|id of the principal's Microsoft Entra tenant
|`AZURE_SUBSCRIPTION_ID`|id of the subscription for the Azure resources

Sample code to create a `AzureProfile`:

```java readme-sample-buildAzureProfile
// AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
AzureProfile profile = new AzureProfile("<YOUR_TENANT_ID>", "<YOUR_SUBSCRIPTION_ID>", AzureEnvironment.AZURE);
```

The sample code assumes global Azure. Please change `AzureEnvironment.AZURE` variable if otherwise.

Sample code for Azure Germany, with `EnvironmentCredential`:

```java readme-sample-buildEnvironmentCredential
AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE_GERMANY);
EnvironmentCredential credential = new EnvironmentCredentialBuilder()
    .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
    .build();
```

### Authenticating with default HttpPipeline

Once the `TokenCredential` and `AzureProfile` are ready, you can move forward with below authenticating code. It helps build http pipeline internally with [default configuration](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/azure-resourcemanager-resources/src/main/java/com/azure/resourcemanager/resources/fluentcore/utils/HttpPipelineProvider.java#L43).

```java readme-sample-authenticateAuth
AzureResourceManager azure = AzureResourceManager.authenticate(credential, profile).withDefaultSubscription();
```

The `Authenticated` class provides access to a subset of Azure APIs that do not require a specific subscription. If the profile does not contain a subscription, you can select a subscription via [`Authenticated::subscriptions`](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/azure-resourcemanager/src/main/java/com/azure/resourcemanager/AzureResourceManager.java#L200). Similarly, you can select a tenant via [`Authenticated::tenants`](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/resourcemanager/azure-resourcemanager/src/main/java/com/azure/resourcemanager/AzureResourceManager.java#L207).

```java readme-sample-authenticateAndListSubs
AzureResourceManager.Authenticated authenticated = AzureResourceManager.authenticate(credential, profile);
String subscriptionId = authenticated.subscriptions().list().iterator().next().subscriptionId();
AzureResourceManager azure = authenticated.withSubscription(subscriptionId);
```

## Advanced Authentication

If you want to take full control of Azure client, you could build your own http pipeline for authentication.

### Preparing HttpPipelinePolicy
  * The `HttpPipelinePolicy` is an interface that process provided request context and invokes the next policy. To learn more, see [policies in Azure Core](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/core/azure-core/src/main/java/com/azure/core/http/policy) and [policies in Azure Management Libraries for Java](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/resourcemanager/azure-resourcemanager-resources/src/main/java/com/azure/resourcemanager/resources/fluentcore/policy).

### Preparing HttpClient
  * The `HttpClient` is a generic interface for sending HTTP requests and getting responses. 
  * [azure-core-http-netty](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/core/azure-core-http-netty) provides a Netty derived HTTP client.
  * [azure-core-http-okhttp](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/core/azure-core-http-okhttp) provides an OkHttp derived HTTP client.

### Authenticating with custom HttpPipeline

Once your custom configurations are ready, you can move forward with `AzureResourceManager::configure`.

```java readme-sample-customPipeline
AzureResourceManager azure = AzureResourceManager.configure()
    .withPolicy(customPolicy)
    .withRetryPolicy(customRetryPolicy)
    .withHttpClient(httpClient)
    .authenticate(credential, profile)
    .withDefaultSubscription();
```
