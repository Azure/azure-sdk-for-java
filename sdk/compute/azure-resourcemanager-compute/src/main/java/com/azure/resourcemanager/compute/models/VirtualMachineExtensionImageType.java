// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.compute.fluent.models.VirtualMachineExtensionImageInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasName;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;

/** An immutable client-side representation of an Azure virtual machine extension image type. */
@Fluent
public interface VirtualMachineExtensionImageType extends HasInnerModel<VirtualMachineExtensionImageInner>, HasName {
    /**
     * Gets the resource ID of the virtual machine extension image type.
     *
     * @return the resource ID of the virtual machine extension image type
     */
    String id();

    /**
     * Gets the region in which virtual machine extension image type is available.
     *
     * @return the region in which virtual machine extension image type is available
     */
    String regionName();

    /**
     * Gets the publisher of this virtual machine extension image type.
     *
     * @return the publisher of this virtual machine extension image type
     */
    VirtualMachinePublisher publisher();

    /**
     * Gets Virtual machine image extension versions available in this type.
     *
     * @return Virtual machine image extension versions available in this type
     */
    VirtualMachineExtensionImageVersions versions();
}
