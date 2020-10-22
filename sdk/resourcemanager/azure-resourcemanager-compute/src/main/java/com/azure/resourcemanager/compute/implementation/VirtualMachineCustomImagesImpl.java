// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.implementation;

import com.azure.resourcemanager.compute.ComputeManager;
import com.azure.resourcemanager.compute.models.VirtualMachineCustomImage;
import com.azure.resourcemanager.compute.models.VirtualMachineCustomImages;
import com.azure.resourcemanager.compute.fluent.models.ImageInner;
import com.azure.resourcemanager.compute.fluent.ImagesClient;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;

/** The implementation for VirtualMachineCustomImages. */
public class VirtualMachineCustomImagesImpl
    extends TopLevelModifiableResourcesImpl<
        VirtualMachineCustomImage, VirtualMachineCustomImageImpl, ImageInner, ImagesClient, ComputeManager>
    implements VirtualMachineCustomImages {

    public VirtualMachineCustomImagesImpl(final ComputeManager computeManager) {
        super(computeManager.serviceClient().getImages(), computeManager);
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
