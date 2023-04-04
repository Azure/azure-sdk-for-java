// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.blob.models;

import com.azure.core.annotation.Immutable;

/**
 * This class contains information about the storage account.
 */
@Immutable
public class StorageAccountInfo {
    private final SkuName skuName;
    private final AccountKind accountKind;
    private final boolean isHnsEnabled;

    /**
     * Constructs a {@link StorageAccountInfo}.
     *
     * @param skuName SKU of the account.
     * @param accountKind Type of the account.
     */
    public StorageAccountInfo(final SkuName skuName, final AccountKind accountKind) {
        this.skuName = skuName;
        this.accountKind = accountKind;
        this.isHnsEnabled = false;
    }

    /**
     * Constructs a {@link StorageAccountInfo}.
     *
     * @param skuName SKU of the account.
     * @param accountKind Type of the account.
     * @param isHnsEnabled whether hierarchical namespace is enabled on the account.
     */
    public StorageAccountInfo(final SkuName skuName, final AccountKind accountKind, Boolean isHnsEnabled) {
        this.skuName = skuName;
        this.accountKind = accountKind;
        this.isHnsEnabled = isHnsEnabled != null && isHnsEnabled;
    }

    /**
     * Gets the SKU of the account.
     *
     * @return the SKU of the account.
     */
    public SkuName getSkuName() {
        return skuName;
    }

    /**
     * Gets the information of the type of the account.
     *
     * @return the type of the account.
     */
    public AccountKind getAccountKind() {
        return accountKind;
    }

    /**
     * Specifies whether hierarchical namespace is enabled on the account.
     *
     * @return whether hierarchical namespace is enabled on the account.
     */
    public boolean isHierarchicalNamespaceEnabled() {
        return isHnsEnabled;
    }
}
