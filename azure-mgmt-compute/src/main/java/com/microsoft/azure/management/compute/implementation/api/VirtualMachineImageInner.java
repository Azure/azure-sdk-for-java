/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation.api;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.rest.serializer.JsonFlatten;

/**
 * Describes a Virtual Machine Image.
 */
@JsonFlatten
public class VirtualMachineImageInner extends VirtualMachineImageResourceInner {
    /**
     * The plan property.
     */
    @JsonProperty(value = "properties.plan")
    private PurchasePlan plan;

    /**
     * The osDiskImage property.
     */
    @JsonProperty(value = "properties.osDiskImage")
    private OSDiskImage osDiskImage;

    /**
     * The dataDiskImages property.
     */
    @JsonProperty(value = "properties.dataDiskImages")
    private List<DataDiskImage> dataDiskImages;

    /**
     * Get the plan value.
     *
     * @return the plan value
     */
    public PurchasePlan plan() {
        return this.plan;
    }

    /**
     * Set the plan value.
     *
     * @param plan the plan value to set
     * @return the VirtualMachineImageInner object itself.
     */
    public VirtualMachineImageInner withPlan(PurchasePlan plan) {
        this.plan = plan;
        return this;
    }

    /**
     * Get the osDiskImage value.
     *
     * @return the osDiskImage value
     */
    public OSDiskImage osDiskImage() {
        return this.osDiskImage;
    }

    /**
     * Set the osDiskImage value.
     *
     * @param osDiskImage the osDiskImage value to set
     * @return the VirtualMachineImageInner object itself.
     */
    public VirtualMachineImageInner withOsDiskImage(OSDiskImage osDiskImage) {
        this.osDiskImage = osDiskImage;
        return this;
    }

    /**
     * Get the dataDiskImages value.
     *
     * @return the dataDiskImages value
     */
    public List<DataDiskImage> dataDiskImages() {
        return this.dataDiskImages;
    }

    /**
     * Set the dataDiskImages value.
     *
     * @param dataDiskImages the dataDiskImages value to set
     * @return the VirtualMachineImageInner object itself.
     */
    public VirtualMachineImageInner withDataDiskImages(List<DataDiskImage> dataDiskImages) {
        this.dataDiskImages = dataDiskImages;
        return this;
    }

}
