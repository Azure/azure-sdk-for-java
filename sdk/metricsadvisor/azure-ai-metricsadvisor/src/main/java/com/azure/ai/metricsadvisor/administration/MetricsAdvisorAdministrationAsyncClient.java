// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration;

import com.azure.ai.metricsadvisor.MetricsAdvisorServiceVersion;
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
import com.azure.ai.metricsadvisor.implementation.AzureCognitiveServiceMetricsAdvisorRestAPIOpenAPIV2Impl;
import com.azure.ai.metricsadvisor.implementation.models.AnomalyAlertingConfiguration;
import com.azure.ai.metricsadvisor.implementation.models.AnomalyAlertingConfigurationPatch;
import com.azure.ai.metricsadvisor.implementation.models.AnomalyDetectionConfigurationPatch;
import com.azure.ai.metricsadvisor.implementation.models.DataSourceCredential;
import com.azure.ai.metricsadvisor.implementation.models.DataSourceCredentialPatch;
import com.azure.ai.metricsadvisor.implementation.models.DataSourceType;
import com.azure.ai.metricsadvisor.implementation.models.EntityStatus;
import com.azure.ai.metricsadvisor.implementation.models.FillMissingPointType;
import com.azure.ai.metricsadvisor.implementation.models.Granularity;
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
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.azure.ai.metricsadvisor.administration.models.DataFeedGranularityType.CUSTOM;
import static com.azure.ai.metricsadvisor.implementation.util.Utility.parseOperationId;
import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;

/**
 * This class provides an asynchronous client that contains all the operations that apply to Azure Metrics Advisor.
 * <p><strong>Instantiating a asynchronous Metrics Advisor Administration Client</strong></p>
 * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.instantiation -->
 * <pre>
 * MetricsAdvisorAdministrationAsyncClient metricsAdvisorAdminAsyncClient =
 *     new MetricsAdvisorAdministrationClientBuilder&#40;&#41;
 *         .credential&#40;new MetricsAdvisorKeyCredential&#40;&quot;&#123;subscription_key&#125;&quot;, &quot;&#123;api_key&#125;&quot;&#41;&#41;
 *         .endpoint&#40;&quot;&#123;endpoint&#125;&quot;&#41;
 *         .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.instantiation -->
 *
 * @see MetricsAdvisorAdministrationClientBuilder
 */
@ServiceClient(builder = MetricsAdvisorAdministrationClientBuilder.class, isAsync = true)
public final class MetricsAdvisorAdministrationAsyncClient {
    private static final String METRICS_ADVISOR_TRACING_NAMESPACE_VALUE = "Microsoft.CognitiveServices";
    private final ClientLogger logger = new ClientLogger(MetricsAdvisorAdministrationAsyncClient.class);
    private final AzureCognitiveServiceMetricsAdvisorRestAPIOpenAPIV2Impl service;

    /**
     * Create a {@link MetricsAdvisorAdministrationAsyncClient} that sends requests to the Metrics Advisor
     * service's endpoint. Each service call goes through the
     * {@link MetricsAdvisorAdministrationClientBuilder#pipeline(HttpPipeline)} http pipeline}.
     *
     * @param service The proxy service used to perform REST calls.
     * @param serviceVersion The versions of Azure Metrics Advisor supported by this client library.
     */
    MetricsAdvisorAdministrationAsyncClient(AzureCognitiveServiceMetricsAdvisorRestAPIOpenAPIV2Impl service,
        MetricsAdvisorServiceVersion serviceVersion) {
        this.service = service;
    }

    /**
     * Create a new data feed.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.createDataFeed#DataFeed -->
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
     *             Arrays.asList&#40;
     *                 new DataFeedDimension&#40;&quot;city&quot;&#41;,
     *                 new DataFeedDimension&#40;&quot;category&quot;&#41;
     *             &#41;&#41;
     *     &#41;
     *     .setIngestionSettings&#40;new DataFeedIngestionSettings&#40;OffsetDateTime.parse&#40;&quot;2020-01-01T00:00:00Z&quot;&#41;&#41;&#41;
     *     .setOptions&#40;new DataFeedOptions&#40;&#41;
     *     .setDescription&#40;&quot;data feed description&quot;&#41;
     *     .setRollupSettings&#40;new DataFeedRollupSettings&#40;&#41;
     *         .setRollupType&#40;DataFeedRollupType.AUTO_ROLLUP&#41;&#41;&#41;;
     *
     * metricsAdvisorAdminAsyncClient.createDataFeed&#40;dataFeed&#41;
     *     .subscribe&#40;createdDataFeed -&gt; &#123;
     *         System.out.printf&#40;&quot;Data feed Id: %s%n&quot;, createdDataFeed.getId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Data feed description: %s%n&quot;, createdDataFeed.getOptions&#40;&#41;.getDescription&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Data feed source type: %s%n&quot;, createdDataFeed.getSourceType&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Data feed creator: %s%n&quot;, createdDataFeed.getCreator&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.createDataFeed#DataFeed -->
     *
     * @param dataFeed The data feed to be created.
     * @return A {@link Mono} containing the created data feed.
     * @throws NullPointerException If {@code dataFeed}, {@code dataFeedName}, {@code dataFeedSource}, {@code metrics},
     * {@code granularityType} or {@code ingestionStartTime} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DataFeed> createDataFeed(DataFeed dataFeed) {
        return createDataFeedWithResponse(dataFeed).flatMap(FluxUtil::toMono);
    }

    /**
     * Create a new data feed with REST response.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.createDataFeedWithResponse#DataFeed -->
     * <pre>
     * DataFeed dataFeed = new DataFeed&#40;&#41;
     *     .setName&#40;&quot;dataFeedName&quot;&#41;
     *     .setSource&#40;new MySqlDataFeedSource&#40;&quot;conn-string&quot;, &quot;query&quot;&#41;&#41;
     *     .setGranularity&#40;new DataFeedGranularity&#40;&#41;.setGranularityType&#40;DataFeedGranularityType.DAILY&#41;&#41;
     *     .setSchema&#40;new DataFeedSchema&#40;
     *         Arrays.asList&#40;
     *             new DataFeedMetric&#40;&quot;metric1&quot;&#41;,
     *             new DataFeedMetric&#40;&quot;metric2&quot;&#41;
     *         &#41;
     *     &#41;&#41;
     *     .setIngestionSettings&#40;new DataFeedIngestionSettings&#40;OffsetDateTime.parse&#40;&quot;2020-01-01T00:00:00Z&quot;&#41;&#41;&#41;
     *     .setOptions&#40;new DataFeedOptions&#40;&#41;
     *         .setDescription&#40;&quot;data feed description&quot;&#41;
     *         .setRollupSettings&#40;new DataFeedRollupSettings&#40;&#41;
     *             .setRollupType&#40;DataFeedRollupType.AUTO_ROLLUP&#41;&#41;&#41;;
     *
     * metricsAdvisorAdminAsyncClient.createDataFeedWithResponse&#40;dataFeed&#41;
     *     .subscribe&#40;dataFeedResponse -&gt; &#123;
     *         System.out.printf&#40;&quot;Data feed create operation status: %s%n&quot;, dataFeedResponse.getStatusCode&#40;&#41;&#41;;
     *         DataFeed createdDataFeed = dataFeedResponse.getValue&#40;&#41;;
     *         System.out.printf&#40;&quot;Data feed Id: %s%n&quot;, createdDataFeed.getId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Data feed description: %s%n&quot;, createdDataFeed.getOptions&#40;&#41;.getDescription&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Data feed source type: %s%n&quot;, createdDataFeed.getSourceType&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Data feed creator: %s%n&quot;, createdDataFeed.getCreator&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.createDataFeedWithResponse#DataFeed -->
     *
     *
     * @param dataFeed The data feed to be created.
     * @return A {@link Response} of a {@link Mono} containing the created {@link DataFeed data feed}.
     * @throws NullPointerException If {@code dataFeed}, {@code dataFeedName}, {@code dataFeedSource}, {@code metrics},
     * {@code granularityType} or {@code ingestionStartTime} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DataFeed>> createDataFeedWithResponse(DataFeed dataFeed) {
        try {
            return withContext(context -> createDataFeedWithResponse(dataFeed, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<DataFeed>> createDataFeedWithResponse(DataFeed dataFeed, Context context) {
        Objects.requireNonNull(dataFeed, "'dataFeed' is required and cannot be null.");
        Objects.requireNonNull(dataFeed.getSource(), "'dataFeedSource' is required and cannot be null.");
        Objects.requireNonNull(dataFeed.getName(), "'dataFeedName' cannot be null or empty.");
        final DataFeedSchema dataFeedSchema = dataFeed.getSchema();
        final DataFeedGranularity dataFeedGranularity = dataFeed.getGranularity();
        final DataFeedIngestionSettings dataFeedIngestionSettings = dataFeed.getIngestionSettings();

        if (dataFeedSchema == null) {
            throw logger.logExceptionAsError(
                new NullPointerException("'dataFeedSchema.metrics' cannot be null or empty."));
        } else {
            Objects.requireNonNull(dataFeedSchema.getMetrics(),
                "'dataFeedSchema.metrics' cannot be null or empty.");
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
            throw logger.logExceptionAsError(
                new NullPointerException(
                    "'dataFeedIngestionSettings.ingestionStartTime' is required and cannot be null."));
        } else {
            Objects.requireNonNull(dataFeedIngestionSettings.getIngestionStartTime(),
                "'dataFeedIngestionSettings.ingestionStartTime' is required and cannot be null.");
        }

        final DataFeedOptions finalDataFeedOptions = dataFeed.getOptions() == null
            ? new DataFeedOptions() : dataFeed.getOptions();
        final DataFeedRollupSettings dataFeedRollupSettings = finalDataFeedOptions.getRollupSettings() == null
            ? new DataFeedRollupSettings() : finalDataFeedOptions.getRollupSettings();
        final DataFeedMissingDataPointFillSettings dataFeedMissingDataPointFillSettings =
            finalDataFeedOptions.getMissingDataPointFillSettings() == null
                ? new DataFeedMissingDataPointFillSettings() : finalDataFeedOptions.getMissingDataPointFillSettings();
        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, METRICS_ADVISOR_TRACING_NAMESPACE_VALUE);

        return service.createDataFeedWithResponseAsync(DataFeedTransforms.toDataFeedDetailSource(dataFeed.getSource())
                .setDataFeedName(dataFeed.getName())
                .setDataFeedDescription(finalDataFeedOptions.getDescription())
                .setGranularityName(Granularity.fromString(dataFeedGranularity.getGranularityType() == null
                    ? null : dataFeedGranularity.getGranularityType().toString()))
                .setGranularityAmount(dataFeedGranularity.getCustomGranularityValue())
                .setDimension(DataFeedTransforms.toInnerDimensionsListForCreate(dataFeedSchema.getDimensions()))
                .setMetrics(DataFeedTransforms.toInnerMetricsListForCreate(dataFeedSchema.getMetrics()))
                .setTimestampColumn(dataFeedSchema.getTimestampColumn())
                .setDataStartFrom(dataFeedIngestionSettings.getIngestionStartTime())
                .setStartOffsetInSeconds(dataFeedIngestionSettings.getIngestionStartOffset() == null
                    ? null : dataFeedIngestionSettings.getIngestionStartOffset().getSeconds())
                .setMaxConcurrency(dataFeedIngestionSettings.getDataSourceRequestConcurrency())
                .setStopRetryAfterInSeconds(dataFeedIngestionSettings.getStopRetryAfter() == null
                    ? null : dataFeedIngestionSettings.getStopRetryAfter().getSeconds())
                .setMinRetryIntervalInSeconds(dataFeedIngestionSettings.getIngestionRetryDelay() == null
                    ? null : dataFeedIngestionSettings.getIngestionRetryDelay().getSeconds())
                .setRollUpColumns(dataFeedRollupSettings.getAutoRollupGroupByColumnNames())
                .setRollUpMethod(RollUpMethod.fromString(dataFeedRollupSettings
                    .getDataFeedAutoRollUpMethod() == null
                    ? null : dataFeedRollupSettings.getDataFeedAutoRollUpMethod().toString()))
                .setNeedRollup(NeedRollupEnum.fromString(dataFeedRollupSettings.getRollupType() == null
                    ? null : dataFeedRollupSettings.getRollupType().toString()))
                .setAllUpIdentification(dataFeedRollupSettings.getRollupIdentificationValue())
                .setFillMissingPointType(FillMissingPointType.fromString(
                    dataFeedMissingDataPointFillSettings.getFillType() == null
                        ? null : dataFeedMissingDataPointFillSettings.getFillType().toString()))
                .setFillMissingPointValue(dataFeedMissingDataPointFillSettings.getCustomFillValue())
                .setViewMode(ViewMode.fromString(finalDataFeedOptions.getAccessMode() == null
                    ? null : finalDataFeedOptions.getAccessMode().toString()))
                .setViewers(finalDataFeedOptions.getViewers())
                .setAdmins(finalDataFeedOptions.getAdmins())
                .setActionLinkTemplate(finalDataFeedOptions.getActionLinkTemplate()), withTracing)
                .flatMap(createDataFeedResponse -> {
                    final String dataFeedId =
                        parseOperationId(createDataFeedResponse.getDeserializedHeaders().getLocation());
                    return getDataFeedWithResponse(dataFeedId);
                });
    }

    /**
     * Get a data feed by its id.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.getDataFeed#String -->
     * <pre>
     * final String dataFeedId = &quot;r47053f1-9080-09lo-bacf-8dccf2e86f&quot;;
     * metricsAdvisorAdminAsyncClient.getDataFeed&#40;dataFeedId&#41;
     *     .subscribe&#40;dataFeed -&gt; &#123;
     *         System.out.printf&#40;&quot;Data feed Id: %s%n&quot;, dataFeed.getId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Data feed description: %s%n&quot;, dataFeed.getOptions&#40;&#41;.getDescription&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Data feed source type: %s%n&quot;, dataFeed.getSourceType&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Data feed creator: %s%n&quot;, dataFeed.getCreator&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.getDataFeed#String -->
     *
     * @param dataFeedId The data feed unique id.
     *
     * @return The data feed for the provided id.
     * @throws IllegalArgumentException If {@code dataFeedId} does not conform to the UUID format specification.
     * @throws NullPointerException thrown if the {@code dataFeedId} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DataFeed> getDataFeed(String dataFeedId) {
        return getDataFeedWithResponse(dataFeedId).flatMap(FluxUtil::toMono);
    }

    /**
     * Get a data feed by its id with REST response.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.getDataFeedWithResponse#String -->
     * <pre>
     * final String dataFeedId = &quot;r47053f1-9080-09lo-bacf-8dccf2e86f&quot;;
     * metricsAdvisorAdminAsyncClient.getDataFeedWithResponse&#40;dataFeedId&#41;
     *     .subscribe&#40;dataFeedResponse -&gt; &#123;
     *         System.out.printf&#40;&quot;Data feed get operation status: %s%n&quot;, dataFeedResponse.getStatusCode&#40;&#41;&#41;;
     *         DataFeed dataFeed = dataFeedResponse.getValue&#40;&#41;;
     *         System.out.printf&#40;&quot;Data feed Id: %s%n&quot;, dataFeed.getId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Data feed description: %s%n&quot;, dataFeed.getOptions&#40;&#41;.getDescription&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Data feed source type: %s%n&quot;, dataFeed.getSourceType&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Data feed creator: %s%n&quot;, dataFeed.getCreator&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.getDataFeedWithResponse#String -->
     *
     * @param dataFeedId The data feed unique id.
     *
     * @return The data feed for the provided id.
     * @throws IllegalArgumentException If {@code dataFeedId} does not conform to the UUID format specification.
     * @throws NullPointerException thrown if the {@code dataFeedId} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DataFeed>> getDataFeedWithResponse(String dataFeedId) {
        try {
            return withContext(context -> getDataFeedWithResponse(dataFeedId, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<DataFeed>> getDataFeedWithResponse(String dataFeedId, Context context) {
        Objects.requireNonNull(dataFeedId, "'dataFeedId' cannot be null.");

        return service.getDataFeedByIdWithResponseAsync(UUID.fromString(dataFeedId), context)
            .map(response -> new SimpleResponse<>(response, DataFeedTransforms.fromInner(response.getValue())));
    }

    /**
     * Update an existing data feed.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.updateDataFeed#DataFeed -->
     * <pre>
     * final String dataFeedId = &quot;r47053f1-9080-09lo-bacf-8dccf2e86f&quot;;
     * metricsAdvisorAdminAsyncClient.getDataFeed&#40;dataFeedId&#41;
     *     .flatMap&#40;existingDataFeed -&gt; &#123;
     *         return metricsAdvisorAdminAsyncClient.updateDataFeed&#40;
     *             existingDataFeed
     *                .setOptions&#40;new DataFeedOptions&#40;&#41;
     *                    .setDescription&#40;&quot;set updated description&quot;&#41;&#41;
     *        &#41;;
     *     &#125;&#41;
     *     .subscribe&#40;updatedDataFeed -&gt; &#123;
     *         System.out.printf&#40;&quot;Data feed Id: %s%n&quot;, updatedDataFeed.getId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Data feed updated description: %s%n&quot;, updatedDataFeed.getOptions&#40;&#41;.getDescription&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.updateDataFeed#DataFeed -->
     *
     * @param dataFeed the data feed that needs to be updated.
     *
     * @return the updated data feed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DataFeed> updateDataFeed(DataFeed dataFeed) {
        return updateDataFeedWithResponse(dataFeed).flatMap(FluxUtil::toMono);
    }

    /**
     * Update an existing data feed with REST response.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.updateDataFeedWithResponse#DataFeed -->
     * <pre>
     * final String dataFeedId = &quot;r47053f1-9080-09lo-bacf-8dccf2e86f&quot;;
     * metricsAdvisorAdminAsyncClient.getDataFeed&#40;dataFeedId&#41;
     *     .flatMap&#40;existingDataFeed -&gt; &#123;
     *         return metricsAdvisorAdminAsyncClient.updateDataFeedWithResponse&#40;
     *             existingDataFeed
     *                 .setOptions&#40;new DataFeedOptions&#40;&#41;
     *                     .setDescription&#40;&quot;set updated description&quot;&#41;&#41;
     *         &#41;;
     *     &#125;&#41;
     *     .subscribe&#40;dataFeedResponse -&gt; &#123;
     *         System.out.printf&#40;&quot;Data feed update operation status: %s%n&quot;, dataFeedResponse.getStatusCode&#40;&#41;&#41;;
     *         DataFeed updatedDataFeed = dataFeedResponse.getValue&#40;&#41;;
     *         System.out.printf&#40;&quot;Data feed Id: %s%n&quot;, updatedDataFeed.getId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Data feed updated description: %s%n&quot;, updatedDataFeed.getOptions&#40;&#41;.getDescription&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.updateDataFeedWithResponse#DataFeed -->
     *
     * @param dataFeed the data feed that needs to be updated.
     *
     * @return the {@link Response} of a {@link Mono} containing the updated {@link DataFeed data feed}.
     **/
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DataFeed>> updateDataFeedWithResponse(DataFeed dataFeed) {
        try {
            return withContext(context -> updateDataFeedWithResponse(dataFeed, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<DataFeed>> updateDataFeedWithResponse(DataFeed dataFeed, Context context) {

        final DataFeedIngestionSettings dataFeedIngestionSettings = dataFeed.getIngestionSettings();
        final DataFeedOptions dataFeedOptions = dataFeed.getOptions() == null
            ? new DataFeedOptions() : dataFeed.getOptions();
        final DataFeedRollupSettings dataFeedRollupSettings = dataFeedOptions.getRollupSettings() == null
            ? new DataFeedRollupSettings() : dataFeedOptions.getRollupSettings();
        final DataFeedMissingDataPointFillSettings dataFeedMissingDataPointFillSettings =
            dataFeedOptions.getMissingDataPointFillSettings() == null
                ? new DataFeedMissingDataPointFillSettings() : dataFeedOptions.getMissingDataPointFillSettings();
        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, METRICS_ADVISOR_TRACING_NAMESPACE_VALUE);

        return service.updateDataFeedWithResponseAsync(UUID.fromString(dataFeed.getId()),
            DataFeedTransforms.toInnerForUpdate(dataFeed.getSource())
                .setDataFeedName(dataFeed.getName())
                .setDataFeedDescription(dataFeedOptions.getDescription())
                .setTimestampColumn(dataFeed.getSchema() == null
                    ? null : dataFeed.getSchema().getTimestampColumn())
                .setDataStartFrom(dataFeed.getIngestionSettings().getIngestionStartTime())
                .setStartOffsetInSeconds(dataFeedIngestionSettings.getIngestionStartOffset() == null
                    ? null : dataFeedIngestionSettings.getIngestionStartOffset().getSeconds())
                .setMaxConcurrency(dataFeedIngestionSettings.getDataSourceRequestConcurrency())
                .setStopRetryAfterInSeconds(dataFeedIngestionSettings.getStopRetryAfter() == null
                    ? null : dataFeedIngestionSettings.getStopRetryAfter().getSeconds())
                .setMinRetryIntervalInSeconds(dataFeedIngestionSettings.getIngestionRetryDelay() == null
                    ? null : dataFeedIngestionSettings.getIngestionRetryDelay().getSeconds())
                .setNeedRollup(
                    dataFeedRollupSettings.getRollupType() != null
                        ? NeedRollupEnum.fromString(dataFeedRollupSettings.getRollupType().toString())
                        : null)
                .setRollUpColumns(dataFeedRollupSettings.getAutoRollupGroupByColumnNames())
                .setRollUpMethod(
                    dataFeedRollupSettings.getDataFeedAutoRollUpMethod() != null
                        ? RollUpMethod.fromString(
                        dataFeedRollupSettings.getDataFeedAutoRollUpMethod().toString())
                        : null)
                .setAllUpIdentification(dataFeedRollupSettings.getRollupIdentificationValue())
                .setFillMissingPointType(
                    dataFeedMissingDataPointFillSettings.getFillType() != null
                        ? FillMissingPointType.fromString(
                        dataFeedMissingDataPointFillSettings.getFillType().toString())
                        : null)
                .setFillMissingPointValue(
                    // For PATCH send 'fill-custom-value' over wire only for 'fill-custom-type'.
                    dataFeedMissingDataPointFillSettings.getFillType() == DataFeedMissingDataPointFillType.CUSTOM_VALUE
                        ? dataFeedMissingDataPointFillSettings.getCustomFillValue()
                        : null)
                .setViewMode(
                    dataFeedOptions.getAccessMode() != null
                        ? ViewMode.fromString(dataFeedOptions.getAccessMode().toString())
                        : null)
                .setViewers(dataFeedOptions.getViewers())
                .setAdmins(dataFeedOptions.getAdmins())
                .setStatus(
                    dataFeed.getStatus() != null
                        ? EntityStatus.fromString(dataFeed.getStatus().toString())
                        : null)
                .setActionLinkTemplate(dataFeedOptions.getActionLinkTemplate()), withTracing)
            .flatMap(updatedDataFeedResponse -> getDataFeedWithResponse(dataFeed.getId()));
    }

    /**
     * Delete a data feed.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.deleteDataFeed#String -->
     * <pre>
     * final String dataFeedId = &quot;t00853f1-9080-447f-bacf-8dccf2e86f&quot;;
     * metricsAdvisorAdminAsyncClient.deleteDataFeed&#40;dataFeedId&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.deleteDataFeed#String -->
     *
     * @param dataFeedId The data feed unique id.
     *
     * @return An empty Mono.
     * @throws IllegalArgumentException If {@code dataFeedId} does not conform to the UUID format specification.
     * @throws NullPointerException thrown if the {@code dataFeedId} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteDataFeed(String dataFeedId) {
        return deleteDataFeedWithResponse(dataFeedId).flatMap(FluxUtil::toMono);
    }

    /**
     * Delete a data feed with REST response.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.deleteDataFeedWithResponse#String -->
     * <pre>
     * final String dataFeedId = &quot;eh0854f1-8927-447f-bacf-8dccf2e86fwe&quot;;
     * metricsAdvisorAdminAsyncClient.deleteDataFeedWithResponse&#40;dataFeedId&#41;
     *     .subscribe&#40;response -&gt;
     *         System.out.printf&#40;&quot;Data feed delete operation status : %s%n&quot;, response.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.deleteDataFeedWithResponse#String -->
     *
     * @param dataFeedId The data feed unique id.
     *
     * @return A response containing status code and headers returned after the operation.
     * @throws IllegalArgumentException If {@code dataFeedId} does not conform to the UUID format specification.
     * @throws NullPointerException thrown if the {@code dataFeedId} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteDataFeedWithResponse(String dataFeedId) {
        try {
            return withContext(context -> deleteDataFeedWithResponse(dataFeedId, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> deleteDataFeedWithResponse(String dataFeedId, Context context) {
        Objects.requireNonNull(dataFeedId, "'dataFeedId' cannot be null.");
        return service.deleteDataFeedWithResponseAsync(UUID.fromString(dataFeedId), context)
            .map(response -> new SimpleResponse<>(response, null));
    }

    /**
     * List information of all data feeds on the metrics advisor account.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.listDataFeeds -->
     * <pre>
     * metricsAdvisorAdminAsyncClient.listDataFeeds&#40;&#41;
     *     .subscribe&#40;dataFeed -&gt; &#123;
     *         System.out.printf&#40;&quot;Data feed Id: %s%n&quot;, dataFeed.getId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Data feed description: %s%n&quot;, dataFeed.getOptions&#40;&#41;.getDescription&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Data feed source type: %s%n&quot;, dataFeed.getSourceType&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Data feed creator: %s%n&quot;, dataFeed.getCreator&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.listDataFeeds -->
     *
     * @return A {@link PagedFlux} containing information of all the {@link DataFeed data feeds} in the account.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<DataFeed> listDataFeeds() {
        return listDataFeeds(new ListDataFeedOptions());
    }

    /**
     * List information of all data feeds on the metrics advisor account.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.listDataFeeds#ListDataFeedOptions -->
     * <pre>
     * metricsAdvisorAdminAsyncClient.listDataFeeds&#40;
     *     new ListDataFeedOptions&#40;&#41;
     *         .setListDataFeedFilter&#40;
     *             new ListDataFeedFilter&#40;&#41;
     *                 .setDataFeedStatus&#40;DataFeedStatus.ACTIVE&#41;
     *                 .setDataFeedGranularityType&#40;DataFeedGranularityType.DAILY&#41;&#41;
     *         .setMaxPageSize&#40;3&#41;&#41;
     *     .subscribe&#40;dataFeed -&gt; &#123;
     *         System.out.printf&#40;&quot;Data feed Id: %s%n&quot;, dataFeed.getId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Data feed description: %s%n&quot;, dataFeed.getOptions&#40;&#41;.getDescription&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Data feed source type: %s%n&quot;, dataFeed.getSourceType&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Data feed creator: %s%n&quot;, dataFeed.getCreator&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Data feed status: %s%n&quot;, dataFeed.getStatus&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Data feed granularity type: %s%n&quot;, dataFeed.getGranularity&#40;&#41;.getGranularityType&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.listDataFeeds#ListDataFeedOptions -->
     *
     * @param listDataFeedOptions The configurable {@link ListDataFeedOptions options} to pass for filtering the output result.
     *
     * @return A {@link PagedFlux} containing information of all the {@link DataFeed data feeds} in the account.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<DataFeed> listDataFeeds(ListDataFeedOptions listDataFeedOptions) {
        try {
            return new PagedFlux<>(() ->
                withContext(context ->
                    listDataFeedsSinglePageAsync(listDataFeedOptions, context)),
                continuationToken ->
                    withContext(context -> listDataFeedsNextPageAsync(continuationToken, context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    PagedFlux<DataFeed> listDataFeeds(ListDataFeedOptions options, Context context) {
        return new PagedFlux<>(() ->
            listDataFeedsSinglePageAsync(options, context),
            continuationToken ->
                listDataFeedsNextPageAsync(continuationToken, context));
    }

    private Mono<PagedResponse<DataFeed>> listDataFeedsSinglePageAsync(ListDataFeedOptions options, Context context) {

        options = options != null ? options : new ListDataFeedOptions();
        final ListDataFeedFilter dataFeedFilter =
            options.getListDataFeedFilter() != null ? options.getListDataFeedFilter() : new ListDataFeedFilter();
        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, METRICS_ADVISOR_TRACING_NAMESPACE_VALUE);

        return service.listDataFeedsSinglePageAsync(dataFeedFilter.getName(),
            dataFeedFilter.getSourceType() != null
                ? DataSourceType.fromString(dataFeedFilter.getSourceType().toString()) : null,
            dataFeedFilter.getGranularityType() != null
                ? Granularity.fromString(dataFeedFilter.getGranularityType().toString()) : null,
            dataFeedFilter.getStatus() != null
                ? EntityStatus.fromString(dataFeedFilter.getStatus().toString()) : null,
            dataFeedFilter.getCreator(),
            options.getSkip(), options.getMaxPageSize(), withTracing)
            .doOnRequest(ignoredValue -> logger.info("Listing information for all data feeds"))
            .doOnSuccess(response -> logger.info("Listed data feeds {}", response))
            .doOnError(error -> logger.warning("Failed to list all data feeds information - {}", error))
            .map(res -> new PagedResponseBase<>(
                res.getRequest(),
                res.getStatusCode(),
                res.getHeaders(),
                res.getValue().stream().map(DataFeedTransforms::fromInner).collect(Collectors.toList()),
                res.getContinuationToken(),
                null));
    }

    private Mono<PagedResponse<DataFeed>> listDataFeedsNextPageAsync(String nextPageLink, Context context) {

        if (CoreUtils.isNullOrEmpty(nextPageLink)) {
            return Mono.empty();
        }
        return service.listDataFeedsNextSinglePageAsync(nextPageLink, context)
            .doOnSubscribe(ignoredValue -> logger.info("Retrieving the next listing page - Page {}", nextPageLink))
            .doOnSuccess(response -> logger.info("Retrieved the next listing page - Page {}", nextPageLink))
            .doOnError(error -> logger.warning("Failed to retrieve the next listing page - Page {}", nextPageLink,
                error))
            .map(res -> new PagedResponseBase<>(
                res.getRequest(),
                res.getStatusCode(),
                res.getHeaders(),
                res.getValue().stream().map(DataFeedTransforms::fromInner).collect(Collectors.toList()),
                res.getContinuationToken(),
                null));
    }

    /**
     * Fetch the ingestion status of a data feed.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.listDataFeedIngestionStatus#String-ListDataFeedIngestionOptions -->
     * <pre>
     * final String dataFeedId = &quot;4957a2f7-a0f4-4fc0-b8d7-d866c1df0f4c&quot;;
     * final OffsetDateTime startTime = OffsetDateTime.parse&#40;&quot;2020-01-01T00:00:00Z&quot;&#41;;
     * final OffsetDateTime endTime = OffsetDateTime.parse&#40;&quot;2020-09-09T00:00:00Z&quot;&#41;;
     * final ListDataFeedIngestionOptions options = new ListDataFeedIngestionOptions&#40;startTime, endTime&#41;;
     * metricsAdvisorAdminAsyncClient.listDataFeedIngestionStatus&#40;dataFeedId, options&#41;
     *     .subscribe&#40;ingestionStatus -&gt; &#123;
     *         System.out.printf&#40;&quot;Timestamp: %s%n&quot;, ingestionStatus.getTimestamp&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Status: %s%n&quot;, ingestionStatus.getStatus&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Message: %s%n&quot;, ingestionStatus.getMessage&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.listDataFeedIngestionStatus#String-ListDataFeedIngestionOptions -->
     *
     * @param dataFeedId The data feed id.
     * @param listDataFeedIngestionOptions The additional parameters.
     *
     * @return The ingestion statuses.
     * @throws IllegalArgumentException If {@code dataFeedId} does not conform to the UUID format specification.
     * @throws NullPointerException If {@code dataFeedId}, {@code options}, {@code options.startTime},
     * {@code options.endTime}  is null.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<DataFeedIngestionStatus> listDataFeedIngestionStatus(
        String dataFeedId,
        ListDataFeedIngestionOptions listDataFeedIngestionOptions) {
        try {
            return new PagedFlux<>(() ->
                withContext(context ->
                    listDataFeedIngestionStatusSinglePageAsync(dataFeedId, listDataFeedIngestionOptions, context)),
                continuationToken ->
                    withContext(context -> listDataFeedIngestionStatusNextPageAsync(continuationToken,
                        listDataFeedIngestionOptions,
                        context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> FluxUtil.monoError(logger, ex));
        }
    }

    PagedFlux<DataFeedIngestionStatus> listDataFeedIngestionStatus(
        String dataFeedId,
        ListDataFeedIngestionOptions options, Context context) {
        return new PagedFlux<>(() ->
            listDataFeedIngestionStatusSinglePageAsync(dataFeedId, options, context),
            continuationToken ->
                listDataFeedIngestionStatusNextPageAsync(continuationToken,
                    options,
                    context));
    }

    private Mono<PagedResponse<DataFeedIngestionStatus>> listDataFeedIngestionStatusSinglePageAsync(
        String dataFeedId,
        ListDataFeedIngestionOptions options, Context context) {
        Objects.requireNonNull(dataFeedId, "'dataFeedId' is required.");
        Objects.requireNonNull(options, "'options' is required.");
        Objects.requireNonNull(options.getStartTime(), "'options.startTime' is required.");
        Objects.requireNonNull(options.getEndTime(), "'options.endTime' is required.");

        IngestionStatusQueryOptions queryOptions = new IngestionStatusQueryOptions()
            .setStartTime(options.getStartTime())
            .setEndTime(options.getEndTime());

        return service.getDataFeedIngestionStatusSinglePageAsync(
            UUID.fromString(dataFeedId),
            queryOptions,
            options.getSkip(),
            options.getMaxPageSize(),
            context.addData(AZ_TRACING_NAMESPACE_KEY, METRICS_ADVISOR_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignoredValue -> logger.info("Listing ingestion status for data feed"))
            .doOnSuccess(response -> logger.info("Listed ingestion status {}", response))
            .doOnError(error -> logger.warning("Failed to ingestion status for data feed", error));
    }

    private Mono<PagedResponse<DataFeedIngestionStatus>> listDataFeedIngestionStatusNextPageAsync(
        String nextPageLink,
        ListDataFeedIngestionOptions options,
        Context context) {
        if (CoreUtils.isNullOrEmpty(nextPageLink)) {
            return Mono.empty();
        }

        IngestionStatusQueryOptions queryOptions = new IngestionStatusQueryOptions()
            .setStartTime(options.getStartTime())
            .setEndTime(options.getEndTime());

        return service.getDataFeedIngestionStatusNextSinglePageAsync(nextPageLink,
            queryOptions,
            context.addData(AZ_TRACING_NAMESPACE_KEY, METRICS_ADVISOR_TRACING_NAMESPACE_VALUE))
            .doOnSubscribe(ignoredValue -> logger.info("Retrieving the next listing page - Page {}", nextPageLink))
            .doOnSuccess(response -> logger.info("Retrieved the next listing page - Page {} {}",
                nextPageLink, response))
            .doOnError(error -> logger.warning("Failed to retrieve the next listing page - Page {}", nextPageLink,
                error));
    }

    /**
     * Refresh data ingestion for a period.
     * <p>
     * The data in the data source for the given period will be reingested
     * and any ingested data for the same period will be overwritten.
     * </p>
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.refreshDataFeedIngestion#String-OffsetDateTime-OffsetDateTime -->
     * <pre>
     * final String dataFeedId = &quot;4957a2f7-a0f4-4fc0-b8d7-d866c1df0f4c&quot;;
     * final OffsetDateTime startTime = OffsetDateTime.parse&#40;&quot;2020-01-01T00:00:00Z&quot;&#41;;
     * final OffsetDateTime endTime = OffsetDateTime.parse&#40;&quot;2020-03-03T00:00:00Z&quot;&#41;;
     * metricsAdvisorAdminAsyncClient.refreshDataFeedIngestion&#40;dataFeedId,
     *     startTime,
     *     endTime&#41;.subscribe&#40;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.refreshDataFeedIngestion#String-OffsetDateTime-OffsetDateTime -->
     *
     * @param dataFeedId The data feed id.
     * @param startTime The start point of the period.
     * @param endTime The end point of of the period.
     *
     * @return A {@link Mono} indicating ingestion reset success or failure.
     * @throws IllegalArgumentException If {@code dataFeedId} does not conform to the UUID format specification.
     * @throws NullPointerException If {@code dataFeedId}, {@code startTime}, {@code endTime}  is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> refreshDataFeedIngestion(
        String dataFeedId,
        OffsetDateTime startTime,
        OffsetDateTime endTime) {
        return refreshDataFeedIngestionWithResponse(dataFeedId,
            startTime,
            endTime).then();
    }

    /**
     * Refresh data ingestion for a period.
     * <p>
     * The data in the data source for the given period will be reingested
     * and any ingested data for the same period will be overwritten.
     * </p>
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.refreshDataFeedIngestionWithResponse#String-OffsetDateTime-OffsetDateTime -->
     * <pre>
     * final String dataFeedId = &quot;4957a2f7-a0f4-4fc0-b8d7-d866c1df0f4c&quot;;
     * final OffsetDateTime startTime = OffsetDateTime.parse&#40;&quot;2020-01-01T00:00:00Z&quot;&#41;;
     * final OffsetDateTime endTime = OffsetDateTime.parse&#40;&quot;2020-03-03T00:00:00Z&quot;&#41;;
     * metricsAdvisorAdminAsyncClient.refreshDataFeedIngestionWithResponse&#40;dataFeedId,
     *     startTime,
     *     endTime&#41;
     *     .subscribe&#40;response -&gt; &#123;
     *         System.out.printf&#40;&quot;Response statusCode: %d%n&quot;, response.getStatusCode&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.refreshDataFeedIngestionWithResponse#String-OffsetDateTime-OffsetDateTime -->
     *
     * @param dataFeedId The data feed id.
     * @param startTime The start point of the period.
     * @param endTime The end point of of the period.
     *
     * @return A {@link Response} of a {@link Mono} with result of reset request.
     * @throws IllegalArgumentException If {@code dataFeedId} does not conform to the UUID format specification.
     * @throws NullPointerException If {@code dataFeedId}, {@code startTime}, {@code endTime}  is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> refreshDataFeedIngestionWithResponse(
        String dataFeedId,
        OffsetDateTime startTime,
        OffsetDateTime endTime) {
        try {
            return withContext(context -> refreshDataFeedIngestionWithResponse(dataFeedId,
                startTime,
                endTime, context));
        } catch (RuntimeException e) {
            return FluxUtil.monoError(logger, e);
        }
    }

    Mono<Response<Void>> refreshDataFeedIngestionWithResponse(
        String dataFeedId,
        OffsetDateTime startTime,
        OffsetDateTime endTime, Context context) {
        Objects.requireNonNull(dataFeedId, "'dataFeedId' is required.");
        Objects.requireNonNull(startTime, "'startTime' is required.");
        Objects.requireNonNull(endTime, "'endTime' is required.");

        return service.resetDataFeedIngestionStatusWithResponseAsync(UUID.fromString(dataFeedId),
            new IngestionProgressResetOptions()
                .setStartTime(startTime)
                .setEndTime(endTime),
            context.addData(AZ_TRACING_NAMESPACE_KEY, METRICS_ADVISOR_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignoredValue -> logger.info("Resetting ingestion status for the data feed"))
            .doOnSuccess(response -> logger.info("Ingestion status got reset {}", response))
            .doOnError(error -> logger.warning("Failed to reset ingestion status for the data feed", error));
    }

    /**
     * Retrieve the ingestion progress of a data feed.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.getDataFeedIngestionProgress#String -->
     * <pre>
     * final String dataFeedId = &quot;4957a2f7-a0f4-4fc0-b8d7-d866c1df0f4c&quot;;
     * metricsAdvisorAdminAsyncClient.getDataFeedIngestionProgress&#40;dataFeedId&#41;
     *     .subscribe&#40;ingestionProgress -&gt; &#123;
     *         System.out.printf&#40;&quot;Latest active timestamp: %s%n&quot;, ingestionProgress.getLatestActiveTimestamp&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Latest successful timestamp: %s%n&quot;, ingestionProgress.getLatestSuccessTimestamp&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.getDataFeedIngestionProgress#String -->
     *
     * @param dataFeedId The data feed id.
     *
     * @return A {@link Mono} containing {@link DataFeedIngestionProgress} of the data feed.
     * @throws IllegalArgumentException If {@code dataFeedId} does not conform to the UUID format specification.
     * @throws NullPointerException thrown if the {@code dataFeedId} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DataFeedIngestionProgress> getDataFeedIngestionProgress(String dataFeedId) {
        return getDataFeedIngestionProgressWithResponse(dataFeedId, Context.NONE)
            .map(Response::getValue);
    }

    /**
     * Retrieve the ingestion progress of a data feed.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.getDataFeedIngestionProgressWithResponse#String -->
     * <pre>
     * final String dataFeedId = &quot;4957a2f7-a0f4-4fc0-b8d7-d866c1df0f4c&quot;;
     * metricsAdvisorAdminAsyncClient.getDataFeedIngestionProgressWithResponse&#40;dataFeedId, Context.NONE&#41;
     *     .subscribe&#40;response -&gt; &#123;
     *         System.out.printf&#40;&quot;Response statusCode: %d%n&quot;, response.getStatusCode&#40;&#41;&#41;;
     *         DataFeedIngestionProgress ingestionProgress = response.getValue&#40;&#41;;
     *         System.out.printf&#40;&quot;Latest active timestamp: %s%n&quot;, ingestionProgress.getLatestActiveTimestamp&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Latest successful timestamp: %s%n&quot;, ingestionProgress.getLatestSuccessTimestamp&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.getDataFeedIngestionProgressWithResponse#String -->
     *
     * @param dataFeedId The data feed id.
     *
     * @return A {@link Response} of a {@link Mono} containing {@link DataFeedIngestionProgress} of the data feed.
     * @throws IllegalArgumentException If {@code dataFeedId} does not conform to the UUID format specification.
     * @throws NullPointerException thrown if the {@code dataFeedId} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DataFeedIngestionProgress>> getDataFeedIngestionProgressWithResponse(String dataFeedId) {
        try {
            return withContext(context -> getDataFeedIngestionProgressWithResponse(dataFeedId, context));
        } catch (RuntimeException e) {
            return FluxUtil.monoError(logger, e);
        }
    }

    Mono<Response<DataFeedIngestionProgress>> getDataFeedIngestionProgressWithResponse(String dataFeedId,
                                                                                       Context context) {
        Objects.requireNonNull(dataFeedId, "'dataFeedId' is required.");
        return service.getIngestionProgressWithResponseAsync(UUID.fromString(dataFeedId),
            context.addData(AZ_TRACING_NAMESPACE_KEY, METRICS_ADVISOR_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignoredValue -> logger.info("Retrieving ingestion progress for metric"))
            .doOnSuccess(response -> logger.info("Retrieved ingestion progress {}", response))
            .doOnError(error -> logger.warning("Failed to retrieve ingestion progress for metric", error));
    }

    /**
     * Create a configuration to detect anomalies in the time series of a metric.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.createDetectionConfig#String-AnomalyDetectionConfiguration -->
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
     * metricsAdvisorAdminAsyncClient
     *     .createDetectionConfig&#40;metricId, detectionConfig&#41;
     *     .subscribe&#40;createdDetectionConfig -&gt; &#123;
     *         System.out.printf&#40;&quot;Detection config Id: %s%n&quot;, createdDetectionConfig.getId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Name: %s%n&quot;, createdDetectionConfig.getName&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Description: %s%n&quot;, createdDetectionConfig.getDescription&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;MetricId: %s%n&quot;, createdDetectionConfig.getMetricId&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.createDetectionConfig#String-AnomalyDetectionConfiguration -->
     *
     * @param metricId The metric id to associate the configuration with.
     * @param detectionConfiguration The anomaly detection configuration.
     * @return A {@link Mono} containing the created {@link AnomalyDetectionConfiguration}.
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
    public Mono<AnomalyDetectionConfiguration> createDetectionConfig(
        String metricId,
        AnomalyDetectionConfiguration detectionConfiguration) {
        return createDetectionConfigWithResponse(metricId, detectionConfiguration)
            .map(Response::getValue);
    }

    /**
     * Create a configuration to detect anomalies in the time series of a metric.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.createDetectionConfigWithResponse#String-AnomalyDetectionConfiguration -->
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
     * metricsAdvisorAdminAsyncClient
     *     .createDetectionConfigWithResponse&#40;metricId, detectionConfig&#41;
     *     .subscribe&#40;response -&gt; &#123;
     *         System.out.printf&#40;&quot;Response statusCode: %d%n&quot;, response.getStatusCode&#40;&#41;&#41;;
     *         AnomalyDetectionConfiguration createdDetectionConfig = response.getValue&#40;&#41;;
     *         System.out.printf&#40;&quot;Detection config Id: %s%n&quot;, createdDetectionConfig.getId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Name: %s%n&quot;, createdDetectionConfig.getName&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Description: %s%n&quot;, createdDetectionConfig.getDescription&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;MetricId: %s%n&quot;, createdDetectionConfig.getMetricId&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.createDetectionConfigWithResponse#String-AnomalyDetectionConfiguration -->
     *
     * @param metricId The metric id to associate the configuration with.
     * @param detectionConfiguration The anomaly detection configuration.
     * @return A {@link Response} of a {@link Mono} containing the created {@link AnomalyDetectionConfiguration}.
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
    public Mono<Response<AnomalyDetectionConfiguration>> createDetectionConfigWithResponse(
        String metricId,
        AnomalyDetectionConfiguration detectionConfiguration) {
        try {
            return withContext(context -> createDetectionConfigWithResponse(metricId,
                detectionConfiguration,
                context));
        } catch (RuntimeException e) {
            return FluxUtil.monoError(logger, e);
        }
    }

    Mono<Response<AnomalyDetectionConfiguration>> createDetectionConfigWithResponse(
        String metricId,
        AnomalyDetectionConfiguration detectionConfiguration,
        Context context) {
        Objects.requireNonNull(metricId, "metricId is required");
        Objects.requireNonNull(detectionConfiguration, "detectionConfiguration is required");

        final com.azure.ai.metricsadvisor.implementation.models.AnomalyDetectionConfiguration
            innerDetectionConfiguration = DetectionConfigurationTransforms.toInnerForCreate(logger,
            metricId,
            detectionConfiguration);
        return service.createAnomalyDetectionConfigurationWithResponseAsync(innerDetectionConfiguration,
            context.addData(AZ_TRACING_NAMESPACE_KEY, METRICS_ADVISOR_TRACING_NAMESPACE_VALUE))
            .doOnSubscribe(ignoredValue -> logger.info("Creating AnomalyDetectionConfiguration"))
            .doOnSuccess(response -> logger.info("Created AnomalyDetectionConfiguration"))
            .doOnError(error -> logger.warning("Failed to create AnomalyDetectionConfiguration", error))
            .flatMap(response -> {
                final String configurationId
                    = Utility.parseOperationId(response.getDeserializedHeaders().getLocation());
                return getDetectionConfigWithResponse(configurationId, context)
                    .map(configurationResponse -> new ResponseBase<Void, AnomalyDetectionConfiguration>(
                        response.getRequest(),
                        response.getStatusCode(),
                        response.getHeaders(),
                        configurationResponse.getValue(),
                        null));
            });
    }

    /**
     * Get the anomaly detection configuration by its id.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.getDetectionConfig#String -->
     * <pre>
     * final String detectionConfigId = &quot;7b8069a1-1564-46da-9f50-b5d0dd9129ab&quot;;
     * metricsAdvisorAdminAsyncClient
     *     .getDetectionConfig&#40;detectionConfigId&#41;
     *     .subscribe&#40;detectionConfig -&gt; &#123;
     *         System.out.printf&#40;&quot;Detection config Id: %s%n&quot;, detectionConfig.getId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Name: %s%n&quot;, detectionConfig.getName&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Description: %s%n&quot;, detectionConfig.getDescription&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;MetricId: %s%n&quot;, detectionConfig.getMetricId&#40;&#41;&#41;;
     *
     *         System.out.printf&#40;&quot;Detection conditions specified for configuration...%n&quot;&#41;;
     *
     *         System.out.printf&#40;&quot;Whole Series Detection Conditions:%n&quot;&#41;;
     *         MetricWholeSeriesDetectionCondition wholeSeriesDetectionCondition
     *             = detectionConfig.getWholeSeriesDetectionCondition&#40;&#41;;
     *
     *         System.out.printf&#40;&quot;- Use %s operator for multiple detection conditions:%n&quot;,
     *             wholeSeriesDetectionCondition.getConditionOperator&#40;&#41;&#41;;
     *
     *         System.out.printf&#40;&quot;- Smart Detection Condition:%n&quot;&#41;;
     *         System.out.printf&#40;&quot; - Sensitivity: %s%n&quot;,
     *             wholeSeriesDetectionCondition.getSmartDetectionCondition&#40;&#41;
     *                 .getSensitivity&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot; - Detection direction: %s%n&quot;,
     *             wholeSeriesDetectionCondition.getSmartDetectionCondition&#40;&#41;
     *                 .getAnomalyDetectorDirection&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot; - Suppress conditions: minimum number: %s; minimum ratio: %s%n&quot;,
     *             wholeSeriesDetectionCondition.getSmartDetectionCondition&#40;&#41;
     *                 .getSuppressCondition&#40;&#41;.getMinNumber&#40;&#41;,
     *             wholeSeriesDetectionCondition.getSmartDetectionCondition&#40;&#41;
     *                 .getSuppressCondition&#40;&#41;.getMinRatio&#40;&#41;&#41;;
     *
     *         System.out.printf&#40;&quot;- Hard Threshold Condition:%n&quot;&#41;;
     *         System.out.printf&#40;&quot; - Lower bound: %s%n&quot;,
     *             wholeSeriesDetectionCondition.getHardThresholdCondition&#40;&#41;
     *                 .getLowerBound&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot; - Upper bound: %s%n&quot;,
     *             wholeSeriesDetectionCondition.getHardThresholdCondition&#40;&#41;
     *                 .getUpperBound&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot; - Suppress conditions: minimum number: %s; minimum ratio: %s%n&quot;,
     *             wholeSeriesDetectionCondition.getHardThresholdCondition&#40;&#41;
     *                 .getSuppressCondition&#40;&#41;.getMinNumber&#40;&#41;,
     *             wholeSeriesDetectionCondition.getHardThresholdCondition&#40;&#41;
     *                 .getSuppressCondition&#40;&#41;.getMinRatio&#40;&#41;&#41;;
     *
     *         System.out.printf&#40;&quot;- Change Threshold Condition:%n&quot;&#41;;
     *         System.out.printf&#40;&quot; - Change percentage: %s%n&quot;,
     *             wholeSeriesDetectionCondition.getChangeThresholdCondition&#40;&#41;
     *                 .getChangePercentage&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot; - Shift point: %s%n&quot;,
     *             wholeSeriesDetectionCondition.getChangeThresholdCondition&#40;&#41;
     *                 .getShiftPoint&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot; - Detect anomaly if within range: %s%n&quot;,
     *             wholeSeriesDetectionCondition.getChangeThresholdCondition&#40;&#41;
     *                 .isWithinRange&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot; - Suppress conditions: minimum number: %s; minimum ratio: %s%n&quot;,
     *             wholeSeriesDetectionCondition.getChangeThresholdCondition&#40;&#41;
     *                 .getSuppressCondition&#40;&#41;.getMinNumber&#40;&#41;,
     *             wholeSeriesDetectionCondition.getChangeThresholdCondition&#40;&#41;
     *                 .getSuppressCondition&#40;&#41;.getMinRatio&#40;&#41;&#41;;
     *
     *         List&lt;MetricSingleSeriesDetectionCondition&gt; seriesDetectionConditions
     *             = detectionConfig.getSeriesDetectionConditions&#40;&#41;;
     *         System.out.printf&#40;&quot;Series Detection Conditions:%n&quot;&#41;;
     *         for &#40;MetricSingleSeriesDetectionCondition seriesDetectionCondition : seriesDetectionConditions&#41; &#123;
     *             DimensionKey seriesKey = seriesDetectionCondition.getSeriesKey&#40;&#41;;
     *             final String seriesKeyStr
     *                 = Arrays.toString&#40;seriesKey.asMap&#40;&#41;.entrySet&#40;&#41;.stream&#40;&#41;.toArray&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;- Series Key: %s%n&quot;, seriesKeyStr&#41;;
     *             System.out.printf&#40;&quot; - Use %s operator for multiple detection conditions:%n&quot;,
     *                 seriesDetectionCondition.getConditionOperator&#40;&#41;&#41;;
     *
     *             System.out.printf&#40;&quot; - Smart Detection Condition:%n&quot;&#41;;
     *             System.out.printf&#40;&quot;  - Sensitivity: %s%n&quot;,
     *                 seriesDetectionCondition.getSmartDetectionCondition&#40;&#41;
     *                     .getSensitivity&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;  - Detection direction: %s%n&quot;,
     *                 seriesDetectionCondition.getSmartDetectionCondition&#40;&#41;
     *                     .getAnomalyDetectorDirection&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;  - Suppress conditions: minimum number: %s; minimum ratio: %s%n&quot;,
     *                 seriesDetectionCondition.getSmartDetectionCondition&#40;&#41;
     *                     .getSuppressCondition&#40;&#41;.getMinNumber&#40;&#41;,
     *                 seriesDetectionCondition.getSmartDetectionCondition&#40;&#41;
     *                     .getSuppressCondition&#40;&#41;.getMinRatio&#40;&#41;&#41;;
     *
     *             System.out.printf&#40;&quot; - Hard Threshold Condition:%n&quot;&#41;;
     *             System.out.printf&#40;&quot;  -  Lower bound: %s%n&quot;,
     *                 seriesDetectionCondition.getHardThresholdCondition&#40;&#41;
     *                     .getLowerBound&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;  -  Upper bound: %s%n&quot;,
     *                 seriesDetectionCondition.getHardThresholdCondition&#40;&#41;
     *                     .getUpperBound&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;  -  Suppress conditions: minimum number: %s; minimum ratio: %s%n&quot;,
     *                 seriesDetectionCondition.getHardThresholdCondition&#40;&#41;
     *                     .getSuppressCondition&#40;&#41;.getMinNumber&#40;&#41;,
     *                 seriesDetectionCondition.getHardThresholdCondition&#40;&#41;
     *                     .getSuppressCondition&#40;&#41;.getMinRatio&#40;&#41;&#41;;
     *
     *             System.out.printf&#40;&quot; - Change Threshold Condition:%n&quot;&#41;;
     *             System.out.printf&#40;&quot;  -  Change percentage: %s%n&quot;,
     *                 seriesDetectionCondition.getChangeThresholdCondition&#40;&#41;
     *                     .getChangePercentage&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;  -  Shift point: %s%n&quot;,
     *                 seriesDetectionCondition.getChangeThresholdCondition&#40;&#41;
     *                     .getShiftPoint&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;  -  Detect anomaly if within range: %s%n&quot;,
     *                 seriesDetectionCondition.getChangeThresholdCondition&#40;&#41;
     *                     .isWithinRange&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;  -  Suppress conditions: minimum number: %s; minimum ratio: %s%n&quot;,
     *                 seriesDetectionCondition.getChangeThresholdCondition&#40;&#41;
     *                     .getSuppressCondition&#40;&#41;.getMinNumber&#40;&#41;,
     *                 seriesDetectionCondition.getChangeThresholdCondition&#40;&#41;
     *                     .getSuppressCondition&#40;&#41;.getMinRatio&#40;&#41;&#41;;
     *         &#125;
     *
     *         List&lt;MetricSeriesGroupDetectionCondition&gt; seriesGroupDetectionConditions
     *             = detectionConfig.getSeriesGroupDetectionConditions&#40;&#41;;
     *         System.out.printf&#40;&quot;Series Group Detection Conditions:%n&quot;&#41;;
     *         for &#40;MetricSeriesGroupDetectionCondition seriesGroupDetectionCondition
     *             : seriesGroupDetectionConditions&#41; &#123;
     *             DimensionKey seriesGroupKey = seriesGroupDetectionCondition.getSeriesGroupKey&#40;&#41;;
     *             final String seriesGroupKeyStr
     *                 = Arrays.toString&#40;seriesGroupKey.asMap&#40;&#41;.entrySet&#40;&#41;.stream&#40;&#41;.toArray&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;- Series Group Key: %s%n&quot;, seriesGroupKeyStr&#41;;
     *             System.out.printf&#40;&quot; - Use %s operator for multiple detection conditions:%n&quot;,
     *                 seriesGroupDetectionCondition.getConditionOperator&#40;&#41;&#41;;
     *
     *             System.out.printf&#40;&quot; - Smart Detection Condition:%n&quot;&#41;;
     *             System.out.printf&#40;&quot;  - Sensitivity: %s%n&quot;,
     *                 seriesGroupDetectionCondition.getSmartDetectionCondition&#40;&#41;
     *                     .getSensitivity&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;  - Detection direction: %s%n&quot;,
     *                 seriesGroupDetectionCondition.getSmartDetectionCondition&#40;&#41;
     *                     .getAnomalyDetectorDirection&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;  - Suppress conditions: minimum number: %s; minimum ratio: %s%n&quot;,
     *                 seriesGroupDetectionCondition.getSmartDetectionCondition&#40;&#41;
     *                     .getSuppressCondition&#40;&#41;.getMinNumber&#40;&#41;,
     *                 seriesGroupDetectionCondition.getSmartDetectionCondition&#40;&#41;
     *                     .getSuppressCondition&#40;&#41;.getMinRatio&#40;&#41;&#41;;
     *
     *             System.out.printf&#40;&quot; - Hard Threshold Condition:%n&quot;&#41;;
     *             System.out.printf&#40;&quot;  -  Lower bound: %s%n&quot;,
     *                 seriesGroupDetectionCondition.getHardThresholdCondition&#40;&#41;
     *                     .getLowerBound&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;  -  Upper bound: %s%n&quot;,
     *                 seriesGroupDetectionCondition.getHardThresholdCondition&#40;&#41;
     *                     .getUpperBound&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;  -  Suppress conditions: minimum number: %s; minimum ratio: %s%n&quot;,
     *                 seriesGroupDetectionCondition.getHardThresholdCondition&#40;&#41;
     *                     .getSuppressCondition&#40;&#41;.getMinNumber&#40;&#41;,
     *                 seriesGroupDetectionCondition.getHardThresholdCondition&#40;&#41;
     *                     .getSuppressCondition&#40;&#41;.getMinRatio&#40;&#41;&#41;;
     *
     *             System.out.printf&#40;&quot; - Change Threshold Condition:%n&quot;&#41;;
     *             System.out.printf&#40;&quot;  -  Change percentage: %s%n&quot;,
     *                 seriesGroupDetectionCondition.getChangeThresholdCondition&#40;&#41;
     *                     .getChangePercentage&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;  -  Shift point: %s%n&quot;,
     *                 seriesGroupDetectionCondition.getChangeThresholdCondition&#40;&#41;
     *                     .getShiftPoint&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;  -  Detect anomaly if within range: %s%n&quot;,
     *                 seriesGroupDetectionCondition.getChangeThresholdCondition&#40;&#41;
     *                     .isWithinRange&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;  -  Suppress conditions: minimum number: %s; minimum ratio: %s%n&quot;,
     *                 seriesGroupDetectionCondition.getChangeThresholdCondition&#40;&#41;
     *                     .getSuppressCondition&#40;&#41;.getMinNumber&#40;&#41;,
     *                 seriesGroupDetectionCondition.getChangeThresholdCondition&#40;&#41;
     *                     .getSuppressCondition&#40;&#41;.getMinRatio&#40;&#41;&#41;;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.getDetectionConfig#String -->
     *
     * @param detectionConfigurationId The anomaly detection configuration id.
     * @return A {@link Mono} containing the {@link AnomalyDetectionConfiguration} for the provided id.
     * @throws IllegalArgumentException If {@code detectionConfigurationId} does not conform to the UUID
     *     format specification.
     * @throws NullPointerException thrown if the {@code detectionConfigurationId} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<AnomalyDetectionConfiguration> getDetectionConfig(
        String detectionConfigurationId) {
        return getDetectionConfigWithResponse(detectionConfigurationId)
            .map(Response::getValue);
    }

    /**
     * Get the anomaly detection configuration by its id.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.getDetectionConfigWithResponse#String -->
     * <pre>
     * final String detectionConfigId = &quot;7b8069a1-1564-46da-9f50-b5d0dd9129ab&quot;;
     * metricsAdvisorAdminAsyncClient
     *     .getDetectionConfigWithResponse&#40;detectionConfigId&#41;
     *     .subscribe&#40;response -&gt; &#123;
     *         System.out.printf&#40;&quot;Response statusCode: %d%n&quot;, response.getStatusCode&#40;&#41;&#41;;
     *
     *         AnomalyDetectionConfiguration detectionConfig = response.getValue&#40;&#41;;
     *         System.out.printf&#40;&quot;Detection config Id: %s%n&quot;, detectionConfig.getId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Name: %s%n&quot;, detectionConfig.getName&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Description: %s%n&quot;, detectionConfig.getDescription&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;MetricId: %s%n&quot;, detectionConfig.getMetricId&#40;&#41;&#41;;
     *
     *         System.out.printf&#40;&quot;Detection conditions specified for configuration...%n&quot;&#41;;
     *
     *         System.out.printf&#40;&quot;Whole Series Detection Conditions:%n&quot;&#41;;
     *         MetricWholeSeriesDetectionCondition wholeSeriesDetectionCondition
     *             = detectionConfig.getWholeSeriesDetectionCondition&#40;&#41;;
     *
     *         System.out.printf&#40;&quot;- Use %s operator for multiple detection conditions:%n&quot;,
     *             wholeSeriesDetectionCondition.getConditionOperator&#40;&#41;&#41;;
     *
     *         System.out.printf&#40;&quot;- Smart Detection Condition:%n&quot;&#41;;
     *         System.out.printf&#40;&quot; - Sensitivity: %s%n&quot;,
     *             wholeSeriesDetectionCondition.getSmartDetectionCondition&#40;&#41;
     *                 .getSensitivity&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot; - Detection direction: %s%n&quot;,
     *             wholeSeriesDetectionCondition.getSmartDetectionCondition&#40;&#41;
     *                 .getAnomalyDetectorDirection&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot; - Suppress conditions: minimum number: %s; minimum ratio: %s%n&quot;,
     *             wholeSeriesDetectionCondition.getSmartDetectionCondition&#40;&#41;
     *                 .getSuppressCondition&#40;&#41;.getMinNumber&#40;&#41;,
     *             wholeSeriesDetectionCondition.getSmartDetectionCondition&#40;&#41;
     *                 .getSuppressCondition&#40;&#41;.getMinRatio&#40;&#41;&#41;;
     *
     *         System.out.printf&#40;&quot;- Hard Threshold Condition:%n&quot;&#41;;
     *         System.out.printf&#40;&quot; - Lower bound: %s%n&quot;,
     *             wholeSeriesDetectionCondition.getHardThresholdCondition&#40;&#41;
     *                 .getLowerBound&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot; - Upper bound: %s%n&quot;,
     *             wholeSeriesDetectionCondition.getHardThresholdCondition&#40;&#41;
     *                 .getUpperBound&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot; - Suppress conditions: minimum number: %s; minimum ratio: %s%n&quot;,
     *             wholeSeriesDetectionCondition.getHardThresholdCondition&#40;&#41;
     *                 .getSuppressCondition&#40;&#41;.getMinNumber&#40;&#41;,
     *             wholeSeriesDetectionCondition.getHardThresholdCondition&#40;&#41;
     *                 .getSuppressCondition&#40;&#41;.getMinRatio&#40;&#41;&#41;;
     *
     *         System.out.printf&#40;&quot;- Change Threshold Condition:%n&quot;&#41;;
     *         System.out.printf&#40;&quot; - Change percentage: %s%n&quot;,
     *             wholeSeriesDetectionCondition.getChangeThresholdCondition&#40;&#41;
     *                 .getChangePercentage&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot; - Shift point: %s%n&quot;,
     *             wholeSeriesDetectionCondition.getChangeThresholdCondition&#40;&#41;
     *                 .getShiftPoint&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot; - Detect anomaly if within range: %s%n&quot;,
     *             wholeSeriesDetectionCondition.getChangeThresholdCondition&#40;&#41;
     *                 .isWithinRange&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot; - Suppress conditions: minimum number: %s; minimum ratio: %s%n&quot;,
     *             wholeSeriesDetectionCondition.getChangeThresholdCondition&#40;&#41;
     *                 .getSuppressCondition&#40;&#41;.getMinNumber&#40;&#41;,
     *             wholeSeriesDetectionCondition.getChangeThresholdCondition&#40;&#41;
     *                 .getSuppressCondition&#40;&#41;.getMinRatio&#40;&#41;&#41;;
     *
     *         List&lt;MetricSingleSeriesDetectionCondition&gt; seriesDetectionConditions
     *             = detectionConfig.getSeriesDetectionConditions&#40;&#41;;
     *         System.out.printf&#40;&quot;Series Detection Conditions:%n&quot;&#41;;
     *         for &#40;MetricSingleSeriesDetectionCondition seriesDetectionCondition : seriesDetectionConditions&#41; &#123;
     *             DimensionKey seriesKey = seriesDetectionCondition.getSeriesKey&#40;&#41;;
     *             final String seriesKeyStr
     *                 = Arrays.toString&#40;seriesKey.asMap&#40;&#41;.entrySet&#40;&#41;.stream&#40;&#41;.toArray&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;- Series Key: %s%n&quot;, seriesKeyStr&#41;;
     *             System.out.printf&#40;&quot; - Use %s operator for multiple detection conditions:%n&quot;,
     *                 seriesDetectionCondition.getConditionOperator&#40;&#41;&#41;;
     *
     *             System.out.printf&#40;&quot; - Smart Detection Condition:%n&quot;&#41;;
     *             System.out.printf&#40;&quot;  - Sensitivity: %s%n&quot;,
     *                 seriesDetectionCondition.getSmartDetectionCondition&#40;&#41;
     *                     .getSensitivity&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;  - Detection direction: %s%n&quot;,
     *                 seriesDetectionCondition.getSmartDetectionCondition&#40;&#41;
     *                     .getAnomalyDetectorDirection&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;  - Suppress conditions: minimum number: %s; minimum ratio: %s%n&quot;,
     *                 seriesDetectionCondition.getSmartDetectionCondition&#40;&#41;
     *                     .getSuppressCondition&#40;&#41;.getMinNumber&#40;&#41;,
     *                 seriesDetectionCondition.getSmartDetectionCondition&#40;&#41;
     *                     .getSuppressCondition&#40;&#41;.getMinRatio&#40;&#41;&#41;;
     *
     *             System.out.printf&#40;&quot; - Hard Threshold Condition:%n&quot;&#41;;
     *             System.out.printf&#40;&quot;  -  Lower bound: %s%n&quot;,
     *                 seriesDetectionCondition.getHardThresholdCondition&#40;&#41;
     *                     .getLowerBound&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;  -  Upper bound: %s%n&quot;,
     *                 seriesDetectionCondition.getHardThresholdCondition&#40;&#41;
     *                     .getUpperBound&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;  -  Suppress conditions: minimum number: %s; minimum ratio: %s%n&quot;,
     *                 seriesDetectionCondition.getHardThresholdCondition&#40;&#41;
     *                     .getSuppressCondition&#40;&#41;.getMinNumber&#40;&#41;,
     *                 seriesDetectionCondition.getHardThresholdCondition&#40;&#41;
     *                     .getSuppressCondition&#40;&#41;.getMinRatio&#40;&#41;&#41;;
     *
     *             System.out.printf&#40;&quot; - Change Threshold Condition:%n&quot;&#41;;
     *             System.out.printf&#40;&quot;  -  Change percentage: %s%n&quot;,
     *                 seriesDetectionCondition.getChangeThresholdCondition&#40;&#41;
     *                     .getChangePercentage&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;  -  Shift point: %s%n&quot;,
     *                 seriesDetectionCondition.getChangeThresholdCondition&#40;&#41;
     *                     .getShiftPoint&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;  -  Detect anomaly if within range: %s%n&quot;,
     *                 seriesDetectionCondition.getChangeThresholdCondition&#40;&#41;
     *                     .isWithinRange&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;  -  Suppress conditions: minimum number: %s; minimum ratio: %s%n&quot;,
     *                 seriesDetectionCondition.getChangeThresholdCondition&#40;&#41;
     *                     .getSuppressCondition&#40;&#41;.getMinNumber&#40;&#41;,
     *                 seriesDetectionCondition.getChangeThresholdCondition&#40;&#41;
     *                     .getSuppressCondition&#40;&#41;.getMinRatio&#40;&#41;&#41;;
     *         &#125;
     *
     *         List&lt;MetricSeriesGroupDetectionCondition&gt; seriesGroupDetectionConditions
     *             = detectionConfig.getSeriesGroupDetectionConditions&#40;&#41;;
     *         System.out.printf&#40;&quot;Series Group Detection Conditions:%n&quot;&#41;;
     *         for &#40;MetricSeriesGroupDetectionCondition seriesGroupDetectionCondition
     *             : seriesGroupDetectionConditions&#41; &#123;
     *             DimensionKey seriesGroupKey = seriesGroupDetectionCondition.getSeriesGroupKey&#40;&#41;;
     *             final String seriesGroupKeyStr
     *                 = Arrays.toString&#40;seriesGroupKey.asMap&#40;&#41;.entrySet&#40;&#41;.stream&#40;&#41;.toArray&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;- Series Group Key: %s%n&quot;, seriesGroupKeyStr&#41;;
     *             System.out.printf&#40;&quot; - Use %s operator for multiple detection conditions:%n&quot;,
     *                 seriesGroupDetectionCondition.getConditionOperator&#40;&#41;&#41;;
     *
     *             System.out.printf&#40;&quot; - Smart Detection Condition:%n&quot;&#41;;
     *             System.out.printf&#40;&quot;  - Sensitivity: %s%n&quot;,
     *                 seriesGroupDetectionCondition.getSmartDetectionCondition&#40;&#41;
     *                     .getSensitivity&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;  - Detection direction: %s%n&quot;,
     *                 seriesGroupDetectionCondition.getSmartDetectionCondition&#40;&#41;
     *                     .getAnomalyDetectorDirection&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;  - Suppress conditions: minimum number: %s; minimum ratio: %s%n&quot;,
     *                 seriesGroupDetectionCondition.getSmartDetectionCondition&#40;&#41;
     *                     .getSuppressCondition&#40;&#41;.getMinNumber&#40;&#41;,
     *                 seriesGroupDetectionCondition.getSmartDetectionCondition&#40;&#41;
     *                     .getSuppressCondition&#40;&#41;.getMinRatio&#40;&#41;&#41;;
     *
     *             System.out.printf&#40;&quot; - Hard Threshold Condition:%n&quot;&#41;;
     *             System.out.printf&#40;&quot;  -  Lower bound: %s%n&quot;,
     *                 seriesGroupDetectionCondition.getHardThresholdCondition&#40;&#41;
     *                     .getLowerBound&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;  -  Upper bound: %s%n&quot;,
     *                 seriesGroupDetectionCondition.getHardThresholdCondition&#40;&#41;
     *                     .getUpperBound&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;  -  Suppress conditions: minimum number: %s; minimum ratio: %s%n&quot;,
     *                 seriesGroupDetectionCondition.getHardThresholdCondition&#40;&#41;
     *                     .getSuppressCondition&#40;&#41;.getMinNumber&#40;&#41;,
     *                 seriesGroupDetectionCondition.getHardThresholdCondition&#40;&#41;
     *                     .getSuppressCondition&#40;&#41;.getMinRatio&#40;&#41;&#41;;
     *
     *             System.out.printf&#40;&quot; - Change Threshold Condition:%n&quot;&#41;;
     *             System.out.printf&#40;&quot;  -  Change percentage: %s%n&quot;,
     *                 seriesGroupDetectionCondition.getChangeThresholdCondition&#40;&#41;
     *                     .getChangePercentage&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;  -  Shift point: %s%n&quot;,
     *                 seriesGroupDetectionCondition.getChangeThresholdCondition&#40;&#41;
     *                     .getShiftPoint&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;  -  Detect anomaly if within range: %s%n&quot;,
     *                 seriesGroupDetectionCondition.getChangeThresholdCondition&#40;&#41;
     *                     .isWithinRange&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;  -  Suppress conditions: minimum number: %s; minimum ratio: %s%n&quot;,
     *                 seriesGroupDetectionCondition.getChangeThresholdCondition&#40;&#41;
     *                     .getSuppressCondition&#40;&#41;.getMinNumber&#40;&#41;,
     *                 seriesGroupDetectionCondition.getChangeThresholdCondition&#40;&#41;
     *                     .getSuppressCondition&#40;&#41;.getMinRatio&#40;&#41;&#41;;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.getDetectionConfigWithResponse#String -->
     *
     * @param detectionConfigurationId The anomaly detection configuration id.
     * @return A {@link Response} of a {@link Mono} containing the {@link AnomalyDetectionConfiguration}
     *     for the provided id.
     * @throws IllegalArgumentException If {@code detectionConfigurationId} does not conform to the UUID
     *     format specification.
     * @throws NullPointerException thrown if the {@code detectionConfigurationId} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<AnomalyDetectionConfiguration>> getDetectionConfigWithResponse(
        String detectionConfigurationId) {
        try {
            return withContext(context -> getDetectionConfigWithResponse(detectionConfigurationId,
                context));
        } catch (RuntimeException e) {
            return FluxUtil.monoError(logger, e);
        }
    }

    Mono<Response<AnomalyDetectionConfiguration>> getDetectionConfigWithResponse(
        String detectionConfigurationId, Context context) {
        Objects.requireNonNull(detectionConfigurationId, "detectionConfigurationId is required.");

        return service.getAnomalyDetectionConfigurationWithResponseAsync(UUID.fromString(detectionConfigurationId),
            context.addData(AZ_TRACING_NAMESPACE_KEY, METRICS_ADVISOR_TRACING_NAMESPACE_VALUE))
            .doOnSubscribe(ignoredValue -> logger.info("Retrieving AnomalyDetectionConfiguration - {}",
                detectionConfigurationId))
            .doOnSuccess(response -> logger.info("Retrieved AnomalyDetectionConfiguration - {}", response))
            .doOnError(error -> logger.warning("Failed to retrieve AnomalyDetectionConfiguration - {}",
                detectionConfigurationId, error))
            .map(response -> {
                AnomalyDetectionConfiguration configuration
                    = DetectionConfigurationTransforms.fromInner(response.getValue());
                return new ResponseBase<Void, AnomalyDetectionConfiguration>(response.getRequest(),
                    response.getStatusCode(),
                    response.getHeaders(),
                    configuration,
                    null);
            });
    }

    /**
     * Update a configuration to detect anomalies in the time series of a metric.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.updateDetectionConfig#AnomalyDetectionConfiguration -->
     * <pre>
     * final String detectionConfigId = &quot;7b8069a1-1564-46da-9f50-b5d0dd9129ab&quot;;
     * metricsAdvisorAdminAsyncClient
     *     .getDetectionConfig&#40;detectionConfigId&#41;
     *     .flatMap&#40;detectionConfig -&gt; &#123;
     *         detectionConfig.setName&#40;&quot;updated config name&quot;&#41;;
     *         detectionConfig.setDescription&#40;&quot;updated with more detection conditions&quot;&#41;;
     *
     *         DimensionKey seriesGroupKey = new DimensionKey&#40;&#41;
     *             .put&#40;&quot;city&quot;, &quot;Seoul&quot;&#41;;
     *         detectionConfig.addSeriesGroupDetectionCondition&#40;
     *             new MetricSeriesGroupDetectionCondition&#40;seriesGroupKey&#41;
     *                 .setSmartDetectionCondition&#40;new SmartDetectionCondition&#40;
     *                     10.0,
     *                     AnomalyDetectorDirection.UP,
     *                     new SuppressCondition&#40;2, 2&#41;&#41;&#41;&#41;;
     *         return metricsAdvisorAdminAsyncClient
     *             .updateDetectionConfig&#40;detectionConfig&#41;;
     *     &#125;&#41;
     *     .subscribe&#40;updatedDetectionConfig -&gt; &#123;
     *         System.out.printf&#40;&quot;Detection config Id: %s%n&quot;, updatedDetectionConfig.getId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Name: %s%n&quot;, updatedDetectionConfig.getName&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Description: %s%n&quot;, updatedDetectionConfig.getDescription&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;MetricId: %s%n&quot;, updatedDetectionConfig.getMetricId&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.updateDetectionConfig#AnomalyDetectionConfiguration -->
     *
     * @param detectionConfiguration The anomaly detection configuration.
     * @return A {@link Mono} containing the updated {@link AnomalyDetectionConfiguration}.
     * @throws NullPointerException thrown if the {@code detectionConfiguration} is null
     *   or {@code detectionConfiguration.id} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<AnomalyDetectionConfiguration> updateDetectionConfig(
        AnomalyDetectionConfiguration detectionConfiguration) {
        return updateDetectionConfigWithResponse(detectionConfiguration)
            .map(Response::getValue);
    }

    /**
     * Update a configuration to detect anomalies in the time series of a metric.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.updateDetectionConfigWithResponse#AnomalyDetectionConfiguration -->
     * <pre>
     * final String detectionConfigId = &quot;7b8069a1-1564-46da-9f50-b5d0dd9129ab&quot;;
     * metricsAdvisorAdminAsyncClient
     *     .getDetectionConfigWithResponse&#40;detectionConfigId&#41;
     *     .flatMap&#40;response -&gt; &#123;
     *         AnomalyDetectionConfiguration detectionConfig = response.getValue&#40;&#41;;
     *         detectionConfig.setName&#40;&quot;updated config name&quot;&#41;;
     *         detectionConfig.setDescription&#40;&quot;updated with more detection conditions&quot;&#41;;
     *         DimensionKey seriesGroupKey = new DimensionKey&#40;&#41;
     *             .put&#40;&quot;city&quot;, &quot;Seoul&quot;&#41;;
     *         detectionConfig.addSeriesGroupDetectionCondition&#40;
     *             new MetricSeriesGroupDetectionCondition&#40;seriesGroupKey&#41;
     *                 .setSmartDetectionCondition&#40;new SmartDetectionCondition&#40;
     *                     10.0,
     *                     AnomalyDetectorDirection.UP,
     *                     new SuppressCondition&#40;2, 2&#41;&#41;&#41;&#41;;
     *         return metricsAdvisorAdminAsyncClient
     *             .updateDetectionConfigWithResponse&#40;detectionConfig&#41;;
     *     &#125;&#41;
     *     .subscribe&#40;response -&gt; &#123;
     *         AnomalyDetectionConfiguration updatedDetectionConfig = response.getValue&#40;&#41;;
     *         System.out.printf&#40;&quot;Detection config Id: %s%n&quot;, updatedDetectionConfig.getId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Name: %s%n&quot;, updatedDetectionConfig.getName&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Description: %s%n&quot;, updatedDetectionConfig.getDescription&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;MetricId: %s%n&quot;, updatedDetectionConfig.getMetricId&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.updateDetectionConfigWithResponse#AnomalyDetectionConfiguration -->
     *
     * @param detectionConfiguration The anomaly detection configuration.
     * @return A {@link Response} of a {@link Mono} containing the updated {@link AnomalyDetectionConfiguration}.
     * @throws NullPointerException thrown if the {@code detectionConfiguration} is null
     *   or {@code detectionConfiguration.id} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<AnomalyDetectionConfiguration>> updateDetectionConfigWithResponse(
        AnomalyDetectionConfiguration detectionConfiguration) {
        try {
            return withContext(context -> updateDetectionConfigWithResponse(detectionConfiguration,
                context));
        } catch (RuntimeException e) {
            return FluxUtil.monoError(logger, e);
        }
    }

    Mono<Response<AnomalyDetectionConfiguration>> updateDetectionConfigWithResponse(
        AnomalyDetectionConfiguration detectionConfiguration,
        Context context) {
        Objects.requireNonNull(detectionConfiguration, "detectionConfiguration is required");
        Objects.requireNonNull(detectionConfiguration.getId(), "detectionConfiguration.id is required");

        final AnomalyDetectionConfigurationPatch innerDetectionConfigurationPatch
            = DetectionConfigurationTransforms.toInnerForUpdate(logger, detectionConfiguration);
        return service.updateAnomalyDetectionConfigurationWithResponseAsync(
            UUID.fromString(detectionConfiguration.getId()),
            innerDetectionConfigurationPatch,
            context.addData(AZ_TRACING_NAMESPACE_KEY, METRICS_ADVISOR_TRACING_NAMESPACE_VALUE))
            .doOnSubscribe(ignoredValue -> logger.info("Updating AnomalyDetectionConfiguration"))
            .doOnSuccess(response -> logger.info("Updated AnomalyDetectionConfiguration"))
            .doOnError(error -> logger.warning("Failed to update AnomalyDetectionConfiguration", error))
            .flatMap(response -> {
                return getDetectionConfigWithResponse(detectionConfiguration.getId(), context)
                    .map(configurationResponse -> new ResponseBase<Void, AnomalyDetectionConfiguration>(
                        response.getRequest(),
                        response.getStatusCode(),
                        response.getHeaders(),
                        configurationResponse.getValue(),
                        null));
            });
    }

    /**
     * Delete a metric anomaly detection configuration.
     *
     * @param detectionConfigurationId The metric anomaly detection configuration unique id.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.deleteDetectionConfig#String -->
     * <pre>
     * final String detectionConfigId = &quot;7b8069a1-1564-46da-9f50-b5d0dd9129ab&quot;;
     * metricsAdvisorAdminAsyncClient
     *     .deleteDetectionConfig&#40;detectionConfigId&#41;
     *     .subscribe&#40;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.deleteDetectionConfig#String -->
     *
     * @return An empty Mono.
     * @throws NullPointerException thrown if the {@code detectionConfigurationId} is null.
     * @throws IllegalArgumentException If {@code detectionConfigurationId} does not conform to the UUID
     *     format specification.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteDetectionConfig(String detectionConfigurationId) {
        return deleteDetectionConfigWithResponse(detectionConfigurationId).then();
    }

    /**
     * Delete a metric anomaly detection configuration.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.deleteDetectionConfigWithResponse#String -->
     * <pre>
     * final String detectionConfigId = &quot;7b8069a1-1564-46da-9f50-b5d0dd9129ab&quot;;
     * metricsAdvisorAdminAsyncClient
     *     .deleteDetectionConfigWithResponse&#40;detectionConfigId&#41;
     *     .subscribe&#40;response -&gt;
     *         System.out.printf&#40;&quot;Response statusCode: %d%n&quot;, response.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.deleteDetectionConfigWithResponse#String -->
     *
     * @param detectionConfigurationId The metric anomaly detection configuration unique id.
     *
     * @return A {@link Response} of a {@link Mono}.
     * @throws NullPointerException thrown if the {@code detectionConfigurationId} is null.
     * @throws IllegalArgumentException If {@code detectionConfigurationId} does not conform to the UUID
     *     format specification.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteDetectionConfigWithResponse(
        String detectionConfigurationId) {
        try {
            return withContext(context -> deleteDetectionConfigWithResponse(
                detectionConfigurationId,
                context));
        } catch (RuntimeException e) {
            return FluxUtil.monoError(logger, e);
        }
    }

    Mono<Response<Void>> deleteDetectionConfigWithResponse(String detectionConfigurationId,
                                                           Context context) {
        Objects.requireNonNull(detectionConfigurationId, "detectionConfigurationId is required.");
        return service.deleteHookWithResponseAsync(UUID.fromString(detectionConfigurationId),
            context.addData(AZ_TRACING_NAMESPACE_KEY, METRICS_ADVISOR_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignoredValue -> logger.info("Deleting MetricAnomalyDetectionConfiguration"))
            .doOnSuccess(response -> logger.info("Deleted MetricAnomalyDetectionConfiguration"))
            .doOnError(error -> logger.warning("Failed to delete MetricAnomalyDetectionConfiguration", error));
    }

    /**
     * Given a metric id, retrieve all anomaly detection configurations applied to it.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.listDetectionConfigs#String -->
     * <pre>
     * final String metricId = &quot;0b836da8-10e6-46cd-8f4f-28262e113a62&quot;;
     * metricsAdvisorAdminAsyncClient.listDetectionConfigs&#40;metricId&#41;
     *     .subscribe&#40;detectionConfig -&gt; &#123;
     *         System.out.printf&#40;&quot;Detection config Id: %s%n&quot;, detectionConfig.getId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Name: %s%n&quot;, detectionConfig.getName&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Description: %s%n&quot;, detectionConfig.getDescription&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;MetricId: %s%n&quot;, detectionConfig.getMetricId&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.listDetectionConfigs#String -->
     *
     * @param metricId The metric id.
     * @return The anomaly detection configurations.
     * @throws NullPointerException thrown if the {@code metricId} is null.
     * @throws IllegalArgumentException If {@code metricId} does not conform to the UUID format specification.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<AnomalyDetectionConfiguration> listDetectionConfigs(
        String metricId) {
        return listDetectionConfigs(metricId, null);
    }

    /**
     * Given a metric id, retrieve all anomaly detection configurations applied to it.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.listDetectionConfigs#String-ListDetectionConfigsOptions -->
     * <pre>
     * final String metricId = &quot;0b836da8-10e6-46cd-8f4f-28262e113a62&quot;;
     * metricsAdvisorAdminAsyncClient.listDetectionConfigs&#40;metricId,
     *     new ListDetectionConfigsOptions&#40;&#41;&#41;
     *     .subscribe&#40;detectionConfig -&gt; &#123;
     *         System.out.printf&#40;&quot;Detection config Id: %s%n&quot;, detectionConfig.getId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Name: %s%n&quot;, detectionConfig.getName&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Description: %s%n&quot;, detectionConfig.getDescription&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;MetricId: %s%n&quot;, detectionConfig.getMetricId&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.listDetectionConfigs#String-ListDetectionConfigsOptions -->
     *
     * @param metricId The metric id.
     * @param listDetectionConfigsOptions the additional configurable options to specify when querying the result.
     * @return The anomaly detection configurations.
     * @throws NullPointerException thrown if the {@code metricId} is null.
     * @throws IllegalArgumentException If {@code metricId} does not conform to the UUID format specification.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<AnomalyDetectionConfiguration> listDetectionConfigs(
        String metricId,
        ListDetectionConfigsOptions listDetectionConfigsOptions) {
        try {
            return new PagedFlux<>(() ->
                withContext(context ->
                    listAnomalyDetectionConfigsSinglePageAsync(metricId, listDetectionConfigsOptions, context)),
                continuationToken ->
                    withContext(context -> listAnomalyDetectionConfigsNextPageAsync(continuationToken,
                        context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> FluxUtil.monoError(logger, ex));
        }

    }

    PagedFlux<AnomalyDetectionConfiguration> listDetectionConfigs(
        String metricId,
        ListDetectionConfigsOptions options,
        Context context) {
        return new PagedFlux<>(() ->
            listAnomalyDetectionConfigsSinglePageAsync(metricId, options, context),
            continuationToken ->
                listAnomalyDetectionConfigsNextPageAsync(continuationToken, context));
    }

    private Mono<PagedResponse<AnomalyDetectionConfiguration>> listAnomalyDetectionConfigsSinglePageAsync(
        String metricId,
        ListDetectionConfigsOptions options,
        Context context) {
        if (options == null) {
            options = new ListDetectionConfigsOptions();
        }
        return service.getAnomalyDetectionConfigurationsByMetricSinglePageAsync(
            UUID.fromString(metricId), options.getSkip(), options.getMaxPageSize(),
            context.addData(AZ_TRACING_NAMESPACE_KEY, METRICS_ADVISOR_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignoredValue -> logger.info("Listing MetricAnomalyDetectionConfigs"))
            .doOnSuccess(response -> logger.info("Listed MetricAnomalyDetectionConfigs {}", response))
            .doOnError(error -> logger.warning("Failed to list the MetricAnomalyDetectionConfigs", error))
            .map(response -> DetectionConfigurationTransforms.fromInnerPagedResponse(response));
    }

    private Mono<PagedResponse<AnomalyDetectionConfiguration>> listAnomalyDetectionConfigsNextPageAsync(
        String nextPageLink, Context context) {
        if (CoreUtils.isNullOrEmpty(nextPageLink)) {
            return Mono.empty();
        }
        return service.getAnomalyDetectionConfigurationsByMetricNextSinglePageAsync(nextPageLink,
            context.addData(AZ_TRACING_NAMESPACE_KEY, METRICS_ADVISOR_TRACING_NAMESPACE_VALUE))
            .doOnSubscribe(ignoredValue -> logger.info("Retrieving the next listing page - Page {}", nextPageLink))
            .doOnSuccess(response -> logger.info("Retrieved the next listing page - Page {} {}",
                nextPageLink,
                response))
            .doOnError(error -> logger.warning("Failed to retrieve the next listing page - Page {}", nextPageLink,
                error))
            .map(response -> DetectionConfigurationTransforms.fromInnerPagedResponse(response));
    }

    /**
     * Creates a notificationHook that receives anomaly incident alerts.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.createHook#NotificationHook -->
     * <pre>
     * NotificationHook emailNotificationHook = new EmailNotificationHook&#40;&quot;email hook&quot;&#41;
     *     .setDescription&#40;&quot;my email hook&quot;&#41;
     *     .setEmailsToAlert&#40;new ArrayList&lt;String&gt;&#40;&#41; &#123;&#123;
     *             add&#40;&quot;alertme&#64;alertme.com&quot;&#41;;
     *         &#125;&#125;&#41;
     *     .setExternalLink&#40;&quot;https:&#47;&#47;adwiki.azurewebsites.net&#47;articles&#47;howto&#47;alerts&#47;create-hooks.html&quot;&#41;;
     *
     * metricsAdvisorAdminAsyncClient.createHook&#40;emailNotificationHook&#41;
     *     .subscribe&#40;hook -&gt; &#123;
     *         EmailNotificationHook createdEmailHook = &#40;EmailNotificationHook&#41; hook;
     *         System.out.printf&#40;&quot;NotificationHook Id: %s%n&quot;, createdEmailHook.getId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;NotificationHook Name: %s%n&quot;, createdEmailHook.getName&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;NotificationHook Description: %s%n&quot;, createdEmailHook.getDescription&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;NotificationHook External Link: %s%n&quot;, createdEmailHook.getExternalLink&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;NotificationHook Emails: %s%n&quot;, String.join&#40;&quot;,&quot;,
     *             createdEmailHook.getEmailsToAlert&#40;&#41;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.createHook#NotificationHook -->
     *
     * @param notificationHook The notificationHook.
     *
     * @return A {@link Mono} containing the created {@link NotificationHook}.
     * @throws NullPointerException If {@code notificationHook}, {@code notificationHook.name},
     * {@code notificationHook.endpoint} (for web notificationHook) is null.
     * @throws IllegalArgumentException If at least one email not present for email notificationHook.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<NotificationHook> createHook(NotificationHook notificationHook) {
        return createHookWithResponse(notificationHook)
            .map(Response::getValue);
    }

    /**
     * Creates a notificationHook that receives anomaly incident alerts.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.createHookWithResponse#NotificationHook -->
     * <pre>
     * NotificationHook emailNotificationHook = new EmailNotificationHook&#40;&quot;email hook&quot;&#41;
     *     .setDescription&#40;&quot;my email hook&quot;&#41;
     *     .setEmailsToAlert&#40;new ArrayList&lt;String&gt;&#40;&#41; &#123;&#123;
     *             add&#40;&quot;alertme&#64;alertme.com&quot;&#41;;
     *         &#125;&#125;&#41;
     *     .setExternalLink&#40;&quot;https:&#47;&#47;adwiki.azurewebsites.net&#47;articles&#47;howto&#47;alerts&#47;create-hooks.html&quot;&#41;;
     *
     * metricsAdvisorAdminAsyncClient.createHookWithResponse&#40;emailNotificationHook&#41;
     *     .subscribe&#40;response -&gt; &#123;
     *         System.out.printf&#40;&quot;Response statusCode: %d%n&quot;, response.getStatusCode&#40;&#41;&#41;;
     *         EmailNotificationHook createdEmailHook = &#40;EmailNotificationHook&#41; response.getValue&#40;&#41;;
     *         System.out.printf&#40;&quot;NotificationHook Id: %s%n&quot;, createdEmailHook.getId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;NotificationHook Name: %s%n&quot;, createdEmailHook.getName&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;NotificationHook Description: %s%n&quot;, createdEmailHook.getDescription&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;NotificationHook External Link: %s%n&quot;, createdEmailHook.getExternalLink&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;NotificationHook Emails: %s%n&quot;, String.join&#40;&quot;,&quot;,
     *             createdEmailHook.getEmailsToAlert&#40;&#41;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.createHookWithResponse#NotificationHook -->
     *
     * @param notificationHook The notificationHook.
     *
     * @return A {@link Response} of a {@link Mono} containing the created {@link NotificationHook}.
     * @throws NullPointerException If {@code notificationHook}, {@code notificationHook.name},
     * {@code notificationHook.endpoint} (for web notificationHook) is null.
     * @throws IllegalArgumentException If at least one email not present for email notificationHook.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<NotificationHook>> createHookWithResponse(NotificationHook notificationHook) {
        try {
            return withContext(context -> createHookWithResponse(notificationHook, context));
        } catch (RuntimeException e) {
            return FluxUtil.monoError(logger, e);
        }
    }

    Mono<Response<NotificationHook>> createHookWithResponse(NotificationHook notificationHook, Context context) {
        Objects.requireNonNull(notificationHook, "'notificationHook' cannot be null.");
        return service.createHookWithResponseAsync(HookTransforms.toInnerForCreate(logger, notificationHook),
            context.addData(AZ_TRACING_NAMESPACE_KEY, METRICS_ADVISOR_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignoredValue -> logger.info("Creating NotificationHook"))
            .doOnSuccess(response -> logger.info("Created NotificationHook {}", response))
            .doOnError(error -> logger.warning("Failed to create notificationHook", error))
            .flatMap(response -> {
                final String hookUri = response.getDeserializedHeaders().getLocation();
                final String hookId = parseOperationId(hookUri);
                return getHookWithResponse(hookId, context)
                    .map(hookResponse -> new ResponseBase<Void, NotificationHook>(response.getRequest(),
                        response.getStatusCode(),
                        response.getHeaders(),
                        hookResponse.getValue(),
                        null));
            });
    }

    /**
     * Get a hook by its id.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.getHook#String -->
     * <pre>
     * final String hookId = &quot;f00853f1-6627-447f-bacf-8dccf2e86fed&quot;;
     * metricsAdvisorAdminAsyncClient.getHook&#40;hookId&#41;
     *     .subscribe&#40;hook -&gt; &#123;
     *         if &#40;hook instanceof EmailNotificationHook&#41; &#123;
     *             EmailNotificationHook emailHook = &#40;EmailNotificationHook&#41; hook;
     *             System.out.printf&#40;&quot;Email Hook Id: %s%n&quot;, emailHook.getId&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Email Hook Name: %s%n&quot;, emailHook.getName&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Email Hook Description: %s%n&quot;, emailHook.getDescription&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Email Hook External Link: %s%n&quot;, emailHook.getExternalLink&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Email Hook Emails: %s%n&quot;, String.join&#40;&quot;,&quot;,
     *                 emailHook.getEmailsToAlert&#40;&#41;&#41;&#41;;
     *         &#125; else if &#40;hook instanceof WebNotificationHook&#41; &#123;
     *             WebNotificationHook webHook = &#40;WebNotificationHook&#41; hook;
     *             System.out.printf&#40;&quot;Web Hook Id: %s%n&quot;, webHook.getId&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Web Hook Name: %s%n&quot;, webHook.getName&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Web Hook Description: %s%n&quot;, webHook.getDescription&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Web Hook External Link: %s%n&quot;, webHook.getExternalLink&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Web Hook Endpoint: %s%n&quot;, webHook.getEndpoint&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Web Hook Headers: %s%n&quot;, webHook.getHttpHeaders&#40;&#41;&#41;;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.getHook#String -->
     *
     * @param hookId The hook unique id.
     *
     * @return A {@link Mono} containing the {@link NotificationHook} for the provided id.
     * @throws IllegalArgumentException If {@code hookId} does not conform to the UUID format specification.
     * @throws NullPointerException thrown if the {@code hookId} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<NotificationHook> getHook(String hookId) {
        return getHookWithResponse(hookId).map(Response::getValue);
    }

    /**
     * Get a hook by its id.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.getHookWithResponse#String -->
     * <pre>
     * final String hookId = &quot;f00853f1-6627-447f-bacf-8dccf2e86fed&quot;;
     * metricsAdvisorAdminAsyncClient.getHookWithResponse&#40;hookId&#41;
     *     .subscribe&#40;response -&gt; &#123;
     *         System.out.printf&#40;&quot;Response status code: %d%n&quot;, response.getStatusCode&#40;&#41;&#41;;
     *         NotificationHook notificationHook = response.getValue&#40;&#41;;
     *         if &#40;notificationHook instanceof EmailNotificationHook&#41; &#123;
     *             EmailNotificationHook emailHook = &#40;EmailNotificationHook&#41; notificationHook;
     *             System.out.printf&#40;&quot;Email Hook Id: %s%n&quot;, emailHook.getId&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Email Hook Name: %s%n&quot;, emailHook.getName&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Email Hook Description: %s%n&quot;, emailHook.getDescription&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Email Hook External Link: %s%n&quot;, emailHook.getExternalLink&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Email Hook Emails: %s%n&quot;, String.join&#40;&quot;,&quot;, emailHook.getEmailsToAlert&#40;&#41;&#41;&#41;;
     *         &#125; else if &#40;notificationHook instanceof WebNotificationHook&#41; &#123;
     *             WebNotificationHook webHook = &#40;WebNotificationHook&#41; notificationHook;
     *             System.out.printf&#40;&quot;Web Hook Id: %s%n&quot;, webHook.getId&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Web Hook Name: %s%n&quot;, webHook.getName&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Web Hook Description: %s%n&quot;, webHook.getDescription&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Web Hook External Link: %s%n&quot;, webHook.getExternalLink&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Web Hook Endpoint: %s%n&quot;, webHook.getEndpoint&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Web Hook Headers: %s%n&quot;, webHook.getHttpHeaders&#40;&#41;&#41;;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.getHookWithResponse#String -->
     *
     * @param hookId The hook unique id.
     *
     * @return A {@link Response} of a {@link Mono} containing the {@link NotificationHook} for the provided id.
     * @throws IllegalArgumentException If {@code hookId} does not conform to the UUID format specification.
     * @throws NullPointerException thrown if the {@code hookId} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<NotificationHook>> getHookWithResponse(String hookId) {
        try {
            return withContext(context -> getHookWithResponse(hookId, context));
        } catch (RuntimeException e) {
            return FluxUtil.monoError(logger, e);
        }
    }

    Mono<Response<NotificationHook>> getHookWithResponse(String hookId, Context context) {
        Objects.requireNonNull(hookId, "hookId is required.");
        return service.getHookWithResponseAsync(UUID.fromString(hookId),
            context.addData(AZ_TRACING_NAMESPACE_KEY, METRICS_ADVISOR_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignoredValue -> logger.info("Retrieving NotificationHook"))
            .doOnSuccess(response -> logger.info("Retrieved NotificationHook {}", response))
            .doOnError(error -> logger.warning("Failed to retrieve hook", error))
            .map(innerResponse -> new ResponseBase<Void, NotificationHook>(innerResponse.getRequest(),
                innerResponse.getStatusCode(),
                innerResponse.getHeaders(),
                HookTransforms.fromInner(logger, innerResponse.getValue()),
                null));
    }

    /**
     * Update an existing notificationHook.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.updateHook#NotificationHook -->
     * <pre>
     * final String emailHookId = &quot;f00853f1-6627-447f-bacf-8dccf2e86fed&quot;;
     * metricsAdvisorAdminAsyncClient.getHook&#40;emailHookId&#41;
     *     .flatMap&#40;hook -&gt; &#123;
     *         EmailNotificationHook emailHook = &#40;EmailNotificationHook&#41; hook;
     *         List&lt;String&gt; emailsToUpdate = new ArrayList&lt;&gt;&#40;emailHook.getEmailsToAlert&#40;&#41;&#41;;
     *         emailsToUpdate.remove&#40;&quot;alertme&#64;alertme.com&quot;&#41;;
     *         emailsToUpdate.add&#40;&quot;alertme2&#64;alertme.com&quot;&#41;;
     *         emailsToUpdate.add&#40;&quot;alertme3&#64;alertme.com&quot;&#41;;
     *         emailHook.setEmailsToAlert&#40;emailsToUpdate&#41;;
     *         return metricsAdvisorAdminAsyncClient.updateHook&#40;emailHook&#41;;
     *     &#125;&#41;
     *     .subscribe&#40;hook -&gt; &#123;
     *         EmailNotificationHook emailHook = &#40;EmailNotificationHook&#41; hook;
     *         System.out.printf&#40;&quot;Email Hook Id: %s%n&quot;, emailHook.getId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Email Hook Name: %s%n&quot;, emailHook.getName&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Email Hook Description: %s%n&quot;, emailHook.getDescription&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Email Hook External Link: %s%n&quot;, emailHook.getExternalLink&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Email Hook Emails: %s%n&quot;, String.join&#40;&quot;,&quot;, emailHook.getEmailsToAlert&#40;&#41;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.updateHook#NotificationHook -->
     *
     * @param notificationHook The notificationHook to update.
     *
     * @return A {@link Mono} containing the updated {@link NotificationHook}.
     * @throws NullPointerException If {@code notificationHook.id} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<NotificationHook> updateHook(NotificationHook notificationHook) {
        return updateHookWithResponse(notificationHook).map(Response::getValue);
    }

    /**
     * Update an existing notification hook.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.updateHookWithResponse#NotificationHook -->
     * <pre>
     * final String emailHookId = &quot;f00853f1-6627-447f-bacf-8dccf2e86fed&quot;;
     * metricsAdvisorAdminAsyncClient.getHookWithResponse&#40;emailHookId&#41;
     *     .flatMap&#40;response -&gt; &#123;
     *         EmailNotificationHook emailHook = &#40;EmailNotificationHook&#41; response.getValue&#40;&#41;;
     *         List&lt;String&gt; emailsToUpdate = new ArrayList&lt;&gt;&#40;emailHook.getEmailsToAlert&#40;&#41;&#41;;
     *         emailsToUpdate.remove&#40;&quot;alertme&#64;alertme.com&quot;&#41;;
     *         emailsToUpdate.add&#40;&quot;alertme2&#64;alertme.com&quot;&#41;;
     *         emailsToUpdate.add&#40;&quot;alertme3&#64;alertme.com&quot;&#41;;
     *         emailHook.setEmailsToAlert&#40;emailsToUpdate&#41;;
     *         return metricsAdvisorAdminAsyncClient.updateHookWithResponse&#40;emailHook&#41;;
     *     &#125;&#41;
     *     .subscribe&#40;response -&gt; &#123;
     *         System.out.printf&#40;&quot;Response statusCode: %d%n&quot;, response.getStatusCode&#40;&#41;&#41;;
     *         EmailNotificationHook emailHook = &#40;EmailNotificationHook&#41; response.getValue&#40;&#41;;
     *         System.out.printf&#40;&quot;Email Hook Id: %s%n&quot;, emailHook.getId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Email Hook Name: %s%n&quot;, emailHook.getName&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Email Hook Description: %s%n&quot;, emailHook.getDescription&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Email Hook External Link: %s%n&quot;, emailHook.getExternalLink&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Email Hook Emails: %s%n&quot;, String.join&#40;&quot;,&quot;, emailHook.getEmailsToAlert&#40;&#41;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.updateHookWithResponse#NotificationHook -->
     *
     * @param notificationHook The notificationHook to update.
     *
     * @return A {@link Response} of a {@link Mono} containing the updated {@link NotificationHook}.
     * @throws NullPointerException If {@code notificationHook.id} is null.
     * @throws IllegalArgumentException If {@code notificationHook.Id} does not conform to the UUID format
     * specification.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<NotificationHook>> updateHookWithResponse(NotificationHook notificationHook) {
        try {
            return withContext(context -> updateHookWithResponse(notificationHook, context));
        } catch (RuntimeException e) {
            return FluxUtil.monoError(logger, e);
        }
    }

    Mono<Response<NotificationHook>> updateHookWithResponse(NotificationHook notificationHook, Context context) {
        Objects.requireNonNull(notificationHook, "'notificationHook' cannot be null.");
        Objects.requireNonNull(notificationHook.getId(), "'notificationHook.id' cannot be null.");
        return service.updateHookWithResponseAsync(UUID.fromString(notificationHook.getId()),
            HookTransforms.toInnerForUpdate(logger, notificationHook),
            context.addData(AZ_TRACING_NAMESPACE_KEY, METRICS_ADVISOR_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignoredValue -> logger.info("Updating NotificationHook"))
            .doOnSuccess(response -> logger.info("Updated NotificationHook {}", response))
            .doOnError(error -> logger.warning("Failed to update notificationHook", error))
            .flatMap(response -> getHookWithResponse(notificationHook.getId(), context)
                .map(hookResponse -> new ResponseBase<Void, NotificationHook>(response.getRequest(),
                    response.getStatusCode(),
                    response.getHeaders(),
                    hookResponse.getValue(),
                    null)));
    }

    /**
     * Delete a hook.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.deleteHook#String -->
     * <pre>
     * final String emailHookId = &quot;f00853f1-6627-447f-bacf-8dccf2e86fed&quot;;
     * metricsAdvisorAdminAsyncClient.deleteHook&#40;emailHookId&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.deleteHook#String -->
     *
     * @param hookId The hook unique id.
     *
     * @return An empty Mono.
     * @throws NullPointerException thrown if the {@code hookId} is null.
     * @throws IllegalArgumentException If {@code hookId} does not conform to the UUID format specification.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteHook(String hookId) {
        return deleteHookWithResponse(hookId).then();
    }

    /**
     * Delete a hook.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.deleteHookWithResponse#String -->
     * <pre>
     * final String emailHookId = &quot;f00853f1-6627-447f-bacf-8dccf2e86fed&quot;;
     * metricsAdvisorAdminAsyncClient.deleteHookWithResponse&#40;emailHookId&#41;
     *     .subscribe&#40;response -&gt; &#123;
     *         System.out.printf&#40;&quot;Response statusCode: %d%n&quot;, response.getStatusCode&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.deleteHookWithResponse#String -->
     *
     * @param hookId The hook unique id.
     *
     * @return A {@link Response} of a {@link Mono}.
     * @throws NullPointerException thrown if the {@code hookId} is null.
     * @throws IllegalArgumentException If {@code hookId} does not conform to the UUID format specification.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteHookWithResponse(String hookId) {
        try {
            return withContext(context -> deleteHookWithResponse(hookId, context));
        } catch (RuntimeException e) {
            return FluxUtil.monoError(logger, e);
        }
    }

    Mono<Response<Void>> deleteHookWithResponse(String hookId, Context context) {
        Objects.requireNonNull(hookId, "hookId is required.");
        return service.deleteHookWithResponseAsync(UUID.fromString(hookId), context)
            .doOnRequest(ignoredValue -> logger.info("Deleting NotificationHook"))
            .doOnSuccess(response -> logger.info("Deleted NotificationHook"))
            .doOnError(error -> logger.warning("Failed to delete hook", error));
    }

    /**
     * List information of hooks on the metrics advisor account.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.listHooks -->
     * <pre>
     * metricsAdvisorAdminAsyncClient.listHooks&#40;&#41;
     *     .subscribe&#40;hook -&gt; &#123;
     *         if &#40;hook instanceof EmailNotificationHook&#41; &#123;
     *             EmailNotificationHook emailHook = &#40;EmailNotificationHook&#41; hook;
     *             System.out.printf&#40;&quot;Email Hook Id: %s%n&quot;, emailHook.getId&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Email Hook Name: %s%n&quot;, emailHook.getName&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Email Hook Description: %s%n&quot;, emailHook.getDescription&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Email Hook External Link: %s%n&quot;, emailHook.getExternalLink&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Email Hook Emails: %s%n&quot;, String.join&#40;&quot;,&quot;, emailHook.getEmailsToAlert&#40;&#41;&#41;&#41;;
     *         &#125; else if &#40;hook instanceof WebNotificationHook&#41; &#123;
     *             WebNotificationHook webHook = &#40;WebNotificationHook&#41; hook;
     *             System.out.printf&#40;&quot;Web Hook Id: %s%n&quot;, webHook.getId&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Web Hook Name: %s%n&quot;, webHook.getName&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Web Hook Description: %s%n&quot;, webHook.getDescription&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Web Hook External Link: %s%n&quot;, webHook.getExternalLink&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Web Hook Endpoint: %s%n&quot;, webHook.getEndpoint&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Web Hook Headers: %s%n&quot;, webHook.getHttpHeaders&#40;&#41;&#41;;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.listHooks -->
     *
     * @return A {@link PagedFlux} containing information of all the {@link NotificationHook} in the account.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<NotificationHook> listHooks() {
        return listHooks(new ListHookOptions());
    }

    /**
     * List information of hooks.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.listHooks#ListHookOptions -->
     * <pre>
     * ListHookOptions options = new ListHookOptions&#40;&#41;
     *     .setSkip&#40;100&#41;
     *     .setMaxPageSize&#40;20&#41;;
     * int[] pageCount = new int[1];
     * metricsAdvisorAdminAsyncClient.listHooks&#40;options&#41;.byPage&#40;&#41;
     *     .subscribe&#40;hookPage -&gt; &#123;
     *         System.out.printf&#40;&quot;Page: %d%n&quot;, pageCount[0]++&#41;;
     *         for &#40;NotificationHook notificationHook : hookPage.getElements&#40;&#41;&#41; &#123;
     *             if &#40;notificationHook instanceof EmailNotificationHook&#41; &#123;
     *                 EmailNotificationHook emailHook = &#40;EmailNotificationHook&#41; notificationHook;
     *                 System.out.printf&#40;&quot;Email Hook Id: %s%n&quot;, emailHook.getId&#40;&#41;&#41;;
     *                 System.out.printf&#40;&quot;Email Hook Name: %s%n&quot;, emailHook.getName&#40;&#41;&#41;;
     *                 System.out.printf&#40;&quot;Email Hook Description: %s%n&quot;, emailHook.getDescription&#40;&#41;&#41;;
     *                 System.out.printf&#40;&quot;Email Hook External Link: %s%n&quot;, emailHook.getExternalLink&#40;&#41;&#41;;
     *                 System.out.printf&#40;&quot;Email Hook Emails: %s%n&quot;, String.join&#40;&quot;,&quot;, emailHook.getEmailsToAlert&#40;&#41;&#41;&#41;;
     *                 System.out.printf&#40;&quot;Email Hook Admins: %s%n&quot;, String.join&#40;&quot;,&quot;, emailHook.getAdmins&#40;&#41;&#41;&#41;;
     *             &#125; else if &#40;notificationHook instanceof WebNotificationHook&#41; &#123;
     *                 WebNotificationHook webHook = &#40;WebNotificationHook&#41; notificationHook;
     *                 System.out.printf&#40;&quot;Web Hook Id: %s%n&quot;, webHook.getId&#40;&#41;&#41;;
     *                 System.out.printf&#40;&quot;Web Hook Name: %s%n&quot;, webHook.getName&#40;&#41;&#41;;
     *                 System.out.printf&#40;&quot;Web Hook Description: %s%n&quot;, webHook.getDescription&#40;&#41;&#41;;
     *                 System.out.printf&#40;&quot;Web Hook External Link: %s%n&quot;, webHook.getExternalLink&#40;&#41;&#41;;
     *                 System.out.printf&#40;&quot;Web Hook Endpoint: %s%n&quot;, webHook.getEndpoint&#40;&#41;&#41;;
     *                 System.out.printf&#40;&quot;Web Hook Headers: %s%n&quot;, webHook.getHttpHeaders&#40;&#41;&#41;;
     *                 System.out.printf&#40;&quot;Web Hook Admins: %s%n&quot;, String.join&#40;&quot;,&quot;, webHook.getAdmins&#40;&#41;&#41;&#41;;
     *             &#125;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.listHooks#ListHookOptions -->
     *
     * @param listHookOptions the additional configurable options to specify when listing hooks.
     *
     * @return A {@link PagedFlux} containing information of the {@link NotificationHook} resources.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<NotificationHook> listHooks(ListHookOptions listHookOptions) {
        try {
            return new PagedFlux<>(() ->
                withContext(context ->
                    listHooksSinglePageAsync(listHookOptions, context)),
                continuationToken ->
                    withContext(context -> listHooksNextPageAsync(continuationToken,
                        context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> FluxUtil.monoError(logger, ex));
        }
    }

    PagedFlux<NotificationHook> listHooks(ListHookOptions options, Context context) {
        return new PagedFlux<>(() ->
            listHooksSinglePageAsync(options, context),
            continuationToken ->
                listHooksNextPageAsync(continuationToken, context));
    }

    private Mono<PagedResponse<NotificationHook>> listHooksSinglePageAsync(ListHookOptions options, Context context) {
        return service.listHooksSinglePageAsync(
            options != null ? options.getHookNameFilter() : null,
            options != null ? options.getSkip() : null,
            options != null ? options.getMaxPageSize() : null,
            context.addData(AZ_TRACING_NAMESPACE_KEY, METRICS_ADVISOR_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignoredValue -> logger.info("Listing hooks"))
            .doOnSuccess(response -> logger.info("Listed hooks {}", response))
            .doOnError(error -> logger.warning("Failed to list the hooks", error))
            .map(response -> HookTransforms.fromInnerPagedResponse(logger, response));
    }

    private Mono<PagedResponse<NotificationHook>> listHooksNextPageAsync(String nextPageLink, Context context) {
        if (CoreUtils.isNullOrEmpty(nextPageLink)) {
            return Mono.empty();
        }
        return service.listHooksNextSinglePageAsync(nextPageLink,
            context.addData(AZ_TRACING_NAMESPACE_KEY, METRICS_ADVISOR_TRACING_NAMESPACE_VALUE))
            .doOnSubscribe(ignoredValue -> logger.info("Retrieving the next listing page - Page {}", nextPageLink))
            .doOnSuccess(response -> logger.info("Retrieved the next listing page - Page {} {}",
                nextPageLink,
                response))
            .doOnError(error -> logger.warning("Failed to retrieve the next listing page - Page {}", nextPageLink,
                error))
            .map(response -> HookTransforms.fromInnerPagedResponse(logger, response));
    }

    /**
     * Create a configuration to trigger alert when anomalies are detected.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.createAlertConfig#AnomalyAlertConfiguration -->
     * <pre>
     * String detectionConfigurationId1 = &quot;9ol48er30-6e6e-4391-b78f-b00dfee1e6f5&quot;;
     * String detectionConfigurationId2 = &quot;3e58er30-6e6e-4391-b78f-b00dfee1e6f5&quot;;
     * String hookId1 = &quot;5f48er30-6e6e-4391-b78f-b00dfee1e6f5&quot;;
     * String hookId2 = &quot;8i48er30-6e6e-4391-b78f-b00dfee1e6f5&quot;;
     *
     * metricsAdvisorAdminAsyncClient.createAlertConfig&#40;
     *     new AnomalyAlertConfiguration&#40;&quot;My AnomalyAlert config name&quot;&#41;
     *         .setDescription&#40;&quot;alert config description&quot;&#41;
     *         .setMetricAlertConfigurations&#40;Arrays.asList&#40;
     *             new MetricAlertConfiguration&#40;detectionConfigurationId1,
     *                 MetricAnomalyAlertScope.forWholeSeries&#40;&#41;&#41;,
     *             new MetricAlertConfiguration&#40;detectionConfigurationId2,
     *                 MetricAnomalyAlertScope.forWholeSeries&#40;&#41;&#41;
     *                 .setAlertConditions&#40;new MetricAnomalyAlertConditions&#40;&#41;
     *                     .setSeverityRangeCondition&#40;new SeverityCondition&#40;AnomalySeverity.HIGH, AnomalySeverity.HIGH&#41;&#41;&#41;&#41;&#41;
     *         .setCrossMetricsOperator&#40;MetricAlertConfigurationsOperator.AND&#41;
     *         .setHookIdsToAlert&#40;Arrays.asList&#40;hookId1, hookId2&#41;&#41;&#41;
     *     .subscribe&#40;anomalyAlertConfiguration -&gt; &#123;
     *         System.out.printf&#40;&quot;DataPoint Anomaly alert configuration Id: %s%n&quot;, anomalyAlertConfiguration.getId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;DataPoint Anomaly alert configuration description: %s%n&quot;,
     *             anomalyAlertConfiguration.getDescription&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;DataPoint Anomaly alert configuration hook ids: %s%n&quot;,
     *             anomalyAlertConfiguration.getHookIdsToAlert&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;DataPoint Anomaly alert configuration cross metrics operator: %s%n&quot;,
     *             anomalyAlertConfiguration.getCrossMetricsOperator&#40;&#41;.toString&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.createAlertConfig#AnomalyAlertConfiguration -->
     *
     * @param alertConfiguration The anomaly alerting configuration.
     *
     * @return A {@link Mono} containing the created {@link AnomalyAlertConfiguration}.
     * @throws NullPointerException thrown if the {@code alertConfiguration} or
     * {@code alertConfiguration.metricAnomalyAlertConfigurations} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<AnomalyAlertConfiguration> createAlertConfig(
        AnomalyAlertConfiguration alertConfiguration) {
        return createAlertConfigWithResponse(alertConfiguration).flatMap(FluxUtil::toMono);
    }

    /**
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.createAlertConfigWithResponse#AnomalyAlertConfiguration -->
     * <pre>
     *
     * String detectionConfigurationId1 = &quot;9ol48er30-6e6e-4391-b78f-b00dfee1e6f5&quot;;
     * String detectionConfigurationId2 = &quot;3e58er30-6e6e-4391-b78f-b00dfee1e6f5&quot;;
     * String hookId1 = &quot;5f48er30-6e6e-4391-b78f-b00dfee1e6f5&quot;;
     * String hookId2 = &quot;8i48er30-6e6e-4391-b78f-b00dfee1e6f5&quot;;
     *
     * metricsAdvisorAdminAsyncClient.createAlertConfigWithResponse&#40;
     *     new AnomalyAlertConfiguration&#40;&quot;My AnomalyAlert config name&quot;&#41;
     *         .setDescription&#40;&quot;alert config description&quot;&#41;
     *         .setMetricAlertConfigurations&#40;Arrays.asList&#40;
     *             new MetricAlertConfiguration&#40;detectionConfigurationId1,
     *                 MetricAnomalyAlertScope.forWholeSeries&#40;&#41;&#41;,
     *             new MetricAlertConfiguration&#40;detectionConfigurationId2,
     *                 MetricAnomalyAlertScope.forWholeSeries&#40;&#41;&#41;
     *                 .setAlertConditions&#40;new MetricAnomalyAlertConditions&#40;&#41;
     *                     .setSeverityRangeCondition&#40;new SeverityCondition&#40;AnomalySeverity.HIGH, AnomalySeverity.HIGH&#41;&#41;&#41;&#41;&#41;
     *         .setCrossMetricsOperator&#40;MetricAlertConfigurationsOperator.AND&#41;
     *         .setHookIdsToAlert&#40;Arrays.asList&#40;hookId1, hookId2&#41;&#41;&#41;
     *     .subscribe&#40;alertConfigurationResponse -&gt; &#123;
     *         System.out.printf&#40;&quot;DataPoint Anomaly alert creation operation status: %s%n&quot;,
     *             alertConfigurationResponse.getStatusCode&#40;&#41;&#41;;
     *         final AnomalyAlertConfiguration anomalyAlertConfiguration = alertConfigurationResponse.getValue&#40;&#41;;
     *         System.out.printf&#40;&quot;DataPoint Anomaly alert configuration Id: %s%n&quot;, anomalyAlertConfiguration.getId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;DataPoint Anomaly alert configuration description: %s%n&quot;,
     *             anomalyAlertConfiguration.getDescription&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;DataPoint Anomaly alert configuration hook ids: %s%n&quot;,
     *             anomalyAlertConfiguration.getHookIdsToAlert&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;DataPoint Anomaly alert configuration cross metrics operator: %s%n&quot;,
     *             anomalyAlertConfiguration.getCrossMetricsOperator&#40;&#41;.toString&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.createAlertConfigWithResponse#AnomalyAlertConfiguration -->
     *
     * @param alertConfiguration The anomaly alerting configuration.
     *
     * @return A {@link Response} of a {@link Mono} containing the created {@link AnomalyAlertConfiguration}.
     * @throws NullPointerException thrown if the {@code alertConfiguration} or
     * {@code alertConfiguration.metricAnomalyAlertConfigurations} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<AnomalyAlertConfiguration>> createAlertConfigWithResponse(
        AnomalyAlertConfiguration alertConfiguration) {
        try {
            return withContext(context -> createAlertConfigWithResponse(alertConfiguration, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<AnomalyAlertConfiguration>> createAlertConfigWithResponse(
        AnomalyAlertConfiguration alertConfiguration, Context context) {
        Objects.requireNonNull(alertConfiguration, "'alertConfiguration' is required.");
        if (CoreUtils.isNullOrEmpty(alertConfiguration.getMetricAlertConfigurations())) {
            Objects.requireNonNull("'alertConfiguration.metricAnomalyAlertConfigurations' is required");
        }
        if (alertConfiguration.getCrossMetricsOperator() == null
            && alertConfiguration.getMetricAlertConfigurations().size() > 1) {
            throw logger.logExceptionAsError(new IllegalArgumentException("crossMetricsOperator is required"
                + " when there are more than one metric level alert configuration."));
        }

        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, METRICS_ADVISOR_TRACING_NAMESPACE_VALUE);
        final AnomalyAlertingConfiguration innerAlertConfiguration
            = AlertConfigurationTransforms.toInnerForCreate(alertConfiguration);

        return service.createAnomalyAlertingConfigurationWithResponseAsync(innerAlertConfiguration, withTracing)
            .doOnSubscribe(ignoredValue -> logger.info("Creating AnomalyAlertConfiguration - {}",
                innerAlertConfiguration))
            .doOnSuccess(response -> logger.info("Created AnomalyAlertConfiguration - {}", response))
            .doOnError(error -> logger.warning("Failed to create AnomalyAlertConfiguration - {}",
                innerAlertConfiguration, error))
            .flatMap(response -> {
                final String configurationId = parseOperationId(response.getDeserializedHeaders().getLocation());
                return getAlertConfigWithResponse(configurationId, context)
                    .map(getResponse -> new ResponseBase<Void, AnomalyAlertConfiguration>(response.getRequest(),
                        response.getStatusCode(),
                        response.getHeaders(),
                        getResponse.getValue(),
                        null));
            });
    }

    /**
     * Get the anomaly alert configuration identified by {@code alertConfigurationId}.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.getAlertConfig#String -->
     * <pre>
     * String alertConfigId = &quot;1p0f8er30-6e6e-4391-b78f-bpfdfee1e6f5&quot;;
     *
     * metricsAdvisorAdminAsyncClient.getAlertConfig&#40;alertConfigId&#41;
     *     .subscribe&#40;anomalyAlertConfiguration -&gt; &#123;
     *         System.out.printf&#40;&quot;DataPoint Anomaly alert configuration Id: %s%n&quot;, anomalyAlertConfiguration.getId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;DataPoint Anomaly alert configuration description: %s%n&quot;,
     *             anomalyAlertConfiguration.getDescription&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;DataPoint Anomaly alert configuration hook ids: %s%n&quot;,
     *             anomalyAlertConfiguration.getHookIdsToAlert&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;DataPoint Anomaly alert configuration cross metrics operator: %s%n&quot;,
     *             anomalyAlertConfiguration.getCrossMetricsOperator&#40;&#41;.toString&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.getAlertConfig#String -->
     *
     * @param alertConfigurationId The anomaly alert configuration id.
     *
     * @return A {@link Mono} containing the {@link AnomalyAlertConfiguration} identified by the given id.
     * @throws IllegalArgumentException If {@code alertConfigurationId} does not conform to the
     * UUID format specification.
     * @throws NullPointerException thrown if the {@code alertConfigurationId} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<AnomalyAlertConfiguration> getAlertConfig(
        String alertConfigurationId) {
        return getAlertConfigWithResponse(alertConfigurationId).flatMap(FluxUtil::toMono);
    }

    /**
     * Get the anomaly alert configuration identified by {@code alertConfigurationId}.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.getAlertConfigWithResponse#String -->
     * <pre>
     * String alertConfigId = &quot;1p0f8er30-6e6e-4391-b78f-bpfdfee1e6f5&quot;;
     *
     * metricsAdvisorAdminAsyncClient.getAlertConfigWithResponse&#40;alertConfigId&#41;
     *     .subscribe&#40;alertConfigurationResponse -&gt; &#123;
     *         System.out.printf&#40;&quot;DataPointAnomaly alert creation operation status: %s%n&quot;,
     *             alertConfigurationResponse.getStatusCode&#40;&#41;&#41;;
     *         final AnomalyAlertConfiguration anomalyAlertConfiguration = alertConfigurationResponse.getValue&#40;&#41;;
     *         System.out.printf&#40;&quot;DataPoint Anomaly alert configuration Id: %s%n&quot;, anomalyAlertConfiguration.getId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;DataPoint Anomaly alert configuration description: %s%n&quot;,
     *             anomalyAlertConfiguration.getDescription&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;DataPoint Anomaly alert configuration hook ids: %s%n&quot;,
     *             anomalyAlertConfiguration.getHookIdsToAlert&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;DataPoint Anomaly alert configuration cross metrics operator: %s%n&quot;,
     *             anomalyAlertConfiguration.getCrossMetricsOperator&#40;&#41;.toString&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.getAlertConfigWithResponse#String -->
     *
     * @param alertConfigurationId The anomaly alert configuration id.
     *
     * @return A {@link Response response} of a {@link Mono}
     * containing the {@link AnomalyAlertConfiguration} identified by the given id.
     * @throws IllegalArgumentException If {@code alertConfigurationId} does not conform to the
     * UUID format specification.
     * @throws NullPointerException thrown if the {@code alertConfigurationId} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<AnomalyAlertConfiguration>> getAlertConfigWithResponse(
        String alertConfigurationId) {
        try {
            return withContext(context -> getAlertConfigWithResponse(alertConfigurationId, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<AnomalyAlertConfiguration>> getAlertConfigWithResponse(
        String alertConfigurationId, Context context) {
        Objects.requireNonNull(alertConfigurationId, "'alertConfigurationId' is required.");
        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, METRICS_ADVISOR_TRACING_NAMESPACE_VALUE);

        return service.getAnomalyAlertingConfigurationWithResponseAsync(UUID.fromString(alertConfigurationId),
            withTracing)
            .doOnSubscribe(ignoredValue -> logger.info("Retrieving AnomalyDetectionConfiguration - {}",
                alertConfigurationId))
            .doOnSuccess(response -> logger.info("Retrieved AnomalyDetectionConfiguration - {}", response))
            .doOnError(error -> logger.warning("Failed to retrieve AnomalyDetectionConfiguration - {}",
                alertConfigurationId, error))
            .map(response -> new ResponseBase<Void, AnomalyAlertConfiguration>(response.getRequest(),
                response.getStatusCode(),
                response.getHeaders(), AlertConfigurationTransforms.fromInner(response.getValue()), null));
    }

    /**
     * Update anomaly alert configuration.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.updateAlertConfig#AnomalyAlertConfiguration -->
     * <pre>
     *
     * String alertConfigId = &quot;1p0f8er30-6e6e-4391-b78f-bpfdfee1e6f5&quot;;
     * String additionalHookId = &quot;2gh8er30-6e6e-4391-b78f-bpfdfee1e6f5&quot;;
     *
     * metricsAdvisorAdminAsyncClient.getAlertConfig&#40;alertConfigId&#41;
     *     .flatMap&#40;existingAnomalyConfig -&gt; &#123;
     *         List&lt;String&gt; hookIds = new ArrayList&lt;&gt;&#40;existingAnomalyConfig.getHookIdsToAlert&#40;&#41;&#41;;
     *         hookIds.add&#40;additionalHookId&#41;;
     *         return metricsAdvisorAdminAsyncClient.updateAlertConfig&#40;
     *             existingAnomalyConfig
     *                 .setHookIdsToAlert&#40;hookIds&#41;
     *                 .setDescription&#40;&quot;updated to add more hook ids&quot;&#41;&#41;;
     *     &#125;&#41;.subscribe&#40;updateAnomalyAlertConfiguration -&gt; &#123;
     *         System.out.printf&#40;&quot;Updated anomaly alert configuration Id: %s%n&quot;,
     *             updateAnomalyAlertConfiguration.getId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Updated anomaly alert configuration description: %s%n&quot;,
     *             updateAnomalyAlertConfiguration.getDescription&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Updated anomaly alert configuration hook ids: %s%n&quot;,
     *             updateAnomalyAlertConfiguration.getHookIdsToAlert&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.updateAlertConfig#AnomalyAlertConfiguration -->
     *
     * @param alertConfiguration The anomaly alert configuration to update.
     *
     * @return A {@link Mono} containing the {@link AnomalyAlertConfiguration} that was updated.
     * @throws NullPointerException thrown if {@code alertConfiguration} or
     * {@code alertConfiguration.metricAnomalyAlertConfigurations} is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<AnomalyAlertConfiguration> updateAlertConfig(
        AnomalyAlertConfiguration alertConfiguration) {
        return updateAlertConfigWithResponse(alertConfiguration).flatMap(FluxUtil::toMono);
    }

    /**
     * Update anomaly alert configuration.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.updateAlertConfigWithResponse#AnomalyAlertConfiguration -->
     * <pre>
     *
     * String alertConfigId = &quot;1p0f8er30-6e6e-4391-b78f-bpfdfee1e6f5&quot;;
     * String additionalHookId = &quot;2gh8er30-6e6e-4391-b78f-bpfdfee1e6f5&quot;;
     *
     * metricsAdvisorAdminAsyncClient.getAlertConfig&#40;alertConfigId&#41;
     *     .flatMap&#40;existingAnomalyConfig -&gt; &#123;
     *         List&lt;String&gt; hookIds = new ArrayList&lt;&gt;&#40;existingAnomalyConfig.getHookIdsToAlert&#40;&#41;&#41;;
     *         hookIds.add&#40;additionalHookId&#41;;
     *         return metricsAdvisorAdminAsyncClient.updateAlertConfigWithResponse&#40;
     *             existingAnomalyConfig
     *                 .setHookIdsToAlert&#40;hookIds&#41;
     *                 .setDescription&#40;&quot;updated to add more hook ids&quot;&#41;&#41;;
     *     &#125;&#41;.subscribe&#40;alertConfigurationResponse -&gt; &#123;
     *         System.out.printf&#40;&quot;Update anomaly alert operation status: %s%n&quot;,
     *             alertConfigurationResponse.getStatusCode&#40;&#41;&#41;;
     *         final AnomalyAlertConfiguration updatAnomalyAlertConfiguration = alertConfigurationResponse.getValue&#40;&#41;;
     *         System.out.printf&#40;&quot;Updated anomaly alert configuration Id: %s%n&quot;,
     *             updatAnomalyAlertConfiguration.getId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Updated anomaly alert configuration description: %s%n&quot;,
     *             updatAnomalyAlertConfiguration.getDescription&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;Updated anomaly alert configuration hook ids: %s%n&quot;,
     *             updatAnomalyAlertConfiguration.getHookIdsToAlert&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.updateAlertConfigWithResponse#AnomalyAlertConfiguration -->
     *
     * @param alertConfiguration The anomaly alert configuration to update.
     *
     * @return A {@link Response} of a {@link Mono} containing the {@link AnomalyAlertConfiguration} that was updated.
     * @throws NullPointerException thrown if {@code alertConfiguration} or
     * {@code alertConfiguration.metricAnomalyAlertConfigurations} is null or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<AnomalyAlertConfiguration>> updateAlertConfigWithResponse(
        AnomalyAlertConfiguration alertConfiguration) {
        try {
            return withContext(context -> updateAlertConfigWithResponse(alertConfiguration, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<AnomalyAlertConfiguration>> updateAlertConfigWithResponse(
        AnomalyAlertConfiguration alertConfiguration, Context context) {
        Objects.requireNonNull(alertConfiguration, "'alertConfiguration' is required");
        if (CoreUtils.isNullOrEmpty(alertConfiguration.getMetricAlertConfigurations())) {
            throw logger.logExceptionAsError(new NullPointerException(
                "'alertConfiguration.metricAnomalyAlertConfigurations' is required and cannot be empty"));
        }
        final AnomalyAlertingConfigurationPatch innerAlertConfiguration
            = AlertConfigurationTransforms.toInnerForUpdate(alertConfiguration);
        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, METRICS_ADVISOR_TRACING_NAMESPACE_VALUE);

        return service.updateAnomalyAlertingConfigurationWithResponseAsync(
            UUID.fromString(alertConfiguration.getId()),
            innerAlertConfiguration,
            withTracing)
            .doOnSubscribe(ignoredValue -> logger.info("Updating AnomalyAlertConfiguration - {}",
                innerAlertConfiguration))
            .doOnSuccess(response -> logger.info("Updated AnomalyAlertConfiguration - {}", response))
            .doOnError(error -> logger.warning("Failed to update AnomalyAlertConfiguration - {}",
                innerAlertConfiguration, error))
            .flatMap(response -> getAlertConfigWithResponse(alertConfiguration.getId(), context)
                .map(getResponse -> new ResponseBase<Void, AnomalyAlertConfiguration>(response.getRequest(),
                    response.getStatusCode(),
                    response.getHeaders(),
                    getResponse.getValue(),
                    null)));
    }

    /**
     * Deletes the anomaly alert configuration identified by {@code alertConfigurationId}.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.deleteAlertConfig#String -->
     * <pre>
     * String alertConfigId = &quot;1p0f8er30-6e6e-4391-b78f-bpfdfee1e6f5&quot;;
     * metricsAdvisorAdminAsyncClient.deleteAlertConfig&#40;alertConfigId&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.deleteAlertConfig#String -->
     *
     * @param alertConfigurationId The anomaly alert configuration id.
     *
     * @return An empty Mono.
     * @throws IllegalArgumentException If {@code alertConfigurationId} does not conform to the
     * UUID format specification.
     * @throws NullPointerException thrown if the {@code alertConfigurationId} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteAlertConfig(String alertConfigurationId) {
        return deleteAlertConfigWithResponse(alertConfigurationId).flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes the anomaly alert configuration identified by {@code alertConfigurationId}.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.deleteAlertConfigWithResponse#String -->
     * <pre>
     * String alertConfigId = &quot;1p0f8er30-6e6e-4391-b78f-bpfdfee1e6f5&quot;;
     *
     * metricsAdvisorAdminAsyncClient.deleteAlertConfigWithResponse&#40;alertConfigId&#41;
     *     .subscribe&#40;response -&gt; &#123;
     *         System.out.printf&#40;&quot;DataPoint  Anomaly alert config delete operation status : %s%n&quot;,
     *             response.getStatusCode&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.deleteAlertConfigWithResponse#String -->
     *
     * @param alertConfigurationId The anomaly alert configuration id.
     *
     * @return A response containing status code and headers returned after the operation.
     * @throws IllegalArgumentException If {@code alertConfigurationId} does not conform to the
     * UUID format specification.
     * @throws NullPointerException thrown if the {@code alertConfigurationId} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteAlertConfigWithResponse(String alertConfigurationId) {
        try {
            return withContext(context -> deleteAlertConfigWithResponse(alertConfigurationId, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> deleteAlertConfigWithResponse(String alertConfigurationId, Context context) {
        Objects.requireNonNull(alertConfigurationId, "'alertConfigurationId' is required.");
        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, METRICS_ADVISOR_TRACING_NAMESPACE_VALUE);

        return service.deleteAnomalyAlertingConfigurationWithResponseAsync(UUID.fromString(alertConfigurationId),
            withTracing)
            .doOnSubscribe(ignoredValue -> logger.info("Deleting AnomalyAlertConfiguration - {}", alertConfigurationId))
            .doOnSuccess(response -> logger.info("Deleted AnomalyAlertConfiguration - {}", response))
            .doOnError(error -> logger.warning("Failed to delete AnomalyAlertConfiguration - {}",
                alertConfigurationId, error));
    }

    /**
     * Fetch the anomaly alert configurations associated with a detection configuration.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.listAlertConfigs#String-ListAnomalyAlertConfigsOptions -->
     * <pre>
     * String detectionConfigId = &quot;3rt98er30-6e6e-4391-b78f-bpfdfee1e6f5&quot;;
     * metricsAdvisorAdminAsyncClient.listAlertConfigs&#40;detectionConfigId, new ListAnomalyAlertConfigsOptions&#40;&#41;&#41;
     *     .subscribe&#40;anomalyAlertConfiguration -&gt; &#123;
     *         System.out.printf&#40;&quot;DataPoint Anomaly alert configuration Id: %s%n&quot;, anomalyAlertConfiguration.getId&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;DataPoint Anomaly alert configuration description: %s%n&quot;,
     *             anomalyAlertConfiguration.getDescription&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;DataPoint Anomaly alert configuration hook ids: %s%n&quot;,
     *             anomalyAlertConfiguration.getHookIdsToAlert&#40;&#41;&#41;;
     *         System.out.printf&#40;&quot;DataPoint Anomaly alert configuration cross metrics operator: %s%n&quot;,
     *             anomalyAlertConfiguration.getCrossMetricsOperator&#40;&#41;.toString&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.listAlertConfigs#String-ListAnomalyAlertConfigsOptions -->
     *
     * @param detectionConfigurationId The id of the detection configuration.
     * @param listAnomalyAlertConfigsOptions th e additional configurable options to specify when querying the result.
     *
     * @return A {@link PagedFlux} containing information of all the
     * {@link AnomalyAlertConfiguration anomaly alert configurations} for the specified detection configuration.
     * @throws NullPointerException thrown if the {@code detectionConfigurationId} is null.
     * @throws IllegalArgumentException If {@code detectionConfigurationId} does not conform to the
     * UUID format specification.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<AnomalyAlertConfiguration> listAlertConfigs(
        String detectionConfigurationId, ListAnomalyAlertConfigsOptions listAnomalyAlertConfigsOptions) {
        try {
            return new PagedFlux<>(() ->
                withContext(context ->
                    listAnomalyAlertConfigsSinglePageAsync(detectionConfigurationId,
                        listAnomalyAlertConfigsOptions,
                        context)),
                continuationToken ->
                    withContext(context -> listAnomalyAlertConfigsNextPageAsync(continuationToken,
                        context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> FluxUtil.monoError(logger, ex));
        }
    }

    PagedFlux<AnomalyAlertConfiguration> listAlertConfigs(
        String detectionConfigurationId, ListAnomalyAlertConfigsOptions options, Context context) {
        return new PagedFlux<>(() ->
            listAnomalyAlertConfigsSinglePageAsync(detectionConfigurationId,
                options,
                context),
            continuationToken ->
                listAnomalyAlertConfigsNextPageAsync(continuationToken, context));
    }

    private Mono<PagedResponse<AnomalyAlertConfiguration>> listAnomalyAlertConfigsSinglePageAsync(
        String detectionConfigurationId, ListAnomalyAlertConfigsOptions options, Context context) {
        Objects.requireNonNull(detectionConfigurationId, "'detectionConfigurationId' is required.");
        if (options == null) {
            options = new ListAnomalyAlertConfigsOptions();
        }
        return service.getAnomalyAlertingConfigurationsByAnomalyDetectionConfigurationSinglePageAsync(
            UUID.fromString(detectionConfigurationId), options.getSkip(), options.getMaxPageSize(),
            context.addData(AZ_TRACING_NAMESPACE_KEY, METRICS_ADVISOR_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignoredValue -> logger.info("Listing AnomalyAlertConfigs"))
            .doOnSuccess(response -> logger.info("Listed AnomalyAlertConfigs {}", response))
            .doOnError(error -> logger.warning("Failed to list the AnomalyAlertConfigs", error))
            .map(response -> AlertConfigurationTransforms.fromInnerPagedResponse(response));
    }

    private Mono<PagedResponse<AnomalyAlertConfiguration>> listAnomalyAlertConfigsNextPageAsync(
        String nextPageLink, Context context) {
        if (CoreUtils.isNullOrEmpty(nextPageLink)) {
            return Mono.empty();
        }
        return service.getAnomalyAlertingConfigurationsByAnomalyDetectionConfigurationNextSinglePageAsync(nextPageLink,
            context.addData(AZ_TRACING_NAMESPACE_KEY, METRICS_ADVISOR_TRACING_NAMESPACE_VALUE))
            .doOnSubscribe(ignoredValue -> logger.info("Retrieving the next listing page - Page {}", nextPageLink))
            .doOnSuccess(response -> logger.info("Retrieved the next listing page - Page {} {}",
                nextPageLink,
                response))
            .doOnError(error -> logger.warning("Failed to retrieve the next listing page - Page {}", nextPageLink,
                error))
            .map(response -> AlertConfigurationTransforms.fromInnerPagedResponse(response));
    }

    /**
     * Create a data source credential entity.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.createDataSourceCredential#DatasourceCredentialEntity -->
     * <pre>
     * DataSourceCredentialEntity datasourceCredential;
     * final String name = &quot;sample_name&quot; + UUID.randomUUID&#40;&#41;;
     * final String cId = &quot;f45668b2-bffa-11eb-8529-0246ac130003&quot;;
     * final String tId = &quot;67890ded-5e07-4e52-b225-4ae8f905afb5&quot;;
     * final String mockSecr = &quot;890hy69-5e07-4e52-b225-4ae8f905afb5&quot;;
     *
     * datasourceCredential = new DataSourceServicePrincipalInKeyVault&#40;&#41;
     *     .setName&#40;name&#41;
     *     .setKeyVaultForDataSourceSecrets&#40;&quot;kv&quot;, cId, mockSecr&#41;
     *     .setTenantId&#40;tId&#41;
     *     .setSecretNameForDataSourceClientId&#40;&quot;DSClientID_1&quot;&#41;
     *     .setSecretNameForDataSourceClientSecret&#40;&quot;DSClientSer_1&quot;&#41;;
     *
     * metricsAdvisorAdminAsyncClient.createDataSourceCredential&#40;datasourceCredential&#41;
     *     .subscribe&#40;credentialEntity -&gt; &#123;
     *         if &#40;credentialEntity instanceof DataSourceServicePrincipalInKeyVault&#41; &#123;
     *             DataSourceServicePrincipalInKeyVault actualCredentialSPInKV
     *                 = &#40;DataSourceServicePrincipalInKeyVault&#41; credentialEntity;
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
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.createDataSourceCredential#DatasourceCredentialEntity -->
     *
     * @param dataSourceCredential The credential entity.
     * @return A {@link Mono} containing the created {@link DataSourceCredentialEntity}.
     * @throws NullPointerException thrown if the {@code credentialEntity} is null
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DataSourceCredentialEntity> createDataSourceCredential(
        DataSourceCredentialEntity dataSourceCredential) {
        return createDataSourceCredentialWithResponse(dataSourceCredential)
            .map(Response::getValue);
    }

    /**
     * Create a data source credential entity with REST response.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.createDataSourceCredentialWithResponse#DatasourceCredentialEntity -->
     * <pre>
     * DataSourceCredentialEntity datasourceCredential;
     * final String name = &quot;sample_name&quot; + UUID.randomUUID&#40;&#41;;
     * final String cId = &quot;f45668b2-bffa-11eb-8529-0246ac130003&quot;;
     * final String tId = &quot;67890ded-5e07-4e52-b225-4ae8f905afb5&quot;;
     * final String mockSecr = &quot;890hy69-5e07-4e52-b225-4ae8f905afb5&quot;;
     *
     * datasourceCredential = new DataSourceServicePrincipalInKeyVault&#40;&#41;
     *     .setName&#40;name&#41;
     *     .setKeyVaultForDataSourceSecrets&#40;&quot;kv&quot;, cId, mockSecr&#41;
     *     .setTenantId&#40;tId&#41;
     *     .setSecretNameForDataSourceClientId&#40;&quot;DSClientID_1&quot;&#41;
     *     .setSecretNameForDataSourceClientSecret&#40;&quot;DSClientSer_1&quot;&#41;;
     *
     * metricsAdvisorAdminAsyncClient.createDataSourceCredentialWithResponse&#40;datasourceCredential&#41;
     *     .subscribe&#40;credentialEntityWithResponse -&gt; &#123;
     *         System.out.printf&#40;&quot;Credential Entity creation operation status: %s%n&quot;,
     *             credentialEntityWithResponse.getStatusCode&#40;&#41;&#41;;
     *         if &#40;credentialEntityWithResponse.getValue&#40;&#41; instanceof DataSourceServicePrincipalInKeyVault&#41; &#123;
     *             DataSourceServicePrincipalInKeyVault actualCredentialSPInKV
     *                 = &#40;DataSourceServicePrincipalInKeyVault&#41; credentialEntityWithResponse.getValue&#40;&#41;;
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
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.createDataSourceCredentialWithResponse#DatasourceCredentialEntity -->
     *
     * @param dataSourceCredential The credential entity.
     * @return A {@link Mono} containing the created {@link DataSourceCredentialEntity}.
     * @throws NullPointerException thrown if the {@code credentialEntity} is null
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DataSourceCredentialEntity>> createDataSourceCredentialWithResponse(
        DataSourceCredentialEntity dataSourceCredential) {
        try {
            return withContext(context -> createDataSourceCredentialWithResponse(dataSourceCredential,
                context));
        } catch (RuntimeException e) {
            return FluxUtil.monoError(logger, e);
        }
    }

    Mono<Response<DataSourceCredentialEntity>> createDataSourceCredentialWithResponse(
        DataSourceCredentialEntity dataSourceCredential,
        Context context) {
        Objects.requireNonNull(dataSourceCredential, "dataSourceCredential is required");

        final DataSourceCredential
            innerDataSourceCredential = DataSourceCredentialEntityTransforms.toInnerForCreate(dataSourceCredential);
        return service.createCredentialWithResponseAsync(innerDataSourceCredential,
            context.addData(AZ_TRACING_NAMESPACE_KEY, METRICS_ADVISOR_TRACING_NAMESPACE_VALUE))
            .doOnSubscribe(ignoredValue -> logger.info("Creating DataSourceCredentialEntity"))
            .doOnSuccess(response -> logger.info("Created DataSourceCredentialEntity"))
            .doOnError(error -> logger.warning("Failed to create DataSourceCredentialEntity", error))
            .flatMap(response -> {
                final String credentialId
                    = Utility.parseOperationId(response.getDeserializedHeaders().getLocation());
                return this.getDataSourceCredentialWithResponse(credentialId, context)
                    .map(configurationResponse -> new ResponseBase<Void, DataSourceCredentialEntity>(
                        response.getRequest(),
                        response.getStatusCode(),
                        response.getHeaders(),
                        configurationResponse.getValue(),
                        null));
            });
    }

    /**
     * Update a data source credential entity.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.updateDataSourceCredential#DatasourceCredentialEntity -->
     * <pre>
     * String credentialId = &quot;&quot;;
     * metricsAdvisorAdminAsyncClient.getDataSourceCredential&#40;credentialId&#41;
     *     .flatMap&#40;existingDatasourceCredential -&gt; &#123;
     *         DataSourceServicePrincipalInKeyVault actualCredentialSPInKV = null;
     *         if &#40;existingDatasourceCredential instanceof DataSourceServicePrincipalInKeyVault&#41; &#123;
     *             actualCredentialSPInKV  = &#40;DataSourceServicePrincipalInKeyVault&#41; existingDatasourceCredential;
     *         &#125;
     *         return metricsAdvisorAdminAsyncClient.updateDataSourceCredential&#40;
     *             actualCredentialSPInKV.setDescription&#40;&quot;set updated description&quot;&#41;&#41;;
     *     &#125;&#41;
     *     .subscribe&#40;credentialEntity -&gt; &#123;
     *         if &#40;credentialEntity instanceof DataSourceServicePrincipalInKeyVault&#41; &#123;
     *             DataSourceServicePrincipalInKeyVault actualCredentialSPInKV
     *                 = &#40;DataSourceServicePrincipalInKeyVault&#41; credentialEntity;
     *             System.out.printf&#40;&quot;Actual credential entity key vault endpoint: %s%n&quot;,
     *                     actualCredentialSPInKV.getKeyVaultEndpoint&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Actual credential entity key vault updated description: %s%n&quot;,
     *                 actualCredentialSPInKV.getDescription&#40;&#41;&#41;;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.updateDataSourceCredential#DatasourceCredentialEntity -->
     *
     * @param dataSourceCredential The credential entity.
     * @return A {@link Mono} containing the updated {@link DataSourceCredentialEntity}.
     * @throws NullPointerException thrown if the {@code credentialEntity} is null
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DataSourceCredentialEntity> updateDataSourceCredential(
        DataSourceCredentialEntity dataSourceCredential) {
        return updateDataSourceCredentialWithResponse(dataSourceCredential)
            .map(Response::getValue);
    }

    /**
     * Update a data source credential entity with REST response.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.updateDataSourceCredentialWithResponse#DatasourceCredentialEntity -->
     * <pre>
     * String credentialId = &quot;&quot;;
     * metricsAdvisorAdminAsyncClient.getDataSourceCredential&#40;credentialId&#41;
     *     .flatMap&#40;existingDatasourceCredential -&gt; &#123;
     *         DataSourceServicePrincipalInKeyVault actualCredentialSPInKV = null;
     *         if &#40;existingDatasourceCredential instanceof DataSourceServicePrincipalInKeyVault&#41; &#123;
     *             actualCredentialSPInKV  = &#40;DataSourceServicePrincipalInKeyVault&#41; existingDatasourceCredential;
     *         &#125;
     *         return metricsAdvisorAdminAsyncClient.updateDataSourceCredentialWithResponse&#40;
     *             actualCredentialSPInKV.setDescription&#40;&quot;set updated description&quot;&#41;&#41;;
     *     &#125;&#41;
     *     .subscribe&#40;credentialEntityWithResponse -&gt; &#123;
     *         System.out.printf&#40;&quot;Credential Entity creation operation status: %s%n&quot;,
     *             credentialEntityWithResponse.getStatusCode&#40;&#41;&#41;;
     *         if &#40;credentialEntityWithResponse.getValue&#40;&#41; instanceof DataSourceServicePrincipalInKeyVault&#41; &#123;
     *             DataSourceServicePrincipalInKeyVault actualCredentialSPInKV
     *                 = &#40;DataSourceServicePrincipalInKeyVault&#41; credentialEntityWithResponse.getValue&#40;&#41;;
     *             System.out.printf&#40;&quot;Actual credential entity key vault endpoint: %s%n&quot;,
     *                 actualCredentialSPInKV.getKeyVaultEndpoint&#40;&#41;&#41;;
     *             System.out.printf&#40;&quot;Actual credential entity key vault updated description: %s%n&quot;,
     *                 actualCredentialSPInKV.getDescription&#40;&#41;&#41;;
     *         &#125;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.updateDataSourceCredentialWithResponse#DatasourceCredentialEntity -->
     *
     * @param dataSourceCredential The credential entity.
     * @return A {@link Mono} containing the updated {@link DataSourceCredentialEntity}.
     * @throws NullPointerException thrown if the {@code credentialEntity} is null
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DataSourceCredentialEntity>> updateDataSourceCredentialWithResponse(
        DataSourceCredentialEntity dataSourceCredential) {
        try {
            return withContext(context -> updateDataSourceCredentialWithResponse(dataSourceCredential,
                context));
        } catch (RuntimeException e) {
            return FluxUtil.monoError(logger, e);
        }
    }

    Mono<Response<DataSourceCredentialEntity>> updateDataSourceCredentialWithResponse(
        DataSourceCredentialEntity dataSourceCredential,
        Context context) {
        Objects.requireNonNull(dataSourceCredential, "dataSourceCredential is required");

        final DataSourceCredentialPatch
            innerDataSourceCredential = DataSourceCredentialEntityTransforms.toInnerForUpdate(dataSourceCredential);
        return service.updateCredentialWithResponseAsync(UUID.fromString(dataSourceCredential.getId()),
            innerDataSourceCredential,
            context.addData(AZ_TRACING_NAMESPACE_KEY, METRICS_ADVISOR_TRACING_NAMESPACE_VALUE))
            .doOnSubscribe(ignoredValue -> logger.info("Updating DataSourceCredentialEntity"))
            .doOnSuccess(response -> logger.info("Updated DataSourceCredentialEntity"))
            .doOnError(error -> logger.warning("Failed to update DataSourceCredentialEntity", error))
            .flatMap(response -> {
                return this.getDataSourceCredentialWithResponse(dataSourceCredential.getId(), context)
                    .map(configurationResponse -> new ResponseBase<Void, DataSourceCredentialEntity>(
                        response.getRequest(),
                        response.getStatusCode(),
                        response.getHeaders(),
                        configurationResponse.getValue(),
                        null));
            });
    }

    /**
     * Get a data source credential entity by its id.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.getDataSourceCredential#String -->
     * <pre>
     * final String datasourceCredentialId = &quot;f45668b2-bffa-11eb-8529-0246ac130003&quot;;
     *
     * metricsAdvisorAdminAsyncClient.getDataSourceCredential&#40;datasourceCredentialId&#41;
     *     .subscribe&#40;credentialEntity -&gt; &#123;
     *         if &#40;credentialEntity instanceof DataSourceServicePrincipalInKeyVault&#41; &#123;
     *             DataSourceServicePrincipalInKeyVault actualCredentialSPInKV
     *                 = &#40;DataSourceServicePrincipalInKeyVault&#41; credentialEntity;
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
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.getDataSourceCredential#String -->
     *
     * @param credentialId The data source credential entity unique id.
     *
     * @return The data source credential entity for the provided id.
     * @throws IllegalArgumentException If {@code credentialId} does not conform to the UUID format specification.
     * @throws NullPointerException thrown if the {@code credentialId} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DataSourceCredentialEntity> getDataSourceCredential(String credentialId) {
        return getDataSourceCredentialWithResponse(credentialId).flatMap(FluxUtil::toMono);
    }

    /**
     *  Get a data source credential entity by its id with REST response.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.getDataSourceCredentialWithResponse#String -->
     * <pre>
     * final String datasourceCredentialId = &quot;f45668b2-bffa-11eb-8529-0246ac130003&quot;;
     *
     * metricsAdvisorAdminAsyncClient.getDataSourceCredentialWithResponse&#40;datasourceCredentialId&#41;
     *     .subscribe&#40;credentialEntityWithResponse -&gt; &#123;
     *         System.out.printf&#40;&quot;Credential Entity creation operation status: %s%n&quot;,
     *             credentialEntityWithResponse.getStatusCode&#40;&#41;&#41;;
     *         if &#40;credentialEntityWithResponse.getValue&#40;&#41; instanceof DataSourceServicePrincipalInKeyVault&#41; &#123;
     *             DataSourceServicePrincipalInKeyVault actualCredentialSPInKV
     *                 = &#40;DataSourceServicePrincipalInKeyVault&#41; credentialEntityWithResponse.getValue&#40;&#41;;
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
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.getDataSourceCredentialWithResponse#String -->
     *
     * @param credentialId The data source credential entity unique id.
     *
     * @return The data source credential entity for the provided id.
     * @throws IllegalArgumentException If {@code credentialId} does not conform to the UUID format specification.
     * @throws NullPointerException thrown if the {@code credentialId} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DataSourceCredentialEntity>> getDataSourceCredentialWithResponse(
        String credentialId) {
        try {
            return withContext(context -> getDataSourceCredentialWithResponse(credentialId, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<DataSourceCredentialEntity>> getDataSourceCredentialWithResponse(String credentialId,
                                                                                   Context context) {
        Objects.requireNonNull(credentialId, "'credentialId' cannot be null.");

        return service.getCredentialWithResponseAsync(UUID.fromString(credentialId), context)
            .map(response -> new SimpleResponse<>(response,
                DataSourceCredentialEntityTransforms.fromInner(response.getValue())));
    }

    /**
     * Deletes the data source credential entity identified by {@code credentialId}.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.deleteDataSourceCredential#String -->
     * <pre>
     * final String datasourceCredentialId = &quot;t00853f1-9080-447f-bacf-8dccf2e86f&quot;;
     * metricsAdvisorAdminAsyncClient.deleteDataFeed&#40;datasourceCredentialId&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.deleteDataSourceCredential#String -->
     *
     * @param credentialId The data source credential entity id.
     *
     * @return An empty Mono.
     * @throws IllegalArgumentException If {@code credentialId} does not conform to the
     * UUID format specification.
     * @throws NullPointerException thrown if the {@code credentialId} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteDataSourceCredential(String credentialId) {
        return deleteDataSourceCredentialWithResponse(credentialId).flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes the data source credential entity identified by {@code credentialId}.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.deleteDataSourceCredentialWithResponse#String -->
     * <pre>
     * final String datasourceCredentialId = &quot;eh0854f1-8927-447f-bacf-8dccf2e86fwe&quot;;
     * metricsAdvisorAdminAsyncClient.deleteDataSourceCredentialWithResponse&#40;datasourceCredentialId&#41;
     *     .subscribe&#40;response -&gt;
     *         System.out.printf&#40;&quot;Datasource credential delete operation status : %s%n&quot;, response.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.deleteDataSourceCredentialWithResponse#String -->
     *
     * @param credentialId The data source credential entity id.
     *
     * @return A response containing status code and headers returned after the operation.
     * @throws IllegalArgumentException If {@code credentialId} does not conform to the
     * UUID format specification.
     * @throws NullPointerException thrown if the {@code credentialId} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteDataSourceCredentialWithResponse(String credentialId) {
        try {
            return withContext(context -> deleteDataSourceCredentialWithResponse(credentialId, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> deleteDataSourceCredentialWithResponse(String credentialId, Context context) {
        Objects.requireNonNull(credentialId, "'credentialId' is required.");
        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, METRICS_ADVISOR_TRACING_NAMESPACE_VALUE);

        return service.deleteCredentialWithResponseAsync(UUID.fromString(credentialId),
            withTracing)
            .doOnSubscribe(ignoredValue -> logger.info("Deleting deleteDataSourceCredentialEntity - {}",
                credentialId))
            .doOnSuccess(response -> logger.info("Deleted deleteDataSourceCredentialEntity - {}", response))
            .doOnError(error -> logger.warning("Failed to delete deleteDataSourceCredentialEntity - {}",
                credentialId, error));
    }

    /**
     * List information of all data source credential entities on the metrics advisor account.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.listDataSourceCredentials -->
     * <pre>
     * metricsAdvisorAdminAsyncClient.listDataSourceCredentials&#40;&#41;
     *     .subscribe&#40;datasourceCredentialEntity -&gt; &#123;
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
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.listDataSourceCredentials -->
     *
     * @return A {@link PagedFlux} containing information of all the {@link DataSourceCredentialEntity data feeds}
     * in the account.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<DataSourceCredentialEntity> listDataSourceCredentials() {
        return listDataSourceCredentials(new ListCredentialEntityOptions());
    }

    /**
     * List information of all data source credential entities on the metrics advisor account.
     *
     * <p><strong>Code sample</strong></p>
     * <!-- src_embed com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.listDataSourceCredentials#ListCredentialEntityOptions -->
     * <pre>
     * metricsAdvisorAdminAsyncClient.listDataSourceCredentials&#40;
     *     new ListCredentialEntityOptions&#40;&#41;
     *         .setMaxPageSize&#40;3&#41;&#41;
     *     .subscribe&#40;datasourceCredentialEntity -&gt; &#123;
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
     * <!-- end com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient.listDataSourceCredentials#ListCredentialEntityOptions -->
     *
     * @param listCredentialEntityOptions The configurable {@link ListCredentialEntityOptions options} to pass for filtering
     * the output result.
     *
     * @return A {@link PagedFlux} containing information of all the {@link DataSourceCredentialEntity data feeds}
     * in the account.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<DataSourceCredentialEntity> listDataSourceCredentials(ListCredentialEntityOptions listCredentialEntityOptions) {
        try {
            return new PagedFlux<>(() ->
                withContext(context ->
                    listCredentialEntitiesSinglePageAsync(listCredentialEntityOptions, context)),
                continuationToken ->
                    withContext(context -> listCredentialEntitiesSNextPageAsync(continuationToken, context)));
        } catch (RuntimeException ex) {
            return new PagedFlux<>(() -> monoError(logger, ex));
        }
    }

    PagedFlux<DataSourceCredentialEntity> listDataSourceCredentials(ListCredentialEntityOptions options,
                                                                    Context context) {
        return new PagedFlux<>(() ->
            listCredentialEntitiesSinglePageAsync(options, context),
            continuationToken ->
                listCredentialEntitiesSNextPageAsync(continuationToken, context));
    }

    private Mono<PagedResponse<DataSourceCredentialEntity>> listCredentialEntitiesSinglePageAsync(
        ListCredentialEntityOptions options, Context context) {
        options = options != null ? options : new ListCredentialEntityOptions();
        final Context withTracing = context.addData(AZ_TRACING_NAMESPACE_KEY, METRICS_ADVISOR_TRACING_NAMESPACE_VALUE);
        return service.listCredentialsSinglePageAsync(options.getSkip(), options.getMaxPageSize(), withTracing)
            .doOnRequest(ignoredValue -> logger.info("Listing information for all data source credentials"))
            .doOnSuccess(response -> logger.info("Listed data source credentials {}", response))
            .doOnError(error -> logger.warning("Failed to list all data source credential information - {}", error))
            .map(res -> new PagedResponseBase<>(
                res.getRequest(),
                res.getStatusCode(),
                res.getHeaders(),
                res.getValue().stream()
                    .map(DataSourceCredentialEntityTransforms::fromInner).collect(Collectors.toList()),
                res.getContinuationToken(),
                null));
    }

    private Mono<PagedResponse<DataSourceCredentialEntity>> listCredentialEntitiesSNextPageAsync(
        String nextPageLink, Context context) {
        if (CoreUtils.isNullOrEmpty(nextPageLink)) {
            return Mono.empty();
        }
        return service.listCredentialsNextSinglePageAsync(nextPageLink, context)
            .doOnSubscribe(ignoredValue -> logger.info("Retrieving the next listing page - Page {}", nextPageLink))
            .doOnSuccess(response -> logger.info("Retrieved the next listing page - Page {}", nextPageLink))
            .doOnError(error -> logger.warning("Failed to retrieve the next listing page - Page {}", nextPageLink,
                error))
            .map(res -> new PagedResponseBase<>(
                res.getRequest(),
                res.getStatusCode(),
                res.getHeaders(),
                res.getValue().stream()
                    .map(DataSourceCredentialEntityTransforms::fromInner).collect(Collectors.toList()),
                res.getContinuationToken(),
                null));
    }
}
