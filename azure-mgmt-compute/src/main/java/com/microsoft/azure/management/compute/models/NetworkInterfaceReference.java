/**
 * Object]
 */

package com.microsoft.azure.management.compute.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.rest.serializer.JsonFlatten;

/**
 * Describes a network interface reference.
 */
@JsonFlatten
public class NetworkInterfaceReference extends SubResource {
    /**
     * Gets or sets whether this is a primary NIC on a virtual machine.
     */
    @JsonProperty(value = "properties.primary")
    private Boolean primary;

    /**
     * Get the primary value.
     *
     * @return the primary value
     */
    public Boolean getPrimary() {
        return this.primary;
    }

    /**
     * Set the primary value.
     *
     * @param primary the primary value to set
     */
    public void setPrimary(Boolean primary) {
        this.primary = primary;
    }

}
