// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.routing;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Single source of truth for Azure region name mappings in the Cosmos Java SDK.
 * <p>
 * Provides two capabilities:
 * <ol>
 *   <li><b>Region ID mapping</b> — canonical name ↔ numeric ID for session token region-level progress tracking.
 *       Must stay in sync with the authoritative regionToIdMapping in
 *       <a href="https://msdata.visualstudio.com/CosmosDB/_git/CosmosDB?path=/Product/Services/Documents/ImageStore/Storage/SingleServiceMasterServerApplication/ServerServicePackage/Settings.xml">Settings.xml</a>.</li>
 *   <li><b>Region name normalization</b> — maps any user-supplied variant ("westus3", "west us 3", "WEST US 3")
 *       to the canonical CosmosDB format ("West US 3"). Unknown regions not in the static map are
 *       returned as-is.</li>
 * </ol>
 */
public class RegionUtils {

    // ========================================================================
    // Region ID mappings — used only for session token region-level progress
    // tracking (localLsn). Must stay in sync with the authoritative
    // regionToIdMapping in Settings.xml:
    // https://msdata.visualstudio.com/CosmosDB/_git/CosmosDB?path=/Product/Services/Documents/ImageStore/Storage/SingleServiceMasterServerApplication/ServerServicePackage/Settings.xml
    // This is a SUBSET of all known regions — only regions with assigned IDs.
    // ========================================================================

    public static final Map<String, Integer> REGION_NAME_TO_REGION_ID_MAPPINGS = Collections.unmodifiableMap(new HashMap<String, Integer>() {
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
            put("Brazil Southeast", 74);
            put("Brazil Northeast", 75);
            put("Chile Central", 76);
            put("West US 3", 77);
            put("Jio India West", 78);
            put("Jio India Central", 79);
            put("Qatar Central", 80);
            put("Israel Central", 81);
            put("Mexico Central", 82);
            put("Spain Central", 83);
            put("Taiwan North", 84);
            put("Singapore Gov", 85);
            put("Poland Central", 86);
            put("Chile North Central", 87);
            put("USSec Central", 88);
            put("Malaysia West", 89);
            put("New Zealand North", 90);
            put("Italy North", 91);
            put("East US SLV", 92);
            put("China North 3", 93);
            put("China East 3", 94);
            put("Austria East", 95);
            put("Taiwan Northwest", 96);
            put("Belgium Central", 97);
            put("Malaysia South", 98);
            put("India South Central", 99);
            put("Indonesia Central", 100);
            put("Finland Central", 101);
            put("Israel Northwest", 102);
            put("Denmark East", 103);
            put("Southeast US", 104);
            put("Ocave", 105);
            put("Arlem", 106);
            put("Bleu France Central", 107);
            put("Bleu France South", 108);
            put("Delos Cloud Germany Central", 109);
            put("Delos Cloud Germany North", 110);
            put("Singapore Central", 111);
            put("Singapore North", 112);
            put("USSec West Central", 113);
            put("South Central US 2", 114);
            put("Southwest US", 115);
            put("East US 3", 116);
            put("Southeast US 3", 117);
            put("USNat North", 118);
            put("Southeast US 5", 119);
            put("Saudi Arabia East", 120);
            put("West Central US FRE", 121);
            put("Northeast US 5", 122);
            put("Southeast Asia 3", 123);
            put("North Europe 3", 124);
        }
    });

    public static final Map<Integer, String> REGION_ID_TO_NORMALIZED_REGION_NAME_MAPPINGS;

    public static final Map<String, Integer> NORMALIZED_REGION_NAME_TO_REGION_ID_MAPPINGS;

    static {
        // Derive both maps programmatically from REGION_NAME_TO_REGION_ID_MAPPINGS
        Map<Integer, String> idToNormalized = new HashMap<>();
        Map<String, Integer> normalizedToId = new HashMap<>();
        for (Map.Entry<String, Integer> entry : REGION_NAME_TO_REGION_ID_MAPPINGS.entrySet()) {
            String normalized = entry.getKey().toLowerCase(Locale.ROOT).replace(" ", "");
            normalizedToId.put(normalized, entry.getValue());
            idToNormalized.putIfAbsent(entry.getValue(), normalized);
        }
        NORMALIZED_REGION_NAME_TO_REGION_ID_MAPPINGS = Collections.unmodifiableMap(normalizedToId);
        REGION_ID_TO_NORMALIZED_REGION_NAME_MAPPINGS = Collections.unmodifiableMap(idToNormalized);
    }

    public static String getRegionName(int regionId) {
        return REGION_ID_TO_NORMALIZED_REGION_NAME_MAPPINGS.getOrDefault(regionId, StringUtils.EMPTY);
    }

    public static int getRegionId(String regionName) {
        return NORMALIZED_REGION_NAME_TO_REGION_ID_MAPPINGS.getOrDefault(regionName, -1);
    }

    // ========================================================================
    // Region name normalization — canonical names derived from the ID map
    // (sourced from Settings.xml regionToIdMapping). Used for normalizing
    // user-supplied preferred regions and excluded regions to the canonical
    // CosmosDB format. Unknown regions not in this map are passed through as-is.
    // ========================================================================

    // Static map: lowercase-no-spaces key → canonical display name
    private static final Map<String, String> NORMALIZED_TO_CANONICAL;

    static {
        Map<String, String> map = new HashMap<>();

        // Seed from the ID map — Settings.xml is the authoritative source for all
        // canonical region names. Every region with an assigned ID is automatically
        // included in the normalization map.
        for (String canonicalName : REGION_NAME_TO_REGION_ID_MAPPINGS.keySet()) {
            addCanonicalMapping(map, canonicalName);
        }

        NORMALIZED_TO_CANONICAL = Collections.unmodifiableMap(map);
    }

    /**
     * Normalizes a region name to the canonical CosmosDB format.
     * <p>
     * Strips spaces, lowercases, and looks up in the static known-region map.
     * If recognized, returns the canonical form (e.g., "West US 3").
     * If not recognized, returns the normalized form (lowercase, no spaces)
     * for forward compatibility — this ensures unknown regions still match
     * after LocationCache applies toLowerCase() to server-returned names.
     *
     * @param regionName the region name to normalize (any casing/spacing variant)
     * @return the canonical CosmosDB region name, or the lowercase space-stripped
     *         form if unrecognized
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

        return normalized;
    }

    /**
     * Normalizes a list of region names to canonical CosmosDB format.
     * Unknown regions not in the static map are passed through as-is.
     *
     * @param regionNames the list of region names to normalize
     * @return a new list with each region normalized
     */
    public static List<String> normalizeRegionNames(List<String> regionNames) {
        if (regionNames == null || regionNames.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> normalized = new ArrayList<>(regionNames.size());
        for (String region : regionNames) {
            if (region != null) {
                normalized.add(getCosmosDBRegionName(region));
            }
        }
        return normalized;
    }

    /**
     * Checks whether a list of region names contains the target region,
     * using canonical normalization + case-insensitive comparison.
     *
     * @param regions the list of region names to search
     * @param target the target region name to find
     * @return true if any region in the list matches the target after normalization
     */
    public static boolean containsRegionIgnoreCase(List<String> regions, String target) {
        if (regions == null || regions.isEmpty()) {
            return false;
        }
        String normalizedTarget = getCosmosDBRegionName(target);
        for (String region : regions) {
            if (region != null && getCosmosDBRegionName(region).equalsIgnoreCase(normalizedTarget)) {
                return true;
            }
        }
        return false;
    }

    private static void addCanonicalMapping(Map<String, String> map, String canonicalName) {
        String key = canonicalName.toLowerCase(Locale.ROOT).replace(" ", "");
        map.putIfAbsent(key, canonicalName);
    }
}
