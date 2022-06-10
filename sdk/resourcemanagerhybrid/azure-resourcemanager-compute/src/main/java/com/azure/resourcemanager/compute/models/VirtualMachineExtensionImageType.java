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
    /** @return the resource ID of the virtual machine extension image type */
    String id();

    /** @return the region in which virtual machine extension image type is available */
    String regionName();

    /** @return the publisher of this virtual machine extension image type */
    VirtualMachinePublisher publisher();

    /** @return Virtual machine image extension versions available in this type */
    VirtualMachineExtensionImageVersions versions();
}
