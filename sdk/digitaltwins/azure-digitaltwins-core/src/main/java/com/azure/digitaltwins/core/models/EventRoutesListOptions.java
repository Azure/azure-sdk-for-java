// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.Context;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The options that can be set when listing event routes using
 * {@link com.azure.digitaltwins.core.DigitalTwinsClient#listEventRoutes(EventRoutesListOptions, Context)} or
 * {@link com.azure.digitaltwins.core.DigitalTwinsAsyncClient#listEventRoutes(EventRoutesListOptions)}.
 */
// This class exists so that the public APIs don't directly consume a generated type and so that we can avoid exposing a validate() method
// that the generated type comes with when client side validation is enabled.
@Fluent
public final class EventRoutesListOptions {
    /*
     * The maximum number of items to retrieve per request. The server may
     * choose to return less than the requested max.
     */
    @JsonProperty(value = "MaxItemCount")
    private Integer maxItemCount;

    /**
     * Get the maxItemCount property: The maximum number of items to retrieve per request. The server may choose to
     * return less than the requested max.
     *
     * @return the maxItemCount value.
     */
    public Integer getMaxItemCount() {
        return this.maxItemCount;
    }

    /**
     * Set the maxItemCount property: The maximum number of items to retrieve per request. The server may choose to
     * return less than the requested max.
     *
     * @param maxItemCount the maxItemCount value to set.
     * @return the EventRoutesListOptions object itself.
     */
    public EventRoutesListOptions setMaxItemCount(Integer maxItemCount) {
        this.maxItemCount = maxItemCount;
        return this;
    }
}
