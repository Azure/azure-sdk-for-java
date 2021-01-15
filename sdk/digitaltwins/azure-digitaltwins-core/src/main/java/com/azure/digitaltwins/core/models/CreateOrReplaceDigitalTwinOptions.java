// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.Context;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The optional parameters for
 * {@link com.azure.digitaltwins.core.DigitalTwinsClient#createOrReplaceDigitalTwinWithResponse(String, Object, Class, CreateOrReplaceDigitalTwinOptions, Context)} and
 * {@link com.azure.digitaltwins.core.DigitalTwinsAsyncClient#createOrReplaceDigitalTwinWithResponse(String, Object, Class, CreateOrReplaceDigitalTwinOptions)}
 */
@Fluent
public final class CreateOrReplaceDigitalTwinOptions {
    /*
     * If-Non-Match header that makes the request method conditional on a recipient cache or origin server either not having any current representation of the target resource.
     * Acceptable values are null or "*".
     * If IfNonMatch option is null the service will replace the existing entity with the new entity.
     * If IfNonMatch option is "*" the service will reject the request if the entity already exists.
     */
    @JsonProperty(value = "If-None-Match")
    private String ifNoneMatch;

    /**
     * Get the ifNoneMatch property
     *
     * If-Non-Match header makes the request method conditional on a recipient cache or origin server either not having any current representation of the target resource.
     * Acceptable values are null or "*".
     * If IfNonMatch option is null the service will replace the existing entity with the new entity.
     * If IfNonMatch option is "*" the service will reject the request if the entity already exists.
     *
     * @return the ifNoneMatch value.
     */
    public String getIfNoneMatch() {
        return this.ifNoneMatch;
    }

    /**
     * Set the ifNoneMatch property.
     *
     * If-Non-Match header makes the request method conditional on a recipient cache or origin server either not having any current representation of the target resource.
     * Acceptable values are null or "*".
     * If IfNonMatch option is null the service will replace the existing entity with the new entity.
     * If IfNonMatch option is "*" the service will reject the request if the entity already exists.
     *
     * @param ifNoneMatch the ifNoneMatch value to set.
     * @return the CreateOrReplaceRelationshipOptions object itself.
     */
    public CreateOrReplaceDigitalTwinOptions setIfNoneMatch(String ifNoneMatch) {
        this.ifNoneMatch = ifNoneMatch;
        return this;
    }
}
