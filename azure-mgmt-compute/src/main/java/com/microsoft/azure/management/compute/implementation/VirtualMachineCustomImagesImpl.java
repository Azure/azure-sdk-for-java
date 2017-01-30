package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.compute.VirtualMachineCustomImage;
import com.microsoft.azure.management.compute.VirtualMachineCustomImages;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import rx.Completable;

/**
 * The implementation for {@link VirtualMachineCustomImages}.
 */
@LangDefinition
class VirtualMachineCustomImagesImpl extends GroupableResourcesImpl<
        VirtualMachineCustomImage,
        VirtualMachineCustomImageImpl,
        ImageInner,
        ImagesInner,
        ComputeManager>
        implements VirtualMachineCustomImages {

    VirtualMachineCustomImagesImpl(
            final ImagesInner client,
            final ComputeManager computeManager) {
        super(client, computeManager);
    }

    @Override
    public Completable deleteByGroupAsync(String groupName, String name) {
        return this.innerCollection.deleteAsync(groupName, name).toCompletable();
    }

    @Override
    public VirtualMachineCustomImage getByGroup(String resourceGroupName, String name) {
        return wrapModel(this.innerCollection.get(resourceGroupName, name));
    }

    @Override
    public PagedList<VirtualMachineCustomImage> listByGroup(String resourceGroupName) {
        return wrapList(this.innerCollection.listByResourceGroup(resourceGroupName));
    }

    @Override
    protected VirtualMachineCustomImageImpl wrapModel(String name) {
        return new VirtualMachineCustomImageImpl(name,
                new ImageInner(),
                this.innerCollection,
                this.manager());
    }

    @Override
    protected VirtualMachineCustomImageImpl wrapModel(ImageInner inner) {
        return new VirtualMachineCustomImageImpl(inner.name(),
                inner,
                this.innerCollection,
                this.manager());
    }

    @Override
    public VirtualMachineCustomImage.DefinitionStages.Blank define(String name) {
        return this.wrapModel(name);
    }

    @Override
    public PagedList<VirtualMachineCustomImage> list() {
        return wrapList(this.innerCollection.list());
    }
}