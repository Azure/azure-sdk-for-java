# Troubleshooting Azure Monitor Query client library issues

This troubleshooting guide contains instructions to diagnose frequently encountered issues while using the Azure
Monitor Query client library for Java.

## Table of contents

* [General Troubleshooting](#general-troubleshooting)
    * [Enable client logging](#enable-client-logging)
    * [Enable HTTP request/response logging](#enable-http-requestresponse-logging)
    * [Troubleshooting authentication issues with logs and metrics query requests](#authentication-errors)
    * [Troubleshooting NoSuchMethodError or NoClassDefFoundError](#dependency-conflicts)
* [Troubleshooting Logs Query](#troubleshooting-logs-query)
    * [Troubleshooting authorization errors](#troubleshooting-authorization-errors-for-logs-query)
    * [Troubleshooting invalid Kusto query](#troubleshooting-invalid-kusto-query)
    * [Troubleshooting empty log query results](#troubleshooting-empty-log-query-results)
    * [Troubleshooting client timeouts when executing logs query request](#troubleshooting-client-timeouts-when-executing-logs-query-request)
    * [Troubleshooting server timeouts when executing logs query request](#troubleshooting-server-timeouts-when-executing-logs-query-request)
    * [Troubleshooting server timeouts on OkHTTP client](#troubleshooting-server-timeouts-on-okhttp-client)
    * [Troubleshooting partially successful logs query requests](#troubleshooting-partially-successful-logs-query-requests)
* [Troubleshooting Metrics Query](#troubleshooting-metrics-query)
    * [Troubleshooting authorization errors](#troubleshooting-authorization-errors-for-metrics-query)
    * [Troubleshooting unsupported granularity for metrics query](#troubleshooting-unsupported-granularity-for-metrics-query)

## General Troubleshooting

### Enable client logging

To troubleshoot issues with Azure Monitor query library, it is important to first enable logging to monitor the
behavior of the application. The errors and warnings in the logs generally provide
useful insights into what went wrong and sometimes include corrective actions to fix issues.
The Azure client libraries for Java have two logging options:

* A built-in logging framework.
* Support for logging using the [SLF4J](https://www.slf4j.org/) interface.

Refer to the instructions in this reference document on how to [configure logging in Azure SDK for Java](https://docs.microsoft.com/azure/developer/java/sdk/logging-overview).

### Enable HTTP request/response logging

Reviewing the HTTP request sent or response received over the wire to/from the Azure Monitor service can be useful in
troubleshooting issues. To enable logging the HTTP request and response payload, the LogsQueryClient and the
MetricsQueryClient can be configured as shown below:

```java readme-sample-enablehttplogging
LogsQueryClient logsQueryClient = new LogsQueryClientBuilder()
        .credential(credential)
        .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
        .buildClient();
// or
MetricsQueryClient metricsQueryClient = new MetricsQueryClientBuilder()
        .credential(credential)
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

Azure Monitor Query supports Azure Active Directory authentication. Both LogsQueryClientBuilder and
MetricsQueryClientBuilder have methods to set the `credential`. To provide a valid credential, you can use
`azure-identity` dependency. For more details on getting started, refer to
the [README](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/monitor/azure-monitor-query#create-the-client)
of Azure Monitor Query library. You can also refer to
the [Azure Identity documentation](https://docs.microsoft.com/azure/developer/java/sdk/identity)
for more details on the various types of credential supported in `azure-identity`.

### Dependency Conflicts

If you see `NoSuchMethodError` or `NoClassDefFoundError` during your application runtime, this is due to a 
dependency version conflict. Please take a look at [troubleshooting dependency version conflicts](https://docs.microsoft.com/azure/developer/java/sdk/troubleshooting-dependency-version-conflict) for more information on 
why this happens and [ways to mitigate this issue](https://docs.microsoft.com/azure/developer/java/sdk/troubleshooting-dependency-version-conflict#mitigate-version-mismatch-issues).

## Troubleshooting Logs Query

### Troubleshooting authorization errors for logs query

If you get an HTTP error with status code 403 (Forbidden), it means that the provided credentials does not have
sufficient permissions to query the workspace.
```text
com.azure.core.exception.HttpResponseException: Status code 403, "{"error":{"message":"The provided credentials have insufficient access to perform the requested operation","code":"InsufficientAccessError","correlationId":""}}"
	at com.azure.monitor.query/com.azure.monitor.query.LogsQueryAsyncClient.lambda$queryWorkspaceWithResponse$7(LogsQueryAsyncClient.java:346)
```

1. Check that the application or user that is making the request has sufficient permissions:
    * You can refer to this document to [manage access to workspaces](https://docs.microsoft.com/azure/azure-monitor/logs/manage-access#manage-access-using-workspace-permissions)
    * If you're querying for logs of a specific resource, then ensure the service principal is assigned the "Log Analytics Reader" role on the resource.
2. If the user or application is granted sufficient privileges to query the workspace, make sure you are
   authenticating as that user/application. If you are authenticating using the
   [DefaultAzureCredential](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity/README.md#authenticating-with-defaultazurecredential)
   then check the logs to verify that the credential used is the one you expected. To enable logging, see [enable
   client logging](#enable-client-logging) section above.

For more help on troubleshooting authentication errors, please see the Azure Identity client library [troubleshooting 
guide](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity/TROUBLESHOOTING.md).

### Troubleshooting invalid Kusto query

If you get an HTTP error with status code 400 (Bad Request), you may have an error in your Kusto query and you'll
see an error message similar to the one below.

```text
com.azure.core.exception.HttpResponseException: Status code 400, "{"error":{"message":"The request had some invalid properties","code":"BadArgumentError","correlationId":"ff3e2a7e-e95c-4437-82cf-9b15761d0850","innererror":{"code":"SyntaxError","message":"A recognition error occurred in the query.","innererror":{"code":"SYN0002","message":"Query could not be parsed at 'joi' on line [2,244]","line":2,"pos":244,"token":"joi"}}}}"
	at com.azure.monitor.query/com.azure.monitor.query.LogsQueryAsyncClient.lambda$queryWorkspaceWithResponse$7(LogsQueryAsyncClient.java:346)
```

The error message may include the line number and position where the Kusto query has an error (line 2, position 244
in the above example). You may also refer to the [Kusto Query Language](https://docs.microsoft.com/azure/data-explorer/kusto/query) reference docs to learn more about querying logs using KQL.

### Troubleshooting empty log query results

If your Kusto query returns empty no logs, please validate the following:

- You have the right workspace ID
- You are setting the correct time interval for the query. Try expanding the time interval for your query to see if that
  returns any results.
- If your Kusto query also has a time interval, the query is evaluated for the intersection of the time interval in the
  query string and the time interval set in the `QueryTimeInterval` param provided the query API. The intersection of
  these time intervals may not have any logs. To avoid any confusion, it's recommended to remove any time interval in
  the Kusto query string and use `QueryTimeInterval` explicitly.

### Troubleshooting client timeouts when executing logs query request

Some Kusto queries can run for a long time on the server depending on the complexity of the query and the number of
results that the query has to fetch. This can lead to the client timing out before the server has had chance to respond.
To increase the client side timeout, you can configure the HTTP client to have an extended timeout by doing the
following.

```java readme-sample-responsetimeout
LogsQueryClient client = new LogsQueryClientBuilder()
        .credential(credential)
        .clientOptions(new HttpClientOptions().setResponseTimeout(Duration.ofSeconds(120)))
        .buildClient();
```

The above code will create a LogsQueryClient with a Netty HTTP client that waits for a response for up to 120 seconds.
The default is 60 seconds.

### Troubleshooting server timeouts when executing logs query request

Similar to the above section, complex Kusto queries can take a long time to complete and such queries are aborted by the
service if they run for more than 3 minutes. For such scenarios, the query APIs on `LogsQueryClient`, provide options to
configure the timeout on the server. The server timeout can be extended up to 10 minutes.

The following code shows a sample on how to set the server timeout to 10 minutes. Note that by setting this server
timeout, the Azure Monitor Query library will automatically also extend the client timeout to wait for 10 minutes for
the server to respond. You don't need to configure your HTTP client to extend the response timeout as shown in the
previous section.

```java readme-sample-servertimeout
LogsQueryClient client = new LogsQueryClientBuilder()
        .credential(credential)
        .buildClient();

client.queryWorkspaceWithResponse("{workspaceId}", "{kusto-query-string}", QueryTimeInterval.LAST_DAY,
        new LogsQueryOptions().setServerTimeout(Duration.ofMinutes(10)), Context.NONE);
```

### Troubleshooting server timeouts on OkHTTP client

Due to the limitations in OkHTTP client, extending the timeout of a specific logs query request is not supported. So, to
workaround this, the client has to be configured with longer timeout value at the time of building the client as shown
below. The downside to doing this is that every request from this client will have this extended client-side timeout.

```java readme-sample-okhttpresponsetimeout
LogsQueryClient client = new LogsQueryClientBuilder()
        .credential(credential)
        .clientOptions(new HttpClientOptions().setResponseTimeout(Duration.ofSeconds(120)))
        .buildClient();
```

### Troubleshooting partially successful logs query requests

By default, if the execution of a Kusto query resulted in a partially successful response, the Azure Monitor Query
client library will throw an exception to indicate to the user that the query was not fully successful. To turn this
behavior off and consume the partially successful response, you can set the `allowPartialErrors` property to `true`
in `LogsQueryOptions` as shown below:

```java readme-sample-allowpartialerrors
client.queryWorkspaceWithResponse("{workspaceId}", "{kusto-query-string}", QueryTimeInterval.LAST_DAY,
        new LogsQueryOptions().setAllowPartialErrors(true), Context.NONE);
```

## Troubleshooting Metrics Query

### Troubleshooting authorization errors for metrics query

If you get an HTTP error with status code 403 (Forbidden), it means that the provided credentials does not have
sufficient permissions to query the workspace.
```text
com.azure.core.exception.HttpResponseException: Status code 403, "{"error":{"code":"AuthorizationFailed","message":"The client '71d56230-5920-4856-8f33-c030b269d870' with object id '71d56230-5920-4856-8f33-c030b269d870' does not have authorization to perform action 'microsoft.insights/metrics/read' over scope '/subscriptions/faa080af-c1d8-40ad-9cce-e1a450ca5b57/resourceGroups/srnagar-azuresdkgroup/providers/Microsoft.CognitiveServices/accounts/srnagara-textanalytics/providers/microsoft.insights' or the scope is invalid. If access was recently granted, please refresh your credentials."}}"
	at com.azure.monitor.query/com.azure.monitor.query.MetricsQueryAsyncClient.lambda$queryResourceWithResponse$4(MetricsQueryAsyncClient.java:227)
```

1. Check that the application or user that is making the request has sufficient permissions:
    * You can refer to this document to [manage access to workspaces](https://docs.microsoft.com/azure/azure-monitor/logs/manage-access#manage-access-using-workspace-permissions)
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

	at com.azure.monitor.query@1.0.0-beta.5/com.azure.monitor.query.MetricsQueryAsyncClient.lambda$queryResourceWithResponse$4(MetricsQueryAsyncClient.java:205)
```

As documented in the error message, the supported granularity for metrics queries are 1 minute, 5 minutes, 15 minutes,
30 minutes, 1 hour, 6 hours, 12 hours and 1 day.
