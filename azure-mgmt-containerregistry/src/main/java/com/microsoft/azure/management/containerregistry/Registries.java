/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.containerregistry;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.containerregistry.implementation.ContainerRegistryManager;
import com.microsoft.azure.management.containerregistry.implementation.RegistriesInner;
import com.microsoft.azure.management.containerregistry.implementation.RegistryListCredentialsResultInner;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsListingByResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsBatchCreation;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeletingById;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import rx.Observable;

@Fluent()
@Beta()
public interface Registries extends
    SupportsCreating<Registry.DefinitionStages.Blank>,
    HasManager<ContainerRegistryManager>,
    HasInner<RegistriesInner>,
    SupportsBatchCreation<Registry>,
    SupportsGettingById<Registry>,
    SupportsDeletingById,
    SupportsDeletingByResourceGroup,
    SupportsListingByResourceGroup<Registry>,
    SupportsGettingByResourceGroup<Registry>,
    SupportsListing<Registry> {

    /**
     * Lists the login credentials for the specified container registry.
     */
    RegistryListCredentialsResultInner listCredentials(String groupName, String registryName);

    /**
     * Lists the login credentials for the specified container registry.
     */
    Observable<RegistryListCredentialsResultInner> listCredentialsAsync(String groupName, String registryName);

    /**
     * Regenerates one of the login credentials for the specified container registry.
     */
    RegistryListCredentialsResultInner regenerateCredential(String groupName, String registryName, PasswordName passwordName);

    /**
     * Regenerates one of the login credentials for the specified container registry.
     */
    Observable<RegistryListCredentialsResultInner> regenerateCredentialAsync(String groupName, String registryName, PasswordName passwordName);
}
