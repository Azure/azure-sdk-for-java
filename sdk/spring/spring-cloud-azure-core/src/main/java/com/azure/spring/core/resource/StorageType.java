// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.resource;

/**
 * Azure Storage Account types.
 *
 * @author Warren Zhu
 * @see <a href="https://docs.microsoft.com/en-us/azure/storage/common/storage-account-overview">StorageAccount</a>
 */
public enum StorageType {
    BLOB("blob"), FILE("file");

    private final String type;

    StorageType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
