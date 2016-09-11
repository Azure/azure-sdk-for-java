package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;

/**
 *  Entry point to virtual machine scale set management API.
 */
public interface VirtualMachineScaleSets extends
        SupportsCreating<VirtualMachineScaleSet.DefinitionStages.Blank> {
}
