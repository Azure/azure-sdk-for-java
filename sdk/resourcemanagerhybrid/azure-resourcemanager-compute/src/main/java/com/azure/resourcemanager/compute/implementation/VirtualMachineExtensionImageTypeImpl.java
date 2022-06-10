// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.compute.implementation;

import com.azure.resourcemanager.compute.models.VirtualMachineExtensionImageType;
import com.azure.resourcemanager.compute.models.VirtualMachineExtensionImageVersions;
import com.azure.resourcemanager.compute.models.VirtualMachinePublisher;
import com.azure.resourcemanager.compute.fluent.models.VirtualMachineExtensionImageInner;
import com.azure.resourcemanager.compute.fluent.VirtualMachineExtensionImagesClient;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;

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
        return this.innerModel().id();
    }

    @Override
    public String name() {
        return this.innerModel().name();
    }

    @Override
    public String regionName() {
        return this.innerModel().location();
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
