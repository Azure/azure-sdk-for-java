// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.models;

import com.azure.core.annotation.Immutable;

import java.util.List;

/**
 * Error details of a failed log query.
 */
@Immutable
public final class LogsQueryErrorDetail {
    /*
     * The error's code.
     */
    private final String code;

    /*
     * A human readable error message.
     */
    private final String message;

    /*
     * Indicates which property in the request is responsible for the error.
     */
    private final String target;

    /*
     * Indicates which value in 'target' is responsible for the error.
     */
    private final String value;

    /*
     * Indicates resources which were responsible for the error.
     */
    private final List<String> resources;

    /*
     * Additional properties that can be provided on the error details object
     */
    private final Object additionalProperties;

    /**
     * Creates an instance of ErrorDetail class.
     * @param code the code value to set.
     * @param message the message value to set.
     * @param target indicates which property in the request is responsible for the error.
     * @param value indicates which value in 'target' is responsible for the error.
     * @param resources indicates resources which were responsible for the error.
     * @param additionalProperties additional properties that can be provided on the error details object
     */
    public LogsQueryErrorDetail(
            String code,
            String message,
            String target,
            String value,
            List<String> resources,
            Object additionalProperties) {
        this.code = code;
        this.message = message;
        this.target = target;
        this.value = value;
        this.resources = resources;
        this.additionalProperties = additionalProperties;
    }

    /**
     * Get the code property: The error's code.
     * @return the code value.
     */
    public String getCode() {
        return this.code;
    }

    /**
     * Get the message property: A human readable error message.
     * @return the message value.
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Get the target property: Indicates which property in the request is responsible for the error.
     * @return the target value.
     */
    public String getTarget() {
        return this.target;
    }

    /**
     * Get the value property: Indicates which value in 'target' is responsible for the error.
     * @return the value value.
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Get the resources property: Indicates resources which were responsible for the error.
     * @return the resources value.
     */
    public List<String> getResources() {
        return this.resources;
    }

    /**
     * Get the additionalProperties property: Additional properties that can be provided on the error details object.
     * @return the additionalProperties value.
     */
    public Object getAdditionalProperties() {
        return this.additionalProperties;
    }
}
