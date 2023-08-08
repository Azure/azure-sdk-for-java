// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.compute.implementation;

import com.azure.resourcemanager.compute.models.ComputeRoles;
import com.azure.resourcemanager.compute.models.OperatingSystemTypes;
import com.azure.resourcemanager.compute.models.VirtualMachineExtensionImage;
import com.azure.resourcemanager.compute.models.VirtualMachineExtensionImageVersion;
import com.azure.resourcemanager.compute.fluent.models.VirtualMachineExtensionImageInner;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;

/** The implementation for {@link VirtualMachineExtensionImage}. */
class VirtualMachineExtensionImageImpl extends WrapperImpl<VirtualMachineExtensionImageInner>
    implements VirtualMachineExtensionImage {
    private final VirtualMachineExtensionImageVersion version;

    VirtualMachineExtensionImageImpl(
        VirtualMachineExtensionImageVersion version, VirtualMachineExtensionImageInner inner) {
        super(inner);
        this.version = version;
    }

    @Override
    public String id() {
        return this.innerModel().id();
    }

    @Override
    public String regionName() {
        return this.innerModel().location();
    }

    @Override
    public String publisherName() {
        return this.version().type().publisher().name();
    }

    @Override
    public String typeName() {
        return this.version().type().name();
    }

    @Override
    public String versionName() {
        return this.version().name();
    }

    @Override
    public OperatingSystemTypes osType() {
        return OperatingSystemTypes.fromString(this.innerModel().operatingSystem());
    }

    @Override
    public ComputeRoles computeRole() {
        return ComputeRoles.fromString(this.innerModel().computeRole());
    }

    @Override
    public String handlerSchema() {
        return this.innerModel().handlerSchema();
    }

    @Override
    public boolean supportsVirtualMachineScaleSets() {
        return this.innerModel().vmScaleSetEnabled();
    }

    @Override
    public boolean supportsMultipleExtensions() {
        return this.innerModel().supportsMultipleExtensions();
    }

    @Override
    public VirtualMachineExtensionImageVersion version() {
        return this.version;
    }
}
