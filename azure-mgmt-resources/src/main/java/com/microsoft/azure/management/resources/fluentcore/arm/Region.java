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
    // CHECKSTYLE IGNORE Javadoc FOR NEXT 48 LINES
    /**************************************************
     * Azure Cloud - Americas
     **************************************************/
    US_WEST("westus", "West US"),
    US_WEST2("westus2", "West US 2"),
    US_CENTRAL("centralus", "Central US"),
    US_EAST("eastus", "East US"),
    US_EAST2("eastus2", "East US 2"),
    US_NORTH_CENTRAL("northcentralus", "North Central US"),
    US_SOUTH_CENTRAL("southcentralus", "South Central US"),
    US_WEST_CENTRAL("westcentralus", "West Central US"),
    CANADA_CENTRAL("canadacentral", "Canada Central"),
    CANADA_EAST("canadaeast", "Canada East"),
    BRAZIL_SOUTH("brazilsouth", "Brazil South"),
    /**************************************************
     * Azure Cloud - Europe
     **************************************************/
    EUROPE_NORTH("northeurope", "North Europe"),
    EUROPE_WEST("westeurope", "West Europe"),
    UK_SOUTH("uksouth", "UK South"),
    UK_WEST("ukwest", "UK West"),
    /**************************************************
     * Azure Cloud - Asia
     **************************************************/
    ASIA_EAST("eastasia",  "East Asia"),
    ASIA_SOUTHEAST("southeastasia", "South East Asia"),
    JAPAN_EAST("japaneast", "Japan East"),
    JAPAN_WEST("japanwest", "Japan West"),
    AUSTRALIA_EAST("australiaeast", "Australia East"),
    AUSTRALIA_SOUTHEAST("australiasoutheast", "Australia Southeast"),
    INDIA_CENTRAL("centralindia", "Central India"),
    INDIA_SOUTH("southindia", "South India"),
    INDIA_WEST("westindia", "West India"),
    /**************************************************
     * Azure China Cloud
     **************************************************/
    CHINA_NORTH("chinanorth", "China North"),
    CHINA_EAST("chinaeast", "China East"),
    /**************************************************
     * Azure German Cloud
     **************************************************/
    GERMANY_CENTRAL("germanycentral", "Germany Central"),
    GERMANY_NORTHEAST("germanynortheast", "Germany Northeast"),
    /**************************************************
     * Azure Government Cloud
     **************************************************/
    GOV_US_VIRGINIA("usgoveast", "US Gov Virginia"),
    GOV_US_IOWA("usgovcentral", "US Gov Iowa");

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
