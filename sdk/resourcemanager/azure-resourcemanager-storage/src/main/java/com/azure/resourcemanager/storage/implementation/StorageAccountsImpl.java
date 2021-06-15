// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storage.implementation;

import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;
import com.azure.resourcemanager.storage.StorageManager;
import com.azure.resourcemanager.storage.fluent.StorageAccountsClient;
import com.azure.resourcemanager.storage.fluent.models.ListServiceSasResponseInner;
import com.azure.resourcemanager.storage.models.CheckNameAvailabilityResult;
import com.azure.resourcemanager.storage.models.MinimumTlsVersion;
import com.azure.resourcemanager.storage.models.ServiceSasParameters;
import com.azure.resourcemanager.storage.models.StorageAccount;
import com.azure.resourcemanager.storage.models.StorageAccountSkuType;
import com.azure.resourcemanager.storage.models.StorageAccounts;
import com.azure.resourcemanager.storage.fluent.models.StorageAccountInner;
import reactor.core.publisher.Mono;

/** The implementation of StorageAccounts and its parent interfaces. */
public class StorageAccountsImpl
    extends TopLevelModifiableResourcesImpl<
        StorageAccount, StorageAccountImpl, StorageAccountInner, StorageAccountsClient, StorageManager>
    implements StorageAccounts {

    public StorageAccountsImpl(final StorageManager storageManager) {
        super(storageManager.serviceClient().getStorageAccounts(), storageManager);
    }

    @Override
    public CheckNameAvailabilityResult checkNameAvailability(String name) {
        return this.checkNameAvailabilityAsync(name).block();
    }

    @Override
    public Mono<CheckNameAvailabilityResult> checkNameAvailabilityAsync(String name) {
        return this
            .inner()
            .checkNameAvailabilityAsync(name)
            .map(CheckNameAvailabilityResult::new);
    }

    @Override
    public StorageAccountImpl define(String name) {
        return wrapModel(name)
            .withSku(StorageAccountSkuType.STANDARD_RAGRS)
            .withGeneralPurposeAccountKindV2()
            .withOnlyHttpsTraffic()
            .withMinimumTlsVersion(MinimumTlsVersion.TLS1_2)
            .withBlobEncryption()
            .withFileEncryption();
    }

    @Override
    protected StorageAccountImpl wrapModel(String name) {
        return new StorageAccountImpl(name, new StorageAccountInner(), this.manager());
    }

    @Override
    protected StorageAccountImpl wrapModel(StorageAccountInner storageAccountInner) {
        if (storageAccountInner == null) {
            return null;
        }
        return new StorageAccountImpl(storageAccountInner.name(), storageAccountInner, this.manager());
    }

    @Override
    public String createSasToken(String resourceGroupName, String accountName, ServiceSasParameters parameters) {
        return createSasTokenAsync(resourceGroupName, accountName, parameters).block();
    }

    @Override
    public Mono<String> createSasTokenAsync(
        String resourceGroupName, String accountName, ServiceSasParameters parameters) {
        return this
            .inner()
            .listServiceSasAsync(resourceGroupName, accountName, parameters)
            .map(ListServiceSasResponseInner::serviceSasToken);
    }

    @Override
    public void failover(String resourceGroupName, String accountName) {
        failoverAsync(resourceGroupName, accountName).block();
    }

    @Override
    public Mono<Void> failoverAsync(String resourceGroupName, String accountName) {
        return this.inner().failoverAsync(resourceGroupName, accountName);
    }
}
