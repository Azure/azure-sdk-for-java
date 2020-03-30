/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.compute.implementation;

import com.azure.management.compute.VirtualMachineExtensionImageType;
import com.azure.management.compute.VirtualMachineExtensionImageVersions;
import com.azure.management.compute.VirtualMachinePublisher;
import com.azure.management.compute.models.VirtualMachineExtensionImageInner;
import com.azure.management.compute.models.VirtualMachineExtensionImagesInner;
import com.azure.management.resources.fluentcore.model.implementation.WrapperImpl;

/**
 * The implementation for VirtualMachineExtensionImageType.
 */
class VirtualMachineExtensionImageTypeImpl
        extends WrapperImpl<VirtualMachineExtensionImageInner>
        implements VirtualMachineExtensionImageType {
    private final VirtualMachineExtensionImagesInner client;
    private final VirtualMachinePublisher publisher;

    VirtualMachineExtensionImageTypeImpl(VirtualMachineExtensionImagesInner client,
                                         VirtualMachinePublisher publisher,
                                         VirtualMachineExtensionImageInner inner) {
        super(inner);
        this.client = client;
        this.publisher = publisher;
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
    public VirtualMachinePublisher publisher() {
        return this.publisher;
    }

    @Override
    public VirtualMachineExtensionImageVersions versions() {
        return new VirtualMachineExtensionImageVersionsImpl(this.client, this);
    }
}