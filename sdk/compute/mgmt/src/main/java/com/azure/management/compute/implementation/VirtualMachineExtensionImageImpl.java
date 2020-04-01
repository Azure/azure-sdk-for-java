/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.compute.implementation;

import com.azure.management.compute.ComputeRoles;
import com.azure.management.compute.OperatingSystemTypes;
import com.azure.management.compute.VirtualMachineExtensionImage;
import com.azure.management.compute.VirtualMachineExtensionImageVersion;
import com.azure.management.compute.models.VirtualMachineExtensionImageInner;
import com.azure.management.resources.fluentcore.model.implementation.WrapperImpl;

/**
 * The implementation for {@link VirtualMachineExtensionImage}.
 */
class VirtualMachineExtensionImageImpl
        extends WrapperImpl<VirtualMachineExtensionImageInner>
        implements VirtualMachineExtensionImage {
    private final VirtualMachineExtensionImageVersion version;

    VirtualMachineExtensionImageImpl(VirtualMachineExtensionImageVersion version, VirtualMachineExtensionImageInner inner) {
        super(inner);
        this.version = version;
    }

    @Override
    public String id() {
        return this.inner().getId();
    }

    @Override
    public String regionName() {
        return this.inner().getLocation();
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
        return OperatingSystemTypes.fromString(this.inner().operatingSystem());
    }

    @Override
    public ComputeRoles computeRole() {
        return ComputeRoles.fromString(this.inner().computeRole());
    }

    @Override
    public String handlerSchema() {
        return this.inner().handlerSchema();
    }

    @Override
    public boolean supportsVirtualMachineScaleSets() {
        return this.inner().vmScaleSetEnabled();
    }

    @Override
    public boolean supportsMultipleExtensions() {
        return this.inner().supportsMultipleExtensions();
    }

    @Override
    public VirtualMachineExtensionImageVersion version() {
        return this.version;
    }
}