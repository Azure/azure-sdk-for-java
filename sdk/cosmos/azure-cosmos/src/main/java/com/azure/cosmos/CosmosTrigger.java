// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.models.CosmosTriggerResponse;
import com.azure.cosmos.models.CosmosTriggerProperties;

/**
 * The type Cosmos sync trigger.
 */
public class CosmosTrigger {
    private final String id;
    private final CosmosContainer container;
    private final CosmosAsyncTrigger trigger;

    /**
     * Instantiates a new Cosmos sync trigger.
     *
     * @param id the id
     * @param container the container
     * @param trigger the trigger
     */
    CosmosTrigger(String id, CosmosContainer container, CosmosAsyncTrigger trigger) {
        this.id = id;
        this.container = container;
        this.trigger = trigger;
    }

    /**
     * Gets getId.
     *
     * @return the string
     */
    public String getId() {
        return id;
    }

    /**
     * Read cosmos trigger.
     *
     * @return the cosmos trigger response
     */
    public CosmosTriggerResponse read() {
        return container.getScripts().blockTriggerResponse(trigger.read());
    }

    /**
     * Replace cosmos trigger.
     *
     * @param triggerSettings the trigger settings
     * @return the cosmos trigger response
     */
    public CosmosTriggerResponse replace(CosmosTriggerProperties triggerSettings) {
        return container.getScripts().blockTriggerResponse(trigger.replace(triggerSettings));
    }

    /**
     * Delete cosmos trigger.
     *
     * @return the cosmos response
     */
    public CosmosTriggerResponse delete() {
        return container.getScripts().blockTriggerResponse(trigger.delete());
    }

}
