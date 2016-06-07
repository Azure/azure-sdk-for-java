/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation.api;

import java.util.List;
import com.microsoft.azure.SubResource;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.rest.serializer.JsonFlatten;
import com.microsoft.azure.Resource;

/**
 * Create or update Availability Set parameters.
 */
@JsonFlatten
public class AvailabilitySetInner extends Resource {
    /**
     * Gets or sets Update Domain count.
     */
    @JsonProperty(value = "properties.platformUpdateDomainCount")
    private Integer platformUpdateDomainCount;

    /**
     * Gets or sets Fault Domain count.
     */
    @JsonProperty(value = "properties.platformFaultDomainCount")
    private Integer platformFaultDomainCount;

    /**
     * Gets or sets a list containing reference to all Virtual Machines
     * created under this Availability Set.
     */
    @JsonProperty(value = "properties.virtualMachines")
    private List<SubResource> virtualMachines;

    /**
     * Gets or sets the resource status information.
     */
    @JsonProperty(value = "properties.statuses")
    private List<InstanceViewStatus> statuses;

    /**
     * Get the platformUpdateDomainCount value.
     *
     * @return the platformUpdateDomainCount value
     */
    public Integer platformUpdateDomainCount() {
        return this.platformUpdateDomainCount;
    }

    /**
     * Set the platformUpdateDomainCount value.
     *
     * @param platformUpdateDomainCount the platformUpdateDomainCount value to set
     * @return the AvailabilitySetInner object itself.
     */
    public AvailabilitySetInner withPlatformUpdateDomainCount(Integer platformUpdateDomainCount) {
        this.platformUpdateDomainCount = platformUpdateDomainCount;
        return this;
    }

    /**
     * Get the platformFaultDomainCount value.
     *
     * @return the platformFaultDomainCount value
     */
    public Integer platformFaultDomainCount() {
        return this.platformFaultDomainCount;
    }

    /**
     * Set the platformFaultDomainCount value.
     *
     * @param platformFaultDomainCount the platformFaultDomainCount value to set
     * @return the AvailabilitySetInner object itself.
     */
    public AvailabilitySetInner withPlatformFaultDomainCount(Integer platformFaultDomainCount) {
        this.platformFaultDomainCount = platformFaultDomainCount;
        return this;
    }

    /**
     * Get the virtualMachines value.
     *
     * @return the virtualMachines value
     */
    public List<SubResource> virtualMachines() {
        return this.virtualMachines;
    }

    /**
     * Set the virtualMachines value.
     *
     * @param virtualMachines the virtualMachines value to set
     * @return the AvailabilitySetInner object itself.
     */
    public AvailabilitySetInner withVirtualMachines(List<SubResource> virtualMachines) {
        this.virtualMachines = virtualMachines;
        return this;
    }

    /**
     * Get the statuses value.
     *
     * @return the statuses value
     */
    public List<InstanceViewStatus> statuses() {
        return this.statuses;
    }

    /**
     * Set the statuses value.
     *
     * @param statuses the statuses value to set
     * @return the AvailabilitySetInner object itself.
     */
    public AvailabilitySetInner withStatuses(List<InstanceViewStatus> statuses) {
        this.statuses = statuses;
        return this;
    }

}
