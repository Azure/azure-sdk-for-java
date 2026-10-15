// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.routing;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Authoritative region-ID registry for the Cosmos Java SDK.
 * <p>
 * This is the <b>only</b> place that depends on the compiled-in region table. It is intentionally
 * scoped to the operations that fundamentally cannot work without an SDK-side region inventory:
 * <ul>
 *   <li>{@link #getRegionId(String)} — map a region name to its numeric ID (used by
 *       {@code PartitionScopedRegionLevelProgress} and {@code RxDocumentClientImpl}).</li>
 *   <li>{@link #getNormalizedRegionNameForId(int)} — reverse lookup for diagnostics and
 *       progress-tracking (used by {@code PartitionScopedRegionLevelProgress}).</li>
 * </ul>
 * <p>
 * For ID-free operations — comparing two region-name strings regardless of casing / separators —
 * use {@link RegionNameNormalizer} instead. Doing so keeps endpoint resolution decoupled from the
 * SDK's compile-time region inventory, so a region the SDK has not yet learned about still routes
 * correctly.
 * <p>
 * The static map must stay in sync with the authoritative regionToIdMapping in
 * <a href="https://msdata.visualstudio.com/CosmosDB/_git/CosmosDB?path=/Product/Services/Documents/ImageStore/Storage/SingleServiceMasterServerApplication/ServerServicePackage/Settings.xml">Settings.xml</a>.
 */
public final class RegionIdRegistry {

    /**
     * Canonical region name → numeric region ID.
     * Subset of all known Azure regions — only those with an assigned region ID.
     */
    public static final Map<String, Integer> CANONICAL_REGION_NAME_TO_REGION_ID_MAPPINGS;

    /** Normalized name → region ID (e.g., "westus3" → 77). */
    public static final Map<String, Integer> NORMALIZED_REGION_NAME_TO_REGION_ID_MAPPINGS;

    /** Region ID → normalized name (e.g., 77 → "westus3"). */
    public static final Map<Integer, String> REGION_ID_TO_NORMALIZED_REGION_NAME_MAPPINGS;

    static {
        Map<String, Integer> canonicalToId = new HashMap<>();
        canonicalToId.put("East US", 1);
        canonicalToId.put("East US 2", 2);
        canonicalToId.put("Central US", 3);
        canonicalToId.put("North Central US", 4);
        canonicalToId.put("South Central US", 5);
        canonicalToId.put("West Central US", 6);
        canonicalToId.put("West US", 7);
        canonicalToId.put("West US 2", 8);
        canonicalToId.put("Canada East", 9);
        canonicalToId.put("Canada Central", 10);
        canonicalToId.put("Brazil South", 11);
        canonicalToId.put("North Europe", 12);
        canonicalToId.put("West Europe", 13);
        canonicalToId.put("France Central", 14);
        canonicalToId.put("France South", 15);
        canonicalToId.put("UK West", 16);
        canonicalToId.put("UK South", 17);
        canonicalToId.put("Germany Central", 18);
        canonicalToId.put("Germany Northeast", 19);
        canonicalToId.put("Germany North", 20);
        canonicalToId.put("Germany West Central", 21);
        canonicalToId.put("Switzerland North", 22);
        canonicalToId.put("Switzerland West", 23);
        canonicalToId.put("Southeast Asia", 24);
        canonicalToId.put("East Asia", 25);
        canonicalToId.put("Australia East", 26);
        canonicalToId.put("Australia Southeast", 27);
        canonicalToId.put("Australia Central", 28);
        canonicalToId.put("Australia Central 2", 29);
        canonicalToId.put("China East", 30);
        canonicalToId.put("China North", 31);
        canonicalToId.put("Central India", 32);
        canonicalToId.put("West India", 33);
        canonicalToId.put("South India", 34);
        canonicalToId.put("Japan East", 35);
        canonicalToId.put("Japan West", 36);
        canonicalToId.put("Korea Central", 37);
        canonicalToId.put("Korea South", 38);
        canonicalToId.put("USGov Virginia", 39);
        canonicalToId.put("USGov Iowa", 40);
        canonicalToId.put("USGov Arizona", 41);
        canonicalToId.put("USGov Texas", 42);
        canonicalToId.put("USDoD East", 43);
        canonicalToId.put("USDoD Central", 44);
        canonicalToId.put("USSec East", 45);
        canonicalToId.put("USSec West", 46);
        canonicalToId.put("South Africa West", 47);
        canonicalToId.put("South Africa North", 48);
        canonicalToId.put("UAE Central", 49);
        canonicalToId.put("UAE North", 50);
        canonicalToId.put("Central US EUAP", 51);
        canonicalToId.put("East US 2 EUAP", 52);
        canonicalToId.put("North Europe 2", 53);
        canonicalToId.put("East Europe", 54);
        canonicalToId.put("APAC Southeast 2", 55);
        canonicalToId.put("UK South 2", 56);
        canonicalToId.put("UK North", 57);
        canonicalToId.put("East US STG", 58);
        canonicalToId.put("South Central US STG", 59);
        canonicalToId.put("Norway East", 60);
        canonicalToId.put("Norway West", 61);
        canonicalToId.put("USGov Wyoming", 62);
        canonicalToId.put("USDoD Southwest", 63);
        canonicalToId.put("USDoD West Central", 64);
        canonicalToId.put("USDoD South Central", 65);
        canonicalToId.put("China East 2", 66);
        canonicalToId.put("China North 2", 67);
        canonicalToId.put("USNat East", 68);
        canonicalToId.put("USNat West", 69);
        canonicalToId.put("China North 10", 70);
        canonicalToId.put("Sweden Central", 71);
        canonicalToId.put("Sweden South", 72);
        canonicalToId.put("Korea South 2", 73);
        canonicalToId.put("Brazil Southeast", 74);
        canonicalToId.put("Brazil Northeast", 75);
        canonicalToId.put("Chile Central", 76);
        canonicalToId.put("West US 3", 77);
        canonicalToId.put("Jio India West", 78);
        canonicalToId.put("Jio India Central", 79);
        canonicalToId.put("Qatar Central", 80);
        canonicalToId.put("Israel Central", 81);
        canonicalToId.put("Mexico Central", 82);
        canonicalToId.put("Spain Central", 83);
        canonicalToId.put("Taiwan North", 84);
        canonicalToId.put("Singapore Gov", 85);
        canonicalToId.put("Poland Central", 86);
        canonicalToId.put("Chile North Central", 87);
        canonicalToId.put("USSec Central", 88);
        canonicalToId.put("Malaysia West", 89);
        canonicalToId.put("New Zealand North", 90);
        canonicalToId.put("Italy North", 91);
        canonicalToId.put("East US SLV", 92);
        canonicalToId.put("China North 3", 93);
        canonicalToId.put("China East 3", 94);
        canonicalToId.put("Austria East", 95);
        canonicalToId.put("Taiwan Northwest", 96);
        canonicalToId.put("Belgium Central", 97);
        canonicalToId.put("Malaysia South", 98);
        canonicalToId.put("India South Central", 99);
        canonicalToId.put("Indonesia Central", 100);
        canonicalToId.put("Finland Central", 101);
        canonicalToId.put("Israel Northwest", 102);
        canonicalToId.put("Denmark East", 103);
        canonicalToId.put("Southeast US", 104);
        canonicalToId.put("Ocave", 105);
        canonicalToId.put("Arlem", 106);
        canonicalToId.put("Bleu France Central", 107);
        canonicalToId.put("Bleu France South", 108);
        canonicalToId.put("Delos Cloud Germany Central", 109);
        canonicalToId.put("Delos Cloud Germany North", 110);
        canonicalToId.put("Singapore Central", 111);
        canonicalToId.put("Singapore North", 112);
        canonicalToId.put("USSec West Central", 113);
        canonicalToId.put("South Central US 2", 114);
        canonicalToId.put("Southwest US", 115);
        canonicalToId.put("East US 3", 116);
        canonicalToId.put("Southeast US 3", 117);
        canonicalToId.put("USNat North", 118);
        canonicalToId.put("Southeast US 5", 119);
        canonicalToId.put("Saudi Arabia East", 120);
        canonicalToId.put("West Central US FRE", 121);
        canonicalToId.put("Northeast US 5", 122);
        canonicalToId.put("Southeast Asia 3", 123);
        canonicalToId.put("North Europe 3", 124);

        CANONICAL_REGION_NAME_TO_REGION_ID_MAPPINGS = Collections.unmodifiableMap(canonicalToId);

        // Derive the normalized-name maps from the canonical table.
        Map<String, Integer> normalizedToId = new HashMap<>(canonicalToId.size());
        Map<Integer, String> idToNormalized = new HashMap<>(canonicalToId.size());
        for (Map.Entry<String, Integer> entry : canonicalToId.entrySet()) {
            String normalizedName = RegionNameNormalizer.normalize(entry.getKey());
            if (normalizedToId.put(normalizedName, entry.getValue()) != null) {
                throw new IllegalStateException("Duplicate normalized region name '" + normalizedName
                    + "' in CANONICAL_REGION_NAME_TO_REGION_ID_MAPPINGS");
            }
            if (idToNormalized.put(entry.getValue(), normalizedName) != null) {
                throw new IllegalStateException("Duplicate region ID " + entry.getValue()
                    + " in CANONICAL_REGION_NAME_TO_REGION_ID_MAPPINGS");
            }
        }
        NORMALIZED_REGION_NAME_TO_REGION_ID_MAPPINGS = Collections.unmodifiableMap(normalizedToId);
        REGION_ID_TO_NORMALIZED_REGION_NAME_MAPPINGS = Collections.unmodifiableMap(idToNormalized);
    }

    private RegionIdRegistry() {
    }

    /**
     * Returns the region ID for a region name (any format accepted), or {@code -1} if the
     * region is not in the SDK's registry.
     * <p>
     * Fast path: tries the raw key first (zero allocations when input is already normalized).
     * Slow path: normalizes via {@link RegionNameNormalizer#normalize(String)} and retries.
     */
    public static int getRegionId(String regionName) {
        if (StringUtils.isEmpty(regionName)) {
            return -1;
        }
        Integer id = NORMALIZED_REGION_NAME_TO_REGION_ID_MAPPINGS.get(regionName);
        if (id != null) {
            return id;
        }
        String normalizedName = RegionNameNormalizer.normalize(regionName);
        Integer fallback = NORMALIZED_REGION_NAME_TO_REGION_ID_MAPPINGS.get(normalizedName);
        return fallback != null ? fallback : -1;
    }

    /**
     * Returns the normalized name for a region ID, or {@code ""} if the ID is not in the
     * SDK's registry.
     */
    public static String getNormalizedRegionNameForId(int regionId) {
        return REGION_ID_TO_NORMALIZED_REGION_NAME_MAPPINGS.getOrDefault(regionId, StringUtils.EMPTY);
    }
}
