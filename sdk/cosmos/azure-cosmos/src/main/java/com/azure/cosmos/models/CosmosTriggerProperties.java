// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.implementation.Resource;
import com.azure.cosmos.implementation.Trigger;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The type Cosmos trigger properties.
 */
public final class CosmosTriggerProperties {

    private Trigger trigger;
    /**
     * Constructor
     */
    CosmosTriggerProperties() {
        this.trigger = new Trigger();
    }

    /**
     * Constructor.
     *
     * @param id the id of the Cosmos trigger.
     * @param body the body of the Cosmos trigger.
     */
    public CosmosTriggerProperties(String id, String body) {
        this.trigger = new Trigger();
        trigger.setId(id);
        trigger.setBody(body);
    }

    /**
     * Constructor.
     *
     * @param jsonNode the json node that represents the trigger properties.
     */
    CosmosTriggerProperties(ObjectNode jsonNode) {
        this.trigger = new Trigger(jsonNode);
    }

    /**
     * Sets the id
     *
     * @param id the name of the resource.
     * @return the current cosmos trigger properties instance
     */
    public CosmosTriggerProperties setId(String id) {
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

    Resource getResource() {
        return this.trigger;
    }

    /**
     * Gets the name of the resource.
     *
     * @return the name of the resource.
     */
    public String getId() {
        return this.trigger.getId();
    }

    /**
     * Gets the ID associated with the resource.
     *
     * @return the ID associated with the resource.
     */
    String getResourceId() {
        return this.trigger.getResourceId();
    }

    /**
     * Get the last modified timestamp associated with the resource.
     * This is only relevant when getting response from the server.
     *
     * @return the timestamp.
     */
    public Instant getTimestamp() {
        return this.trigger.getTimestamp();
    }

    /**
     * Get the entity tag associated with the resource.
     * This is only relevant when getting response from the server.
     *
     * @return the e tag.
     */
    public String getETag() {
        return this.trigger.getETag();
    }

    static List<CosmosTriggerProperties> getFromV2Results(List<Trigger> results) {
        return results.stream().map(trigger -> new CosmosTriggerProperties(trigger.getPropertyBag()))
                   .collect(Collectors.toList());
    }
}
