// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration;

import com.azure.ai.metricsadvisor.administration.models.AnomalyAlertConfiguration;
import com.azure.ai.metricsadvisor.administration.models.AnomalyDetectionConfiguration;
import com.azure.ai.metricsadvisor.administration.models.DataFeed;
import com.azure.ai.metricsadvisor.administration.models.DataFeedGranularity;
import com.azure.ai.metricsadvisor.administration.models.DataFeedIngestionProgress;
import com.azure.ai.metricsadvisor.administration.models.DataFeedIngestionSettings;
import com.azure.ai.metricsadvisor.administration.models.DataFeedIngestionStatus;
import com.azure.ai.metricsadvisor.administration.models.DataFeedMissingDataPointFillSettings;
import com.azure.ai.metricsadvisor.administration.models.DataFeedMissingDataPointFillType;
import com.azure.ai.metricsadvisor.administration.models.DataFeedOptions;
import com.azure.ai.metricsadvisor.administration.models.DataFeedRollupSettings;
import com.azure.ai.metricsadvisor.administration.models.DataFeedSchema;
import com.azure.ai.metricsadvisor.administration.models.DataSourceCredentialEntity;
import com.azure.ai.metricsadvisor.administration.models.ListAnomalyAlertConfigsOptions;
import com.azure.ai.metricsadvisor.administration.models.ListCredentialEntityOptions;
import com.azure.ai.metricsadvisor.administration.models.ListDataFeedFilter;
import com.azure.ai.metricsadvisor.administration.models.ListDataFeedIngestionOptions;
import com.azure.ai.metricsadvisor.administration.models.ListDataFeedOptions;
import com.azure.ai.metricsadvisor.administration.models.ListDetectionConfigsOptions;
import com.azure.ai.metricsadvisor.administration.models.ListHookOptions;
import com.azure.ai.metricsadvisor.administration.models.NotificationHook;
import com.azure.ai.metricsadvisor.implementation.MetricsAdvisorImpl;
import com.azure.ai.metricsadvisor.implementation.models.AnomalyAlertingConfiguration;
import com.azure.ai.metricsadvisor.implementation.models.AnomalyAlertingConfigurationPatch;
import com.azure.ai.metricsadvisor.implementation.models.AnomalyDetectionConfigurationPatch;
import com.azure.ai.metricsadvisor.implementation.models.CreateAnomalyAlertingConfigurationResponse;
import com.azure.ai.metricsadvisor.implementation.models.CreateAnomalyDetectionConfigurationResponse;
import com.azure.ai.metricsadvisor.implementation.models.CreateCredentialResponse;
import com.azure.ai.metricsadvisor.implementation.models.CreateDataFeedResponse;
import com.azure.ai.metricsadvisor.implementation.models.CreateHookResponse;
import com.azure.ai.metricsadvisor.implementation.models.DataFeedDetail;
import com.azure.ai.metricsadvisor.implementation.models.DataSourceCredential;
import com.azure.ai.metricsadvisor.implementation.models.DataSourceCredentialPatch;
import com.azure.ai.metricsadvisor.implementation.models.DataSourceType;
import com.azure.ai.metricsadvisor.implementation.models.EntityStatus;
import com.azure.ai.metricsadvisor.implementation.models.FillMissingPointType;
import com.azure.ai.metricsadvisor.implementation.models.Granularity;
import com.azure.ai.metricsadvisor.implementation.models.HookInfo;
import com.azure.ai.metricsadvisor.implementation.models.IngestionProgressResetOptions;
import com.azure.ai.metricsadvisor.implementation.models.IngestionStatusQueryOptions;
import com.azure.ai.metricsadvisor.implementation.models.NeedRollupEnum;
import com.azure.ai.metricsadvisor.implementation.models.RollUpMethod;
import com.azure.ai.metricsadvisor.implementation.models.ViewMode;
import com.azure.ai.metricsadvisor.implementation.util.AlertConfigurationTransforms;
import com.azure.ai.metricsadvisor.implementation.util.DataFeedTransforms;
import com.azure.ai.metricsadvisor.implementation.util.DataSourceCredentialEntityTransforms;
import com.azure.ai.metricsadvisor.implementation.util.DetectionConfigurationTransforms;
import com.azure.ai.metricsadvisor.implementation.util.HookTransforms;
import com.azure.ai.metricsadvisor.implementation.util.Utility;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.azure.ai.metricsadvisor.administration.models.DataFeedGranularityType.CUSTOM;
import static com.azure.ai.metricsadvisor.implementation.util.Utility.parseOperationId;
import static com.azure.ai.metricsadvisor.implementation.util.Utility.toDataFeedIngestionProgress;
import static com.azure.ai.metricsadvisor.implementation.util.Utility.toDataFeedIngestionStatus;

/**
 * <p>This class provides a synchronous client to connect to the Metrics Advisor Azure Cognitive Service.</p>
 * <p>This client provides synchronous methods to perform:</p>
 * <ol>
 *     <li>Connect to a variety of data sources, Metrics Advisor can connect to, and ingest multi-dimensional metric data
 *     from many data stores, including: SQL Server, Azure Blob Storage, and MongoDB. Use
 *     {@link com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient#createDataFeed(DataFeed)}
 *     method to add your respective data source.</li>
 *     <li>Customize anomaly detection configuration to detect anomalies for your needs using the
 *     {@link com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient#createDetectionConfig(String, AnomalyDetectionConfiguration)}
 *     method.</li>
 *     <li>Add real-time notification through multiple channels. Configure hooks for multiple alerting and detection
 *     configuration using the
 *     {@link com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient#createHook(NotificationHook)}
 *     method./li>
 * </ol>
 *
 * <p>Service clients are the point of interaction for developers to use Azure Metrics Advisor.
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
 * <p><strong>Sample: Construct a MetricsAdvisorAdministrationClient with DefaultAzureCredential</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a
 * {@link com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient}, using the `DefaultAzureCredentialBuilder` to configure it.</p>
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
 * @see com.azure.ai.metricsadvisor
 * @see MetricsAdvisorAdministrationAsyncClient
 * @see MetricsAdvisorAdministrationClientBuilder
 */
@ServiceClient(builder = MetricsAdvisorAdministrationClientBuilder.class)
public final class MetricsAdvisorAdministrationClient {
    private final ClientLogger logger = new ClientLogger(MetricsAdvisorAdministrationClient.class);
    private final MetricsAdvisorImpl service;

    /**
     * Create a {@link MetricsAdvisorAdministrationClient client} that sends requests to the Metrics Advisor service's
     * endpoint.
     * Each service call goes through the {@link MetricsAdvisorAdministrationClientBuilder#pipeline http pipeline}.
     *
     * @param service The proxy service used to perform REST calls.
     */
    MetricsAdvisorAdministrationClient(MetricsAdvisorImpl service) {
        this.service = service;
    }

    /**
     * Create a new data feed.
     *
     * <p><strong>Code sample</strong></p>
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
     *
     * @param dataFeed The data feed to be created.
     * @return The created data feed.
     * @throws NullPointerException If {@code dataFeed}, {@code dataFeedName}, {@code dataFeedSource}, {@code metrics},
     * {@code granularityType} or {@code ingestionStartTime} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DataFeed createDataFeed(DataFeed dataFeed) {
        return createDataFeedWithResponse(dataFeed, Context.NONE).getValue();
    }

    /**
     * Create a new data feed with REST response.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createDataFeedWithResponse#DataFeed-Context -->
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
     * final Response&lt;DataFeed&gt; createdDataFeedResponse =
     *     metricsAdvisorAdminClient.createDataFeedWithResponse&#40;dataFeed, Context.NONE&#41;;
     *
     * System.out.printf&#40;&quot;Data feed create operation status: %s%n&quot;, createdDataFeedResponse.getStatusCode&#40;&#41;&#41;;
     * DataFeed createdDataFeed = createdDataFeedResponse.getValue&#40;&#41;;
     * System.out.printf&#40;&quot;Data feed Id: %s%n&quot;, createdDataFeed.getId&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Data feed description: %s%n&quot;, createdDataFeed.getOptions&#40;&#41;.getDescription&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Data feed source type: %s%n&quot;, createdDataFeed.getSourceType&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Data feed creator: %s%n&quot;, createdDataFeed.getCreator&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createDataFeedWithResponse#DataFeed-Context -->
     *
     * @param dataFeed The data feed to be created.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link Response} containing the created data feed.
     * @throws NullPointerException If {@code dataFeed}, {@code dataFeedName}, {@code dataFeedSource}, {@code metrics},
     * {@code granularityType} or {@code ingestionStartTime} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DataFeed> createDataFeedWithResponse(DataFeed dataFeed, Context context) {
        return createDataFeedWithResponseSync(dataFeed, context);
    }

    Response<DataFeed> createDataFeedWithResponseSync(DataFeed dataFeed, Context context) {
        Objects.requireNonNull(dataFeed, "'dataFeed' is required and cannot be null.");
        Objects.requireNonNull(dataFeed.getSource(), "'dataFeedSource' is required and cannot be null.");
        Objects.requireNonNull(dataFeed.getName(), "'dataFeedName' cannot be null or empty.");
        final DataFeedSchema dataFeedSchema = dataFeed.getSchema();
        final DataFeedGranularity dataFeedGranularity = dataFeed.getGranularity();
        final DataFeedIngestionSettings dataFeedIngestionSettings = dataFeed.getIngestionSettings();

        if (dataFeedSchema == null) {
            throw logger
                .logExceptionAsError(new NullPointerException("'dataFeedSchema.metrics' cannot be null or empty."));
        } else {
            Objects.requireNonNull(dataFeedSchema.getMetrics(), "'dataFeedSchema.metrics' cannot be null or empty.");
        }

        if (dataFeedGranularity == null) {
            throw logger.logExceptionAsError(
                new NullPointerException("'dataFeedGranularity.granularityType' is required and cannot be null."));

        } else {
            Objects.requireNonNull(dataFeedGranularity.getGranularityType(),
                "'dataFeedGranularity.granularityType' is required.");
            if (CUSTOM.equals(dataFeedGranularity.getGranularityType())) {
                Objects.requireNonNull(dataFeedGranularity.getCustomGranularityValue(),
                    "'dataFeedGranularity.customGranularityValue' is required when granularity type is CUSTOM");
            }
        }

        if (dataFeedIngestionSettings == null) {
            throw logger.logExceptionAsError(new NullPointerException(
                "'dataFeedIngestionSettings.ingestionStartTime' is required and cannot be null."));
        } else {
            Objects.requireNonNull(dataFeedIngestionSettings.getIngestionStartTime(),
                "'dataFeedIngestionSettings.ingestionStartTime' is required and cannot be null.");
        }

        final DataFeedOptions finalDataFeedOptions
            = dataFeed.getOptions() == null ? new DataFeedOptions() : dataFeed.getOptions();
        final DataFeedRollupSettings dataFeedRollupSettings = finalDataFeedOptions.getRollupSettings() == null
            ? new DataFeedRollupSettings()
            : finalDataFeedOptions.getRollupSettings();
        final DataFeedMissingDataPointFillSettings dataFeedMissingDataPointFillSettings
            = finalDataFeedOptions.getMissingDataPointFillSettings() == null
                ? new DataFeedMissingDataPointFillSettings()
                : finalDataFeedOptions.getMissingDataPointFillSettings();

        CreateDataFeedResponse createDataFeedResponse = service.createDataFeedWithResponse(DataFeedTransforms
            .toDataFeedDetailSource(dataFeed.getSource())
            .setDataFeedName(dataFeed.getName())
            .setDataFeedDescription(finalDataFeedOptions.getDescription())
            .setGranularityName(Granularity.fromString(dataFeedGranularity.getGranularityType() == null
                ? null
                : dataFeedGranularity.getGranularityType().toString()))
            .setGranularityAmount(dataFeedGranularity.getCustomGranularityValue())
            .setDimension(DataFeedTransforms.toInnerDimensionsListForCreate(dataFeedSchema.getDimensions()))
            .setMetrics(DataFeedTransforms.toInnerMetricsListForCreate(dataFeedSchema.getMetrics()))
            .setTimestampColumn(dataFeedSchema.getTimestampColumn())
            .setDataStartFrom(dataFeedIngestionSettings.getIngestionStartTime())
            .setStartOffsetInSeconds(dataFeedIngestionSettings.getIngestionStartOffset() == null
                ? null
                : dataFeedIngestionSettings.getIngestionStartOffset().getSeconds())
            .setMaxConcurrency(dataFeedIngestionSettings.getDataSourceRequestConcurrency())
            .setStopRetryAfterInSeconds(dataFeedIngestionSettings.getStopRetryAfter() == null
                ? null
                : dataFeedIngestionSettings.getStopRetryAfter().getSeconds())
            .setMinRetryIntervalInSeconds(dataFeedIngestionSettings.getIngestionRetryDelay() == null
                ? null
                : dataFeedIngestionSettings.getIngestionRetryDelay().getSeconds())
            .setRollUpColumns(dataFeedRollupSettings.getAutoRollupGroupByColumnNames())
            .setRollUpMethod(RollUpMethod.fromString(dataFeedRollupSettings.getDataFeedAutoRollUpMethod() == null
                ? null
                : dataFeedRollupSettings.getDataFeedAutoRollUpMethod().toString()))
            .setNeedRollup(NeedRollupEnum.fromString(dataFeedRollupSettings.getRollupType() == null
                ? null
                : dataFeedRollupSettings.getRollupType().toString()))
            .setAllUpIdentification(dataFeedRollupSettings.getRollupIdentificationValue())
            .setFillMissingPointType(
                FillMissingPointType.fromString(dataFeedMissingDataPointFillSettings.getFillType() == null
                    ? null
                    : dataFeedMissingDataPointFillSettings.getFillType().toString()))
            .setFillMissingPointValue(dataFeedMissingDataPointFillSettings.getCustomFillValue())
            .setViewMode(ViewMode.fromString(
                finalDataFeedOptions.getAccessMode() == null ? null : finalDataFeedOptions.getAccessMode().toString()))
            .setViewers(finalDataFeedOptions.getViewers())
            .setAdmins(finalDataFeedOptions.getAdmins())
            .setActionLinkTemplate(finalDataFeedOptions.getActionLinkTemplate()), context);

        final String dataFeedId = parseOperationId(createDataFeedResponse.getDeserializedHeaders().getLocation());
        return getDataFeedWithResponse(dataFeedId, context);
    }

    /**
     * Get a data feed by its id.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getDataFeed#String -->
     * <pre>
     * final String dataFeedId = &quot;r47053f1-9080-09lo-bacf-8dccf2e86f&quot;;
     * DataFeed dataFeed = metricsAdvisorAdminClient.getDataFeed&#40;dataFeedId&#41;;
     * System.out.printf&#40;&quot;Data feed Id: %s%n&quot;, dataFeed.getId&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Data feed description: %s%n&quot;, dataFeed.getOptions&#40;&#41;.getDescription&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Data feed source type: %s%n&quot;, dataFeed.getSourceType&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Data feed creator: %s%n&quot;, dataFeed.getCreator&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getDataFeed#String -->
     *
     * @param dataFeedId The data feed unique id.
     *
     * @return The data feed for the provided id.
     * @throws IllegalArgumentException If {@code dataFeedId} does not conform to the UUID format specification.
     * @throws NullPointerException thrown if the {@code dataFeedId} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DataFeed getDataFeed(String dataFeedId) {
        return getDataFeedWithResponse(dataFeedId, Context.NONE).getValue();
    }

    /**
     * Get a data feed by its id with REST response.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getDataFeedWithResponse#String-Context -->
     * <pre>
     * final String dataFeedId = &quot;r47053f1-9080-09lo-bacf-8dccf2e86f&quot;;
     * final Response&lt;DataFeed&gt; dataFeedResponse =
     *     metricsAdvisorAdminClient.getDataFeedWithResponse&#40;dataFeedId, Context.NONE&#41;;
     *
     * System.out.printf&#40;&quot;Data feed get operation status: %s%n&quot;, dataFeedResponse.getStatusCode&#40;&#41;&#41;;
     * DataFeed dataFeed = dataFeedResponse.getValue&#40;&#41;;
     * System.out.printf&#40;&quot;Data feed Id: %s%n&quot;, dataFeed.getId&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Data feed description: %s%n&quot;, dataFeed.getOptions&#40;&#41;.getDescription&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Data feed source type: %s%n&quot;, dataFeed.getSourceType&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Data feed creator: %s%n&quot;, dataFeed.getCreator&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getDataFeedWithResponse#String-Context -->
     *
     * @param dataFeedId The data feed unique id.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return The data feed for the provided id.
     * @throws IllegalArgumentException If {@code dataFeedId} does not conform to the UUID format specification.
     * @throws NullPointerException thrown if the {@code dataFeedId} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DataFeed> getDataFeedWithResponse(String dataFeedId, Context context) {
        return getDataFeedWithResponseSync(dataFeedId, context);
    }

    Response<DataFeed> getDataFeedWithResponseSync(String dataFeedId, Context context) {
        Objects.requireNonNull(dataFeedId, "'dataFeedId' cannot be null.");

        Response<DataFeedDetail> response = service.getDataFeedByIdWithResponse(UUID.fromString(dataFeedId), context);
        return new SimpleResponse<>(response, DataFeedTransforms.fromInner(response.getValue()));
    }

    /**
     * Update a data feed.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateDataFeed#DataFeed -->
     * <pre>
     * final String dataFeedId = &quot;r47053f1-9080-09lo-bacf-8dccf2e86f&quot;;
     * DataFeed existingDataFeed = metricsAdvisorAdminClient.getDataFeed&#40;dataFeedId&#41;;
     * final DataFeed updatedDataFeed = metricsAdvisorAdminClient.updateDataFeed&#40;
     *     existingDataFeed.setOptions&#40;new DataFeedOptions&#40;&#41;.setDescription&#40;&quot;set updated description&quot;&#41;&#41;&#41;;
     *
     * System.out.printf&#40;&quot;Data feed Id: %s%n&quot;, updatedDataFeed.getId&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Data feed updated description: %s%n&quot;, updatedDataFeed.getOptions&#40;&#41;.getDescription&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateDataFeed#DataFeed -->
     *
     * @param dataFeed the data feed that needs to be updated.
     *
     * @return the updated data feed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DataFeed updateDataFeed(DataFeed dataFeed) {
        return updateDataFeedWithResponse(dataFeed, Context.NONE).getValue();
    }

    /**
     * Update a data feed with REST response.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateDataFeedWithResponse#DataFeed-Context -->
     * <pre>
     * DataFeed existingDataFeed = new DataFeed&#40;&#41;;
     * final Response&lt;DataFeed&gt; updateDataFeedWithResponse =
     *     metricsAdvisorAdminClient.updateDataFeedWithResponse&#40;
     *         existingDataFeed.setOptions&#40;new DataFeedOptions&#40;&#41;.setDescription&#40;&quot;set updated description&quot;&#41;&#41;,
     *         Context.NONE&#41;;
     *
     * System.out.printf&#40;&quot;Data feed update operation status: %s%n&quot;, updateDataFeedWithResponse.getStatusCode&#40;&#41;&#41;;
     * DataFeed dataFeed = updateDataFeedWithResponse.getValue&#40;&#41;;
     * System.out.printf&#40;&quot;Data feed Id: %s%n&quot;, dataFeed.getId&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Data feed updated description: %s%n&quot;, dataFeed.getOptions&#40;&#41;.getDescription&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateDataFeedWithResponse#DataFeed-Context -->
     *
     * @param dataFeed the data feed that needs to be updated.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return the updated data feed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DataFeed> updateDataFeedWithResponse(DataFeed dataFeed, Context context) {
        return updateDataFeedWithResponseSync(dataFeed, context);
    }

    Response<DataFeed> updateDataFeedWithResponseSync(DataFeed dataFeed, Context context) {
        final DataFeedIngestionSettings dataFeedIngestionSettings = dataFeed.getIngestionSettings();
        final DataFeedOptions dataFeedOptions
            = dataFeed.getOptions() == null ? new DataFeedOptions() : dataFeed.getOptions();
        final DataFeedRollupSettings dataFeedRollupSettings = dataFeedOptions.getRollupSettings() == null
            ? new DataFeedRollupSettings()
            : dataFeedOptions.getRollupSettings();
        final DataFeedMissingDataPointFillSettings dataFeedMissingDataPointFillSettings
            = dataFeedOptions.getMissingDataPointFillSettings() == null
                ? new DataFeedMissingDataPointFillSettings()
                : dataFeedOptions.getMissingDataPointFillSettings();

        service.updateDataFeedWithResponse(UUID.fromString(dataFeed.getId()),
            DataFeedTransforms.toInnerForUpdate(dataFeed.getSource())
                .setDataFeedName(dataFeed.getName())
                .setDataFeedDescription(dataFeedOptions.getDescription())
                .setTimestampColumn(dataFeed.getSchema() == null ? null : dataFeed.getSchema().getTimestampColumn())
                .setDataStartFrom(dataFeed.getIngestionSettings().getIngestionStartTime())
                .setStartOffsetInSeconds(dataFeedIngestionSettings.getIngestionStartOffset() == null
                    ? null
                    : dataFeedIngestionSettings.getIngestionStartOffset().getSeconds())
                .setMaxConcurrency(dataFeedIngestionSettings.getDataSourceRequestConcurrency())
                .setStopRetryAfterInSeconds(dataFeedIngestionSettings.getStopRetryAfter() == null
                    ? null
                    : dataFeedIngestionSettings.getStopRetryAfter().getSeconds())
                .setMinRetryIntervalInSeconds(dataFeedIngestionSettings.getIngestionRetryDelay() == null
                    ? null
                    : dataFeedIngestionSettings.getIngestionRetryDelay().getSeconds())
                .setNeedRollup(dataFeedRollupSettings.getRollupType() != null
                    ? NeedRollupEnum.fromString(dataFeedRollupSettings.getRollupType().toString())
                    : null)
                .setRollUpColumns(dataFeedRollupSettings.getAutoRollupGroupByColumnNames())
                .setRollUpMethod(dataFeedRollupSettings.getDataFeedAutoRollUpMethod() != null
                    ? RollUpMethod.fromString(dataFeedRollupSettings.getDataFeedAutoRollUpMethod().toString())
                    : null)
                .setAllUpIdentification(dataFeedRollupSettings.getRollupIdentificationValue())
                .setFillMissingPointType(dataFeedMissingDataPointFillSettings.getFillType() != null
                    ? FillMissingPointType.fromString(dataFeedMissingDataPointFillSettings.getFillType().toString())
                    : null)
                .setFillMissingPointValue(
                    // For PATCH send 'fill-custom-value' over wire only for 'fill-custom-type'.
                    dataFeedMissingDataPointFillSettings.getFillType() == DataFeedMissingDataPointFillType.CUSTOM_VALUE
                        ? dataFeedMissingDataPointFillSettings.getCustomFillValue()
                        : null)
                .setViewMode(dataFeedOptions.getAccessMode() != null
                    ? ViewMode.fromString(dataFeedOptions.getAccessMode().toString())
                    : null)
                .setViewers(dataFeedOptions.getViewers())
                .setAdmins(dataFeedOptions.getAdmins())
                .setStatus(
                    dataFeed.getStatus() != null ? EntityStatus.fromString(dataFeed.getStatus().toString()) : null)
                .setActionLinkTemplate(dataFeedOptions.getActionLinkTemplate()),
            context);
        return getDataFeedWithResponse(dataFeed.getId(), context);
    }

    /**
     * Delete a data feed.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteDataFeed#String -->
     * <pre>
     * final String dataFeedId = &quot;r47053f1-9080-09lo-bacf-8dccf2e86f&quot;;
     * metricsAdvisorAdminClient.deleteDataFeed&#40;dataFeedId&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteDataFeed#String -->
     *
     * @param dataFeedId The data feed unique id.
     *
     * @throws IllegalArgumentException If {@code dataFeedId} does not conform to the UUID format specification.
     * @throws NullPointerException thrown if the {@code dataFeedId} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteDataFeed(String dataFeedId) {
        deleteDataFeedWithResponse(dataFeedId, Context.NONE);
    }

    /**
     * Delete a data feed with REST response.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteDataFeedWithResponse#String-Context -->
     * <pre>
     * final String dataFeedId = &quot;r47053f1-9080-09lo-bacf-8dccf2e86f&quot;;
     * final Response&lt;Void&gt; response = metricsAdvisorAdminClient
     *     .deleteDataFeedWithResponse&#40;dataFeedId, Context.NONE&#41;;
     * System.out.printf&#40;&quot;Data feed delete operation status : %s%n&quot;, response.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteDataFeedWithResponse#String-Context -->
     *
     * @param dataFeedId The data feed unique id.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return a REST Response.
     * @throws IllegalArgumentException If {@code dataFeedId} does not conform to the UUID format specification.
     * @throws NullPointerException thrown if the {@code dataFeedId} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteDataFeedWithResponse(String dataFeedId, Context context) {
        return deleteDataFeedWithResponseSync(dataFeedId, context);
    }

    Response<Void> deleteDataFeedWithResponseSync(String dataFeedId, Context context) {
        Objects.requireNonNull(dataFeedId, "'dataFeedId' cannot be null.");
        Response<Void> response = service.deleteDataFeedWithResponse(UUID.fromString(dataFeedId), context);
        return new SimpleResponse<>(response, null);
    }

    /**
     * List information of all data feeds on the metrics advisor account.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listDataFeeds -->
     * <pre>
     * metricsAdvisorAdminClient.listDataFeeds&#40;&#41;
     *     .forEach&#40;dataFeed -&gt; &#123;
     *         System.out.printf&#40;&quot;Data feed Id: %s%n&quot;, dataFeed.getId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Data feed description: %s%n&quot;, dataFeed.getOptions&#40;&#41;.getDescription&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Data feed source type: %s%n&quot;, dataFeed.getSourceType&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Data feed creator: %s%n&quot;, dataFeed.getCreator&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listDataFeeds -->
     *
     * @return A {@link PagedIterable} containing information of all the {@link DataFeed data feeds} in the account.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<DataFeed> listDataFeeds() {
        return listDataFeeds(null, Context.NONE);
    }

    /**
     * List information of all data feeds on the metrics advisor account with REST response.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listDataFeeds#ListDataFeedOptions-Context -->
     * <pre>
     * metricsAdvisorAdminClient.listDataFeeds&#40;
     *     new ListDataFeedOptions&#40;&#41;
     *         .setListDataFeedFilter&#40;
     *             new ListDataFeedFilter&#40;&#41;
     *                 .setDataFeedStatus&#40;DataFeedStatus.ACTIVE&#41;
     *                 .setDataFeedGranularityType&#40;DataFeedGranularityType.DAILY&#41;&#41;
     *         .setMaxPageSize&#40;3&#41;, Context.NONE&#41;
     *     .forEach&#40;dataFeed -&gt; &#123;
     *         System.out.printf&#40;&quot;Data feed Id: %s%n&quot;, dataFeed.getId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Data feed description: %s%n&quot;, dataFeed.getOptions&#40;&#41;.getDescription&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Data feed source type: %s%n&quot;, dataFeed.getSourceType&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Data feed creator: %s%n&quot;, dataFeed.getCreator&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Data feed status: %s%n&quot;, dataFeed.getStatus&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Data feed granularity type: %s%n&quot;, dataFeed.getGranularity&#40;&#41;.getGranularityType&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listDataFeeds#ListDataFeedOptions-Context -->
     *
     * @param options The configurable {@link ListDataFeedOptions options} to pass for filtering the output result.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link PagedIterable} containing information of all the {@link DataFeed data feeds} in the account.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<DataFeed> listDataFeeds(ListDataFeedOptions options, Context context) {
        return listDataFeedsSync(options, context);
    }

    PagedIterable<DataFeed> listDataFeedsSync(ListDataFeedOptions options, Context context) {
        return new PagedIterable<>(() -> listDataFeedsSinglePageSync(options, context),
            continuationToken -> listDataFeedsNextPageSync(continuationToken, context));
    }

    private PagedResponse<DataFeed> listDataFeedsSinglePageSync(ListDataFeedOptions options, Context context) {

        options = options != null ? options : new ListDataFeedOptions();
        final ListDataFeedFilter dataFeedFilter
            = options.getListDataFeedFilter() != null ? options.getListDataFeedFilter() : new ListDataFeedFilter();

        PagedResponse<DataFeedDetail> res = service.listDataFeedsSinglePage(dataFeedFilter.getName(),
            dataFeedFilter.getSourceType() != null
                ? DataSourceType.fromString(dataFeedFilter.getSourceType().toString())
                : null,
            dataFeedFilter.getGranularityType() != null
                ? Granularity.fromString(dataFeedFilter.getGranularityType().toString())
                : null,
            dataFeedFilter.getStatus() != null ? EntityStatus.fromString(dataFeedFilter.getStatus().toString()) : null,
            dataFeedFilter.getCreator(), options.getSkip(), options.getMaxPageSize(), context);

        return new PagedResponseBase<>(res.getRequest(), res.getStatusCode(), res.getHeaders(),
            res.getValue().stream().map(DataFeedTransforms::fromInner).collect(Collectors.toList()),
            res.getContinuationToken(), null);
    }

    private PagedResponse<DataFeed> listDataFeedsNextPageSync(String nextPageLink, Context context) {

        if (CoreUtils.isNullOrEmpty(nextPageLink)) {
            return null;
        }
        PagedResponse<DataFeedDetail> res = service.listDataFeedsNextSinglePage(nextPageLink, context);
        return new PagedResponseBase<>(res.getRequest(), res.getStatusCode(), res.getHeaders(),
            res.getValue().stream().map(DataFeedTransforms::fromInner).collect(Collectors.toList()),
            res.getContinuationToken(), null);
    }

    /**
     * Fetch the ingestion status of a data feed.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listDataFeedIngestionStatus#String-ListDataFeedIngestionOptions -->
     * <pre>
     * final String dataFeedId = &quot;4957a2f7-a0f4-4fc0-b8d7-d866c1df0f4c&quot;;
     * final OffsetDateTime startTime = OffsetDateTime.parse&#40;&quot;2020-01-01T00:00:00Z&quot;&#41;;
     * final OffsetDateTime endTime = OffsetDateTime.parse&#40;&quot;2020-09-09T00:00:00Z&quot;&#41;;
     * final ListDataFeedIngestionOptions options = new ListDataFeedIngestionOptions&#40;startTime, endTime&#41;;
     * PagedIterable&lt;DataFeedIngestionStatus&gt; ingestionStatuses
     *     = metricsAdvisorAdminClient.listDataFeedIngestionStatus&#40;dataFeedId, options&#41;;
     *
     * for &#40;DataFeedIngestionStatus ingestionStatus : ingestionStatuses&#41; &#123;
     *     System.out.printf&#40;&quot;Timestamp: %s%n&quot;, ingestionStatus.getTimestamp&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Status: %s%n&quot;, ingestionStatus.getStatus&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Message: %s%n&quot;, ingestionStatus.getMessage&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listDataFeedIngestionStatus#String-ListDataFeedIngestionOptions -->
     *
     * @param dataFeedId The data feed id.
     * @param options The additional parameters.
     * @return The ingestion statuses.
     * @throws IllegalArgumentException If {@code dataFeedId} does not conform to the UUID format specification.
     * @throws NullPointerException If {@code dataFeedId}, {@code options}, {@code options.startTime},
     *     {@code options.endTime}  is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<DataFeedIngestionStatus> listDataFeedIngestionStatus(String dataFeedId,
        ListDataFeedIngestionOptions options) {
        return listDataFeedIngestionStatus(dataFeedId, options, Context.NONE);
    }

    /**
     * Fetch the ingestion status of a data feed.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listDataFeedIngestionStatus#String-ListDataFeedIngestionOptions-Context -->
     * <pre>
     * final String dataFeedId = &quot;4957a2f7-a0f4-4fc0-b8d7-d866c1df0f4c&quot;;
     * final OffsetDateTime startTime = OffsetDateTime.parse&#40;&quot;2020-01-01T00:00:00Z&quot;&#41;;
     * final OffsetDateTime endTime = OffsetDateTime.parse&#40;&quot;2020-09-09T00:00:00Z&quot;&#41;;
     * final ListDataFeedIngestionOptions options = new ListDataFeedIngestionOptions&#40;startTime, endTime&#41;;
     * PagedIterable&lt;DataFeedIngestionStatus&gt; ingestionStatuses
     *     = metricsAdvisorAdminClient.listDataFeedIngestionStatus&#40;dataFeedId, options, Context.NONE&#41;;
     * Stream&lt;PagedResponse&lt;DataFeedIngestionStatus&gt;&gt; ingestionStatusPageStream = ingestionStatuses.streamByPage&#40;&#41;;
     * int[] pageCount = new int[1];
     * ingestionStatusPageStream.forEach&#40;ingestionStatusPage -&gt; &#123;
     *     System.out.printf&#40;&quot;Page: %d%n&quot;, pageCount[0]++&#41;;
     *     for &#40;DataFeedIngestionStatus ingestionStatus : ingestionStatusPage.getElements&#40;&#41;&#41; &#123;
     *         System.out.printf&#40;&quot;Timestamp: %s%n&quot;, ingestionStatus.getTimestamp&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Status: %s%n&quot;, ingestionStatus.getStatus&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Message: %s%n&quot;, ingestionStatus.getMessage&#40;&#41;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listDataFeedIngestionStatus#String-ListDataFeedIngestionOptions-Context -->
     *
     * @param dataFeedId The data feed id.
     * @param options The additional parameters.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The ingestion statuses.
     * @throws IllegalArgumentException If {@code dataFeedId} does not conform to the UUID format specification.
     * @throws NullPointerException If {@code dataFeedId}, {@code options}, {@code options.startTime},
     *     {@code options.endTime}  is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<DataFeedIngestionStatus> listDataFeedIngestionStatus(String dataFeedId,
        ListDataFeedIngestionOptions options, Context context) {
        return listDataFeedIngestionStatusSync(dataFeedId, options, context);
    }

    PagedIterable<DataFeedIngestionStatus> listDataFeedIngestionStatusSync(String dataFeedId,
        ListDataFeedIngestionOptions options, Context context) {
        return new PagedIterable<>(() -> listDataFeedIngestionStatusSinglePageSync(dataFeedId, options, context),
            continuationToken -> listDataFeedIngestionStatusNextPageSync(continuationToken, options, context));
    }

    private PagedResponse<DataFeedIngestionStatus> listDataFeedIngestionStatusSinglePageSync(String dataFeedId,
        ListDataFeedIngestionOptions options, Context context) {
        Objects.requireNonNull(dataFeedId, "'dataFeedId' is required.");
        Objects.requireNonNull(options, "'options' is required.");
        Objects.requireNonNull(options.getStartTime(), "'options.startTime' is required.");
        Objects.requireNonNull(options.getEndTime(), "'options.endTime' is required.");

        IngestionStatusQueryOptions queryOptions
            = new IngestionStatusQueryOptions().setStartTime(options.getStartTime()).setEndTime(options.getEndTime());

        PagedResponse<com.azure.ai.metricsadvisor.implementation.models.DataFeedIngestionStatus> res
            = service.getDataFeedIngestionStatusSinglePage(UUID.fromString(dataFeedId), queryOptions, options.getSkip(),
                options.getMaxPageSize(), context);
        return new PagedResponseBase<>(res.getRequest(), res.getStatusCode(), res.getHeaders(),
            toDataFeedIngestionStatus(res.getValue()), res.getContinuationToken(), null);
    }

    private PagedResponse<DataFeedIngestionStatus> listDataFeedIngestionStatusNextPageSync(String nextPageLink,
        ListDataFeedIngestionOptions options, Context context) {
        if (CoreUtils.isNullOrEmpty(nextPageLink)) {
            return null;
        }

        IngestionStatusQueryOptions queryOptions
            = new IngestionStatusQueryOptions().setStartTime(options.getStartTime()).setEndTime(options.getEndTime());

        PagedResponse<com.azure.ai.metricsadvisor.implementation.models.DataFeedIngestionStatus> res
            = service.getDataFeedIngestionStatusNextSinglePage(nextPageLink, queryOptions, context);
        return new PagedResponseBase<>(res.getRequest(), res.getStatusCode(), res.getHeaders(),
            toDataFeedIngestionStatus(res.getValue()), res.getContinuationToken(), null);
    }

    /**
     * Refresh data ingestion for a period.
     * <p>
     * The data in the data source for the given period will be re-ingested
     * and any ingested data for the same period will be overwritten.
     * </p>
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.refreshDataFeedIngestion#String-OffsetDateTime-OffsetDateTime -->
     * <pre>
     * final String dataFeedId = &quot;4957a2f7-a0f4-4fc0-b8d7-d866c1df0f4c&quot;;
     * final OffsetDateTime startTime = OffsetDateTime.parse&#40;&quot;2020-01-01T00:00:00Z&quot;&#41;;
     * final OffsetDateTime endTime = OffsetDateTime.parse&#40;&quot;2020-03-03T00:00:00Z&quot;&#41;;
     * metricsAdvisorAdminClient.refreshDataFeedIngestion&#40;dataFeedId,
     *     startTime,
     *     endTime&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.refreshDataFeedIngestion#String-OffsetDateTime-OffsetDateTime -->
     *
     * @param dataFeedId The data feed id.
     * @param startTime The start point of the period.
     * @param endTime The end point of the period.
     * @throws IllegalArgumentException If {@code dataFeedId} does not conform to the UUID format specification.
     * @throws NullPointerException If {@code dataFeedId}, {@code startTime}, {@code endTime}  is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void refreshDataFeedIngestion(String dataFeedId, OffsetDateTime startTime, OffsetDateTime endTime) {
        refreshDataFeedIngestionWithResponse(dataFeedId, startTime, endTime, Context.NONE);
    }

    /**
     * Refresh data ingestion for a period.
     * <p>
     * The data in the data source for the given period will be re-ingested
     * and any ingested data for the same period will be overwritten.
     * </p>
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.refreshDataFeedIngestionWithResponse#String-OffsetDateTime-OffsetDateTime-Context -->
     * <pre>
     * final String dataFeedId = &quot;4957a2f7-a0f4-4fc0-b8d7-d866c1df0f4c&quot;;
     * final OffsetDateTime startTime = OffsetDateTime.parse&#40;&quot;2020-01-01T00:00:00Z&quot;&#41;;
     * final OffsetDateTime endTime = OffsetDateTime.parse&#40;&quot;2020-03-03T00:00:00Z&quot;&#41;;
     * Response&lt;Void&gt; response = metricsAdvisorAdminClient.refreshDataFeedIngestionWithResponse&#40;dataFeedId,
     *     startTime,
     *     endTime,
     *     Context.NONE&#41;;
     * System.out.printf&#40;&quot;Response statusCode: %d%n&quot;, response.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.refreshDataFeedIngestionWithResponse#String-OffsetDateTime-OffsetDateTime-Context -->
     *
     * @param dataFeedId The data feed id.
     * @param startTime The start point of the period.
     * @param endTime The end point of the period.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The response.
     * @throws IllegalArgumentException If {@code dataFeedId} does not conform to the UUID format specification.
     * @throws NullPointerException If {@code dataFeedId}, {@code startTime}, {@code endTime}  is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> refreshDataFeedIngestionWithResponse(String dataFeedId, OffsetDateTime startTime,
        OffsetDateTime endTime, Context context) {
        return refreshDataFeedIngestionWithResponseSync(dataFeedId, startTime, endTime, context);
    }

    Response<Void> refreshDataFeedIngestionWithResponseSync(String dataFeedId, OffsetDateTime startTime,
        OffsetDateTime endTime, Context context) {
        Objects.requireNonNull(dataFeedId, "'dataFeedId' is required.");
        Objects.requireNonNull(startTime, "'startTime' is required.");
        Objects.requireNonNull(endTime, "'endTime' is required.");

        return service.resetDataFeedIngestionStatusWithResponse(UUID.fromString(dataFeedId),
            new IngestionProgressResetOptions().setStartTime(startTime).setEndTime(endTime), context);
    }

    /**
     * Retrieve the ingestion progress of a data feed.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getDataFeedIngestionProgress#String -->
     * <pre>
     * final String dataFeedId = &quot;4957a2f7-a0f4-4fc0-b8d7-d866c1df0f4c&quot;;
     * DataFeedIngestionProgress ingestionProgress
     *     = metricsAdvisorAdminClient.getDataFeedIngestionProgress&#40;dataFeedId&#41;;
     * System.out.printf&#40;&quot;Latest active timestamp: %s%n&quot;, ingestionProgress.getLatestActiveTimestamp&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Latest successful timestamp: %s%n&quot;, ingestionProgress.getLatestSuccessTimestamp&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getDataFeedIngestionProgress#String -->
     *
     * @param dataFeedId The data feed id.
     * @return The {@link DataFeedIngestionProgress} of the data feed.
     * @throws IllegalArgumentException If {@code dataFeedId} does not conform to the UUID format specification.
     * @throws NullPointerException thrown if the {@code dataFeedId} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DataFeedIngestionProgress getDataFeedIngestionProgress(String dataFeedId) {
        return getDataFeedIngestionProgressWithResponse(dataFeedId, Context.NONE).getValue();
    }

    /**
     * Retrieve the ingestion progress of a data feed.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getDataFeedIngestionProgressWithResponse#String-Context -->
     * <pre>
     * final String dataFeedId = &quot;4957a2f7-a0f4-4fc0-b8d7-d866c1df0f4c&quot;;
     * Response&lt;DataFeedIngestionProgress&gt; response
     *     = metricsAdvisorAdminClient.getDataFeedIngestionProgressWithResponse&#40;dataFeedId, Context.NONE&#41;;
     * System.out.printf&#40;&quot;Response statusCode: %d%n&quot;, response.getStatusCode&#40;&#41;&#41;;
     * DataFeedIngestionProgress ingestionProgress = response.getValue&#40;&#41;;
     * System.out.printf&#40;&quot;Latest active timestamp: %s%n&quot;, ingestionProgress.getLatestActiveTimestamp&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Latest successful timestamp: %s%n&quot;, ingestionProgress.getLatestSuccessTimestamp&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getDataFeedIngestionProgressWithResponse#String-Context -->
     *
     * @param dataFeedId The data feed id.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} containing {@link DataFeedIngestionProgress} of the data feed.
     * @throws IllegalArgumentException If {@code dataFeedId} does not conform to the UUID format specification.
     * @throws NullPointerException thrown if the {@code dataFeedId} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DataFeedIngestionProgress> getDataFeedIngestionProgressWithResponse(String dataFeedId,
        Context context) {
        return getDataFeedIngestionProgressWithResponseSync(dataFeedId, context);
    }

    Response<DataFeedIngestionProgress> getDataFeedIngestionProgressWithResponseSync(String dataFeedId,
        Context context) {
        Objects.requireNonNull(dataFeedId, "'dataFeedId' is required.");
        Response<com.azure.ai.metricsadvisor.implementation.models.DataFeedIngestionProgress> response
            = service.getIngestionProgressWithResponse(UUID.fromString(dataFeedId), context);
        return new SimpleResponse<>(response, toDataFeedIngestionProgress(response.getValue()));
    }

    /**
     * Create a configuration to detect anomalies in the time series of a metric.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createDetectionConfig#String-AnomalyDetectionConfiguration -->
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
     * AnomalyDetectionConfiguration createdDetectionConfig = metricsAdvisorAdminClient
     *     .createDetectionConfig&#40;metricId, detectionConfig&#41;;
     * System.out.printf&#40;&quot;Detection config Id: %s%n&quot;, createdDetectionConfig.getId&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Name: %s%n&quot;, createdDetectionConfig.getName&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Description: %s%n&quot;, createdDetectionConfig.getDescription&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;MetricId: %s%n&quot;, createdDetectionConfig.getMetricId&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createDetectionConfig#String-AnomalyDetectionConfiguration -->
     *
     * @param metricId The metric id to associate the configuration with.
     * @param detectionConfiguration The anomaly detection configuration.
     * @return The created {@link AnomalyDetectionConfiguration}.
     * @throws IllegalArgumentException If {@code metricId} does not conform to the UUID
     *     format specification, or {@code detectionConfiguration.name} is not set.
     * @throws NullPointerException thrown if the {@code metricId} is null
     *   or {@code detectionConfiguration} is null
     *   or {@code detectionConfiguration.wholeSeriesCondition} is null
     *   or {@code seriesKey} is missing for any {@code MetricSingleSeriesDetectionCondition} in the configuration
     *   or {@code seriesGroupKey} is missing for any {@code MetricSeriesGroupDetectionCondition} in the configuration
     *   or {@code conditionOperator} is missing when multiple nested conditions are set in a
     *      {@code MetricSingleSeriesDetectionCondition} or {@code MetricSeriesGroupDetectionCondition}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public AnomalyDetectionConfiguration createDetectionConfig(String metricId,
        AnomalyDetectionConfiguration detectionConfiguration) {
        return createDetectionConfigWithResponse(metricId, detectionConfiguration, Context.NONE).getValue();
    }

    /**
     * Create a configuration to detect anomalies in the time series of a metric.
     *
     * <p><strong>Code sample</strong></p>
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
     * @param metricId The metric id to associate the configuration with.
     * @param detectionConfiguration The anomaly detection configuration.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} containing the created {@link AnomalyDetectionConfiguration}.
     * @throws IllegalArgumentException If {@code metricId} does not conform to the UUID
     *     format specification, or {@code detectionConfiguration.name} is not set.
     * @throws NullPointerException thrown if the {@code metricId} is null
     *   or {@code detectionConfiguration} is null
     *   or {@code detectionConfiguration.wholeSeriesCondition} is null
     *   or {@code seriesKey} is missing for any {@code MetricSingleSeriesDetectionCondition} in the configuration
     *   or {@code seriesGroupKey} is missing for any {@code MetricSeriesGroupDetectionCondition} in the configuration
     *   or {@code conditionOperator} is missing when multiple nested conditions are set in a
     *      {@code MetricSingleSeriesDetectionCondition} or {@code MetricSeriesGroupDetectionCondition}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<AnomalyDetectionConfiguration> createDetectionConfigWithResponse(String metricId,
        AnomalyDetectionConfiguration detectionConfiguration, Context context) {
        return createDetectionConfigWithResponseSync(metricId, detectionConfiguration, context);
    }

    Response<AnomalyDetectionConfiguration> createDetectionConfigWithResponseSync(String metricId,
        AnomalyDetectionConfiguration detectionConfiguration, Context context) {
        Objects.requireNonNull(metricId, "metricId is required");
        Objects.requireNonNull(detectionConfiguration, "detectionConfiguration is required");

        final com.azure.ai.metricsadvisor.implementation.models.AnomalyDetectionConfiguration innerDetectionConfiguration
            = DetectionConfigurationTransforms.toInnerForCreate(logger, metricId, detectionConfiguration);
        CreateAnomalyDetectionConfigurationResponse response
            = service.createAnomalyDetectionConfigurationWithResponse(innerDetectionConfiguration, context);
        final String configurationId = Utility.parseOperationId(response.getDeserializedHeaders().getLocation());
        Response<AnomalyDetectionConfiguration> configurationResponse
            = getDetectionConfigWithResponse(configurationId, context);
        return new ResponseBase<Void, AnomalyDetectionConfiguration>(response.getRequest(), response.getStatusCode(),
            response.getHeaders(), configurationResponse.getValue(), null);
    }

    /**
     * Get the anomaly detection configuration by its id.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getDetectionConfig#String -->
     * <pre>
     * final String detectionConfigId = &quot;7b8069a1-1564-46da-9f50-b5d0dd9129ab&quot;;
     * AnomalyDetectionConfiguration detectionConfig = metricsAdvisorAdminClient
     *     .getDetectionConfig&#40;detectionConfigId&#41;;
     * System.out.printf&#40;&quot;Detection config Id: %s%n&quot;, detectionConfig.getId&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Name: %s%n&quot;, detectionConfig.getName&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Description: %s%n&quot;, detectionConfig.getDescription&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;MetricId: %s%n&quot;, detectionConfig.getMetricId&#40;&#41;&#41;;
     *
     * System.out.printf&#40;&quot;Detection conditions specified for configuration...%n&quot;&#41;;
     *
     * System.out.printf&#40;&quot;Whole Series Detection Conditions:%n&quot;&#41;;
     * MetricWholeSeriesDetectionCondition wholeSeriesDetectionCondition
     *     = detectionConfig.getWholeSeriesDetectionCondition&#40;&#41;;
     *
     * System.out.printf&#40;&quot;- Use %s operator for multiple detection conditions:%n&quot;,
     *     wholeSeriesDetectionCondition.getConditionOperator&#40;&#41;&#41;;
     *
     * System.out.printf&#40;&quot;- Smart Detection Condition:%n&quot;&#41;;
     * System.out.printf&#40;&quot; - Sensitivity: %s%n&quot;,
     *     wholeSeriesDetectionCondition.getSmartDetectionCondition&#40;&#41;
     *         .getSensitivity&#40;&#41;&#41;;
     * System.out.printf&#40;&quot; - Detection direction: %s%n&quot;,
     *     wholeSeriesDetectionCondition.getSmartDetectionCondition&#40;&#41;
     *         .getAnomalyDetectorDirection&#40;&#41;&#41;;
     * System.out.printf&#40;&quot; - Suppress conditions: minimum number: %s; minimum ratio: %s%n&quot;,
     *     wholeSeriesDetectionCondition.getSmartDetectionCondition&#40;&#41;
     *         .getSuppressCondition&#40;&#41;.getMinNumber&#40;&#41;,
     *     wholeSeriesDetectionCondition.getSmartDetectionCondition&#40;&#41;
     *         .getSuppressCondition&#40;&#41;.getMinRatio&#40;&#41;&#41;;
     *
     * System.out.printf&#40;&quot;- Hard Threshold Condition:%n&quot;&#41;;
     * System.out.printf&#40;&quot; - Lower bound: %s%n&quot;,
     *     wholeSeriesDetectionCondition.getHardThresholdCondition&#40;&#41;
     *         .getLowerBound&#40;&#41;&#41;;
     * System.out.printf&#40;&quot; - Upper bound: %s%n&quot;,
     *     wholeSeriesDetectionCondition.getHardThresholdCondition&#40;&#41;
     *         .getUpperBound&#40;&#41;&#41;;
     * System.out.printf&#40;&quot; - Suppress conditions: minimum number: %s; minimum ratio: %s%n&quot;,
     *     wholeSeriesDetectionCondition.getHardThresholdCondition&#40;&#41;
     *         .getSuppressCondition&#40;&#41;.getMinNumber&#40;&#41;,
     *     wholeSeriesDetectionCondition.getHardThresholdCondition&#40;&#41;
     *         .getSuppressCondition&#40;&#41;.getMinRatio&#40;&#41;&#41;;
     *
     * System.out.printf&#40;&quot;- Change Threshold Condition:%n&quot;&#41;;
     * System.out.printf&#40;&quot; - Change percentage: %s%n&quot;,
     *     wholeSeriesDetectionCondition.getChangeThresholdCondition&#40;&#41;
     *         .getChangePercentage&#40;&#41;&#41;;
     * System.out.printf&#40;&quot; - Shift point: %s%n&quot;,
     *     wholeSeriesDetectionCondition.getChangeThresholdCondition&#40;&#41;
     *         .getShiftPoint&#40;&#41;&#41;;
     * System.out.printf&#40;&quot; - Detect anomaly if within range: %s%n&quot;,
     *     wholeSeriesDetectionCondition.getChangeThresholdCondition&#40;&#41;
     *         .isWithinRange&#40;&#41;&#41;;
     * System.out.printf&#40;&quot; - Suppress conditions: minimum number: %s; minimum ratio: %s%n&quot;,
     *     wholeSeriesDetectionCondition.getChangeThresholdCondition&#40;&#41;
     *         .getSuppressCondition&#40;&#41;.getMinNumber&#40;&#41;,
     *     wholeSeriesDetectionCondition.getChangeThresholdCondition&#40;&#41;
     *         .getSuppressCondition&#40;&#41;.getMinRatio&#40;&#41;&#41;;
     *
     * List&lt;MetricSingleSeriesDetectionCondition&gt; seriesDetectionConditions
     *     = detectionConfig.getSeriesDetectionConditions&#40;&#41;;
     * System.out.printf&#40;&quot;Series Detection Conditions:%n&quot;&#41;;
     * for &#40;MetricSingleSeriesDetectionCondition seriesDetectionCondition : seriesDetectionConditions&#41; &#123;
     *     DimensionKey seriesKey = seriesDetectionCondition.getSeriesKey&#40;&#41;;
     *     final String seriesKeyStr
     *         = Arrays.toString&#40;seriesKey.asMap&#40;&#41;.entrySet&#40;&#41;.toArray&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;- Series Key:%s%n&quot;, seriesKeyStr&#41;;
     *     System.out.printf&#40;&quot; - Use %s operator for multiple detection conditions:%n&quot;,
     *         seriesDetectionCondition.getConditionOperator&#40;&#41;&#41;;
     *
     *     System.out.printf&#40;&quot; - Smart Detection Condition:%n&quot;&#41;;
     *     System.out.printf&#40;&quot;  - Sensitivity: %s%n&quot;,
     *         seriesDetectionCondition.getSmartDetectionCondition&#40;&#41;
     *             .getSensitivity&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;  - Detection direction: %s%n&quot;,
     *         seriesDetectionCondition.getSmartDetectionCondition&#40;&#41;
     *             .getAnomalyDetectorDirection&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;  - Suppress conditions: minimum number: %s; minimum ratio: %s%n&quot;,
     *         seriesDetectionCondition.getSmartDetectionCondition&#40;&#41;
     *             .getSuppressCondition&#40;&#41;.getMinNumber&#40;&#41;,
     *         seriesDetectionCondition.getSmartDetectionCondition&#40;&#41;
     *             .getSuppressCondition&#40;&#41;.getMinRatio&#40;&#41;&#41;;
     *
     *     System.out.printf&#40;&quot; - Hard Threshold Condition:%n&quot;&#41;;
     *     System.out.printf&#40;&quot;  -  Lower bound: %s%n&quot;,
     *         seriesDetectionCondition.getHardThresholdCondition&#40;&#41;
     *             .getLowerBound&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;  -  Upper bound: %s%n&quot;,
     *         seriesDetectionCondition.getHardThresholdCondition&#40;&#41;
     *             .getUpperBound&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;  -  Suppress conditions: minimum number: %s; minimum ratio: %s%n&quot;,
     *         seriesDetectionCondition.getHardThresholdCondition&#40;&#41;
     *             .getSuppressCondition&#40;&#41;.getMinNumber&#40;&#41;,
     *         seriesDetectionCondition.getHardThresholdCondition&#40;&#41;
     *             .getSuppressCondition&#40;&#41;.getMinRatio&#40;&#41;&#41;;
     *
     *     System.out.printf&#40;&quot; - Change Threshold Condition:%n&quot;&#41;;
     *     System.out.printf&#40;&quot;  -  Change percentage: %s%n&quot;,
     *         seriesDetectionCondition.getChangeThresholdCondition&#40;&#41;
     *             .getChangePercentage&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;  -  Shift point: %s%n&quot;,
     *         seriesDetectionCondition.getChangeThresholdCondition&#40;&#41;
     *             .getShiftPoint&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;  -  Detect anomaly if within range: %s%n&quot;,
     *         seriesDetectionCondition.getChangeThresholdCondition&#40;&#41;
     *             .isWithinRange&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;  -  Suppress conditions: minimum number: %s; minimum ratio: %s%n&quot;,
     *         seriesDetectionCondition.getChangeThresholdCondition&#40;&#41;
     *             .getSuppressCondition&#40;&#41;.getMinNumber&#40;&#41;,
     *         seriesDetectionCondition.getChangeThresholdCondition&#40;&#41;
     *             .getSuppressCondition&#40;&#41;.getMinRatio&#40;&#41;&#41;;
     * &#125;
     *
     * List&lt;MetricSeriesGroupDetectionCondition&gt; seriesGroupDetectionConditions
     *     = detectionConfig.getSeriesGroupDetectionConditions&#40;&#41;;
     * System.out.printf&#40;&quot;Series Group Detection Conditions:%n&quot;&#41;;
     * for &#40;MetricSeriesGroupDetectionCondition seriesGroupDetectionCondition
     *     : seriesGroupDetectionConditions&#41; &#123;
     *     DimensionKey seriesGroupKey = seriesGroupDetectionCondition.getSeriesGroupKey&#40;&#41;;
     *     final String seriesGroupKeyStr
     *         = Arrays.toString&#40;seriesGroupKey.asMap&#40;&#41;.entrySet&#40;&#41;.toArray&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;- Series Group Key:%s%n&quot;, seriesGroupKeyStr&#41;;
     *     System.out.printf&#40;&quot; - Use %s operator for multiple detection conditions:%n&quot;,
     *         seriesGroupDetectionCondition.getConditionOperator&#40;&#41;&#41;;
     *
     *     System.out.printf&#40;&quot; - Smart Detection Condition:%n&quot;&#41;;
     *     System.out.printf&#40;&quot;  - Sensitivity: %s%n&quot;,
     *         seriesGroupDetectionCondition.getSmartDetectionCondition&#40;&#41;
     *             .getSensitivity&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;  - Detection direction: %s%n&quot;,
     *         seriesGroupDetectionCondition.getSmartDetectionCondition&#40;&#41;
     *             .getAnomalyDetectorDirection&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;  - Suppress conditions: minimum number: %s; minimum ratio: %s%n&quot;,
     *         seriesGroupDetectionCondition.getSmartDetectionCondition&#40;&#41;
     *             .getSuppressCondition&#40;&#41;.getMinNumber&#40;&#41;,
     *         seriesGroupDetectionCondition.getSmartDetectionCondition&#40;&#41;
     *             .getSuppressCondition&#40;&#41;.getMinRatio&#40;&#41;&#41;;
     *
     *     System.out.printf&#40;&quot; - Hard Threshold Condition:%n&quot;&#41;;
     *     System.out.printf&#40;&quot;  -  Lower bound: %s%n&quot;,
     *         seriesGroupDetectionCondition.getHardThresholdCondition&#40;&#41;
     *             .getLowerBound&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;  -  Upper bound: %s%n&quot;,
     *         seriesGroupDetectionCondition.getHardThresholdCondition&#40;&#41;
     *             .getUpperBound&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;  -  Suppress conditions: minimum number: %s; minimum ratio: %s%n&quot;,
     *         seriesGroupDetectionCondition.getHardThresholdCondition&#40;&#41;
     *             .getSuppressCondition&#40;&#41;.getMinNumber&#40;&#41;,
     *         seriesGroupDetectionCondition.getHardThresholdCondition&#40;&#41;
     *             .getSuppressCondition&#40;&#41;.getMinRatio&#40;&#41;&#41;;
     *
     *     System.out.printf&#40;&quot; - Change Threshold Condition:%n&quot;&#41;;
     *     System.out.printf&#40;&quot;  -  Change percentage: %s%n&quot;,
     *         seriesGroupDetectionCondition.getChangeThresholdCondition&#40;&#41;
     *             .getChangePercentage&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;  -  Shift point: %s%n&quot;,
     *         seriesGroupDetectionCondition.getChangeThresholdCondition&#40;&#41;
     *             .getShiftPoint&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;  -  Detect anomaly if within range: %s%n&quot;,
     *         seriesGroupDetectionCondition.getChangeThresholdCondition&#40;&#41;
     *             .isWithinRange&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;  -  Suppress conditions: minimum number: %s; minimum ratio: %s%n&quot;,
     *         seriesGroupDetectionCondition.getChangeThresholdCondition&#40;&#41;
     *             .getSuppressCondition&#40;&#41;.getMinNumber&#40;&#41;,
     *         seriesGroupDetectionCondition.getChangeThresholdCondition&#40;&#41;
     *             .getSuppressCondition&#40;&#41;.getMinRatio&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getDetectionConfig#String -->
     *
     * @param detectionConfigurationId The anomaly detection configuration id.
     * @return The {@link AnomalyDetectionConfiguration} identified by the given id.
     * @throws IllegalArgumentException If {@code detectionConfigurationId} does not conform to the UUID
     *     format specification.
     * @throws NullPointerException thrown if the {@code detectionConfigurationId} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public AnomalyDetectionConfiguration getDetectionConfig(String detectionConfigurationId) {
        return getDetectionConfigWithResponse(detectionConfigurationId, Context.NONE).getValue();
    }

    /**
     * Get the anomaly detection configuration by its id.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getDetectionConfigWithResponse#String-Context -->
     * <pre>
     * final String detectionConfigId = &quot;7b8069a1-1564-46da-9f50-b5d0dd9129ab&quot;;
     * Response&lt;AnomalyDetectionConfiguration&gt; response = metricsAdvisorAdminClient
     *     .getDetectionConfigWithResponse&#40;detectionConfigId, Context.NONE&#41;;
     * System.out.printf&#40;&quot;Response statusCode: %d%n&quot;, response.getStatusCode&#40;&#41;&#41;;
     * AnomalyDetectionConfiguration detectionConfig = response.getValue&#40;&#41;;
     * System.out.printf&#40;&quot;Detection config Id: %s%n&quot;, detectionConfig.getId&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Name: %s%n&quot;, detectionConfig.getName&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Description: %s%n&quot;, detectionConfig.getDescription&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;MetricId: %s%n&quot;, detectionConfig.getMetricId&#40;&#41;&#41;;
     *
     * System.out.printf&#40;&quot;Detection conditions specified for configuration...%n&quot;&#41;;
     *
     * System.out.printf&#40;&quot;Whole Series Detection Conditions:%n&quot;&#41;;
     * MetricWholeSeriesDetectionCondition wholeSeriesDetectionCondition
     *     = detectionConfig.getWholeSeriesDetectionCondition&#40;&#41;;
     *
     * System.out.printf&#40;&quot;- Use %s operator for multiple detection conditions:%n&quot;,
     *     wholeSeriesDetectionCondition.getConditionOperator&#40;&#41;&#41;;
     *
     * System.out.printf&#40;&quot;- Smart Detection Condition:%n&quot;&#41;;
     * System.out.printf&#40;&quot; - Sensitivity: %s%n&quot;,
     *     wholeSeriesDetectionCondition.getSmartDetectionCondition&#40;&#41;
     *         .getSensitivity&#40;&#41;&#41;;
     * System.out.printf&#40;&quot; - Detection direction: %s%n&quot;,
     *     wholeSeriesDetectionCondition.getSmartDetectionCondition&#40;&#41;
     *         .getAnomalyDetectorDirection&#40;&#41;&#41;;
     * System.out.printf&#40;&quot; - Suppress conditions: minimum number: %s; minimum ratio: %s%n&quot;,
     *     wholeSeriesDetectionCondition.getSmartDetectionCondition&#40;&#41;
     *         .getSuppressCondition&#40;&#41;.getMinNumber&#40;&#41;,
     *     wholeSeriesDetectionCondition.getSmartDetectionCondition&#40;&#41;
     *         .getSuppressCondition&#40;&#41;.getMinRatio&#40;&#41;&#41;;
     *
     * System.out.printf&#40;&quot;- Hard Threshold Condition:%n&quot;&#41;;
     * System.out.printf&#40;&quot; - Lower bound: %s%n&quot;,
     *     wholeSeriesDetectionCondition.getHardThresholdCondition&#40;&#41;
     *         .getLowerBound&#40;&#41;&#41;;
     * System.out.printf&#40;&quot; - Upper bound: %s%n&quot;,
     *     wholeSeriesDetectionCondition.getHardThresholdCondition&#40;&#41;
     *         .getUpperBound&#40;&#41;&#41;;
     * System.out.printf&#40;&quot; - Suppress conditions: minimum number: %s; minimum ratio: %s%n&quot;,
     *     wholeSeriesDetectionCondition.getHardThresholdCondition&#40;&#41;
     *         .getSuppressCondition&#40;&#41;.getMinNumber&#40;&#41;,
     *     wholeSeriesDetectionCondition.getHardThresholdCondition&#40;&#41;
     *         .getSuppressCondition&#40;&#41;.getMinRatio&#40;&#41;&#41;;
     *
     * System.out.printf&#40;&quot;- Change Threshold Condition:%n&quot;&#41;;
     * System.out.printf&#40;&quot; - Change percentage: %s%n&quot;,
     *     wholeSeriesDetectionCondition.getChangeThresholdCondition&#40;&#41;
     *         .getChangePercentage&#40;&#41;&#41;;
     * System.out.printf&#40;&quot; - Shift point: %s%n&quot;,
     *     wholeSeriesDetectionCondition.getChangeThresholdCondition&#40;&#41;
     *         .getShiftPoint&#40;&#41;&#41;;
     * System.out.printf&#40;&quot; - Detect anomaly if within range: %s%n&quot;,
     *     wholeSeriesDetectionCondition.getChangeThresholdCondition&#40;&#41;
     *         .isWithinRange&#40;&#41;&#41;;
     * System.out.printf&#40;&quot; - Suppress conditions: minimum number: %s; minimum ratio: %s%n&quot;,
     *     wholeSeriesDetectionCondition.getChangeThresholdCondition&#40;&#41;
     *         .getSuppressCondition&#40;&#41;.getMinNumber&#40;&#41;,
     *     wholeSeriesDetectionCondition.getChangeThresholdCondition&#40;&#41;
     *         .getSuppressCondition&#40;&#41;.getMinRatio&#40;&#41;&#41;;
     *
     * List&lt;MetricSingleSeriesDetectionCondition&gt; seriesDetectionConditions
     *     = detectionConfig.getSeriesDetectionConditions&#40;&#41;;
     * System.out.printf&#40;&quot;Series Detection Conditions:%n&quot;&#41;;
     * for &#40;MetricSingleSeriesDetectionCondition seriesDetectionCondition : seriesDetectionConditions&#41; &#123;
     *     DimensionKey seriesKey = seriesDetectionCondition.getSeriesKey&#40;&#41;;
     *     final String seriesKeyStr
     *         = Arrays.toString&#40;seriesKey.asMap&#40;&#41;.entrySet&#40;&#41;.toArray&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;- Series Key:%s%n&quot;, seriesKeyStr&#41;;
     *     System.out.printf&#40;&quot; - Use %s operator for multiple detection conditions:%n&quot;,
     *         seriesDetectionCondition.getConditionOperator&#40;&#41;&#41;;
     *
     *     System.out.printf&#40;&quot; - Smart Detection Condition:%n&quot;&#41;;
     *     System.out.printf&#40;&quot;  - Sensitivity: %s%n&quot;,
     *         seriesDetectionCondition.getSmartDetectionCondition&#40;&#41;
     *             .getSensitivity&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;  - Detection direction: %s%n&quot;,
     *         seriesDetectionCondition.getSmartDetectionCondition&#40;&#41;
     *             .getAnomalyDetectorDirection&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;  - Suppress conditions: minimum number: %s; minimum ratio: %s%n&quot;,
     *         seriesDetectionCondition.getSmartDetectionCondition&#40;&#41;
     *             .getSuppressCondition&#40;&#41;.getMinNumber&#40;&#41;,
     *         seriesDetectionCondition.getSmartDetectionCondition&#40;&#41;
     *             .getSuppressCondition&#40;&#41;.getMinRatio&#40;&#41;&#41;;
     *
     *     System.out.printf&#40;&quot; - Hard Threshold Condition:%n&quot;&#41;;
     *     System.out.printf&#40;&quot;  -  Lower bound: %s%n&quot;,
     *         seriesDetectionCondition.getHardThresholdCondition&#40;&#41;
     *             .getLowerBound&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;  -  Upper bound: %s%n&quot;,
     *         seriesDetectionCondition.getHardThresholdCondition&#40;&#41;
     *             .getUpperBound&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;  -  Suppress conditions: minimum number: %s; minimum ratio: %s%n&quot;,
     *         seriesDetectionCondition.getHardThresholdCondition&#40;&#41;
     *             .getSuppressCondition&#40;&#41;.getMinNumber&#40;&#41;,
     *         seriesDetectionCondition.getHardThresholdCondition&#40;&#41;
     *             .getSuppressCondition&#40;&#41;.getMinRatio&#40;&#41;&#41;;
     *
     *     System.out.printf&#40;&quot; - Change Threshold Condition:%n&quot;&#41;;
     *     System.out.printf&#40;&quot;  -  Change percentage: %s%n&quot;,
     *         seriesDetectionCondition.getChangeThresholdCondition&#40;&#41;
     *             .getChangePercentage&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;  -  Shift point: %s%n&quot;,
     *         seriesDetectionCondition.getChangeThresholdCondition&#40;&#41;
     *             .getShiftPoint&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;  -  Detect anomaly if within range: %s%n&quot;,
     *         seriesDetectionCondition.getChangeThresholdCondition&#40;&#41;
     *             .isWithinRange&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;  -  Suppress conditions: minimum number: %s; minimum ratio: %s%n&quot;,
     *         seriesDetectionCondition.getChangeThresholdCondition&#40;&#41;
     *             .getSuppressCondition&#40;&#41;.getMinNumber&#40;&#41;,
     *         seriesDetectionCondition.getChangeThresholdCondition&#40;&#41;
     *             .getSuppressCondition&#40;&#41;.getMinRatio&#40;&#41;&#41;;
     * &#125;
     *
     * List&lt;MetricSeriesGroupDetectionCondition&gt; seriesGroupDetectionConditions
     *     = detectionConfig.getSeriesGroupDetectionConditions&#40;&#41;;
     * System.out.printf&#40;&quot;Series Group Detection Conditions:%n&quot;&#41;;
     * for &#40;MetricSeriesGroupDetectionCondition seriesGroupDetectionCondition
     *     : seriesGroupDetectionConditions&#41; &#123;
     *     DimensionKey seriesGroupKey = seriesGroupDetectionCondition.getSeriesGroupKey&#40;&#41;;
     *     final String seriesGroupKeyStr
     *         = Arrays.toString&#40;seriesGroupKey.asMap&#40;&#41;.entrySet&#40;&#41;.toArray&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;- Series Group Key:%s%n&quot;, seriesGroupKeyStr&#41;;
     *     System.out.printf&#40;&quot; - Use %s operator for multiple detection conditions:%n&quot;,
     *         seriesGroupDetectionCondition.getConditionOperator&#40;&#41;&#41;;
     *
     *     System.out.printf&#40;&quot; - Smart Detection Condition:%n&quot;&#41;;
     *     System.out.printf&#40;&quot;  - Sensitivity: %s%n&quot;,
     *         seriesGroupDetectionCondition.getSmartDetectionCondition&#40;&#41;
     *             .getSensitivity&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;  - Detection direction: %s%n&quot;,
     *         seriesGroupDetectionCondition.getSmartDetectionCondition&#40;&#41;
     *             .getAnomalyDetectorDirection&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;  - Suppress conditions: minimum number: %s; minimum ratio: %s%n&quot;,
     *         seriesGroupDetectionCondition.getSmartDetectionCondition&#40;&#41;
     *             .getSuppressCondition&#40;&#41;.getMinNumber&#40;&#41;,
     *         seriesGroupDetectionCondition.getSmartDetectionCondition&#40;&#41;
     *             .getSuppressCondition&#40;&#41;.getMinRatio&#40;&#41;&#41;;
     *
     *     System.out.printf&#40;&quot; - Hard Threshold Condition:%n&quot;&#41;;
     *     System.out.printf&#40;&quot;  -  Lower bound: %s%n&quot;,
     *         seriesGroupDetectionCondition.getHardThresholdCondition&#40;&#41;
     *             .getLowerBound&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;  -  Upper bound: %s%n&quot;,
     *         seriesGroupDetectionCondition.getHardThresholdCondition&#40;&#41;
     *             .getUpperBound&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;  -  Suppress conditions: minimum number: %s; minimum ratio: %s%n&quot;,
     *         seriesGroupDetectionCondition.getHardThresholdCondition&#40;&#41;
     *             .getSuppressCondition&#40;&#41;.getMinNumber&#40;&#41;,
     *         seriesGroupDetectionCondition.getHardThresholdCondition&#40;&#41;
     *             .getSuppressCondition&#40;&#41;.getMinRatio&#40;&#41;&#41;;
     *
     *     System.out.printf&#40;&quot; - Change Threshold Condition:%n&quot;&#41;;
     *     System.out.printf&#40;&quot;  -  Change percentage: %s%n&quot;,
     *         seriesGroupDetectionCondition.getChangeThresholdCondition&#40;&#41;
     *             .getChangePercentage&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;  -  Shift point: %s%n&quot;,
     *         seriesGroupDetectionCondition.getChangeThresholdCondition&#40;&#41;
     *             .getShiftPoint&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;  -  Detect anomaly if within range: %s%n&quot;,
     *         seriesGroupDetectionCondition.getChangeThresholdCondition&#40;&#41;
     *             .isWithinRange&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;  -  Suppress conditions: minimum number: %s; minimum ratio: %s%n&quot;,
     *         seriesGroupDetectionCondition.getChangeThresholdCondition&#40;&#41;
     *             .getSuppressCondition&#40;&#41;.getMinNumber&#40;&#41;,
     *         seriesGroupDetectionCondition.getChangeThresholdCondition&#40;&#41;
     *             .getSuppressCondition&#40;&#41;.getMinRatio&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getDetectionConfigWithResponse#String-Context -->
     *
     * @param detectionConfigurationId The anomaly detection configuration id.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} containing the {@link AnomalyDetectionConfiguration} for the provided id.
     * @throws IllegalArgumentException If {@code detectionConfigurationId} does not conform to the UUID
     *     format specification.
     * @throws NullPointerException thrown if the {@code detectionConfigurationId} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<AnomalyDetectionConfiguration> getDetectionConfigWithResponse(String detectionConfigurationId,
        Context context) {
        return getDetectionConfigWithResponseSync(detectionConfigurationId, context);
    }

    Response<AnomalyDetectionConfiguration> getDetectionConfigWithResponseSync(String detectionConfigurationId,
        Context context) {
        Objects.requireNonNull(detectionConfigurationId, "detectionConfigurationId is required.");

        Response<com.azure.ai.metricsadvisor.implementation.models.AnomalyDetectionConfiguration> response
            = service.getAnomalyDetectionConfigurationWithResponse(UUID.fromString(detectionConfigurationId), context);

        AnomalyDetectionConfiguration configuration = DetectionConfigurationTransforms.fromInner(response.getValue());
        return new ResponseBase<Void, AnomalyDetectionConfiguration>(response.getRequest(), response.getStatusCode(),
            response.getHeaders(), configuration, null);
    }

    /**
     * Update a configuration to detect anomalies in the time series of a metric.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateDetectionConfig#AnomalyDetectionConfiguration -->
     * <pre>
     * final String detectionConfigId = &quot;7b8069a1-1564-46da-9f50-b5d0dd9129ab&quot;;
     * AnomalyDetectionConfiguration detectionConfig = metricsAdvisorAdminClient
     *     .getDetectionConfig&#40;detectionConfigId&#41;;
     *
     * detectionConfig.setName&#40;&quot;updated config name&quot;&#41;;
     * detectionConfig.setDescription&#40;&quot;updated with more detection conditions&quot;&#41;;
     * DimensionKey seriesGroupKey = new DimensionKey&#40;&#41;
     *     .put&#40;&quot;city&quot;, &quot;Seoul&quot;&#41;;
     * detectionConfig.addSeriesGroupDetectionCondition&#40;
     *     new MetricSeriesGroupDetectionCondition&#40;seriesGroupKey&#41;
     *         .setSmartDetectionCondition&#40;new SmartDetectionCondition&#40;
     *             10.0,
     *             AnomalyDetectorDirection.UP,
     *             new SuppressCondition&#40;2, 2&#41;&#41;&#41;&#41;;
     *
     * AnomalyDetectionConfiguration updatedDetectionConfig = metricsAdvisorAdminClient
     *     .updateDetectionConfig&#40;detectionConfig&#41;;
     *
     * System.out.printf&#40;&quot;Detection config Id: %s%n&quot;, updatedDetectionConfig.getId&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Name: %s%n&quot;, updatedDetectionConfig.getName&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Description: %s%n&quot;, updatedDetectionConfig.getDescription&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;MetricId: %s%n&quot;, updatedDetectionConfig.getMetricId&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateDetectionConfig#AnomalyDetectionConfiguration -->
     *
     * @param detectionConfiguration The anomaly detection configuration.
     * @return The updated {@link AnomalyDetectionConfiguration}.
     * @throws NullPointerException thrown if the {@code detectionConfiguration} is null
     *   or {@code detectionConfiguration.id} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public AnomalyDetectionConfiguration updateDetectionConfig(AnomalyDetectionConfiguration detectionConfiguration) {
        return updateDetectionConfigWithResponse(detectionConfiguration, Context.NONE).getValue();
    }

    /**
     * Update a configuration to detect anomalies in the time series of a metric.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateDetectionConfigWithResponse#AnomalyDetectionConfiguration-Context -->
     * <pre>
     * final String detectionConfigId = &quot;7b8069a1-1564-46da-9f50-b5d0dd9129ab&quot;;
     * Response&lt;AnomalyDetectionConfiguration&gt; getResponse = metricsAdvisorAdminClient
     *     .getDetectionConfigWithResponse&#40;detectionConfigId, Context.NONE&#41;;
     * AnomalyDetectionConfiguration detectionConfig = getResponse.getValue&#40;&#41;;
     * detectionConfig.setName&#40;&quot;updated config name&quot;&#41;;
     * detectionConfig.setDescription&#40;&quot;updated with more detection conditions&quot;&#41;;
     * DimensionKey seriesGroupKey = new DimensionKey&#40;&#41;
     *     .put&#40;&quot;city&quot;, &quot;Seoul&quot;&#41;;
     * detectionConfig.addSeriesGroupDetectionCondition&#40;
     *     new MetricSeriesGroupDetectionCondition&#40;seriesGroupKey&#41;
     *         .setSmartDetectionCondition&#40;new SmartDetectionCondition&#40;
     *             10.0,
     *             AnomalyDetectorDirection.UP,
     *             new SuppressCondition&#40;2, 2&#41;&#41;&#41;&#41;;
     *
     * Response&lt;AnomalyDetectionConfiguration&gt; updateResponse = metricsAdvisorAdminClient
     *     .updateDetectionConfigWithResponse&#40;detectionConfig, Context.NONE&#41;;
     *
     * System.out.printf&#40;&quot;Response StatusCode: %s%n&quot;, updateResponse.getStatusCode&#40;&#41;&#41;;
     * AnomalyDetectionConfiguration updatedDetectionConfig = updateResponse.getValue&#40;&#41;;
     * System.out.printf&#40;&quot;Detection config Id: %s%n&quot;, updatedDetectionConfig.getId&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Name: %s%n&quot;, updatedDetectionConfig.getName&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Description: %s%n&quot;, updatedDetectionConfig.getDescription&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;MetricId: %s%n&quot;, updatedDetectionConfig.getMetricId&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateDetectionConfigWithResponse#AnomalyDetectionConfiguration-Context -->
     *
     * @param detectionConfiguration The anomaly detection configuration.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} containing the updated {@link AnomalyDetectionConfiguration}.
     * @throws NullPointerException thrown if the {@code detectionConfiguration} is null
     *   or {@code detectionConfiguration.id} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<AnomalyDetectionConfiguration>
        updateDetectionConfigWithResponse(AnomalyDetectionConfiguration detectionConfiguration, Context context) {
        return updateDetectionConfigWithResponseSync(detectionConfiguration, context);
    }

    Response<AnomalyDetectionConfiguration>
        updateDetectionConfigWithResponseSync(AnomalyDetectionConfiguration detectionConfiguration, Context context) {
        Objects.requireNonNull(detectionConfiguration, "detectionConfiguration is required");
        Objects.requireNonNull(detectionConfiguration.getId(), "detectionConfiguration.id is required");

        final AnomalyDetectionConfigurationPatch innerDetectionConfigurationPatch
            = DetectionConfigurationTransforms.toInnerForUpdate(logger, detectionConfiguration);
        Response<com.azure.ai.metricsadvisor.implementation.models.AnomalyDetectionConfiguration> response
            = service.updateAnomalyDetectionConfigurationWithResponse(UUID.fromString(detectionConfiguration.getId()),
                innerDetectionConfigurationPatch, context);

        Response<AnomalyDetectionConfiguration> configurationResponse
            = getDetectionConfigWithResponse(detectionConfiguration.getId(), context);
        return new ResponseBase<Void, AnomalyDetectionConfiguration>(response.getRequest(), response.getStatusCode(),
            response.getHeaders(), configurationResponse.getValue(), null);
    }

    /**
     * Delete a metric anomaly detection configuration.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteDetectionConfig#String -->
     * <pre>
     * final String detectionConfigId = &quot;7b8069a1-1564-46da-9f50-b5d0dd9129ab&quot;;
     * metricsAdvisorAdminClient
     *     .deleteDetectionConfig&#40;detectionConfigId&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteDetectionConfig#String -->
     *
     * @param detectionConfigurationId The metric anomaly detection configuration unique id.
     *
     * @throws NullPointerException thrown if the {@code detectionConfigurationId} is null.
     * @throws IllegalArgumentException If {@code detectionConfigurationId} does not conform to the UUID
     *     format specification.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteDetectionConfig(String detectionConfigurationId) {
        deleteDetectionConfigWithResponse(detectionConfigurationId, Context.NONE).getValue();
    }

    /**
     * Delete a metric anomaly detection configuration.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteDetectionConfigWithResponse#String-Context -->
     * <pre>
     * final String detectionConfigId = &quot;7b8069a1-1564-46da-9f50-b5d0dd9129ab&quot;;
     * Response&lt;Void&gt; response = metricsAdvisorAdminClient
     *     .deleteDetectionConfigWithResponse&#40;detectionConfigId, Context.NONE&#41;;
     * System.out.printf&#40;&quot;Response Status Code: %s%n&quot;, response.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteDetectionConfigWithResponse#String-Context -->
     *
     * @param detectionConfigurationId The metric anomaly detection configuration unique id.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} of containing result of delete operation.
     * @throws NullPointerException thrown if the {@code detectionConfigurationId} is null.
     * @throws IllegalArgumentException If {@code detectionConfigurationId} does not conform to the UUID
     *     format specification.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteDetectionConfigWithResponse(String detectionConfigurationId, Context context) {
        return deleteDetectionConfigWithResponseSync(detectionConfigurationId, context);
    }

    Response<Void> deleteDetectionConfigWithResponseSync(String detectionConfigurationId, Context context) {
        Objects.requireNonNull(detectionConfigurationId, "detectionConfigurationId is required.");
        return service.deleteHookWithResponse(UUID.fromString(detectionConfigurationId), context);
    }

    /**
     * Given a metric id, retrieve all anomaly detection configurations applied to it.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listDetectionConfigs#String -->
     * <pre>
     * final String metricId = &quot;0b836da8-10e6-46cd-8f4f-28262e113a62&quot;;
     * PagedIterable&lt;AnomalyDetectionConfiguration&gt; configsIterable
     *     = metricsAdvisorAdminClient.listDetectionConfigs&#40;metricId&#41;;
     *
     * for &#40;AnomalyDetectionConfiguration detectionConfig : configsIterable&#41; &#123;
     *     System.out.printf&#40;&quot;Detection config Id: %s%n&quot;, detectionConfig.getId&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Name: %s%n&quot;, detectionConfig.getName&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Description: %s%n&quot;, detectionConfig.getDescription&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;MetricId: %s%n&quot;, detectionConfig.getMetricId&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listDetectionConfigs#String -->
     *
     * @param metricId The metric id.
     * @return The anomaly detection configurations.
     * @throws NullPointerException thrown if the {@code metricId} is null.
     * @throws IllegalArgumentException If {@code metricId} does not conform to the UUID format specification.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<AnomalyDetectionConfiguration> listDetectionConfigs(String metricId) {
        return listDetectionConfigs(metricId, null, Context.NONE);
    }

    /**
     * Given a metric id, retrieve all anomaly detection configurations applied to it.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listDetectionConfigs#String-ListDetectionConfigsOptions-Context -->
     * <pre>
     * final String metricId = &quot;0b836da8-10e6-46cd-8f4f-28262e113a62&quot;;
     * PagedIterable&lt;AnomalyDetectionConfiguration&gt; configsIterable
     *     = metricsAdvisorAdminClient.listDetectionConfigs&#40;metricId,
     *         new ListDetectionConfigsOptions&#40;&#41;, Context.NONE&#41;;
     *
     * Stream&lt;PagedResponse&lt;AnomalyDetectionConfiguration&gt;&gt; configByPageStream
     *     = configsIterable.streamByPage&#40;&#41;;
     *
     * configByPageStream.forEach&#40;configPage -&gt; &#123;
     *     IterableStream&lt;AnomalyDetectionConfiguration&gt; pageElements = configPage.getElements&#40;&#41;;
     *     for &#40;AnomalyDetectionConfiguration detectionConfig : pageElements&#41; &#123;
     *         System.out.printf&#40;&quot;Detection config Id: %s%n&quot;, detectionConfig.getId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Name: %s%n&quot;, detectionConfig.getName&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Description: %s%n&quot;, detectionConfig.getDescription&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;MetricId: %s%n&quot;, detectionConfig.getMetricId&#40;&#41;&#41;;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listDetectionConfigs#String-ListDetectionConfigsOptions-Context -->
     *
     * @param metricId The metric id.
     * @param options th e additional configurable options to specify when querying the result.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return The anomaly detection configurations.
     * @throws NullPointerException thrown if the {@code metricId} is null.
     * @throws IllegalArgumentException If {@code metricId} does not conform to the UUID format specification.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<AnomalyDetectionConfiguration> listDetectionConfigs(String metricId,
        ListDetectionConfigsOptions options, Context context) {
        return listDetectionConfigsSync(metricId, options, context);
    }

    PagedIterable<AnomalyDetectionConfiguration> listDetectionConfigsSync(String metricId,
        ListDetectionConfigsOptions options, Context context) {
        return new PagedIterable<>(() -> listAnomalyDetectionConfigsSinglePageSync(metricId, options, context),
            continuationToken -> listAnomalyDetectionConfigsNextPageSync(continuationToken, context));
    }

    private PagedResponse<AnomalyDetectionConfiguration> listAnomalyDetectionConfigsSinglePageSync(String metricId,
        ListDetectionConfigsOptions options, Context context) {
        if (options == null) {
            options = new ListDetectionConfigsOptions();
        }
        PagedResponse<com.azure.ai.metricsadvisor.implementation.models.AnomalyDetectionConfiguration> response
            = service.getAnomalyDetectionConfigurationsByMetricSinglePage(UUID.fromString(metricId), options.getSkip(),
                options.getMaxPageSize(), context);
        return DetectionConfigurationTransforms.fromInnerPagedResponse(response);
    }

    private PagedResponse<AnomalyDetectionConfiguration> listAnomalyDetectionConfigsNextPageSync(String nextPageLink,
        Context context) {
        if (CoreUtils.isNullOrEmpty(nextPageLink)) {
            return null;
        }
        PagedResponse<com.azure.ai.metricsadvisor.implementation.models.AnomalyDetectionConfiguration> response
            = service.getAnomalyDetectionConfigurationsByMetricNextSinglePage(nextPageLink, context);

        return DetectionConfigurationTransforms.fromInnerPagedResponse(response);
    }

    /**
     * Creates a notificationHook that receives anomaly incident alerts.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createHook#NotificationHook -->
     * <pre>
     * NotificationHook emailNotificationHook = new EmailNotificationHook&#40;&quot;email notificationHook&quot;&#41;
     *     .setDescription&#40;&quot;my email notificationHook&quot;&#41;
     *     .setEmailsToAlert&#40;new ArrayList&lt;String&gt;&#40;&#41; &#123;&#123;
     *             add&#40;&quot;alertme&#64;alertme.com&quot;&#41;;
     *         &#125;&#125;&#41;
     *     .setExternalLink&#40;&quot;https:&#47;&#47;adwiki.azurewebsites.net&#47;articles&#47;howto&#47;alerts&#47;create-hooks.html&quot;&#41;;
     *
     * NotificationHook notificationHook = metricsAdvisorAdminClient.createHook&#40;emailNotificationHook&#41;;
     * EmailNotificationHook createdEmailHook = &#40;EmailNotificationHook&#41; notificationHook;
     * System.out.printf&#40;&quot;NotificationHook Id: %s%n&quot;, createdEmailHook.getId&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;NotificationHook Name: %s%n&quot;, createdEmailHook.getName&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;NotificationHook Description: %s%n&quot;, createdEmailHook.getDescription&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;NotificationHook External Link: %s%n&quot;, createdEmailHook.getExternalLink&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;NotificationHook Emails: %s%n&quot;, String.join&#40;&quot;,&quot;,
     *     createdEmailHook.getEmailsToAlert&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createHook#NotificationHook -->
     *
     * @param notificationHook The notificationHook.
     * @return The created {@link NotificationHook}.
     * @throws NullPointerException If {@code notificationHook}, {@code notificationHook.name},
     * {@code notificationHook.endpoint} (for web notificationHook) is null.
     * @throws IllegalArgumentException If at least one email not present for email notificationHook.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public NotificationHook createHook(NotificationHook notificationHook) {
        return createHookWithResponse(notificationHook, Context.NONE).getValue();
    }

    /**
     * Creates a notificationHook that receives anomaly incident alerts.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createHookWithResponse#NotificationHook-Context -->
     * <pre>
     * NotificationHook emailNotificationHook = new EmailNotificationHook&#40;&quot;email hook&quot;&#41;
     *     .setDescription&#40;&quot;my email hook&quot;&#41;
     *     .setEmailsToAlert&#40;new ArrayList&lt;String&gt;&#40;&#41; &#123;&#123;
     *             add&#40;&quot;alertme&#64;alertme.com&quot;&#41;;
     *         &#125;&#125;&#41;
     *     .setExternalLink&#40;&quot;https:&#47;&#47;adwiki.azurewebsites.net&#47;articles&#47;howto&#47;alerts&#47;create-hooks.html&quot;&#41;;
     *
     * Response&lt;NotificationHook&gt; response
     *     = metricsAdvisorAdminClient.createHookWithResponse&#40;emailNotificationHook, Context.NONE&#41;;
     * System.out.printf&#40;&quot;Response statusCode: %d%n&quot;, response.getStatusCode&#40;&#41;&#41;;
     * EmailNotificationHook createdEmailHook = &#40;EmailNotificationHook&#41; response.getValue&#40;&#41;;
     * System.out.printf&#40;&quot;NotificationHook Id: %s%n&quot;, createdEmailHook.getId&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;NotificationHook Name: %s%n&quot;, createdEmailHook.getName&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;NotificationHook Description: %s%n&quot;, createdEmailHook.getDescription&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;NotificationHook External Link: %s%n&quot;, createdEmailHook.getExternalLink&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;NotificationHook Emails: %s%n&quot;, String.join&#40;&quot;,&quot;,
     *     createdEmailHook.getEmailsToAlert&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createHookWithResponse#NotificationHook-Context -->
     *
     * @param notificationHook The notificationHook.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} containing the created {@link NotificationHook}.
     * @throws NullPointerException If {@code notificationHook}, {@code notificationHook.name},
     * {@code notificationHook.endpoint} (for web notificationHook) is null.
     * @throws IllegalArgumentException If at least one email not present for email notificationHook.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<NotificationHook> createHookWithResponse(NotificationHook notificationHook, Context context) {
        return createHookWithResponseSync(notificationHook, context);
    }

    Response<NotificationHook> createHookWithResponseSync(NotificationHook notificationHook, Context context) {
        Objects.requireNonNull(notificationHook, "'notificationHook' cannot be null.");
        CreateHookResponse response
            = service.createHookWithResponse(HookTransforms.toInnerForCreate(logger, notificationHook), context);
        final String hookUri = response.getDeserializedHeaders().getLocation();
        final String hookId = parseOperationId(hookUri);
        Response<NotificationHook> hookResponse = getHookWithResponse(hookId, context);
        return new ResponseBase<Void, NotificationHook>(response.getRequest(), response.getStatusCode(),
            response.getHeaders(), hookResponse.getValue(), null);
    }

    /**
     * Get a hook by its id.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getHook#String -->
     * <pre>
     * final String hookId = &quot;f00853f1-6627-447f-bacf-8dccf2e86fed&quot;;
     * NotificationHook notificationHook = metricsAdvisorAdminClient.getHook&#40;hookId&#41;;
     * if &#40;notificationHook instanceof EmailNotificationHook&#41; &#123;
     *     EmailNotificationHook emailHook = &#40;EmailNotificationHook&#41; notificationHook;
     *     System.out.printf&#40;&quot;NotificationHook Id: %s%n&quot;, emailHook.getId&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;NotificationHook Name: %s%n&quot;, emailHook.getName&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;NotificationHook Description: %s%n&quot;, emailHook.getDescription&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;NotificationHook External Link: %s%n&quot;, emailHook.getExternalLink&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;NotificationHook Emails: %s%n&quot;, String.join&#40;&quot;,&quot;, emailHook.getEmailsToAlert&#40;&#41;&#41;&#41;;
     * &#125; else if &#40;notificationHook instanceof WebNotificationHook&#41; &#123;
     *     WebNotificationHook webHook = &#40;WebNotificationHook&#41; notificationHook;
     *     System.out.printf&#40;&quot;NotificationHook Id: %s%n&quot;, webHook.getId&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;NotificationHook Name: %s%n&quot;, webHook.getName&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;NotificationHook Description: %s%n&quot;, webHook.getDescription&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;NotificationHook External Link: %s%n&quot;, webHook.getExternalLink&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;NotificationHook Endpoint: %s%n&quot;, webHook.getEndpoint&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;NotificationHook Headers: %s%n&quot;, webHook.getHttpHeaders&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getHook#String -->
     *
     * @param hookId The hook unique id.
     * @return The {@link NotificationHook} for the provided id.
     * @throws IllegalArgumentException If {@code hookId} does not conform to the UUID format specification.
     * @throws NullPointerException thrown if the {@code hookId} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public NotificationHook getHook(String hookId) {
        return getHookWithResponse(hookId, Context.NONE).getValue();
    }

    /**
     * Get a hook by its id.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getHookWithResponse#String-Context -->
     * <pre>
     * final String hookId = &quot;f00853f1-6627-447f-bacf-8dccf2e86fed&quot;;
     * Response&lt;NotificationHook&gt; response = metricsAdvisorAdminClient.getHookWithResponse&#40;hookId, Context.NONE&#41;;
     * System.out.printf&#40;&quot;Response statusCode: %d%n&quot;, response.getStatusCode&#40;&#41;&#41;;
     * NotificationHook notificationHook = response.getValue&#40;&#41;;
     * if &#40;notificationHook instanceof EmailNotificationHook&#41; &#123;
     *     EmailNotificationHook emailHook = &#40;EmailNotificationHook&#41; notificationHook;
     *     System.out.printf&#40;&quot;NotificationHook Id: %s%n&quot;, emailHook.getId&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;NotificationHook Name: %s%n&quot;, emailHook.getName&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;NotificationHook Description: %s%n&quot;, emailHook.getDescription&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;NotificationHook External Link: %s%n&quot;, emailHook.getExternalLink&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;NotificationHook Emails: %s%n&quot;, String.join&#40;&quot;,&quot;, emailHook.getEmailsToAlert&#40;&#41;&#41;&#41;;
     * &#125; else if &#40;notificationHook instanceof WebNotificationHook&#41; &#123;
     *     WebNotificationHook webHook = &#40;WebNotificationHook&#41; notificationHook;
     *     System.out.printf&#40;&quot;NotificationHook Id: %s%n&quot;, webHook.getId&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;NotificationHook Name: %s%n&quot;, webHook.getName&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;NotificationHook Description: %s%n&quot;, webHook.getDescription&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;NotificationHook External Link: %s%n&quot;, webHook.getExternalLink&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;NotificationHook Endpoint: %s%n&quot;, webHook.getEndpoint&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;NotificationHook Headers: %s%n&quot;, webHook.getHttpHeaders&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getHookWithResponse#String-Context -->
     *
     * @param hookId The hook unique id.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} containing the {@link NotificationHook} for the provided id.
     * @throws IllegalArgumentException If {@code hookId} does not conform to the UUID format specification.
     * @throws NullPointerException thrown if the {@code hookId} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<NotificationHook> getHookWithResponse(String hookId, Context context) {
        return getHookWithResponseSync(hookId, context);
    }

    Response<NotificationHook> getHookWithResponseSync(String hookId, Context context) {
        Objects.requireNonNull(hookId, "hookId is required.");
        Response<HookInfo> innerResponse = service.getHookWithResponse(UUID.fromString(hookId), context);
        return new ResponseBase<Void, NotificationHook>(innerResponse.getRequest(), innerResponse.getStatusCode(),
            innerResponse.getHeaders(), HookTransforms.fromInner(logger, innerResponse.getValue()), null);
    }

    /**
     * Update an existing notificationHook.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateHook#NotificationHook -->
     * <pre>
     * final String emailHookId = &quot;f00853f1-6627-447f-bacf-8dccf2e86fed&quot;;
     * NotificationHook notificationHook = metricsAdvisorAdminClient.getHook&#40;emailHookId&#41;;
     * EmailNotificationHook emailHook = &#40;EmailNotificationHook&#41; notificationHook;
     * List&lt;String&gt; emailsToUpdate = new ArrayList&lt;&gt;&#40;emailHook.getEmailsToAlert&#40;&#41;&#41;;
     * emailsToUpdate.remove&#40;&quot;alertme&#64;alertme.com&quot;&#41;;
     * emailsToUpdate.add&#40;&quot;alertme2&#64;alertme.com&quot;&#41;;
     * emailsToUpdate.add&#40;&quot;alertme3&#64;alertme.com&quot;&#41;;
     * emailHook.setEmailsToAlert&#40;emailsToUpdate&#41;;
     * NotificationHook updatedNotificationHook = metricsAdvisorAdminClient.updateHook&#40;emailHook&#41;;
     * EmailNotificationHook updatedEmailHook = &#40;EmailNotificationHook&#41; updatedNotificationHook;
     * System.out.printf&#40;&quot;NotificationHook Id: %s%n&quot;, updatedEmailHook.getId&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;NotificationHook Name: %s%n&quot;, updatedEmailHook.getName&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;NotificationHook Description: %s%n&quot;, updatedEmailHook.getDescription&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;NotificationHook External Link: %s%n&quot;, updatedEmailHook.getExternalLink&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;NotificationHook Emails: %s%n&quot;, String.join&#40;&quot;,&quot;,
     *     updatedEmailHook.getEmailsToAlert&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateHook#NotificationHook -->
     *
     * @param notificationHook The notificationHook to update.
     * @return The updated {@link NotificationHook}.
     * @throws NullPointerException If {@code notificationHook.id} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public NotificationHook updateHook(NotificationHook notificationHook) {
        return updateHookWithResponse(notificationHook, Context.NONE).getValue();
    }

    /**
     * Update an existing notificationHook.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateHookWithResponse#NotificationHook-Context -->
     * <pre>
     * final String emailHookId = &quot;f00853f1-6627-447f-bacf-8dccf2e86fed&quot;;
     * Response&lt;NotificationHook&gt; response
     *     = metricsAdvisorAdminClient.getHookWithResponse&#40;emailHookId, Context.NONE&#41;;
     * EmailNotificationHook emailHook = &#40;EmailNotificationHook&#41; response.getValue&#40;&#41;;
     * List&lt;String&gt; emailsToUpdate = new ArrayList&lt;&gt;&#40;emailHook.getEmailsToAlert&#40;&#41;&#41;;
     * emailsToUpdate.remove&#40;&quot;alertme&#64;alertme.com&quot;&#41;;
     * emailsToUpdate.add&#40;&quot;alertme2&#64;alertme.com&quot;&#41;;
     * emailsToUpdate.add&#40;&quot;alertme3&#64;alertme.com&quot;&#41;;
     * emailHook.setEmailsToAlert&#40;emailsToUpdate&#41;;
     * Response&lt;NotificationHook&gt; updateResponse
     *     = metricsAdvisorAdminClient.updateHookWithResponse&#40;emailHook, Context.NONE&#41;;
     * EmailNotificationHook updatedEmailHook = &#40;EmailNotificationHook&#41; updateResponse.getValue&#40;&#41;;
     * System.out.printf&#40;&quot;Email Hook Id: %s%n&quot;, updatedEmailHook.getId&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Email Hook Name: %s%n&quot;, updatedEmailHook.getName&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Email Hook Description: %s%n&quot;, updatedEmailHook.getDescription&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Email Hook External Link: %s%n&quot;, updatedEmailHook.getExternalLink&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Email Hook Emails: %s%n&quot;, String.join&#40;&quot;,&quot;,
     *     updatedEmailHook.getEmailsToAlert&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateHookWithResponse#NotificationHook-Context -->
     *
     * @param notificationHook The notificationHook to update.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} containing the updated {@link NotificationHook}.
     * @throws NullPointerException If {@code notificationHook.id} is null.
     * @throws IllegalArgumentException If {@code notificationHook.Id} does not conform to the UUID format
     * specification.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<NotificationHook> updateHookWithResponse(NotificationHook notificationHook, Context context) {
        return updateHookWithResponseSync(notificationHook, context);
    }

    Response<NotificationHook> updateHookWithResponseSync(NotificationHook notificationHook, Context context) {
        Objects.requireNonNull(notificationHook, "'notificationHook' cannot be null.");
        Objects.requireNonNull(notificationHook.getId(), "'notificationHook.id' cannot be null.");
        Response<HookInfo> response = service.updateHookWithResponse(UUID.fromString(notificationHook.getId()),
            HookTransforms.toInnerForUpdate(logger, notificationHook), context);
        Response<NotificationHook> hookResponse = getHookWithResponse(notificationHook.getId(), context);
        return new ResponseBase<Void, NotificationHook>(response.getRequest(), response.getStatusCode(),
            response.getHeaders(), hookResponse.getValue(), null);
    }

    /**
     * Delete a hook.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteHook#String -->
     * <pre>
     * final String emailHookId = &quot;f00853f1-6627-447f-bacf-8dccf2e86fed&quot;;
     * metricsAdvisorAdminClient.deleteHook&#40;emailHookId&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteHook#String -->
     *
     * @param hookId The hook unique id.
     *
     * @throws NullPointerException thrown if the {@code hookId} is null.
     * @throws IllegalArgumentException If {@code hookId} does not conform to the UUID format specification.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteHook(String hookId) {
        deleteHookWithResponse(hookId, Context.NONE);
    }

    /**
     * Delete a hook.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteHookWithResponse#String-Context -->
     * <pre>
     * final String emailHookId = &quot;f00853f1-6627-447f-bacf-8dccf2e86fed&quot;;
     * Response&lt;Void&gt; response
     *     = metricsAdvisorAdminClient.deleteHookWithResponse&#40;emailHookId, Context.NONE&#41;;
     * System.out.printf&#40;&quot;Response status code: %d%n&quot;, response.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteHookWithResponse#String-Context -->
     *
     * @param hookId The hook unique id.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response}.
     * @throws NullPointerException thrown if the {@code hookId} is null.
     * @throws IllegalArgumentException If {@code hookId} does not conform to the UUID format specification.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteHookWithResponse(String hookId, Context context) {
        return deleteHookWithResponseSync(hookId, context);
    }

    Response<Void> deleteHookWithResponseSync(String hookId, Context context) {
        Objects.requireNonNull(hookId, "hookId is required.");
        return service.deleteHookWithResponse(UUID.fromString(hookId), context);
    }

    /**
     * List information of hooks on the metrics advisor account.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listHooks -->
     * <pre>
     * PagedIterable&lt;NotificationHook&gt; hooks = metricsAdvisorAdminClient.listHooks&#40;&#41;;
     * for &#40;NotificationHook notificationHook : hooks&#41; &#123;
     *     if &#40;notificationHook instanceof EmailNotificationHook&#41; &#123;
     *         EmailNotificationHook emailHook = &#40;EmailNotificationHook&#41; notificationHook;
     *         System.out.printf&#40;&quot;Email Hook Id: %s%n&quot;, emailHook.getId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Email Hook Name: %s%n&quot;, emailHook.getName&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Email Hook Description: %s%n&quot;, emailHook.getDescription&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Email Hook External Link: %s%n&quot;, emailHook.getExternalLink&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Email Hook Emails: %s%n&quot;, String.join&#40;&quot;,&quot;, emailHook.getEmailsToAlert&#40;&#41;&#41;&#41;;
     *     &#125; else if &#40;notificationHook instanceof WebNotificationHook&#41; &#123;
     *         WebNotificationHook webHook = &#40;WebNotificationHook&#41; notificationHook;
     *         System.out.printf&#40;&quot;Web Hook Id: %s%n&quot;, webHook.getId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Web Hook Name: %s%n&quot;, webHook.getName&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Web Hook Description: %s%n&quot;, webHook.getDescription&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Web Hook External Link: %s%n&quot;, webHook.getExternalLink&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Web Hook Endpoint: %s%n&quot;, webHook.getEndpoint&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Web Hook Headers: %s%n&quot;, webHook.getHttpHeaders&#40;&#41;&#41;;
     *     &#125;
     * &#125;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listHooks -->
     *
     * @return A {@link PagedIterable} containing information of all the {@link NotificationHook} in the account.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<NotificationHook> listHooks() {
        return listHooks(new ListHookOptions(), Context.NONE);
    }

    /**
     * List information of hooks.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listHooks#ListHookOptions-Context -->
     * <pre>
     * ListHookOptions options = new ListHookOptions&#40;&#41;
     *     .setSkip&#40;100&#41;
     *     .setMaxPageSize&#40;20&#41;;
     * PagedIterable&lt;NotificationHook&gt; hooks = metricsAdvisorAdminClient.listHooks&#40;options, Context.NONE&#41;;
     * Stream&lt;PagedResponse&lt;NotificationHook&gt;&gt; hooksPageStream = hooks.streamByPage&#40;&#41;;
     * int[] pageCount = new int[1];
     * hooksPageStream.forEach&#40;hookPage -&gt; &#123;
     *     System.out.printf&#40;&quot;Page: %d%n&quot;, pageCount[0]++&#41;;
     *     for &#40;NotificationHook notificationHook : hookPage.getElements&#40;&#41;&#41; &#123;
     *         if &#40;notificationHook instanceof EmailNotificationHook&#41; &#123;
     *             EmailNotificationHook emailHook = &#40;EmailNotificationHook&#41; notificationHook;
     *             System.out.printf&#40;&quot;Email Hook Id: %s%n&quot;, emailHook.getId&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Email Hook Name: %s%n&quot;, emailHook.getName&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Email Hook Description: %s%n&quot;, emailHook.getDescription&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Email Hook External Link: %s%n&quot;, emailHook.getExternalLink&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Email Hook Emails: %s%n&quot;, String.join&#40;&quot;,&quot;,
     *                 emailHook.getEmailsToAlert&#40;&#41;&#41;&#41;;
     *             System.out.printf&#40;&quot;Email Hook Admins: %s%n&quot;, String.join&#40;&quot;,&quot;, emailHook.getAdmins&#40;&#41;&#41;&#41;;
     *         &#125; else if &#40;notificationHook instanceof WebNotificationHook&#41; &#123;
     *             WebNotificationHook webHook = &#40;WebNotificationHook&#41; notificationHook;
     *             System.out.printf&#40;&quot;Web Hook Id: %s%n&quot;, webHook.getId&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Web Hook Name: %s%n&quot;, webHook.getName&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Web Hook Description: %s%n&quot;, webHook.getDescription&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Web Hook External Link: %s%n&quot;, webHook.getExternalLink&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Web Hook Endpoint: %s%n&quot;, webHook.getEndpoint&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Web Hook Headers: %s%n&quot;, webHook.getHttpHeaders&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Web Hook Admins: %s%n&quot;, String.join&#40;&quot;,&quot;, webHook.getAdmins&#40;&#41;&#41;&#41;;
     *         &#125;
     *     &#125;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listHooks#ListHookOptions-Context -->
     *
     * @param options The additional parameters.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link PagedIterable} containing information of the {@link NotificationHook} resources.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<NotificationHook> listHooks(ListHookOptions options, Context context) {
        return listHooksSync(options, context);
    }

    PagedIterable<NotificationHook> listHooksSync(ListHookOptions options, Context context) {
        return new PagedIterable<>(() -> listHooksSinglePageSync(options, context),
            continuationToken -> listHooksNextPageSync(continuationToken, context));
    }

    private PagedResponse<NotificationHook> listHooksSinglePageSync(ListHookOptions options, Context context) {
        PagedResponse<HookInfo> response
            = service.listHooksSinglePage(options != null ? options.getHookNameFilter() : null,
                options != null ? options.getSkip() : null, options != null ? options.getMaxPageSize() : null, context);
        return HookTransforms.fromInnerPagedResponse(logger, response);
    }

    private PagedResponse<NotificationHook> listHooksNextPageSync(String nextPageLink, Context context) {
        if (CoreUtils.isNullOrEmpty(nextPageLink)) {
            return null;
        }
        PagedResponse<HookInfo> response = service.listHooksNextSinglePage(nextPageLink, context);
        return HookTransforms.fromInnerPagedResponse(logger, response);
    }

    /**
     * Create a configuration to trigger alert when anomalies are detected.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createAlertConfig#AnomalyAlertConfiguration -->
     * <pre>
     * String detectionConfigurationId1 = &quot;9ol48er30-6e6e-4391-b78f-b00dfee1e6f5&quot;;
     * String detectionConfigurationId2 = &quot;3e58er30-6e6e-4391-b78f-b00dfee1e6f5&quot;;
     * String hookId1 = &quot;5f48er30-6e6e-4391-b78f-b00dfee1e6f5&quot;;
     * String hookId2 = &quot;8i48er30-6e6e-4391-b78f-b00dfee1e6f5&quot;;
     *
     * final AnomalyAlertConfiguration anomalyAlertConfiguration
     *     = metricsAdvisorAdminClient.createAlertConfig&#40;
     *         new AnomalyAlertConfiguration&#40;&quot;My AnomalyAlert config name&quot;&#41;
     *         .setDescription&#40;&quot;alert config description&quot;&#41;
     *         .setMetricAlertConfigurations&#40;Arrays.asList&#40;
     *             new MetricAlertConfiguration&#40;detectionConfigurationId1,
     *                 MetricAnomalyAlertScope.forWholeSeries&#40;&#41;&#41;,
     *             new MetricAlertConfiguration&#40;detectionConfigurationId2,
     *                 MetricAnomalyAlertScope.forWholeSeries&#40;&#41;&#41;
     *                 .setAlertConditions&#40;new MetricAnomalyAlertConditions&#40;&#41;
     *                     .setSeverityRangeCondition&#40;new SeverityCondition&#40;AnomalySeverity.HIGH,
     *                         AnomalySeverity.HIGH&#41;&#41;&#41;&#41;&#41;
     *         .setCrossMetricsOperator&#40;MetricAlertConfigurationsOperator.AND&#41;
     *         .setHookIdsToAlert&#40;Arrays.asList&#40;hookId1, hookId2&#41;&#41;&#41;;
     *
     * System.out.printf&#40;&quot;DataPoint Anomaly alert configuration Id: %s%n&quot;,
     *     anomalyAlertConfiguration.getId&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;DataPoint Anomaly alert configuration description: %s%n&quot;,
     *     anomalyAlertConfiguration.getDescription&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;DataPoint Anomaly alert configuration hook ids: %s%n&quot;,
     *     anomalyAlertConfiguration.getHookIdsToAlert&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;DataPoint Anomaly alert configuration cross metrics operator: %s%n&quot;,
     *     anomalyAlertConfiguration.getCrossMetricsOperator&#40;&#41;.toString&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createAlertConfig#AnomalyAlertConfiguration -->
     *
     * @param alertConfiguration The anomaly alerting configuration.
     *
     * @return The {@link AnomalyAlertConfiguration} that was created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public AnomalyAlertConfiguration createAlertConfig(AnomalyAlertConfiguration alertConfiguration) {
        return createAlertConfigWithResponse(alertConfiguration, Context.NONE).getValue();
    }

    /**
     * Create a configuration to trigger alert when anomalies are detected.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createAlertConfigWithResponse#AnomalyAlertConfiguration-Context -->
     * <pre>
     *
     * String detectionConfigurationId1 = &quot;9ol48er30-6e6e-4391-b78f-b00dfee1e6f5&quot;;
     * String detectionConfigurationId2 = &quot;3e58er30-6e6e-4391-b78f-b00dfee1e6f5&quot;;
     * String hookId1 = &quot;5f48er30-6e6e-4391-b78f-b00dfee1e6f5&quot;;
     * String hookId2 = &quot;8i48er30-6e6e-4391-b78f-b00dfee1e6f5&quot;;
     *
     * final Response&lt;AnomalyAlertConfiguration&gt; alertConfigurationResponse
     *     = metricsAdvisorAdminClient.createAlertConfigWithResponse&#40;
     *         new AnomalyAlertConfiguration&#40;&quot;My AnomalyAlert config name&quot;&#41;
     *         .setDescription&#40;&quot;alert config description&quot;&#41;
     *         .setMetricAlertConfigurations&#40;Arrays.asList&#40;
     *             new MetricAlertConfiguration&#40;detectionConfigurationId1,
     *                 MetricAnomalyAlertScope.forWholeSeries&#40;&#41;&#41;,
     *             new MetricAlertConfiguration&#40;detectionConfigurationId2,
     *                 MetricAnomalyAlertScope.forWholeSeries&#40;&#41;&#41;
     *                 .setAlertConditions&#40;new MetricAnomalyAlertConditions&#40;&#41;
     *                     .setSeverityRangeCondition&#40;new SeverityCondition&#40;AnomalySeverity.HIGH,
     *                         AnomalySeverity.HIGH&#41;&#41;&#41;&#41;&#41;
     *         .setCrossMetricsOperator&#40;MetricAlertConfigurationsOperator.AND&#41;
     *         .setHookIdsToAlert&#40;Arrays.asList&#40;hookId1, hookId2&#41;&#41;, Context.NONE&#41;;
     *
     * System.out.printf&#40;&quot;DataPoint Anomaly alert creation operation status: %s%n&quot;,
     *     alertConfigurationResponse.getStatusCode&#40;&#41;&#41;;
     * final AnomalyAlertConfiguration anomalyAlertConfiguration = alertConfigurationResponse.getValue&#40;&#41;;
     * System.out.printf&#40;&quot;DataPoint Anomaly alert configuration Id: %s%n&quot;, anomalyAlertConfiguration.getId&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;DataPoint Anomaly alert configuration description: %s%n&quot;,
     *     anomalyAlertConfiguration.getDescription&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;DataPoint Anomaly alert configuration hook ids: %s%n&quot;,
     *     anomalyAlertConfiguration.getHookIdsToAlert&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;DataPoint Anomaly alert configuration cross metrics operator: %s%n&quot;,
     *     anomalyAlertConfiguration.getCrossMetricsOperator&#40;&#41;.toString&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createAlertConfigWithResponse#AnomalyAlertConfiguration-Context -->
     *
     * @param alertConfiguration The anomaly alerting configuration.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} containing the created {@link AnomalyAlertConfiguration}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<AnomalyAlertConfiguration>
        createAlertConfigWithResponse(AnomalyAlertConfiguration alertConfiguration, Context context) {
        return createAlertConfigWithResponseSync(alertConfiguration, context);
    }

    Response<AnomalyAlertConfiguration> createAlertConfigWithResponseSync(AnomalyAlertConfiguration alertConfiguration,
        Context context) {
        Objects.requireNonNull(alertConfiguration, "'alertConfiguration' is required.");
        if (CoreUtils.isNullOrEmpty(alertConfiguration.getMetricAlertConfigurations())) {
            throw logger.logExceptionAsError(
                new NullPointerException("'alertConfiguration.metricAnomalyAlertConfigurations' is required"));
        }
        if (alertConfiguration.getCrossMetricsOperator() == null
            && alertConfiguration.getMetricAlertConfigurations().size() > 1) {
            throw logger.logExceptionAsError(new IllegalArgumentException("crossMetricsOperator is required"
                + " when there are more than one metric level alert configuration."));
        }

        final AnomalyAlertingConfiguration innerAlertConfiguration
            = AlertConfigurationTransforms.toInnerForCreate(alertConfiguration);

        CreateAnomalyAlertingConfigurationResponse response
            = service.createAnomalyAlertingConfigurationWithResponse(innerAlertConfiguration, context);
        final String configurationId = parseOperationId(response.getDeserializedHeaders().getLocation());
        Response<AnomalyAlertConfiguration> getResponse = getAlertConfigWithResponse(configurationId, context);
        return new ResponseBase<Void, AnomalyAlertConfiguration>(response.getRequest(), response.getStatusCode(),
            response.getHeaders(), getResponse.getValue(), null);
    }

    /**
     * Get the anomaly alert configuration identified by {@code alertConfigurationId}.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getAlertConfig#String -->
     * <pre>
     * String alertConfigId = &quot;1p0f8er30-6e6e-4391-b78f-bpfdfee1e6f5&quot;;
     *
     * AnomalyAlertConfiguration anomalyAlertConfiguration
     *     = metricsAdvisorAdminClient.getAlertConfig&#40;alertConfigId&#41;;
     * System.out.printf&#40;&quot;DataPoint Anomaly alert configuration Id: %s%n&quot;,
     *     anomalyAlertConfiguration.getId&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;DataPoint Anomaly alert configuration description: %s%n&quot;,
     *     anomalyAlertConfiguration.getDescription&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;DataPoint Anomaly alert configuration hook ids: %s%n&quot;,
     *     anomalyAlertConfiguration.getHookIdsToAlert&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;DataPoint Anomaly alert configuration cross metrics operator: %s%n&quot;,
     *     anomalyAlertConfiguration.getCrossMetricsOperator&#40;&#41;.toString&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getAlertConfig#String -->
     *
     * @param alertConfigurationId The anomaly alert configuration id.
     *
     * @return The {@link AnomalyAlertConfiguration} identified by the given id.
     * @throws NullPointerException thrown if the {@code alertConfigurationId} is null.
     * @throws IllegalArgumentException If {@code alertConfigurationId} does not conform to the
     * UUID format specification.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public AnomalyAlertConfiguration getAlertConfig(String alertConfigurationId) {
        return getAlertConfigWithResponse(alertConfigurationId, Context.NONE).getValue();
    }

    /**
     * Get the anomaly alert configuration identified by {@code alertConfigurationId}.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getAlertConfigWithResponse#String-Context -->
     * <pre>
     * String alertConfigId = &quot;1p0f8er30-6e6e-4391-b78f-bpfdfee1e6f5&quot;;
     *
     * Response&lt;AnomalyAlertConfiguration&gt; alertConfigurationResponse
     *     = metricsAdvisorAdminClient.getAlertConfigWithResponse&#40;alertConfigId, Context.NONE&#41;;
     *
     * System.out.printf&#40;&quot;DataPoint Anomaly alert creation operation status: %s%n&quot;,
     *     alertConfigurationResponse.getStatusCode&#40;&#41;&#41;;
     * final AnomalyAlertConfiguration anomalyAlertConfiguration = alertConfigurationResponse.getValue&#40;&#41;;
     * System.out.printf&#40;&quot;DataPoint Anomaly alert configuration Id: %s%n&quot;, anomalyAlertConfiguration.getId&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;DataPoint Anomaly alert configuration description: %s%n&quot;,
     *     anomalyAlertConfiguration.getDescription&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;DataPoint Anomaly alert configuration hook ids: %s%n&quot;,
     *     anomalyAlertConfiguration.getHookIdsToAlert&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;DataPoint Anomaly alert configuration cross metrics operator: %s%n&quot;,
     *     anomalyAlertConfiguration.getCrossMetricsOperator&#40;&#41;.toString&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getAlertConfigWithResponse#String-Context -->
     *
     * @param alertConfigurationId The anomaly alert configuration id.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response response} containing the {@link AnomalyAlertConfiguration} identified by the given id.
     * @throws IllegalArgumentException If {@code alertConfigurationId} does not conform to the
     * UUID format specification.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<AnomalyAlertConfiguration> getAlertConfigWithResponse(String alertConfigurationId,
        Context context) {
        return getAlertConfigWithResponseSync(alertConfigurationId, context);
    }

    Response<AnomalyAlertConfiguration> getAlertConfigWithResponseSync(String alertConfigurationId, Context context) {
        Objects.requireNonNull(alertConfigurationId, "'alertConfigurationId' is required.");

        Response<AnomalyAlertingConfiguration> response
            = service.getAnomalyAlertingConfigurationWithResponse(UUID.fromString(alertConfigurationId), context);
        return new ResponseBase<Void, AnomalyAlertConfiguration>(response.getRequest(), response.getStatusCode(),
            response.getHeaders(), AlertConfigurationTransforms.fromInner(response.getValue()), null);
    }

    /**
     * Update anomaly alert configuration.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateAlertConfig#AnomalyAlertConfiguration -->
     * <pre>
     *
     * String alertConfigId = &quot;1p0f8er30-6e6e-4391-b78f-bpfdfee1e6f5&quot;;
     * String additionalHookId = &quot;2gh8er30-6e6e-4391-b78f-bpfdfee1e6f5&quot;;
     *
     * AnomalyAlertConfiguration existingAnomalyConfig
     *     = metricsAdvisorAdminClient.getAlertConfig&#40;alertConfigId&#41;;
     * List&lt;String&gt; hookIds = new ArrayList&lt;&gt;&#40;existingAnomalyConfig.getHookIdsToAlert&#40;&#41;&#41;;
     * hookIds.add&#40;additionalHookId&#41;;
     * final AnomalyAlertConfiguration updatedAnomalyAlertConfiguration
     *     = metricsAdvisorAdminClient.updateAlertConfig&#40;
     *     existingAnomalyConfig
     *         .setHookIdsToAlert&#40;hookIds&#41;
     *         .setDescription&#40;&quot;updated to add more hook ids&quot;&#41;
     * &#41;;
     *
     * System.out.printf&#40;&quot;Updated anomaly alert configuration Id: %s%n&quot;, updatedAnomalyAlertConfiguration.getId&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Updated anomaly alert configuration description: %s%n&quot;,
     *     updatedAnomalyAlertConfiguration.getDescription&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Updated anomaly alert configuration hook ids: %s%n&quot;,
     *     updatedAnomalyAlertConfiguration.getHookIdsToAlert&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateAlertConfig#AnomalyAlertConfiguration -->
     *
     * @param alertConfiguration The anomaly alert configuration to update.
     *
     * @return The {@link AnomalyAlertConfiguration} that was updated.
     * @throws NullPointerException thrown if {@code alertConfiguration} or
     * {@code alertConfiguration.metricAnomalyAlertConfigurations} is null or empty.
     * @throws IllegalArgumentException If {@code alertConfigurationId} does not conform to the
     * UUID format specification.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public AnomalyAlertConfiguration updateAlertConfig(AnomalyAlertConfiguration alertConfiguration) {
        return updateAlertConfigWithResponse(alertConfiguration, Context.NONE).getValue();
    }

    /**
     * Update anomaly alert configuration.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateAlertConfigWithResponse#AnomalyAlertConfiguration-Context -->
     * <pre>
     *
     * String alertConfigId = &quot;1p0f8er30-6e6e-4391-b78f-bpfdfee1e6f5&quot;;
     * String additionalHookId = &quot;2gh8er30-6e6e-4391-b78f-bpfdfee1e6f5&quot;;
     *
     * AnomalyAlertConfiguration existingAnomalyConfig
     *     = metricsAdvisorAdminClient.getAlertConfig&#40;alertConfigId&#41;;
     * List&lt;String&gt; hookIds = new ArrayList&lt;&gt;&#40;existingAnomalyConfig.getHookIdsToAlert&#40;&#41;&#41;;
     * hookIds.add&#40;additionalHookId&#41;;
     * final Response&lt;AnomalyAlertConfiguration&gt; alertConfigurationResponse
     *     = metricsAdvisorAdminClient.updateAlertConfigWithResponse&#40;
     *     existingAnomalyConfig
     *         .setHookIdsToAlert&#40;hookIds&#41;
     *         .setDescription&#40;&quot;updated to add more hook ids&quot;&#41;, Context.NONE&#41;;
     *
     * System.out.printf&#40;&quot;Update anomaly alert operation status: %s%n&quot;, alertConfigurationResponse.getStatusCode&#40;&#41;&#41;;
     * final AnomalyAlertConfiguration updatedAnomalyAlertConfiguration = alertConfigurationResponse.getValue&#40;&#41;;
     * System.out.printf&#40;&quot;Updated anomaly alert configuration Id: %s%n&quot;, updatedAnomalyAlertConfiguration.getId&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Updated anomaly alert configuration description: %s%n&quot;,
     *     updatedAnomalyAlertConfiguration.getDescription&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Updated anomaly alert configuration hook ids: %sf%n&quot;,
     *     updatedAnomalyAlertConfiguration.getHookIdsToAlert&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateAlertConfigWithResponse#AnomalyAlertConfiguration-Context -->
     *
     * @param alertConfiguration The anomaly alert configuration to update.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link Response} containing the {@link AnomalyAlertConfiguration} that was updated.
     * @throws NullPointerException thrown if {@code alertConfiguration} or
     * {@code alertConfiguration.metricAnomalyAlertConfigurations} is null or empty.
     * @throws IllegalArgumentException If {@code alertConfigurationId} does not conform to the
     * UUID format specification.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<AnomalyAlertConfiguration>
        updateAlertConfigWithResponse(AnomalyAlertConfiguration alertConfiguration, Context context) {
        return updateAlertConfigWithResponseSync(alertConfiguration, context);
    }

    Response<AnomalyAlertConfiguration> updateAlertConfigWithResponseSync(AnomalyAlertConfiguration alertConfiguration,
        Context context) {
        Objects.requireNonNull(alertConfiguration, "'alertConfiguration' is required");
        if (CoreUtils.isNullOrEmpty(alertConfiguration.getMetricAlertConfigurations())) {
            throw logger.logExceptionAsError(new NullPointerException(
                "'alertConfiguration.metricAnomalyAlertConfigurations' is required and cannot be empty"));
        }
        final AnomalyAlertingConfigurationPatch innerAlertConfiguration
            = AlertConfigurationTransforms.toInnerForUpdate(alertConfiguration);

        Response<AnomalyAlertingConfiguration> response = service.updateAnomalyAlertingConfigurationWithResponse(
            UUID.fromString(alertConfiguration.getId()), innerAlertConfiguration, context);
        Response<AnomalyAlertConfiguration> getResponse
            = getAlertConfigWithResponse(alertConfiguration.getId(), context);
        return new ResponseBase<Void, AnomalyAlertConfiguration>(response.getRequest(), response.getStatusCode(),
            response.getHeaders(), getResponse.getValue(), null);
    }

    /**
     * Deletes the anomaly alert configuration identified by {@code alertConfigurationId}.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteAlertConfig#String -->
     * <pre>
     * String alertConfigId = &quot;1p0f8er30-6e6e-4391-b78f-bpfdfee1e6f5&quot;;
     * metricsAdvisorAdminClient.deleteAlertConfig&#40;alertConfigId&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteAlertConfig#String -->
     *
     * @param alertConfigurationId The anomaly alert configuration id.
     * @throws NullPointerException thrown if the {@code alertConfigurationId} is null.
     * @throws IllegalArgumentException If {@code alertConfigurationId} does not conform to the
     * UUID format specification.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteAlertConfig(String alertConfigurationId) {
        deleteAlertConfigWithResponse(alertConfigurationId, Context.NONE);
    }

    /**
     * Deletes the anomaly alert configuration identified by {@code alertConfigurationId}.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteAlertConfigWithResponse#String-Context -->
     * <pre>
     * String alertConfigId = &quot;1p0f8er30-6e6e-4391-b78f-bpfdfee1e6f5&quot;;
     * final Response&lt;Void&gt; response =
     *     metricsAdvisorAdminClient.deleteAlertConfigWithResponse&#40;alertConfigId, Context.NONE&#41;;
     *
     * System.out.printf&#40;&quot;DataPoint Anomaly alert config delete operation status : %s%n&quot;, response.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteAlertConfigWithResponse#String-Context -->
     *
     * @param alertConfigurationId The anomaly alert configuration id.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A response containing status code and headers returned after the operation.
     * @throws NullPointerException thrown if the {@code alertConfigurationId} is null.
     * @throws IllegalArgumentException If {@code alertConfigurationId} does not conform to the
     * UUID format specification.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteAlertConfigWithResponse(String alertConfigurationId, Context context) {
        return deleteAlertConfigWithResponseSync(alertConfigurationId, context);
    }

    Response<Void> deleteAlertConfigWithResponseSync(String alertConfigurationId, Context context) {
        Objects.requireNonNull(alertConfigurationId, "'alertConfigurationId' is required.");

        return service.deleteAnomalyAlertingConfigurationWithResponse(UUID.fromString(alertConfigurationId), context);
    }

    /**
     * Fetch the anomaly alert configurations associated with a detection configuration.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listAlertConfigs#String-ListAnomalyAlertConfigsOptions -->
     * <pre>
     * String detectionConfigId = &quot;3rt98er30-6e6e-4391-b78f-bpfdfee1e6f5&quot;;
     * metricsAdvisorAdminClient.listAlertConfigs&#40;detectionConfigId, new ListAnomalyAlertConfigsOptions&#40;&#41;&#41;
     *     .forEach&#40;anomalyAlertConfiguration -&gt; &#123;
     *         System.out.printf&#40;&quot;DataPoint Anomaly alert configuration Id: %s%n&quot;, anomalyAlertConfiguration.getId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;DataPoint Anomaly alert configuration description: %s%n&quot;,
     *             anomalyAlertConfiguration.getDescription&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;DataPoint Anomaly alert configuration hook ids: %s%n&quot;,
     *             anomalyAlertConfiguration.getHookIdsToAlert&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;DataPoint Anomaly alert configuration cross metrics operator: %s%n&quot;,
     *             anomalyAlertConfiguration.getCrossMetricsOperator&#40;&#41;.toString&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listAlertConfigs#String-ListAnomalyAlertConfigsOptions -->
     *
     * @param detectionConfigurationId The id of the detection configuration.
     * @param options th e additional configurable options to specify when querying the result.
     *
     * @return A {@link PagedIterable} containing information of all the
     * {@link AnomalyAlertConfiguration anomaly alert configurations} for the specified detection configuration.
     * @throws NullPointerException thrown if the {@code detectionConfigurationId} is null.
     * @throws IllegalArgumentException If {@code detectionConfigurationId} does not conform to the
     * UUID format specification.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<AnomalyAlertConfiguration> listAlertConfigs(String detectionConfigurationId,
        ListAnomalyAlertConfigsOptions options) {
        return listAlertConfigs(detectionConfigurationId, options, Context.NONE);
    }

    /**
     * Fetch the anomaly alert configurations associated with a detection configuration.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listAlertConfigs#String-ListAnomalyAlertConfigsOptions-Context -->
     * <pre>
     * String detectionConfigId = &quot;3rt98er30-6e6e-4391-b78f-bpfdfee1e6f5&quot;;
     * metricsAdvisorAdminClient.listAlertConfigs&#40;detectionConfigId,
     *     new ListAnomalyAlertConfigsOptions&#40;&#41;, Context.NONE&#41;
     *     .forEach&#40;anomalyAlertConfiguration -&gt; &#123;
     *         System.out.printf&#40;&quot;DataPoint Anomaly alert configuration Id: %s%n&quot;, anomalyAlertConfiguration.getId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;DataPoint Anomaly alert configuration description: %s%n&quot;,
     *             anomalyAlertConfiguration.getDescription&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;DataPoint Anomaly alert configuration hook ids: %s%n&quot;,
     *             anomalyAlertConfiguration.getHookIdsToAlert&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;DataPoint Anomaly alert configuration cross metrics operator: %s%n&quot;,
     *             anomalyAlertConfiguration.getCrossMetricsOperator&#40;&#41;.toString&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listAlertConfigs#String-ListAnomalyAlertConfigsOptions-Context -->
     *
     * @param detectionConfigurationId The id of the detection configuration.
     * @param options th e additional configurable options to specify when querying the result.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link PagedIterable} containing information of all the
     * {@link AnomalyAlertConfiguration anomaly alert configurations} for the specified detection configuration.
     * @throws NullPointerException thrown if the {@code detectionConfigurationId} is null.
     * @throws IllegalArgumentException If {@code detectionConfigurationId} does not conform to the
     * UUID format specification.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<AnomalyAlertConfiguration> listAlertConfigs(String detectionConfigurationId,
        ListAnomalyAlertConfigsOptions options, Context context) {
        return listAlertConfigsSync(detectionConfigurationId, options, context);
    }

    PagedIterable<AnomalyAlertConfiguration> listAlertConfigsSync(String detectionConfigurationId,
        ListAnomalyAlertConfigsOptions options, Context context) {
        return new PagedIterable<>(
            () -> listAnomalyAlertConfigsSinglePageSync(detectionConfigurationId, options, context),
            continuationToken -> listAnomalyAlertConfigsNextPageSync(continuationToken, context));
    }

    private PagedResponse<AnomalyAlertConfiguration> listAnomalyAlertConfigsSinglePageSync(
        String detectionConfigurationId, ListAnomalyAlertConfigsOptions options, Context context) {
        Objects.requireNonNull(detectionConfigurationId, "'detectionConfigurationId' is required.");
        if (options == null) {
            options = new ListAnomalyAlertConfigsOptions();
        }
        PagedResponse<AnomalyAlertingConfiguration> response
            = service.getAnomalyAlertingConfigurationsByAnomalyDetectionConfigurationSinglePage(
                UUID.fromString(detectionConfigurationId), options.getSkip(), options.getMaxPageSize(), context);
        return AlertConfigurationTransforms.fromInnerPagedResponse(response);
    }

    private PagedResponse<AnomalyAlertConfiguration> listAnomalyAlertConfigsNextPageSync(String nextPageLink,
        Context context) {
        if (CoreUtils.isNullOrEmpty(nextPageLink)) {
            return null;
        }
        PagedResponse<AnomalyAlertingConfiguration> response = service
            .getAnomalyAlertingConfigurationsByAnomalyDetectionConfigurationNextSinglePage(nextPageLink, context);
        return AlertConfigurationTransforms.fromInnerPagedResponse(response);
    }

    /**
     * Create a data source credential entity.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createDataSourceCredential#DatasourceCredentialEntity -->
     * <pre>
     * DataSourceCredentialEntity datasourceCredential;
     * final String name = &quot;sample_name&quot; + UUID.randomUUID&#40;&#41;;
     * final String cId = &quot;f45668b2-bffa-11eb-8529-0246ac130003&quot;;
     * final String tId = &quot;67890ded-5e07-4e52-b225-4ae8f905afb5&quot;;
     * final String mockSecret = &quot;890hy69-5e07-4e52-b225-4ae8f905afb5&quot;;
     *
     * datasourceCredential = new DataSourceServicePrincipalInKeyVault&#40;&#41;
     *     .setName&#40;name&#41;
     *     .setKeyVaultForDataSourceSecrets&#40;&quot;kv&quot;, cId, mockSecret&#41;
     *     .setTenantId&#40;tId&#41;
     *     .setSecretNameForDataSourceClientId&#40;&quot;DSClientID_1&quot;&#41;
     *     .setSecretNameForDataSourceClientSecret&#40;&quot;DSClientSer_1&quot;&#41;;
     *
     * DataSourceCredentialEntity credentialEntity =
     *     metricsAdvisorAdminClient.createDataSourceCredential&#40;datasourceCredential&#41;;
     * if &#40;credentialEntity instanceof DataSourceServicePrincipalInKeyVault&#41; &#123;
     *     DataSourceServicePrincipalInKeyVault actualCredentialSPInKV
     *         = &#40;DataSourceServicePrincipalInKeyVault&#41; credentialEntity;
     *     System.out
     *         .printf&#40;&quot;Actual credential entity key vault endpoint: %s%n&quot;,
     *             actualCredentialSPInKV.getKeyVaultEndpoint&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Actual credential entity key vault client Id: %s%n&quot;,
     *         actualCredentialSPInKV.getKeyVaultClientId&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Actual credential entity key vault secret name for data source: %s%n&quot;,
     *         actualCredentialSPInKV.getSecretNameForDataSourceClientId&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Actual credential entity key vault secret for data source: %s%n&quot;,
     *         actualCredentialSPInKV.getSecretNameForDataSourceClientSecret&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createDataSourceCredential#DatasourceCredentialEntity -->
     *
     * @param dataSourceCredential The credential entity.
     * @return The created {@link DataSourceCredentialEntity}.
     * @throws NullPointerException thrown if the {@code credentialEntity} is null
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DataSourceCredentialEntity createDataSourceCredential(DataSourceCredentialEntity dataSourceCredential) {
        return createDataSourceCredentialWithResponse(dataSourceCredential, Context.NONE).getValue();
    }

    /**
     * Create a data source credential entity with REST response.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createDataSourceCredentialWithResponse#DatasourceCredentialEntity-Context -->
     * <pre>
     * DataSourceCredentialEntity datasourceCredential;
     * final String name = &quot;sample_name&quot; + UUID.randomUUID&#40;&#41;;
     * final String cId = &quot;f45668b2-bffa-11eb-8529-0246ac130003&quot;;
     * final String tId = &quot;67890ded-5e07-4e52-b225-4ae8f905afb5&quot;;
     * final String mockSecret = &quot;890hy69-5e07-4e52-b225-4ae8f905afb5&quot;;
     *
     * datasourceCredential = new DataSourceServicePrincipalInKeyVault&#40;&#41;
     *     .setName&#40;name&#41;
     *     .setKeyVaultForDataSourceSecrets&#40;&quot;kv&quot;, cId, mockSecret&#41;
     *     .setTenantId&#40;tId&#41;
     *     .setSecretNameForDataSourceClientId&#40;&quot;DSClientID_1&quot;&#41;
     *     .setSecretNameForDataSourceClientSecret&#40;&quot;DSClientSer_1&quot;&#41;;
     *
     * Response&lt;DataSourceCredentialEntity&gt; credentialEntityWithResponse =
     *     metricsAdvisorAdminClient.createDataSourceCredentialWithResponse&#40;datasourceCredential, Context.NONE&#41;;
     *
     * System.out.printf&#40;&quot;Credential Entity creation operation status: %s%n&quot;,
     *     credentialEntityWithResponse.getStatusCode&#40;&#41;&#41;;
     * if &#40;credentialEntityWithResponse.getValue&#40;&#41; instanceof DataSourceServicePrincipalInKeyVault&#41; &#123;
     *     DataSourceServicePrincipalInKeyVault actualCredentialSPInKV
     *         = &#40;DataSourceServicePrincipalInKeyVault&#41; credentialEntityWithResponse.getValue&#40;&#41;;
     *     System.out
     *         .printf&#40;&quot;Actual credential entity key vault endpoint: %s%n&quot;,
     *             actualCredentialSPInKV.getKeyVaultEndpoint&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Actual credential entity key vault client Id: %s%n&quot;,
     *         actualCredentialSPInKV.getKeyVaultClientId&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Actual credential entity key vault secret name for data source: %s%n&quot;,
     *         actualCredentialSPInKV.getSecretNameForDataSourceClientId&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Actual credential entity key vault secret for data source: %s%n&quot;,
     *         actualCredentialSPInKV.getSecretNameForDataSourceClientSecret&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.createDataSourceCredentialWithResponse#DatasourceCredentialEntity-Context -->
     *
     * @param dataSourceCredential The credential entity.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link Response} containing the created {@link DataSourceCredentialEntity}.
     * @throws NullPointerException thrown if the {@code credentialEntity} is null
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DataSourceCredentialEntity>
        createDataSourceCredentialWithResponse(DataSourceCredentialEntity dataSourceCredential, Context context) {
        return createDataSourceCredentialWithResponseSync(dataSourceCredential, context);
    }

    Response<DataSourceCredentialEntity>
        createDataSourceCredentialWithResponseSync(DataSourceCredentialEntity dataSourceCredential, Context context) {
        Objects.requireNonNull(dataSourceCredential, "dataSourceCredential is required");

        final DataSourceCredential innerDataSourceCredential
            = DataSourceCredentialEntityTransforms.toInnerForCreate(dataSourceCredential);
        CreateCredentialResponse response = service.createCredentialWithResponse(innerDataSourceCredential, context);

        final String credentialId = Utility.parseOperationId(response.getDeserializedHeaders().getLocation());
        Response<DataSourceCredentialEntity> configurationResponse
            = this.getDataSourceCredentialWithResponse(credentialId, context);
        return new ResponseBase<Void, DataSourceCredentialEntity>(response.getRequest(), response.getStatusCode(),
            response.getHeaders(), configurationResponse.getValue(), null);
    }

    /**
     * Get a data source credential entity by its id.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getDataSourceCredential#String -->
     * <pre>
     * final String datasourceCredentialId = &quot;f45668b2-bffa-11eb-8529-0246ac130003&quot;;
     *
     * DataSourceCredentialEntity credentialEntity =
     *     metricsAdvisorAdminClient.getDataSourceCredential&#40;datasourceCredentialId&#41;;
     * if &#40;credentialEntity instanceof DataSourceServicePrincipalInKeyVault&#41; &#123;
     *     DataSourceServicePrincipalInKeyVault actualCredentialSPInKV
     *         = &#40;DataSourceServicePrincipalInKeyVault&#41; credentialEntity;
     *     System.out
     *         .printf&#40;&quot;Actual credential entity key vault endpoint: %s%n&quot;,
     *             actualCredentialSPInKV.getKeyVaultEndpoint&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Actual credential entity key vault client Id: %s%n&quot;,
     *         actualCredentialSPInKV.getKeyVaultClientId&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Actual credential entity key vault secret name for data source: %s%n&quot;,
     *         actualCredentialSPInKV.getSecretNameForDataSourceClientId&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Actual credential entity key vault secret for data source: %s%n&quot;,
     *         actualCredentialSPInKV.getSecretNameForDataSourceClientSecret&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getDataSourceCredential#String -->
     *
     * @param credentialId The data source credential entity unique id.
     *
     * @return The data source credential entity for the provided id.
     * @throws IllegalArgumentException If {@code credentialId} does not conform to the UUID format specification.
     * @throws NullPointerException thrown if the {@code credentialId} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DataSourceCredentialEntity getDataSourceCredential(String credentialId) {
        return getDataSourceCredentialWithResponse(credentialId, Context.NONE).getValue();
    }

    /**
     * Get a data source credential entity by its id with REST response.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getDataSourceCredentialWithResponse#String-Context -->
     * <pre>
     * final String datasourceCredentialId = &quot;f45668b2-bffa-11eb-8529-0246ac130003&quot;;
     *
     * Response&lt;DataSourceCredentialEntity&gt; credentialEntityWithResponse =
     *     metricsAdvisorAdminClient.getDataSourceCredentialWithResponse&#40;datasourceCredentialId, Context.NONE&#41;;
     * System.out.printf&#40;&quot;Credential Entity creation operation status: %s%n&quot;,
     *     credentialEntityWithResponse.getStatusCode&#40;&#41;&#41;;
     * if &#40;credentialEntityWithResponse.getValue&#40;&#41; instanceof DataSourceServicePrincipalInKeyVault&#41; &#123;
     *     DataSourceServicePrincipalInKeyVault actualCredentialSPInKV
     *         = &#40;DataSourceServicePrincipalInKeyVault&#41; credentialEntityWithResponse.getValue&#40;&#41;;
     *     System.out
     *         .printf&#40;&quot;Actual credential entity key vault endpoint: %s%n&quot;,
     *             actualCredentialSPInKV.getKeyVaultEndpoint&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Actual credential entity key vault client Id: %s%n&quot;,
     *         actualCredentialSPInKV.getKeyVaultClientId&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Actual credential entity key vault secret name for data source: %s%n&quot;,
     *         actualCredentialSPInKV.getSecretNameForDataSourceClientId&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Actual credential entity key vault secret for data source: %s%n&quot;,
     *         actualCredentialSPInKV.getSecretNameForDataSourceClientSecret&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.getDataSourceCredentialWithResponse#String-Context -->
     *
     * @param credentialId The data source credential entity unique id.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return The data feed for the provided id.
     * @throws IllegalArgumentException If {@code dataFeedId} does not conform to the UUID format specification.
     * @throws NullPointerException thrown if the {@code dataFeedId} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DataSourceCredentialEntity> getDataSourceCredentialWithResponse(String credentialId,
        Context context) {
        return getDataSourceCredentialWithResponseSync(credentialId, context);
    }

    Response<DataSourceCredentialEntity> getDataSourceCredentialWithResponseSync(String credentialId, Context context) {
        Objects.requireNonNull(credentialId, "'credentialId' cannot be null.");

        Response<DataSourceCredential> response
            = service.getCredentialWithResponse(UUID.fromString(credentialId), context);
        return new SimpleResponse<>(response, DataSourceCredentialEntityTransforms.fromInner(response.getValue()));
    }

    /**
     * Update a data source credential entity.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateDataSourceCredential#DatasourceCredentialEntity -->
     * <pre>
     * final String datasourceCredentialId = &quot;f45668b2-bffa-11eb-8529-0246ac130003&quot;;
     * DataSourceCredentialEntity existingDatasourceCredential =
     *     metricsAdvisorAdminClient.getDataSourceCredential&#40;datasourceCredentialId&#41;;
     * DataSourceServicePrincipalInKeyVault actualCredentialSPInKV = null;
     * if &#40;existingDatasourceCredential instanceof DataSourceServicePrincipalInKeyVault&#41; &#123;
     *     actualCredentialSPInKV  = &#40;DataSourceServicePrincipalInKeyVault&#41; existingDatasourceCredential;
     * &#125;
     *
     * DataSourceCredentialEntity credentialEntity =
     *     metricsAdvisorAdminClient.updateDataSourceCredential&#40;
     *         actualCredentialSPInKV.setDescription&#40;&quot;set updated description&quot;&#41;&#41;;
     *
     * if &#40;credentialEntity instanceof DataSourceServicePrincipalInKeyVault&#41; &#123;
     *     DataSourceServicePrincipalInKeyVault updatedCredentialSPInKV
     *         = &#40;DataSourceServicePrincipalInKeyVault&#41; credentialEntity;
     *     System.out.printf&#40;&quot;Actual credential entity key vault endpoint: %s%n&quot;,
     *         updatedCredentialSPInKV.getKeyVaultEndpoint&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Actual credential entity key vault updated description: %s%n&quot;,
     *         updatedCredentialSPInKV.getDescription&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateDataSourceCredential#DatasourceCredentialEntity -->
     *
     * @param dataSourceCredential The credential entity.
     *
     * @return The updated {@link DataSourceCredentialEntity}.
     * @throws NullPointerException thrown if the {@code credentialEntity} is null
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DataSourceCredentialEntity updateDataSourceCredential(DataSourceCredentialEntity dataSourceCredential) {
        return updateDataSourceCredentialWithResponse(dataSourceCredential, Context.NONE).getValue();
    }

    /**
     * Update a data source credential entity.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateDataSourceCredentialWithResponse#DatasourceCredentialEntity-Context -->
     * <pre>
     * final String datasourceCredentialId = &quot;f45668b2-bffa-11eb-8529-0246ac130003&quot;;
     * DataSourceCredentialEntity existingDatasourceCredential =
     *     metricsAdvisorAdminClient.getDataSourceCredential&#40;datasourceCredentialId&#41;;
     * DataSourceServicePrincipalInKeyVault actualCredentialSPInKV = null;
     * if &#40;existingDatasourceCredential instanceof DataSourceServicePrincipalInKeyVault&#41; &#123;
     *     actualCredentialSPInKV  = &#40;DataSourceServicePrincipalInKeyVault&#41; existingDatasourceCredential;
     * &#125;
     * Response&lt;DataSourceCredentialEntity&gt; credentialEntityWithResponse =
     *     metricsAdvisorAdminClient.updateDataSourceCredentialWithResponse&#40;
     *         actualCredentialSPInKV.setDescription&#40;&quot;set updated description&quot;&#41;, Context.NONE&#41;;
     *
     * System.out.printf&#40;&quot;Credential Entity creation operation status: %s%n&quot;,
     *     credentialEntityWithResponse.getStatusCode&#40;&#41;&#41;;
     * if &#40;credentialEntityWithResponse.getValue&#40;&#41; instanceof DataSourceServicePrincipalInKeyVault&#41; &#123;
     *     DataSourceServicePrincipalInKeyVault updatedCredentialSPInKV
     *         = &#40;DataSourceServicePrincipalInKeyVault&#41; credentialEntityWithResponse.getValue&#40;&#41;;
     *     System.out.printf&#40;&quot;Actual credential entity key vault endpoint: %s%n&quot;,
     *         updatedCredentialSPInKV.getKeyVaultEndpoint&#40;&#41;&#41;;
     *     System.out.printf&#40;&quot;Actual credential entity key vault updated description: %s%n&quot;,
     *         updatedCredentialSPInKV.getDescription&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.updateDataSourceCredentialWithResponse#DatasourceCredentialEntity-Context -->
     *
     * @param dataSourceCredential The credential entity.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link Response} containing the updated {@link DataSourceCredentialEntity}.
     * @throws NullPointerException thrown if the {@code credentialEntity} is null
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DataSourceCredentialEntity>
        updateDataSourceCredentialWithResponse(DataSourceCredentialEntity dataSourceCredential, Context context) {
        return updateDataSourceCredentialWithResponseSync(dataSourceCredential, context);
    }

    Response<DataSourceCredentialEntity>
        updateDataSourceCredentialWithResponseSync(DataSourceCredentialEntity dataSourceCredential, Context context) {
        Objects.requireNonNull(dataSourceCredential, "dataSourceCredential is required");

        final DataSourceCredentialPatch innerDataSourceCredential
            = DataSourceCredentialEntityTransforms.toInnerForUpdate(dataSourceCredential);
        Response<DataSourceCredential> response = service.updateCredentialWithResponse(
            UUID.fromString(dataSourceCredential.getId()), innerDataSourceCredential, context);
        Response<DataSourceCredentialEntity> configurationResponse
            = getDataSourceCredentialWithResponse(dataSourceCredential.getId(), context);
        return new ResponseBase<Void, DataSourceCredentialEntity>(response.getRequest(), response.getStatusCode(),
            response.getHeaders(), configurationResponse.getValue(), null);
    }

    /**
     * Delete a data source credential entity.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteDataSourceCredential#String -->
     * <pre>
     * final String datasourceCredentialId = &quot;t00853f1-9080-447f-bacf-8dccf2e86f&quot;;
     * metricsAdvisorAdminClient.deleteDataFeed&#40;datasourceCredentialId&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteDataSourceCredential#String -->
     *
     * @param credentialId The data source credential entity unique id.
     *
     * @throws IllegalArgumentException If {@code credentialId} does not conform to the UUID format specification.
     * @throws NullPointerException thrown if the {@code credentialId} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteDataSourceCredential(String credentialId) {
        deleteDataSourceCredentialWithResponse(credentialId, Context.NONE);
    }

    /**
     * Delete a data source credential entity with REST response.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteDataSourceCredentialWithResponse#String-Context -->
     * <pre>
     * final String datasourceCredentialId = &quot;eh0854f1-8927-447f-bacf-8dccf2e86fwe&quot;;
     * Response&lt;Void&gt; response =
     *     metricsAdvisorAdminClient.deleteDataSourceCredentialWithResponse&#40;datasourceCredentialId, Context.NONE&#41;;
     * System.out.printf&#40;&quot;Datasource credential delete operation status : %s%n&quot;, response.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.deleteDataSourceCredentialWithResponse#String-Context -->
     *
     * @param credentialId The data source credential entity unique id.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return a REST Response.
     * @throws IllegalArgumentException If {@code dataFeedId} does not conform to the UUID format specification.
     * @throws NullPointerException thrown if the {@code dataFeedId} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteDataSourceCredentialWithResponse(String credentialId, Context context) {
        return deleteDataSourceCredentialWithResponseSync(credentialId, context);
    }

    Response<Void> deleteDataSourceCredentialWithResponseSync(String credentialId, Context context) {
        Objects.requireNonNull(credentialId, "'credentialId' is required.");

        return service.deleteCredentialWithResponse(UUID.fromString(credentialId), context);
    }

    /**
     * List information of all data source credential entities on the metrics advisor account.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listDataSourceCredentials -->
     * <pre>
     * metricsAdvisorAdminClient.listDataSourceCredentials&#40;&#41;
     *     .forEach&#40;datasourceCredentialEntity -&gt; &#123;
     *         if &#40;datasourceCredentialEntity instanceof DataSourceServicePrincipalInKeyVault&#41; &#123;
     *             DataSourceServicePrincipalInKeyVault actualCredentialSPInKV
     *                 = &#40;DataSourceServicePrincipalInKeyVault&#41; datasourceCredentialEntity;
     *             System.out
     *                 .printf&#40;&quot;Actual credential entity key vault endpoint: %s%n&quot;,
     *                     actualCredentialSPInKV.getKeyVaultEndpoint&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Actual credential entity key vault client Id: %s%n&quot;,
     *                 actualCredentialSPInKV.getKeyVaultClientId&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Actual credential entity key vault secret name for data source: %s%n&quot;,
     *                 actualCredentialSPInKV.getSecretNameForDataSourceClientId&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Actual credential entity key vault secret for data source: %s%n&quot;,
     *                 actualCredentialSPInKV.getSecretNameForDataSourceClientSecret&#40;&#41;&#41;;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listDataSourceCredentials -->
     *
     * @return A {@link PagedIterable} containing information of all the {@link DataSourceCredentialEntity}
     * in the account.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<DataSourceCredentialEntity> listDataSourceCredentials() {
        return listDataSourceCredentials(null, Context.NONE);
    }

    /**
     * List information of all data source credential entities on the metrics advisor account.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listDataSourceCredentials#ListCredentialEntityOptions-Context -->
     * <pre>
     * metricsAdvisorAdminClient.listDataSourceCredentials&#40;
     *         new ListCredentialEntityOptions&#40;&#41;
     *             .setMaxPageSize&#40;3&#41;,
     *         Context.NONE&#41;
     *     .forEach&#40;datasourceCredentialEntity -&gt; &#123;
     *         if &#40;datasourceCredentialEntity instanceof DataSourceServicePrincipalInKeyVault&#41; &#123;
     *             DataSourceServicePrincipalInKeyVault actualCredentialSPInKV
     *                 = &#40;DataSourceServicePrincipalInKeyVault&#41; datasourceCredentialEntity;
     *             System.out
     *                 .printf&#40;&quot;Actual credential entity key vault endpoint: %s%n&quot;,
     *                     actualCredentialSPInKV.getKeyVaultEndpoint&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Actual credential entity key vault client Id: %s%n&quot;,
     *                 actualCredentialSPInKV.getKeyVaultClientId&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Actual credential entity key vault secret name for data source: %s%n&quot;,
     *                 actualCredentialSPInKV.getSecretNameForDataSourceClientId&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Actual credential entity key vault secret for data source: %s%n&quot;,
     *                 actualCredentialSPInKV.getSecretNameForDataSourceClientSecret&#40;&#41;&#41;;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient.listDataSourceCredentials#ListCredentialEntityOptions-Context -->
     *
     * @param options The configurable {@link ListCredentialEntityOptions options} to pass for filtering the output
     * result.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     *
     * @return A {@link PagedIterable} containing information of all the {@link DataSourceCredentialEntity}
     * in the account.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<DataSourceCredentialEntity> listDataSourceCredentials(ListCredentialEntityOptions options,
        Context context) {
        return listDataSourceCredentialsSync(options, context);
    }

    PagedIterable<DataSourceCredentialEntity> listDataSourceCredentialsSync(ListCredentialEntityOptions options,
        Context context) {
        return new PagedIterable<>(() -> listCredentialEntitiesSinglePageSync(options, context),
            continuationToken -> listCredentialEntitiesSNextPageSync(continuationToken, context));
    }

    private PagedResponse<DataSourceCredentialEntity>
        listCredentialEntitiesSinglePageSync(ListCredentialEntityOptions options, Context context) {
        options = options != null ? options : new ListCredentialEntityOptions();
        PagedResponse<DataSourceCredential> res
            = service.listCredentialsSinglePage(options.getSkip(), options.getMaxPageSize(), context);
        return new PagedResponseBase<>(res.getRequest(), res.getStatusCode(), res.getHeaders(),
            res.getValue().stream().map(DataSourceCredentialEntityTransforms::fromInner).collect(Collectors.toList()),
            res.getContinuationToken(), null);
    }

    private PagedResponse<DataSourceCredentialEntity> listCredentialEntitiesSNextPageSync(String nextPageLink,
        Context context) {
        if (CoreUtils.isNullOrEmpty(nextPageLink)) {
            return null;
        }
        PagedResponse<DataSourceCredential> res = service.listCredentialsNextSinglePage(nextPageLink, context);
        return new PagedResponseBase<>(res.getRequest(), res.getStatusCode(), res.getHeaders(),
            res.getValue().stream().map(DataSourceCredentialEntityTransforms::fromInner).collect(Collectors.toList()),
            res.getContinuationToken(), null);
    }
}
