/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;

import java.util.List;

/**
 * Collection of stamp capacities.
 */
public class StampCapacityCollectionInner {
    /**
     * Collection of resources.
     */
    private List<StampCapacity> value;

    /**
     * Link to next page of resources.
     */
    private String nextLink;

    /**
     * Get the value value.
     *
     * @return the value value
     */
    public List<StampCapacity> value() {
        return this.value;
    }

    /**
     * Set the value value.
     *
     * @param value the value value to set
     * @return the StampCapacityCollectionInner object itself.
     */
    public StampCapacityCollectionInner withValue(List<StampCapacity> value) {
        this.value = value;
        return this;
    }

    /**
     * Get the nextLink value.
     *
     * @return the nextLink value
     */
    public String nextLink() {
        return this.nextLink;
    }

    /**
     * Set the nextLink value.
     *
     * @param nextLink the nextLink value to set
     * @return the StampCapacityCollectionInner object itself.
     */
    public StampCapacityCollectionInner withNextLink(String nextLink) {
        this.nextLink = nextLink;
        return this;
    }

}
