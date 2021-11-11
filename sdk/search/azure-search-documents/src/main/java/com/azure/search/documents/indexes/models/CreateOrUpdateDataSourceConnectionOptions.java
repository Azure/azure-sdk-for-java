// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import java.util.Objects;

/**
 * This model represents a property bag containing all options for creating or updating a {@link
 * SearchIndexerDataSourceConnection data source connection}.
 */
public final class CreateOrUpdateDataSourceConnectionOptions {
    private final SearchIndexerDataSourceConnection dataSourceConnection;

    private boolean onlyIfUnchanged;
    private Boolean cacheResetRequirementsIgnored;

    /**
     * Creates the property bag used to create or update a {@link SearchIndexerDataSourceConnection data source
     * connection}.
     *
     * @param dataSourceConnection The {@link SearchIndexerDataSourceConnection data source connection} being created or
     * updated.
     * @throws NullPointerException If {@code dataSourceConnection} is null.
     */
    public CreateOrUpdateDataSourceConnectionOptions(SearchIndexerDataSourceConnection dataSourceConnection) {
        this.dataSourceConnection = Objects.requireNonNull(dataSourceConnection,
            "'dataSourceConnection' cannot be null.");
    }

    /**
     * Gets the {@link SearchIndexerDataSourceConnection data source connection} that will be created or updated.
     *
     * @return The {@link SearchIndexerDataSourceConnection data source connection} that will be created or updated.
     */
    public SearchIndexerDataSourceConnection getDataSourceConnection() {
        return dataSourceConnection;
    }

    /**
     * Sets the flag that determines whether an update will only occur if the {@link SearchIndexerDataSourceConnection
     * data source connection} has not been changed since the update has been triggered.
     *
     * @param onlyIfUnchanged Flag that determines whether an update will only occur if the {@link
     * SearchIndexerDataSourceConnection data source connection} has not been changed since the update has been
     * triggered.
     * @return The updated CreateOrUpdateDataSourceConnectionOptions object.
     */
    public CreateOrUpdateDataSourceConnectionOptions setOnlyIfUnchanged(boolean onlyIfUnchanged) {
        this.onlyIfUnchanged = onlyIfUnchanged;
        return this;
    }

    /**
     * Gets the flag that determines whether an update will only occur if the {@link SearchIndexerDataSourceConnection
     * data source connection} has not been changed since the update has been triggered.
     *
     * @return Whether an update will only occur if the {@link SearchIndexerDataSourceConnection data source connection}
     * has not been changed since the update has been triggered.
     */
    public boolean isOnlyIfUnchanged() {
        return onlyIfUnchanged;
    }

    /**
     * Sets an optional flag that determines whether the created or updated {@link SearchIndexerDataSourceConnection
     * data source connection} ignores cache reset requirements.
     *
     * @param cacheResetRequirementsIgnored An optional flag that determines whether the created or updated {@link
     * SearchIndexerDataSourceConnection data source connection} ignores cache reset requirements.
     * @return The updated CreateOrUpdateDataSourceConnectionOptions object.
     */
    public CreateOrUpdateDataSourceConnectionOptions setCacheResetRequirementsIgnored(Boolean cacheResetRequirementsIgnored) {
        this.cacheResetRequirementsIgnored = cacheResetRequirementsIgnored;
        return this;
    }

    /**
     * Gets an optional flag that determines whether the created or updated {@link SearchIndexerDataSourceConnection
     * data source connection} ignores cache reset requirements.
     *
     * @return Whether the created or updated {@link SearchIndexerDataSourceConnection data source connection} ignores
     * cache reset requirements.
     */
    public Boolean isCacheResetRequirementsIgnored() {
        return cacheResetRequirementsIgnored;
    }
}
