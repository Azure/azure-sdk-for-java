// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 * <p><a href="https://learn.microsoft.com/azure/ai-services/metrics-advisor/">Azure Metrics Advisor</a> is a
 *  cloud-based service provided by Microsoft Azure that is designed to help organizations monitor
 * and analyze metrics and time-series data from various sources. It is particularly focused on aiding in the
 *  detection of anomalies, trends, and patterns within this data, which can be invaluable for improving operational
 * efficiency, identifying issues early, and making data-driven decisions.</p>
 *
 * <p>Here are some key features and capabilities of Azure Metrics Advisor:</p>
 *
 * <ul>
 * <li>Anomaly Detection: Azure Metrics Advisor employs machine learning algorithms to automatically identify
 * anomalies in time-series data. It can differentiate between normal variations and unusual patterns, helping
 * organizations detect issues or opportunities for improvement.</li>
 *
 * <li>Time-Series Data Ingestion: The service allows you to ingest time-series data from various sources, including
 * Azure Monitor, Application Insights, IoT Hub, and custom data sources. This flexibility enables you to monitor a wide
 * range of metrics.</li>
 *
 * <li>Data Exploration: Users can explore their data, view historical trends, and gain insights into the behavior of
 * various metrics over time. This can be useful for identifying seasonal patterns and understanding the normal
 * behavior of your systems.</li>
 *
 * <li>Integration with Azure Services: Azure Metrics Advisor integrates with other Azure services, such as Azure
 * Monitor and Azure Data Explorer, making it part of a broader ecosystem for monitoring and analytics.</li>
 *
 * <li>Alerts and Notifications: You can set up alerts and notifications based on detected anomalies or specific
 * thresholds, ensuring that you are informed promptly when issues arise.</li>
 * </ul>
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
 * <!-- src_embed com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricSeriesData#String-List-OffsetDateTime-OffsetDateTime -->
 * <pre>
 * final OffsetDateTime startTime = OffsetDateTime.parse&#40;&quot;2020-09-09T00:00:00Z&quot;&#41;;
 * final OffsetDateTime endTime = OffsetDateTime.parse&#40;&quot;2020-09-09T12:00:00Z&quot;&#41;;
 *
 * metricsAdvisorClient.listMetricSeriesData&#40;&quot;metricId&quot;,
 *     Arrays.asList&#40;new DimensionKey&#40;new HashMap&lt;String, String&gt;&#40;&#41; &#123;&#123;
 *             put&#40;&quot;Dim1&quot;, &quot;value1&quot;&#41;;
 *         &#125;&#125;&#41;&#41;, startTime, endTime&#41;
 *     .forEach&#40;metricSeriesData -&gt; &#123;
 *         System.out.println&#40;&quot;List of data points for this series:&quot;&#41;;
 *         System.out.println&#40;metricSeriesData.getMetricValues&#40;&#41;&#41;;
 *         System.out.println&#40;&quot;Timestamps of the data related to this time series:&quot;&#41;;
 *         System.out.println&#40;metricSeriesData.getTimestamps&#40;&#41;&#41;;
 *         System.out.printf&#40;&quot;Series Key:&quot;&#41;;
 *         System.out.println&#40;metricSeriesData.getSeriesKey&#40;&#41;.asMap&#40;&#41;&#41;;
 *     &#125;&#41;;
 * </pre>
 * <!-- end com.azure.ai.metricsadvisor.MetricsAdvisorClient.listMetricSeriesData#String-List-OffsetDateTime-OffsetDateTime -->
 *
 * <p><strong>Note:</strong> For asynchronous sample, refer to {@link com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient#listMetricSeriesData(java.lang.String, java.util.List, java.time.OffsetDateTime, java.time.OffsetDateTime) AsyncListMetricSeriesData} API.</p>
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
 * final MetricWholeSeriesDetectionCondition wholeSeriesCondition = new MetricWholeSeriesDetectionCondition&#40;&#41;
 *     .setConditionOperator&#40;DetectionConditionOperator.OR&#41;
 *     .setSmartDetectionCondition&#40;new SmartDetectionCondition&#40;
 *         50,
 *         AnomalyDetectorDirection.BOTH,
 *         new SuppressCondition&#40;50, 50&#41;&#41;&#41;
 *     .setHardThresholdCondition&#40;new HardThresholdCondition&#40;
 *         AnomalyDetectorDirection.BOTH,
 *         new SuppressCondition&#40;5, 5&#41;&#41;
 *         .setLowerBound&#40;0.0&#41;
 *         .setUpperBound&#40;100.0&#41;&#41;
 *     .setChangeThresholdCondition&#40;new ChangeThresholdCondition&#40;
 *         50,
 *         30,
 *         true,
 *         AnomalyDetectorDirection.BOTH,
 *         new SuppressCondition&#40;2, 2&#41;&#41;&#41;;
 *
 * final String detectionConfigName = &quot;my_detection_config&quot;;
 * final String detectionConfigDescription = &quot;anomaly detection config for metric&quot;;
 * final AnomalyDetectionConfiguration detectionConfig
 *     = new AnomalyDetectionConfiguration&#40;detectionConfigName&#41;
 *     .setDescription&#40;detectionConfigDescription&#41;
 *     .setWholeSeriesDetectionCondition&#40;wholeSeriesCondition&#41;;
 *
 * final String metricId = &quot;0b836da8-10e6-46cd-8f4f-28262e113a62&quot;;
 * Response&lt;AnomalyDetectionConfiguration&gt; response = metricsAdvisorAdminClient
 *     .createDetectionConfigWithResponse&#40;metricId, detectionConfig, Context.NONE&#41;;
 * System.out.printf&#40;&quot;Response statusCode: %d%n&quot;, response.getStatusCode&#40;&#41;&#41;;
 * AnomalyDetectionConfiguration createdDetectionConfig = response.getValue&#40;&#41;;
 * System.out.printf&#40;&quot;Detection config Id: %s%n&quot;, createdDetectionConfig.getId&#40;&#41;&#41;;
 * System.out.printf&#40;&quot;Name: %s%n&quot;, createdDetectionConfig.getName&#40;&#41;&#41;;
 * System.out.printf&#40;&quot;Description: %s%n&quot;, createdDetectionConfig.getDescription&#40;&#41;&#41;;
 * System.out.printf&#40;&quot;MetricId: %s%n&quot;, createdDetectionConfig.getMetricId&#40;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createDetectionConfigWithResponse#String-AnomalyDetectionConfiguration-Context -->
 *
 * <p><strong>Note:</strong> For asynchronous sample, refer to {@link com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient#createDetectionConfigWithResponse(java.lang.String, com.azure.ai.metricsadvisor.administration.models.AnomalyDetectionConfiguration) AsyncCreateDetectionConfigWithResponse} API.</p>
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
 * <p><strong>Note:</strong> For asynchronous sample, refer to {@link com.azure.ai.metricsadvisor.MetricsAdvisorClient#listIncidentRootCauses(java.lang.String, java.lang.String) AsyncListIncidentRootCauses} API.</p>
 *
 * @see com.azure.ai.metricsadvisor.MetricsAdvisorClient
 * @see com.azure.ai.metricsadvisor.MetricsAdvisorAsyncClient
 * @see com.azure.ai.metricsadvisor.MetricsAdvisorClientBuilder
 */
package com.azure.ai.metricsadvisor;
