# Troubleshoot Azure Form Recognizer client library issues

This troubleshooting guide contains instructions to diagnose frequently encountered issues while using the Azure Form Recognizer client library for Java.

## Table of Contents
* [Troubleshooting Errors & Exceptions](#troubleshooting-errors--exceptions)
    * [Handling HttpResponseException](#handling-httpresponseexception)
    * [Build model error](#build-model-error)
       * [Invalid training dataset](#invalid-training-data-set)
       * [Invalid SAS URL](#invalid-sas-url)
    * [Generic Error](#generic-error)
* [Unexpected time to build a custom model](#unexpected-time-to-build-a-custom-model)
* [Enable HTTP request/response logging](#enable-http-requestresponse-logging)

## Troubleshooting Errors & Exceptions
### Handling HttpResponseException
Form Recognizer service methods throw a [HttpResponseException](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core/src/main/java/com/azure/core/exception/HttpResponseException.java) or its subclass on failure.
The HttpResponseException thrown by the Azure Form Recognizer client library includes detailed response error object
that provides specific useful insights into what went wrong and includes corrective actions to fix common issues.
This error information can be found inside the message property of the HttpResponseException object.

Here's the example of how to catch it with synchronous client

```java readme-sample-handlingException
try {
    documentAnalysisClient.beginAnalyzeDocumentFromUrl("prebuilt-receipt", "invalidSourceUrl");
} catch (HttpResponseException e) {
    System.out.println(e.getMessage());
    // Do something with the exception
}
```

With async clients, you can catch and handle exceptions in the error callbacks:

```java readme-sample-async-handlingException
administrationAsyncClient.deleteModel("{modelId}")
    .doOnSuccess(
        ignored -> System.out.println("Success!"))
    .doOnError(
        error -> error instanceof ResourceNotFoundException,
        error -> System.out.println("Exception: Delete could not be performed."));
```

### Build model error
You may have seen a Build model error, mostly when you are trying to build your custom model.
It looks like this:

```text
com.azure.core.exception.HttpResponseException: Invalid model created with model Id [modelId], errorCode: [2012], message: <specific-reason>"
```

The most common scenarios when this might occur is, if you are building the model with an 
[Invalid data set](#invalid-training-data-set) or an [Invalid SAS Url](#invalid-sas-url).

#### Invalid training data set
This error indicates that the provided data set does not match the training data requirements.
Learn more about building a training data set, [here](https://aka.ms/customModelV3)
The error looks like this:

```json
{
"code": "InvalidRequest",
"message": "Invalid request.",
"innererror": {
  "code": "ModelBuildError",
  "message": "Could not build the model: OCR file 'Form_1.jpg.ocr.json' has an invalid schema."
  }
}
```

The exception looks like this:
```text
com.azure.core.exception.HttpResponseException: Invalid request, errorCode: [ModelBuildError], message: Could not build the model: OCR file 'Form_1.jpg.ocr.json' has an invalid schema"
```

#### Invalid SAS URL
This error suggests you to specifically enable listing permissions on the blob storage SAS URL for the Form Recognizer service to access the training dataset resource.

You may have seen this error like:
```
com.azure.core.exception.HttpResponseException: Invalid model created with model Id [modelId], errorCode: [2012], message: Unable to list blobs on the Azure blob storage account.
```

### Generic Error
A "Generic Error" in the SDK's is most often caused by heavy load on the service and throttling of the service and retrying after backoff time 
should help mitigate this.
The error looks like:

```
com.azure.core.exception.HttpResponseException: Status code 200, Invalid model created with model Id [modelId], errorCode: [3014], message: Generic error during training."
```

### Unexpected time to build a custom model
It is common to notice a longer time than what is expected to build a custom model when using `DocumentBuildMode: "neural"`
with `DocumentModelAdministrationClient#beginBuildDocumentModel()`.

For simpler use-cases, you can use [Custom Template models](https://aka.ms/custom-template-models) which are easy-to-train models and take lesser time to build over the 
[Custom Neural models](https://aka.ms/custom-neural-models) that are deep learned models.

### Enable HTTP request/response logging

Reviewing the HTTP request sent or response received over the wire to/from the Azure Form Recognizer service can be useful in
troubleshooting issues. To enable logging the HTTP request and response payload, the `DocumentAnalysisClient` can be configured as shown below:

```java readme-sample-enablehttplogging
DocumentAnalysisClient client = new DocumentAnalysisClientBuilder()
    .endpoint("{endpoint}")
    .credential(defaultCredential)
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
