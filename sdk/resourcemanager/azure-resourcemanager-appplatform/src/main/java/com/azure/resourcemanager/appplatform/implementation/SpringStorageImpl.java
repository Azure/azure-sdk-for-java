// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appplatform.implementation;

import com.azure.resourcemanager.appplatform.AppPlatformManager;
import com.azure.resourcemanager.appplatform.fluent.models.StorageResourceInner;
import com.azure.resourcemanager.appplatform.models.SpringService;
import com.azure.resourcemanager.appplatform.models.SpringStorage;
import com.azure.resourcemanager.appplatform.models.StorageAccount;
import com.azure.resourcemanager.appplatform.models.StorageProperties;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import reactor.core.publisher.Mono;

public class SpringStorageImpl
    extends ExternalChildResourceImpl<SpringStorage, StorageResourceInner, SpringServiceImpl, SpringService>
    implements SpringStorage, SpringStorage.Definition<SpringStorageImpl>, SpringStorage.Update {
    protected SpringStorageImpl(String name, SpringServiceImpl parent, StorageResourceInner innerObject) {
        super(name, parent, innerObject);
    }

    @Override
    public String storageAccountName() {
        StorageAccount account = getStorageAccountProperties();
        if (account != null) {
            return account.accountName();
        }
        return null;
    }

    @Override
    public SpringStorageImpl withExistingStorageAccount(String accountName) {
        StorageProperties properties = innerModel().properties();
        if (properties != null && !(properties instanceof StorageAccount)) {
            return this;
        }
        if (properties == null) {
            properties = new StorageAccount();
            innerModel().withProperties(properties);
        }
        ((StorageAccount) properties).withAccountName(accountName);
        return this;
    }

    @Override
    public SpringStorageImpl withAccountKey(String accountKey) {
        StorageProperties properties = innerModel().properties();
        if (properties != null && !(properties instanceof StorageAccount)) {
            return this;
        }
        if (properties == null) {
            properties = new StorageAccount();
            innerModel().withProperties(properties);
        }
        ((StorageAccount) properties).withAccountKey(accountKey);
        return this;
    }

    @Override
    public String id() {
        return innerModel().id();
    }

    @Override
    public Mono<SpringStorage> createResourceAsync() {
        return manager().serviceClient()
            .getStorages()
            .createOrUpdateAsync(parent().resourceGroupName(), parent().name(), name(), innerModel())
            .map(inner -> {
                this.setInner(inner);
                return this;
            });
    }

    @Override
    public Mono<SpringStorage> updateResourceAsync() {
        return createResourceAsync();
    }

    @Override
    public Mono<Void> deleteResourceAsync() {
        return manager().serviceClient()
            .getStorages()
            .deleteAsync(parent().resourceGroupName(), parent().name(), name());
    }

    @Override
    protected Mono<StorageResourceInner> getInnerAsync() {
        return manager().serviceClient()
            .getStorages()
            .getAsync(parent().resourceGroupName(), parent().name(), name());
    }

    @Override
    public SpringStorageImpl update() {
        prepareUpdate();
        return this;
    }

    @Override
    public AppPlatformManager manager() {
        return parent().manager();
    }

    private StorageAccount getStorageAccountProperties() {
        StorageProperties storageProperties = innerModel().properties();
        if (storageProperties instanceof StorageAccount) {
            return (StorageAccount) storageProperties;
        }
        return null;
    }
}
