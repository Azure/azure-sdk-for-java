// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Duration;

/**
 * JSON configuration for one container-wide server-error fault injection rule.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
final class FaultInjectionConfig {

    @JsonProperty("serverErrorType")
    private FaultInjectionServerErrorType serverErrorType;

    @JsonProperty("startDelay")
    private Duration startDelay;

    @JsonProperty("duration")
    private Duration duration;

    @JsonProperty("injectionRate")
    private Double injectionRate;

    @JsonProperty("region")
    private String region;

    FaultInjectionConfig() {
    }

    FaultInjectionConfig(
        FaultInjectionServerErrorType serverErrorType,
        Duration startDelay,
        Duration duration,
        double injectionRate) {

        this(serverErrorType, startDelay, duration, injectionRate, null);
    }

    FaultInjectionConfig(
        FaultInjectionServerErrorType serverErrorType,
        Duration startDelay,
        Duration duration,
        double injectionRate,
        String region) {

        this.serverErrorType = serverErrorType;
        this.startDelay = startDelay;
        this.duration = duration;
        this.injectionRate = injectionRate;
        this.region = region;
    }

    FaultInjectionServerErrorType getServerErrorType() {
        return this.serverErrorType;
    }

    Duration getStartDelay() {
        return this.startDelay;
    }

    Duration getDuration() {
        return this.duration;
    }

    double getInjectionRate() {
        return this.injectionRate;
    }

    String getRegion() {
        return this.region;
    }

    void validate() {
        if (this.serverErrorType == null) {
            throw new IllegalArgumentException("faultInjection.serverErrorType is required");
        }

        if (this.startDelay == null || this.startDelay.isNegative()) {
            throw new IllegalArgumentException("faultInjection.startDelay must be a non-negative ISO-8601 duration");
        }

        if (this.duration == null || this.duration.isZero() || this.duration.isNegative()) {
            throw new IllegalArgumentException("faultInjection.duration must be a positive ISO-8601 duration");
        }

        if (this.injectionRate == null || this.injectionRate <= 0 || this.injectionRate > 1) {
            throw new IllegalArgumentException("faultInjection.injectionRate must be greater than 0 and at most 1");
        }

        if (this.region != null && this.region.trim().isEmpty()) {
            throw new IllegalArgumentException("faultInjection.region must not be empty");
        }
    }
}