package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.management.compute.ComputeRoles;
import com.microsoft.azure.management.compute.OperatingSystemTypes;
import com.microsoft.azure.management.compute.VirtualMachineExtensionImage;
import com.microsoft.azure.management.compute.VirtualMachineExtensionImageVersion;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.WrapperImpl;

/**
 * The implementation for {@link VirtualMachineExtensionImage}.
 */
public class VirtualMachineExtensionImageImpl
        extends WrapperImpl<VirtualMachineExtensionImageInner>
        implements VirtualMachineExtensionImage {
    private final VirtualMachineExtensionImageVersion version;

    VirtualMachineExtensionImageImpl(VirtualMachineExtensionImageVersion version, VirtualMachineExtensionImageInner inner) {
        super(inner);
        this.version = version;
    }

    @Override
    public String id() {
        return this.inner().id();
    }

    @Override
    public String regionName() {
        return this.inner().location();
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
    public boolean vmScaleSetEnabled() {
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