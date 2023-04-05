# Troubleshoot Azure Container Registry client library issues

This troubleshooting guide contains instructions to diagnose frequently encountered issues while using the Azure Container Registry client library for Java.

## General Troubleshooting

Container registry service methods throw a [HttpResponseException](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core/src/main/java/com/azure/core/exception/HttpResponseException.java) or its subclass on failure.

Here's the example of how to catch it with synchronous client

```java readme-sample-getProperties
DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
ContainerRepository containerRepository = new ContainerRegistryClientBuilder()
    .endpoint(endpoint)
    .credential(credential)
    .buildClient()
    .getRepository(repositoryName);
try {
    containerRepository.getProperties();
} catch (HttpResponseException exception) {
    // Do something with the exception.
}
```

With async clients, you can catch and handle exceptions in the error callbacks:

```java readme-sample-anonymousAsyncClientThrows
anonymousClient.deleteRepository(repositoryName)
    .doOnSuccess(
        ignored -> System.out.println("Unexpected Success: Delete is not allowed on anonymous access!"))
    .doOnError(
        error -> error instanceof ClientAuthenticationException,
        error -> System.out.println("Expected exception: Delete is not allowed on anonymous access"));
```

Azure Container Registry client library log errors by default. The errors and warnings in the logs or traces provide
useful insights into what went wrong and include corrective actions to fix common issues.

Make sure logging or tracing are enabled to monitor the behavior of the application and to troubleshoot issues with Azure Container Registry library. 

### Enable client logging

The Azure client libraries for Java have two logging options:

* A built-in logging framework.
* Support for logging using the [SLF4J](https://www.slf4j.org/) interface.

Refer to the instructions in this reference document on how to [configure logging in Azure SDK for Java](https://docs.microsoft.com/azure/developer/java/sdk/logging-overview).

### Enable HTTP request/response logging

Reviewing the HTTP request sent or response received over the wire to/from the Azure Container Registry service can be useful in
troubleshooting issues. To enable logging the HTTP request and response payload, the `ContainerRegistryClient` can be configured as shown below:

```java readme-sample-enablehttplogging
ContainerRegistryClient registryClient = new ContainerRegistryClientBuilder()
    .endpoint(endpoint)
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

### Enable tracing

You can visualize `ContainerRegistryClient` calls along with underlying HTTP calls by enabling distributed tracing using OpenTelemetry or
ApplicationInsights Java agent. Please refer to [configure tracing in Azure SDK for Java](https://docs.microsoft.com/azure/developer/java/sdk/tracing) for the details.

## Troubleshooting authentication errors

### HTTP 401 Errors

HTTP 401 errors indicates problems authenticating. Check exception message or logs for more information.

#### ARM access token is disabled

You may see error similar to the one below, it indicates an attempt to perform operation that requires authentication without credentials.

```text
AcrErrorsException: Status code 401, "{"errors":[{"code":"UNAUTHORIZED","message":"arm aad token disallowed"}]}"
```

The error indicates that authentication with ARM access token was disabled on accessed Container Registry resource. Check if audience was provided to
Container Registry client builder. When ARM AAD tokens are disabled on the Container Registry resource, audience should not be set.
Refer to [ACR CLI reference](https://learn.microsoft.com/cli/azure/acr/config/authentication-as-arm?view=azure-cli-latest) for information on how to
check and configure authentication with ARM tokens.

#### Anonymous access issues
You may see error similar to the one below, it indicates an attempt to perform operation that requires authentication without credentials.

```text
AcrErrorsException: Status code 401, "{"errors":[{"code":"UNAUTHORIZED","message":"authentication required, visit https://aka.ms/acr/authorization for more information."}]}"
```

Unauthorized access can only be enabled for read (pull) operations such as listing repositories, getting properties or tags.
Refer to [Anonymous pull access](https://docs.microsoft.com/azure/container-registry/anonymous-pull-access) to learn about anonymous access limitation. 

### HTTP 403 Errors

HTTP 403 errors indicate the user is not authorized to perform a specific operation in Azure Container Registry.

#### Insufficient permissions

If you see an error similar to the one below, it means that the provided credentials does not have permissions to access the registry.
```text
AcrErrorsException: Status code 403, "{"errors":[{"code":"DENIED","message":"retrieving permissions failed"}]}"
```

1. Check that the application or user that is making the request has sufficient permissions.
   Check [Troubleshoot registry login](https://docs.microsoft.com/azure/container-registry/container-registry-troubleshoot-login) for possible solutions.
2. If the user or application is granted sufficient privileges to query the workspace, make sure you are
   authenticating as that user/application. If you are authenticating using the
   [DefaultAzureCredential](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity/README.md#authenticating-with-defaultazurecredential)
   then check the logs to verify that the credential used is the one you expected. To enable logging, see [enable
   client logging](#enable-client-logging) section above.

For more help on troubleshooting authentication errors, please see the Azure Identity client library [troubleshooting
guide](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity/TROUBLESHOOTING.md).

#### Network access issues

You may see an error similar to the one below, it indicates that public access to Azure Container registry is disabled or restricted.
Refer to [Troubleshoot network issues with registry](https://docs.microsoft.com/azure/container-registry/container-registry-troubleshoot-access) for more information.
```text
AcrErrorsException: Status code 403, "{"errors":[{"code":"DENIED","message":"client with IP <> is not allowed access. Refer https://aka.ms/acr/firewall to grant access."}]}
```

## Service errors

When working with `ContainerRegistryContentClient` and `ContainerRegistryAsyncContentClient` you may get an `HttpResponseException` exception with
message containing additional information and [Docker error code](https://docs.docker.com/registry/spec/api/#errors-2).

### Getting BLOB_UPLOAD_INVALID

In rare cases, transient error (such as connection reset) can happen during blob upload which may lead to `ResourceNotFoundException` being thrown with message similar to
`{"errors":[{"code":"BLOB_UPLOAD_INVALID","message":"blob upload invalid"}]}` resulting in a failed upload. In this case upload should to be restarted from the beginning.

The following code snippet shows how to access detailed error information:   
```java com.azure.containers.containerregistry.uploadBlobErrorHandling
BinaryData configContent = BinaryData.fromObject(Collections.singletonMap("hello", "world"));

try {
    UploadRegistryBlobResult uploadResult = contentClient.uploadBlob(configContent);
    System.out.printf("Uploaded blob: digest - '%s', size - %s\n", uploadResult.getDigest(),
        uploadResult.getSizeInBytes());
} catch (HttpResponseException ex) {
    if (ex.getValue() instanceof ResponseError) {
        ResponseError error = (ResponseError) ex.getValue();
        System.out.printf("Upload failed: code '%s'\n", error.getCode());
        if ("BLOB_UPLOAD_INVALID".equals(error.getCode())) {
            System.out.println("Transient upload issue, starting upload over");
            // retry upload
        }
    }
}
```

## Dependency Conflicts

If you see `NoSuchMethodError` or `NoClassDefFoundError` during your application runtime, this is due to a
dependency version conflict. Please take a look at [troubleshooting dependency version conflicts](https://docs.microsoft.com/azure/developer/java/sdk/troubleshooting-dependency-version-conflict) for more information on
why this happens and [ways to mitigate this issue](https://docs.microsoft.com/azure/developer/java/sdk/troubleshooting-dependency-version-conflict#mitigate-version-mismatch-issues).
