// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.CosmosItemSerializer;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.models.TriggerOperation;
import com.azure.cosmos.models.TriggerType;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Represents a trigger in the Azure Cosmos DB database service.
 * <p>
 * Cosmos DB supports pre and post triggers defined in JavaScript to be executed on creates, updates and deletes. For
 * additional details, refer to the server-side JavaScript API documentation.
 */
public class Trigger extends Resource {

    /**
     * Constructor.
     */
    public Trigger() {
        super();
    }

    /**
     * Constructor.
     *
     * @param jsonNode the json node that represents the trigger.
     */
    public Trigger(ObjectNode jsonNode) {
        super(jsonNode);
    }

    /**
     * Get the body of the trigger.
     *
     * @return the body of the trigger.
     */
    public String getBody() {
        return super.getString(Constants.Properties.BODY);
    }

    /**
     * Set the body of the trigger.
     *
     * @param body the body of the trigger.
     */
    public void setBody(String body) {
        this.set(Constants.Properties.BODY, body, CosmosItemSerializer.DEFAULT_SERIALIZER);
    }

    /**
     * Get the type of the trigger.
     *
     * @return the trigger type.
     */
    public TriggerType getTriggerType() {
        TriggerType result = TriggerType.PRE;
        try {
            result = TriggerType.valueOf(
                    StringUtils.upperCase(super.getString(Constants.Properties.TRIGGER_TYPE)));
        } catch (IllegalArgumentException e) {
            // ignore the exception and return the default
            this.getLogger().warn("INVALID triggerType value {}.", super.getString(Constants.Properties.TRIGGER_TYPE));
        }
        return result;
    }

    /**
     * Set the type of the resource.
     *
     * @param triggerType the trigger type.
     */
    public void setTriggerType(TriggerType triggerType) {
        this.set(Constants.Properties.TRIGGER_TYPE, triggerType.toString(), CosmosItemSerializer.DEFAULT_SERIALIZER);
    }

    /**
     * Get the operation type of the trigger.
     *
     * @return the trigger operation.
     */
    public TriggerOperation getTriggerOperation() {
        TriggerOperation result = TriggerOperation.CREATE;
        try {
            result = TriggerOperation.valueOf(
                    StringUtils.upperCase(super.getString(Constants.Properties.TRIGGER_OPERATION)));
        } catch (IllegalArgumentException e) {
            // ignore the exception and return the default
            this.getLogger().warn("INVALID triggerOperation value {}.", super.getString(Constants.Properties.TRIGGER_OPERATION));
        }
        return result;
    }

    /**
     * Set the operation type of the trigger.
     *
     * @param triggerOperation the trigger operation.
     */
    public void setTriggerOperation(TriggerOperation triggerOperation) {
        this.set(Constants.Properties.TRIGGER_OPERATION, triggerOperation.toString(), CosmosItemSerializer.DEFAULT_SERIALIZER);
    }
}
