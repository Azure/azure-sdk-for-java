// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import java.util.Objects;

/**
 * This model represents a property bag containing all options for creating or updating an {@link SearchIndexer
 * indexer}.
 */
public class CreateOrUpdateIndexerOptions {
    private final SearchIndexer indexer;

    private boolean onlyIfUnchanged;
    private Boolean cacheReprocessingChangeDetectionDisabled;
    private Boolean cacheResetRequirementsIgnored;

    /**
     * Creates the property bag used to create or update an {@link SearchIndexer indexer}.
     *
     * @param indexer The {@link SearchIndexer indexer} being created or updated.
     * @throws NullPointerException If {@code indexer} is null.
     */
    public CreateOrUpdateIndexerOptions(SearchIndexer indexer) {
        this.indexer = Objects.requireNonNull(indexer, "'indexer' cannot be null.");
    }

    /**
     * Gets the {@link SearchIndexer indexer} that will be created or updated.
     *
     * @return The {@link SearchIndexer indexer} that will be created or updated.
     */
    public SearchIndexer getIndexer() {
        return indexer;
    }

    /**
     * Sets the flag that determines whether an update will only occur if the {@link SearchIndexer indexer} has not been
     * changed since the update has been triggered.
     *
     * @param onlyIfUnchanged Flag that determines whether an update will only occur if the {@link SearchIndexer
     * indexer} has not been changed since the update has been triggered.
     * @return The updated CreateOrUpdateIndexerOptions object.
     */
    public CreateOrUpdateIndexerOptions setOnlyIfUnchanged(boolean onlyIfUnchanged) {
        this.onlyIfUnchanged = onlyIfUnchanged;
        return this;
    }

    /**
     * Gets the flag that determines whether an update will only occur if the {@link SearchIndexer indexer} has not been
     * changed since the update has been triggered.
     *
     * @return Whether an update will only occur if the {@link SearchIndexer indexer} has not been changed since the
     * update has been triggered.
     */
    public boolean isOnlyIfUnchanged() {
        return onlyIfUnchanged;
    }

    /**
     * Sets an optional flag that determines whether the created or updated {@link SearchIndexer indexer} disables cache
     * reprocessing change detection.
     *
     * @param cacheReprocessingChangeDetectionDisabled An optional flag that determines whether the created or updated
     * {@link SearchIndexer indexer} disables cache reprocessing change detection.
     * @return The updated CreateOrUpdateIndexerOptions object.
     */
    public CreateOrUpdateIndexerOptions setCacheReprocessingChangeDetectionDisabled(
        Boolean cacheReprocessingChangeDetectionDisabled) {
        this.cacheReprocessingChangeDetectionDisabled = cacheReprocessingChangeDetectionDisabled;
        return this;
    }

    /**
     * Gets an optional flag that determines whether the created or updated {@link SearchIndexer indexer} disables cache
     * reprocessing change detection.
     *
     * @return Whether the created or updated {@link SearchIndexer indexer} disables cache reprocessing change
     * detection.
     */
    public Boolean isCacheReprocessingChangeDetectionDisabled() {
        return cacheReprocessingChangeDetectionDisabled;
    }

    /**
     * Sets an optional flag that determines whether the created or updated {@link SearchIndexer indexer} ignores cache
     * reset requirements.
     *
     * @param cacheResetRequirementsIgnored An optional flag that determines whether the created or updated {@link
     * SearchIndexer indexer} ignores cache reset requirements.
     * @return The updated CreateOrUpdateIndexerOptions object.
     */
    public CreateOrUpdateIndexerOptions setCacheResetRequirementsIgnored(Boolean cacheResetRequirementsIgnored) {
        this.cacheResetRequirementsIgnored = cacheResetRequirementsIgnored;
        return this;
    }

    /**
     * Gets an optional flag that determines whether the created or updated {@link SearchIndexer indexer} ignores cache
     * reset requirements.
     *
     * @return Whether the created or updated {@link SearchIndexer indexer} ignores cache reset requirements.
     */
    public Boolean isCacheResetRequirementsIgnored() {
        return cacheResetRequirementsIgnored;
    }
}
