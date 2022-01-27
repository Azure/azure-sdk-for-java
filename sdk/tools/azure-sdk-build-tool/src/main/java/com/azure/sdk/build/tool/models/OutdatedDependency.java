package com.azure.sdk.build.tool.models;

import java.util.List;
import java.util.Objects;

/**
 * A model class to represent an outdated dependency and a list of suggested replacements.
 */
public class OutdatedDependency {
    private final String gav;
    private final List<String> suggestedReplacementGav;

    public OutdatedDependency(final String gav, final List<String> suggestedReplacementGav) {
        this.gav = gav;
        this.suggestedReplacementGav = suggestedReplacementGav;
    }

    public String getGav() {
        return gav;
    }

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
