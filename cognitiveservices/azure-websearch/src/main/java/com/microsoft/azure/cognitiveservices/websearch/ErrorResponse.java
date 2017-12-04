/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.websearch;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * The top-level response that represents a failed request.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type")
@JsonTypeName("ErrorResponse")
public class ErrorResponse extends Response {
    /**
     * A list of errors that describe the reasons why the request failed.
     */
    @JsonProperty(value = "errors", required = true)
    private List<Error> errors;

    /**
     * Get the errors value.
     *
     * @return the errors value
     */
    public List<Error> errors() {
        return this.errors;
    }

    /**
     * Set the errors value.
     *
     * @param errors the errors value to set
     * @return the ErrorResponse object itself.
     */
    public ErrorResponse withErrors(List<Error> errors) {
        this.errors = errors;
        return this;
    }

}
