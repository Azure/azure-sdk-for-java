// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import java.util.Locale;
import java.util.StringJoiner;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public final class CosmosRegionSwitchHint {

    private final String hintRepresentation;

    public static final CosmosRegionSwitchHint NONE =
            new CosmosRegionSwitchHint("none");
    public static final CosmosRegionSwitchHint LOCAL_REGION_PREFERRED =
            new CosmosRegionSwitchHint("localRegionPrefered");
    public static final CosmosRegionSwitchHint REMOTE_REGION_PREFERRED =
            new CosmosRegionSwitchHint("remoteRegionPrefered");

    private CosmosRegionSwitchHint(String hintRepresentation) {
        this.hintRepresentation = hintRepresentation;
    }

    public static CosmosRegionSwitchHint fromString(String hintRepresentation) {
        checkNotNull(hintRepresentation, "Argument 'hintRepresentation' must not be null.");

        String normalizedName = hintRepresentation.trim().toLowerCase(Locale.ROOT);
        switch (normalizedName) {
            case "none": return CosmosRegionSwitchHint.NONE;
            case "localregionprefered": return CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED;
            case "remoteregionprefered": return CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED;

            default:
                String errorMessage = String.format(
                        "Argument 'hintRepresentation' has invalid value '%s' - valid values are: %s",
                        hintRepresentation,
                        getValidValues());

                throw new IllegalArgumentException(errorMessage);
        }
    }

    private static String getValidValues() {
        return new StringJoiner(", ")
                .add(CosmosRegionSwitchHint.NONE.hintRepresentation)
                .add(CosmosRegionSwitchHint.LOCAL_REGION_PREFERRED.hintRepresentation)
                .add(CosmosRegionSwitchHint.REMOTE_REGION_PREFERRED.hintRepresentation)
                .toString();
    }
}
