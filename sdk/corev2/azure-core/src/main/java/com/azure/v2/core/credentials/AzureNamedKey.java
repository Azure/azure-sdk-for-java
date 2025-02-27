// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.core.credentials;

import io.clientcore.core.annotations.Metadata;
import io.clientcore.core.annotations.TypeConditions;

/**
 * Represents a credential bag containing the key and the name of the key.
 *
 * @see AzureNamedKeyCredential
 */
@Metadata(conditions = TypeConditions.IMMUTABLE)
public final class AzureNamedKey {
    private final String name;
    private final String key;

    AzureNamedKey(String name, String key) {
        this.name = name;
        this.key = key;
    }

    /**
     * Retrieves the key.
     *
     * @return The key.
     */
    public String getKey() {
        return key;
    }

    /**
     * Retrieves the name associated with the key.
     *
     * @return The name of the key.
     */
    public String getName() {
        return name;
    }
}
