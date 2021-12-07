// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storage.implementation;

import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;
import com.azure.resourcemanager.storage.StorageManager;
import com.azure.resourcemanager.storage.fluent.BlobServicesClient;
import com.azure.resourcemanager.storage.models.BlobServiceProperties;
import com.azure.resourcemanager.storage.models.BlobServices;
import com.azure.resourcemanager.storage.fluent.models.BlobServicePropertiesInner;
import reactor.core.publisher.Mono;

public class BlobServicesImpl extends WrapperImpl<BlobServicesClient> implements BlobServices {
    private final StorageManager manager;

    public BlobServicesImpl(StorageManager manager) {
        super(manager.serviceClient().getBlobServices());
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
        return innerModel().getServicePropertiesAsync(resourceGroupName, accountName).map(inner -> wrapModel(inner));
    }
}
