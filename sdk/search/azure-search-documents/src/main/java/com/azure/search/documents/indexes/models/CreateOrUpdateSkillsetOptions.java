// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import java.util.Objects;

/**
 * This model represents a property bag containing all options for creating or updating a {@link SearchIndexerSkillset
 * skillset}.
 */
public final class CreateOrUpdateSkillsetOptions {
    private final SearchIndexerSkillset skillset;

    private boolean onlyIfUnchanged;
    private Boolean cacheReprocessingChangeDetectionDisabled;
    private Boolean resetRequirementsIgnored;

    /**
     * Creates the property bag used to create or update a {@link SearchIndexerSkillset skillset}.
     *
     * @param skillset The {@link SearchIndexerSkillset skillset} being created or updated.
     * @throws NullPointerException If {@code skillset} is null.
     */
    public CreateOrUpdateSkillsetOptions(SearchIndexerSkillset skillset) {
        this.skillset = Objects.requireNonNull(skillset, "'skillset' cannot be null.");
    }

    /**
     * Gets the {@link SearchIndexerSkillset skillset} that will be created or updated.
     *
     * @return The {@link SearchIndexerSkillset skillset} that will be created or updated.
     */
    public SearchIndexerSkillset getSkillset() {
        return skillset;
    }

    /**
     * Sets the flag that determines whether an update will only occur if the {@link SearchIndexerSkillset skillset} has
     * not been changed since the update has been triggered.
     *
     * @param onlyIfUnchanged Flag that determines whether an update will only occur if the {@link SearchIndexerSkillset
     * skillset} has not been changed since the update has been triggered.
     * @return The updated CreateOrUpdateSkillsetOptions object.
     */
    public CreateOrUpdateSkillsetOptions setOnlyIfUnchanged(boolean onlyIfUnchanged) {
        this.onlyIfUnchanged = onlyIfUnchanged;
        return this;
    }

    /**
     * Gets the flag that determines whether an update will only occur if the {@link SearchIndexerSkillset skillset} has
     * not been changed since the update has been triggered.
     *
     * @return Whether an update will only occur if the {@link SearchIndexerSkillset skillset} has not been changed
     * since the update has been triggered.
     */
    public boolean isOnlyIfUnchanged() {
        return onlyIfUnchanged;
    }

    /**
     * Sets an optional flag that determines whether the created or updated {@link SearchIndexerSkillset skillset}
     * disables cache reprocessing change detection.
     *
     * @param cacheReprocessingChangeDetectionDisabled An optional flag that determines whether the created or updated
     * {@link SearchIndexerSkillset skillset} disables cache reprocessing change detection.
     * @return The updated CreateOrUpdateSkillsetOptions object.
     */
    public CreateOrUpdateSkillsetOptions setCacheReprocessingChangeDetectionDisabled(
        Boolean cacheReprocessingChangeDetectionDisabled) {
        this.cacheReprocessingChangeDetectionDisabled = cacheReprocessingChangeDetectionDisabled;
        return this;
    }

    /**
     * Gets an optional flag that determines whether the created or updated {@link SearchIndexerSkillset skillset}
     * disables cache reprocessing change detection.
     *
     * @return Whether the created or updated {@link SearchIndexerSkillset skillset} disables cache reprocessing change
     * detection.
     */
    public Boolean isCacheReprocessingChangeDetectionDisabled() {
        return cacheReprocessingChangeDetectionDisabled;
    }

    /**
     * Sets an optional flag that determines whether the created or updated {@link SearchIndexerSkillset skillset}
     * ignores cache reset requirements.
     *
     * @param resetRequirementsIgnored An optional flag that determines whether the created or updated {@link
     * SearchIndexerSkillset skillset} ignores cache reset requirements.
     * @return The updated CreateOrUpdateSkillsetOptions object.
     */
    public CreateOrUpdateSkillsetOptions setResetRequirementsIgnored(Boolean resetRequirementsIgnored) {
        this.resetRequirementsIgnored = resetRequirementsIgnored;
        return this;
    }

    /**
     * Gets an optional flag that determines whether the created or updated {@link SearchIndexerSkillset skillset}
     * ignores cache reset requirements.
     *
     * @return Whether the created or updated {@link SearchIndexerSkillset skillset} ignores cache reset requirements.
     */
    public Boolean isResetRequirementsIgnored() {
        return resetRequirementsIgnored;
    }
}
