// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.models;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Defines values for DiskSkuTypes. */
// TODO: Naming: this should really be DiskSkuType
public final class DiskSkuTypes {
    // This needs to be at the beginning for the initialization to happen correctly
    private static final Map<String, DiskSkuTypes> VALUES_BY_NAME = new HashMap<>();

    /** Static value STANDARD_LRS for DiskSkuTypes. */
    public static final DiskSkuTypes STANDARD_LRS = new DiskSkuTypes(DiskStorageAccountTypes.STANDARD_LRS);

    /** Static value PREMIUM_LRS for DiskSkuTypes. */
    public static final DiskSkuTypes PREMIUM_LRS = new DiskSkuTypes(DiskStorageAccountTypes.PREMIUM_LRS);

    /** Static value STANDARD_SSD_LRS for DiskSkuTypes. */
    public static final DiskSkuTypes STANDARD_SSD_LRS = new DiskSkuTypes(DiskStorageAccountTypes.STANDARD_SSD_LRS);

    /** Static value ULTRA_SSD_LRS for DiskSkuTypes. */
    public static final DiskSkuTypes ULTRA_SSD_LRS = new DiskSkuTypes(DiskStorageAccountTypes.ULTRA_SSD_LRS);

    /** The actual serialized value for a DiskSkuTypes instance. */
    private final DiskStorageAccountTypes value;

    /** @return predefined disk SKU types */
    public static DiskSkuTypes[] values() {
        Collection<DiskSkuTypes> valuesCollection = VALUES_BY_NAME.values();
        return valuesCollection.toArray(new DiskSkuTypes[valuesCollection.size()]);
    }

    /**
     * Creates a custom value for DiskSkuTypes.
     *
     * @param value the custom value
     */
    public DiskSkuTypes(DiskStorageAccountTypes value) {
        // TODO: This constructor should be private, but keeping as is for now to keep 1.0.0 back compat
        this.value = value;
        if (value != null) {
            VALUES_BY_NAME.put(value.toString().toLowerCase(Locale.ROOT), this);
        }
    }

    /**
     * Parses a value into a disk SKU type and creates a new DiskSkuType instance if not found among the existing ones.
     *
     * @param value a disk SKU type name
     * @return the parsed or created disk SKU type
     */
    public static DiskSkuTypes fromStorageAccountType(DiskStorageAccountTypes value) {
        if (value == null) {
            return null;
        }

        DiskSkuTypes result = VALUES_BY_NAME.get(value.toString().toLowerCase(Locale.ROOT));
        if (result != null) {
            return result;
        } else {
            return new DiskSkuTypes(value);
        }
    }

    /**
     * Parses a value into a disk SKU type and creates a new DiskSkuType instance if not found among the existing ones.
     *
     * @param diskSku a disk SKU type name
     * @return the parsed or created disk SKU type
     */
    public static DiskSkuTypes fromDiskSku(DiskSku diskSku) {
        if (diskSku == null) {
            return null;
        }
        return fromStorageAccountType(diskSku.name());
    }

    /** @return the account type associated with the SKU. */
    public DiskStorageAccountTypes accountType() {
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
        } else if (value == null) {
            return ((DiskSkuTypes) obj).value == null;
        } else {
            return value.equals(((DiskSkuTypes) obj).value);
        }
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
