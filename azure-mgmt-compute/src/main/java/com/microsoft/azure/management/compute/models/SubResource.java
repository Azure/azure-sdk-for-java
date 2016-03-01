/**
 * Object]
 */

package com.microsoft.azure.management.compute.models;

import com.microsoft.azure.BaseResource;

/**
 * The SubResource model.
 */
public class SubResource extends BaseResource {
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
     */
    public void setId(String id) {
        this.id = id;
    }

}
