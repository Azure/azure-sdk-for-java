// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storage.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.storage.StorageManager;
import com.azure.resourcemanager.storage.fluent.SkusClient;
import com.azure.resourcemanager.storage.models.StorageSku;
import com.azure.resourcemanager.storage.models.StorageSkus;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;

/** The implementation for {@link StorageSkus}. */
public class StorageSkusImpl implements StorageSkus {

    private final StorageManager manager;

    public StorageSkusImpl(StorageManager storageManager) {
        this.manager = storageManager;
    }

    @Override
    public StorageManager manager() {
        return this.manager;
    }

    @Override
    public PagedIterable<StorageSku> list() {
        return PagedConverter.mapPage(this.inner().list(), StorageSkuImpl::new);
    }

    @Override
    public PagedFlux<StorageSku> listAsync() {
        return PagedConverter.mapPage(this.inner().listAsync(), StorageSkuImpl::new);
    }

    public SkusClient inner() {
        return manager.serviceClient().getSkus();
    }
}
