// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.routing;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Maps region name variants (any casing/spacing) to the canonical CosmosDB region name format.
 * <p>
 * For example, "westus2" → "West US 2", "west us 3" → "West US 3", "EAST US" → "East US".
 * <p>
 * If the input region name is not recognized, it is returned as-is to support forward compatibility
 * with new Azure regions that may not yet be in the static list.
 */
public final class RegionNameMapper {

    private static final Map<String, String> NORMALIZED_TO_CANONICAL;

    static {
        Map<String, String> map = new HashMap<>();

        // Americas
        addMapping(map, "West US");
        addMapping(map, "West US 2");
        addMapping(map, "West US 3");
        addMapping(map, "West Central US");
        addMapping(map, "East US");
        addMapping(map, "East US 2");
        addMapping(map, "East US 3");
        addMapping(map, "Central US");
        addMapping(map, "South Central US");
        addMapping(map, "North Central US");
        addMapping(map, "Canada East");
        addMapping(map, "Canada Central");
        addMapping(map, "Brazil South");
        addMapping(map, "Brazil Southeast");
        addMapping(map, "Mexico Central");
        addMapping(map, "Chile Central");

        // Europe
        addMapping(map, "North Europe");
        addMapping(map, "West Europe");
        addMapping(map, "France Central");
        addMapping(map, "France South");
        addMapping(map, "UK West");
        addMapping(map, "UK South");
        addMapping(map, "Germany North");
        addMapping(map, "Germany West Central");
        addMapping(map, "Germany Central");
        addMapping(map, "Germany Northeast");
        addMapping(map, "Switzerland North");
        addMapping(map, "Switzerland West");
        addMapping(map, "Norway East");
        addMapping(map, "Norway West");
        addMapping(map, "Sweden Central");
        addMapping(map, "Sweden South");
        addMapping(map, "Poland Central");
        addMapping(map, "Italy North");
        addMapping(map, "Spain Central");
        addMapping(map, "Austria East");
        addMapping(map, "Belgium Central");
        addMapping(map, "Denmark East");
        addMapping(map, "Finland Central");
        addMapping(map, "Greece Central");

        // Asia Pacific
        addMapping(map, "East Asia");
        addMapping(map, "Southeast Asia");
        addMapping(map, "Japan East");
        addMapping(map, "Japan West");
        addMapping(map, "Australia East");
        addMapping(map, "Australia Southeast");
        addMapping(map, "Australia Central");
        addMapping(map, "Australia Central 2");
        addMapping(map, "Central India");
        addMapping(map, "West India");
        addMapping(map, "South India");
        addMapping(map, "Jio India Central");
        addMapping(map, "Jio India West");
        addMapping(map, "Korea Central");
        addMapping(map, "Korea South");
        addMapping(map, "New Zealand North");
        addMapping(map, "Indonesia Central");
        addMapping(map, "Malaysia South");
        addMapping(map, "Malaysia West");
        addMapping(map, "Taiwan North");
        addMapping(map, "Taiwan Northwest");

        // Middle East & Africa
        addMapping(map, "UAE Central");
        addMapping(map, "UAE North");
        addMapping(map, "South Africa North");
        addMapping(map, "South Africa West");
        addMapping(map, "Qatar Central");
        addMapping(map, "Israel Central");
        addMapping(map, "Israel Northwest");
        addMapping(map, "Saudi Arabia East");

        // China
        addMapping(map, "China East");
        addMapping(map, "China East 2");
        addMapping(map, "China East 3");
        addMapping(map, "China North");
        addMapping(map, "China North 2");
        addMapping(map, "China North 3");

        // US Government
        addMapping(map, "USGov Virginia");
        addMapping(map, "USGov Iowa");
        addMapping(map, "USGov Arizona");
        addMapping(map, "USGov Texas");
        addMapping(map, "USDoD Central");
        addMapping(map, "USDoD East");
        addMapping(map, "USNat East");
        addMapping(map, "USNat West");
        addMapping(map, "USSec East");
        addMapping(map, "USSec West");
        addMapping(map, "USSec West Central");

        // EUAP / Canary
        addMapping(map, "Central US EUAP");
        addMapping(map, "East US 2 EUAP");

        NORMALIZED_TO_CANONICAL = Collections.unmodifiableMap(map);
    }

    private RegionNameMapper() {
    }

    /**
     * Normalizes a region name to the canonical CosmosDB format.
     * <p>
     * Strips spaces, lowercases, and looks up in the known-region map.
     * If recognized, returns the canonical form (e.g., "West US 3").
     * If not recognized, returns the input as-is for forward compatibility.
     *
     * @param regionName the region name to normalize (any casing/spacing variant)
     * @return the canonical CosmosDB region name, or the original input if unrecognized
     */
    public static String getCosmosDBRegionName(String regionName) {
        if (StringUtils.isEmpty(regionName)) {
            return regionName;
        }

        String normalized = regionName.toLowerCase(Locale.ROOT).replace(" ", "");
        return NORMALIZED_TO_CANONICAL.getOrDefault(normalized, regionName);
    }

    private static void addMapping(Map<String, String> map, String canonicalName) {
        String key = canonicalName.toLowerCase(Locale.ROOT).replace(" ", "");
        map.putIfAbsent(key, canonicalName);
    }
}
