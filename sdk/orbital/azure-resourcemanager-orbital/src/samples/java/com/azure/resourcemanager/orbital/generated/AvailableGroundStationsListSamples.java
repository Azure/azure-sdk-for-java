// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.orbital.generated;

import com.azure.resourcemanager.orbital.models.CapabilityParameter;

/**
 * Samples for AvailableGroundStations List.
 */
public final class AvailableGroundStationsListSamples {
    /*
     * x-ms-original-file: specification/orbital/resource-manager/Microsoft.Orbital/stable/2022-11-01/examples/
     * AvailableGroundStationsByCapabilityList.json
     */
    /**
     * Sample code: List of Ground Stations by Capability.
     * 
     * @param manager Entry point to OrbitalManager.
     */
    public static void listOfGroundStationsByCapability(com.azure.resourcemanager.orbital.OrbitalManager manager) {
        manager.availableGroundStations().list(CapabilityParameter.EARTH_OBSERVATION, com.azure.core.util.Context.NONE);
    }
}
