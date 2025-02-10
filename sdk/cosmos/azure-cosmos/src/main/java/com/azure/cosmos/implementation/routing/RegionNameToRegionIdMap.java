// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.routing;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * ATTENTION: Please ensure the below map is consistent with <a href="https://msdata.visualstudio.com/CosmosDB/_git/CosmosDB?path=%2FProduct%2FCosmos%2FCosmosFabric%2FBackend%2FCommon%2FRegionToIdMap.cs&version=GBmaster">RegionToIdMap.cs</a> to avoid breaking behavior.
 * <p>
 * The purpose of the below map is to track region-specific progress from the session token (localLsn). If we know
 * the region name a request was routed to - the below map will help us obtain the localLsn for that region and partition combination
 * */
public class RegionNameToRegionIdMap {
    public static final Map<String, Integer> REGION_NAME_TO_REGION_ID_MAPPINGS = new HashMap<String, Integer>() {
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
            put("easteurope", 54);
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
            put("USSec West Central", 113);
        }
    };

    public static final Map<Integer, String> REGION_ID_TO_NORMALIZED_REGION_NAME_MAPPINGS = new HashMap<Integer, String>() {
        {
            put(49, "uaecentral");
            put(14, "francecentral");
            put(65, "usdodsouthcentral");
            put(26, "australiaeast");
            put(27, "australiasoutheast");
            put(16, "ukwest");
            put(40, "usgoviowa");
            put(72, "swedensouth");
            put(69, "usnatwest");
            put(13, "westeurope");
            put(50, "uaenorth");
            put(53, "northeurope2");
            put(36, "japanwest");
            put(5, "southcentralus");
            put(37, "koreacentral");
            put(60, "norwayeast");
            put(11, "brazilsouth");
            put(29, "australiacentral2");
            put(28, "australiacentral");
            put(73, "koreasouth2");
            put(32, "centralindia");
            put(35, "japaneast");
            put(45, "usseceast");
            put(25, "eastasia");
            put(6, "westcentralus");
            put(19, "germanynortheast");
            put(23, "switzerlandwest");
            put(52, "eastus2euap");
            put(8, "westus2");
            put(43, "usdodeast");
            put(17, "uksouth");
            put(56, "uksouth2");
            put(10, "canadacentral");
            put(68, "usnateast");
            put(20, "germanynorth");
            put(9, "canadaeast");
            put(67, "chinanorth2");
            put(22, "switzerlandnorth");
            put(58, "eastusstg");
            put(1, "eastus");
            put(57, "uknorth");
            put(4, "northcentralus");
            put(54, "easteurope");
            put(42, "usgovtexas");
            put(61, "norwaywest");
            put(55, "apacsoutheast2");
            put(12, "northeurope");
            put(59, "southcentralusstg");
            put(21, "germanywestcentral");
            put(24, "southeastasia");
            put(71, "swedencentral");
            put(31, "chinanorth");
            put(62, "usgovwyoming");
            put(30, "chinaeast");
            put(2, "eastus2");
            put(34, "southindia");
            put(51, "centraluseuap");
            put(18, "germanycentral");
            put(7, "westus");
            put(44, "usdodcentral");
            put(66, "chinaeast2");
            put(39, "usgovvirginia");
            put(64, "usdodwestcentral");
            put(70, "chinanorth10");
            put(41, "usgovarizona");
            put(33, "westindia");
            put(38, "koreasouth");
            put(3, "centralus");
            put(63, "usdodsouthwest");
            put(47, "southafricawest");
            put(46, "ussecwest");
            put(15, "francesouth");
            put(48, "southafricanorth");
            put(113, "ussecwestcentral");
        }
    };

    public static final Map<String, Integer> NORMALIZED_REGION_NAME_TO_REGION_ID_MAPPINGS = new HashMap<String, Integer>() {
        {
            put("southafricanorth", 48);
            put("westus2", 8);
            put("australiacentral", 28);
            put("apacsoutheast2", 55);
            put("eastasia", 25);
            put("uknorth", 57);
            put("francecentral", 14);
            put("southafricawest", 47);
            put("usgovtexas", 42);
            put("koreacentral", 37);
            put("centralus", 3);
            put("japaneast", 35);
            put("westeurope", 13);
            put("norwayeast", 60);
            put("eastus", 1);
            put("australiasoutheast", 27);
            put("centralindia", 32);
            put("usdodeast", 43);
            put("germanycentral", 18);
            put("usdodwestcentral", 64);
            put("switzerlandwest", 23);
            put("chinaeast2", 66);
            put("westus", 7);
            put("northcentralus", 4);
            put("usdodcentral", 44);
            put("uaenorth", 50);
            put("centraluseuap", 51);
            put("germanywestcentral", 21);
            put("ussecwest", 46);
            put("usnateast", 68);
            put("uksouth", 17);
            put("usgovvirginia", 39);
            put("usgoviowa", 40);
            put("chinanorth2", 67);
            put("germanynorth", 20);
            put("easteurope", 54);
            put("uksouth2", 56);
            put("ukwest", 16);
            put("japanwest", 36);
            put("usdodsouthcentral", 65);
            put("australiaeast", 26);
            put("westindia", 33);
            put("australiacentral2", 29);
            put("southindia", 34);
            put("eastus2euap", 52);
            put("canadaeast", 9);
            put("southeastasia", 24);
            put("koreasouth", 38);
            put("southcentralus", 5);
            put("eastusstg", 58);
            put("chinanorth10", 70);
            put("swedensouth", 72);
            put("westcentralus", 6);
            put("eastus2", 2);
            put("chinaeast", 30);
            put("usgovarizona", 41);
            put("norwaywest", 61);
            put("uaecentral", 49);
            put("swedencentral", 71);
            put("usdodsouthwest", 63);
            put("usnatwest", 69);
            put("chinanorth", 31);
            put("northeurope2", 53);
            put("usgovwyoming", 62);
            put("brazilsouth", 11);
            put("koreasouth2", 73);
            put("canadacentral", 10);
            put("southcentralusstg", 59);
            put("usseceast", 45);
            put("francesouth", 15);
            put("germanynortheast", 19);
            put("switzerlandnorth", 22);
            put("northeurope", 12);
            put("ussecwestcentral", 113);
        }
    };

    public static String getRegionName(int regionId) {
        return REGION_ID_TO_NORMALIZED_REGION_NAME_MAPPINGS.getOrDefault(regionId, StringUtils.EMPTY);
    }

    public static int getRegionId(String regionName) {
        return NORMALIZED_REGION_NAME_TO_REGION_ID_MAPPINGS.getOrDefault(regionName, -1);
    }
}
