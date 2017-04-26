/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.compute.VirtualMachineOffer;
import com.microsoft.azure.management.compute.VirtualMachineSkus;
import com.microsoft.azure.management.compute.VirtualMachineSku;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;
import rx.Observable;
import rx.functions.Func1;

import java.util.List;

/**
 * The implementation for {@link VirtualMachineSkus}.
 */
@LangDefinition
class VirtualMachineSkusImpl
        extends ReadableWrappersImpl<VirtualMachineSku, VirtualMachineSkuImpl, VirtualMachineImageResourceInner>
        implements VirtualMachineSkus {

    private final VirtualMachineImagesInner innerCollection;
    private final VirtualMachineOffer offer;

    VirtualMachineSkusImpl(VirtualMachineOffer offer, VirtualMachineImagesInner innerCollection) {
        this.innerCollection = innerCollection;
        this.offer = offer;
    }

    @Override
    public PagedList<VirtualMachineSku> list() {
        return wrapList(innerCollection.listSkus(
                offer.region().toString(),
                offer.publisher().name(),
                offer.name()));
    }

    @Override
    protected VirtualMachineSkuImpl wrapModel(VirtualMachineImageResourceInner inner) {
        if (inner == null) {
            return null;
        }
        return new VirtualMachineSkuImpl(this.offer, inner.name(), innerCollection);
    }

    @Override
    public Observable<VirtualMachineSku> listAsync() {
        return innerCollection.listSkusAsync(
                offer.region().toString(),
                offer.publisher().name(),
                offer.name()).flatMap(new Func1<List<VirtualMachineImageResourceInner>, Observable<VirtualMachineImageResourceInner>>() {
            @Override
            public Observable<VirtualMachineImageResourceInner> call(List<VirtualMachineImageResourceInner> virtualMachineImageResourceInners) {
                return Observable.from(virtualMachineImageResourceInners);
            }
        }).map(new Func1<VirtualMachineImageResourceInner, VirtualMachineSku>() {
            @Override
            public VirtualMachineSku call(VirtualMachineImageResourceInner virtualMachineImageResourceInner) {
                return wrapModel(virtualMachineImageResourceInner);
            }
        });
    }
}
