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
import com.microsoft.azure.management.containerregistry.StorageAccountProperties;
import com.microsoft.azure.management.containerregistry.PasswordName;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import org.joda.time.DateTime;
import rx.Observable;
import rx.functions.Func1;

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

    protected RegistryImpl(String name, RegistryInner innerObject, ContainerRegistryManager manager) {
        super(name, innerObject, manager);
        this.createParameters = new RegistryCreateParametersInner();
        Sku sku = new Sku();
        sku.withName("Basic");
        this.createParameters.withSku(sku);
    }

    @Override
    public Sku sku() {
        return this.inner().sku();
    }

    @Override
    public String loginServer() {
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
    public StorageAccountProperties storageAccount() {
        return this.inner().storageAccount();
    }

    @Override
    public RegistryImpl withAdminUserEnabled() {
        if (this.isInCreateMode()) {
            this.createParameters.withAdminUserEnabled(true);
        } else {
            this.updateParameters.withAdminUserEnabled(true);
        }

        return this;
    }

    @Override
    public RegistryImpl withoutAdminUserEnabled() {
        if (this.isInCreateMode()) {
            this.createParameters.withAdminUserEnabled(false);
        } else {
            this.updateParameters.withAdminUserEnabled(false);
        }

        return this;
    }

    @Override
    public RegistryImpl withExistingStorageAccount(String name, String accessKey) {
        StorageAccountParameters storageAccountParameters = new StorageAccountParameters();
        storageAccountParameters.withName(name);
        storageAccountParameters.withAccessKey(accessKey);
        this.createParameters.withStorageAccount(storageAccountParameters);
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
        if (this.isInCreateMode()) {
            createParameters.withLocation(this.regionName().toLowerCase());
            createParameters.withTags(this.inner().getTags());
            return this.manager().inner().registries().createAsync(resourceGroupName(), name(), this.createParameters)
                    .map(new Func1<RegistryInner, Registry>() {
                        @Override
                        public Registry call(RegistryInner containerServiceInner) {
                            self.setInner(containerServiceInner);
                            return self;
                        }
                    });
        } else {
            this.updateParameters.withTags(this.inner().getTags());
            return this.manager().inner().registries().updateAsync(resourceGroupName(), name(), this.updateParameters)
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
    public RegistryListCredentialsResultInner regenerateCredential(PasswordName passwordName) {
        return this.regenerateCredentialAsync(passwordName).toBlocking().last();

    }

    @Override
    public Observable<RegistryListCredentialsResultInner> regenerateCredentialAsync(PasswordName passwordName) {
        return this.manager().inner().registries().regenerateCredentialAsync(this.resourceGroupName(),
                this.name(), passwordName);
    }

    @Override
    public RegistryListCredentialsResultInner listCredentials() {
        return this.listCredentialsAsync().toBlocking().last();
    }

    @Override
    public Observable<RegistryListCredentialsResultInner> listCredentialsAsync() {
        return this.manager().inner().registries().listCredentialsAsync(this.resourceGroupName(),
                this.name());
    }
}
