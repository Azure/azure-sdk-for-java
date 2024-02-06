// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.routing;

import java.util.*;

public class RegionNameToRegionIdMap {
    private static final Map<String, Integer> regionIdToRegionNameMappings = new HashMap<>() {
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
        }
    };
}
