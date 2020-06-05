// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.management.compute.implementation;

import com.azure.management.compute.ComputeManager;
import com.azure.management.compute.models.VirtualMachineCustomImage;
import com.azure.management.compute.models.VirtualMachineCustomImages;
import com.azure.management.compute.fluent.inner.ImageInner;
import com.azure.management.compute.fluent.ImagesClient;
import com.azure.management.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;

/** The implementation for VirtualMachineCustomImages. */
public class VirtualMachineCustomImagesImpl
    extends TopLevelModifiableResourcesImpl<
        VirtualMachineCustomImage, VirtualMachineCustomImageImpl, ImageInner, ImagesClient, ComputeManager>
    implements VirtualMachineCustomImages {

    public VirtualMachineCustomImagesImpl(final ComputeManager computeManager) {
        super(computeManager.inner().getImages(), computeManager);
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
        return new VirtualMachineCustomImageImpl(inner.name(), inner, this.manager());
    }

    @Override
    public VirtualMachineCustomImageImpl define(String name) {
        return this.wrapModel(name);
    }
}
