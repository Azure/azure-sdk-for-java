// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.util.ExpandableStringEnum;

/**
 * Defines currently available regional authorities, or "AutoDiscoverRegion" to auto-detect the region.
 */
public final class RegionalAuthority extends ExpandableStringEnum<RegionalAuthority> {
    /**
     * In cases where the region is not known ahead of time, attempts to automatically discover the appropriate
     * regional authority. This works on some azure hosts, such as some VMs (through IDMS), and Azure Functions
     * (using host populated environment variables). If the auto-detection fails, the non-regional authority is
     * used.
     */
    public static final RegionalAuthority AUTO_DISCOVER_REGION = fromString("AutoDiscoverRegion");

    /**
     * The regional authority for the Azure "westus" region.
     */
    public static final RegionalAuthority US_WEST = fromString("westus");

    /**
     * The regional authority for the Azure "westus2" region.
     */
    public static final RegionalAuthority US_WEST2 = fromString("westus2");

    /**
     * The regional authority for the Azure "centralus" region.
     */
    public static final RegionalAuthority US_CENTRAL = fromString("centralus");

    /**
     * The regional authority for the Azure "eastus" region.
     */
    public static final RegionalAuthority US_EAST = fromString("eastus");

    /**
     * The regional authority for the Azure "eastus2" region.
     */
    public static final RegionalAuthority US_EAST2 = fromString("eastus2");

    /**
     * The regional authority for the Azure "northcentralus" region.
     */
    public static final RegionalAuthority US_NORTH_CENTRAL = fromString("northcentralus");

    /**
     * The regional authority for the Azure "southcentralus" region.
     */
    public static final RegionalAuthority US_SOUTH_CENTRAL = fromString("southcentralus");

    /**
     * The regional authority for the Azure "westcentralus" region.
     */
    public static final RegionalAuthority US_WEST_CENTRAL = fromString("westcentralus");

    /**
     * The regional authority for the Azure "canadacentral" region.
     */
    public static final RegionalAuthority CANADA_CENTRAL = fromString("canadacentral");

    /**
     * The regional authority for the Azure "canadaeast" region.
     */
    public static final RegionalAuthority CANADA_EAST = fromString("canadaeast");

    /**
     * The regional authority for the Azure "brazilsouth" region.
     */
    public static final RegionalAuthority BRAZIL_SOUTH = fromString("brazilsouth");

    /**
     * The regional authority for the Azure "northeurope" region.
     */
    public static final RegionalAuthority EUROPE_NORTH = fromString("northeurope");

    /**
     * The regional authority for the Azure "westeurope" region.
     */
    public static final RegionalAuthority EUROPE_WEST = fromString("westeurope");

    /**
     * The regional authority for the Azure "uksouth" region.
     */
    public static final RegionalAuthority UK_SOUTH = fromString("uksouth");

    /**
     * The regional authority for the Azure "ukwest" region.
     */
    public static final RegionalAuthority UK_WEST = fromString("ukwest");

    /**
     * The regional authority for the Azure "francecentral" region.
     */
    public static final RegionalAuthority FRANCE_CENTRAL = fromString("francecentral");

    /**
     * The regional authority for the Azure "francesouth" region.
     */
    public static final RegionalAuthority FRANCE_SOUTH = fromString("francesouth");

    /**
     * The regional authority for the Azure "switzerlandnorth" region.
     */
    public static final RegionalAuthority SWITZERLAND_NORTH = fromString("switzerlandnorth");

    /**
     * The regional authority for the Azure "switzerlandwest" region.
     */
    public static final RegionalAuthority SWITZERLAND_WEST = fromString("switzerlandwest");

    /**
     * The regional authority for the Azure "germanynorth" region.
     */
    public static final RegionalAuthority GERMANY_NORTH = fromString("germanynorth");

    /**
     * The regional authority for the Azure "germanywestcentral" region.
     */
    public static final RegionalAuthority GERMANY_WEST_CENTRAL = fromString("germanywestcentral");

    /**
     * The regional authority for the Azure "norwaywest" region.
     */
    public static final RegionalAuthority NORWAY_WEST = fromString("norwaywest");

    /**
     * The regional authority for the Azure "norwayeast" region.
     */
    public static final RegionalAuthority NORWAY_EAST = fromString("norwayeast");

    /**
     * The regional authority for the Azure "eastasia" region.
     */
    public static final RegionalAuthority ASIA_EAST = fromString("eastasia");

    /**
     * The regional authority for the Azure "southeastasia" region.
     */
    public static final RegionalAuthority ASIA_SOUTH_EAST = fromString("southeastasia");

    /**
     * The regional authority for the Azure "japaneast" region.
     */
    public static final RegionalAuthority JAPAN_EAST = fromString("japaneast");

    /**
     * The regional authority for the Azure "japanwest" region.
     */
    public static final RegionalAuthority JAPAN_WEST = fromString("japanwest");

    /**
     * The regional authority for the Azure "australiaeast" region.
     */
    public static final RegionalAuthority AUSTRALIA_EAST = fromString("australiaeast");

    /**
     * The regional authority for the Azure "australiasoutheast" region.
     */
    public static final RegionalAuthority AUSTRALIA_SOUTH_EAST = fromString("australiasoutheast");

    /**
     * The regional authority for the Azure "australiacentral" region.
     */
    public static final RegionalAuthority AUSTRALIA_CENTRAL = fromString("australiacentral");

    /**
     * The regional authority for the Azure "australiacentral2" region.
     */
    public static final RegionalAuthority AUSTRALIA_CENTRAL2 = fromString("australiacentral2");

    /**
     * The regional authority for the Azure "centralindia" region.
     */
    public static final RegionalAuthority INDIA_CENTRAL = fromString("centralindia");

    /**
     * The regional authority for the Azure "southindia" region.
     */
    public static final RegionalAuthority INDIA_SOUTH = fromString("southindia");

    /**
     * The regional authority for the Azure "westindia" region.
     */
    public static final RegionalAuthority INDIA_WEST = fromString("westindia");

    /**
     * The regional authority for the Azure "koreasouth" region.
     */
    public static final RegionalAuthority KOREA_SOUTH = fromString("koreasouth");

    /**
     * The regional authority for the Azure "koreacentral" region.
     */
    public static final RegionalAuthority KOREA_CENTRAL = fromString("koreacentral");

    /**
     * The regional authority for the Azure "uaecentral" region.
     */
    public static final RegionalAuthority UAE_CENTRAL = fromString("uaecentral");

    /**
     * The regional authority for the Azure "uaenorth" region.
     */
    public static final RegionalAuthority UAE_NORTH = fromString("uaenorth");

    /**
     * The regional authority for the Azure "southafricanorth" region.
     */
    public static final RegionalAuthority SOUTH_AFRICA_NORTH = fromString("southafricanorth");

    /**
     * The regional authority for the Azure "southafricawest" region.
     */
    public static final RegionalAuthority SOUTH_AFRICA_WEST = fromString("southafricawest");

    /**
     * The regional authority for the Azure "chinanorth" region.
     */
    public static final RegionalAuthority CHINA_NORTH = fromString("chinanorth");

    /**
     * The regional authority for the Azure "chinaeast" region.
     */
    public static final RegionalAuthority CHINA_EAST = fromString("chinaeast");

    /**
     * The regional authority for the Azure "chinanorth2" region.
     */
    public static final RegionalAuthority CHINA_NORTH2 = fromString("chinanorth2");

    /**
     * The regional authority for the Azure "chinaeast2" region.
     */
    public static final RegionalAuthority CHINA_EAST2 = fromString("chinaeast2");

    /**
     * The regional authority for the Azure "germanycentral" region.
     */
    public static final RegionalAuthority GERMANY_CENTRAL = fromString("germanycentral");

    /**
     * The regional authority for the Azure "germanynortheast" region.
     */
    public static final RegionalAuthority GERMANY_NORTH_EAST = fromString("germanynortheast");

    /**
     * The regional authority for the Azure "usgovvirginia" region.
     */
    public static final RegionalAuthority GOVERNMENT_US_VIRGINIA = fromString("usgovvirginia");

    /**
     * The regional authority for the Azure "usgoviowa" region.
     */
    public static final RegionalAuthority GOVERNMENT_US_IOWA = fromString("usgoviowa");

    /**
     * The regional authority for the Azure "usgovarizona" region.
     */
    public static final RegionalAuthority GOVERNMENT_US_ARIZONA = fromString("usgovarizona");

    /**
     * The regional authority for the Azure "usgovtexas" region.
     */
    public static final RegionalAuthority GOVERNMENT_US_TEXAS = fromString("usgovtexas");

    /**
     * The regional authority for the Azure "usdodeast" region.
     */
    public static final RegionalAuthority GOVERNMENT_US_DOD_EAST = fromString("usdodeast");

    /**
     * The regional authority for the Azure "usdodcentral" region.
     */
    public static final RegionalAuthority GOVERNMENT_US_DOD_CENTRAL = fromString("usdodcentral");

    /**
     * Returns the {@link RegionalAuthority} associated with the name.
     * @param name The name of the regional authority.
     * @return The {@link RegionalAuthority} associated with this name.
     */
    public static RegionalAuthority fromString(String name) {
        return fromString(name, RegionalAuthority.class);
    }
}
