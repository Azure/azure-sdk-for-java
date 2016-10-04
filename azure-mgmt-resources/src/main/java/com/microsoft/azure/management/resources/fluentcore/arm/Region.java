/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.arm;

/**
 * Enumeration of the Azure datacenter regions. See https://azure.microsoft.com/regions/
 */
public enum Region {
    // CHECKSTYLE IGNORE Javadoc FOR NEXT 21 LINES
    US_WEST("westus", "West US"),
    US_WEST2("westus2", "West US 2"),
    US_CENTRAL("centralus", "Central US"),
    US_EAST("eastus", "East US"),
    US_EAST2("eastus2", "East US 2"),
    US_NORTH_CENTRAL("northcentralus", "North Central US"),
    US_SOUTH_CENTRAL("southcentralus", "South Central US"),
    EUROPE_NORTH("northeurope", "North Europe"),
    EUROPE_WEST("westeurope", "West Europe"),
    ASIA_EAST("eastasia",  "East Asia"),
    ASIA_SOUTHEAST("southeastasia", "South East Asia"),
    JAPAN_EAST("japaneast", "Japan East"),
    JAPAN_WEST("japanwest", "Japan West"),
    BRAZIL_SOUTH("brazilsouth", "Brazil South"),
    AUSTRALIA_EAST("australiaeast", "Australia East"),
    AUSTRALIA_SOUTHEAST("australiasoutheast", "Australia Southeast"),
    INDIA_CENTRAL("centralindia", "Central India"),
    INDIA_SOUTH("southindia", "South India"),
    INDIA_WEST("westindia", "West India"),
    CHINA_NORTH("chinanorth", "China North"),
    CHINA_EAST("chinaeast", "China East");

    private final String name;
    private final String label;

    Region(String name, String label) {
        this.name = name;
        this.label = label;
    }

    @Override
    public String toString() {
        return this.name;
    }

    /**
     * Parses a label into a Region object.
     *
     * @param label the region label
     * @return the parsed region or null if there's no such region
     */
    public static Region fromLabel(String label) {
        for (Region region : Region.values()) {
            if (region.label.equalsIgnoreCase(label)) {
                return region;
            }
        }
        return null;
    }

    /**
     * Parses a name into a Region object.
     *
     * @param name the region name
     * @return the parsed region or null if there's no such region
     */
    public static Region fromName(String name) {
        for (Region region : Region.values()) {
            if (region.name.equalsIgnoreCase(name)) {
                return region;
            }
        }
        return null;
    }
}
