# Troubleshooting App Configuration issues

This troubleshooting guide covers failure investigation techniques, common errors for the credential types in the Azure 
App Configuration Java client library, and mitigation steps to resolve these errors.

## Table of Contents

* [General Troubleshooting](#general-troubleshooting)
  * [Enable client logging](#enable-client-logging)
  * [Enable HTTP request/response logging](#enable-http-requestresponse-logging)
  * [Troubleshooting authentication issues with logs and metrics query requests](#authentication-errors)
  * [Troubleshooting NoSuchMethodError or NoClassDefFoundError](#dependency-conflicts)

* [Identifying and Troubleshooting Issues by Response Code](#troubleshooting-issues-by-response-code)
* [Get additional help](#get-additional-help)

## General Troubleshooting

### Enable client logging

To troubleshoot issues with Azure App Configuration library, it is important to first enable logging to monitor the
behavior of the application. The errors and warnings in the logs generally provide useful insights into what went wrong 
and sometimes include corrective actions to fix issues. The Azure client libraries for Java have two logging options:

* A built-in logging framework.
* Support for logging using the [SLF4J](https://www.slf4j.org/) interface.

Refer to the instructions in this reference document on how to [configure logging in Azure SDK for Java](https://docs.microsoft.com/azure/developer/java/sdk/logging-overview).

### Enable HTTP request/response logging

Reviewing the HTTP request sent or response received over the wire to/from the Azure App Configuration service can be 
useful in troubleshooting issues. To enable logging the HTTP request and response payload, the [ConfigurationClient][configuration_client] 
can be configured as shown below:

```java readme-sample-enablehttplogging
ConfigurationClient configurationClient = new ConfigurationClientBuilder()
        .connectionString(connectionString)
        .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
        .buildClient();
// or
DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
ConfigurationClient configurationClientAad = new ConfigurationClientBuilder()
        .credential(credential)
        .endpoint(endpoint)
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
new ConfigurationClientBuilder().httpLogOptions(new HttpLogOptions().addAllowedHeaderName("safe-to-log-header-name"))
```

### Authentication errors

Azure App Configuration supports Azure Active Directory authentication. [ConfigurationClientBuilder][configuration_client_builder] has method to set 
the `credential`. To provide a valid credential, you can use `azure-identity` dependency. For more details on getting 
started, refer to the [README](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/appconfiguration/azure-data-appconfiguration#create-a-configuration-client)
of Azure App Configuration library. You can also refer to the [Azure Identity documentation](https://docs.microsoft.com/azure/developer/java/sdk/identity)
for more details on the various types of credential supported in `azure-identity`.

### Dependency Conflicts

If you see `NoSuchMethodError` or `NoClassDefFoundError` during your application runtime, this is due to a
dependency version conflict. Please take a look at [troubleshooting dependency version conflicts](https://docs.microsoft.com/azure/developer/java/sdk/troubleshooting-dependency-version-conflict) for more information on
why this happens and [ways to mitigate this issue](https://docs.microsoft.com/azure/developer/java/sdk/troubleshooting-dependency-version-conflict#mitigate-version-mismatch-issues).

## Troubleshooting Issues By Response Code


## Get additional help

Additional information on ways to reach out for support can be found in the [SUPPORT.md](https://github.com/Azure/azure-sdk-for-java/blob/main/SUPPORT.md) at the root of the repo.

<!-- Links -->
[configuration_client]: https://learn.microsoft.com/java/api/com.azure.data.appconfiguration.configurationclient?view=azure-java-stable
[configuration_client_builder]: https://learn.microsoft.com/java/api/com.azure.data.appconfiguration.configurationclientbuilder?view=azure-java-stable
