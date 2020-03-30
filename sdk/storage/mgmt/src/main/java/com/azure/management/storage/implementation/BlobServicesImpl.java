/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.storage.implementation;


import com.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import com.azure.management.storage.BlobServiceProperties;
import com.azure.management.storage.BlobServices;
import com.azure.management.storage.models.BlobServicePropertiesInner;
import com.azure.management.storage.models.BlobServicesInner;
import reactor.core.publisher.Mono;

class BlobServicesImpl extends WrapperImpl<BlobServicesInner> implements BlobServices {
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
        return inner().getServicePropertiesAsync(resourceGroupName, accountName)
                .map(inner -> wrapModel(inner));
    }
}