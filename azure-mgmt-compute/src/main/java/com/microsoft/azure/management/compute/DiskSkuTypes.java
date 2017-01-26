/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute;

/**
 * Defines values for DiskSkuTypes.
 */
public final class DiskSkuTypes {
    /** Static value Aligned for DiskSkuTypes. */
    public static final DiskSkuTypes STANDARD_LRS = new DiskSkuTypes(StorageAccountTypes.STANDARD_LRS);

    /** Static value Classic for DiskSkuTypes. */
    public static final DiskSkuTypes PREMIUM_LRS = new DiskSkuTypes(StorageAccountTypes.PREMIUM_LRS);

    /** The actual serialized value for a DiskSkuTypes instance. */
    private StorageAccountTypes value;

    /**
     * Creates a custom value for DiskSkuTypes.
     * @param value the custom value
     */
    public DiskSkuTypes(StorageAccountTypes value) {
        this.value = value;
    }

    /**
     * @return the account type associated with the sku.
     */
    public StorageAccountTypes accountType() {
        return this.value;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DiskSkuTypes)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        DiskSkuTypes rhs = (DiskSkuTypes) obj;
        if (value == null) {
            return rhs.value == null;
        } else {
            return value.equals(rhs.value);
        }
    }
}
