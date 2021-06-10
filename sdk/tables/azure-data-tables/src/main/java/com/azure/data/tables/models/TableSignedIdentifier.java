// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables.models;

import com.azure.core.annotation.Fluent;

/**
 * A signed identifier.
 */
@Fluent
public final class TableSignedIdentifier {
    /*
     * A unique id
     */
    private String id;

    /*
     * An access policy.
     */
    private TableAccessPolicy accessPolicy;

    /**
     * Get the unique id.
     *
     * @return The id.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Set a unique id.
     *
     * @param id The id to set.
     *
     * @return The updated {@link TableSignedIdentifier} object.
     */
    public TableSignedIdentifier setId(String id) {
        this.id = id;

        return this;
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
