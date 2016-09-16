package com.microsoft.azure.management.batch.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.batch.BatchAccount;
import com.microsoft.azure.management.batch.BatchAccounts;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import com.microsoft.azure.management.storage.implementation.StorageManager;

/**
 * Implementation for BatchAccounts and its parent interfaces.
 */
public class BatchAccountsImpl
        extends GroupableResourcesImpl<BatchAccount, BatchAccountImpl, AccountResourceInner, AccountsInner, BatchManager>
        implements BatchAccounts {
    private final StorageManager storageManager;

    protected BatchAccountsImpl(AccountsInner innerCollection, BatchManager manager, StorageManager storageManager) {
        super(innerCollection, manager);
        this.storageManager = storageManager;
    }

    @Override
    public void delete(String id) {
        delete(ResourceUtils.groupFromResourceId(id), ResourceUtils.nameFromResourceId(id));
    }

    @Override
    public void delete(String groupName, String name) {
        this.innerCollection.delete(groupName, name);
    }

    @Override
    protected BatchAccountImpl wrapModel(String name) {
        AccountResourceInner inner = new AccountResourceInner();

        return new BatchAccountImpl(
                name,
                inner,
                this.innerCollection,
                super.myManager, this.storageManager);
    }

    @Override
    public PagedList<BatchAccount> list() {
        return wrapList(this.innerCollection.list());
    }

    @Override
    public PagedList<BatchAccount> listByGroup(String resourceGroupName) {
        return wrapList(this.innerCollection.listByResourceGroup(resourceGroupName));
    }

    @Override
    protected BatchAccountImpl wrapModel(AccountResourceInner inner) {
        return new BatchAccountImpl(
                inner.name(),
                inner,
                this.innerCollection,
                this.myManager,
                this.storageManager);
    }

    @Override
    public BatchAccount.DefinitionStages.Blank define(String name) {
        return wrapModel(name);
    }

    @Override
    public BatchAccount getByGroup(String groupName, String name) {
        return wrapModel(this.innerCollection.get(groupName, name));
    }
}
