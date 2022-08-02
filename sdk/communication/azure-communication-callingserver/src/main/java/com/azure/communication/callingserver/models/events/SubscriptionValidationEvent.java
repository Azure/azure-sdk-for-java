// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models.events;

import com.azure.core.annotation.Immutable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The SubscriptionValidationEvent model. */
@Immutable
public final class SubscriptionValidationEvent implements CallingServerBaseEvent {
    /*
     * validationCode
     */
    @JsonProperty(value = "validationCode")
    private final String validationCode;

    /*
     * validationUrl
     */
    @JsonProperty(value = "validationUrl")
    private final String validationUrl;

    /*
     * The event types.
     */
    @JsonIgnore
    private final AcsEventType type;

    @JsonCreator
    private SubscriptionValidationEvent() {
        this.validationCode = null;
        this.validationUrl = null;
        this.type = AcsEventType.SUBSCRIPTION_VALIDATION_EVENT;
    }

    /**
     * Get the validationCode property: validationCode.
     *
     * @return the validationCode value.
     */
    public String getValidationCode() {
        return this.validationCode;
    }

    /**
     * Get the validationUrl property: The validationUrl property.
     *
     * @return the validationUrl value.
     */
    public String getValidationUrl() {
        return this.validationUrl;
    }

    /**
     * Get the type property.
     *
     * @return the type value.
     */
    public AcsEventType getType() {
        return this.type;
    }
}
