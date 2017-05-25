/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.batch.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.batch.AccountKeyType;
import com.microsoft.azure.management.batch.Application;
import com.microsoft.azure.management.batch.AutoStorageBaseProperties;
import com.microsoft.azure.management.batch.AutoStorageProperties;
import com.microsoft.azure.management.batch.BatchAccount;
import com.microsoft.azure.management.batch.BatchAccountKeys;
import com.microsoft.azure.management.batch.ProvisioningState;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.management.storage.implementation.StorageManager;
import rx.Observable;
import rx.functions.Func1;

import java.util.List;
import java.util.Map;

/**
 * Implementation for BatchAccount and its parent interfaces.
 */
@LangDefinition
public class BatchAccountImpl
        extends
            GroupableResourceImpl<
                    BatchAccount,
                    BatchAccountInner,
                    BatchAccountImpl,
                    BatchManager>
        implements
            BatchAccount,
            BatchAccount.Definition,
            BatchAccount.Update {
    private final StorageManager storageManager;
    private String creatableStorageAccountKey;
    private StorageAccount existingStorageAccountToAssociate;
    private ApplicationsImpl applicationsImpl;
    private AutoStorageProperties autoStorage;

    protected BatchAccountImpl(String name,
                               BatchAccountInner innerObject,
                               BatchManager manager,
                               final StorageManager storageManager) {
        super(name, innerObject, manager);
        this.storageManager = storageManager;
        this.applicationsImpl = new ApplicationsImpl(this);
    }

    @Override
    public Observable<BatchAccount> refreshAsync() {
        return super.refreshAsync().map(new Func1<BatchAccount, BatchAccount>() {
            @Override
            public BatchAccount call(BatchAccount batchAccount) {
                BatchAccountImpl impl = (BatchAccountImpl) batchAccount;
                impl.applicationsImpl.refresh();
                return impl;
            }
        });
    }

    @Override
    protected Observable<BatchAccountInner> getInnerAsync() {
        return this.manager().inner().batchAccounts().getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    @Override
    public Observable<BatchAccount> createResourceAsync() {
        final BatchAccountImpl self = this;

        handleStorageSettings();
        BatchAccountCreateParametersInner batchAccountCreateParametersInner = new BatchAccountCreateParametersInner();
        if (autoStorage != null) {
            batchAccountCreateParametersInner.withAutoStorage(new AutoStorageBaseProperties());
            batchAccountCreateParametersInner.autoStorage().withStorageAccountId(autoStorage.storageAccountId());
        }
        else {
            batchAccountCreateParametersInner.withAutoStorage(null);
        }

        batchAccountCreateParametersInner.withLocation(this.inner().location());
        batchAccountCreateParametersInner.withTags(this.inner().getTags());

        return this.manager().inner().batchAccounts().createAsync(this.resourceGroupName(), this.name(), batchAccountCreateParametersInner)
                .map(new Func1<BatchAccountInner, BatchAccount>() {
                    @Override
                    public BatchAccount call(BatchAccountInner batchAccountInner) {
                        self.creatableStorageAccountKey = null;
                        setInner(batchAccountInner);

                        return self;
                    }
                }).flatMap(new Func1<BatchAccount, Observable<? extends BatchAccount>>() {
                    @Override
                    public Observable<? extends BatchAccount> call(BatchAccount batchAccount) {
                        return self.applicationsImpl.commitAndGetAllAsync()
                                .map(new Func1<List<ApplicationImpl>, BatchAccount>() {
                                    @Override
                                    public BatchAccount call(List<ApplicationImpl> applications) {
                                        return self;
                                    }
                                });
                    }
                });
    }

    @Override
    public Observable<BatchAccount> updateResourceAsync() {
        // TODO - ans - remove call to createResourceAsync and uncomment code below, after PATCH start sending the nulls.
        return createResourceAsync();
    /*
        final  BatchAccountImpl self = this;
        handleStorageSettings();
                BatchAccountUpdateParametersInner batchAccountUpdateParametersInner = new BatchAccountUpdateParametersInner();
        if (self.inner().autoStorage() != null) {
            batchAccountUpdateParametersInner.withAutoStorage(new AutoStorageBaseProperties());
            batchAccountUpdateParametersInner.autoStorage().withStorageAccountId(self.inner().autoStorage().storageAccountId());
        } else {
            batchAccountUpdateParametersInner.withAutoStorage(null);
        }

        batchAccountUpdateParametersInner.withTags(self.inner().getTags());

        return self.innerCollection.updateAsync(self.resourceGroupName(), self.name(), batchAccountUpdateParametersInner)
                .map(new Func1<ServiceResponse<BatchAccountInner>, batchAccount>() {
                    @Override
                    public BatchAccount call(ServiceResponse<BatchAccountInner> batchAccount) {
                        setInner(BatchAccountInner.getBody());
                        return self;
                    }
                });
    */
    }

    @Override
    public ProvisioningState provisioningState() {
        return this.inner().provisioningState();
    }

    @Override
    public String accountEndpoint() {
        return this.inner().accountEndpoint();
    }

    @Override
    public AutoStorageProperties autoStorage() {
        return this.inner().autoStorage();
    }

    @Override
    public int coreQuota() {
        return Utils.toPrimitiveInt(this.inner().dedicatedCoreQuota());
    }

    @Override
    public int poolQuota() {
        return Utils.toPrimitiveInt(this.inner().poolQuota());
    }

    @Override
    public int activeJobAndJobScheduleQuota() {
        return Utils.toPrimitiveInt(this.inner().activeJobAndJobScheduleQuota());
    }

    @Override
    public BatchAccountKeys getKeys() {
        BatchAccountKeysInner keys = this.manager().inner().batchAccounts().getKeys(
                this.resourceGroupName(), this.name());

        return new BatchAccountKeys(keys.primary(), keys.secondary());
    }

    @Override
    public BatchAccountKeys regenerateKeys(AccountKeyType keyType) {
        BatchAccountKeysInner keys = this.manager().inner().batchAccounts().regenerateKey(
                this.resourceGroupName(), this.name(), keyType);
        return new BatchAccountKeys(keys.primary(), keys.secondary());
    }

    @Override
    public void synchronizeAutoStorageKeys() {
        this.manager().inner().batchAccounts().synchronizeAutoStorageKeys(this.resourceGroupName(), this.name());
    }

    @Override
    public Map<String, Application> applications() {
        return this.applicationsImpl.asMap();
    }

    @Override
    public BatchAccountImpl withExistingStorageAccount(StorageAccount storageAccount) {
        this.existingStorageAccountToAssociate = storageAccount;
        this.creatableStorageAccountKey = null;
        return this;
    }

    @Override
    public BatchAccountImpl withNewStorageAccount(Creatable<StorageAccount> creatable) {
        // This method's effect is NOT additive.
        if (this.creatableStorageAccountKey == null) {
            this.creatableStorageAccountKey = creatable.key();
            this.addCreatableDependency(creatable);
        }
        this.existingStorageAccountToAssociate = null;
        return this;
    }

    @Override
    public BatchAccountImpl withNewStorageAccount(String storageAccountName) {
        StorageAccount.DefinitionStages.WithGroup definitionWithGroup = this.storageManager
                .storageAccounts()
                .define(storageAccountName)
                .withRegion(this.regionName());
        Creatable<StorageAccount> definitionAfterGroup;
        if (this.creatableGroup != null) {
            definitionAfterGroup = definitionWithGroup.withNewResourceGroup(this.creatableGroup);
        } else {
            definitionAfterGroup = definitionWithGroup.withExistingResourceGroup(this.resourceGroupName());
        }
        return withNewStorageAccount(definitionAfterGroup);
    }

    @Override
    public BatchAccountImpl withoutStorageAccount() {
        this.existingStorageAccountToAssociate = null;
        this.creatableStorageAccountKey = null;
        this.autoStorage = null;
        return this;
    }

    @Override
    public ApplicationImpl defineNewApplication(String applicationId) {
        return this.applicationsImpl.define(applicationId);
    }

    @Override
    public ApplicationImpl updateApplication(String applicationId) {
        return this.applicationsImpl.update(applicationId);
    }

    @Override
    public Update withoutApplication(String applicationId) {
        this.applicationsImpl.remove(applicationId);
        return this;
    }

    private void handleStorageSettings() {
        StorageAccount storageAccount;
        if (this.creatableStorageAccountKey != null) {
            storageAccount = (StorageAccount) this.createdResource(this.creatableStorageAccountKey);
            existingStorageAccountToAssociate = storageAccount;
        } else if (this.existingStorageAccountToAssociate != null) {
            storageAccount = this.existingStorageAccountToAssociate;
        } else {
            return;
        }

        if (autoStorage == null) {
            autoStorage = new AutoStorageProperties();
        }

        autoStorage.withStorageAccountId(storageAccount.id());
    }

    BatchAccountImpl withApplication(ApplicationImpl application) {
        this.applicationsImpl.addApplication(application);
        return this;
    }
}
