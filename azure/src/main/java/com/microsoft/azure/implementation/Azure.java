/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.implementation;

import com.microsoft.azure.management.compute.AvailabilitySets;
import com.microsoft.azure.management.compute.VirtualMachines;
import com.microsoft.azure.management.resources.GenericResources;
import com.microsoft.azure.management.resources.Subscriptions;
import com.microsoft.azure.management.resources.Tenants;
import com.microsoft.azure.management.resources.fluentcore.arm.AzureConfigurable;
import com.microsoft.azure.management.resources.fluentcore.collection.*;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.microsoft.azure.management.storage.StorageAccounts;
import com.microsoft.azure.management.storage.Usages;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.credentials.ServiceClientCredentials;

public final class Azure {
    private final RestClient restClient;
    // The service specific managers
    ResourceManager.Authenticated resourceManager;

    public static Configure configure() {
        return new AzureConfigureImpl();
    }

    public static Azure authenticate(ServiceClientCredentials credentials) {
        return new Azure(credentials);
    }

    public static Azure authenticate(RestClient restClient) {
        return new Azure(restClient);
    }

    public interface Configure extends AzureConfigurable<Configure> {
        Azure authenticate(ServiceClientCredentials credentials);
    }

    public interface Subscription {
        ResourceGroups resourceGroups();
        GenericResources genericResources();
        StorageAccounts storageAccounts();
        Usages storageUsages();
        AvailabilitySets availabilitySets();
        VirtualMachines virtualMachines();
    }

    public interface  ResourceGroups extends SupportsListing<Azure.ResourceGroup>,
            SupportsGetting<Azure.ResourceGroup>,
            SupportsCreating<Azure.ResourceGroup.DefinitionBlank>,
            SupportsDeleting,
            SupportsUpdating<Azure.ResourceGroup.UpdateBlank> {
    }

    public interface ResourceGroup extends com.microsoft.azure.management.resources.ResourceGroup {
        StorageAccounts.InGroup storageAccounts();
        // VirtualMachinesInGroup virtualMachines();
        // AvailabilitySetsInGroup availabilitySets();
        // VirtualNetworksInGroup virtualNetworks();
    }

    private Azure(ServiceClientCredentials credentials) {
        this.restClient = new RestClient
                .Builder("https://management.azure.com")
                .withCredentials(credentials)
                .build();
    }

    private Azure(RestClient restClient) {
        this.restClient = restClient;
    }

    public Subscriptions subscriptions() {
        return resourceManager().subscriptions();
    }

    public Tenants tenants() {
        return resourceManager().tenants();
    }

    public Azure.Subscription withSubscription(String subscriptionId) {
        return new AzureSubscriptionImpl(restClient, subscriptionId);
    }

    private ResourceManager.Authenticated resourceManager() {
        if (resourceManager == null) {
            resourceManager = ResourceManager.authenticate(this.restClient);
        }
        return resourceManager;
    }
}
