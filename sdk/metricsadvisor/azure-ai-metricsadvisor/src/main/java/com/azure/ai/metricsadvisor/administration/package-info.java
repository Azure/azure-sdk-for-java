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
 *     <li>Connecting your own time series data</li>
 *     <li>Fine tuning the anomaly detection configuration</li>
 *     <li>Configuring alerts for detected anomalies</li>
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
 * {@link com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient} is the synchronous service client and
 * {@link com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient} is the asynchronous service client.
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
 * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.instantiation.withAAD -->
 * <pre>
 * MetricsAdvisorAdministrationAsyncClient metricsAdvisorAdminAsyncClient =
 *     new MetricsAdvisorAdministrationClientBuilder&#40;&#41;
 *         .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;
 *         .endpoint&#40;&quot;&#123;endpoint&#125;&quot;&#41;
 *         .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.instantiation.withAAD  -->
 *
 * <p>Further, see the code sample below to use
 * {@link com.azure.ai.metricsadvisor.models.MetricsAdvisorKeyCredential MetricsAdvisorKeyCredential} for client creation.</p>
 *
 * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.instantiation -->
 * <pre>
 * MetricsAdvisorAdministrationAsyncClient metricsAdvisorAdminAsyncClient =
 *     new MetricsAdvisorAdministrationClientBuilder&#40;&#41;
 *         .credential&#40;new MetricsAdvisorKeyCredential&#40;&quot;&#123;subscription_key&#125;&quot;, &quot;&#123;api_key&#125;&quot;&#41;&#41;
 *         .endpoint&#40;&quot;&#123;endpoint&#125;&quot;&#41;
 *         .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.instantiation  -->
 *
 * <p>Let's take a look at the advisor client scenarios and their respective usage below.</p>
 *
 * <br>
 *
 * <hr>
 * <h2>Onboard your data by connecting to an SQL data source</h2>
 * <p>Metrics Advisor provides connectors for different data sources, such as Azure SQL Database, Azure Data Explorer,
 * and Azure Table Storage.</p>
 * <p><strong>Sample: Onboard a given SQL source of data to Metrics Advisor service</strong></p>
 *
 * <p>The following code sample demonstrates to connect data source to create a data feed using an SQL data source</p>
 *
 * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createDataFeed#DataFeed -->
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
 * <p>
 *
 * <br>
 *
 * <hr>
 *
 * <h2>Subscribe anomalies for notification</h2>
 *
 * <p>After an anomaly is detected by Metrics Advisor, an alert notification will be triggered based on alert settings,
 * using a hook. An alert setting can be used with multiple detection configurations, various parameters are available to customize your alert rule.</p>
 *
 * <p>The following code sample demonstrates how to configure an alert notification for a detected anomaly with multiple detection configurations</p>
 * <p><strong>Sample: Configure alerts and get notifications using a hook.</strong></p>
 *
 ```java readme-sample-createHook
 * <!-- src_embed readme-sample-createHook -->
 * <pre>
 * NotificationHook emailNotificationHook = new EmailNotificationHook&#40;&quot;email Hook&quot;&#41;
 *     .setDescription&#40;&quot;my email Hook&quot;&#41;
 *     .setEmailsToAlert&#40;Collections.singletonList&#40;&quot;alertme&#64;alertme.com&quot;&#41;&#41;
 *     .setExternalLink&#40;&quot;https:&#47;&#47;adwiki.azurewebsites.net&#47;articles&#47;howto&#47;alerts&#47;create-hooks.html&quot;&#41;;
 *
 * final NotificationHook notificationHook = metricsAdvisorAdminClient.createHook&#40;emailNotificationHook&#41;;
 * EmailNotificationHook createdEmailHook = &#40;EmailNotificationHook&#41; notificationHook;
 * System.out.printf&#40;&quot;Email Hook Id: %s%n&quot;, createdEmailHook.getId&#40;&#41;&#41;;
 * System.out.printf&#40;&quot;Email Hook name: %s%n&quot;, createdEmailHook.getName&#40;&#41;&#41;;
 * System.out.printf&#40;&quot;Email Hook description: %s%n&quot;, createdEmailHook.getDescription&#40;&#41;&#41;;
 * System.out.printf&#40;&quot;Email Hook external Link: %s%n&quot;, createdEmailHook.getExternalLink&#40;&#41;&#41;;
 * System.out.printf&#40;&quot;Email Hook emails to alert: %s%n&quot;,
 *     String.join&#40;&quot;,&quot;, createdEmailHook.getEmailsToAlert&#40;&#41;&#41;&#41;;
 * </pre>
 * <!-- end readme-sample-createHook -->
 *
 * @see com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient
 * @see com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient
 * @see com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClientBuilder
 */
package com.azure.ai.metricsadvisor.administration;
