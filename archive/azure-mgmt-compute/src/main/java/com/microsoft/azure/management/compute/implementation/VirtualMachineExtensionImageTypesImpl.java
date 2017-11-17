/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.compute.VirtualMachineExtensionImageType;
import com.microsoft.azure.management.compute.VirtualMachineExtensionImageTypes;
import com.microsoft.azure.management.compute.VirtualMachinePublisher;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;
import rx.Observable;

/**
 * The implementation for VirtualMachineExtensionImageTypes.
 */
@LangDefinition
class VirtualMachineExtensionImageTypesImpl
        extends ReadableWrappersImpl<VirtualMachineExtensionImageType, VirtualMachineExtensionImageTypeImpl, VirtualMachineExtensionImageInner>
        implements VirtualMachineExtensionImageTypes {
    private final VirtualMachineExtensionImagesInner client;
    private final VirtualMachinePublisher publisher;

    VirtualMachineExtensionImageTypesImpl(VirtualMachineExtensionImagesInner client, VirtualMachinePublisher publisher) {
        this.client = client;
        this.publisher = publisher;
    }

    @Override
    public PagedList<VirtualMachineExtensionImageType> list() {
        return wrapList(this.client.listTypes(this.publisher.region().toString(), this.publisher.name()));
    }

    @Override
    protected VirtualMachineExtensionImageTypeImpl wrapModel(VirtualMachineExtensionImageInner inner) {
        if (inner == null) {
            return null;
        }
        return new VirtualMachineExtensionImageTypeImpl(this.client, this.publisher, inner);
    }

    @Override
    public Observable<VirtualMachineExtensionImageType> listAsync() {
        return wrapListAsync(client.listTypesAsync(this.publisher.region().toString(), this.publisher.name()));
    }
}