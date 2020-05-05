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
        return TracerProvider.cosmosWithContext(withContext(context -> read(context)),
            container.getDatabase().getClient().getTracerProvider());
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
        return TracerProvider.cosmosWithContext(withContext(context -> replace(triggerSettings, context)),
            container.getDatabase().getClient().getTracerProvider());
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
        return TracerProvider.cosmosWithContext(withContext(context -> delete(context)),
            container.getDatabase().getClient().getTracerProvider());
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
        Mono<CosmosAsyncTriggerResponse> responseMono = container.getDatabase()
            .getDocClientWrapper()
            .readTrigger(getLink(), null)
            .map(response -> ModelBridgeInternal.createCosmosAsyncTriggerResponse(response, container))
            .single();
        return this.container.getDatabase().getClient().getTracerProvider().traceEnabledCosmosResponsePublisher(responseMono, context, spanName, container.getDatabase().getId(), container.getDatabase().getClient().getServiceEndpoint());
    }

    private Mono<CosmosAsyncTriggerResponse> replace(CosmosTriggerProperties triggerSettings, Context context) {
        String spanName = "replaceTrigger." + container.getId();
        Mono<CosmosAsyncTriggerResponse> responseMono = container.getDatabase()
            .getDocClientWrapper()
            .replaceTrigger(new Trigger(ModelBridgeInternal.toJsonFromJsonSerializable(triggerSettings)), null)
            .map(response -> ModelBridgeInternal.createCosmosAsyncTriggerResponse(response, container))
            .single();
        return this.container.getDatabase().getClient().getTracerProvider().traceEnabledCosmosResponsePublisher(responseMono, context, spanName, container.getDatabase().getId(), container.getDatabase().getClient().getServiceEndpoint());
    }

    private Mono<CosmosAsyncTriggerResponse> delete(Context context) {
        String spanName = "deleteTrigger." + container.getId();
        Mono<CosmosAsyncTriggerResponse> responseMono = container.getDatabase()
            .getDocClientWrapper()
            .deleteTrigger(getLink(), null)
            .map(response -> ModelBridgeInternal.createCosmosAsyncTriggerResponse(response, container))
            .single();
        return this.container.getDatabase().getClient().getTracerProvider().traceEnabledCosmosResponsePublisher(responseMono, context, spanName, container.getDatabase().getId(), container.getDatabase().getClient().getServiceEndpoint());
    }
}
