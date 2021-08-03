// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables.models;

import com.azure.core.annotation.Fluent;

import java.util.Objects;

/**
 * A signed identifier.
 */
@Fluent
public final class TableSignedIdentifier {
    /*
     * A unique id
     */
    private final String id;

    /*
     * An access policy.
     */
    private TableAccessPolicy accessPolicy;

    /**
     * Create a {@link TableSignedIdentifier}.
     *
     * @param id A unique id for this {@link TableSignedIdentifier}.
     */
    public TableSignedIdentifier(String id) {
        Objects.requireNonNull(id, "'id' cannot be null");

        this.id = id;
    }

    /**
     * Get the unique id.
     *
     * @return The id.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Get the {@link TableAccessPolicy}.
     *
     * @return The {@link TableAccessPolicy}.
     */
    public TableAccessPolicy getAccessPolicy() {
        return this.accessPolicy;
    }

    /**
     * Set a {@link TableAccessPolicy}.
     *
     * @param accessPolicy The {@link TableAccessPolicy} to set.
     *
     * @return The updated {@link TableSignedIdentifier} object.
     */
    public TableSignedIdentifier setAccessPolicy(TableAccessPolicy accessPolicy) {
        this.accessPolicy = accessPolicy;

        return this;
    }
}
