// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.models.ChildResource;

/** An immutable client-side representation of an extension associated with virtual machine instance in a scale set. */
@Fluent
public interface VirtualMachineScaleSetVMInstanceExtension
    extends VirtualMachineExtensionBase, ChildResource<VirtualMachineScaleSetVM> {
    /** @return the instance view of the scale set virtual machine extension */
    VirtualMachineExtensionInstanceView instanceView();
}
