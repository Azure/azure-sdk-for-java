// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;

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
     * @return the cosmos sync trigger response
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosTriggerResponse read() throws CosmosClientException {
        return container.getScripts().mapTriggerResponseAndBlock(trigger.read());
    }

    /**
     * Replace cosmos trigger.
     *
     * @param triggerSettings the trigger settings
     * @return the cosmos sync trigger response
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosTriggerResponse replace(CosmosTriggerProperties triggerSettings) throws CosmosClientException {
        return container.getScripts().mapTriggerResponseAndBlock(trigger.replace(triggerSettings));
    }

    /**
     * Delete cosmos trigger.
     *
     * @return the cosmos sync response
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosTriggerResponse delete() throws CosmosClientException {
        return container.getScripts().mapTriggerResponseAndBlock(trigger.delete());
    }

}
