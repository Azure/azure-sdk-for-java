/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.containerregistry.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.containerregistry.Registry;
import com.microsoft.azure.management.containerregistry.Sku;
import com.microsoft.azure.management.containerregistry.StorageAccountParameters;
import com.microsoft.azure.management.containerregistry.PasswordName;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.management.storage.StorageAccountKey;
import com.microsoft.azure.management.storage.implementation.StorageManager;
import org.joda.time.DateTime;
import rx.Observable;
import rx.functions.Func1;

import java.util.List;

/**
 * Implementation for Registry and its create and update interfaces.
 */
@LangDefinition
public class RegistryImpl
        extends
        GroupableResourceImpl<
                        Registry,
                        RegistryInner,
                        RegistryImpl,
                        ContainerRegistryManager>
        implements Registry,
        Registry.Definition,
        Registry.Update {

    private RegistryCreateParametersInner createParameters;
    private RegistryUpdateParametersInner updateParameters;
    private final StorageManager storageManager;
    private StorageAccount storageAccount;
    private String creatableStorageAccountKey;

    protected RegistryImpl(String name, RegistryInner innerObject, ContainerRegistryManager manager,
                           final StorageManager storageManager) {
        super(name, innerObject, manager);
        this.createParameters = new RegistryCreateParametersInner();
        Sku sku = new Sku();
        sku.withName("Basic");
        this.createParameters.withSku(sku);
        this.storageManager = storageManager;
    }

    @Override
    public Sku sku() {
        return this.inner().sku();
    }

    @Override
    public String loginServerUrl() {
        return this.inner().loginServer();
    }

    @Override
    public DateTime creationDate() {
        return this.inner().creationDate();
    }

    @Override
    public boolean adminUserEnabled() {
        return this.inner().adminUserEnabled();
    }

    @Override
    public String storageAccountName() {
        if (this.inner().storageAccount() == null) {
            return null;
        }

        return this.inner().storageAccount().name();
    }

    @Override
    public RegistryImpl withRegistryNameAsAdminUser() {
        if (this.isInCreateMode()) {
            this.createParameters.withAdminUserEnabled(true);
        } else {
            this.updateParameters.withAdminUserEnabled(true);
        }

        return this;
    }

    @Override
    public RegistryImpl withoutRegistryNameAsAdminUser() {
        if (this.isInCreateMode()) {
            this.createParameters.withAdminUserEnabled(false);
        } else {
            this.updateParameters.withAdminUserEnabled(false);
        }

        return this;
    }

    @Override
    public RegistryImpl withExistingStorageAccount(StorageAccount storageAccount) {
        this.storageAccount = storageAccount;
        return this;
    }

    @Override
    public RegistryImpl withNewStorageAccount(String storageAccountName) {
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
    public RegistryImpl withNewStorageAccount(Creatable<StorageAccount> creatable) {
        if (this.creatableStorageAccountKey == null) {
            this.creatableStorageAccountKey = creatable.key();
            this.addCreatableDependency(creatable);
        }
        return this;
    }

    @Override
    protected Observable<RegistryInner> getInnerAsync() {
        return this.manager().inner().registries().getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    @Override
    public RegistryImpl update() {
        updateParameters = new RegistryUpdateParametersInner();
        return super.update();
    }

    @Override
    public Observable<Registry> createResourceAsync() {
        final RegistryImpl self = this;
        if (isInCreateMode()) {
            return this.handleStorageSettings().flatMap(new Func1<StorageAccountParameters, Observable<? extends Registry>>() {
                @Override
                public Observable<? extends Registry> call(StorageAccountParameters storageAccountParameters) {
                    createParameters.withStorageAccount(storageAccountParameters);
                    createParameters.withLocation(regionName().toLowerCase());
                    createParameters.withTags(inner().getTags());
                    return manager().inner().registries().createAsync(resourceGroupName(), name(), createParameters)
                            .map(new Func1<RegistryInner, Registry>() {
                                @Override
                                public Registry call(RegistryInner containerServiceInner) {
                                    self.setInner(containerServiceInner);
                                    return self;
                                }
                            });
                }
            });
        } else {
            updateParameters.withTags(inner().getTags());
            return manager().inner().registries().updateAsync(resourceGroupName(), name(), updateParameters)
                    .map(new Func1<RegistryInner, Registry>() {
                        @Override
                        public Registry call(RegistryInner containerServiceInner) {
                            self.setInner(containerServiceInner);
                            return self;
                        }
                    });
        }
    }

    @Override
    public RegistryListCredentials regenerateCredential(PasswordName passwordName) {
        return this.regenerateCredentialAsync(passwordName).toBlocking().last();

    }

    @Override
    public Observable<RegistryListCredentials> regenerateCredentialAsync(PasswordName passwordName) {
        return this.manager().inner().registries().regenerateCredentialAsync(this.resourceGroupName(),
                this.name(), passwordName);
    }

    @Override
    public RegistryListCredentials listCredentials() {
        return this.listCredentialsAsync().toBlocking().last();
    }

    @Override
    public Observable<RegistryListCredentials> listCredentialsAsync() {
        return this.manager().inner().registries().listCredentialsAsync(this.resourceGroupName(),
                this.name());
    }

    private Observable<StorageAccountParameters> handleStorageSettings() {
        final Func1<StorageAccount, StorageAccountParameters> onStorageAccountReady = new Func1<StorageAccount, StorageAccountParameters>() {
            @Override
            public StorageAccountParameters call(StorageAccount storageAccount) {
                RegistryImpl.this.storageAccount = storageAccount;
                List<StorageAccountKey> keys = storageAccount.getKeys();
                StorageAccountParameters storageAccountParameters =
                        new StorageAccountParameters();
                storageAccountParameters.withName(storageAccount.name());
                storageAccountParameters.withAccessKey(keys.get(0).value());
                return storageAccountParameters;
            }
        };

        if (this.creatableStorageAccountKey != null) {
            return Observable.just((StorageAccount) this.createdResource(this.creatableStorageAccountKey))
                    .map(onStorageAccountReady);
        } else {
            return Observable.just(this.storageAccount)
                    .map(onStorageAccountReady);
        }
    }
}
