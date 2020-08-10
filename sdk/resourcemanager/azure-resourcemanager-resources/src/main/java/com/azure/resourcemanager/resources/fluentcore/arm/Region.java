// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.arm;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Enumeration of the Azure datacenter regions. See https://azure.microsoft.com/regions/
 */
public final class Region {
    // This needs to be at the beginning for the initialization to happen correctly
    private static final ConcurrentMap<String, Region> VALUES_BY_NAME = new ConcurrentHashMap<>();

    // CHECKSTYLE IGNORE Javadoc FOR NEXT 67 LINES
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
    public static final Region FRANCE_CENTRAL = new Region("francecentral", "France Central");
    public static final Region FRANCE_SOUTH = new Region("francesouth", "France South");
    public static final Region SWITZERLAND_NORTH = new Region("switzerlandnorth", "Switzerland North");
    public static final Region SWITZERLAND_WEST = new Region("switzerlandwest", "Switzerland West");
    public static final Region GERMANY_NORTH = new Region("germanynorth", "Germany North");
    public static final Region GERMANY_WESTCENTRAL = new Region("germanywestcentral", "Germany West Central");
    public static final Region NORWAY_WEST = new Region("norwaywest", "Norway West");
    public static final Region NORWAY_EAST = new Region("norwayeast", "Norway East");
    /**************************************************
     * Azure Cloud - Asia
     **************************************************/
    public static final Region ASIA_EAST = new Region("eastasia", "East Asia");
    public static final Region ASIA_SOUTHEAST = new Region("southeastasia", "South East Asia");
    public static final Region JAPAN_EAST = new Region("japaneast", "Japan East");
    public static final Region JAPAN_WEST = new Region("japanwest", "Japan West");
    public static final Region AUSTRALIA_EAST = new Region("australiaeast", "Australia East");
    public static final Region AUSTRALIA_SOUTHEAST = new Region("australiasoutheast", "Australia Southeast");
    public static final Region AUSTRALIA_CENTRAL = new Region("australiacentral", "Australia Central");
    public static final Region AUSTRALIA_CENTRAL2 = new Region("australiacentral2", "Australia Central 2");
    public static final Region INDIA_CENTRAL = new Region("centralindia", "Central India");
    public static final Region INDIA_SOUTH = new Region("southindia", "South India");
    public static final Region INDIA_WEST = new Region("westindia", "West India");
    public static final Region KOREA_CENTRAL = new Region("koreacentral", "Korea Central");
    public static final Region KOREA_SOUTH = new Region("koreasouth", "Korea South");
    /**************************************************
     * Azure Cloud - Middle East and Africa
     **************************************************/
    public static final Region UAE_CENTRAL = new Region("uaecentral", "UAE Central");
    public static final Region UAE_NORTH = new Region("uaenorth", "UAE North");
    public static final Region SOUTHAFRICA_NORTH = new Region("southafricanorth", "South Africa North");
    public static final Region SOUTHAFRICA_WEST = new Region("southafricawest", "South Africa West");
    /**************************************************
     * Azure China Cloud
     **************************************************/
    public static final Region CHINA_NORTH = new Region("chinanorth", "China North");
    public static final Region CHINA_EAST = new Region("chinaeast", "China East");
    public static final Region CHINA_NORTH2 = new Region("chinanorth2", "China North 2");
    public static final Region CHINA_EAST2 = new Region("chinaeast2", "China East 2");
    /**************************************************
     * Azure German Cloud
     **************************************************/
    public static final Region GERMANY_CENTRAL = new Region("germanycentral", "Germany Central");
    public static final Region GERMANY_NORTHEAST = new Region("germanynortheast", "Germany Northeast");
    /**************************************************
     * Azure Government Cloud
     **************************************************/
    /**
     * U.S. government cloud in Virginia.
     */
    public static final Region GOV_US_VIRGINIA = new Region("usgovvirginia", "US Gov Virginia");

    /**
     * U.S. government cloud in Iowa.
     */
    public static final Region GOV_US_IOWA = new Region("usgoviowa", "US Gov Iowa");

    /**
     * U.S. government cloud in Arizona.
     */
    public static final Region GOV_US_ARIZONA = new Region("usgovarizona", "US Gov Arizona");

    /**
     * U.S. government cloud in Texas.
     */
    public static final Region GOV_US_TEXAS = new Region("usgovtexas", "US Gov Texas");

    /**
     * U.S. Department of Defense cloud - East.
     */
    public static final Region GOV_US_DOD_EAST = new Region("usdodeast", "US DoD East");

    /**
     * U.S. Department of Defense cloud - Central.
     */
    public static final Region GOV_US_DOD_CENTRAL = new Region("usdodcentral", "US DoD Central");

    private final String name;
    private final String label;

    /**
     * @return predefined Azure regions
     */
    public static Region[] values() {
        Collection<Region> valuesCollection = VALUES_BY_NAME.values();
        return valuesCollection.toArray(new Region[valuesCollection.size()]);
    }

    private Region(String name, String label) {
        this.name = name;
        this.label = label;
        VALUES_BY_NAME.put(name.toLowerCase(Locale.ROOT), this);
    }

    /**
     * Creates a region from a name and a label.
     *
     * @param name the uniquely identifiable name of the region
     * @param label the label of the region
     * @return the newly created region
     */
    public static Region create(String name, String label) {
        Region region = VALUES_BY_NAME.get(name.toLowerCase(Locale.ROOT));
        if (region != null) {
            return region;
        } else {
            return new Region(name, label);
        }
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
     * Finds a region based on a label or name.
     * <p>
     * A region name is lower-cased label with spaces removed.
     *
     * @param labelOrName the region name or label
     * @return the found region or null if there's no such region
     */
    public static Region findByLabelOrName(String labelOrName) {
        if (labelOrName == null) {
            return null;
        }

        return VALUES_BY_NAME.get(labelOrName.toLowerCase(Locale.ROOT).replace(" ", ""));
    }

    /**
     * Parses a name into a Region object and creates a new Region instance if not found among the existing ones.
     *
     * @param name a region name
     * @return the parsed or created region
     */
    public static Region fromName(String name) {
        if (name == null) {
            return null;
        }

        Region region = VALUES_BY_NAME.get(name.toLowerCase(Locale.ROOT).replace(" ", ""));
        if (region != null) {
            return region;
        } else {
            return Region.create(name.toLowerCase(Locale.ROOT).replace(" ", ""), name);
        }
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Region)) {
            return false;
        } else if (obj == this) {
            return true;
        } else {
            Region rhs = (Region) obj;
            return this.name.equalsIgnoreCase(rhs.name);
        }
    }
}
