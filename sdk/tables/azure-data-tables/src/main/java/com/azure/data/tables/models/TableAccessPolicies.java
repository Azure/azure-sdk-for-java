// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.models;

import com.azure.core.annotation.Immutable;

import java.util.List;

/**
 * This class contains values which correlate to the access polices set for a specific table.
 */
@Immutable
public final class TableAccessPolicies {
    private final List<TableSignedIdentifier> identifiers;

    /**
     * Constructs a {@link TableAccessPolicies}.
     *
     * @param identifiers {@link TableSignedIdentifier TableSignedIdentifiers} associated with the table.
     */
    public TableAccessPolicies(List<TableSignedIdentifier> identifiers) {
        this.identifiers = identifiers;
    }

    /**
     * @return the {@link TableSignedIdentifier TableSignedIdentifiers} associated with the table.
     */
    public List<TableSignedIdentifier> getIdentifiers() {
        return this.identifiers;
    }
}
