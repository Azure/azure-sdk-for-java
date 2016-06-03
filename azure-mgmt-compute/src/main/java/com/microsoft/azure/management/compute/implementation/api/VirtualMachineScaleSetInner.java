/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.rest.serializer.JsonFlatten;
import com.microsoft.azure.Resource;

/**
 * Describes a Virtual Machine Scale Set.
 */
@JsonFlatten
public class VirtualMachineScaleSetInner extends Resource {
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
    public Sku sku() {
        return this.sku;
    }

    /**
     * Set the sku value.
     *
     * @param sku the sku value to set
     * @return the VirtualMachineScaleSetInner object itself.
     */
    public VirtualMachineScaleSetInner withSku(Sku sku) {
        this.sku = sku;
        return this;
    }

    /**
     * Get the upgradePolicy value.
     *
     * @return the upgradePolicy value
     */
    public UpgradePolicy upgradePolicy() {
        return this.upgradePolicy;
    }

    /**
     * Set the upgradePolicy value.
     *
     * @param upgradePolicy the upgradePolicy value to set
     * @return the VirtualMachineScaleSetInner object itself.
     */
    public VirtualMachineScaleSetInner withUpgradePolicy(UpgradePolicy upgradePolicy) {
        this.upgradePolicy = upgradePolicy;
        return this;
    }

    /**
     * Get the virtualMachineProfile value.
     *
     * @return the virtualMachineProfile value
     */
    public VirtualMachineScaleSetVMProfile virtualMachineProfile() {
        return this.virtualMachineProfile;
    }

    /**
     * Set the virtualMachineProfile value.
     *
     * @param virtualMachineProfile the virtualMachineProfile value to set
     * @return the VirtualMachineScaleSetInner object itself.
     */
    public VirtualMachineScaleSetInner withVirtualMachineProfile(VirtualMachineScaleSetVMProfile virtualMachineProfile) {
        this.virtualMachineProfile = virtualMachineProfile;
        return this;
    }

    /**
     * Get the provisioningState value.
     *
     * @return the provisioningState value
     */
    public String provisioningState() {
        return this.provisioningState;
    }

    /**
     * Set the provisioningState value.
     *
     * @param provisioningState the provisioningState value to set
     * @return the VirtualMachineScaleSetInner object itself.
     */
    public VirtualMachineScaleSetInner withProvisioningState(String provisioningState) {
        this.provisioningState = provisioningState;
        return this;
    }

    /**
     * Get the overProvision value.
     *
     * @return the overProvision value
     */
    public Boolean overProvision() {
        return this.overProvision;
    }

    /**
     * Set the overProvision value.
     *
     * @param overProvision the overProvision value to set
     * @return the VirtualMachineScaleSetInner object itself.
     */
    public VirtualMachineScaleSetInner withOverProvision(Boolean overProvision) {
        this.overProvision = overProvision;
        return this;
    }

}
