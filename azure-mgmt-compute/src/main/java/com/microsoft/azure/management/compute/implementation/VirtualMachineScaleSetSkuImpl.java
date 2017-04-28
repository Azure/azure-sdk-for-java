/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.compute.VirtualMachineScaleSetSku;
import com.microsoft.azure.management.compute.VirtualMachineScaleSetSkuCapacity;
import com.microsoft.azure.management.compute.VirtualMachineScaleSetSkuTypes;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.WrapperImpl;

/**
 * Implementation of VirtualMachineScaleSetSku.
 */
@LangDefinition
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

