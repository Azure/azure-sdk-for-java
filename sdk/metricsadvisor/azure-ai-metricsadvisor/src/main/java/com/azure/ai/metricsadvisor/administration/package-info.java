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
 * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.instantiation.withAAD -->
 * <pre>
 * MetricsAdvisorAdministrationClient metricsAdvisorAdminClient =
 *     new MetricsAdvisorAdministrationClientBuilder&#40;&#41;
 *         .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;
 *         .endpoint&#40;&quot;&#123;endpoint&#125;&quot;&#41;
 *         .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.instantiation.withAAD  -->
 *
 * <p>Further, see the code sample below to use
 * {@link com.azure.ai.metricsadvisor.models.MetricsAdvisorKeyCredential MetricsAdvisorKeyCredential} for client creation.</p>
 *
 * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.instantiation -->
 * <pre>
 * MetricsAdvisorAdministrationClient metricsAdvisorAdminClient =
 *     new MetricsAdvisorAdministrationClientBuilder&#40;&#41;
 *         .credential&#40;new MetricsAdvisorKeyCredential&#40;&quot;&#123;subscription_key&#125;&quot;, &quot;&#123;api_key&#125;&quot;&#41;&#41;
 *         .endpoint&#40;&quot;&#123;endpoint&#125;&quot;&#41;
 *         .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.instantiation  -->
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
 * <pre>
 * DataFeed dataFeed = new DataFeed&#40;&#41;
 *     .setName&#40;&quot;dataFeedName&quot;&#41;
 *     .setSource&#40;new MySqlDataFeedSource&#40;&quot;conn-string&quot;, &quot;query&quot;&#41;&#41;
 *     .setGranularity&#40;new DataFeedGranularity&#40;&#41;.setGranularityType&#40;DataFeedGranularityType.DAILY&#41;&#41;
 *     .setSchema&#40;new DataFeedSchema&#40;
 *         Arrays.asList&#40;
 *             new DataFeedMetric&#40;&quot;cost&quot;&#41;,
 *             new DataFeedMetric&#40;&quot;revenue&quot;&#41;
 *         &#41;&#41;.setDimensions&#40;
 *         Arrays.asList&#40;
 *             new DataFeedDimension&#40;&quot;city&quot;&#41;,
 *             new DataFeedDimension&#40;&quot;category&quot;&#41;
 *         &#41;&#41;
 *     &#41;
 *     .setIngestionSettings&#40;new DataFeedIngestionSettings&#40;OffsetDateTime.parse&#40;&quot;2020-01-01T00:00:00Z&quot;&#41;&#41;&#41;
 *     .setOptions&#40;new DataFeedOptions&#40;&#41;
 *         .setDescription&#40;&quot;data feed description&quot;&#41;
 *         .setRollupSettings&#40;new DataFeedRollupSettings&#40;&#41;
 *             .setRollupType&#40;DataFeedRollupType.AUTO_ROLLUP&#41;&#41;&#41;;
 *
 * DataFeed createdDataFeed = metricsAdvisorAdminClient.createDataFeed&#40;dataFeed&#41;;
 *
 * System.out.printf&#40;&quot;Data feed Id: %s%n&quot;, createdDataFeed.getId&#40;&#41;&#41;;
 * System.out.printf&#40;&quot;Data feed description: %s%n&quot;, createdDataFeed.getOptions&#40;&#41;.getDescription&#40;&#41;&#41;;
 * System.out.printf&#40;&quot;Data feed source type: %s%n&quot;, createdDataFeed.getSourceType&#40;&#41;&#41;;
 * System.out.printf&#40;&quot;Data feed creator: %s%n&quot;, createdDataFeed.getCreator&#40;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createDataFeed#DataFeed -->
 * <p><strong>Note:</strong> For asynchronous sample, refer to {@link com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient#createDataFeed(com.azure.ai.metricsadvisor.administration.models.DataFeed) AsyncCreateDataFeed} API.</p>
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
 * <p><strong>Note:</strong> For asynchronous sample, refer to {@link com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient#createHook(com.azure.ai.metricsadvisor.administration.models.NotificationHook) AsyncCreateHook} API.</p>
 *
 * @see com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient
 * @see com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient
 * @see com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClientBuilder
 */
package com.azure.ai.metricsadvisor.administration;
