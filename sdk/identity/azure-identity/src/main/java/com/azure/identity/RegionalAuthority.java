// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.util.ExpandableStringEnum;

/**
 * Defines currently available regional authorities, or "AutoDiscoverRegion" to auto-detect the region.
 */
public class RegionalAuthority extends ExpandableStringEnum<RegionalAuthority> {
    public static final RegionalAuthority AUTO_DISCOVER_REGION = fromString("AutoDiscoverRegion");
    public static final RegionalAuthority US_WEST = fromString("westus");
    public static final RegionalAuthority US_WEST2 = fromString("westus2");
    public static final RegionalAuthority US_CENTRAL = fromString("centralus");
    public static final RegionalAuthority US_EAST = fromString("eastus");
    public static final RegionalAuthority US_EAST2 = fromString("eastus2");
    public static final RegionalAuthority US_NORTH_CENTRAL = fromString("northcentralus");
    public static final RegionalAuthority US_SOUTH_CENTRAL = fromString("southcentralus");
    public static final RegionalAuthority US_WEST_CENTRAL = fromString("westcentralus");
    public static final RegionalAuthority CANADA_CENTRAL = fromString("canadacentral");
    public static final RegionalAuthority CANADA_EAST = fromString("canadaeast");
    public static final RegionalAuthority BRAZIL_SOUTH = fromString("brazilsouth");
    public static final RegionalAuthority EUROPE_NORTH = fromString("northeurope");
    public static final RegionalAuthority EUROPE_WEST = fromString("westeurope");
    public static final RegionalAuthority UK_SOUTH = fromString("uksouth");
    public static final RegionalAuthority UK_WEST = fromString("ukwest");
    public static final RegionalAuthority FRANCE_CENTRAL = fromString("francecentral");
    public static final RegionalAuthority FRANCE_SOUTH = fromString("francesouth");
    public static final RegionalAuthority SWITZERLAND_NORTH = fromString("switzerlandnorth");
    public static final RegionalAuthority SWITZERLAND_WEST = fromString("switzerlandwest");
    public static final RegionalAuthority GERMANY_NORTH = fromString("germanynorth");
    public static final RegionalAuthority GERMANY_WEST_CENTRAL = fromString("germanywestcentral");
    public static final RegionalAuthority NORWAY_WEST = fromString("norwaywest");
    public static final RegionalAuthority NORWAY_EAST = fromString("norwayeast");
    public static final RegionalAuthority ASIA_EAST = fromString("eastasia");
    public static final RegionalAuthority ASIA_SOUTH_EAST = fromString("southeastasia");
    public static final RegionalAuthority JAPAN_EAST = fromString("japaneast");
    public static final RegionalAuthority JAPAN_WEST = fromString("japanwest");
    public static final RegionalAuthority AUSTRALIA_EAST = fromString("australiaeast");
    public static final RegionalAuthority AUSTRALIA_SOUTH_EAST = fromString("australiasoutheast");
    public static final RegionalAuthority AUSTRALIA_CENTRAL = fromString("australiacentral");
    public static final RegionalAuthority AUSTRALIA_CENTRAL2 = fromString("australiacentral2");
    public static final RegionalAuthority INDIA_CENTRAL = fromString("centralindia");
    public static final RegionalAuthority INDIA_SOUTH = fromString("southindia");
    public static final RegionalAuthority INDIA_WEST = fromString("westindia");
    public static final RegionalAuthority KOREA_SOUTH = fromString("koreasouth");
    public static final RegionalAuthority KOREA_CENTRAL = fromString("koreacentral");
    public static final RegionalAuthority UAE_CENTRAL = fromString("uaecentral");
    public static final RegionalAuthority UAE_NORTH = fromString("uaenorth");
    public static final RegionalAuthority SOUTH_AFRICA_NORTH = fromString("southafricanorth");
    public static final RegionalAuthority SOUTH_AFRICA_WEST = fromString("southafricawest");
    public static final RegionalAuthority CHINA_NORTH = fromString("chinanorth");
    public static final RegionalAuthority CHINA_EAST = fromString("chinaeast");
    public static final RegionalAuthority CHINA_NORTH2 = fromString("chinanorth2");
    public static final RegionalAuthority CHINA_EAST2 = fromString("chinaeast2");
    public static final RegionalAuthority GERMANY_CENTRAL = fromString("germanycentral");
    public static final RegionalAuthority GERMANY_NORTH_EAST = fromString("germanynortheast");
    public static final RegionalAuthority GOVERNMENT_US_VIRGINIA = fromString("usgovvirginia");
    public static final RegionalAuthority GOVERNMENT_US_IOWA = fromString("usgoviowa");
    public static final RegionalAuthority GOVERNMENT_US_ARIZONA = fromString("usgovarizona");
    public static final RegionalAuthority GOVERNMENT_US_TEXAS = fromString("usgovtexas");
    public static final RegionalAuthority GOVERNMENT_US_DOD_EAST = fromString("usdodeast");
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
