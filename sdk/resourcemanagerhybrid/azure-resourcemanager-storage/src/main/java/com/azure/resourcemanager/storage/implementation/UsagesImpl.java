// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.storage.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.storage.StorageManager;
import com.azure.resourcemanager.storage.fluent.UsagesClient;
import com.azure.resourcemanager.storage.models.Usages;
import com.azure.resourcemanager.storage.fluent.models.UsageInner;

/** The implementation of {@link Usages}. */
public class UsagesImpl implements Usages {

    private final StorageManager manager;

    public UsagesImpl(StorageManager storageManager) {
        this.manager = storageManager;
    }

    @Override
    public StorageManager manager() {
        return this.manager;
    }

    @Override
    public PagedIterable<UsageInner> list() {
        return null;
    }

    @Override
    public PagedFlux<UsageInner> listAsync() {
        return null;
    }

    public UsagesClient inner() {
        return this.manager().serviceClient().getUsages();
    }
}
