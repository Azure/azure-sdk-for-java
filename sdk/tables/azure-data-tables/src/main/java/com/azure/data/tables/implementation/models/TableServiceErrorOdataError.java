// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables.implementation.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The TableServiceErrorOdataError model. */
@Fluent
public final class TableServiceErrorOdataError {
    /*
     * The service error code.
     */
    @JsonProperty(value = "code")
    private String code;

    /*
     * The service error message.
     */
    @JsonProperty(value = "message")
    private TableServiceErrorOdataErrorMessage message;

    /**
     * Get the code property: The service error code.
     *
     * @return the code value.
     */
    public String getCode() {
        return this.code;
    }

    /**
     * Set the code property: The service error code.
     *
     * @param code the code value to set.
     * @return the TableServiceErrorOdataError object itself.
     */
    public TableServiceErrorOdataError setCode(String code) {
        this.code = code;
        return this;
    }

    /**
     * Get the message property: The service error message.
     *
     * @return the message value.
     */
    public TableServiceErrorOdataErrorMessage getMessage() {
        return this.message;
    }

    /**
     * Set the message property: The service error message.
     *
     * @param message the message value to set.
     * @return the TableServiceErrorOdataError object itself.
     */
    public TableServiceErrorOdataError setMessage(TableServiceErrorOdataErrorMessage message) {
        this.message = message;
        return this;
    }
}

