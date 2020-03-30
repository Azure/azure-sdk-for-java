/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.storage.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.management.storage.Sku;
import com.azure.management.storage.StorageSku;
import com.azure.management.storage.StorageSkus;
import com.azure.management.storage.models.SkusInner;

/**
 * The implementation for {@link StorageSkus}.
 */
class StorageSkusImpl
        implements
        StorageSkus {

    private final StorageManager manager;

    StorageSkusImpl(StorageManager storageManager) {
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
    public SkusInner inner() {
        return this.inner();
    }
}