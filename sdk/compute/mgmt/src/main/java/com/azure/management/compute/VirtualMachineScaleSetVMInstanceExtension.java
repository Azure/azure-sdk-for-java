/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.compute;

import com.azure.core.annotation.Fluent;
import com.azure.management.resources.fluentcore.arm.models.ChildResource;

/**
 * An immutable client-side representation of an extension associated with virtual machine instance
 * in a scale set.
 */
@Fluent
public interface VirtualMachineScaleSetVMInstanceExtension extends
        VirtualMachineExtensionBase,
        ChildResource<VirtualMachineScaleSetVM> {
    /**
     * @return the instance view of the scale set virtual machine extension
     */
    VirtualMachineExtensionInstanceView instanceView();
}
