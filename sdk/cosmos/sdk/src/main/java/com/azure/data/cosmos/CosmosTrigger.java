// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.Paths;
import com.azure.data.cosmos.internal.Trigger;
import reactor.core.publisher.Mono;

public class CosmosTrigger {

    private CosmosContainer container;
    private String id;

    CosmosTrigger(String id, CosmosContainer container) {
        this.id = id;
        this.container = container;
    }

    /**
     * Get the id of the {@link CosmosTrigger}
     * @return the id of the {@link CosmosTrigger}
     */
    public String id() {
        return id;
    }

    /**
     * Set the id of the {@link CosmosTrigger}
     * @param id the id of the {@link CosmosTrigger}
     * @return the same {@link CosmosTrigger} that had the id set
     */
    CosmosTrigger id(String id) {
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
    public Mono<CosmosTriggerResponse> read() {
        return container.getDatabase()
                .getDocClientWrapper()
                .readTrigger(getLink(), null)
                .map(response -> new CosmosTriggerResponse(response, container))
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
    public Mono<CosmosTriggerResponse> replace(CosmosTriggerProperties triggerSettings) {
        return container.getDatabase()
                .getDocClientWrapper()
                .replaceTrigger(new Trigger(triggerSettings.toJson()), null)
                .map(response -> new CosmosTriggerResponse(response, container))
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
    public Mono<CosmosResponse> delete() {
        return container.getDatabase()
                .getDocClientWrapper()
                .deleteTrigger(getLink(), null)
                .map(response -> new CosmosResponse(response.getResource()))
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
        builder.append(id());
        return builder.toString();
    }

}
