// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.perPartitionCircuitBreaker;

import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
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
    private String circuitBreakerType = "CONSECUTIVE_EXCEPTION_COUNT_BASED";

    @JsonSetter(nulls = Nulls.SKIP)
    @JsonProperty
    private int consecutiveExceptionCountToleratedForReads = 10;

    @JsonSetter(nulls = Nulls.SKIP)
    @JsonProperty
    private int consecutiveExceptionCountToleratedForWrites = 5;

    private String cachedConfigAsString = "";

    public Boolean isPartitionLevelCircuitBreakerEnabled() {
        return isPartitionLevelCircuitBreakerEnabled;
    }

    // todo (abhmohanty): keep this method around for future-proofing (adding more circuit breaker types)
    public String getCircuitBreakerType() {
        return circuitBreakerType;
    }

    public int getConsecutiveExceptionCountToleratedForReads() {
        return consecutiveExceptionCountToleratedForReads;
    }

    public int getConsecutiveExceptionCountToleratedForWrites() {
        return consecutiveExceptionCountToleratedForWrites;
    }

    public String toJson() {
        try {
            return Utils.getSimpleObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Unable to convert to Json String", e);
        }
    }

    public String getConfigAsString() {

        if (StringUtils.isEmpty(this.cachedConfigAsString)) {
            this.cachedConfigAsString = "(" + "cb: " + this.isPartitionLevelCircuitBreakerEnabled + ", " +
                "type: " + this.circuitBreakerType + ", " +
                "rexcntt: " + this.consecutiveExceptionCountToleratedForReads + ", " +
                "wexcntt: " + this.consecutiveExceptionCountToleratedForWrites + ")";
        }

        return this.cachedConfigAsString;
    }

    public static PartitionLevelCircuitBreakerConfig fromJsonString(String jsonString) {
        try {
            return Utils.getSimpleObjectMapper().readValue(jsonString, PartitionLevelCircuitBreakerConfig.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Unable to convert from Json String", e);
        }
    }
}
