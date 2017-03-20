/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.compute.VirtualMachineCustomImage;
import com.microsoft.azure.management.compute.VirtualMachineCustomImages;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;

/**
 * The implementation for VirtualMachineCustomImages.
 */
@LangDefinition
class VirtualMachineCustomImagesImpl extends TopLevelModifiableResourcesImpl<
        VirtualMachineCustomImage,
        VirtualMachineCustomImageImpl,
        ImageInner,
        ImagesInner,
        ComputeManager>
        implements VirtualMachineCustomImages {

    VirtualMachineCustomImagesImpl(final ComputeManager computeManager) {
        super(computeManager.inner().images(), computeManager);
    }

    @Override
    protected VirtualMachineCustomImageImpl wrapModel(String name) {
        return new VirtualMachineCustomImageImpl(name, new ImageInner(), this.manager());
    }

    @Override
    protected VirtualMachineCustomImageImpl wrapModel(ImageInner inner) {
        return new VirtualMachineCustomImageImpl(inner.name(), inner, this.manager());
    }

    @Override
    public VirtualMachineCustomImageImpl define(String name) {
        return this.wrapModel(name);
    }
}