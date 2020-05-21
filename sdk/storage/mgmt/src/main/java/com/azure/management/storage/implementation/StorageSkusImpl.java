// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.management.storage.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.management.storage.StorageManager;
import com.azure.management.storage.models.StorageSku;
import com.azure.management.storage.models.StorageSkus;
import com.azure.management.storage.fluent.SkusClient;

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
        return manager.inner().skus();
    }
}
