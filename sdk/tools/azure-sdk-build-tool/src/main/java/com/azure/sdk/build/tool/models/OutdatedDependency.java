package com.azure.sdk.build.tool.models;

import java.util.List;
import java.util.Objects;

/**
 * A model class to represent an outdated dependency and a list of suggested replacements.
 */
public class OutdatedDependency {
    private final String gav;
    private final List<String> suggestedReplacementGav;

    /**
     * Creates an instance of {@link OutdatedDependency}.
     * @param gav The group, artifact and version string.
     * @param suggestedReplacementGav The suggested replacement for the outdated dependency.
     */
    public OutdatedDependency(final String gav, final List<String> suggestedReplacementGav) {
        this.gav = gav;
        this.suggestedReplacementGav = suggestedReplacementGav;
    }

    /**
     * Returns the group, artifact and version string for the outdated dependency.
     * @return The group, artifact and version string.
     */
    public String getGav() {
        return gav;
    }

    /**
     * Returns the list of suggested replacements for the outdated dependency.
     * @return The list of suggested replacements for the outdated dependency.
     */
    public List<String> getSuggestedReplacementGav() {
        return suggestedReplacementGav;
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
        return gav.equals(that.gav);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gav);
    }
}
