// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** Defines SQL Database Sku. */
@Fluent
public final class DatabaseSku {
    private static final ConcurrentMap<String, DatabaseSku> VALUES = new ConcurrentHashMap<>();
<Generated>

    private final Sku sku;

    /**
     * Creates a database sku.
     *
     * @param name the sku name
     * @param tier the sku tier
     * @param family the sku family
     * @param capacity the sku capacity
     * @param size the sku size
     */
    public DatabaseSku(String name, String tier, String family, Integer capacity, String size) {
        this.sku = new Sku().withName(name).withTier(tier).withFamily(family).withCapacity(capacity).withSize(size);
        VALUES.putIfAbsent(toString(), this);
    }

    /**
     * Parses a sku to a DatabaseSku instance.
     *
     * @param sku the sku.
     * @return the DatabaseSku object, or null if sku is null.
     */
    public static DatabaseSku fromSku(Sku sku) {
        if (sku == null) {
            return null;
        }
        return new DatabaseSku(sku.name(), sku.tier(), sku.family(), sku.capacity(), sku.size());
    }

    /**
     * Lists the pre-defined database sku.
     *
     * @return immutable collection of the pre-defined database sku
     */
    public static Collection<DatabaseSku> getAll() {
        return VALUES.values();
    }

    @Override
    public String toString() {
        return String.format("%s/%s/%s/%d/%s", sku.name(), sku.tier(), sku.family(), sku.capacity(), sku.size());
    }

    /** @return the underneath sku description */
    @JsonValue
    public Sku toSku() {
        return new Sku()
            .withName(sku.name())
            .withTier(sku.tier())
            .withFamily(sku.family())
            .withCapacity(sku.capacity())
            .withSize(sku.size());
    }

    @Override
    public int hashCode() {
        return sku.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DatabaseSku)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        DatabaseSku rhs = (DatabaseSku) obj;
        return toString().equalsIgnoreCase(rhs.toString());
    }
}
