// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.models.JsonPatchDocument;
import com.azure.core.util.Context;
import com.azure.digitaltwins.core.implementation.models.DigitalTwinsUpdateOptions;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * The optional parameters for
 * {@link com.azure.digitaltwins.core.DigitalTwinsClient#updateDigitalTwinWithResponse(String, JsonPatchDocument, UpdateDigitalTwinOptions, Context)} and
 * {@link com.azure.digitaltwins.core.DigitalTwinsAsyncClient#updateDigitalTwinWithResponse(String, JsonPatchDocument, UpdateDigitalTwinOptions)}
 */
@Fluent
public final class UpdateDigitalTwinOptions {
    /*
     * Only perform the operation if the entity's etag matches one of the etags
     * provided or * is provided.
     */
    @JsonProperty(value = "If-Match")
    private String ifMatch;

    /**
     * Get the ifMatch property: Only perform the operation if the entity's etag matches one of the etags provided or *
     * is provided.
     *
     * @return the ifMatch value.
     */
    public String getIfMatch() {
        return this.ifMatch;
    }

    /**
     * Set the ifMatch property: Only perform the operation if the entity's etag matches one of the etags provided or *
     * is provided.
     *
     * @param ifMatch the ifMatch value to set.
     * @return the UpdateDigitalTwinOptions object itself.
     */
    public UpdateDigitalTwinOptions setIfMatch(String ifMatch) {
        this.ifMatch = ifMatch;
        return this;
    }
}
