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

/** Entry point for Key Vault keys API. */
@Fluent
public interface Keys
    extends SupportsCreating<Key.DefinitionStages.Blank>,
        SupportsDeletingById,
        SupportsGettingById<Key>,
        SupportsGettingByName<Key>,
        SupportsListing<Key> {
    /**
     * Gets a Key Vault key.
     *
     * @param name the name of the key
     * @param version the version of the key
     * @return the key
     */
    Key getByNameAndVersion(String name, String version);

    /**
     * Gets a Key Vault key.
     *
     * @param name the name of the key
     * @param version the version of the key
     * @return the key
     */
    Mono<Key> getByNameAndVersionAsync(String name, String version);

    /**
     * Restores a backup key into a Key Vault key.
     *
     * @param backup the backup key
     * @return the key restored from the backup
     */
    Key restore(byte[] backup);

    /**
     * Restores a backup key into a Key Vault key.
     *
     * @param backup the backup key
     * @return the key restored from the backup
     */
    Mono<Key> restoreAsync(byte[] backup);
}
