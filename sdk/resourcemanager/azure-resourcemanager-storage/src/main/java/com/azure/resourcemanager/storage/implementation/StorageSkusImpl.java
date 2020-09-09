// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storage.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.storage.StorageManager;
import com.azure.resourcemanager.storage.fluent.SkusClient;
import com.azure.resourcemanager.storage.models.StorageSku;
import com.azure.resourcemanager.storage.models.StorageSkus;

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
        return this.inner().list().mapPage(StorageSkuImpl::new);
    }

    @Override
    public PagedFlux<StorageSku> listAsync() {
        return this.inner().listAsync().mapPage(StorageSkuImpl::new);
    }

    @Override
    public SkusClient inner() {
        return manager.inner().getSkus();
    }
}
