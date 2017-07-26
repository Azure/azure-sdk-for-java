/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.storage.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.storage.AccessTier;
import com.microsoft.azure.management.storage.CustomDomain;
import com.microsoft.azure.management.storage.Encryption;
import com.microsoft.azure.management.storage.EncryptionService;
import com.microsoft.azure.management.storage.EncryptionServices;
import com.microsoft.azure.management.storage.StorageAccountEncryptionKeySource;
import com.microsoft.azure.management.storage.StorageAccountEncryptionStatus;
import com.microsoft.azure.management.storage.Kind;
import com.microsoft.azure.management.storage.ProvisioningState;
import com.microsoft.azure.management.storage.PublicEndpoints;
import com.microsoft.azure.management.storage.Sku;
import com.microsoft.azure.management.storage.SkuName;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.management.storage.StorageAccountKey;
import com.microsoft.azure.management.storage.StorageService;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import org.joda.time.DateTime;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation for StorageAccount and its parent interfaces.
 */
@LangDefinition
class StorageAccountImpl
        extends GroupableResourceImpl<
            StorageAccount,
            StorageAccountInner,
            StorageAccountImpl,
            StorageManager>
        implements
        StorageAccount,
        StorageAccount.Definition,
        StorageAccount.Update {

    private PublicEndpoints publicEndpoints;
    private AccountStatuses accountStatuses;
    private StorageAccountCreateParametersInner createParameters;
    private StorageAccountUpdateParametersInner updateParameters;

    StorageAccountImpl(String name,
                              StorageAccountInner innerModel,
                              final StorageManager storageManager) {
        super(name, innerModel, storageManager);
        this.createParameters = new StorageAccountCreateParametersInner();
    }

    @Override
    public AccountStatuses accountStatuses() {
        if (accountStatuses == null) {
            accountStatuses = new AccountStatuses(this.inner().statusOfPrimary(), this.inner().statusOfSecondary());
        }
        return accountStatuses;
    }

    @Override
    public Sku sku() {
        return this.inner().sku();
    }

    @Override
    public Kind kind() {
        return inner().kind();
    }

    @Override
    public DateTime creationTime() {
        return this.inner().creationTime();
    }

    @Override
    public CustomDomain customDomain() {
        return this.inner().customDomain();
    }

    @Override
    public DateTime lastGeoFailoverTime() {
        return this.inner().lastGeoFailoverTime();
    }

    @Override
    public ProvisioningState provisioningState() {
        return this.inner().provisioningState();
    }

    @Override
    public PublicEndpoints endPoints() {
        if (publicEndpoints == null) {
            publicEndpoints = new PublicEndpoints(this.inner().primaryEndpoints(), this.inner().secondaryEndpoints());
        }
        return publicEndpoints;
    }

    @Override
    public Encryption encryption() {
        return inner().encryption();
    }

    @Override
    public StorageAccountEncryptionKeySource encryptionKeySource() {
        if (this.inner().encryption() == null
                || this.inner().encryption().keySource() == null) {
            return null;
        }
        return StorageAccountEncryptionKeySource.fromString(this.inner().encryption().keySource());
    }

    @Override
    public Map<StorageService, StorageAccountEncryptionStatus> encryptionStatuses() {
        HashMap<StorageService, StorageAccountEncryptionStatus> statuses = new HashMap<>();
        if (this.inner().encryption() != null
                && this.inner().encryption().services() != null) {
            // Status of blob service
            //
            // Status for other service needs to be added as storage starts supporting it
            statuses.put(StorageService.BLOB, new BlobServiceEncryptionStatusImpl(this.inner().encryption().services()));
        } else {
            statuses.put(StorageService.BLOB, new BlobServiceEncryptionStatusImpl(new EncryptionServices()));
        }
        return statuses;
    }

    @Override
    public AccessTier accessTier() {
        return inner().accessTier();
    }

    @Override
    public List<StorageAccountKey> getKeys() {
        return this.getKeysAsync().toBlocking().last();
    }

    @Override
    public Observable<List<StorageAccountKey>> getKeysAsync() {
        return this.manager().inner().storageAccounts().listKeysAsync(
                this.resourceGroupName(), this.name()).map(new Func1<StorageAccountListKeysResultInner, List<StorageAccountKey>>() {
            @Override
            public List<StorageAccountKey> call(StorageAccountListKeysResultInner storageAccountListKeysResultInner) {
                return storageAccountListKeysResultInner.keys();
            }
        });
    }

    @Override
    public ServiceFuture<List<StorageAccountKey>> getKeysAsync(ServiceCallback<List<StorageAccountKey>> callback) {
        return ServiceFuture.fromBody(this.getKeysAsync(), callback);
    }

    @Override
    public List<StorageAccountKey> regenerateKey(String keyName) {
        return this.regenerateKeyAsync(keyName).toBlocking().last();
    }

    @Override
    public Observable<List<StorageAccountKey>> regenerateKeyAsync(String keyName) {
        return this.manager().inner().storageAccounts().regenerateKeyAsync(
                this.resourceGroupName(), this.name(), keyName).map(new Func1<StorageAccountListKeysResultInner, List<StorageAccountKey>>() {
            @Override
            public List<StorageAccountKey> call(StorageAccountListKeysResultInner storageAccountListKeysResultInner) {
                return storageAccountListKeysResultInner.keys();
            }
        });
    }

    @Override
    public ServiceFuture<List<StorageAccountKey>> regenerateKeyAsync(String keyName, ServiceCallback<List<StorageAccountKey>> callback) {
        return ServiceFuture.fromBody(this.regenerateKeyAsync(keyName), callback);
    }

    @Override
    public Observable<StorageAccount> refreshAsync() {
        return super.refreshAsync().map(new Func1<StorageAccount, StorageAccount>() {
            @Override
            public StorageAccount call(StorageAccount storageAccount) {
                StorageAccountImpl impl = (StorageAccountImpl) storageAccount;
                impl.clearWrapperProperties();
                return impl;
            }
        });
    }

    @Override
    protected Observable<StorageAccountInner> getInnerAsync() {
        return this.manager().inner().storageAccounts().getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    @Override
    public StorageAccountImpl withSku(SkuName skuName) {
        if (isInCreateMode()) {
            createParameters.withSku(new Sku().withName(skuName));
        } else {
            updateParameters.withSku(new Sku().withName(skuName));
        }
        return this;
    }

    @Override
    public StorageAccountImpl withBlobStorageAccountKind() {
        createParameters.withKind(Kind.BLOB_STORAGE);
        return this;
    }

    @Override
    public StorageAccountImpl withGeneralPurposeAccountKind() {
        createParameters.withKind(Kind.STORAGE);
        return this;
    }

    @Override
    public StorageAccountImpl withEncryption(Encryption encryption) {
        if (isInCreateMode()) {
            createParameters.withEncryption(encryption);
        } else {
            updateParameters.withEncryption(encryption);
        }
        return this;
    }

    @Override
    public StorageAccountImpl withEncryption() {
        Encryption encryption;
        if (this.inner().encryption() != null) {
            encryption = this.inner().encryption();
        } else {
            encryption = new Encryption();
        }
        if (encryption.services() == null) {
            encryption.withServices(new EncryptionServices());
        }
        if (encryption.keySource() == null) {
            encryption.withKeySource("Microsoft.Storage");
        }
        // Enable encryption for blob service
        //
        if (encryption.services().blob() == null) {
            encryption.services().withBlob(new EncryptionService());
        }
        encryption.services().blob().withEnabled(true);
        // Code for enabling encryption for other service will be added as storage start supporting them.
        //
        if (isInCreateMode()) {
            createParameters.withEncryption(encryption);
        } else {
            updateParameters.withEncryption(encryption);
        }
        return this;
    }

    @Override
    public StorageAccountImpl withoutEncryption() {
        if (this.inner().encryption() == null
                || this.inner().encryption().services() == null) {
            return this;
        }
        Encryption encryption = this.inner().encryption();
        // Disable encryption for blob service
        //
        if (encryption.services().blob() == null) {
            return this;
        }
        encryption.services().blob().withEnabled(false);
        // Code for disabling encryption for other service will be added as storage start supporting them.
        //
        updateParameters.withEncryption(encryption);
        return this;
    }

    private void clearWrapperProperties() {
        accountStatuses = null;
        publicEndpoints = null;
    }

    @Override
    public StorageAccountImpl update() {
        updateParameters = new StorageAccountUpdateParametersInner();
        return super.update();
    }

    @Override
    public Observable<StorageAccount> updateResourceAsync() {
        updateParameters.withTags(this.inner().getTags());
        return this.manager().inner().storageAccounts().updateAsync(
                resourceGroupName(), name(), updateParameters)
                .map(innerToFluentMap(this));
    }

    @Override
    public StorageAccountImpl withCustomDomain(CustomDomain customDomain) {
        if (isInCreateMode()) {
            createParameters.withCustomDomain(customDomain);
        } else {
            updateParameters.withCustomDomain(customDomain);
        }
        return this;
    }

    @Override
    public StorageAccountImpl withCustomDomain(String name) {
        return withCustomDomain(new CustomDomain().withName(name));
    }

    @Override
    public StorageAccountImpl withCustomDomain(String name, boolean useSubDomain) {
        return withCustomDomain(new CustomDomain().withName(name).withUseSubDomain(useSubDomain));
    }

    @Override
    public StorageAccountImpl withAccessTier(AccessTier accessTier) {
        if (isInCreateMode()) {
            createParameters.withAccessTier(accessTier);
        } else {
            if (this.inner().kind() != Kind.BLOB_STORAGE) {
                throw new UnsupportedOperationException("Access tier can not be changed for general purpose storage accounts.");
            }
            updateParameters.withAccessTier(accessTier);
        }
        return this;
    }

    // CreateUpdateTaskGroup.ResourceCreator implementation
    @Override
    public Observable<StorageAccount> createResourceAsync() {
        createParameters.withLocation(this.regionName());
        createParameters.withTags(this.inner().getTags());
        final StorageAccountsInner client = this.manager().inner().storageAccounts();
        return this.manager().inner().storageAccounts().createAsync(
                this.resourceGroupName(), this.name(), createParameters)
                .flatMap(new Func1<StorageAccountInner, Observable<StorageAccountInner>>() {
                    @Override
                    public Observable<StorageAccountInner> call(StorageAccountInner storageAccountInner) {
                        return client.getByResourceGroupAsync(resourceGroupName(), name());
                    }
                })
                .map(innerToFluentMap(this))
                .doOnNext(new Action1<StorageAccount>() {
                    @Override
                    public void call(StorageAccount storageAccount) {
                        clearWrapperProperties();
                    }
                });
    }
}
