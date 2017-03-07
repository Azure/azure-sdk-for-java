/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.batch.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.batch.BatchAccount;
import com.microsoft.azure.management.batch.BatchAccounts;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;
import com.microsoft.azure.management.storage.implementation.StorageManager;

/**
 * Implementation for BatchAccounts and its parent interfaces.
 */
@LangDefinition
public class BatchAccountsImpl
        extends TopLevelModifiableResourcesImpl<BatchAccount, BatchAccountImpl, BatchAccountInner, BatchAccountsInner, BatchManager>
        implements BatchAccounts {
    private final StorageManager storageManager;

    protected BatchAccountsImpl(BatchManager manager, StorageManager storageManager) {
        super(manager.inner().batchAccounts(), manager);
        this.storageManager = storageManager;
    }

    @Override
    protected BatchAccountImpl wrapModel(String name) {
        BatchAccountInner inner = new BatchAccountInner();

        return new BatchAccountImpl(name, inner, this.manager(), this.storageManager);
    }

    @Override
    protected BatchAccountImpl wrapModel(BatchAccountInner inner) {
        if (inner == null) {
            return null;
        }
        return new BatchAccountImpl(
                inner.name(),
                inner,
                this.manager(),
                this.storageManager);
    }

    @Override
    public BatchAccount.DefinitionStages.Blank define(String name) {
        return wrapModel(name);
    }

    @Override
    public int getBatchAccountQuotaByLocation(Region region) {
        return this.manager().inner().locations().getQuotas(region.toString()).accountQuota();
    }
}
