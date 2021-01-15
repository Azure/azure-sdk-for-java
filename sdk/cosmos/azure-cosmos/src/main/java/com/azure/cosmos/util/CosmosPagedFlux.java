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
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.CosmosPagedFluxOptions;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.TracerProvider;
import com.azure.cosmos.implementation.clientTelemetry.ClientTelemetry;
import com.azure.cosmos.implementation.clientTelemetry.ReportPayload;
import com.azure.cosmos.models.FeedResponse;
import org.HdrHistogram.ConcurrentDoubleHistogram;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Signal;

import java.time.Duration;
import java.time.Instant;
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

    private final Function<CosmosPagedFluxOptions, Flux<FeedResponse<T>>> optionsFluxFunction;

    private final Consumer<FeedResponse<T>> feedResponseConsumer;

    CosmosPagedFlux(Function<CosmosPagedFluxOptions, Flux<FeedResponse<T>>> optionsFluxFunction) {
        this.optionsFluxFunction = optionsFluxFunction;
        this.feedResponseConsumer = null;
    }

    CosmosPagedFlux(Function<CosmosPagedFluxOptions, Flux<FeedResponse<T>>> optionsFluxFunction,
                    Consumer<FeedResponse<T>> feedResponseConsumer) {
        this.optionsFluxFunction = optionsFluxFunction;
        this.feedResponseConsumer = feedResponseConsumer;
    }

    /**
     * Handle for invoking "side-effects" on each FeedResponse returned by CosmosPagedFlux
     *
     * @param feedResponseConsumer handler
     * @return CosmosPagedFlux instance with attached handler
     */
    @Beta(value = Beta.SinceVersion.V4_6_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public CosmosPagedFlux<T> handle(Consumer<FeedResponse<T>> feedResponseConsumer) {
        return new CosmosPagedFlux<T>(this.optionsFluxFunction, feedResponseConsumer);
    }

    @Override
    public Flux<FeedResponse<T>> byPage() {
        CosmosPagedFluxOptions cosmosPagedFluxOptions = new CosmosPagedFluxOptions();
        return FluxUtil.fluxContext(context -> byPage(cosmosPagedFluxOptions, context));
    }

    @Override
    public Flux<FeedResponse<T>> byPage(String continuationToken) {
        CosmosPagedFluxOptions cosmosPagedFluxOptions = new CosmosPagedFluxOptions();
        cosmosPagedFluxOptions.setRequestContinuation(continuationToken);
        return FluxUtil.fluxContext(context -> byPage(cosmosPagedFluxOptions, context));
    }

    @Override
    public Flux<FeedResponse<T>> byPage(int preferredPageSize) {
        CosmosPagedFluxOptions cosmosPagedFluxOptions = new CosmosPagedFluxOptions();
        cosmosPagedFluxOptions.setMaxItemCount(preferredPageSize);
        return FluxUtil.fluxContext(context -> byPage(cosmosPagedFluxOptions, context));
    }

    @Override
    public Flux<FeedResponse<T>> byPage(String continuationToken, int preferredPageSize) {
        CosmosPagedFluxOptions cosmosPagedFluxOptions = new CosmosPagedFluxOptions();
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

    private Flux<FeedResponse<T>> byPage(CosmosPagedFluxOptions pagedFluxOptions, Context context) {
        final AtomicReference<Context> parentContext = new AtomicReference<>(Context.NONE);
        AtomicReference<Instant> startTime = new AtomicReference<>();
        return this.optionsFluxFunction.apply(pagedFluxOptions).doOnSubscribe(ignoredValue -> {
            if (pagedFluxOptions.getTracerProvider().isEnabled()) {
                parentContext.set(pagedFluxOptions.getTracerProvider().startSpan(pagedFluxOptions.getTracerSpanName(),
                    pagedFluxOptions.getDatabaseId(), pagedFluxOptions.getServiceEndpoint(),
                    context));
            }
            startTime.set(Instant.now());
        }).doOnComplete(() -> {
            if (pagedFluxOptions.getTracerProvider().isEnabled()) {
                pagedFluxOptions.getTracerProvider().endSpan(parentContext.get(), Signal.complete(),
                    HttpConstants.StatusCodes.OK);
            }
        }).doOnError(throwable -> {
            if (pagedFluxOptions.getTracerProvider().isEnabled()) {
                pagedFluxOptions.getTracerProvider().endSpan(parentContext.get(), Signal.error(throwable),
                    TracerProvider.ERROR_CODE);
            }

            if (pagedFluxOptions.getCosmosAsyncClient() != null &&
                Configs.isClientTelemetryEnabled(BridgeInternal.isClientTelemetryEnabled(pagedFluxOptions.getCosmosAsyncClient())) &&
                throwable instanceof CosmosException) {
                CosmosException cosmosException = (CosmosException) throwable;
                fillClientTelemetry(pagedFluxOptions.getCosmosAsyncClient(), 0, pagedFluxOptions.getContainerId(),
                    pagedFluxOptions.getDatabaseId(),
                    pagedFluxOptions.getOperationType(), pagedFluxOptions.getResourceType(),
                    BridgeInternal.getContextClient(pagedFluxOptions.getCosmosAsyncClient()).getConsistencyLevel(),
                    (float) cosmosException.getRequestCharge(), Duration.between(startTime.get(), Instant.now()));
            }
            startTime.set(Instant.now());
        }).doOnNext(feedResponse -> {
            //  If the user has passed feedResponseConsumer, then call it with each feedResponse
            if (feedResponseConsumer != null) {
                feedResponseConsumer.accept(feedResponse);
            }

            if (pagedFluxOptions.getCosmosAsyncClient() != null &&
                Configs.isClientTelemetryEnabled(BridgeInternal.isClientTelemetryEnabled(pagedFluxOptions.getCosmosAsyncClient()))) {
                fillClientTelemetry(pagedFluxOptions.getCosmosAsyncClient(), HttpConstants.StatusCodes.OK,
                    pagedFluxOptions.getContainerId(),
                    pagedFluxOptions.getDatabaseId(),
                    pagedFluxOptions.getOperationType(), pagedFluxOptions.getResourceType(),
                    BridgeInternal.getContextClient(pagedFluxOptions.getCosmosAsyncClient()).getConsistencyLevel(),
                    (float) feedResponse.getRequestCharge(), Duration.between(startTime.get(), Instant.now()));
                startTime.set(Instant.now());
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
}
