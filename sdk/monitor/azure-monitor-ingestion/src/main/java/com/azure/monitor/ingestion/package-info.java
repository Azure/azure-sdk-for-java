// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
/**
 * The Azure Monitor Ingestion client library is used to send custom logs to an <a
 * href="https://learn.microsoft.com/azure/azure-monitor/overview">Azure Monitor</a>
 * Log Analytics workspace.
 *
 * <h2>Getting Started</h2>
 *
 * <h3>Prerequisites</h3>
 *
 * <p>The client library requires the following:</p>
 *
 * <ul>
 * <li>Java 8 or later</li>
 * <li>An Azure subscription</li>
 * <li>An existing Azure Monitor Data Collection Rule</li>
 * <li>An existing Azure Monitor Data Collection Endpoint</li>
 * <li>An existing Azure Monitor Log Analytics workspace</li>
 * </ul>
 *
 * <hr/>
 *
 * <h3>Authenticate a Client</h3>
 *
 * <p>
 * The {@link com.azure.monitor.ingestion.LogsIngestionClient LogIngestionClient} and
 * {@link com.azure.monitor.ingestion.LogsIngestionAsyncClient LogIngestionAsyncClient} can be authenticated
 * using Microsoft Entra ID. To authenticate with Microsoft Entra ID, create a
 * {@link com.azure.core.credential.TokenCredential TokenCredential} that can be passed to the
 * {@link com.azure.monitor.ingestion.LogsIngestionClientBuilder LogIngestionClientBuilder}. The Azure Identity
 * library provides implementations of {@link com.azure.core.credential.TokenCredential TokenCredential} for
 * multiple authentication flows. See {@link com.azure.core.credential.TokenCredential TokenCredential} for multiple
 * authentication flows. See <a
 * href="https://learn.microsoft.com/java/api/overview/azure/identity-readme?view=azure-java-stable">Azure Identity</a>
 * <a href="https://learn.microsoft.com/java/api/overview/azure/identity-readme?view=azure-java-stable">Azure
 * Identity</a>
 * for more information. See {@link com.azure.monitor.ingestion.LogsIngestionClientBuilder LogIngestionClientBuilder}
 * for more examples on authenticating a client.
 * </p>
 *
 * <p>
 * The following sample demonstrates how to create a {@link com.azure.monitor.ingestion.LogsIngestionClient
 * LogIngestionClient}
 * using {@link com.azure.monitor.ingestion.LogsIngestionClientBuilder LogIngestionClientBuilder} and TokenCredential
 * authentication.
 * </p>
 *
 * <!-- src_embed com.azure.monitor.ingestion.LogsIngestionClient.instantiation -->
 * <pre>
 * LogsIngestionClient logsIngestionClient = new LogsIngestionClientBuilder&#40;&#41;
 *         .credential&#40;tokenCredential&#41;
 *         .endpoint&#40;&quot;&lt;data-collection-endpoint&gt;&quot;&#41;
 *         .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.monitor.ingestion.LogsIngestionClient.instantiation -->
 *
 * <hr/>
 *
 * <h2>Overview</h2>
 *
 * <p>
 * The Logs Ingestion REST API in Azure Monitor lets you send data to a Log Analytics workspace.
 * The API allows you to send data to <a
 * href="https://learn.microsoft.com/azure/azure-monitor/logs/logs-ingestion-api-overview#supported-tables">supported
 * tables</a>
 * or to custom tables that you create. You can
 * also extend the schema of Azure tables with custom columns to accept additional data.
 * </p>
 *
 * <p>
 * The Azure Monitor Ingestion client library provides both synchronous and asynchronous client implementations,
 * providing you the capability to send custom logs to an Azure Monitor Log Analytics workspace.
 * </p>
 *
 * <hr/>
 *
 * <h3>Key Concepts</h3>
 *
 * <p><strong>Data Collection Endpoint</strong></p>
 *
 * <p>
 * Data Collection Endpoints (DCEs) allow you to uniquely configure ingestion settings for Azure Monitor.
 * <a
 * href="https://learn.microsoft.com/azure/azure-monitor/essentials/data-collection-endpoint-overview?tabs=portal">This
 * article</a>
 * provides an overview of data collection endpoints including their contents and structure and how you can create and
 * work with them.
 * </p>
 *
 * <p><strong>Data Collection Rule</strong></p>
 *
 * <p>
 * Data collection rules (DCR) define data collected by Azure Monitor and specify how and where that data should be sent
 * or stored. The REST API call must specify a DCR to use. A single DCE can support multiple DCRs, so you can specify a
 * different DCR for different sources and target tables.
 * </p>
 *
 * <p>
 * The DCR must understand the structure of the input data and the structure of the target table. If the two don't
 * match,
 * it can use a transformation to convert the source data to match the target table. You may also use the transform
 * to filter source data and perform any other calculations or conversions.
 * </p>
 *
 * <p>
 * For more details, see <a
 * href="https://learn.microsoft.com/azure/azure-monitor/essentials/data-collection-rule-overview">Data collection
 * rules</a>
 * in Azure Monitor. For information on how to retrieve a DCR ID,
 * <a
 * href="https://learn.microsoft.com/azure/azure-monitor/logs/tutorial-logs-ingestion-portal#collect-information-from-the-dcr">see
 * this tutorial</a>.
 * </p>
 *
 * <p><strong>Log Analytics Workspace Tables</strong></p>
 *
 * <p>
 * Custom logs can send data to any custom table that you create and to
 * <a href="https://learn.microsoft.com/azure/azure-monitor/logs/logs-ingestion-api-overview#supported-tables">certain
 * built-in tables</a>
 * in your Log Analytics workspace. The target table must exist before you can send data to it.
 * </p>
 *
 * <p><strong>Logs retrieval</strong></p>
 *
 * <p>
 * The logs that were uploaded using this library can be queried using the
 * <a href="https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/monitor/azure-monitor-query#readme">Azure Monitor
 * Query client library</a>.
 * </p>
 *
 * <hr/>
 *
 * <h2>Client Usage</h2>
 *
 * <h3>
 * Uploading logs to Azure Monitor
 * </h3>
 *
 * <p>
 * The following sample demonstrates how to upload logs to Azure Monitor using
 * {@link com.azure.monitor.ingestion.LogsIngestionClient LogIngestionClient}.
 * </p>
 *
 * <!-- src_embed com.azure.monitor.ingestion.LogsIngestionClient.upload -->
 * <pre>
 * List&lt;Object&gt; logs = getLogs&#40;&#41;;
 * logsIngestionClient.upload&#40;&quot;&lt;data-collection-rule-id&gt;&quot;, &quot;&lt;stream-name&gt;&quot;, logs&#41;;
 * System.out.println&#40;&quot;Logs uploaded successfully&quot;&#41;;
 * </pre>
 * <!-- end com.azure.monitor.ingestion.LogsIngestionClient.upload -->
 *
 * <p>
 * For more synchronous and asynchronous client usage information, see
 * {@link com.azure.monitor.ingestion.LogsIngestionClient} and
 * {@link com.azure.monitor.ingestion.LogsIngestionAsyncClient}, respectively.
 * </p>
 *
 * @see com.azure.monitor.ingestion.LogsIngestionClient
 * @see com.azure.monitor.ingestion.LogsIngestionAsyncClient
 * @see com.azure.monitor.ingestion.LogsIngestionClientBuilder
 */
package com.azure.monitor.ingestion;
