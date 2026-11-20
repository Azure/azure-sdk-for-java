// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.routing;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;

import java.util.Locale;

/**
 * Pure region-name normalization. Lowercase + strip spaces, hyphens, and underscores.
 * <p>
 * Used wherever the SDK needs case- and separator-insensitive equality between two region
 * name strings it already has in hand (e.g., comparing customer-supplied exclude regions
 * against gateway-supplied region names). The result is suitable for {@link String#equals(Object)}
 * comparison and {@code HashSet} membership.
 * <p>
 * Examples:
 * <ul>
 *   <li>{@code "West US 3"}     → {@code "westus3"}</li>
 *   <li>{@code "EAST-US"}       → {@code "eastus"}</li>
 *   <li>{@code "east_us_2"}     → {@code "eastus2"}</li>
 *   <li>{@code "Future Region"} → {@code "futureregion"} (unknown regions pass through correctly)</li>
 * </ul>
 * <p>
 * This class deliberately does <b>not</b> consult any static region map. Symmetric normalization
 * means two strings that the SDK does not know about still compare correctly to each other.
 * For ID-aware operations (canonical name lookup, region-ID resolution) see {@link RegionIdRegistry}.
 */
public final class RegionNameNormalizer {

    private RegionNameNormalizer() {
    }

    /**
     * Returns the normalized form of a region name, or the input itself if it is null or empty.
     * Pure string transform; never throws and never allocates a string when the input is empty.
     */
    public static String normalize(String regionName) {
        if (StringUtils.isEmpty(regionName)) {
            return regionName;
        }
        return regionName.toLowerCase(Locale.ROOT)
            .replace(" ", "")
            .replace("-", "")
            .replace("_", "");
    }
}
