/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.compute.VirtualMachineCustomImage;
import com.microsoft.azure.management.compute.VirtualMachineCustomImages;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ListableResourcesImpl;
import rx.Completable;
import rx.Observable;

/**
 * The implementation for VirtualMachineCustomImages.
 */
@LangDefinition
class VirtualMachineCustomImagesImpl extends ListableResourcesImpl<
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
    public Completable deleteByGroupAsync(String groupName, String name) {
        return this.inner().deleteAsync(groupName, name).toCompletable();
    }

    @Override
    protected Observable<ImageInner> getAsync(String resourceGroupName, String name) {
        return this.inner().getByResourceGroupAsync(resourceGroupName, name);
    }

    @Override
    public PagedList<VirtualMachineCustomImage> listByGroup(String resourceGroupName) {
        return wrapList(this.inner().listByResourceGroup(resourceGroupName));
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

    @Override
    public PagedList<VirtualMachineCustomImage> list() {
        return wrapList(this.inner().list());
    }
}