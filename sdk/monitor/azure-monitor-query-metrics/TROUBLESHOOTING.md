# Troubleshooting Azure Monitor Metrics Query client library issues

This troubleshooting guide contains instructions to diagnose frequently encountered issues while using the Azure
Monitor Query client library for Java.

## Table of contents

* [General Troubleshooting](#general-troubleshooting)
    * [Enable client logging](#enable-client-logging)
    * [Enable HTTP request/response logging](#enable-http-requestresponse-logging)
    * [Troubleshooting authentication issues with and metrics query requests](#authentication-errors)
    * [Troubleshooting NoSuchMethodError or NoClassDefFoundError](#dependency-conflicts)
* [Troubleshooting Metrics Query](#troubleshooting-metrics-query)
    * [Troubleshooting authorization errors](#troubleshooting-authorization-errors-for-metrics-query)
    * [Troubleshooting unsupported granularity for metrics query](#troubleshooting-unsupported-granularity-for-metrics-query)

## General Troubleshooting

### Enable client logging

To troubleshoot issues with Azure Monitor Query Metrics library, it is important to first enable logging to monitor the
behavior of the application. The errors and warnings in the logs generally provide
useful insights into what went wrong and sometimes include corrective actions to fix issues.
The Azure client libraries for Java have two logging options:

* A built-in logging framework.
* Support for logging using the [SLF4J](https://www.slf4j.org/) interface.

Refer to the instructions in this reference document on how to [configure logging in Azure SDK for Java](https://docs.microsoft.com/azure/developer/java/sdk/logging-overview).

### Enable HTTP request/response logging

Reviewing the HTTP request sent or response received over the wire to/from the Azure Monitor service can be useful in
troubleshooting issues. To enable logging the HTTP request and response payload, the
MetricsClient can be configured as shown below:

```java readme-sample-enablehttplogging
// Enable HTTP logging for troubleshooting
MetricsClient metricsClient = new MetricsClientBuilder()
    .credential(new DefaultAzureCredentialBuilder().build())
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

### Authentication errors

Azure Monitor Query Metrics supports Azure Active Directory authentication. MetricsClientBuilder has a method to set the `credential`. To provide a valid credential, you can use
`azure-identity` dependency. For more details on getting started, refer to
the [README](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/monitor/azure-monitor-query-metrics#Synchronous-clients)
of Azure Monitor Query Metrics library. You can also refer to
the [Azure Identity documentation](https://docs.microsoft.com/azure/developer/java/sdk/identity)
for more details on the various types of credential supported in `azure-identity`.

### Dependency Conflicts

If you see `NoSuchMethodError` or `NoClassDefFoundError` during your application runtime, this is due to a
dependency version conflict. Please take a look at [troubleshooting dependency version conflicts](https://docs.microsoft.com/azure/developer/java/sdk/troubleshooting-dependency-version-conflict) for more information on
why this happens and [ways to mitigate this issue](https://docs.microsoft.com/azure/developer/java/sdk/troubleshooting-dependency-version-conflict#mitigate-version-mismatch-issues).

## Troubleshooting Metrics Query

### Troubleshooting authorization errors for metrics query

If you get an HTTP error with status code 403 (Forbidden), it means that the provided credentials does not have
sufficient permissions to query the workspace.
```text
com.azure.core.exception.HttpResponseException: Status code 403, "{"error":{"code":"AuthorizationFailed","message":"The client {client-id} with object id {object-id} does not have authorization to perform action 'microsoft.insights/metrics/read' over scope '/subscriptions/{subscription-id}/resourceGroups/{resource-group}/providers/Microsoft.CognitiveServices/accounts/{account-name}/providers/microsoft.insights' or the scope is invalid. If access was recently granted, please refresh your credentials."}}"
	at com.azure.monitor.query.metrics/com.azure.monitor.query.metrics.MetricsAsyncClient.lambda$queryResourceWithResponse$4(MetricsAsyncClient.java:227)
```

1. Check that the application or user that is making the request has sufficient permissions:
    * You can refer to this document to [manage access to workspaces](https://learn.microsoft.com/azure/azure-monitor/metrics/azure-monitor-workspace-manage).
2. If the user or application is granted sufficient privileges to query the workspace, make sure you are
   authenticating as that user/application. If you are authenticating using the
   [DefaultAzureCredential](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity/README.md#authenticating-with-defaultazurecredential)
   then check the logs to verify that the credential used is the one you expected. To enable logging, see [enable
   client logging](#enable-client-logging) section above.

For more help on troubleshooting authentication errors, please see the Azure Identity client library [troubleshooting
guide](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity/TROUBLESHOOTING.md).

### Troubleshooting unsupported granularity for metrics query

If you notice the following exception, this is due to an invalid time granularity in the metrics query request. Your
query might look something like the following where `MetricsQueryOptions().setGranularity()` is set to an unsupported
duration.

```text
com.azure.core.exception.HttpResponseException: Status code 400, "{"code":"BadRequest","message":"Invalid time grain duration: PT10M, supported ones are: 00:01:00,00:05:00,00:15:00,00:30:00,01:00:00,06:00:00,12:00:00,1.00:00:00"}"

	at com.azure.monitor.query@1.0.0-beta.5/com.azure.monitor.query.MetricsAsyncClient.lambda$queryResourceWithResponse$4(MetricsAsyncClient.java:205)
```

As documented in the error message, the supported granularity for metrics queries are 1 minute, 5 minutes, 15 minutes,
30 minutes, 1 hour, 6 hours, 12 hours and 1 day.
