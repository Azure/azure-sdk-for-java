/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.containerregistry.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.containerregistry.*;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import org.joda.time.DateTime;
import rx.Observable;
import rx.functions.Func1;

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
    }

    public Sku sku() {
        return this.inner().sku();
    }

    public String loginServer() {
        return this.inner().loginServer();
    }

    public DateTime creationDate() {
        return this.inner().creationDate();
    }

    public boolean adminUserEnabled() {
        return this.inner().adminUserEnabled();
    }

    public StorageAccountProperties storageAccount() {
        return this.inner().storageAccount();
    }

    public RegistryImpl withSku(Sku sku) {
        this.createParameters.withSku(sku);
        return this;
    }

    public RegistryImpl withAdminUserEnabled() {
        if(this.isInCreateMode()) {
            this.createParameters.withAdminUserEnabled(true);
        }else {
            this.updateParameters.withAdminUserEnabled(true);
        }

        return this;
    }

    public RegistryImpl withoutAdminUserEnabled() {
        if(this.isInCreateMode()) {
            this.createParameters.withAdminUserEnabled(false);
        }else {
            this.updateParameters.withAdminUserEnabled(false);
        }

        return this;
    }

    /**
     * The parameters of a storage account for the container registry. If specified, the storage account must be in the same physical location as the container registry.
     *
     * @return the next stage
     */
    public RegistryImpl withStorageAccount(String name, String accessKey) {
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
        RegistryCreateParametersInner inner = new RegistryCreateParametersInner();
        return this.manager().inner().registries().createAsync(resourceGroupName(), name(), this.createParameters)
                .map(new Func1<RegistryInner, Registry>() {
                    @Override
                    public Registry call(RegistryInner containerServiceInner) {
                        self.setInner(containerServiceInner);
                        return self;
                    }
                });
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
