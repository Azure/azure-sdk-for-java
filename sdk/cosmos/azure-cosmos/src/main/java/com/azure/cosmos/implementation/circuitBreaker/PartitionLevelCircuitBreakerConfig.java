// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.circuitBreaker;

import com.azure.cosmos.implementation.Utils;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.core.JsonProcessingException;

public class PartitionLevelCircuitBreakerConfig {

    public static final PartitionLevelCircuitBreakerConfig DEFAULT = new PartitionLevelCircuitBreakerConfig();

    @JsonSetter(nulls = Nulls.SKIP)
    @JsonProperty
    private Boolean isPartitionLevelCircuitBreakerEnabled = false;

    @JsonSetter(nulls = Nulls.SKIP)
    @JsonProperty
    private String circuitBreakerType = "COUNT_BASED";

    @JsonSetter(nulls = Nulls.SKIP)
    @JsonProperty
    private String circuitBreakerFailureTolerance = "LOW";

    public Boolean isPartitionLevelCircuitBreakerEnabled() {
        return isPartitionLevelCircuitBreakerEnabled;
    }

    public String getCircuitBreakerType() {
        return circuitBreakerType;
    }

    public String getCircuitBreakerFailureTolerance() {
        return circuitBreakerFailureTolerance;
    }

    public String toJson() {
        try {
            return Utils.getSimpleObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Unable to convert to Json String", e);
        }
    }

    public static PartitionLevelCircuitBreakerConfig fromJsonString(String jsonString) {
        try {
            return Utils.getSimpleObjectMapper().readValue(jsonString, PartitionLevelCircuitBreakerConfig.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Unable to convert from Json String", e);
        }
    }
}
