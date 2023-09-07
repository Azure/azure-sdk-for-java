// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 * <p><a href="https://learn.microsoft.com/azure/ai-services/metrics-advisor/">
 * Azure Metrics Advisor</a> is a cloud-based service provided by Microsoft Azure that provides a set of APIs for
 * data ingestion, anomaly detection, and diagnostics, without needing to know machine learning.</p>
 *
 * <p>The Azure Metrics Advisor client library allows Java developers to interact with the Azure Metrics Advisor
 * service.
 * It provides a set of classes and methods that abstract the underlying RESTful API of Azure
 * Metrics Advisor, making it easier to integrate the service into Java applications.</p>
 *
 * <p>The Azure Metrics Advisor client library provides the following capabilities:</p>
 *
 * <ol>
 *     <li>Analyze multi-dimensional data from multiple data sources.</li>
 *     <li>Identify and correlate anomalies</li>
 *     <li>Configure and fine-tune the anomaly detection model used on your data</li>
 *     <li>Diagnose anomalies and help with root cause analysis</li>
 * </ol>
 *
 * <h2>Getting Started</h2>
 *
 * <p>The Azure Metrics Advisor library provides advisor clients like
 * {@link com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient}
 * and {@link com.azure.ai.metricsadvisor.MetricsAdvisorClient} to connect to the Metrics Advisor
 * Azure Cognitive Service to perform data monitoring.
 * It also provides administration clients like
 * {@link com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient}
 * and {@link com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient} to
 * build and manage models from custom documents.</p>
 *
 * <p>Service clients are the point of interaction for developers to use Azure Form Recognizer.
 * {@link com.azure.ai.metricsadvisor.MetricsAdvisorClient} is the synchronous service client and
 * {@link com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient} is the asynchronous service client.
 * The examples shown in this document use a credential object named DefaultAzureCredential for authentication, which is
 * appropriate for most scenarios, including local development and production environments. Additionally, we
 * recommend using
 * <a href="https://learn.microsoft.com/azure/active-directory/managed-identities-azure-resources/">managed identity</a>
 * for authentication in production environments.
 * You can find more information on different ways of authenticating and their corresponding credential types in the
 * <a href="https://learn.microsoft.com/java/api/overview/azure/identity-readme">Azure Identity documentation"</a>.
 * </p>
 *
 * <p><strong>Sample: Construct a MetricsAdvisorClient with DefaultAzureCredential</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a
 * {@link com.azure.ai.metricsadvisor.MetricsAdvisorClient}, using
 * the `DefaultAzureCredentialBuilder` to configure it.</p>
 *
 * <!-- src_embed com.azure.ai.metricsadvisor.MetricsAdvisorClient.withAAD -->
 * <pre>
 * MetricsAdvisorClient metricsAdvisorClient =
 *     new MetricsAdvisorClientBuilder&#40;&#41;
 *         .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;
 *         .endpoint&#40;&quot;&#123;endpoint&#125;&quot;&#41;
 *         .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.ai.metricsadvisor.MetricsAdvisorClient.withAAD  -->
 *
 * <p>Further, see the code sample below to use
 * {@link com.azure.ai.metricsadvisor.models.MetricsAdvisorKeyCredential MetricsAdvisorKeyCredential} for client creation.</p>
 *
 * <!-- src_embed com.azure.ai.metricsadvisor.MetricsAdvisorClient.instantiation -->
 * <pre>
 * MetricsAdvisorClient metricsAdvisorClient =
 *     new MetricsAdvisorClientBuilder&#40;&#41;
 *         .credential&#40;new MetricsAdvisorKeyCredential&#40;&quot;&#123;subscription_key&#125;&quot;, &quot;&#123;api_key&#125;&quot;&#41;&#41;
 *         .endpoint&#40;&quot;&#123;endpoint&#125;&quot;&#41;
 *         .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.ai.metricsadvisor.MetricsAdvisorClient.instantiation  -->
 *
 * <p>Let's take a look at the advisor client scenarios and their respective usage below.</p>
 *
 * <br>
 *
 * <hr>
 * <h2>Get the time series data from metric for you own ingested data</h2>
 *
 * <p><strong>Sample: Given a metric list the time series data from a metricId</strong></p>
 *
 * <p>The following code sample demonstrates to get metric data time series information.</p>
 *
 * <!-- src_embed com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listMetricSeriesData#String-List-OffsetDateTime-OffsetDateTime -->
 * <pre>
 * final String metricId = &quot;2dgfbbbb-41ec-a637-677e77b81455&quot;;
 * final OffsetDateTime startTime = OffsetDateTime.parse&#40;&quot;2020-09-09T00:00:00Z&quot;&#41;;
 * final OffsetDateTime endTime = OffsetDateTime.parse&#40;&quot;2020-09-09T12:00:00Z&quot;&#41;;
 *
 * final List&lt;DimensionKey&gt; seriesKeyFilter
 *     = Arrays.asList&#40;new DimensionKey&#40;&#41;.put&#40;&quot;cost&quot;, &quot;redmond&quot;&#41;&#41;;
 *
 * metricsAdvisorAsyncClient.listMetricSeriesData&#40;metricId, seriesKeyFilter, startTime, endTime&#41;
 *     .subscribe&#40;metricSeriesData -&gt; &#123;
 *         System.out.println&#40;&quot;List of data points for this series:&quot;&#41;;
 *         System.out.println&#40;metricSeriesData.getMetricValues&#40;&#41;&#41;;
 *         System.out.println&#40;&quot;Timestamps of the data related to this time series:&quot;&#41;;
 *         System.out.println&#40;metricSeriesData.getTimestamps&#40;&#41;&#41;;
 *         System.out.printf&#40;&quot;Series Key:&quot;&#41;;
 *         System.out.println&#40;metricSeriesData.getSeriesKey&#40;&#41;.asMap&#40;&#41;&#41;;
 *     &#125;&#41;;
 * </pre>
 * <!-- end com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listMetricSeriesData#String-List-OffsetDateTime-OffsetDateTime -->
 * <p>
 *
 * <br>
 *
 * <hr>
 *
 * <h2>Fetch the anomalies identified by an anomaly detection configuration</h2>
 *
 * <p>You can use the detected anomalies reflect actual anomalies in your data.</p>
 *
 * <p><strong>Sample: Query anomalies under anomaly detection configuration</strong></p>
 *
 * <p>The following code sample demonstrates how fetch all anomalies detected for a specific anomaly detection
 * configuration.</p>
 *
 * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createDetectionConfigWithResponse#String-AnomalyDetectionConfiguration-Context -->
 * <pre>
 * final String detectionConfigurationId = &quot;c0f2539f-b804-4ab9-a70f-0da0c89c76d8&quot;;
 * final OffsetDateTime startTime = OffsetDateTime.parse&#40;&quot;2020-09-09T00:00:00Z&quot;&#41;;
 * final OffsetDateTime endTime = OffsetDateTime.parse&#40;&quot;2020-09-09T12:00:00Z&quot;&#41;;
 *
 * metricsAdvisorAsyncClient.listAnomaliesForDetectionConfig&#40;detectionConfigurationId,
 *     startTime, endTime&#41;
 *     .subscribe&#40;anomaly -&gt; &#123;
 *         System.out.printf&#40;&quot;DataPoint Anomaly AnomalySeverity: %s%n&quot;, anomaly.getSeverity&#40;&#41;&#41;;
 *         System.out.printf&#40;&quot;Series Key:&quot;&#41;;
 *         DimensionKey seriesKey = anomaly.getSeriesKey&#40;&#41;;
 *         for &#40;Map.Entry&lt;String, String&gt; dimension : seriesKey.asMap&#40;&#41;.entrySet&#40;&#41;&#41; &#123;
 *             System.out.printf&#40;&quot;DimensionName: %s DimensionValue:%s%n&quot;,
 *                 dimension.getKey&#40;&#41;, dimension.getValue&#40;&#41;&#41;;
 *         &#125;
 *     &#125;&#41;;
 * </pre>
 * <!-- end com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient.listAnomaliesForDetectionConfig#String-OffsetDateTime-OffsetDateTime -->
 * <p>
 *
 * <br>
 *
 * <hr>
 *
 * <h2>List the root causes for an incident</h2>
 *
 * <p>Metrics Advisor will automatically group anomalies that share the same root cause into one incident.
 * An incident usually indicates a real issue, Metrics Advisor performs analysis on top of it and provides automatic
 * root cause analysis insights.</p>
 *
 * <p><strong>Sample: Find all the possible root causes for an incident</strong></p>
 *
 * <!-- src_embed com.azure.ai.metricsadvisor.MetricsAdvisorClient.listIncidentRootCauses#String-String -->
 * <pre>
 * final String detectionConfigurationId = &quot;c0dddf2539f-b804-4ab9-a70f-0da0c89c76d8&quot;;
 * final String incidentId = &quot;c5thh0f2539f-b804-4ab9-a70f-0da0c89c456d&quot;;
 *
 * metricsAdvisorClient.listIncidentRootCauses&#40;detectionConfigurationId, incidentId&#41;
 *     .forEach&#40;incidentRootCause -&gt; &#123;
 *         System.out.printf&#40;&quot;Description: %s%n&quot;, incidentRootCause.getDescription&#40;&#41;&#41;;
 *         System.out.printf&#40;&quot;Series Key:&quot;&#41;;
 *         System.out.println&#40;incidentRootCause.getSeriesKey&#40;&#41;.asMap&#40;&#41;&#41;;
 *         System.out.printf&#40;&quot;Confidence for the detected incident root cause %.2f%n&quot;,
 *             incidentRootCause.getContributionScore&#40;&#41;&#41;;
 *     &#125;&#41;;
 *
 * </pre>
 * <!-- end com.azure.ai.metricsadvisor.MetricsAdvisorClient.listIncidentRootCauses#String-String -->
 *
 * @see com.azure.ai.metricsadvisor.MetricsAdvisorClient
 * @see com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient
 * @see com.azure.ai.metricsadvisor.MetricsAdvisorClientBuilder
 */
package com.azure.ai.metricsadvisor;
