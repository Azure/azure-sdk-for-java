// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 * <p>Azure Monitor Query service is a powerful tool that allows you to query and analyze log data from various sources
 * in Azure. It is built on top of the Kusto Query Language (KQL), which is a powerful query language that allows you
 * to perform complex queries on large datasets. With Azure Monitor Query, you can easily search and analyze
 * log data from various sources, including virtual machines, containers, and applications.</p>
 *
 * <p>Azure Monitor Query java client library is a library that allows you to execute read-only queries against
 * Azure Monitor’s two data platforms: Logs and Metrics. The library provides both synchronous and asynchronous forms
 * of the clients.</p>
 *
 * <ul>
 *     <li><a href="https://learn.microsoft.com/azure/azure-monitor/logs/data-platform-logs">Logs</a> - Collects and
 *     organizes log and performance data from monitored resources. Data from different sources such as platform logs
 *     from Azure services, log and performance data from virtual machines agents, and usage and performance data from
 *     apps can be consolidated into a single <a href="https://learn.microsoft.com/azure/azure-monitor/logs/data-platform-logs#log-analytics-and-workspaces">Azure Log Analytics workspace</a>.
 *     The various data types can be analyzed together using the Kusto Query Language.</li>
 *
 *     <li><a href="https://learn.microsoft.com/azure/azure-monitor/essentials/data-platform-metrics">Metrics</a> - Collects
 *     numeric data from monitored resources into a time series database. Metrics are numerical values that are
 *     collected at regular intervals and describe some aspect of a system at a particular time. Metrics are lightweight
 *     and capable of supporting near real-time scenarios, making them particularly useful for alerting and fast
 *     detection of issues.</li>
 * </ul>
 *
 * <h2>Getting Started</h2>
 *
 * <p>In order to interact with the Monitor service you'll need to create an instance of the
 * {@link com.azure.monitor.query.LogsQueryClient} or {@link com.azure.monitor.query.MetricsQueryClient} class. To make
 * this possible you'll need to use AAD authentication via
 * <a href="https://learn.microsoft.com/java/api/overview/azure/identity-readme?view=azure-java-stable"> Azure Identity</a>
 * to connect to the service.</p>
 *
 * <p><strong>Sample: Construct Asynchronous Clients</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a {@link com.azure.monitor.query.LogsQueryAsyncClient}
 * using the {@link com.azure.monitor.query.LogsQueryClientBuilder}.</p>
 *
 * <!-- src_embed com.azure.monitor.query.LogsQueryAsyncClient.instantiation -->
 * <pre>
 * LogsQueryAsyncClient logsQueryAsyncClient = new LogsQueryClientBuilder&#40;&#41;
 *         .credential&#40;tokenCredential&#41;
 *         .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.monitor.query.LogsQueryAsyncClient.instantiation -->
 *
 * <p>The following code sample demonstrates the creation of a {@link com.azure.monitor.query.MetricsQueryAsyncClient}
 * using the {@link com.azure.monitor.query.MetricsQueryClientBuilder}.</p>
 *
 * <!-- src_embed com.azure.monitor.query.MetricsQueryAsyncClient.instantiation -->
 * <pre>
 * MetricsQueryAsyncClient metricsQueryAsyncClient = new MetricsQueryClientBuilder&#40;&#41;
 *         .credential&#40;tokenCredential&#41;
 *         .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.monitor.query.MetricsQueryAsyncClient.instantiation -->
 *
 * <p><strong>Sample: Construct Synchronous Clients</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a {@link com.azure.monitor.query.LogsQueryClient}
 * using the {@link com.azure.monitor.query.LogsQueryClientBuilder}.</p>
 *
 * <!-- src_embed com.azure.monitor.query.LogsQueryClient.instantiation -->
 * <pre>
 * LogsQueryClient logsQueryClient = new LogsQueryClientBuilder&#40;&#41;
 *         .credential&#40;tokenCredential&#41;
 *         .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.monitor.query.LogsQueryClient.instantiation -->
 *
 * <p>The following code sample demonstrates the creation of a {@link com.azure.monitor.query.MetricsQueryClient}
 * using the {@link com.azure.monitor.query.MetricsQueryClientBuilder}.</p>
 *
 * <!-- src_embed com.azure.monitor.query.MetricsQueryClient.instantiation -->
 * <pre>
 * MetricsQueryClient metricsQueryClient = new MetricsQueryClientBuilder&#40;&#41;
 *         .credential&#40;tokenCredential&#41;
 *         .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.monitor.query.MetricsQueryClient.instantiation -->
 *
 * <br/>
 *
 * <hr/>
 *
 * <h2>Query Workspace</h2>
 *
 * <p>The {@link com.azure.monitor.query.LogsQueryClient#queryWorkspace(java.lang.String, java.lang.String, com.azure.monitor.query.models.QueryTimeInterval) Query Workspace API} method can be used to
 * query logs from a given workspace.</p>
 *
 * <p>The sample below shows how to query logs from the last 24 hours</p>
 *
 * <!-- src_embed com.azure.monitor.query.LogsQueryClient.query#String-String-QueryTimeInterval -->
 * <pre>
 * LogsQueryResult queryResult = logsQueryClient.queryWorkspace&#40;&quot;&#123;workspace-id&#125;&quot;, &quot;&#123;kusto-query&#125;&quot;,
 *         QueryTimeInterval.LAST_DAY&#41;;
 * for &#40;LogsTableRow row : queryResult.getTable&#40;&#41;.getRows&#40;&#41;&#41; &#123;
 *     System.out.println&#40;row.getRow&#40;&#41;
 *             .stream&#40;&#41;
 *             .map&#40;LogsTableCell::getValueAsString&#41;
 *             .collect&#40;Collectors.joining&#40;&quot;,&quot;&#41;&#41;&#41;;
 * &#125;
 * </pre>
 * <!-- end com.azure.monitor.query.LogsQueryClient.query#String-String-QueryTimeInterval -->
 *
 *
 * <p><strong>Note:</strong> For asynchronous sample, refer to {@link com.azure.monitor.query.LogsQueryAsyncClient#queryWorkspace(java.lang.String, java.lang.String, com.azure.monitor.query.models.QueryTimeInterval) QueryWorkspace Async API}.</p>
 *
 * <br/>
 *
 * <hr/>
 *
 * @see com.azure.monitor.query.LogsQueryClientBuilder
 * @see com.azure.monitor.query.LogsQueryClient
 * @see com.azure.monitor.query.LogsQueryAsyncClient
 * @see com.azure.monitor.query.MetricsQueryClientBuilder
 * @see com.azure.monitor.query.MetricsQueryClient
 * @see com.azure.monitor.query.MetricsQueryAsyncClient
 */
package com.azure.monitor.query;
