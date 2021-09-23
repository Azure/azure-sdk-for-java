// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.util;

import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.IterableStream;
import com.azure.core.util.paging.ContinuablePagedFlux;
import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.ClientSideRequestStatistics;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.CosmosPagedFluxOptions;
import com.azure.cosmos.implementation.FeedResponseDiagnostics;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.QueryMetrics;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.SerializationDiagnosticsContext;
import com.azure.cosmos.implementation.TracerProvider;
import com.azure.cosmos.implementation.clienttelemetry.ClientTelemetry;
import com.azure.cosmos.implementation.clienttelemetry.ReportPayload;
import com.azure.cosmos.implementation.query.QueryInfo;
import com.azure.cosmos.models.FeedResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.HdrHistogram.ConcurrentDoubleHistogram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Signal;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Cosmos implementation of {@link ContinuablePagedFlux}.
 * <p>
 * This type is a Flux that provides the ability to operate on pages of type {@link FeedResponse} and individual items
 * in such pages. This type supports {@link String} type continuation tokens, allowing for restarting from a
 * previously-retrieved continuation token.
 * <p>
 * For more information on the base type, refer {@link ContinuablePagedFlux}
 *
 * @param <T> The type of elements in a {@link com.azure.core.util.paging.ContinuablePage}
 * @see com.azure.core.util.paging.ContinuablePage
 * @see CosmosPagedFluxOptions
 * @see FeedResponse
 */
public final class CosmosPagedFlux<T> extends ContinuablePagedFlux<String, T, FeedResponse<T>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CosmosPagedFlux.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private final Function<CosmosPagedFluxOptions, Flux<FeedResponse<T>>> optionsFluxFunction;

    private final Consumer<FeedResponse<T>> feedResponseConsumer;
    private ImplementationBridgeHelpers.CosmosDiagnosticsHelper.CosmosDiagnosticsAccessor cosmosDiagnosticsAccessor;
    private final int defaultPageSize;

    CosmosPagedFlux(Function<CosmosPagedFluxOptions, Flux<FeedResponse<T>>> optionsFluxFunction) {
        this(optionsFluxFunction, null, -1);
    }

    CosmosPagedFlux(Function<CosmosPagedFluxOptions, Flux<FeedResponse<T>>> optionsFluxFunction,
                    Consumer<FeedResponse<T>> feedResponseConsumer) {
        this(optionsFluxFunction, feedResponseConsumer, -1);
    }

    CosmosPagedFlux(Function<CosmosPagedFluxOptions, Flux<FeedResponse<T>>> optionsFluxFunction,
                    Consumer<FeedResponse<T>> feedResponseConsumer,
                    int defaultPageSize) {
        this.optionsFluxFunction = optionsFluxFunction;
        this.feedResponseConsumer = feedResponseConsumer;
        this.cosmosDiagnosticsAccessor = ImplementationBridgeHelpers.CosmosDiagnosticsHelper.getCosmosDiagnosticsAccessor();
        this.defaultPageSize = defaultPageSize;
    }

    /**
     * Handle for invoking "side-effects" on each FeedResponse returned by CosmosPagedFlux
     *
     * @param newFeedResponseConsumer handler
     * @return CosmosPagedFlux instance with attached handler
     */
    public CosmosPagedFlux<T> handle(Consumer<FeedResponse<T>> newFeedResponseConsumer) {
        if (this.feedResponseConsumer != null) {
            return new CosmosPagedFlux<T>(
                this.optionsFluxFunction,
                this.feedResponseConsumer.andThen(newFeedResponseConsumer));
        } else {
            return new CosmosPagedFlux<T>(this.optionsFluxFunction, newFeedResponseConsumer);
        }
    }

    @Override
    public Flux<FeedResponse<T>> byPage() {
        CosmosPagedFluxOptions cosmosPagedFluxOptions = this.createCosmosPagedFluxOptions();
        return FluxUtil.fluxContext(context -> byPage(cosmosPagedFluxOptions, context));
    }

    @Override
    public Flux<FeedResponse<T>> byPage(String continuationToken) {
        CosmosPagedFluxOptions cosmosPagedFluxOptions = this.createCosmosPagedFluxOptions();
        cosmosPagedFluxOptions.setRequestContinuation(continuationToken);
        return FluxUtil.fluxContext(context -> byPage(cosmosPagedFluxOptions, context));
    }

    @Override
    public Flux<FeedResponse<T>> byPage(int preferredPageSize) {
        CosmosPagedFluxOptions cosmosPagedFluxOptions = this.createCosmosPagedFluxOptions();
        cosmosPagedFluxOptions.setMaxItemCount(preferredPageSize);
        return FluxUtil.fluxContext(context -> byPage(cosmosPagedFluxOptions, context));
    }

    @Override
    public Flux<FeedResponse<T>> byPage(String continuationToken, int preferredPageSize) {
        CosmosPagedFluxOptions cosmosPagedFluxOptions = this.createCosmosPagedFluxOptions();
        cosmosPagedFluxOptions.setRequestContinuation(continuationToken);
        cosmosPagedFluxOptions.setMaxItemCount(preferredPageSize);
        return FluxUtil.fluxContext(context -> byPage(cosmosPagedFluxOptions, context));
    }

    /**
     * Subscribe to consume all items of type {@code T} in the sequence respectively. This is recommended for most
     * common scenarios. This will seamlessly fetch next page when required and provide with a {@link Flux} of items.
     *
     * @param coreSubscriber The subscriber for this {@link CosmosPagedFlux}
     */
    @Override
    public void subscribe(CoreSubscriber<? super T> coreSubscriber) {
        Flux<FeedResponse<T>> pagedResponse = this.byPage();
        pagedResponse.flatMap(tFeedResponse -> {
            IterableStream<T> elements = tFeedResponse.getElements();
            if (elements == null) {
                return Flux.empty();
            }
            return Flux.fromIterable(elements);
        }).subscribe(coreSubscriber);
    }

    CosmosPagedFlux<T> withDefaultPageSize(int pageSize) {
        return new CosmosPagedFlux<T>(this.optionsFluxFunction, this.feedResponseConsumer, pageSize);
    }

    private CosmosPagedFluxOptions createCosmosPagedFluxOptions() {
        CosmosPagedFluxOptions cosmosPagedFluxOptions = new CosmosPagedFluxOptions();

        if (this.defaultPageSize > 0) {
            cosmosPagedFluxOptions.setMaxItemCount(this.defaultPageSize);
        }

        return cosmosPagedFluxOptions;
    }

    private Flux<FeedResponse<T>> byPage(CosmosPagedFluxOptions pagedFluxOptions, Context context) {
        final AtomicReference<Context> parentContext = new AtomicReference<>(Context.NONE);
        AtomicReference<Instant> startTime = new AtomicReference<>();
        return this.optionsFluxFunction.apply(pagedFluxOptions).doOnSubscribe(ignoredValue -> {
            if (pagedFluxOptions.getTracerProvider() != null && pagedFluxOptions.getTracerProvider().isEnabled()) {
                parentContext.set(pagedFluxOptions.getTracerProvider().startSpan(pagedFluxOptions.getTracerSpanName(),
                    pagedFluxOptions.getDatabaseId(), pagedFluxOptions.getServiceEndpoint(),
                    context));
            }
            startTime.set(Instant.now());
        }).doOnComplete(() -> {
            if (pagedFluxOptions.getTracerProvider() != null && pagedFluxOptions.getTracerProvider().isEnabled()) {
                pagedFluxOptions.getTracerProvider().endSpan(parentContext.get(), Signal.complete(),
                    HttpConstants.StatusCodes.OK);
            }
        }).doOnError(throwable -> {
            if (pagedFluxOptions.getCosmosAsyncClient() != null &&
                Configs.isClientTelemetryEnabled(BridgeInternal.isClientTelemetryEnabled(pagedFluxOptions.getCosmosAsyncClient())) &&
                throwable instanceof CosmosException) {
                CosmosException cosmosException = (CosmosException) throwable;
                // not adding diagnostics on trace event for exception as this information is already there as
                // part of exception message
                if (this.cosmosDiagnosticsAccessor.isDiagnosticsCapturedInPagedFlux(cosmosException.getDiagnostics()).compareAndSet(false, true)) {
                    fillClientTelemetry(pagedFluxOptions.getCosmosAsyncClient(), 0, pagedFluxOptions.getContainerId(),
                        pagedFluxOptions.getDatabaseId(),
                        pagedFluxOptions.getOperationType(), pagedFluxOptions.getResourceType(),
                        BridgeInternal.getContextClient(pagedFluxOptions.getCosmosAsyncClient()).getConsistencyLevel(),
                        (float) cosmosException.getRequestCharge(), Duration.between(startTime.get(), Instant.now()));
                }
            }

            if (isTracerEnabled(pagedFluxOptions)) {
                pagedFluxOptions.getTracerProvider().endSpan(parentContext.get(), Signal.error(throwable),
                    TracerProvider.ERROR_CODE);
            }
            startTime.set(Instant.now());
        }).doOnNext(feedResponse -> {
            if (isTracerEnabled(pagedFluxOptions) &&
                this.cosmosDiagnosticsAccessor.isDiagnosticsCapturedInPagedFlux(feedResponse.getCosmosDiagnostics()).compareAndSet(false, true)) {
                try {
                    Duration threshold = pagedFluxOptions.getThresholdForDiagnosticsOnTracer();
                    if (threshold == null) {
                        threshold = pagedFluxOptions.getTracerProvider().QUERY_THRESHOLD_FOR_DIAGNOSTICS;
                    }

                    if (Duration.between(startTime.get(), Instant.now()).compareTo(threshold) > 0) {
                        addDiagnosticsOnTracerEvent(pagedFluxOptions.getTracerProvider(),
                            feedResponse.getCosmosDiagnostics(), parentContext.get());
                    }
                } catch (JsonProcessingException ex) {
                    LOGGER.warn("Error while serializing diagnostics for tracer", ex.getMessage());
                }
            }
            //  If the user has passed feedResponseConsumer, then call it with each feedResponse
            if (feedResponseConsumer != null) {
                feedResponseConsumer.accept(feedResponse);
            }

            if (pagedFluxOptions.getCosmosAsyncClient() != null &&
                Configs.isClientTelemetryEnabled(BridgeInternal.isClientTelemetryEnabled(pagedFluxOptions.getCosmosAsyncClient()))) {
                if (this.cosmosDiagnosticsAccessor.isDiagnosticsCapturedInPagedFlux(feedResponse.getCosmosDiagnostics()).compareAndSet(false, true)) {
                    fillClientTelemetry(pagedFluxOptions.getCosmosAsyncClient(), HttpConstants.StatusCodes.OK,
                        pagedFluxOptions.getContainerId(),
                        pagedFluxOptions.getDatabaseId(),
                        pagedFluxOptions.getOperationType(), pagedFluxOptions.getResourceType(),
                        BridgeInternal.getContextClient(pagedFluxOptions.getCosmosAsyncClient()).getConsistencyLevel(),
                        (float) feedResponse.getRequestCharge(), Duration.between(startTime.get(), Instant.now()));
                    startTime.set(Instant.now());
                };
            }
        });
    }

    private void fillClientTelemetry(CosmosAsyncClient cosmosAsyncClient,
                                    int statusCode,
                                    String containerId,
                                    String databaseId,
                                    OperationType operationType,
                                    ResourceType resourceType,
                                    ConsistencyLevel consistencyLevel,
                                    float requestCharge,
                                    Duration latency) {
        ClientTelemetry telemetry = BridgeInternal.getContextClient(cosmosAsyncClient).getClientTelemetry();
        ReportPayload reportPayloadLatency = createReportPayload(cosmosAsyncClient,
            statusCode, containerId, databaseId
            , operationType, resourceType, consistencyLevel, ClientTelemetry.REQUEST_LATENCY_NAME,
            ClientTelemetry.REQUEST_LATENCY_UNIT);
        ConcurrentDoubleHistogram latencyHistogram = telemetry.getClientTelemetryInfo().getOperationInfoMap().get(reportPayloadLatency);
        if (latencyHistogram != null) {
            ClientTelemetry.recordValue(latencyHistogram, latency.toNanos() / 1000);
        } else {
            if (statusCode == HttpConstants.StatusCodes.OK) {
                latencyHistogram = new ConcurrentDoubleHistogram(ClientTelemetry.REQUEST_LATENCY_MAX_MICRO_SEC,
                    ClientTelemetry.REQUEST_LATENCY_SUCCESS_PRECISION);
            } else {
                latencyHistogram = new ConcurrentDoubleHistogram(ClientTelemetry.REQUEST_LATENCY_MAX_MICRO_SEC,
                    ClientTelemetry.REQUEST_LATENCY_FAILURE_PRECISION);
            }

            latencyHistogram.setAutoResize(true);
            ClientTelemetry.recordValue(latencyHistogram, latency.toNanos() / 1000);
            telemetry.getClientTelemetryInfo().getOperationInfoMap().put(reportPayloadLatency, latencyHistogram);
        }

        ReportPayload reportPayloadRequestCharge = createReportPayload(cosmosAsyncClient,
            statusCode, containerId, databaseId
            , operationType, resourceType, consistencyLevel, ClientTelemetry.REQUEST_CHARGE_NAME,
            ClientTelemetry.REQUEST_CHARGE_UNIT);
        ConcurrentDoubleHistogram requestChargeHistogram = telemetry.getClientTelemetryInfo().getOperationInfoMap().get(reportPayloadRequestCharge);
        if (requestChargeHistogram != null) {
            ClientTelemetry.recordValue(requestChargeHistogram, requestCharge);
        } else {
            requestChargeHistogram = new ConcurrentDoubleHistogram(ClientTelemetry.REQUEST_CHARGE_MAX, ClientTelemetry.REQUEST_CHARGE_PRECISION);
            requestChargeHistogram.setAutoResize(true);
            ClientTelemetry.recordValue(requestChargeHistogram, requestCharge);
            telemetry.getClientTelemetryInfo().getOperationInfoMap().put(reportPayloadRequestCharge,
                requestChargeHistogram);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // the following helper/accessor only helps to access this class outside of this package.//
    ///////////////////////////////////////////////////////////////////////////////////////////

    static {
        ImplementationBridgeHelpers.CosmosPageFluxHelper.setCosmosPageFluxAccessor(
            new ImplementationBridgeHelpers.CosmosPageFluxHelper.CosmosPageFluxAccessor() {

                @Override
                public <T> CosmosPagedFlux<T> getCosmosPagedFlux(Function<CosmosPagedFluxOptions, Flux<FeedResponse<T>>> optionsFluxFunction) {
                    return new CosmosPagedFlux<>(optionsFluxFunction);
                }
            });
    }

    private ReportPayload createReportPayload(CosmosAsyncClient cosmosAsyncClient,
                                              int statusCode,
                                              String containerId,
                                              String databaseId,
                                              OperationType operationType,
                                              ResourceType resourceType,
                                              ConsistencyLevel consistencyLevel,
                                              String metricsName,
                                              String unitName) {
        ReportPayload reportPayload = new ReportPayload(metricsName, unitName);
        reportPayload.setConsistency(consistencyLevel == null ?
            BridgeInternal.getContextClient(cosmosAsyncClient).getConsistencyLevel() :
            consistencyLevel);

        reportPayload.setDatabaseName(databaseId);
        reportPayload.setContainerName(containerId);
        reportPayload.setOperation(operationType);
        reportPayload.setResource(resourceType);
        reportPayload.setStatusCode(statusCode);
        return reportPayload;
    }

    private void addDiagnosticsOnTracerEvent(TracerProvider tracerProvider, CosmosDiagnostics cosmosDiagnostics, Context parentContext) throws JsonProcessingException {
        if (cosmosDiagnostics == null) {
            return;
        }

        Map<String, Object> attributes = new HashMap<>();
        QueryInfo.QueryPlanDiagnosticsContext queryPlanDiagnosticsContext =
            cosmosDiagnosticsAccessor.getFeedResponseDiagnostics(cosmosDiagnostics) != null ?
                cosmosDiagnosticsAccessor.getFeedResponseDiagnostics(cosmosDiagnostics).getQueryPlanDiagnosticsContext() : null;
        if (queryPlanDiagnosticsContext != null) {
            attributes.put("JSON",
                mapper.writeValueAsString(queryPlanDiagnosticsContext));
            tracerProvider.addEvent("Query Plan Statistics", attributes,
                OffsetDateTime.ofInstant(queryPlanDiagnosticsContext.getStartTimeUTC(), ZoneOffset.UTC), parentContext);
        }

        FeedResponseDiagnostics feedResponseDiagnostics = cosmosDiagnosticsAccessor.getFeedResponseDiagnostics(cosmosDiagnostics);
        if (feedResponseDiagnostics != null && feedResponseDiagnostics.getQueryMetricsMap() != null && feedResponseDiagnostics.getQueryMetricsMap().size() > 0) {
            for(Map.Entry<String, QueryMetrics> entry : feedResponseDiagnostics.getQueryMetricsMap().entrySet()) {
                attributes = new HashMap<>();
                attributes.put("Query Metrics", entry.getValue().toString());
                tracerProvider.addEvent("Query Metrics for PKRange " + entry.getKey(), attributes,
                    OffsetDateTime.now(), parentContext);
            }
        }

        int queryDiagnosticsCounter = 1;
        for (ClientSideRequestStatistics clientSideRequestStatistics :
            BridgeInternal.getClientSideRequestStatisticsList(cosmosDiagnostics)) {
            attributes = new HashMap<>();
            //adding Supplemental StoreResponse
            int counter = 1;
            for (ClientSideRequestStatistics.StoreResponseStatistics statistics :
                clientSideRequestStatistics.getResponseStatisticsList()) {
                attributes.put("StoreResponse" + counter++,
                    mapper.writeValueAsString(statistics));
            }

            //adding Supplemental StoreResponse
            counter = 1;
            for (ClientSideRequestStatistics.StoreResponseStatistics statistics :
                ClientSideRequestStatistics.getCappedSupplementalResponseStatisticsList(clientSideRequestStatistics.getSupplementalResponseStatisticsList())) {
                attributes.put("Supplemental StoreResponse" + counter++,
                    mapper.writeValueAsString(statistics));
            }

            //adding retry context
            if (clientSideRequestStatistics.getRetryContext().getRetryStartTime() != null) {
                attributes.put("Retry Context",
                    mapper.writeValueAsString(clientSideRequestStatistics.getRetryContext()));
            }

            //adding addressResolutionStatistics
            counter = 1;
            for (ClientSideRequestStatistics.AddressResolutionStatistics addressResolutionStatistics :
                clientSideRequestStatistics.getAddressResolutionStatistics().values()) {
                attributes.put("AddressResolutionStatistics" + counter++,
                    mapper.writeValueAsString(addressResolutionStatistics));
            }

            //adding serializationDiagnosticsContext
            if (clientSideRequestStatistics.getSerializationDiagnosticsContext().serializationDiagnosticsList != null) {
                counter = 1;
                for (SerializationDiagnosticsContext.SerializationDiagnostics serializationDiagnostics :
                    clientSideRequestStatistics.getSerializationDiagnosticsContext().serializationDiagnosticsList) {
                    attributes = new HashMap<>();
                    attributes.put("SerializationDiagnostics" + counter++,
                        mapper.writeValueAsString(serializationDiagnostics));
                }
            }

            //adding gatewayStatistics
            if(clientSideRequestStatistics.getGatewayStatistics()  != null) {
                attributes.put("GatewayStatistics",
                    mapper.writeValueAsString(clientSideRequestStatistics.getGatewayStatistics()));
            }

            //adding systemInformation
            attributes.put("RegionContacted",
                mapper.writeValueAsString(clientSideRequestStatistics.getRegionsContacted()));


            //adding systemInformation
            attributes.put("SystemInformation",
                mapper.writeValueAsString(ClientSideRequestStatistics.fetchSystemInformation()));

            //adding clientCfgs
            attributes.put("ClientCfgs",
                mapper.writeValueAsString(clientSideRequestStatistics.getDiagnosticsClientContext()));

            if (clientSideRequestStatistics.getResponseStatisticsList() != null && clientSideRequestStatistics.getResponseStatisticsList().size() > 0
                && clientSideRequestStatistics.getResponseStatisticsList().get(0).getStoreResult() != null) {
                String eventName =
                    "Diagnostics for PKRange " + clientSideRequestStatistics.getResponseStatisticsList().get(0).getStoreResult().partitionKeyRangeId;
                tracerProvider.addEvent(eventName, attributes,
                    OffsetDateTime.ofInstant(clientSideRequestStatistics.getRequestStartTimeUTC(), ZoneOffset.UTC), parentContext);
            } else if (clientSideRequestStatistics.getGatewayStatistics() != null) {
                String eventName =
                    "Diagnostics for PKRange " + clientSideRequestStatistics.getGatewayStatistics().getPartitionKeyRangeId();
                tracerProvider.addEvent(eventName, attributes,
                    OffsetDateTime.ofInstant(clientSideRequestStatistics.getRequestStartTimeUTC(), ZoneOffset.UTC), parentContext);

            } else {
                String eventName = "Diagnostics " + queryDiagnosticsCounter++;
                tracerProvider.addEvent(eventName, attributes,
                    OffsetDateTime.ofInstant(clientSideRequestStatistics.getRequestStartTimeUTC(), ZoneOffset.UTC), parentContext);
            }
        }
    }

    private boolean isTracerEnabled(CosmosPagedFluxOptions pagedFluxOptions) {
        return pagedFluxOptions.getTracerProvider() != null && pagedFluxOptions.getTracerProvider().isEnabled();
    }
}
