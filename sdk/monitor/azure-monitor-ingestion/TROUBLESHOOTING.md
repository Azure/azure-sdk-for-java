# Troubleshooting Azure Monitor Ingestion client library issues

This troubleshooting guide contains instructions to diagnose frequently encountered issues while using the Azure
Monitor Ingestion client library for Java.

## Table of contents

* [General Troubleshooting](#general-troubleshooting)
    * [Enable client logging](#enable-client-logging)
    * [Enable HTTP request/response logging](#enable-http-requestresponse-logging)
    * [Troubleshooting authentication issues](#authentication-errors)
    * [Troubleshooting NoSuchMethodError or NoClassDefFoundError](#dependency-conflicts)
* [Troubleshooting Logs Ingestion](#troubleshooting-logs-query)
    * [Troubleshooting authorization errors](#troubleshooting-authorization-errors)
    * [Troubleshooting invalid Kusto query](#troubleshooting-invalid-kusto-query)
    * [Troubleshooting empty log query results](#troubleshooting-empty-log-query-results)
    * [Troubleshooting client timeouts when executing logs query request](#troubleshooting-client-timeouts-when-executing-logs-query-request)
    * [Troubleshooting server timeouts when executing logs query request](#troubleshooting-server-timeouts-when-executing-logs-query-request)
    * [Troubleshooting server timeouts on OkHTTP client](#troubleshooting-server-timeouts-on-okhttp-client)
    * [Troubleshooting partially successful logs query requests](#troubleshooting-partially-successful-logs-query-requests)

## General Troubleshooting

### Enable client logging

To troubleshoot issues with Azure Monitor query library, it is important to first enable logging to monitor the
behavior of the application. The errors and warnings in the logs generally provide
useful insights into what went wrong and sometimes include corrective actions to fix issues.
The Azure client libraries for Java have two logging options:

* A built-in logging framework.
* Support for logging using the [SLF4J](https://www.slf4j.org/) interface.

Refer to the instructions in this reference document on how
to [configure logging in Azure SDK for Java](https://docs.microsoft.com/azure/developer/java/sdk/logging-overview).

### Enable HTTP request/response logging

Reviewing the HTTP request sent or response received over the wire to/from the Azure Monitor service can be useful in
troubleshooting issues. To enable logging the HTTP request and response payload, the LogsQueryClient and the
MetricsQueryClient can be configured as shown below:

```java readme-sample-enablehttplogging
LogsIngestionClient logsIngestionClient = new LogsIngestionClientBuilder()
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

Azure Monitor Ingestion supports Azure Active Directory authentication. The LogsIngestionClientBuilder can be configured
to set the `credential`. To provide a valid credential, you can use
`azure-identity` dependency. For more details on getting started, refer to
the [README](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/monitor/azure-monitor-ingestion#create-the-client)
of Azure Monitor Ingestion library. You can also refer to
the [Azure Identity documentation](https://docs.microsoft.com/azure/developer/java/sdk/identity)
for more details on the various types of credential supported in `azure-identity`.

### Dependency Conflicts

If you see `NoSuchMethodError` or `NoClassDefFoundError` during your application runtime, this is due to a
dependency version conflict. Please take a look
at [troubleshooting dependency version conflicts](https://docs.microsoft.com/azure/developer/java/sdk/troubleshooting-dependency-version-conflict)
for more information on
why this happens
and [ways to mitigate this issue](https://docs.microsoft.com/azure/developer/java/sdk/troubleshooting-dependency-version-conflict#mitigate-version-mismatch-issues).

## Troubleshooting Logs Ingestion

### Troubleshooting authorization errors

If you get an HTTP error with status code 403 (Forbidden), it means that the provided credentials does not have
sufficient permissions to query the workspace.

```text
com.azure.core.exception.HttpResponseException: Status code 403, "{"error":{"code":"OperationFailed","message":"The 
authentication token provided does not have access to ingest data for the data collection rule with immutable Id 
'<REDACTED>' PipelineAccessResult: AccessGranted: False, IsRbacPresent: False, IsDcrDceBindingValid: , DcrArmId: <REDACTED>,
 Message: Required authorization action was not found for tenantId <REDACTED> objectId <REDACTED> on resourceId <REDACTED>
 ConfigurationId: <REDACTED>.."}}"
```

1. Check that the application or user that is making the request has sufficient permissions:
    * You can refer to this document
      to [manage access to data collection rule](https://learn.microsoft.com/azure/azure-monitor/logs/tutorial-logs-ingestion-portal#assign-permissions-to-the-dcr)
    * To ingest logs, ensure the service principal is assigned the "Monitoring Metrics Publisher" role for the data
      collection rule.
2. If the user or application is granted sufficient privileges to query the workspace, make sure you are
   authenticating as that user/application. If you are authenticating using the
   [DefaultAzureCredential](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity/README.md#authenticating-with-defaultazurecredential)
   then check the logs to verify that the credential used is the one you expected. To enable logging, see [enable
   client logging](#enable-client-logging) section above.
3. The permissions may take up to 30 minutes to propagate. So, if the permissions were granted recently, retry after
   some time.

### Troubleshooting missing logs in Log Analytics workspace after successful ingestion

When you send logs to Azure Monitor for ingestion, it's possible for the request to succeed, but you may not see the
data appearing in the designated Log Analytics workspace table as configured in the Data Collection Rule. To investigate
and resolve this issue, please ensure the following:

- Double-check that you are using the correct data collection endpoint when configuring
  the `LogsIngestionClientBuilder`.
  Using the wrong endpoint can result in data not being properly sent to the Log Analytics workspace.

- Make sure you provide the correct data collection rule id (DCR ID) to the `upload` method. The DCR ID is an immutable
  identifier that determines the transformation rules applied to the uploaded logs and directs them to the appropriate
  Log Analytics workspace table.

- Verify that the custom table specified in the data collection rule exists in the Log Analytics workspace. Ensure that
  you provide the accurate name of the custom table to the upload method. Mismatched table names can lead to logs not
  being stored correctly.

- Confirm that the logs you're sending adhere to the format expected by the data collection rule. The data should be in
  the form of a JSON object or array, structured according to the requirements specified in the DCR. Additionally, it's
  essential to encode the request body in UTF-8 to avoid any data transmission issues.

- Keep in mind that data ingestion may take some time, especially if you're sending data to a specific table for the
  first time. In such cases, allow up to 15 minutes for the data to be fully ingested and available for querying and
  analysis.

### Troubleshooting slow logs upload

If you experience delays when uploading logs, it could be due to reaching service limits, which may trigger the rate
limiter to throttle your client. To determine if your client has reached service limits, you can enable logging and
check if the service is returning errors with an HTTP status code 429. For more information on service limits, you can
refer to the [Azure Monitor service limits documentation](https://learn.microsoft.com/en-us/azure/azure-monitor/service-limits#logs-ingestion-api).
To enable client logging and troubleshoot this issue further, please follow the instructions provided in the section 
titled ["Enable Client Logging."](#enable-client-logging)

If there are no throttling errors, then consider increasing the concurrency to upload multiple log requests in parallel.
To set the concurrency, use `UploadLogsOptions` type to `setMaxConcurrency`.

```java readme-sample-uploadLogsWithMaxConcurrency
DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

LogsIngestionClient client = new LogsIngestionClientBuilder()
        .endpoint("<data-collection-endpoint")
        .credential(tokenCredential)
        .buildClient();

List<Object> logs = getLogs();
LogsUploadOptions logsUploadOptions = new LogsUploadOptions()
        .setMaxConcurrency(3);
client.upload("<data-collection-rule-id>", "<stream-name>", logs, logsUploadOptions,
        Context.NONE);
System.out.println("Logs uploaded successfully");
```
