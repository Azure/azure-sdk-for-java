// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.tracing.Tracer;
import com.azure.cosmos.CosmosClientException;
import com.azure.cosmos.models.CosmosAsyncItemResponse;
import com.azure.cosmos.models.CosmosResponse;
import com.azure.cosmos.models.ResourceWrapper;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;

public class TracerProvider {
    private final List<Tracer> tracers = new ArrayList<>();
    private final boolean isEnabled;
    public final static String DB_TYPE_VALUE = "Cosmos";
    public final static String DB_TYPE = "db.type";
    public final static String DB_INSTANCE = "db.instance";
    public final static String DB_URL = "db.url";
    public static final String DB_STATEMENT = "db.statement";
    public static final String ERROR_MSG = "error.msg";
    public static final String ERROR_TYPE = "error.type";
    public static final String ERROR_STACK = "error.stack";
    public static final String COSMOS_CALL_DEPTH = "cosmosCallDepth";
    public static final String RESOURCE_PROVIDER_NAME = "Microsoft.DocumentDB";


    public final static Function<reactor.util.context.Context, reactor.util.context.Context> CALL_DEPTH_ATTRIBUTE_FUNC = (
        reactor.util.context.Context reactorContext) -> {
        CallDepth callDepth = reactorContext.getOrDefault(COSMOS_CALL_DEPTH, CallDepth.ZERO);
        assert callDepth != null;
        if (callDepth.equals(CallDepth.ZERO)) {
            return reactorContext.put(COSMOS_CALL_DEPTH, CallDepth.ONE);
        } else if (callDepth.equals(CallDepth.ONE)) {
            return reactorContext.put(COSMOS_CALL_DEPTH, CallDepth.TWO_OR_MORE);
        } else {
            return reactorContext;
        }
    };

    public TracerProvider(Iterable<Tracer> tracers) {
        Objects.requireNonNull(tracers, "'tracers' cannot be null.");
        tracers.forEach(this.tracers::add);
        isEnabled = this.tracers.size() > 0;
    }

    public boolean isEnabled() {
        return isEnabled;
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
        for (Tracer tracer : tracers) {
            local = tracer.start(methodName, local); // start the span and return the started span
            if (databaseId != null) {
                tracer.setAttribute(TracerProvider.DB_INSTANCE, databaseId, local);
            }

            tracer.setAttribute(AZ_TRACING_NAMESPACE_KEY, RESOURCE_PROVIDER_NAME, local);
            tracer.setAttribute(TracerProvider.DB_TYPE, DB_TYPE_VALUE, local);
            tracer.setAttribute(TracerProvider.DB_URL, endpoint, local);
            tracer.setAttribute(TracerProvider.DB_STATEMENT, methodName, local);
        }

        return local;
    }

    /**
     * Given a context containing the current tracing span the span is marked completed with status info from
     * {@link Signal}.  For each tracer plugged into the SDK the current tracing span is marked as completed.
     *
     * @param context Additional metadata that is passed through the call stack.
     * @param signal  The signal indicates the status and contains the metadata we need to end the tracing span.
     */
    public <T extends CosmosResponse<? extends Resource>> void endSpan(Context context, Signal<T> signal, int statusCode) {
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

                    if (throwable instanceof CosmosClientException) {
                        CosmosClientException exception = (CosmosClientException) throwable;
                        // confirm ?
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

    public <T extends CosmosResponse<? extends ResourceWrapper>> Mono<T> traceEnabledCosmosResponsePublisher(Mono<T> resultPublisher,
                                                                                                             Context context,
                                                                                                             String spanName,
                                                                                                             String databaseId,
                                                                                                             String endpoint) {
        return traceEnabledPublisher(resultPublisher,  context, spanName,databaseId, endpoint,
            (T response) -> response.getStatusCode());
    }

    public <T> Mono<T> traceEnabledNonCosmosResponsePublisher(Mono<T> resultPublisher,
                                                              Context context,
                                                              String spanName,
                                                              String databaseId,
                                                              String endpoint) {
        return traceEnabledPublisher(resultPublisher,  context, spanName, databaseId, endpoint, (T response) -> HttpConstants.StatusCodes.OK);
    }

    public <T> Mono<CosmosAsyncItemResponse<T>> traceEnabledCosmosItemResponsePublisher(Mono<CosmosAsyncItemResponse<T>> resultPublisher,
                                                                                        Context context,
                                                                                        String spanName,
                                                                                        String databaseId,
                                                                                        String endpoint) {
        return traceEnabledPublisher(resultPublisher, context, spanName,databaseId, endpoint,
            CosmosAsyncItemResponse::getStatusCode);
    }

    public <T> Mono<T> traceEnabledPublisher(Mono<T> resultPublisher,
                                             Context context,
                                             String spanName,
                                             String databaseId,
                                             String endpoint,
                                             Function<T, Integer> statusCodeFunc) {
        final AtomicReference<Context> parentContext = new AtomicReference<>(Context.NONE);
        CallDepth callDepth = FluxUtil.toReactorContext(context).getOrDefault(COSMOS_CALL_DEPTH,
            CallDepth.ZERO);
        assert callDepth != null;
        final boolean isNestedCall = callDepth.equals(CallDepth.TWO_OR_MORE);
        return resultPublisher
            .doOnSubscribe(ignoredValue -> {
                if (!isNestedCall) {
                    parentContext.set(this.startSpan(spanName, databaseId, endpoint,
                        context));
                }
            }).doOnSuccess(response -> {
                if (!isNestedCall) {
                    this.endSpan(parentContext.get(), Signal.complete(), statusCodeFunc.apply(response));
                }
            }).doOnError(throwable -> {
                if (!isNestedCall) {
                    this.endSpan(parentContext.get(), Signal.error(throwable), 0);
                }
            });
    }

    private void end(int statusCode, Throwable throwable, Context context) {
        for (Tracer tracer : tracers) {
            if (throwable != null) {
                tracer.setAttribute(TracerProvider.ERROR_MSG, throwable.getMessage(), context);
                tracer.setAttribute(TracerProvider.ERROR_TYPE, throwable.getClass().getName(), context);
                StringWriter errorStack = new StringWriter();
                throwable.printStackTrace(new PrintWriter(errorStack));
                tracer.setAttribute(TracerProvider.ERROR_STACK, errorStack.toString(), context);
            }
            tracer.end(statusCode, throwable, context);
        }
    }

    public enum CallDepth {
        ZERO,
        ONE,
        TWO_OR_MORE,
    }
}
