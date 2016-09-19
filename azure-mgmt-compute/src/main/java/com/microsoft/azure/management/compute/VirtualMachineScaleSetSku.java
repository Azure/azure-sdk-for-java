package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.apigeneration.LangDefinition;

/**
 * A type representing sku available for virtual machines in a scale set.
 */
@LangDefinition(ContainerName = "~/")
public interface VirtualMachineScaleSetSku {
    /**
     * @return the type of resource the sku applies to.
     */
     String resourceType();

    /**
     * @return the Sku type.
     */
    VirtualMachineScaleSetSkuTypes skuType();

    /**
     * @return available scaling information.
     */
    VirtualMachineScaleSetSkuCapacity capacity();
}
