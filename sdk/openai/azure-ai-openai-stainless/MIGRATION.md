# Migration Guide from Azure OpenAI Java SDK to OpenAI Java SDK

## Overview

The [Azure OpenAI Java SDK's latest release][latest_aoai_sdk_release] is the last in the current shape of the SDK. Currently, we recommend users to make use of the [OpenAI Java SDK][openai_java], which can be used to access Azure OpenAI services too. We are currently working on providing classes and types to more easily interact with the service. On the meantime please use this migration guide to access the latest features of OpenAI.

## Client Instantiation & Authentication

### Project setup

Replace the import to `azure-ai-openai` with any of the [dependency import](https://github.com/openai/openai-java?tab=readme-ov-file#installation) that suits your setup.

### Authentication

#### OpenAI

For authenticating a client for OpenAI usage:

```java
OpenAIClient client = OpenAIOkHttpClient.builder()
    .credential(BearerTokenCredential.create(System.getenv("OPENAI_KEY")))
    .build();
```

#### Azure OpenAI Entra ID

Make sure you have correctly included in your project [Azure Identity][azure_identity] and your [Azure OpenAI][azure_openai_access] endpoint, then:

```java
OpenAIClient client = OpenAIOkHttpClient.builder()
    .baseUrl(System.getenv("AZURE_OPENAI_ENDPOINT"))
    // Set the Azure Entra ID
    .credential(BearerTokenCredential.create(AuthenticationUtil.getBearerTokenSupplier(
        new DefaultAzureCredentialBuilder().build(), "https://cognitiveservices.azure.com/.default")))
    .build();
```

#### Azure key authentication

For this mode of authentication you will need your service `key` and `endpoint`. Check your access to your [Azure OpenAI resource][azure_openai_access]:

```java
OpenAIClient client = OpenAIOkHttpClient.builder()
    .baseUrl(System.getenv("AZURE_OPENAI_ENDPOINT"))
    .credential(AzureApiKeyCredential.create(System.getenv("AZURE_OPENAI_KEY")))
    .build();
```

### Sync & Async client

In order to use the To use the sync client:

```java
OpenAIClient client = OpenAIOkHttpClient.builder()
    .baseUrl(System.getenv("AZURE_OPENAI_ENDPOINT"))
    .credential(AzureApiKeyCredential.create(System.getenv("AZURE_OPENAI_KEY")))
    .build();
```

And for aync:

```java
OpenAIClientAsync client = OpenAIOkHttpClientAsync.builder()
    .baseUrl(System.getenv("AZURE_OPENAI_ENDPOINT"))
    .credential(AzureApiKeyCredential.create(System.getenv("AZURE_OPENAI_KEY")))
    .build();
```

One notable difference betwee the async Azure OpenAI Java SDK and the OpenAI SDK, is that the former uses [reactor](https://projectreactor.io/) and the latter [CompletableFutures](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/CompletableFuture.html).



<!-- LINKS -->
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity
[azure_openai_access]: https://learn.microsoft.com/azure/cognitive-services/openai/overview#how-do-i-get-access-to-azure-openai
[openai_java]: https://github.com/openai/openai-java
[latest_aoai_sdk_release]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/CHANGELOG.md#100-beta16-2025-03-26
