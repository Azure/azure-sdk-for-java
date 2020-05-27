package com.microsoft.azure.tables.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The TableServiceError model. */
@Fluent
public final class TableServiceError {
    /*
     * The error message.
     */
    @JsonProperty(value = "Message")
    private String message;

    /**
     * Get the message property: The error message.
     *
     * @return the message value.
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Set the message property: The error message.
     *
     * @param message the message value to set.
     * @return the TableServiceError object itself.
     */
    public TableServiceError setMessage(String message) {
        this.message = message;
        return this;
    }
}
