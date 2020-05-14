// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.implementation.Resource;
import com.azure.cosmos.implementation.Trigger;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The type Cosmos trigger properties.
 */
public final class CosmosTriggerProperties extends ResourceWrapper{

    private Trigger trigger;
    /**
     * Constructor
     */
    public CosmosTriggerProperties() {
        this.trigger = new Trigger();
    }

    /**
     * Constructor.
     *
     * @param jsonString the json string that represents the trigger properties.
     */
    CosmosTriggerProperties(String jsonString) {
        this.trigger = new Trigger(jsonString);
    }

    /**
     * Sets the id
     *
     * @param id the name of the resource.
     * @return the current cosmos trigger properties instance
     */
    public CosmosTriggerProperties setId(String id) {
        this.trigger = new Trigger();
        trigger.setId(id);

        return this;
    }

    /**
     * Get the body of the trigger.
     *
     * @return the body of the trigger.
     */
    public String getBody() {
        return this.trigger.getBody();
    }

    /**
     * Set the body of the trigger.
     *
     * @param body the body of the trigger.
     * @return the CosmosTriggerProperties.
     */
    public CosmosTriggerProperties setBody(String body) {
        this.trigger.setBody(body);
        return this;
    }

    /**
     * Get the type of the trigger.
     *
     * @return the trigger type.
     */
    public TriggerType getTriggerType() {
        return this.trigger.getTriggerType();
    }

    /**
     * Set the type of the resource.
     *
     * @param triggerType the trigger type.
     * @return the CosmosTriggerProperties.
     */
    public CosmosTriggerProperties setTriggerType(TriggerType triggerType) {
        this.trigger.setTriggerType(triggerType);
        return this;
    }

    /**
     * Get the operation type of the trigger.
     *
     * @return the trigger operation.
     */
    public TriggerOperation getTriggerOperation() {
        return this.trigger.getTriggerOperation();
    }

    /**
     * Set the operation type of the trigger.
     *
     * @param triggerOperation the trigger operation.
     * @return the CosmosTriggerProperties.
     */
    public CosmosTriggerProperties setTriggerOperation(TriggerOperation triggerOperation) {
        this.trigger.setTriggerOperation(triggerOperation);
        return this;
    }

    static List<CosmosTriggerProperties> getFromV2Results(List<Trigger> results) {
        return results.stream().map(trigger -> new CosmosTriggerProperties(trigger.toJson()))
                   .collect(Collectors.toList());
    }

    @Override
    Resource getResource() {
        return this.trigger;
    }
}
