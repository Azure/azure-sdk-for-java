// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.databoxedge.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.databoxedge.fluent.models.PeriodicTimerProperties;
import com.azure.resourcemanager.databoxedge.fluent.models.TriggerInner;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/** Trigger details. */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "kind")
@JsonTypeName("PeriodicTimerEvent")
@Fluent
public final class PeriodicTimerEventTrigger extends TriggerInner {
    /*
     * Periodic timer trigger properties.
     */
    @JsonProperty(value = "properties", required = true)
    private PeriodicTimerProperties innerProperties = new PeriodicTimerProperties();

    /** Creates an instance of PeriodicTimerEventTrigger class. */
    public PeriodicTimerEventTrigger() {
    }

    /**
     * Get the innerProperties property: Periodic timer trigger properties.
     *
     * @return the innerProperties value.
     */
    private PeriodicTimerProperties innerProperties() {
        return this.innerProperties;
    }

    /**
     * Get the sourceInfo property: Periodic timer details.
     *
     * @return the sourceInfo value.
     */
    public PeriodicTimerSourceInfo sourceInfo() {
        return this.innerProperties() == null ? null : this.innerProperties().sourceInfo();
    }

    /**
     * Set the sourceInfo property: Periodic timer details.
     *
     * @param sourceInfo the sourceInfo value to set.
     * @return the PeriodicTimerEventTrigger object itself.
     */
    public PeriodicTimerEventTrigger withSourceInfo(PeriodicTimerSourceInfo sourceInfo) {
        if (this.innerProperties() == null) {
            this.innerProperties = new PeriodicTimerProperties();
        }
        this.innerProperties().withSourceInfo(sourceInfo);
        return this;
    }

    /**
     * Get the sinkInfo property: Role Sink information.
     *
     * @return the sinkInfo value.
     */
    public RoleSinkInfo sinkInfo() {
        return this.innerProperties() == null ? null : this.innerProperties().sinkInfo();
    }

    /**
     * Set the sinkInfo property: Role Sink information.
     *
     * @param sinkInfo the sinkInfo value to set.
     * @return the PeriodicTimerEventTrigger object itself.
     */
    public PeriodicTimerEventTrigger withSinkInfo(RoleSinkInfo sinkInfo) {
        if (this.innerProperties() == null) {
            this.innerProperties = new PeriodicTimerProperties();
        }
        this.innerProperties().withSinkInfo(sinkInfo);
        return this;
    }

    /**
     * Get the customContextTag property: A custom context tag typically used to correlate the trigger against its
     * usage. For example, if a periodic timer trigger is intended for certain specific IoT modules in the device, the
     * tag can be the name or the image URL of the module.
     *
     * @return the customContextTag value.
     */
    public String customContextTag() {
        return this.innerProperties() == null ? null : this.innerProperties().customContextTag();
    }

    /**
     * Set the customContextTag property: A custom context tag typically used to correlate the trigger against its
     * usage. For example, if a periodic timer trigger is intended for certain specific IoT modules in the device, the
     * tag can be the name or the image URL of the module.
     *
     * @param customContextTag the customContextTag value to set.
     * @return the PeriodicTimerEventTrigger object itself.
     */
    public PeriodicTimerEventTrigger withCustomContextTag(String customContextTag) {
        if (this.innerProperties() == null) {
            this.innerProperties = new PeriodicTimerProperties();
        }
        this.innerProperties().withCustomContextTag(customContextTag);
        return this;
    }

    /**
     * Validates the instance.
     *
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    @Override
    public void validate() {
        super.validate();
        if (innerProperties() == null) {
            throw LOGGER
                .logExceptionAsError(
                    new IllegalArgumentException(
                        "Missing required property innerProperties in model PeriodicTimerEventTrigger"));
        } else {
            innerProperties().validate();
        }
    }

    private static final ClientLogger LOGGER = new ClientLogger(PeriodicTimerEventTrigger.class);
}
