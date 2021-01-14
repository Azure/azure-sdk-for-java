// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.core.util.Context;
import com.azure.core.util.tracing.Tracer;
import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.TransactionalBatchResponse;
import com.azure.cosmos.implementation.clientTelemetry.ClientTelemetry;
import com.azure.cosmos.implementation.clientTelemetry.ReportPayload;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import org.HdrHistogram.ConcurrentDoubleHistogram;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;

public class TracerProvider {
    private Tracer tracer;
    public final static String DB_TYPE_VALUE = "Cosmos";
    public final static String DB_TYPE = "db.type";
    public final static String DB_INSTANCE = "db.instance";
    public final static String DB_URL = "db.url";
    public static final String DB_STATEMENT = "db.statement";
    public static final String ERROR_MSG = "error.msg";
    public static final String ERROR_TYPE = "error.type";
    public static final String COSMOS_CALL_DEPTH = "cosmosCallDepth";
    public static final String COSMOS_CALL_DEPTH_VAL = "nested";
    public static final int ERROR_CODE = 0;
    public static final String RESOURCE_PROVIDER_NAME = "Microsoft.DocumentDB";

    public TracerProvider(Tracer tracer) {
        this.tracer = tracer;
    }

    public boolean isEnabled() {
        return tracer != null;
    }

    /**
     * For each tracer plugged into the SDK a new tracing span is created.
     * <p>
     * The {@code context} will be checked for containing information about a parent span. If a parent span is found the
     * new span will be added as a child, otherwise the span will be created and added to the context and any downstream
     * start calls will use the created span as the parent.
     *
     * @param context Additional metadata that is passed through the call stack.
     * @return An updated context object.
     */
    public Context startSpan(String methodName, String databaseId, String endpoint, Context context) {
        Context local = Objects.requireNonNull(context, "'context' cannot be null.");
        local = local.addData(AZ_TRACING_NAMESPACE_KEY, RESOURCE_PROVIDER_NAME);
        local = tracer.start(methodName, local); // start the span and return the started span
        if (databaseId != null) {
            tracer.setAttribute(TracerProvider.DB_INSTANCE, databaseId, local);
        }

        tracer.setAttribute(TracerProvider.DB_TYPE, DB_TYPE_VALUE, local);
        tracer.setAttribute(TracerProvider.DB_URL, endpoint, local);
        tracer.setAttribute(TracerProvider.DB_STATEMENT, methodName, local);
        return local;
    }

    /**
     * Given a context containing the current tracing span the span is marked completed with status info from
     * {@link Signal}.  For each tracer plugged into the SDK the current tracing span is marked as completed.
     *
     * @param context Additional metadata that is passed through the call stack.
     * @param signal  The signal indicates the status and contains the metadata we need to end the tracing span.
     */
    public <T extends CosmosResponse<? extends Resource>> void endSpan(Context context,
                                                                       Signal<T> signal,
                                                                       int statusCode) {
        Objects.requireNonNull(context, "'context' cannot be null.");
        Objects.requireNonNull(signal, "'signal' cannot be null.");

        switch (signal.getType()) {
            case ON_COMPLETE:
                end(statusCode, null, context);
                break;
            case ON_ERROR:
                Throwable throwable = null;
                if (signal.hasError()) {
                    // The last status available is on error, this contains the thrown error.
                    throwable = signal.getThrowable();

                    if (throwable instanceof CosmosException) {
                        CosmosException exception = (CosmosException) throwable;
                        statusCode = exception.getStatusCode();
                    }
                }
                end(statusCode, throwable, context);
                break;
            default:
                // ON_SUBSCRIBE and ON_NEXT don't have the information to end the span so just return.
                break;
        }
    }

    public <T extends CosmosResponse<?>> Mono<T> traceEnabledCosmosResponsePublisher(Mono<T> resultPublisher,
                                                                                     Context context,
                                                                                     String spanName,
                                                                                     String databaseId,
                                                                                     String endpoint) {
        return traceEnabledPublisher(resultPublisher, context, spanName, databaseId, endpoint,
            (T response) -> response.getStatusCode());
    }

    public Mono<TransactionalBatchResponse> traceEnabledBatchResponsePublisher(Mono<TransactionalBatchResponse> resultPublisher,
                                                                               Context context,
                                                                               String spanName,
                                                                               String databaseId,
                                                                               String endpoint) {
        return traceEnabledPublisher(resultPublisher, context, spanName, databaseId, endpoint,
            TransactionalBatchResponse::getStatusCode);
    }

    public <T> Mono<CosmosItemResponse<T>> traceEnabledCosmosItemResponsePublisher(Mono<CosmosItemResponse<T>> resultPublisher,
                                                                                   Context context,
                                                                                   String spanName,
                                                                                   String containerId,
                                                                                   String databaseId,
                                                                                   CosmosAsyncClient client,
                                                                                   ConsistencyLevel consistencyLevel,
                                                                                   OperationType operationType,
                                                                                   ResourceType resourceType) {

        return publisherWithClientTelemetry(resultPublisher, context, spanName, containerId, databaseId,
            BridgeInternal.getServiceEndpoint(client),
            client,
            consistencyLevel,
            operationType,
            resourceType,
            CosmosItemResponse::getStatusCode);
    }

    private <T> Mono<T> traceEnabledPublisher(Mono<T> resultPublisher,
                                              Context context,
                                              String spanName,
                                              String databaseId,
                                              String endpoint,
                                              Function<T, Integer> statusCodeFunc) {
        final AtomicReference<Context> parentContext = new AtomicReference<>(Context.NONE);
        Optional<Object> callDepth = context.getData(COSMOS_CALL_DEPTH);
        final boolean isNestedCall = callDepth.isPresent();
        return resultPublisher
            .doOnSubscribe(ignoredValue -> {
                if (isEnabled() && !isNestedCall) {
                    parentContext.set(this.startSpan(spanName, databaseId, endpoint,
                        context));
                }
            }).doOnSuccess(response -> {
                if (isEnabled() && !isNestedCall) {
                    this.endSpan(parentContext.get(), Signal.complete(), statusCodeFunc.apply(response));
                }
            }).doOnError(throwable -> {
                if (isEnabled() && !isNestedCall) {
                    this.endSpan(parentContext.get(), Signal.error(throwable), ERROR_CODE);
                }
            });
    }

    private <T> Mono<T> publisherWithClientTelemetry(Mono<T> resultPublisher,
                                                     Context context,
                                                     String spanName,
                                                     String containerId,
                                                     String databaseId,
                                                     String endpoint,
                                                     CosmosAsyncClient client,
                                                     ConsistencyLevel consistencyLevel,
                                                     OperationType operationType,
                                                     ResourceType resourceType,
                                                     Function<T, Integer> statusCodeFunc) {
        Mono<T> tracerMono = traceEnabledPublisher(resultPublisher, context, spanName, databaseId, endpoint, statusCodeFunc);
        return tracerMono
            .doOnSuccess(response -> {
                if (Configs.isClientTelemetryEnabled(BridgeInternal.isClientTelemetryEnabled(client)) && response instanceof CosmosItemResponse) {
                    @SuppressWarnings("unchecked")
                    CosmosItemResponse<T> itemResponse = (CosmosItemResponse<T>) response;
                    fillClientTelemetry(client, itemResponse.getDiagnostics(), itemResponse.getStatusCode(),
                        ModelBridgeInternal.getPayloadLength(itemResponse), containerId,
                        databaseId, operationType, resourceType, consistencyLevel,
                        (float) itemResponse.getRequestCharge());
                }
            }).doOnError(throwable -> {
                if (Configs.isClientTelemetryEnabled(BridgeInternal.isClientTelemetryEnabled(client)) && throwable instanceof CosmosException) {
                    CosmosException cosmosException = (CosmosException) throwable;
                    fillClientTelemetry(client, cosmosException.getDiagnostics(), cosmosException.getStatusCode(),
                        null, containerId,
                        databaseId, operationType, resourceType, consistencyLevel,
                        (float) cosmosException.getRequestCharge());
                }
            });
    }

    private void end(int statusCode, Throwable throwable, Context context) {
        if (throwable != null) {
            tracer.setAttribute(TracerProvider.ERROR_MSG, throwable.getMessage(), context);
            tracer.setAttribute(TracerProvider.ERROR_TYPE, throwable.getClass().getName(), context);
        }
        tracer.end(statusCode, throwable, context);
    }

    private void fillClientTelemetry(CosmosAsyncClient cosmosAsyncClient,
                                    CosmosDiagnostics cosmosDiagnostics,
                                    int statusCode,
                                    Integer objectSize,
                                    String containerId,
                                    String databaseId,
                                    OperationType operationType,
                                    ResourceType resourceType,
                                    ConsistencyLevel consistencyLevel,
                                    float requestCharge) {
        ClientTelemetry telemetry = BridgeInternal.getContextClient(cosmosAsyncClient).getClientTelemetry();
        ReportPayload reportPayloadLatency = createReportPayload(cosmosAsyncClient, cosmosDiagnostics,
            statusCode, objectSize, containerId, databaseId
            , operationType, resourceType, consistencyLevel, ClientTelemetry.REQUEST_LATENCY_NAME,
            ClientTelemetry.REQUEST_LATENCY_UNIT);
        ConcurrentDoubleHistogram latencyHistogram = telemetry.getClientTelemetryInfo().getOperationInfoMap().get(reportPayloadLatency);
        if (latencyHistogram != null) {
            ClientTelemetry.recordValue(latencyHistogram, cosmosDiagnostics.getDuration().toNanos()/1000);
        } else {
            if (statusCode >= HttpConstants.StatusCodes.MINIMUM_SUCCESS_STATUSCODE && statusCode <= HttpConstants.StatusCodes.MAXIMUM_SUCCESS_STATUSCODE) {
                latencyHistogram = new ConcurrentDoubleHistogram(ClientTelemetry.REQUEST_LATENCY_MAX_MICRO_SEC, ClientTelemetry.REQUEST_LATENCY_SUCCESS_PRECISION);
            } else {
                latencyHistogram = new ConcurrentDoubleHistogram(ClientTelemetry.REQUEST_LATENCY_MAX_MICRO_SEC, ClientTelemetry.REQUEST_LATENCY_FAILURE_PRECISION);
            }

            latencyHistogram.setAutoResize(true);
            ClientTelemetry.recordValue(latencyHistogram, cosmosDiagnostics.getDuration().toNanos()/1000);
            telemetry.getClientTelemetryInfo().getOperationInfoMap().put(reportPayloadLatency, latencyHistogram);
        }

        ReportPayload reportPayloadRequestCharge = createReportPayload(cosmosAsyncClient, cosmosDiagnostics,
            statusCode, objectSize, containerId, databaseId
            , operationType, resourceType, consistencyLevel, ClientTelemetry.REQUEST_CHARGE_NAME, ClientTelemetry.REQUEST_CHARGE_UNIT);
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
                                              CosmosDiagnostics cosmosDiagnostics,
                                              int statusCode,
                                              Integer objectSize,
                                              String containerId,
                                              String databaseId,
                                              OperationType operationType,
                                              ResourceType resourceType,
                                              ConsistencyLevel consistencyLevel,
                                              String metricsName,
                                              String unitName) {
        ReportPayload reportPayload = new ReportPayload(metricsName, unitName);
        reportPayload.setRegionsContacted(BridgeInternal.getRegionsContacted(cosmosDiagnostics).toString());
        reportPayload.setConsistency(consistencyLevel == null ?
            BridgeInternal.getContextClient(cosmosAsyncClient).getConsistencyLevel() :
            consistencyLevel);
        if (objectSize != null) {
            reportPayload.setGreaterThan1Kb(objectSize > ClientTelemetry.ONE_KB_TO_BYTES);
        }

        reportPayload.setDatabaseName(databaseId);
        reportPayload.setContainerName(containerId);
        reportPayload.setOperation(operationType);
        reportPayload.setResource(resourceType);
        reportPayload.setStatusCode(statusCode);
        return reportPayload;
    }
}
