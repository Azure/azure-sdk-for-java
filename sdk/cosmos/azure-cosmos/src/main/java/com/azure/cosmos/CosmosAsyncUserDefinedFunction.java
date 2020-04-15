// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.tracing.ProcessKind;
import com.azure.cosmos.implementation.Paths;
import com.azure.cosmos.implementation.TracerProvider;
import com.azure.cosmos.implementation.UserDefinedFunction;
import com.azure.cosmos.models.CosmosAsyncUserDefinedFunctionResponse;
import com.azure.cosmos.models.CosmosUserDefinedFunctionProperties;
import com.azure.cosmos.models.ModelBridgeInternal;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.core.util.FluxUtil.withContext;

/**
 * The type Cosmos async user defined function.
 */
public class CosmosAsyncUserDefinedFunction {

    @SuppressWarnings("EnforceFinalFields")
    private final CosmosAsyncContainer container;
    private String id;

    CosmosAsyncUserDefinedFunction(String id, CosmosAsyncContainer container) {
        this.id = id;
        this.container = container;
    }

    /**
     * Get the id of the {@link CosmosAsyncUserDefinedFunction}
     *
     * @return the id of the {@link CosmosAsyncUserDefinedFunction}
     */
    public String getId() {
        return id;
    }

    /**
     * Set the id of the {@link CosmosAsyncUserDefinedFunction}
     *
     * @param id the id of the {@link CosmosAsyncUserDefinedFunction}
     * @return the same {@link CosmosAsyncUserDefinedFunction} that had the id set
     */
    CosmosAsyncUserDefinedFunction setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Read a user defined function.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single resource response for the read user defined
     * function.
     * In case of failure the {@link Mono} will error.
     *
     * @return an {@link Mono} containing the single resource response for the read user defined function or an error.
     */
    public Mono<CosmosAsyncUserDefinedFunctionResponse> read() {
        return withContext(context -> read(context)).subscriberContext(reactorContext -> {
            Optional<String> master = reactorContext.getOrEmpty(TracerProvider.MASTER_CALL);
            if (master.isPresent()) {
                reactorContext = reactorContext.put(TracerProvider.NESTED_CALL, true);
            }
            return reactorContext.put(TracerProvider.MASTER_CALL, true);
        });
    }

    /**
     * Replaces a cosmos user defined function.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single resource response with the replaced user
     * defined function.
     * In case of failure the {@link Mono} will error.
     *
     * @param udfSettings the cosmos user defined function properties.
     * @return an {@link Mono} containing the single resource response with the replaced cosmos user defined function
     * or an error.
     */
    public Mono<CosmosAsyncUserDefinedFunctionResponse> replace(CosmosUserDefinedFunctionProperties udfSettings) {
        return withContext(context -> replace(udfSettings, context)).subscriberContext(reactorContext -> {
            Optional<String> master = reactorContext.getOrEmpty(TracerProvider.MASTER_CALL);
            if (master.isPresent()) {
                reactorContext = reactorContext.put(TracerProvider.NESTED_CALL, true);
            }
            return reactorContext.put(TracerProvider.MASTER_CALL, true);
        });
    }

    /**
     * Deletes a cosmos user defined function.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single resource response for the deleted user
     * defined function.
     * In case of failure the {@link Mono} will error.
     *
     * @return an {@link Mono} containing the single resource response for the deleted cosmos user defined function or
     * an error.
     */
    public Mono<CosmosAsyncUserDefinedFunctionResponse> delete() {
        return withContext(context -> delete(context)).subscriberContext(reactorContext -> {
            Optional<String> master = reactorContext.getOrEmpty(TracerProvider.MASTER_CALL);
            if (master.isPresent()) {
                reactorContext = reactorContext.put(TracerProvider.NESTED_CALL, true);
            }
            return reactorContext.put(TracerProvider.MASTER_CALL, true);
        });
    }

    String getURIPathSegment() {
        return Paths.USER_DEFINED_FUNCTIONS_PATH_SEGMENT;
    }

    String getParentLink() {
        return container.getLink();
    }

    String getLink() {
        StringBuilder builder = new StringBuilder();
        builder.append(getParentLink());
        builder.append("/");
        builder.append(getURIPathSegment());
        builder.append("/");
        builder.append(getId());
        return builder.toString();
    }

    private Mono<CosmosAsyncUserDefinedFunctionResponse> read(Context context) {
        final boolean isTracingEnabled = this.container.getDatabase().getClient().getTracerProvider().isEnabled();
        final AtomicReference<Context> parentContext = isTracingEnabled
            ? new AtomicReference<>(Context.NONE)
            : null;
        String spanName = "readUDF." + container.getId();
        Map<String, String> tracingAttributes = new HashMap<String, String>() {{
            put(TracerProvider.DB_TYPE, TracerProvider.DB_TYPE_VALUE);
            put(TracerProvider.DB_INSTANCE, container.getDatabase().getId());
            put(TracerProvider.DB_URL, container.getDatabase().getClient().getServiceEndpoint());
            put(TracerProvider.DB_STATEMENT, spanName);
        }};

        return container.getDatabase().getDocClientWrapper().readUserDefinedFunction(getLink(), null)
            .map(response -> ModelBridgeInternal.createCosmosAsyncUserDefinedFunctionResponse(response, container)).single()
            .doOnSubscribe(ignoredValue -> {
                if (isTracingEnabled) {
                    reactor.util.context.Context reactorContext = FluxUtil.toReactorContext(context);
                    Objects.requireNonNull(reactorContext.hasKey(TracerProvider.MASTER_CALL));
                    Optional<Object> callerFunc = reactorContext.getOrEmpty(TracerProvider.NESTED_CALL);
                    if (!callerFunc.isPresent()) {
                        parentContext.set(this.container.getDatabase().getClient().getTracerProvider().startSpan(spanName,
                            context.addData(TracerProvider.ATTRIBUTE_MAP, tracingAttributes), ProcessKind.DATABASE));
                    }
                }
            }).doOnSuccess(response -> {
                if (isTracingEnabled) {
                    this.container.getDatabase().getClient().getTracerProvider().endSpan(parentContext.get(),
                        Signal.complete(), response.getStatusCode());
                }
            }).doOnError(throwable -> {
                if (isTracingEnabled) {
                    this.container.getDatabase().getClient().getTracerProvider().endSpan(parentContext.get(),
                        Signal.error(throwable), 0);
                }
            });
    }

    private Mono<CosmosAsyncUserDefinedFunctionResponse> replace(CosmosUserDefinedFunctionProperties udfSettings,
                                                                 Context context) {
        final boolean isTracingEnabled = this.container.getDatabase().getClient().getTracerProvider().isEnabled();
        final AtomicReference<Context> parentContext = isTracingEnabled
            ? new AtomicReference<>(Context.NONE)
            : null;
        String spanName = "replaceUDF." + container.getId();
        Map<String, String> tracingAttributes = new HashMap<String, String>() {{
            put(TracerProvider.DB_TYPE, TracerProvider.DB_TYPE_VALUE);
            put(TracerProvider.DB_INSTANCE, container.getDatabase().getId());
            put(TracerProvider.DB_URL, container.getDatabase().getClient().getServiceEndpoint());
            put(TracerProvider.DB_STATEMENT, spanName);
        }};

        return container.getDatabase()
            .getDocClientWrapper()
            .replaceUserDefinedFunction(new UserDefinedFunction(ModelBridgeInternal.toJsonFromJsonSerializable(udfSettings)), null)
            .map(response -> ModelBridgeInternal.createCosmosAsyncUserDefinedFunctionResponse(response, container))
            .single()
            .doOnSubscribe(ignoredValue -> {
                if (isTracingEnabled) {
                    reactor.util.context.Context reactorContext = FluxUtil.toReactorContext(context);
                    Objects.requireNonNull(reactorContext.hasKey(TracerProvider.MASTER_CALL));
                    Optional<Object> callerFunc = reactorContext.getOrEmpty(TracerProvider.NESTED_CALL);
                    if (!callerFunc.isPresent()) {
                        parentContext.set(this.container.getDatabase().getClient().getTracerProvider().startSpan(spanName,
                            context.addData(TracerProvider.ATTRIBUTE_MAP, tracingAttributes), ProcessKind.DATABASE));
                    }
                }
            }).doOnSuccess(response -> {
                if (isTracingEnabled) {
                    this.container.getDatabase().getClient().getTracerProvider().endSpan(parentContext.get(),
                        Signal.complete(), response.getStatusCode());
                }
            }).doOnError(throwable -> {
                if (isTracingEnabled) {
                    this.container.getDatabase().getClient().getTracerProvider().endSpan(parentContext.get(),
                        Signal.error(throwable), 0);
                }
            });
    }

    private Mono<CosmosAsyncUserDefinedFunctionResponse> delete(Context context) {
        final boolean isTracingEnabled = this.container.getDatabase().getClient().getTracerProvider().isEnabled();
        final AtomicReference<Context> parentContext = isTracingEnabled
            ? new AtomicReference<>(Context.NONE)
            : null;
        String spanName = "deleteUDF." + container.getId();
        Map<String, String> tracingAttributes = new HashMap<String, String>() {{
            put(TracerProvider.DB_TYPE, TracerProvider.DB_TYPE_VALUE);
            put(TracerProvider.DB_INSTANCE, container.getDatabase().getId());
            put(TracerProvider.DB_URL, container.getDatabase().getClient().getServiceEndpoint());
            put(TracerProvider.DB_STATEMENT, spanName);
        }};

        return container.getDatabase()
            .getDocClientWrapper()
            .deleteUserDefinedFunction(this.getLink(), null)
            .map(response -> ModelBridgeInternal.createCosmosAsyncUserDefinedFunctionResponse(response, container))
            .single()
            .doOnSubscribe(ignoredValue -> {
                if (isTracingEnabled) {
                    reactor.util.context.Context reactorContext = FluxUtil.toReactorContext(context);
                    Objects.requireNonNull(reactorContext.hasKey(TracerProvider.MASTER_CALL));
                    Optional<Object> callerFunc = reactorContext.getOrEmpty(TracerProvider.NESTED_CALL);
                    if (!callerFunc.isPresent()) {
                        parentContext.set(this.container.getDatabase().getClient().getTracerProvider().startSpan(spanName,
                            context.addData(TracerProvider.ATTRIBUTE_MAP, tracingAttributes), ProcessKind.DATABASE));
                    }
                }
            }).doOnSuccess(response -> {
                if (isTracingEnabled) {
                    this.container.getDatabase().getClient().getTracerProvider().endSpan(parentContext.get(),
                        Signal.complete(), response.getStatusCode());
                }
            }).doOnError(throwable -> {
                if (isTracingEnabled) {
                    this.container.getDatabase().getClient().getTracerProvider().endSpan(parentContext.get(),
                        Signal.error(throwable), 0);
                }
            });
    }
}
