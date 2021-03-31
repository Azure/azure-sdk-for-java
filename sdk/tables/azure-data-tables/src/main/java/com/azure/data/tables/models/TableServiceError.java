package com.azure.data.tables.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TableServiceError {
    /*
     * The service error code.
     */
    private String code;

    /*
     * Language code of the error message.
     */
    @JsonProperty(value = "lang")
    private String lang;

    /*
     * The error message.
     */
    @JsonProperty(value = "value")
    private String message;

    /**
     * Create an instance of {@link TableServiceError}.
     *
     * @param code The service error code.
     * @param lang Language code of the error message.
     * @param message The error message.
     */
    public TableServiceError(String code, String lang, String message) {
        this.code = code;
        this.lang = lang;
        this.message = message;
    }

    /**
     * Get the service error code.
     *
     * @return The service error code.
     */
    public String getCode() {
        return this.code;
    }

    /**
     * Get the language code of the error message.
     *
     * @return The language code of the error message.
     */
    public String getLang() {
        return this.lang;
    }

    /**
     * Get the error message.
     *
     * @return The error message.
     */
    public String getMessage() {
        return this.message;
    }
}
