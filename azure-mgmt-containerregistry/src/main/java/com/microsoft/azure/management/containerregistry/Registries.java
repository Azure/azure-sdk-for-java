/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.containerregistry;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.apigeneration.Beta.SinceVersion;
import com.microsoft.azure.management.containerregistry.implementation.ContainerRegistryManager;
import com.microsoft.azure.management.containerregistry.implementation.RegistriesInner;
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

/**
 * Entry point to the registry management API.
 */
@Fluent()
@Beta(SinceVersion.V1_1_0)
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
     * Gets the login credentials for the specified container registry.
     * @param groupName the group name
     * @param registryName the registry name
     * @return the login credentials
     */
    @Beta(SinceVersion.V1_3_0)
    RegistryCredentials getCredentials(String groupName, String registryName);

    /**
     * Gets the login credentials for the specified container registry.
     * @param groupName the group name
     * @param registryName the registry name
     * @return the login credentials
     */
    @Beta(SinceVersion.V1_3_0)
    Observable<RegistryCredentials> getCredentialsAsync(String groupName, String registryName);

    /**
     * Regenerates the value for one of the admin user access key for the specified container registry.
     * @param groupName the group name
     * @param registryName the registry name
     * @param accessKeyName the admin user access key name to regenerate the value for
     * @return the list of credentials
     */
    @Beta(SinceVersion.V1_3_0)
    RegistryCredentials regenerateCredential(String groupName, String registryName, AccessKeyName accessKeyName);

    /**
     * Regenerates the value for one of the admin user access key for the specified container registry.
     * @param groupName the group name
     * @param registryName the registry name
     * @param accessKeyName the admin user access key name to regenerate the value for
     * @return the list of credentials
     */
    @Beta(SinceVersion.V1_3_0)
    Observable<RegistryCredentials> regenerateCredentialAsync(String groupName, String registryName, AccessKeyName accessKeyName);
}
