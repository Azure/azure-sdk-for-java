package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.models.ChildResource;

/**
 * An immutable client-side representation of an extension associated with virtual machine instance
 * in a scale set.
 */
@Fluent
public interface VirtualMachineScaleSetVMInstanceExtension extends
        VirtualMachineExtensionBase,
        ChildResource<VirtualMachineScaleSetVM> {
}
