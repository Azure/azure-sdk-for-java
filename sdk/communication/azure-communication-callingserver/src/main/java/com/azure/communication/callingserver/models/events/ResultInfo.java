// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models.events;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The ResultInfo model. */
@Fluent
public final class ResultInfo {
    /*
     * The code property.
     */
    @JsonProperty(value = "code")
    private Integer code;

    /*
     * The subCode property.
     */
    @JsonProperty(value = "subCode")
    private Integer subCode;

    /*
     * The message property.
     */
    @JsonProperty(value = "message")
    private String message;

    /**
     * Get the code property: The code property.
     *
     * @return the code value.
     */
    public Integer getCode() {
        return this.code;
    }

    /**
     * Set the code property: The code property.
     *
     * @param code the code value to set.
     * @return the ResultInfo object itself.
     */
    public ResultInfo setCode(Integer code) {
        this.code = code;
        return this;
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
     * Set the subCode property: The subCode property.
     *
     * @param subCode the subCode value to set.
     * @return the ResultInfo object itself.
     */
    public ResultInfo setSubCode(Integer subCode) {
        this.subCode = subCode;
        return this;
    }

    /**
     * Get the message property: The message property.
     *
     * @return the message value.
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Set the message property: The message property.
     *
     * @param message the message value to set.
     * @return the ResultInfo object itself.
     */
    public ResultInfo setMessage(String message) {
        this.message = message;
        return this;
    }
}
