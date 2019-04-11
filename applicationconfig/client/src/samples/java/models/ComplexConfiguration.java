// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A complex configuration object that has multiple properties stored in Azure App Configuration service.
 */
public class ComplexConfiguration {
    @JsonProperty("endpointUri")
    private String endpointUri;

    @JsonProperty("name")
    private String name;

    @JsonProperty("numberOfInstances")
    private int numberOfInstances;

    public String endpointUri() {
        return endpointUri;
    }

    public ComplexConfiguration endpointUri(String endpointUri) {
        this.endpointUri = endpointUri;
        return this;
    }

    public String name() {
        return name;
    }

    public ComplexConfiguration name(String name) {
        this.name = name;
        return this;
    }

    public int numberOfInstances() {
        return numberOfInstances;
    }

    public ComplexConfiguration numberOfInstances(int numberOfInstances) {
        this.numberOfInstances = numberOfInstances;
        return this;
    }

    @Override
    public String toString() {
        return "Name: " + name() + ", Endpoint: " + endpointUri() + ", # of instances: " + numberOfInstances();
    }
}
