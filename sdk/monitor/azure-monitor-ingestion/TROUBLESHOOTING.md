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

Refer to the instructions in this reference document on how to [configure logging in Azure SDK for Java](https://docs.microsoft.com/azure/developer/java/sdk/logging-overview).

### Enable HTTP request/response logging

Reviewing the HTTP request sent or response received over the wire to/from the Azure Monitor service can be useful in
troubleshooting issues. To enable logging the HTTP request and response payload, the LogsQueryClient and the
MetricsQueryClient can be configured as shown below:

```java readme-sample-enablehttplogging
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

Azure Monitor Ingestion supports Azure Active Directory authentication. The LogsIngestionClientBuilder can be configured to set the `credential`. To provide a valid credential, you can use
`azure-identity` dependency. For more details on getting started, refer to
the [README](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/monitor/azure-monitor-ingestion#create-the-client)
of Azure Monitor Ingestion library. You can also refer to
the [Azure Identity documentation](https://docs.microsoft.com/azure/developer/java/sdk/identity)
for more details on the various types of credential supported in `azure-identity`.

### Dependency Conflicts

If you see `NoSuchMethodError` or `NoClassDefFoundError` during your application runtime, this is due to a
dependency version conflict. Please take a look at [troubleshooting dependency version conflicts](https://docs.microsoft.com/azure/developer/java/sdk/troubleshooting-dependency-version-conflict) for more information on
why this happens and [ways to mitigate this issue](https://docs.microsoft.com/azure/developer/java/sdk/troubleshooting-dependency-version-conflict#mitigate-version-mismatch-issues).

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
    * You can refer to this document to [manage access to data collection rule](https://learn.microsoft.com/azure/azure-monitor/logs/tutorial-logs-ingestion-portal#assign-permissions-to-the-dcr)
    * To ingest logs, ensure the service principal is assigned the "Monitoring Metrics Publisher" role for the data collection rule.
2. If the user or application is granted sufficient privileges to query the workspace, make sure you are
   authenticating as that user/application. If you are authenticating using the
   [DefaultAzureCredential](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity/README.md#authenticating-with-defaultazurecredential)
   then check the logs to verify that the credential used is the one you expected. To enable logging, see [enable
   client logging](#enable-client-logging) section above. 
3. The permissions may take up to 30 minutes to propagate. So, if the permissions were granted recently, retry after some time.

### Troubleshooting missing logs in Log Analytics workspace after successful ingestion

When ingesting logs to Azure Monitor, the request can succeed but the data may not show up in the expected Log Analytics 
workspace table as configured in the Data Collection Rule. To investigate this, verify the following:
- Ensure that the correct data collection endpoint is used to configure the `LogsIngestionClientBuilder`.
- Ensure that the correct data collection rule id (the immutable DCR ID) is provided to the `upload` method. The DCR ID determines the
transformation rules to be applied to the uploaded logs and sent to the Log Analytics workspace table. 
- Ensure that the custom table used in data collection rule exists in Log Analytics workspace and the correct name of the custom table
is provided to the `upload` method.
- Ensure the logs are in the format the data collection rule was configured to accept. The shape of the data must be a 
JSON object or array with a structure that matches the format expected by the stream in the DCR. Additionally, it is 
important to ensure that the request body is properly encoded in UTF-8 to prevent any issues with data transmission.
- The data might take some time to be ingested, especially if this is the first time data is being sent to a particular 
table. It shouldn't take longer than 15 minutes.

