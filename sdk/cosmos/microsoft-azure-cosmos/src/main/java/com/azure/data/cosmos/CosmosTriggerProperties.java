// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.Constants;
import com.azure.data.cosmos.internal.ResourceResponse;
import com.azure.data.cosmos.internal.Trigger;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

public class CosmosTriggerProperties extends Resource {

    /**
     * Constructor
     */
    public CosmosTriggerProperties(){
        super();
    }

    /**
     * Constructor.
     *
     * @param jsonString the json string that represents the trigger properties.
     */
    CosmosTriggerProperties(String jsonString){
        super(jsonString);
    }

    CosmosTriggerProperties(ResourceResponse<Trigger> response) {
        super(response.getResource().toJson());
    }

    /**
     * Sets the id
     *
     * @param id the name of the resource.
     * @return the current cosmos trigger properties instance
     */
    public CosmosTriggerProperties id(String id) {
        super.id(id);
        return this;
    }

    /**
     * Get the body of the trigger.
     *
     * @return the body of the trigger.
     */
    public String body() {
        return super.getString(Constants.Properties.BODY);
    }

    /**
     * Set the body of the trigger.
     *
     * @param body the body of the trigger.
     * @return the CosmosTriggerProperties.
     */
    public CosmosTriggerProperties body(String body) {
        super.set(Constants.Properties.BODY, body);
        return this;
    }

    /**
     * Get the type of the trigger.
     *
     * @return the trigger type.
     */
    public TriggerType triggerType() {
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
     * @return the CosmosTriggerProperties.
     */
    public CosmosTriggerProperties triggerType(TriggerType triggerType) {
        super.set(Constants.Properties.TRIGGER_TYPE, triggerType.toString());
        return this;
    }

    /**
     * Get the operation type of the trigger.
     *
     * @return the trigger operation.
     */
    public TriggerOperation triggerOperation() {
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
     * @return the CosmosTriggerProperties.
     */
    public CosmosTriggerProperties triggerOperation(TriggerOperation triggerOperation) {
        super.set(Constants.Properties.TRIGGER_OPERATION, triggerOperation.toString());
        return this;
    }

    static List<CosmosTriggerProperties> getFromV2Results(List<Trigger> results) {
        return results.stream().map(trigger -> new CosmosTriggerProperties(trigger.toJson())).collect(Collectors.toList());
    }
}
