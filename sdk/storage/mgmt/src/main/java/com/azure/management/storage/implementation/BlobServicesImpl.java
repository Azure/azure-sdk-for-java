// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.management.storage.implementation;

import com.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import com.azure.management.storage.models.BlobServiceProperties;
import com.azure.management.storage.models.BlobServices;
import com.azure.management.storage.inner.BlobServicePropertiesInner;
import com.azure.management.storage.inner.BlobServicesClient;
import reactor.core.publisher.Mono;

class BlobServicesImpl extends WrapperImpl<BlobServicesClient> implements BlobServices {
    private final StorageManager manager;

    BlobServicesImpl(StorageManager manager) {
        super(manager.inner().blobServices());
        this.manager = manager;
    }

    public StorageManager getManager() {
        return this.manager;
    }

    @Override
    public BlobServicePropertiesImpl define(String name) {
        return wrapModel(name);
    }

    private BlobServicePropertiesImpl wrapModel(BlobServicePropertiesInner inner) {
        return new BlobServicePropertiesImpl(inner, getManager());
    }

    private BlobServicePropertiesImpl wrapModel(String name) {
        return new BlobServicePropertiesImpl(name, this.getManager());
    }

    @Override
    public Mono<BlobServiceProperties> getServicePropertiesAsync(String resourceGroupName, String accountName) {
        return inner().getServicePropertiesAsync(resourceGroupName, accountName).map(inner -> wrapModel(inner));
    }
}
