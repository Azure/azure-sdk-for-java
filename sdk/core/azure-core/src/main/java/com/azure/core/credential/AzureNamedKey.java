// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.credential;

/**
 * Represents a credential bag containing the key and the name of the key.
 *
 * @see AzureNamedKeyCredential
 */
public class AzureNamedKey {
    private final String name;
    private final String key;

    AzureNamedKey(String name, String key) {
        this.name = name;
        this.key = key;
    }

    /**
     * Retrieves the key associated to this credential.
     *
     * @return The key being used to authorize requests.
     */
    public String getKey() {
        return key;
    }

    /**
     * Retrieves the name associated to this credential.
     *
     * @return The key being used to authorize requests.
     */
    public String getName() {
        return name;
    }
}
