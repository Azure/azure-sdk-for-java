# Guide for migrating to `azure-monitor-query` from `azure-loganalytics` and `azure-applicationinsights-query`

This guide assists in migrating from `azure-loganalytics` and `azure-applicationinsights-query` to `azure-monitor-query`. 

## Migration benefits

A natural question to ask when considering whether or not to adopt a new version or library is its benefits. As Azure has matured and been embraced by a more diverse group of developers, we have been 
focused on learning the patterns and practices to best support developer productivity and to understand the gaps that the Java client libraries have.

There were several areas of consistent feedback expressed across the Azure client library ecosystem. The most important is that the client libraries for different Azure services have not had a 
consistent organization, naming, and API structure. Additionally, many developers have felt that the learning curve was difficult, and the APIs did not offer a good, approachable, and consistent 
onboarding story for those learning Azure or exploring a specific Azure service.

To improve the development experience across Azure services, a set of uniform [design guidelines][Guidelines] was created for all languages to drive a
consistent experience with established API patterns for all services. A set of [Java design guidelines][GuidelinesJava] was introduced to ensure that Java clients have a natural and idiomatic feel 
with respect to the Java ecosystem. Further details are available in the guidelines for those interested.

In addition to the improved development experience, the new `azure-monitor-query` also has new features that are not available in `azure-loganalytics` and `azure-applicationinsights-query`. This library supports querying Azure Monitor for
both logs and metrics while the `azure-loganalytics` and `azure-applicationinsights-query`  libraries only supported querying logs. The new library also includes additional capabilities for querying logs like executing a batch of queries, 
setting the server timeout, getting the visualization information and statistics for a query.

Another key difference is that `azure-loganalytics` and `azure-applicationinsights-query` only have a preview release. It's not recommended for use in a production environment. The `azure-monitor-query` package has a stable release and is ready for production. The latest stable version can be found in the [README][README]. 

### Cross-service SDK improvements

The modern Azure Monitor Query client library also provides the ability to share in some of the cross-service improvements made to the Azure development experience. Some improvements include:

- A unified logging and diagnostics pipeline offering a common view of the activities across each of the client libraries.
- A unified asynchronous programming model using [Project Reactor][project-reactor].
- A unified method of creating clients via client builders to interact with Azure services.

## Important changes

### Group ID, artifact ID, and package names

Group IDs, artifact IDs, and package names for the modern Azure client libraries for Java have changed. They follow the [Java SDK naming guidelines][GuidelinesJavaDesign]. Each will have the group ID `com.azure`, an artifact ID following the pattern `azure-[area]-[service]`, and the root package name `com.azure.[area].[Service]`. The legacy clients have a group ID of `com.microsoft.azure`, and their package names followed the pattern `com.microsoft.azure.[service]`. This provides a quick and accessible means to help understand, at a glance, whether you're using modern or legacy clients.

The Azure Monitor Query client library's package and namespaces begin with `com.azure.monitor.query` and were released starting with version 1.0.0. The legacy client libraries had package names starting with `com.microsoft.azure.loganalytics` or `com.microsoft.azure.applicationinsights` and a version of 1.0.0-beta.1.

#### Instantiate clients 

In `azure-loganalytics`, the `LogAnalyticsDataClient` is instantiated via the `LogAnalyticsDataClientImpl` constructor. The client contains both sync and async methods.

```java
// ApplicationTokenCredentials work well for service principal authentication
ApplicationTokenCredentials credentials = new ApplicationTokenCredentials(
        "<clientId>",
        "<tenantId>",
        "<clientSecret>",
        AzureEnvironment.AZURE
        );

// New up client. Accepts credentials, or a pre-authenticated restClient
LogAnalyticsDataClient client = new LogAnalyticsDataClientImpl(credentials);
```

In `azure-applicationinsights`, the `ApplicationInsightsDataClient` is instantiated via the `ApplicationInsightsDataClientImpl` constructor. The client contains sub-client called `Querys` that have sync and async methods to execute queries.

```java
// ApplicationTokenCredentials work well for service principal authentication
ApplicationTokenCredentials credentials = new ApplicationTokenCredentials(
        "<clientId>",
        "<tenantId>",
        "<clientSecret>",
        AzureEnvironment.AZURE
        );

// New up client. Accepts credentials, or a pre-authenticated restClient
ApplicationInsightsDataClient client = new ApplicationInsightsDataClientImpl(credentials);
Querys applicationInsightsQueryClient = client.querys();
```

In `azure-monitor-query`:

- The creation of the client is done through the [LogsQueryClientBuilder][LogsQueryClientBuilder]. The sync and async operations are separated to [LogsQueryClient] and [LogsQueryAsyncClient].
- The authentication is done through `TokenCredential`. There are several `TokenCredential` types implemented in the `azure-identity` library that support Azure AD auth. For more details on `azure-identity`, see the [Azure Identity README][azure-identity-readme].

***Create a sync client***
```java
TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();
LogsQueryClient logsQueryClient = new LogsQueryClientBuilder()
        .credential(tokenCredential)
        .buildClient();
```

***Create an async client***
```java
TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();
LogsQueryAsyncClient logsQueryAsyncClient = new LogsQueryClientBuilder()
        .credential(tokenCredential)
        .buildAsyncClient();
```

#### Query logs synchronously
In `azure-loganalytics`, logs can be queried synchronously as shown below:

```java
String query = "Heartbeat | take 1";
String workspaceId = "<workspace-id>";
QueryResults queryResults = logAnalyticsClient.query(workspaceId, new QueryBody().withQuery(query));
```
In `azure-applicationinsights-query`, logs can be queried synchronously as shown below:

```java
String query = "Heartbeat | take 1";
String workspaceId = "<workspace-id>";
QueryResults queryResults = applicationInsightsQueryClient.execute(workspaceId, new QueryBody().withQuery(query));
```

In `azure-monitor-query`, logs can be queried synchronously using the sync client as shown below:

```java
String query = "Heartbeat | take 1";
String workspaceId = "<workspace-id>";
LogsQueryResult queryResults = logsQueryClient.queryWorkspace(workspaceId, query, QueryTimeInterval.ALL);
```

#### Query logs asynchronously
In `azure-loganalytics`, logs can be queried asynchronously as shown below:

```java
String query = "Heartbeat | take 1";
String workspaceId = "<workspace-id>";
Observable<QueryResults> queryResultsObservable = logAnalyticsClient.queryAsync(workspaceId, new QueryBody().withQuery(query));
queryResultsObservable.subscribe(queryResults -> {
            // process results
            queryResults.tables();
        });
```

In `azure-applicationinsights-query`, logs can be queried asynchronously as shown below:

```java
String query = "Heartbeat | take 1";
String workspaceId = "<workspace-id>";
Observable<QueryResults> queryResultsObservable = applicationInsightsQueryClient.executeAsync(workspaceId, new QueryBody().withQuery(query));
queryResultsObservable.subscribe(queryResults -> {
            // process results
            queryResults.tables();
        });
```

In `azure-monitor-query`, logs can be queried synchronously using the sync client as shown below:

```java
String query = "Heartbeat | take 1";
String workspaceId = "<workspace-id>";
Mono<LogsQueryResult> queryResultsMono = logsQueryAsyncClient.queryWorkspace(workspaceId, query, QueryTimeInterval.ALL);
queryResultsMono.subscribe(queryResult -> {
            // process results
            queryResult.getAllTables();
        });
```

## Additional samples

More examples can be found at [Azure Monitor Query samples][README-Samples].

<!-- Links -->
[LogsQueryClientBuilder]: https://azuresdkdocs.blob.core.windows.net/$web/java/azure-monitor-query/latest/com/azure/monitor/query/LogsQueryClientBuilder.html
[LogsQueryClient]: https://azuresdkdocs.blob.core.windows.net/$web/java/azure-monitor-query/latest/com/azure/monitor/query/LogsQueryClient.html
[LogsQueryAsyncClient]: https://azuresdkdocs.blob.core.windows.net/$web/java/azure-monitor-query/latest/com/azure/monitor/query/LogsQueryAsyncClient.html
[Guidelines]: https://azure.github.io/azure-sdk/general_introduction.html
[GuidelinesJava]: https://azure.github.io/azure-sdk/java_introduction.html
[GuidelinesJavaDesign]: https://azure.github.io/azure-sdk/java_introduction.html#namespaces
[project-reactor]: https://projectreactor.io/
[README-Samples]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/monitor/azure-monitor-query/src/samples/java/README.md
[README]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/monitor/azure-monitor-query/README.md
[azure-identity-readme]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity/README.md

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fmonitor%2Fazure-monitor-query%2Fmigration-guide.png)
