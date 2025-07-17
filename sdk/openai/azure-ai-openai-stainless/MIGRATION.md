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

### Azure OpenAI Service version pinning

To supply the version of the service you want to use of the Azure OpenAI service, do the following:

```java
import com.openai.azure.AzureOpenAIServiceVersion;
import com.openai.client.okhttp.OpenAIOkHttpClient;

OpenAIOkHttpClient.Builder clientBuilder = new OpenAIOkHttpClient.Builder()
    .azureServiceVersion(AzureOpenAIServiceVersion.latestStableVersion())
    // continue your client setup normally from this point onwards
```

> [!NOTE] If the `AzureOpenAIServiceVersion` is omitted in the `ClientBuilder` setup, the latest review version in the [OpenAI Java SDK] will be used by default. 

### Sync & Async client

In order to use the To use the sync client:

```java
OpenAIClient client = OpenAIOkHttpClient.builder()
    .baseUrl(System.getenv("AZURE_OPENAI_ENDPOINT"))
    .credential(AzureApiKeyCredential.create(System.getenv("AZURE_OPENAI_KEY")))
    .build();
```

And for async:

```java
OpenAIClientAsync client = OpenAIOkHttpClientAsync.builder()
    .baseUrl(System.getenv("AZURE_OPENAI_ENDPOINT"))
    .credential(AzureApiKeyCredential.create(System.getenv("AZURE_OPENAI_KEY")))
    .build();
```

One notable difference between the async Azure OpenAI Java SDK and the OpenAI SDK, is that the former uses [reactor](https://projectreactor.io/) and the latter [CompletableFutures](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/CompletableFuture.html).

## Using the SDK

While using the [OpenAI Java SDK][openai_java], all the officially supported fields for the [OpenAI service][openai_openapi] should be supported. So for the latest features, please visit their documentation as it becomes available.

For fields that are "Azure-specific", we will provide guidelines on how to work with available types in the interim. We are planning on having better typed support in the near future.

Every [OpenAI Java SDK][openai_java] type has `Map<String, JsonValue>` named `additionalProperties` in which all fields that are not exclusively [OpenAI service related][openai_openapi] are populated. All the fields that are Azure OpenAI specific (i.e. content filter, Azure AI Search, etc.) will be populated in the aforementioned `Map<String, JsonValue>`. Next, you will find what we believe to be the more common scenarios, and how to provide/access these Azure OpenAI specific fields, while working with the [OpenAI Java SDK][openai_java].

> [!TIP]
> We strongly recommend keeping an eye on our `samples` and `test` folders. 
> We use those tests to regularly validate the Azure OpenAI service.

### OYD: Chat Completions

To supply your Azure AI Search fields, you should setup your request as follows:

```java
OpenAIClient client = createClient(apiType, apiVersion);
    ChatCompletionCreateParams params = createParamsBuilder(testModel)
        .messages(asList(createSystemMessageParam(),
            createUserMessageParam("<your_prompt>")))
        .additionalBodyProperties(createExtraBodyForByod())
        .build();
    ChatCompletion completion = client.chat().completions().create(params);
    
// Then the definition for the method where the Azure fields are added:
Map<String, JsonValue> createExtraBodyForByod() {
    Map<String, JsonValue> authentication = new HashMap<>();
    authentication.put("type", JsonValue.from("api_key"));
    authentication.put("key", JsonValue.from(System.getenv("AZURE_SEARCH_API_KEY")));

    Map<String, JsonValue> parameters = new HashMap<>();
    parameters.put("endpoint", JsonValue.from(System.getenv("AZURE_SEARCH_ENDPOINT")));
    parameters.put("index_name", JsonValue.from(System.getenv("AZURE_OPENAI_SEARCH_INDEX")));
    parameters.put("authentication", JsonValue.from(authentication));
    parameters.put("fields_mapping", JsonValue.from(Collections.singletonMap("title_field", "title")));
    parameters.put("query_type", JsonValue.from("simple"));

    Map<String, JsonValue> dataSource = new HashMap<>();
    dataSource.put("type", JsonValue.from("azure_search"));
    dataSource.put("parameters", JsonValue.from(parameters));

    Map<String, JsonValue> extraBody = new HashMap<>();
    extraBody.put("data_sources", JsonValue.from(asList(dataSource)));
    return extraBody;
}
```

In order to access the returned Azure fields you can do the following:

```java 
Map<String, JsonValue> additionalProperties = choice.message()._additionalProperties();

assertTrue(additionalProperties.containsKey("end_turn"));

assertTrue(additionalProperties.containsKey("context"));
Map<String, JsonValue> context = (Map<String, JsonValue>) additionalProperties.get("context").asObject().get();
assertNotNull(context);
assertTrue(context.containsKey("intent"));
assertFalse(CoreUtils.isNullOrEmpty((String) context.get("intent").asString().get()));
assertTrue(context.containsKey("citations"));
assertTrue(((List<JsonValue>) context.get("citations").asArray().get()).size() > 0);
```

For more graceful handling, we suggest using `computeIfPresent` or `getOrDefault`.

### Streaming Chat Completions

For streaming with a sync client you can now do the following:

```java
OpenAIClient client = createClient(apiType, apiVersion);

StringBuilder responseBuilder = new StringBuilder();
try (StreamResponse<ChatCompletionChunk> streamChunks = client.chat().completions().createStreaming(
        createChatCompletionParams(testModel, "Tell a story about a cat and a dog who are best friends. "
                + "It should be at least 100 words long, but at most 120. Be strict about these limits."))) {
    streamChunks.stream()
        .map(it -> (!it.choices().isEmpty()) ? it.choices().get(0).delta().content().orElse("") : "")
        .forEach(System.out::print);
}
```

Of in an async scenario:

```java
OpenAIClientAsync client = //...
        
client.chat()
    .completions()
    .createStreaming(createChatCompletionParams(testModel,
        "Tell a story about a cat and a dog who are best friends. "
            + "It should be at least 100 words long, but at most 120. Be strict about these limits."))
    .subscribe(it -> {
        String delta = !it.choices().isEmpty() ? it.choices().get(0).delta().content().orElse("") : "";
        System.out.print(delta);
    });
```

### Responses

To use `Responses` with the [OpenAI Java SDK][openai_java], you can do the following:

```java
OpenAIClient client = // ...
client.responses().create(...)
```

### Assistants

`Assistants` is still a beta feature. If you want to use it with the [OpenAI Java SDK][openai_java], you can do the following:

```java
OpenAIClient client = // ...
client.beta().assistants().create(...) 
```


## Issues

For issues with the [OpenAI Java SDK][openai_java], please follow the guidelines in the repository for opening issues. 

<!-- LINKS -->
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity
[azure_openai_access]: https://learn.microsoft.com/azure/cognitive-services/openai/overview#how-do-i-get-access-to-azure-openai
[openai_java]: https://github.com/openai/openai-java
[openai_openapi]: https://github.com/openai/openai-openapi
[latest_aoai_sdk_release]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/CHANGELOG.md#100-beta16-2025-03-26
