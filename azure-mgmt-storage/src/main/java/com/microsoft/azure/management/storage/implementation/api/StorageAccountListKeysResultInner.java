/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.storage.implementation.api;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The ListKeys operation response.
 */
public class StorageAccountListKeysResultInner {
    /**
     * Gets the list of account keys and their properties.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<StorageAccountKey> keys;

    /**
     * Get the keys value.
     *
     * @return the keys value
     */
    public List<StorageAccountKey> keys() {
        return this.keys;
    }

}
