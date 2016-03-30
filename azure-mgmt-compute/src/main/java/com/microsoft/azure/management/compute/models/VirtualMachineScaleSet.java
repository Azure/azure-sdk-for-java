/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.rest.serializer.JsonFlatten;
import com.microsoft.azure.Resource;

/**
 * Describes a Virtual Machine Scale Set.
 */
@JsonFlatten
public class VirtualMachineScaleSet extends Resource {
    /**
     * Gets or sets the virtual machine scale set sku.
     */
    private Sku sku;

    /**
     * Gets or sets the upgrade policy.
     */
    @JsonProperty(value = "properties.upgradePolicy")
    private UpgradePolicy upgradePolicy;

    /**
     * Gets or sets the virtual machine profile.
     */
    @JsonProperty(value = "properties.virtualMachineProfile")
    private VirtualMachineScaleSetVMProfile virtualMachineProfile;

    /**
     * Gets or sets the provisioning state, which only appears in the response.
     */
    @JsonProperty(value = "properties.provisioningState")
    private String provisioningState;

    /**
     * Specifies whether the Virtual Machine Scale Set should be
     * overprovisioned.
     */
    @JsonProperty(value = "properties.overProvision")
    private Boolean overProvision;

    /**
     * Get the sku value.
     *
     * @return the sku value
     */
    public Sku getSku() {
        return this.sku;
    }

    /**
     * Set the sku value.
     *
     * @param sku the sku value to set
     */
    public void setSku(Sku sku) {
        this.sku = sku;
    }

    /**
     * Get the upgradePolicy value.
     *
     * @return the upgradePolicy value
     */
    public UpgradePolicy getUpgradePolicy() {
        return this.upgradePolicy;
    }

    /**
     * Set the upgradePolicy value.
     *
     * @param upgradePolicy the upgradePolicy value to set
     */
    public void setUpgradePolicy(UpgradePolicy upgradePolicy) {
        this.upgradePolicy = upgradePolicy;
    }

    /**
     * Get the virtualMachineProfile value.
     *
     * @return the virtualMachineProfile value
     */
    public VirtualMachineScaleSetVMProfile getVirtualMachineProfile() {
        return this.virtualMachineProfile;
    }

    /**
     * Set the virtualMachineProfile value.
     *
     * @param virtualMachineProfile the virtualMachineProfile value to set
     */
    public void setVirtualMachineProfile(VirtualMachineScaleSetVMProfile virtualMachineProfile) {
        this.virtualMachineProfile = virtualMachineProfile;
    }

    /**
     * Get the provisioningState value.
     *
     * @return the provisioningState value
     */
    public String getProvisioningState() {
        return this.provisioningState;
    }

    /**
     * Set the provisioningState value.
     *
     * @param provisioningState the provisioningState value to set
     */
    public void setProvisioningState(String provisioningState) {
        this.provisioningState = provisioningState;
    }

    /**
     * Get the overProvision value.
     *
     * @return the overProvision value
     */
    public Boolean getOverProvision() {
        return this.overProvision;
    }

    /**
     * Set the overProvision value.
     *
     * @param overProvision the overProvision value to set
     */
    public void setOverProvision(Boolean overProvision) {
        this.overProvision = overProvision;
    }

}
