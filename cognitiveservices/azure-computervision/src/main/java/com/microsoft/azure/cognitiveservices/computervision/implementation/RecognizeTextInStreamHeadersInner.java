/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.computervision.implementation;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Defines headers for RecognizeTextInStream operation.
 */
public class RecognizeTextInStreamHeadersInner {
    /**
     * URL to query for status of the operation. The operation ID will expire
     * in 48 hours.
     */
    @JsonProperty(value = "Operation-Location")
    private String operationLocation;

    /**
     * Get the operationLocation value.
     *
     * @return the operationLocation value
     */
    public String operationLocation() {
        return this.operationLocation;
    }

    /**
     * Set the operationLocation value.
     *
     * @param operationLocation the operationLocation value to set
     * @return the RecognizeTextInStreamHeadersInner object itself.
     */
    public RecognizeTextInStreamHeadersInner withOperationLocation(String operationLocation) {
        this.operationLocation = operationLocation;
        return this;
    }

}
