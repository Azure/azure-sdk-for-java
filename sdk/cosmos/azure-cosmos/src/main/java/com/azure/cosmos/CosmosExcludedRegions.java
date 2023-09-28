// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;

/**
 * A class which encapsulates a set of excluded regions.
 * */
public final class CosmosExcludedRegions {
    private final Set<String> excludedRegions;
    private final String excludedRegionsAsString;
    private static final Pattern SPACE_PATTERN = Pattern.compile(" ");

    /**
     * Instantiates {@code CosmosExcludedRegions}.
     *
     * @throws IllegalArgumentException if {@code excludedRegions} is set as null.
     * */
    public CosmosExcludedRegions(Set<String> excludedRegions) {

        checkArgument(excludedRegions != null, "excludedRegions cannot be set to null");

        this.excludedRegions = new HashSet<>(excludedRegions);
        this.excludedRegionsAsString = stringifyExcludedRegions(this.excludedRegions);
    }

    /**
     * Gets the set of excluded regions.
     *
     * @return a set of excluded regions.
     * */
    public Set<String> getExcludedRegions() {

        if (this.excludedRegions == null || this.excludedRegions.isEmpty()) {
            return new HashSet<>();
        }

        return new HashSet<>(this.excludedRegions);
    }

    @Override
    public String toString() {
        return this.excludedRegionsAsString;
    }

    private static String stringifyExcludedRegions(Set<String> excludedRegions) {
        String substring = "";

        if (excludedRegions == null || excludedRegions.isEmpty()) {
            substring =  "";
        } else {
            substring = excludedRegions
                .stream()
                .map(r -> SPACE_PATTERN.matcher(r.toLowerCase(Locale.ROOT)).replaceAll(""))
                .collect(Collectors.joining(","));
        }

        return "[" + substring + "]";
    }
}
