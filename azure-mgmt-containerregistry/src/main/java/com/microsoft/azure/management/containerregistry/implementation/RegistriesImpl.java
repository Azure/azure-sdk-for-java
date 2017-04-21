package com.microsoft.azure.management.containerregistry.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.containerregistry.PasswordName;
import com.microsoft.azure.management.containerregistry.RegenerateCredentialParameters;
import com.microsoft.azure.management.containerregistry.Registries;
import com.microsoft.azure.management.containerregistry.Registry;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupPagedList;
import rx.Completable;
import rx.Observable;
import rx.functions.Func1;

import java.util.List;

/**
 * Created by alvab on 4/21/17.
 */
public class RegistriesImpl
        extends
        GroupableResourcesImpl<
                Registry,
                RegistryImpl,
                RegistryInner,
                RegistriesInner,
                ContainerRegistryManager>
        implements Registries {

    protected RegistriesImpl(final ContainerRegistryManager manager) {
        super(manager.inner().registries(), manager);
    }

    @Override
    public PagedList<Registry> list() {
        final RegistriesImpl self = this;
        return new GroupPagedList<Registry>(this.manager().resourceManager().resourceGroups().list()) {
            @Override
            public List<Registry> listNextGroup(String resourceGroupName) {
                return wrapList(self.inner().listByResourceGroup(resourceGroupName));

            }
        };
    }

    @Override
    public Observable<Registry> listAsync() {
        return this.manager().resourceManager().resourceGroups().listAsync()
                .flatMap(new Func1<ResourceGroup, Observable<Registry>>() {
                    @Override
                    public Observable<Registry> call(ResourceGroup resourceGroup) {
                        return wrapPageAsync(inner().listByResourceGroupAsync(resourceGroup.name()));
                    }
                });
    }

    @Override
    public Observable<Registry> listByResourceGroupAsync(String resourceGroupName) {
        return wrapPageAsync(this.inner().listByResourceGroupAsync(resourceGroupName));
    }


    @Override
    public PagedList<Registry> listByResourceGroup(String groupName) {
        return wrapList(this.inner().listByResourceGroup(groupName));
    }

    @Override
    protected Observable<RegistryInner> getInnerAsync(String resourceGroupName, String name) {
        return this.inner().getByResourceGroupAsync(resourceGroupName, name);
    }

    @Override
    public RegistryImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    protected Completable deleteInnerAsync(String groupName, String name) {
        return this.inner().deleteAsync(groupName, name).toCompletable();
    }

    /**************************************************************
     * Fluent model helpers.
     **************************************************************/

    @Override
    protected RegistryImpl wrapModel(String name) {
        return new RegistryImpl(name,
                new RegistryInner(),
                this.manager());
    }

    @Override
    protected RegistryImpl wrapModel(RegistryInner containerServiceInner) {
        if (containerServiceInner == null) {
            return null;
        }

        return new RegistryImpl(containerServiceInner.name(),
                containerServiceInner,
                this.manager());
    }

    @Override
    public RegistryListCredentialsResultInner listCredentials(String resourceGroupName, String registryName) {
        return this.listCredentialsAsync(resourceGroupName, registryName).toBlocking().last();
    }

    @Override
    public Observable<RegistryListCredentialsResultInner> listCredentialsAsync(String resourceGroupName, String registryName) {
        return this.inner().listCredentialsAsync(resourceGroupName, registryName);
    }

    @Override
    public RegistryListCredentialsResultInner regenerateCredential(String resourceGroupName, String registryName, PasswordName passwordName) {
        return this.regenerateCredentialAsync(resourceGroupName, registryName, passwordName).toBlocking().last();
    }

    @Override
    public Observable<RegistryListCredentialsResultInner> regenerateCredentialAsync(String resourceGroupName, String registryName, PasswordName passwordName) {
        return this.inner().regenerateCredentialAsync(resourceGroupName, registryName, passwordName);
    }

}
