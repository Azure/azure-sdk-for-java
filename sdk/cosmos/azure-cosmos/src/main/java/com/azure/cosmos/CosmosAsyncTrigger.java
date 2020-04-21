// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.core.util.Context;
import com.azure.cosmos.implementation.Paths;
import com.azure.cosmos.implementation.TracerProvider;
import com.azure.cosmos.implementation.Trigger;
import com.azure.cosmos.models.CosmosAsyncTriggerResponse;
import com.azure.cosmos.models.CosmosTriggerProperties;
import com.azure.cosmos.models.ModelBridgeInternal;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.azure.core.util.FluxUtil.withContext;

/**
 * The type Cosmos async trigger. This contains methods to operate on a cosmos trigger asynchronously
 */
public class CosmosAsyncTrigger {

    private final CosmosAsyncContainer container;
    private String id;

    CosmosAsyncTrigger(String id, CosmosAsyncContainer container) {
        this.id = id;
        this.container = container;
    }

    /**
     * Get the id of the {@link CosmosAsyncTrigger}
     *
     * @return the id of the {@link CosmosAsyncTrigger}
     */
    public String getId() {
        return id;
    }

    /**
     * Set the id of the {@link CosmosAsyncTrigger}
     *
     * @param id the id of the {@link CosmosAsyncTrigger}
     * @return the same {@link CosmosAsyncTrigger} that had the id set
     */
    CosmosAsyncTrigger setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Reads a cosmos trigger by the trigger link.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single resource response for the read trigger.
     * In case of failure the {@link Mono} will error.
     *
     * @return an {@link Mono} containing the single resource response for the read cosmos trigger or an error.
     */
    public Mono<CosmosAsyncTriggerResponse> read() {
        return withContext(context -> read(context)).subscriberContext(reactorContext -> {
            Optional<String> master = reactorContext.getOrEmpty(TracerProvider.MASTER_CALL);
            if (master.isPresent()) {
                reactorContext = reactorContext.put(TracerProvider.NESTED_CALL, true);
            }
            return reactorContext.put(TracerProvider.MASTER_CALL, true);
        });
    }

    /**
     * Replaces a cosmos trigger.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single resource response with the replaced trigger.
     * In case of failure the {@link Mono} will error.
     *
     * @param triggerSettings the cosmos trigger properties.
     * @return an {@link Mono} containing the single resource response with the replaced cosmos trigger or an error.
     */
    public Mono<CosmosAsyncTriggerResponse> replace(CosmosTriggerProperties triggerSettings) {
        return withContext(context -> replace(triggerSettings, context)).subscriberContext(reactorContext -> {
            Optional<String> master = reactorContext.getOrEmpty(TracerProvider.MASTER_CALL);
            if (master.isPresent()) {
                reactorContext = reactorContext.put(TracerProvider.NESTED_CALL, true);
            }
            return reactorContext.put(TracerProvider.MASTER_CALL, true);
        });
    }

    /**
     * Deletes a cosmos trigger.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single resource response for the deleted trigger.
     * In case of failure the {@link Mono} will error.
     *
     * @return an {@link Mono} containing the single resource response for the deleted cosmos trigger or an error.
     */
    public Mono<CosmosAsyncTriggerResponse> delete() {
        return withContext(context -> delete(context)).subscriberContext(reactorContext -> {
            Optional<String> master = reactorContext.getOrEmpty(TracerProvider.MASTER_CALL);
            if (master.isPresent()) {
                reactorContext = reactorContext.put(TracerProvider.NESTED_CALL, true);
            }
            return reactorContext.put(TracerProvider.MASTER_CALL, true);
        });
    }

    String getURIPathSegment() {
        return Paths.TRIGGERS_PATH_SEGMENT;
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

    private Mono<CosmosAsyncTriggerResponse> read(Context context) {
        String spanName = "readTrigger." + container.getId();
        Map<String, String> tracingAttributes = new HashMap<String, String>() {{
            put(TracerProvider.DB_TYPE, TracerProvider.DB_TYPE_VALUE);
            put(TracerProvider.DB_INSTANCE, container.getDatabase().getId());
            put(TracerProvider.DB_URL, container.getDatabase().getClient().getServiceEndpoint());
            put(TracerProvider.DB_STATEMENT, spanName);
        }};

        Mono<CosmosAsyncTriggerResponse> responseMono = container.getDatabase()
            .getDocClientWrapper()
            .readTrigger(getLink(), null)
            .map(response -> ModelBridgeInternal.createCosmosAsyncTriggerResponse(response, container))
            .single();
        return this.container.getDatabase().getClient().getTracerProvider().traceEnabledCosmosResponsePublisher(responseMono, tracingAttributes, context, spanName);
    }

    private Mono<CosmosAsyncTriggerResponse> replace(CosmosTriggerProperties triggerSettings, Context context) {
        String spanName = "replaceTrigger." + container.getId();
        Map<String, String> tracingAttributes = new HashMap<String, String>() {{
            put(TracerProvider.DB_TYPE, TracerProvider.DB_TYPE_VALUE);
            put(TracerProvider.DB_INSTANCE, container.getDatabase().getId());
            put(TracerProvider.DB_URL, container.getDatabase().getClient().getServiceEndpoint());
            put(TracerProvider.DB_STATEMENT, spanName);
        }};

        Mono<CosmosAsyncTriggerResponse> responseMono = container.getDatabase()
            .getDocClientWrapper()
            .replaceTrigger(new Trigger(ModelBridgeInternal.toJsonFromJsonSerializable(triggerSettings)), null)
            .map(response -> ModelBridgeInternal.createCosmosAsyncTriggerResponse(response, container))
            .single();
        return this.container.getDatabase().getClient().getTracerProvider().traceEnabledCosmosResponsePublisher(responseMono, tracingAttributes, context, spanName);
    }

    private Mono<CosmosAsyncTriggerResponse> delete(Context context) {
        String spanName = "deleteTrigger." + container.getId();
        Map<String, String> tracingAttributes = new HashMap<String, String>() {{
            put(TracerProvider.DB_TYPE, TracerProvider.DB_TYPE_VALUE);
            put(TracerProvider.DB_INSTANCE, container.getDatabase().getId());
            put(TracerProvider.DB_URL, container.getDatabase().getClient().getServiceEndpoint());
            put(TracerProvider.DB_STATEMENT, spanName);
        }};

        Mono<CosmosAsyncTriggerResponse> responseMono = container.getDatabase()
            .getDocClientWrapper()
            .deleteTrigger(getLink(), null)
            .map(response -> ModelBridgeInternal.createCosmosAsyncTriggerResponse(response, container))
            .single();
        return this.container.getDatabase().getClient().getTracerProvider().traceEnabledCosmosResponsePublisher(responseMono, tracingAttributes, context, spanName);
    }
}
