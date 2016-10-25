/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.arm;

import com.fasterxml.jackson.annotation.JsonValue;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Enumeration of the Azure datacenter regions. See https://azure.microsoft.com/regions/
 */
public class Region {
    // CHECKSTYLE IGNORE Javadoc FOR NEXT 48 LINES
    /**************************************************
     * Azure Cloud - Americas
     **************************************************/
    public static final Region US_WEST = new Region("westus", "West US");
    public static final Region US_WEST2 = new Region("westus2", "West US 2");
    public static final Region US_CENTRAL = new Region("centralus", "Central US");
    public static final Region US_EAST = new Region("eastus", "East US");
    public static final Region US_EAST2 = new Region("eastus2", "East US 2");
    public static final Region US_NORTH_CENTRAL = new Region("northcentralus", "North Central US");
    public static final Region US_SOUTH_CENTRAL = new Region("southcentralus", "South Central US");
    public static final Region US_WEST_CENTRAL = new Region("westcentralus", "West Central US");
    public static final Region CANADA_CENTRAL = new Region("canadacentral", "Canada Central");
    public static final Region CANADA_EAST = new Region("canadaeast", "Canada East");
    public static final Region BRAZIL_SOUTH = new Region("brazilsouth", "Brazil South");
    /**************************************************
     * Azure Cloud - Europe
     **************************************************/
    public static final Region EUROPE_NORTH = new Region("northeurope", "North Europe");
    public static final Region EUROPE_WEST = new Region("westeurope", "West Europe");
    public static final Region UK_SOUTH = new Region("uksouth", "UK South");
    public static final Region UK_WEST = new Region("ukwest", "UK West");
    /**************************************************
     * Azure Cloud - Asia
     **************************************************/
    public static final Region ASIA_EAST = new Region("eastasia",  "East Asia");
    public static final Region ASIA_SOUTHEAST = new Region("southeastasia", "South East Asia");
    public static final Region JAPAN_EAST = new Region("japaneast", "Japan East");
    public static final Region JAPAN_WEST = new Region("japanwest", "Japan West");
    public static final Region AUSTRALIA_EAST = new Region("australiaeast", "Australia East");
    public static final Region AUSTRALIA_SOUTHEAST = new Region("australiasoutheast", "Australia Southeast");
    public static final Region INDIA_CENTRAL = new Region("centralindia", "Central India");
    public static final Region INDIA_SOUTH = new Region("southindia", "South India");
    public static final Region INDIA_WEST = new Region("westindia", "West India");
    /**************************************************
     * Azure China Cloud
     **************************************************/
    public static final Region CHINA_NORTH = new Region("chinanorth", "China North");
    public static final Region CHINA_EAST = new Region("chinaeast", "China East");
    /**************************************************
     * Azure German Cloud
     **************************************************/
    public static final Region GERMANY_CENTRAL = new Region("germanycentral", "Germany Central");
    public static final Region GERMANY_NORTHEAST = new Region("germanynortheast", "Germany Northeast");
    /**************************************************
     * Azure Government Cloud
     **************************************************/
    public static final Region GOV_US_VIRGINIA = new Region("usgovvirginia", "US Gov Virginia");
    public static final Region GOV_US_IOWA = new Region("usgoviowa", "US Gov Iowa");

    private static final Region[] VALUES;

    private final String name;
    private final String label;

    static {
        Field[] declaredFields = Region.class.getDeclaredFields();
        List<Region> values = new ArrayList<>();
        for (Field field : declaredFields) {
            if (Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers())) {
                field.setAccessible(true);
                try {
                    values.add((Region) field.get(null));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        VALUES = values.toArray(new Region[values.size()]);
    }

    public static Region[] values() {
        return VALUES;
    }

    /**
     * Create a region from a name and a label.
     *
     * @param name the uniquely identifiable name of the region
     * @param label the label of the region
     */
    public Region(String name, String label) {
        this.name = name;
        this.label = label;
    }

    @JsonValue
    @Override
    public String toString() {
        return name();
    }

    /**
     * @return the name of the region
     */
    public String name() {
        return this.name;
    }

    /**
     * @return the label of the region
     */
    public String label() {
        return this.label;
    }

    /**
     * Parses a label into a Region object.
     *
     * @param label the region label
     * @return the parsed region or null if there's no such region
     */
    public static Region fromLabel(String label) {
        for (Region region : Region.VALUES) {
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
        for (Region region : Region.VALUES) {
            if (region.name.equalsIgnoreCase(name)) {
                return region;
            }
        }
        return null;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Region)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        Region rhs = (Region) obj;
        return name.equalsIgnoreCase(rhs.name);
    }
}
