// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storage.models;

/** Defines sku values for storage account resource. */
public final class StorageAccountSkuType {
    /** Static value STANDARD_LRS for StorageAccountSkuType. */
    public static final StorageAccountSkuType STANDARD_LRS = new StorageAccountSkuType(SkuName.STANDARD_LRS);

    /** Static value STANDARD_GRS for StorageAccountSkuType. */
    public static final StorageAccountSkuType STANDARD_GRS = new StorageAccountSkuType(SkuName.STANDARD_GRS);

    /** Static value STANDARD_RAGRS for StorageAccountSkuType. */
    public static final StorageAccountSkuType STANDARD_RAGRS = new StorageAccountSkuType(SkuName.STANDARD_RAGRS);

    /** Static value STANDARD_RAGRS for StorageAccountSkuType. */
    public static final StorageAccountSkuType STANDARD_ZRS = new StorageAccountSkuType(SkuName.STANDARD_ZRS);

    /** Static value PREMIUM_LRS for StorageAccountSkuType. */
    public static final StorageAccountSkuType PREMIUM_LRS = new StorageAccountSkuType(SkuName.PREMIUM_LRS);

    private final SkuName name;

    /** @return the storage account sku name */
    public SkuName name() {
        return this.name;
    }

    /**
     * Creates StorageAccountSkuType from sku name.
     *
     * @param name the sku name
     * @return StorageAccountSkuType corresponds to the given sku name
     */
    public static StorageAccountSkuType fromSkuName(SkuName name) {
        return new StorageAccountSkuType(name);
    }

    /**
     * Creates StorageAccountSkuType.
     *
     * @param name the sku name
     */
    private StorageAccountSkuType(SkuName name) {
        this.name = name;
    }
}
