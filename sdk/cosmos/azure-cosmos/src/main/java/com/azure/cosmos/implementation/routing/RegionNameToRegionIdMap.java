// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.routing;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Single source of truth for Azure region name mappings in the Cosmos Java SDK.
 * <p>
 * Provides three capabilities:
 * <ol>
 *   <li><b>Region ID mapping</b> — canonical name ↔ numeric ID for session token region-level progress tracking.
 *       Must stay in sync with
 *       <a href="https://msdata.visualstudio.com/CosmosDB/_git/CosmosDB?path=%2FProduct%2FCosmos%2FCosmosFabric%2FBackend%2FCommon%2FRegionToIdMap.cs&amp;version=GBmaster">RegionToIdMap.cs</a>.</li>
 *   <li><b>Region name normalization</b> — maps any user-supplied variant ("westus3", "west us 3", "WEST US 3")
 *       to the canonical CosmosDB format ("West US 3").</li>
 *   <li><b>Dynamic registration</b> — learns new canonical names from server responses at runtime, so regions
 *       not yet in the static list can still be normalized after the first account read.</li>
 * </ol>
 */
public class RegionNameToRegionIdMap {

    // ========================================================================
    // Region ID mappings (synced with backend RegionToIdMap.cs)
    // ========================================================================

    public static final Map<String, Integer> REGION_NAME_TO_REGION_ID_MAPPINGS = new HashMap<String, Integer>() {
        {
            put("East US", 1);
            put("East US 2", 2);
            put("Central US", 3);
            put("North Central US", 4);
            put("South Central US", 5);
            put("West Central US", 6);
            put("West US", 7);
            put("West US 2", 8);
            put("Canada East", 9);
            put("Canada Central", 10);
            put("Brazil South", 11);
            put("North Europe", 12);
            put("West Europe", 13);
            put("France Central", 14);
            put("France South", 15);
            put("UK West", 16);
            put("UK South", 17);
            put("Germany Central", 18);
            put("Germany Northeast", 19);
            put("Germany North", 20);
            put("Germany West Central", 21);
            put("Switzerland North", 22);
            put("Switzerland West", 23);
            put("Southeast Asia", 24);
            put("East Asia", 25);
            put("Australia East", 26);
            put("Australia Southeast", 27);
            put("Australia Central", 28);
            put("Australia Central 2", 29);
            put("China East", 30);
            put("China North", 31);
            put("Central India", 32);
            put("West India", 33);
            put("South India", 34);
            put("Japan East", 35);
            put("Japan West", 36);
            put("Korea Central", 37);
            put("Korea South", 38);
            put("USGov Virginia", 39);
            put("USGov Iowa", 40);
            put("USGov Arizona", 41);
            put("USGov Texas", 42);
            put("USDoD East", 43);
            put("USDoD Central", 44);
            put("USSec East", 45);
            put("USSec West", 46);
            put("South Africa West", 47);
            put("South Africa North", 48);
            put("UAE Central", 49);
            put("UAE North", 50);
            put("Central US EUAP", 51);
            put("East US 2 EUAP", 52);
            put("North Europe 2", 53);
            put("East Europe", 54);
            put("APAC Southeast 2", 55);
            put("UK South 2", 56);
            put("UK North", 57);
            put("East US STG", 58);
            put("South Central US STG", 59);
            put("Norway East", 60);
            put("Norway West", 61);
            put("USGov Wyoming", 62);
            put("USDoD Southwest", 63);
            put("USDoD West Central", 64);
            put("USDoD South Central", 65);
            put("China East 2", 66);
            put("China North 2", 67);
            put("USNat East", 68);
            put("USNat West", 69);
            put("China North 10", 70);
            put("Sweden Central", 71);
            put("Sweden South", 72);
            put("Korea South 2", 73);
            put("Bleu France Central", 107);
            put("Bleu France South", 108);
            put("Delos Cloud Germany Central", 109);
            put("Delos Cloud Germany North", 110);
            put("Singapore Central", 111);
            put("Singapore North", 112);
            put("USSec West Central", 113);
        }
    };

    public static final Map<Integer, String> REGION_ID_TO_NORMALIZED_REGION_NAME_MAPPINGS = new HashMap<Integer, String>() {
        {
            put(1, "eastus");
            put(2, "eastus2");
            put(3, "centralus");
            put(4, "northcentralus");
            put(5, "southcentralus");
            put(6, "westcentralus");
            put(7, "westus");
            put(8, "westus2");
            put(9, "canadaeast");
            put(10, "canadacentral");
            put(11, "brazilsouth");
            put(12, "northeurope");
            put(13, "westeurope");
            put(14, "francecentral");
            put(15, "francesouth");
            put(16, "ukwest");
            put(17, "uksouth");
            put(18, "germanycentral");
            put(19, "germanynortheast");
            put(20, "germanynorth");
            put(21, "germanywestcentral");
            put(22, "switzerlandnorth");
            put(23, "switzerlandwest");
            put(24, "southeastasia");
            put(25, "eastasia");
            put(26, "australiaeast");
            put(27, "australiasoutheast");
            put(28, "australiacentral");
            put(29, "australiacentral2");
            put(30, "chinaeast");
            put(31, "chinanorth");
            put(32, "centralindia");
            put(33, "westindia");
            put(34, "southindia");
            put(35, "japaneast");
            put(36, "japanwest");
            put(37, "koreacentral");
            put(38, "koreasouth");
            put(39, "usgovvirginia");
            put(40, "usgoviowa");
            put(41, "usgovarizona");
            put(42, "usgovtexas");
            put(43, "usdodeast");
            put(44, "usdodcentral");
            put(45, "usseceast");
            put(46, "ussecwest");
            put(47, "southafricawest");
            put(48, "southafricanorth");
            put(49, "uaecentral");
            put(50, "uaenorth");
            put(51, "centraluseuap");
            put(52, "eastus2euap");
            put(53, "northeurope2");
            put(54, "easteurope");
            put(55, "apacsoutheast2");
            put(56, "uksouth2");
            put(57, "uknorth");
            put(58, "eastusstg");
            put(59, "southcentralusstg");
            put(60, "norwayeast");
            put(61, "norwaywest");
            put(62, "usgovwyoming");
            put(63, "usdodsouthwest");
            put(64, "usdodwestcentral");
            put(65, "usdodsouthcentral");
            put(66, "chinaeast2");
            put(67, "chinanorth2");
            put(68, "usnateast");
            put(69, "usnatwest");
            put(70, "chinanorth10");
            put(71, "swedencentral");
            put(72, "swedensouth");
            put(73, "koreasouth2");
            put(107, "bleufrancecentral");
            put(108, "bleufrancesouth");
            put(109, "deloscloudgermanycentral");
            put(110, "deloscloudgermanynorth");
            put(111, "singaporecentral");
            put(112, "singaporenorth");
            put(113, "ussecwestcentral");
        }
    };

    public static final Map<String, Integer> NORMALIZED_REGION_NAME_TO_REGION_ID_MAPPINGS;

    static {
        // Build normalized→ID map from REGION_NAME_TO_REGION_ID_MAPPINGS
        Map<String, Integer> normalizedMap = new HashMap<>();
        for (Map.Entry<String, Integer> entry : REGION_NAME_TO_REGION_ID_MAPPINGS.entrySet()) {
            String normalized = entry.getKey().toLowerCase(Locale.ROOT).replace(" ", "");
            normalizedMap.put(normalized, entry.getValue());
        }
        NORMALIZED_REGION_NAME_TO_REGION_ID_MAPPINGS = Collections.unmodifiableMap(normalizedMap);
    }

    public static String getRegionName(int regionId) {
        return REGION_ID_TO_NORMALIZED_REGION_NAME_MAPPINGS.getOrDefault(regionId, StringUtils.EMPTY);
    }

    public static int getRegionId(String regionName) {
        return NORMALIZED_REGION_NAME_TO_REGION_ID_MAPPINGS.getOrDefault(regionName, -1);
    }

    // ========================================================================
    // Region name normalization (any variant → canonical CosmosDB format)
    // ========================================================================

    // Static map: lowercase-no-spaces key → canonical display name
    private static final Map<String, String> NORMALIZED_TO_CANONICAL;

    // Dynamic map: populated from server-returned DatabaseAccountLocation names
    // at runtime, so new regions not in the static list can still be normalized
    private static final ConcurrentHashMap<String, String> DYNAMIC_NORMALIZED_TO_CANONICAL = new ConcurrentHashMap<>();

    static {
        Map<String, String> map = new HashMap<>();

        // Seed from the ID map (all backend-known regions)
        for (String canonicalName : REGION_NAME_TO_REGION_ID_MAPPINGS.keySet()) {
            addCanonicalMapping(map, canonicalName);
        }

        // Additional regions that don't have IDs yet (from .NET SDK Regions.cs / Azure portal)
        addCanonicalMapping(map, "West US 3");
        addCanonicalMapping(map, "East US 3");
        addCanonicalMapping(map, "Brazil Southeast");
        addCanonicalMapping(map, "Mexico Central");
        addCanonicalMapping(map, "Chile Central");
        addCanonicalMapping(map, "Poland Central");
        addCanonicalMapping(map, "Italy North");
        addCanonicalMapping(map, "Spain Central");
        addCanonicalMapping(map, "Austria East");
        addCanonicalMapping(map, "Belgium Central");
        addCanonicalMapping(map, "Denmark East");
        addCanonicalMapping(map, "Finland Central");
        addCanonicalMapping(map, "Greece Central");
        addCanonicalMapping(map, "Jio India Central");
        addCanonicalMapping(map, "Jio India West");
        addCanonicalMapping(map, "New Zealand North");
        addCanonicalMapping(map, "Indonesia Central");
        addCanonicalMapping(map, "Malaysia South");
        addCanonicalMapping(map, "Malaysia West");
        addCanonicalMapping(map, "Taiwan North");
        addCanonicalMapping(map, "Taiwan Northwest");
        addCanonicalMapping(map, "Qatar Central");
        addCanonicalMapping(map, "Israel Central");
        addCanonicalMapping(map, "Israel Northwest");
        addCanonicalMapping(map, "Saudi Arabia East");
        addCanonicalMapping(map, "China East 3");
        addCanonicalMapping(map, "China North 3");

        NORMALIZED_TO_CANONICAL = Collections.unmodifiableMap(map);
    }

    /**
     * Normalizes a region name to the canonical CosmosDB format.
     * <p>
     * Strips spaces, lowercases, and looks up in both the static known-region map
     * and the dynamic map (populated from server responses). If recognized, returns
     * the canonical form (e.g., "West US 3"). If not recognized, returns the input as-is.
     *
     * @param regionName the region name to normalize (any casing/spacing variant)
     * @return the canonical CosmosDB region name, or the original input if unrecognized
     */
    public static String getCosmosDBRegionName(String regionName) {
        if (StringUtils.isEmpty(regionName)) {
            return regionName;
        }

        String normalized = regionName.toLowerCase(Locale.ROOT).replace(" ", "");

        String canonical = NORMALIZED_TO_CANONICAL.get(normalized);
        if (canonical != null) {
            return canonical;
        }

        canonical = DYNAMIC_NORMALIZED_TO_CANONICAL.get(normalized);
        if (canonical != null) {
            return canonical;
        }

        return regionName;
    }

    /**
     * Registers a canonical region name learned from a server response.
     * <p>
     * Called when processing {@code DatabaseAccountLocation} names from the account read response.
     * This ensures that new Azure regions (not yet in the static list) can still be normalized
     * correctly for subsequent preferred-region or exclude-region lookups.
     *
     * @param canonicalRegionName the canonical region name from the server (e.g., "West US 4")
     */
    public static void registerRegionName(String canonicalRegionName) {
        if (StringUtils.isEmpty(canonicalRegionName)) {
            return;
        }
        String key = canonicalRegionName.toLowerCase(Locale.ROOT).replace(" ", "");
        if (!NORMALIZED_TO_CANONICAL.containsKey(key)) {
            DYNAMIC_NORMALIZED_TO_CANONICAL.putIfAbsent(key, canonicalRegionName);
        }
    }

    private static void addCanonicalMapping(Map<String, String> map, String canonicalName) {
        String key = canonicalName.toLowerCase(Locale.ROOT).replace(" ", "");
        map.putIfAbsent(key, canonicalName);
    }
}
