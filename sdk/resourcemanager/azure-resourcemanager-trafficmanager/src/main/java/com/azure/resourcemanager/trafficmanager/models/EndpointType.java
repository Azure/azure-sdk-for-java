// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.trafficmanager.models;

/** Possible endpoint types supported in a Traffic manager profile. */
public enum EndpointType {
    /** Enum value Microsoft.Network/trafficManagerProfiles/azureEndpoints. */
    AZURE("Microsoft.Network/trafficManagerProfiles/azureEndpoints"),

    /** Enum value Microsoft.Network/trafficManagerProfiles/externalEndpoints. */
    EXTERNAL("Microsoft.Network/trafficManagerProfiles/externalEndpoints"),

    /** Enum value Microsoft.Network/trafficManagerProfiles/nestedEndpoints. */
    NESTED_PROFILE("Microsoft.Network/trafficManagerProfiles/nestedEndpoints");

    private String value;

    EndpointType(String value) {
        this.value = value;
    }

    /**
     * Parses a string value to a TrafficRoutingMethod instance.
     *
     * @param value the string value to parse.
     * @return the parsed TrafficRoutingMethod object, or null if unable to parse.
     */
    public static EndpointType fromValue(String value) {
        EndpointType[] items = EndpointType.values();
        for (EndpointType item : items) {
            if (item.toString().equalsIgnoreCase(value)) {
                return item;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return this.value;
    }

    /** @return the local name of the endpoint type */
    public String localName() {
        if (this.value != null) {
            return this.value.substring(this.value.lastIndexOf('/') + 1);
        }
        return null;
    }
}
