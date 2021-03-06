// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.datamigration.fluent.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.datamigration.models.NameCheckFailureReason;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Indicates whether a proposed resource name is available. */
@Immutable
public final class NameAvailabilityResponseInner {
    @JsonIgnore private final ClientLogger logger = new ClientLogger(NameAvailabilityResponseInner.class);

    /*
     * If true, the name is valid and available. If false, 'reason' describes
     * why not.
     */
    @JsonProperty(value = "nameAvailable", access = JsonProperty.Access.WRITE_ONLY)
    private Boolean nameAvailable;

    /*
     * The reason why the name is not available, if nameAvailable is false
     */
    @JsonProperty(value = "reason", access = JsonProperty.Access.WRITE_ONLY)
    private NameCheckFailureReason reason;

    /*
     * The localized reason why the name is not available, if nameAvailable is
     * false
     */
    @JsonProperty(value = "message", access = JsonProperty.Access.WRITE_ONLY)
    private String message;

    /**
     * Get the nameAvailable property: If true, the name is valid and available. If false, 'reason' describes why not.
     *
     * @return the nameAvailable value.
     */
    public Boolean nameAvailable() {
        return this.nameAvailable;
    }

    /**
     * Get the reason property: The reason why the name is not available, if nameAvailable is false.
     *
     * @return the reason value.
     */
    public NameCheckFailureReason reason() {
        return this.reason;
    }

    /**
     * Get the message property: The localized reason why the name is not available, if nameAvailable is false.
     *
     * @return the message value.
     */
    public String message() {
        return this.message;
    }

    /**
     * Validates the instance.
     *
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
    }
}
