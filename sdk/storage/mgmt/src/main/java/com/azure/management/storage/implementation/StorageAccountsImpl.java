/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.storage.implementation;

import com.azure.management.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;
import com.azure.management.storage.CheckNameAvailabilityResult;
import com.azure.management.storage.ServiceSasParameters;
import com.azure.management.storage.SkuName;
import com.azure.management.storage.StorageAccount;
import com.azure.management.storage.StorageAccountCheckNameAvailabilityParameters;
import com.azure.management.storage.StorageAccounts;
import com.azure.management.storage.models.StorageAccountInner;
import com.azure.management.storage.models.StorageAccountsInner;
import reactor.core.publisher.Mono;

/**
 * The implementation of StorageAccounts and its parent interfaces.
 */
class StorageAccountsImpl
        extends TopLevelModifiableResourcesImpl<
        StorageAccount,
        StorageAccountImpl,
        StorageAccountInner,
        StorageAccountsInner,
        StorageManager>
        implements StorageAccounts {

    StorageAccountsImpl(final StorageManager storageManager) {
        super(storageManager.inner().storageAccounts(), storageManager);
    }

    @Override
    public CheckNameAvailabilityResult checkNameAvailability(String name) {
        return this.checkNameAvailabilityAsync(name).block();
    }

    @Override
    public Mono<CheckNameAvailabilityResult> checkNameAvailabilityAsync(String name) {
        return this.inner().checkNameAvailabilityAsync(name).map(checkNameAvailabilityResultInner -> new CheckNameAvailabilityResult(checkNameAvailabilityResultInner));
    }

    @Override
    public StorageAccountImpl define(String name) {
        return wrapModel(name)
                .withSku(SkuName.STANDARD_GRS)
                .withGeneralPurposeAccountKind();
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
        return new StorageAccountImpl(storageAccountInner.getName(), storageAccountInner, this.manager());
    }

    @Override
    public String createSasToken(String resourceGroupName, String accountName, ServiceSasParameters parameters) {
        return createSasTokenAsync(resourceGroupName, accountName, parameters).block();
    }

    @Override
    public Mono<String> createSasTokenAsync(String resourceGroupName, String accountName, ServiceSasParameters parameters) {
        return this.inner().listServiceSASAsync(resourceGroupName, accountName, parameters).map(listServiceSasResponseInner -> listServiceSasResponseInner.getServiceSasToken());
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
