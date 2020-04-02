/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.containerregistry.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.management.containerregistry.AccessKeyType;
import com.azure.management.containerregistry.CheckNameAvailabilityResult;
import com.azure.management.containerregistry.PasswordName;
import com.azure.management.containerregistry.Registries;
import com.azure.management.containerregistry.Registry;
import com.azure.management.containerregistry.RegistryCredentials;
import com.azure.management.containerregistry.RegistryUsage;
import com.azure.management.containerregistry.SourceUploadDefinition;
import com.azure.management.containerregistry.models.RegistriesInner;
import com.azure.management.containerregistry.models.RegistryInner;
import com.azure.management.containerregistry.models.RegistryUsageListResultInner;
import com.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import com.azure.management.resources.fluentcore.utils.PagedConverter;
import com.azure.management.storage.implementation.StorageManager;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Implementation for Registries.
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
    private final StorageManager storageManager;
    protected RegistriesImpl(final ContainerRegistryManager manager,
                             final StorageManager storageManager) {
        super(manager.inner().registries(), manager);
        this.storageManager = storageManager;
    }

    @Override
    public PagedIterable<Registry> list() {
        return new PagedIterable<>(this.listAsync());
    }

    @Override
    public PagedFlux<Registry> listAsync() {
        return this.inner().listAsync()
                .mapPage(inner -> new RegistryImpl(inner.getName(), inner, this.manager(), this.storageManager));
    }

    @Override
    public PagedFlux<Registry> listByResourceGroupAsync(String resourceGroupName) {
        return wrapPageAsync(this.inner().listByResourceGroupAsync(resourceGroupName));
    }


    @Override
    public PagedIterable<Registry> listByResourceGroup(String groupName) {
        return wrapList(this.inner().listByResourceGroup(groupName));
    }

    @Override
    protected Mono<RegistryInner> getInnerAsync(String resourceGroupName, String name) {
        return this.inner().getByResourceGroupAsync(resourceGroupName, name);
    }

    @Override
    public RegistryImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    protected Mono<Void> deleteInnerAsync(String groupName, String name) {
        return this.inner().deleteAsync(groupName, name);
    }

    /**************************************************************
     * Fluent model helpers.
     **************************************************************/

    @Override
    protected RegistryImpl wrapModel(String name) {
        return new RegistryImpl(name,
                new RegistryInner(),
                this.manager(),
                this.storageManager);
    }

    @Override
    protected RegistryImpl wrapModel(RegistryInner containerServiceInner) {
        if (containerServiceInner == null) {
            return null;
        }

        return new RegistryImpl(containerServiceInner.getName(),
                containerServiceInner,
                this.manager(),
                this.storageManager);
    }

    @Override
    public RegistryCredentials getCredentials(String resourceGroupName, String registryName) {
        return new RegistryCredentialsImpl(this.inner().listCredentials(resourceGroupName, registryName));
    }

    @Override
    public Mono<RegistryCredentials> getCredentialsAsync(String resourceGroupName, String registryName) {
        return this.inner().listCredentialsAsync(resourceGroupName, registryName)
            .map(registryListCredentialsResultInner -> new RegistryCredentialsImpl(registryListCredentialsResultInner));
    }

    @Override
    public RegistryCredentials regenerateCredential(String resourceGroupName, String registryName, AccessKeyType accessKeyType) {
        return new RegistryCredentialsImpl(this.inner().regenerateCredential(resourceGroupName, registryName, PasswordName.fromString(accessKeyType.toString())));
    }

    @Override
    public Mono<RegistryCredentials> regenerateCredentialAsync(String resourceGroupName, String registryName, AccessKeyType accessKeyType) {
        return this.inner().regenerateCredentialAsync(resourceGroupName, registryName, PasswordName.fromString(accessKeyType.toString()))
            .map(registryListCredentialsResultInner -> new RegistryCredentialsImpl(registryListCredentialsResultInner));
    }

    @Override
    public Collection<RegistryUsage> listQuotaUsages(String resourceGroupName, String registryName) {
        RegistryUsageListResultInner resultInner = this.inner().listUsages(resourceGroupName, registryName);

        return Collections.unmodifiableList(resultInner != null && resultInner.value() != null ? resultInner.value() : new ArrayList<RegistryUsage>());
    }

    @Override
    public PagedFlux<RegistryUsage> listQuotaUsagesAsync(String resourceGroupName, String registryName) {
        return PagedConverter.convertListToPagedFlux(this.inner().listUsagesAsync(resourceGroupName, registryName)
            .flatMap(registryUsageListResultInner -> {
                if (registryUsageListResultInner.value() == null) {
                    return Mono.empty();
                }
                return Mono.just(registryUsageListResultInner.value());
            }));
    }

    @Override
    public CheckNameAvailabilityResult checkNameAvailability(String name) {
        return new CheckNameAvailabilityResultImpl(this.inner().checkNameAvailability(name));
    }

    @Override
    public Mono<CheckNameAvailabilityResult> checkNameAvailabilityAsync(String name) {
        return this.inner().checkNameAvailabilityAsync(name)
            .map(registryNameStatusInner -> new CheckNameAvailabilityResultImpl(registryNameStatusInner));
    }

    @Override
    public SourceUploadDefinition getBuildSourceUploadUrl(String rgName, String acrName) {
        return this.getBuildSourceUploadUrlAsync(rgName, acrName).block();
    }

    @Override
    public Mono<SourceUploadDefinition> getBuildSourceUploadUrlAsync(String rgName, String acrName) {
        return this.manager().inner().registries()
                .getBuildSourceUploadUrlAsync(rgName, acrName)
                .map(sourceUploadDefinitionInner -> new SourceUploadDefinitionImpl(sourceUploadDefinitionInner));
    }

    @Override
    public WebhooksClient webhooks() {
        return new WebhooksClientImpl(this.manager(), null);
    }
}
