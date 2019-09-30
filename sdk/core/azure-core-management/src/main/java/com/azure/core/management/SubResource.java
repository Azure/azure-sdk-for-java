// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management;

/**
 * The SubResource model.
 */
public class SubResource {
    /**
     * Resource Id.
     */
    private String id;

    /**
     * Get the id value.
     *
     * @return the id value
     */
    public String getId() {
        return this.id;
    }

    /**
     * Set the id value.
     *
     * @param id the id value to set
     * @return the sub resource itself
     */
    public SubResource setId(String id) {
        this.id = id;
        return this;
    }
}
