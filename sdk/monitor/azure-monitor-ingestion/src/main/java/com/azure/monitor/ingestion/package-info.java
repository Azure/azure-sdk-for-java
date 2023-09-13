// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 * The Azure Monitor Ingestion client library is used to send custom logs to an [Azure Monitor](https://learn.microsoft.com/azure/azure-monitor/overview)
 * Log Analytics workspace.
 *
 * <h2>Getting Started</h2>
 *
 * <p><strong>Create the client</strong></p>
 * An authenticated client is required to upload logs to Azure Monitor Log Analytics workspace. This package includes both synchronous and
 * asynchronous forms of the clients. Use {@link com.azure.monitor.ingestion.LogsIngestionClientBuilder LogsIngestionClientBuilder} to
 * customize and create an instance of {@link com.azure.monitor.ingestion.LogsIngestionClient LogsIngestionClient} or
 * {@link com.azure.monitor.ingestion.LogsIngestionAsyncClient LogsIngestionAsyncClient}.
 *
 * <p><strong>Authenticate the client</strong></p>
 * <p>
 * The {@code LogsIngestionClient} and {@code LogsIngestionAsyncClient} can be authenticated using Azure Active Directory.
 * To authenticate with Azure Active Directory, create a {@link com.azure.core.credential.TokenCredential TokenCredential}
 * that can be passed to the {@code LogsIngestionClientBuilder}. The Azure Identity library provides implementations of
 * {@code TokenCredential} for multiple authentication flows. See
 * <a href="https://learn.microsoft.com/java/api/overview/azure/identity-readme?view=azure-java-stable">Azure Identity</a>
 * for more information.
 *
 * <h2>Key Concepts</h2>
 * <p><strong>Data Collection Endpoint</strong></p>
 * Data Collection Endpoints (DCEs) allow you to uniquely configure ingestion settings for Azure Monitor.
 * <a href="https://learn.microsoft.com/azure/azure-monitor/essentials/data-collection-endpoint-overview?tabs=portal">This article</a>
 * provides an overview of data collection endpoints including their contents and structure and how you can create and work with them.
 *
 *
 * <p><strong>Data Collection Rule</strong></p>
 * <p>
 * Data collection rules (DCR) define data collected by Azure Monitor and specify how and where that data should be sent
 * or stored. The REST API call must specify a DCR to use. A single DCE can support multiple DCRs, so you can specify a different DCR for different sources and target tables.
 * <p>
 * The DCR must understand the structure of the input data and the structure of the target table. If the two don't match,
 * it can use a transformation to convert the source data to match the target table. You may also use the transform to filter source data and perform any other calculations or conversions.
 * <p>
 * For more details, see <a href="https://learn.microsoft.com/azure/azure-monitor/essentials/data-collection-rule-overview">Data collection rules</a>
 * in Azure Monitor. For information on how to retrieve a DCR ID,
 * <a href="https://learn.microsoft.com/azure/azure-monitor/logs/tutorial-logs-ingestion-portal#collect-information-from-the-dcr">see this tutorial</a>.
 *
 * <p><strong>Log Analytics Workspace Tables</strong></p>
 * Custom logs can send data to any custom table that you create and to certain built-in tables in your Log Analytics
 * workspace. The target table must exist before you can send data to it. The following built-in tables are currently supported:
 * <ol>
 * <li>CommonSecurityLog</li>
 * <li>SecurityEvents</li>
 * <li>Syslog</li>
 * <li>WindowsEvents</li>
 * </ol>
 * <p><strong>Logs retrieval</strong></p>
 * The logs that were uploaded using this library can be queried using the
 * <a href="https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/monitor/azure-monitor-query#readme">Azure Monitor Query client library</a>.
 */
package com.azure.monitor.ingestion;
