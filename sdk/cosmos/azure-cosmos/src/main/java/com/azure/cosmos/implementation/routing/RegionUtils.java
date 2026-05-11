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
public final class RegionUtils {

    // ========================================================================
    // Region ID mappings — used only for session token region-level progress
    // tracking (localLsn). Must stay in sync with the authoritative
    // regionToIdMapping in Settings.xml:
    // https://msdata.visualstudio.com/CosmosDB/_git/CosmosDB?path=/Product/Services/Documents/ImageStore/Storage/SingleServiceMasterServerApplication/ServerServicePackage/Settings.xml
    // This is a SUBSET of all known regions — only regions with assigned IDs.
    // ========================================================================

    public static final Map<String, Integer> CANONICAL_REGION_NAME_TO_REGION_ID_MAPPINGS = Collections.unmodifiableMap(new HashMap<String, Integer>() {
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

    // ========================================================================
    // Derived maps — built from CANONICAL_REGION_NAME_TO_REGION_ID_MAPPINGS.
    //
    // Naming convention:
    //   "normalized"  = lowercase, no spaces (e.g., "westus3")
    //   "canonical"   = official display form (e.g., "West US 3")
    // ========================================================================

    /** Maps region ID → normalized name (e.g., 77 → "westus3"). */
    public static final Map<Integer, String> REGION_ID_TO_NORMALIZED_REGION_NAME_MAPPINGS;

    /** Maps normalized name → region ID (e.g., "westus3" → 77). */
    public static final Map<String, Integer> NORMALIZED_REGION_NAME_TO_REGION_ID_MAPPINGS;

    /** Maps normalized name → canonical display name (e.g., "westus3" → "West US 3"). */
    private static final Map<String, String> NORMALIZED_TO_CANONICAL;

    static {
        // Derive all maps programmatically from CANONICAL_REGION_NAME_TO_REGION_ID_MAPPINGS.
        // Keys in the source map are canonical names (e.g., "West US 3").
        Map<Integer, String> idToNormalized = new HashMap<>();
        Map<String, Integer> normalizedToId = new HashMap<>();
        Map<String, String> normalizedToCanonical = new HashMap<>();

        for (Map.Entry<String, Integer> entry : CANONICAL_REGION_NAME_TO_REGION_ID_MAPPINGS.entrySet()) {
            String canonicalName = entry.getKey();                                   // e.g., "West US 3"
            String normalizedName = canonicalName.toLowerCase(Locale.ROOT).replace(" ", ""); // e.g., "westus3"

            if (normalizedToId.put(normalizedName, entry.getValue()) != null) {
                throw new IllegalStateException("Duplicate normalized region name '" + normalizedName + "' in CANONICAL_REGION_NAME_TO_REGION_ID_MAPPINGS");
            }
            if (idToNormalized.put(entry.getValue(), normalizedName) != null) {
                throw new IllegalStateException("Duplicate region ID " + entry.getValue() + " in CANONICAL_REGION_NAME_TO_REGION_ID_MAPPINGS");
            }
            normalizedToCanonical.putIfAbsent(normalizedName, canonicalName);
        }

        NORMALIZED_REGION_NAME_TO_REGION_ID_MAPPINGS = Collections.unmodifiableMap(normalizedToId);
        REGION_ID_TO_NORMALIZED_REGION_NAME_MAPPINGS = Collections.unmodifiableMap(idToNormalized);
        NORMALIZED_TO_CANONICAL = Collections.unmodifiableMap(normalizedToCanonical);
    }

    private RegionUtils() {
    }

    /**
     * Returns the normalized name for a region ID.
     *
     * @param regionId the numeric region ID
     * @return the normalized name (e.g., "westus3"), or empty string if unknown
     */
    public static String getRegionName(int regionId) {
        return REGION_ID_TO_NORMALIZED_REGION_NAME_MAPPINGS.getOrDefault(regionId, StringUtils.EMPTY);
    }

    /**
     * Returns the region ID for a region name (any format accepted).
     * <p>
     * Fast path: tries the raw key first (zero allocations for already-normalized input).
     * Slow path: normalizes (lowercase + strip spaces) and retries.
     *
     * @param regionName the region name in any format (canonical, normalized, or raw)
     * @return the region ID, or -1 if not found
     */
    public static int getRegionId(String regionName) {
        if (StringUtils.isEmpty(regionName)) {
            return -1;
        }
        // Fast path: input is already in normalized form (e.g., "westus3")
        int id = NORMALIZED_REGION_NAME_TO_REGION_ID_MAPPINGS.getOrDefault(regionName, -1);
        if (id != -1) {
            return id;
        }
        // Slow path: normalize input to lowercase-no-spaces and retry
        String normalizedName = regionName.toLowerCase(Locale.ROOT).replace(" ", "");
        return NORMALIZED_REGION_NAME_TO_REGION_ID_MAPPINGS.getOrDefault(normalizedName, -1);
    }

    /**
     * Returns the canonical CosmosDB region name for any input variant.
     * <p>
     * Converts input to normalized form (lowercase, no spaces), then looks up
     * the canonical display name in the static map.
     * <ul>
     *   <li>Known region: returns canonical form (e.g., "westus3" → "West US 3")</li>
     *   <li>Unknown region: returns customer-passed string as-is</li>
     * </ul>
     *
     * @param regionName the region name in any format (e.g., "westus3", "WEST US 3", "West US 3")
     * @return the canonical region name (e.g., "West US 3"), or the input as-is if unrecognized
     */
    public static String getCanonicalRegionName(String regionName) {
        if (StringUtils.isEmpty(regionName)) {
            return regionName;
        }

        // Normalize to lowercase-no-spaces for map lookup
        String normalizedName = regionName.toLowerCase(Locale.ROOT).replace(" ", "");

        // Look up canonical display name
        String canonicalName = NORMALIZED_TO_CANONICAL.get(normalizedName);
        if (canonicalName != null) {
            return canonicalName;
        }

        // Unknown region — return customer-passed string as-is.
        // Downstream consumers apply toLowerCase() or equalsIgnoreCase() as needed.
        return regionName;
    }

    /**
     * Returns the normalized form of a region name: lowercase, no spaces.
     * <p>
     * Examples:
     * <ul>
     *   <li>"West US 3" → "westus3"</li>
     *   <li>"EAST US" → "eastus"</li>
     *   <li>"Future Region" → "futureregion"</li>
     * </ul>
     * Used by {@code LocationHelper} for constructing regional endpoint URLs.
     * DNS is case-insensitive, so casing doesn't affect resolution.
     *
     * @param regionName the region name in any format
     * @return the normalized form (lowercase, no spaces)
     */
    public static String getNormalizedRegionName(String regionName) {
        if (StringUtils.isEmpty(regionName)) {
            return regionName;
        }
        return regionName.toLowerCase(Locale.ROOT).replace(" ", "");
    }

    /**
     * Converts a list of region names to their canonical CosmosDB form.
     * <p>
     * Known regions are mapped to canonical names (e.g., "westus3" → "West US 3").
     * Unknown regions are passed through as-is. Null elements are dropped.
     *
     * @param regionNames the list of region names in any format
     * @return a new list with each region in canonical form
     */
    public static List<String> canonicalizeRegionNames(List<String> regionNames) {
        if (regionNames == null || regionNames.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> canonicalized = new ArrayList<>(regionNames.size());
        for (String region : regionNames) {
            if (region != null) {
                canonicalized.add(getCanonicalRegionName(region));
            }
        }
        return canonicalized;
    }

    /**
     * Checks whether a list of region names contains the target region,
     * using canonical normalization + case-insensitive comparison.
     * <p>
     * Both the list elements and the target are first canonicalized via
     * {@link #getCanonicalRegionName(String)}, then compared with
     * {@code equalsIgnoreCase}. This handles all format variants:
     * "westus3", "West US 3", "WEST US 3" all match each other.
     *
     * @param regions the list of region names to search (any format)
     * @param target the target region name to find (any format)
     * @return true if any region in the list matches the target after canonicalization
     */
    public static boolean containsRegionIgnoreCase(List<String> regions, String target) {
        if (regions == null || regions.isEmpty()) {
            return false;
        }
        String canonicalTarget = getCanonicalRegionName(target);
        for (String region : regions) {
            if (region != null && getCanonicalRegionName(region).equalsIgnoreCase(canonicalTarget)) {
                return true;
            }
        }
        return false;
    }
}
