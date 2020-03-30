/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.compute.implementation;

import com.azure.management.compute.VirtualMachineExtensionImage;
import com.azure.management.compute.VirtualMachineExtensionImageType;
import com.azure.management.compute.VirtualMachineExtensionImageVersion;
import com.azure.management.compute.models.VirtualMachineExtensionImageInner;
import com.azure.management.compute.models.VirtualMachineExtensionImagesInner;
import com.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import reactor.core.publisher.Mono;

/**
 * The implementation for VirtualMachineExtensionImageVersion.
 */
class VirtualMachineExtensionImageVersionImpl
        extends WrapperImpl<VirtualMachineExtensionImageInner>
        implements VirtualMachineExtensionImageVersion {
    private final VirtualMachineExtensionImagesInner client;
    private final VirtualMachineExtensionImageType type;

    VirtualMachineExtensionImageVersionImpl(VirtualMachineExtensionImagesInner client,
                                            VirtualMachineExtensionImageType extensionImageType,
                                            VirtualMachineExtensionImageInner inner) {
        super(inner);
        this.client = client;
        this.type = extensionImageType;
    }

    @Override
    public String id() {
        return this.inner().getId();
    }

    @Override
    public String name() {
        return this.inner().getName();
    }

    @Override
    public String regionName() {
        return this.inner().getLocation();
    }

    @Override
    public VirtualMachineExtensionImageType type() {
        return this.type;
    }

    @Override
    public VirtualMachineExtensionImage getImage() {
        VirtualMachineExtensionImageInner inner = this.client.get(this.regionName(),
                this.type().publisher().name(),
                this.type().name(),
                this.name());
        if (inner == null) {
            return null;
        }
        return new VirtualMachineExtensionImageImpl(this, inner);
    }

    @Override
    public Mono<VirtualMachineExtensionImage> getImageAsync() {
        final VirtualMachineExtensionImageVersionImpl self = this;
        return client.getAsync(regionName(), type().publisher().name(), type().name(), name())
                .onErrorResume(e -> Mono.empty())
                .map(inner -> new VirtualMachineExtensionImageImpl(self, inner));
    }
}