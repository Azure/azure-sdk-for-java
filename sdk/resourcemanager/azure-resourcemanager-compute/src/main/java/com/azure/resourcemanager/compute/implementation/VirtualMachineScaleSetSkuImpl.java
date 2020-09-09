// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.compute.implementation;

import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetSku;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetSkuCapacity;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetSkuTypes;
import com.azure.resourcemanager.compute.fluent.inner.VirtualMachineScaleSetSkuInner;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;

/** Implementation of VirtualMachineScaleSetSku. */
class VirtualMachineScaleSetSkuImpl extends WrapperImpl<VirtualMachineScaleSetSkuInner>
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
        return VirtualMachineScaleSetSkuTypes.fromSku(this.inner().sku());
    }

    @Override
    public VirtualMachineScaleSetSkuCapacity capacity() {
        return this.inner().capacity();
    }
}
