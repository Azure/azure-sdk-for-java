// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.management.compute.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.management.compute.models.VirtualMachineExtensionImageType;
import com.azure.management.compute.models.VirtualMachineExtensionImageVersion;
import com.azure.management.compute.models.VirtualMachineExtensionImageVersions;
import com.azure.management.compute.fluent.inner.VirtualMachineExtensionImageInner;
import com.azure.management.compute.fluent.VirtualMachineExtensionImagesClient;
import com.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;
import com.azure.management.resources.fluentcore.utils.PagedConverter;

/** The implementation for VirtualMachineExtensionImageVersions. */
public class VirtualMachineExtensionImageVersionsImpl
    extends ReadableWrappersImpl<
        VirtualMachineExtensionImageVersion, VirtualMachineExtensionImageVersionImpl, VirtualMachineExtensionImageInner>
    implements VirtualMachineExtensionImageVersions {
    private final VirtualMachineExtensionImagesClient client;
    private final VirtualMachineExtensionImageType type;

    VirtualMachineExtensionImageVersionsImpl(
        VirtualMachineExtensionImagesClient client, VirtualMachineExtensionImageType type) {
        this.client = client;
        this.type = type;
    }

    @Override
    public PagedIterable<VirtualMachineExtensionImageVersion> list() {
        return new PagedIterable<>(listAsync());
    }

    @Override
    protected VirtualMachineExtensionImageVersionImpl wrapModel(VirtualMachineExtensionImageInner inner) {
        if (inner == null) {
            return null;
        }
        return new VirtualMachineExtensionImageVersionImpl(this.client, this.type, inner);
    }

    @Override
    public PagedFlux<VirtualMachineExtensionImageVersion> listAsync() {
        return PagedConverter
            .convertListToPagedFlux(client.listVersionsAsync(type.regionName(), type.publisher().name(), type.name()))
            .mapPage(this::wrapModel);
    }
}
