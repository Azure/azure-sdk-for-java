/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Defines values for DiskSkuTypes.
 */
//TODO: Naming: this should really be DiskSkuType
public final class DiskSkuTypes {
    // This needs to be at the beginning for the initialization to happen correctly
    private static final Map<String, DiskSkuTypes> VALUES_BY_NAME = new HashMap<>();

    /** Static value Aligned for DiskSkuTypes. */
    public static final DiskSkuTypes STANDARD_LRS = new DiskSkuTypes(StorageAccountTypes.STANDARD_LRS);

    /** Static value Classic for DiskSkuTypes. */
    public static final DiskSkuTypes PREMIUM_LRS = new DiskSkuTypes(StorageAccountTypes.PREMIUM_LRS);

    /** The actual serialized value for a DiskSkuTypes instance. */
    private StorageAccountTypes value;

    /**
     * @return predefined disk SKU types
     */
    public static DiskSkuTypes[] values() {
        Collection<DiskSkuTypes> valuesCollection = VALUES_BY_NAME.values();
        return valuesCollection.toArray(new DiskSkuTypes[valuesCollection.size()]);
    }

    /**
     * Creates a custom value for DiskSkuTypes.
     * @param value the custom value
     */
    public DiskSkuTypes(StorageAccountTypes value) {
        // TODO: This constructor should be private, but keeping as is for now to keep 1.0.0 back compat
        this.value = value;
        if (value != null) {
            VALUES_BY_NAME.put(value.toString().toLowerCase(), this);
        }
    }

    /**
     * Parses a value into a disk SKU type and creates a new DiskSkuType instance if not found among the existing ones.
     *
     * @param value a disk SKU type name
     * @return the parsed or created disk SKU type
     */
    public static DiskSkuTypes fromStorageAccountType(StorageAccountTypes value) {
        if (value == null) {
            return null;
        }

        DiskSkuTypes result = VALUES_BY_NAME.get(value.toString().toLowerCase());
        if (result != null) {
            return result;
        } else {
            return new DiskSkuTypes(value);
        }
    }

    /**
     * @return the account type associated with the SKU.
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
        } else if (obj == this) {
            return true;
        } else  if (value == null) {
            return ((DiskSkuTypes) obj).value == null;
        } else {
            return value.equals(((DiskSkuTypes) obj).value);
        }
    }
}
