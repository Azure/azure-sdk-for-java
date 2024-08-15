# Troubleshooting AI Inference issues

This troubleshooting guide covers failure investigation techniques, common errors for the credential types in the Azure
AI Inference Java client library, and mitigation steps to resolve these errors. The common best practice sample can be found
in [Best Practice Samples][best_practice_samples].

## Table of Contents

* [General Troubleshooting](#general-troubleshooting)
    * [Enable client logging](#enable-client-logging)
    * [Enable HTTP request/response logging](#enable-http-requestresponse-logging)
    * [Troubleshooting Exceptions](#troubleshooting-exceptions)
    * [Troubleshooting NoSuchMethodError or NoClassDefFoundError](#dependency-conflicts)
    * [Network Issues](#network-issues)
* [Get additional help](#get-additional-help)

## General Troubleshooting

### Enable client logging

To troubleshoot issues with Azure Inference library, it is important to first enable logging to monitor the
behavior of the application. The errors and warnings in the logs generally provide useful insights into what went wrong
and sometimes include corrective actions to fix issues. The Azure client libraries for Java have two logging options:

* A built-in logging framework.
* Support for logging using the [SLF4J](https://www.slf4j.org/) interface.

Refer to the instructions in this reference document on how to [configure logging in Azure SDK for Java][logging_overview].

### Enable HTTP request/response logging

Reviewing the HTTP request sent or response received over the wire to/from the Azure Model service can be
useful in troubleshooting issues. To enable logging the HTTP request and response payload, the [ChatCompletionsClient][chat_completions_client]
can be configured as shown below. If there is no SLF4J's `Logger` on the class path, set an environment variable
[AZURE_LOG_LEVEL][azure_log_level] in your machine to enable logging.

```java readme-sample-enablehttplogging
        ChatCompletionsClient chatCompletionsClient = new ChatCompletionsClientBuilder()
            .endpoint("{endpoint}")
            .credential(new AzureKeyCredential("{key}"))
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .buildClient();
// or
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
        ChatCompletionsClient configurationClientAad = new ChatCompletionsClientBuilder()
            .credential(credential)
            .endpoint("{endpoint}")
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .buildClient();
```

Alternatively, you can configure logging HTTP requests and responses for your entire application by setting the
following environment variable. Note that this change will enable logging for every Azure client that supports logging
HTTP request/response.

Environment variable name: `AZURE_HTTP_LOG_DETAIL_LEVEL`

| Value            | Logging level                                                        |
|------------------|----------------------------------------------------------------------|
| none             | HTTP request/response logging is disabled                            |
| basic            | Logs only URLs, HTTP methods, and time to finish the request.        |
| headers          | Logs everything in BASIC, plus all the request and response headers. |
| body             | Logs everything in BASIC, plus all the request and response body.    |
| body_and_headers | Logs everything in HEADERS and BODY.                                 |

**NOTE**: When logging the body of request and response, please ensure that they do not contain confidential
information. When logging headers, the client library has a default set of headers that are considered safe to log
but this set can be updated by updating the log options in the builder as shown below:

```java
clientBuilder.httpLogOptions(new HttpLogOptions().addAllowedHeaderName("safe-to-log-header-name"))
```

### Troubleshooting exceptions
Azure Inference service methods throw a [HttpResponseException][http_response_exception] or its subclass on failure.
The `HttpResponseException` thrown by the chat completions client library includes detailed response error object
that provides specific useful insights into what went wrong and includes corrective actions to fix common issues.
This error information can be found inside the message property of the `HttpResponseException` object.

Here's the example of how to catch it with synchronous client

```java readme-sample-troubleshootingExceptions
List<ChatRequestMessage> chatMessages = new ArrayList<>();
chatMessages.add(new ChatRequestSystemMessage("You are a helpful assistant. You will talk like a pirate."));
chatMessages.add(ChatRequestUserMessage.fromString("Can you help me?"));
chatMessages.add(new ChatRequestAssistantMessage("Of course, me hearty! What can I do for ye?"));
chatMessages.add(ChatRequestUserMessage.fromString("What's the best way to train a parrot?"));

try {
        ChatCompletions chatCompletions = client.complete(new ChatCompletionsOptions(chatMessages));
    } catch (HttpResponseException e) {
        System.out.println(e.getMessage());
        // Do something with the exception
    }
```

With async clients, you can catch and handle exceptions in the error callbacks:

```java readme-sample-troubleshootingExceptions-async
asyncClient.complete(new ChatCompletionsOptions(chatMessages))
        .doOnSuccess(ignored -> System.out.println("Success!"))
        .doOnError(
        error -> error instanceof ResourceNotFoundException,
    error -> System.out.println("Exception: 'getChatCompletions' could not be performed."));
```

### Authentication errors

Azure Inference supports Azure Active Directory authentication. [ChatCompletionsClientBuilder][chat_completions_client_builder]
has method to set the `credential`. To provide a valid credential, you can use `azure-identity` dependency. For more
details on getting started, refer to the [README][how_to_create_chat_completions_client] of Azure Inference library.
You can also refer to the [Azure Identity documentation][identity_doc] for more details on the various types of
credential supported in `azure-identity`.

### Dependency conflicts

If you see `NoSuchMethodError` or `NoClassDefFoundError` during your application runtime, this is due to a
dependency version conflict. Please take a look at [troubleshooting dependency version conflicts][troubleshooting_dependency_conflict]
for more information on why this happens and [ways to mitigate this issue][troubleshooting_mitigate_version_mismatch].

### Network issues

If you have network issues, please take a look at [troubleshooting network issues][troubleshooting_network_issues].

## Get additional help

Additional information on ways to reach out for support can be found in the [SUPPORT.md][support] at the root of the repo.

<!-- Links -->
[azure_log_level]: https://learn.microsoft.com/azure/developer/java/sdk/logging-overview#default-logger-for-temporary-debugging
[best_practice_samples]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/src/samples/README.md
[chat_completions_client]: https://learn.microsoft.com/java/api/overview/azure/ai-openai-readme?view=azure-java-preview
[chat_completions_client_builder]: https://learn.microsoft.com/java/api/overview/azure/ai-openai-readme?view=azure-java-preview#authentication
[how_to_create_chat_completions_client]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/openai/azure-ai-openai/README.md#authentication
[http_response_exception]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core/src/main/java/com/azure/core/exception/HttpResponseException.java
[identity_doc]: https://docs.microsoft.com/azure/developer/java/sdk/identity
[logging_overview]: https://docs.microsoft.com/azure/developer/java/sdk/logging-overview
[support]: https://github.com/Azure/azure-sdk-for-java/blob/main/SUPPORT.md
[troubleshooting_network_issues]: https://learn.microsoft.com/azure/developer/java/sdk/troubleshooting-network
[troubleshooting_dependency_conflict]: https://docs.microsoft.com/azure/developer/java/sdk/troubleshooting-dependency-version-conflict
[troubleshooting_mitigate_version_mismatch]: https://docs.microsoft.com/azure/developer/java/sdk/troubleshooting-dependency-version-conflict#mitigate-version-mismatch-issues
