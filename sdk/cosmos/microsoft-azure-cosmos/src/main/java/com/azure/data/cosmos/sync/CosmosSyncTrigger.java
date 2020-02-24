// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.sync;

import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.CosmosTrigger;
import com.azure.data.cosmos.CosmosTriggerProperties;

/**
 * The type Cosmos sync trigger.
 */
public class CosmosSyncTrigger {
    private final String id;
    private final CosmosSyncContainer container;
    private final CosmosTrigger trigger;

    /**
     * Instantiates a new Cosmos sync trigger.
     *
     * @param id the id
     * @param container the container
     * @param trigger the trigger
     */
    CosmosSyncTrigger(String id, CosmosSyncContainer container, CosmosTrigger trigger) {
        this.id = id;
        this.container = container;
        this.trigger = trigger;
    }

    /**
     * Gets id.
     *
     * @return the string
     */
    public String id() {
        return id;
    }

    /**
     * Read cosmos trigger.
     *
     * @return the cosmos sync trigger response
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosSyncTriggerResponse read() throws CosmosClientException {
        return container.getScripts().mapTriggerResponseAndBlock(trigger.read());
    }

    /**
     * Replace cosmos trigger.
     *
     * @param triggerSettings the trigger settings
     * @return the cosmos sync trigger response
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosSyncTriggerResponse replace(CosmosTriggerProperties triggerSettings) throws CosmosClientException {
        return container.getScripts().mapTriggerResponseAndBlock(trigger.replace(triggerSettings));
    }

    /**
     * Delete cosmos trigger.
     *
     * @return the cosmos sync response
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosSyncResponse delete() throws CosmosClientException {
        return container.getScripts().mapDeleteResponseAndBlock(trigger.delete());
    }

}
