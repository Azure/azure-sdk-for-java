// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.compute.implementation;

import com.azure.resourcemanager.compute.models.VirtualMachineExtensionImage;
import com.azure.resourcemanager.compute.models.VirtualMachineExtensionImageType;
import com.azure.resourcemanager.compute.models.VirtualMachineExtensionImageVersion;
import com.azure.resourcemanager.compute.fluent.inner.VirtualMachineExtensionImageInner;
import com.azure.resourcemanager.compute.fluent.VirtualMachineExtensionImagesClient;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;
import reactor.core.publisher.Mono;

/** The implementation for VirtualMachineExtensionImageVersion. */
class VirtualMachineExtensionImageVersionImpl extends WrapperImpl<VirtualMachineExtensionImageInner>
    implements VirtualMachineExtensionImageVersion {
    private final VirtualMachineExtensionImagesClient client;
    private final VirtualMachineExtensionImageType type;

    VirtualMachineExtensionImageVersionImpl(
        VirtualMachineExtensionImagesClient client,
        VirtualMachineExtensionImageType extensionImageType,
        VirtualMachineExtensionImageInner inner) {
        super(inner);
        this.client = client;
        this.type = extensionImageType;
    }

    @Override
    public String id() {
        return this.inner().id();
    }

    @Override
    public String name() {
        return this.inner().name();
    }

    @Override
    public String regionName() {
        return this.inner().location();
    }

    @Override
    public VirtualMachineExtensionImageType type() {
        return this.type;
    }

    @Override
    public VirtualMachineExtensionImage getImage() {
        VirtualMachineExtensionImageInner inner =
            this.client.get(this.regionName(), this.type().publisher().name(), this.type().name(), this.name());
        if (inner == null) {
            return null;
        }
        return new VirtualMachineExtensionImageImpl(this, inner);
    }

    @Override
    public Mono<VirtualMachineExtensionImage> getImageAsync() {
        final VirtualMachineExtensionImageVersionImpl self = this;
        return client
            .getAsync(regionName(), type().publisher().name(), type().name(), name())
            .map(inner -> new VirtualMachineExtensionImageImpl(self, inner));
    }
}
