/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.compute.implementation;

import com.azure.management.compute.VirtualMachineScaleSetSku;
import com.azure.management.compute.VirtualMachineScaleSetSkuCapacity;
import com.azure.management.compute.VirtualMachineScaleSetSkuTypes;
import com.azure.management.compute.models.VirtualMachineScaleSetSkuInner;
import com.azure.management.resources.fluentcore.model.implementation.WrapperImpl;

/**
 * Implementation of VirtualMachineScaleSetSku.
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
        return VirtualMachineScaleSetSkuTypes.fromSku(this.inner().sku());
    }

    @Override
    public VirtualMachineScaleSetSkuCapacity capacity() {
        return this.inner().capacity();
    }
}

