/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.compute.implementation;

import com.azure.management.compute.VirtualMachineCustomImage;
import com.azure.management.compute.VirtualMachineCustomImages;
import com.azure.management.compute.models.ImageInner;
import com.azure.management.compute.models.ImagesInner;
import com.azure.management.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;

/**
 * The implementation for VirtualMachineCustomImages.
 */
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
        if (inner == null) {
            return null;
        }
        return new VirtualMachineCustomImageImpl(inner.getName(), inner, this.manager());
    }

    @Override
    public VirtualMachineCustomImageImpl define(String name) {
        return this.wrapModel(name);
    }
}