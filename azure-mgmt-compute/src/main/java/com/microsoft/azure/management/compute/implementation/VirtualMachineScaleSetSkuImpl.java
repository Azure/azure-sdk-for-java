package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.management.compute.VirtualMachineScaleSetSku;
import com.microsoft.azure.management.compute.VirtualMachineScaleSetSkuCapacity;
import com.microsoft.azure.management.compute.VirtualMachineScaleSetSkuTypes;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.WrapperImpl;

/**
 * implementation of {@link VirtualMachineScaleSetSku}.
 */
class VirtualMachineScaleSetSkuImpl
        extends WrapperImpl<VirtualMachineScaleSetSkuInner>
        implements VirtualMachineScaleSetSku {
    VirtualMachineScaleSetSkuImpl(VirtualMachineScaleSetSkuInner inner) {
        super(inner);
    }

    @Override
    public String resourceType() {
        return this.inner().resourceType();
    }

    @Override
    public VirtualMachineScaleSetSkuTypes skuType() {
        return new VirtualMachineScaleSetSkuTypes(this.inner().sku());
    }

    @Override
    public VirtualMachineScaleSetSkuCapacity capacity() {
        return this.inner().capacity();
    }
}

