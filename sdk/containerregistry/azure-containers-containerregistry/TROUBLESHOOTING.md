# Troubleshoot Azure Container Registry client library issues

This troubleshooting guide contains instructions to diagnose frequently encountered issues while using the Azure Container Registry client library for Java.

## General Troubleshooting

To troubleshoot issues with Azure Container Registry library, it is important to first enable logging to monitor the
behavior of the application. The errors and warnings in the logs or traces generally provide
useful insights into what went wrong and sometimes include corrective actions to fix issues.

### Enable client logging

The Azure client libraries for Java have two logging options:

* A built-in logging framework.
* Support for logging using the [SLF4J](https://www.slf4j.org/) interface.

Refer to the instructions in this reference document on how to [configure logging in Azure SDK for Java](https://docs.microsoft.com/azure/developer/java/sdk/logging-overview).

### Enable HTTP request/response logging

Reviewing the HTTP request sent or response received over the wire to/from the Azure Container Registry service can be useful in
troubleshooting issues. To enable logging the HTTP request and response payload, the `ContainerRegistryClient` can be configured as shown below:

```java readme-sample-enablehttplogging
ContainerRegistryClient client = new ContainerRegistryClientBuilder()
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

#### Anonymous access issues
You may see error similar to the one below, it indicates an attempt to perform operation that requires authentication without credentials.

```text
AcrErrorsException: Status code 401, "{"errors":[{"code":"UNAUTHORIZED","message":"authentication required, visit https://aka.ms/acr/authorization for more information."}]}"
```

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
 
## Dependency Conflicts

If you see `NoSuchMethodError` or `NoClassDefFoundError` during your application runtime, this is due to a
dependency version conflict. Please take a look at [troubleshooting dependency version conflicts](https://docs.microsoft.com/azure/developer/java/sdk/troubleshooting-dependency-version-conflict) for more information on
why this happens and [ways to mitigate this issue](https://docs.microsoft.com/azure/developer/java/sdk/troubleshooting-dependency-version-conflict#mitigate-version-mismatch-issues).
