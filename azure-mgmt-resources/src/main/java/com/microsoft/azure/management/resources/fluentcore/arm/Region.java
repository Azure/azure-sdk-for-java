package com.microsoft.azure.management.resources.fluentcore.arm;

/**
 * Enumeration of the Azure datacenter regions. See https://azure.microsoft.com/regions/
 */
public enum Region {
	US_WEST("westus", "West US"),
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
	INDIA_WEST("westindia", "West India");
	
	private final String name;
	public final String label;
	Region(String name, String label) {
		this.name = name;
		this.label = label;
	}
	
	@Override
	public String toString() {
		return this.name;
	}
	
	
	public static Region fromLabel(String label) {
		for(Region region : Region.values()) {
			if(region.label.equalsIgnoreCase(label)) {
				return region;
			}
		}
		
		return null;
	}

	
	public static Region fromName(String name) {
		for(Region region : Region.values()) {
			if(region.name.equalsIgnoreCase(name)) {
				return region;
			}
		}
		
		return null;
	}
}
