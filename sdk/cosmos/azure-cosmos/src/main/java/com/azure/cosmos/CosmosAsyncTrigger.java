// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.internal.Paths;
import com.azure.cosmos.internal.Trigger;
import com.azure.cosmos.internal.Paths;
import com.azure.cosmos.internal.Trigger;
import reactor.core.publisher.Mono;

public class CosmosAsyncTrigger {

    private CosmosAsyncContainer container;
    private String id;

    CosmosAsyncTrigger(String id, CosmosAsyncContainer container) {
        this.id = id;
        this.container = container;
    }

    /**
     * Get the id of the {@link CosmosAsyncTrigger}
     * @return the id of the {@link CosmosAsyncTrigger}
     */
    public String getId() {
        return id;
    }

    /**
     * Set the id of the {@link CosmosAsyncTrigger}
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
        return container.getDatabase()
                .getDocClientWrapper()
                .readTrigger(getLink(), null)
                .map(response -> new CosmosAsyncTriggerResponse(response, container))
                .single();
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
        return container.getDatabase()
                .getDocClientWrapper()
                .replaceTrigger(new Trigger(triggerSettings.toJson()), null)
                .map(response -> new CosmosAsyncTriggerResponse(response, container))
                .single();
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
        return container.getDatabase()
                .getDocClientWrapper()
                .deleteTrigger(getLink(), null)
                .map(response -> new CosmosAsyncTriggerResponse(response, container))
                .single();
    }

    String URIPathSegment() {
        return Paths.TRIGGERS_PATH_SEGMENT;
    }

    String parentLink() {
        return container.getLink();
    }

    String getLink() {
        StringBuilder builder = new StringBuilder();
        builder.append(parentLink());
        builder.append("/");
        builder.append(URIPathSegment());
        builder.append("/");
        builder.append(getId());
        return builder.toString();
    }

}
