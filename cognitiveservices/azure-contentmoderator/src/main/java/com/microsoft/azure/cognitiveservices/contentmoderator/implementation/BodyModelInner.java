/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.contentmoderator.implementation;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The BodyModelInner model.
 */
public class BodyModelInner {
    /**
     * The dataRepresentation property.
     */
    @JsonProperty(value = "DataRepresentation")
    private String dataRepresentation;

    /**
     * The value property.
     */
    @JsonProperty(value = "Value")
    private String value;

    /**
     * Get the dataRepresentation value.
     *
     * @return the dataRepresentation value
     */
    public String dataRepresentation() {
        return this.dataRepresentation;
    }

    /**
     * Set the dataRepresentation value.
     *
     * @param dataRepresentation the dataRepresentation value to set
     * @return the BodyModelInner object itself.
     */
    public BodyModelInner withDataRepresentation(String dataRepresentation) {
        this.dataRepresentation = dataRepresentation;
        return this;
    }

    /**
     * Get the value value.
     *
     * @return the value value
     */
    public String value() {
        return this.value;
    }

    /**
     * Set the value value.
     *
     * @param value the value value to set
     * @return the BodyModelInner object itself.
     */
    public BodyModelInner withValue(String value) {
        this.value = value;
        return this;
    }

}
