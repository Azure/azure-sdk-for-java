/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.compute.VirtualMachineExtensionImage;
import com.microsoft.azure.management.compute.VirtualMachineExtensionImageType;
import com.microsoft.azure.management.compute.VirtualMachineExtensionImageVersion;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.WrapperImpl;

/**
 * The implementation for VirtualMachineExtensionImageVersion.
 */
@LangDefinition
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
        VirtualMachineExtensionImageInner inner = this.client.get(this.regionName(),
                this.type().publisher().name(),
                this.type().name(),
                this.name());
        return new VirtualMachineExtensionImageImpl(this, inner);
    }
}