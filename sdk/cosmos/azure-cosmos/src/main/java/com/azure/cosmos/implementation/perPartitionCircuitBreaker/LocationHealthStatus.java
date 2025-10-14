// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.perPartitionCircuitBreaker;

public enum LocationHealthStatus {
    Healthy("Healthy"),
    HealthyWithFailures("HealthyWithFailures"),
    Unavailable("Unavailable"),
    HealthyTentative("HealthyTentative");

    private final String stringifiedRepresentation;

    LocationHealthStatus(String stringifiedRepresentation) {
        this.stringifiedRepresentation = stringifiedRepresentation;
    }

    public String getStringifiedLocationHealthStatus() {
        return stringifiedRepresentation;
    }
}
