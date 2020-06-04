// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.management.compute.implementation;

import com.azure.management.compute.models.VirtualMachineExtensionImageType;
import com.azure.management.compute.models.VirtualMachineExtensionImageVersions;
import com.azure.management.compute.models.VirtualMachinePublisher;
import com.azure.management.compute.fluent.inner.VirtualMachineExtensionImageInner;
import com.azure.management.compute.fluent.VirtualMachineExtensionImagesClient;
import com.azure.management.resources.fluentcore.model.implementation.WrapperImpl;

/** The implementation for VirtualMachineExtensionImageType. */
class VirtualMachineExtensionImageTypeImpl extends WrapperImpl<VirtualMachineExtensionImageInner>
    implements VirtualMachineExtensionImageType {
    private final VirtualMachineExtensionImagesClient client;
    private final VirtualMachinePublisher publisher;

    VirtualMachineExtensionImageTypeImpl(
        VirtualMachineExtensionImagesClient client,
        VirtualMachinePublisher publisher,
        VirtualMachineExtensionImageInner inner) {
        super(inner);
        this.client = client;
        this.publisher = publisher;
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
    public VirtualMachinePublisher publisher() {
        return this.publisher;
    }

    @Override
    public VirtualMachineExtensionImageVersions versions() {
        return new VirtualMachineExtensionImageVersionsImpl(this.client, this);
    }
}
