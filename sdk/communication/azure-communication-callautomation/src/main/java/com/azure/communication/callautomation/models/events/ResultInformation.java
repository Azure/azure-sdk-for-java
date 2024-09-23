// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import com.azure.core.annotation.Immutable;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The ResultInformation model. */
@Immutable
public final class ResultInformation {
    /*
     * The code property.
     */
    @JsonProperty(value = "code")
    private final Integer code;

    /*
     * The subCode property.
     */
    @JsonProperty(value = "subCode")
    private final Integer subCode;

    /*
     * The message property.
     */
    @JsonProperty(value = "message")
    private final String message;

    private ResultInformation() {
        code = null;
        subCode = null;
        message = null;
    }

    /**
     * Get the code property: The code property.
     *
     * @return the code value.
     */
    public Integer getCode() {
        return this.code;
    }

    /**
     * Get the subCode property: The subCode property.
     *
     * @return the subCode value.
     */
    public Integer getSubCode() {
        return this.subCode;
    }

    /**
     * Get the message property: The message property.
     *
     * @return the message value.
     */
    public String getMessage() {
        return this.message;
    }
}
