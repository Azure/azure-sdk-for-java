// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.keyvault.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingById;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsGettingByName;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsCreating;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsDeletingById;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListing;
import reactor.core.publisher.Mono;

/** Entry point for Key Vault secrets API. */
@Fluent
public interface Secrets
    extends SupportsCreating<Secret.DefinitionStages.Blank>,
        SupportsDeletingById,
        SupportsGettingById<Secret>,
        SupportsGettingByName<Secret>,
        SupportsListing<Secret> {
    /**
     * Gets a Key Vault secret when the secret is enabled.
     *
     * @param name the name of the secret
     * @param version the version of the secret
     * @return the secret
     */
    Secret getByNameAndVersion(String name, String version);

    /**
     * Gets a Key Vault secret when the secret is enabled.
     *
     * @param name the name of the secret
     * @param version the version of the secret
     * @return the secret
     */
    Mono<Secret> getByNameAndVersionAsync(String name, String version);

    /**
     * Enables a secret.
     *
     * @param name the name of the secret
     * @param version the version of the secret
     * @return the secret
     */
    Secret enableByNameAndVersion(String name, String version);

    /**
     * Enables a secret.
     *
     * @param name the name of the secret
     * @param version the version of the secret
     * @return the secret
     */
    Mono<Secret> enableByNameAndVersionAsync(String name, String version);

    /**
     * Disables a secret.
     *
     * @param name the name of the secret
     * @param version the version of the secret
     */
    void disableByNameAndVersion(String name, String version);

    /**
     * Disables a secret.
     *
     * @param name the name of the secret
     * @param version the version of the secret
     * @return completion
     */
    Mono<Void> disableByNameAndVersionAsync(String name, String version);
}
