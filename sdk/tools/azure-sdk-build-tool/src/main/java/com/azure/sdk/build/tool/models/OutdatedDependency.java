// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.sdk.build.tool.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

/**
 * A model class to represent an outdated dependency and a list of suggested replacements.
 */
public class OutdatedDependency {
    @JsonProperty
    private final String outdatedDependency;
    @JsonProperty
    private final List<String> suggestedReplacements;

    /**
     * Creates an instance of {@link OutdatedDependency}.
     * @param outdatedDependency The group, artifact and version string.
     * @param suggestedReplacements The suggested replacement for the outdated dependency.
     */
    public OutdatedDependency(final String outdatedDependency, final List<String> suggestedReplacements) {
        this.outdatedDependency = outdatedDependency;
        this.suggestedReplacements = suggestedReplacements;
    }

    /**
     * Returns the group, artifact and version string for the outdated dependency.
     * @return The group, artifact and version string.
     */
    public String getOutdatedDependency() {
        return outdatedDependency;
    }

    /**
     * Returns the list of suggested replacements for the outdated dependency.
     * @return The list of suggested replacements for the outdated dependency.
     */
    public List<String> getSuggestedReplacements() {
        return suggestedReplacements;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final OutdatedDependency that = (OutdatedDependency) o;
        return outdatedDependency.equals(that.outdatedDependency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(outdatedDependency);
    }
}
