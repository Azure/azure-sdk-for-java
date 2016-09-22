package com.microsoft.azure.management.batch.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.batch.AutoStorageBaseProperties;
import com.microsoft.azure.management.batch.AutoStorageProperties;
import com.microsoft.azure.management.batch.BatchAccount;
import com.microsoft.azure.management.batch.BatchAccountKeys;
import com.microsoft.azure.management.batch.ProvisioningState;
import com.microsoft.azure.management.batch.AccountKeyType;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.management.storage.implementation.StorageManager;
import rx.Observable;
import rx.functions.Func1;

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
    private final BatchAccountsInner innerCollection;
    private final StorageManager storageManager;
    private String creatableStorageAccountKey;
    private StorageAccount existingStorageAccountToAssociate;

    private BatchAccountKeys cachedKeys;

    protected BatchAccountImpl(String name, BatchAccountInner innerObject, BatchAccountsInner innerCollection, BatchManager manager, final StorageManager storageManager) {
        super(name, innerObject, manager);
        this.innerCollection = innerCollection;
        this.storageManager = storageManager;
    }

    @Override
    public BatchAccount refresh() {
        BatchAccountInner response =
                this.innerCollection.get(this.resourceGroupName(), this.name());
        this.setInner(response);
        return this;
    }

    @Override
    public Observable<BatchAccount> createResourceAsync() {
        final BatchAccountImpl self = this;

        handleStorageSettings();
        BatchAccountCreateParametersInner batchAccountCreateParametersInner = new BatchAccountCreateParametersInner();
        if (this.inner().autoStorage() != null) {
            batchAccountCreateParametersInner.withAutoStorage(new AutoStorageBaseProperties());
            batchAccountCreateParametersInner.autoStorage().withStorageAccountId(this.inner().autoStorage().storageAccountId());
        }
        else {
            batchAccountCreateParametersInner.withAutoStorage(null);
        }

        batchAccountCreateParametersInner.withLocation(this.inner().location());
        batchAccountCreateParametersInner.withTags(this.inner().getTags());

        return this.innerCollection.createAsync(this.resourceGroupName(), this.name(), batchAccountCreateParametersInner)
                .map(new Func1<BatchAccountInner, BatchAccount>() {
                    @Override
                    public BatchAccount call(BatchAccountInner batchAccountInner) {
                        self.creatableStorageAccountKey = null;
                        self.existingStorageAccountToAssociate = null;
                        setInner(batchAccountInner);

                        return self;
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
        return this.inner().coreQuota();
    }

    @Override
    public int poolQuota() {
        return this.inner().poolQuota();
    }

    @Override
    public int activeJobAndJobScheduleQuota() {
        return this.inner().activeJobAndJobScheduleQuota();
    }

    @Override
    public BatchAccountKeys keys() {
        if (cachedKeys == null) {
            cachedKeys = refreshKeys();
        }

        return cachedKeys;
    }

    @Override
    public BatchAccountKeys refreshKeys() {
        BatchAccountKeysInner keys = this.innerCollection.getKeys(this.resourceGroupName(), this.name());
        cachedKeys = new BatchAccountKeys(keys.primary(), keys.secondary());

        return cachedKeys;
    }

    @Override
    public BatchAccountKeys regenerateKeys(AccountKeyType keyType) {
        BatchAccountKeysInner keys = this.innerCollection.regenerateKey(this.resourceGroupName(), this.name(), keyType);
        cachedKeys = new BatchAccountKeys(keys.primary(), keys.secondary());

        return cachedKeys;
    }

    @Override
    public void synchronizeAutoStorageKeys() {
        this.innerCollection.synchronizeAutoStorageKeys(this.resourceGroupName(), this.name());
    }

    @Override
    public BatchAccountImpl withStorageAccount(StorageAccount storageAccount) {
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
        this.inner().withAutoStorage(null);
        return this;
    }


    private void handleStorageSettings() {
        StorageAccount storageAccount;
        if (this.creatableStorageAccountKey != null) {
            storageAccount = (StorageAccount) this.createdResource(this.creatableStorageAccountKey);
        } else if (this.existingStorageAccountToAssociate != null) {
            storageAccount = this.existingStorageAccountToAssociate;
        } else {
            this.inner().withAutoStorage(null);
            return;
        }

        if (this.inner().autoStorage() == null) {
            this.inner().withAutoStorage(new AutoStorageProperties());
        }

        inner().autoStorage().withStorageAccountId(storageAccount.id());
    }
}
