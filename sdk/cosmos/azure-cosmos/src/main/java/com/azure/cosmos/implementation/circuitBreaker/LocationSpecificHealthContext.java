// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.circuitBreaker;

import com.azure.cosmos.implementation.DiagnosticsInstantSerializer;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;
import java.io.Serializable;
import java.time.Instant;

@JsonSerialize(using = LocationSpecificHealthContext.LocationSpecificHealthContextSerializer.class)
public class LocationSpecificHealthContext implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int exceptionCountForWriteForCircuitBreaking;
    private final int successCountForWriteForRecovery;
    private final int exceptionCountForReadForCircuitBreaking;
    private final int successCountForReadForRecovery;
    private final Instant unavailableSince;
    private final LocationHealthStatus locationHealthStatus;
    private final boolean isExceptionThresholdBreached;

    LocationSpecificHealthContext(
        int successCountForWriteForRecovery,
        int exceptionCountForWriteForCircuitBreaking,
        int successCountForReadForRecovery,
        int exceptionCountForReadForCircuitBreaking,
        Instant unavailableSince,
        LocationHealthStatus locationHealthStatus,
        boolean isExceptionThresholdBreached) {

        this.successCountForWriteForRecovery = successCountForWriteForRecovery;
        this.exceptionCountForWriteForCircuitBreaking = exceptionCountForWriteForCircuitBreaking;
        this.successCountForReadForRecovery = successCountForReadForRecovery;
        this.exceptionCountForReadForCircuitBreaking = exceptionCountForReadForCircuitBreaking;
        this.unavailableSince = unavailableSince;
        this.locationHealthStatus = locationHealthStatus;
        this.isExceptionThresholdBreached = isExceptionThresholdBreached;
    }

    public boolean isExceptionThresholdBreached() {
        return this.isExceptionThresholdBreached;
    }

    public boolean isRegionAvailableToProcessRequests() {
        return this.locationHealthStatus == LocationHealthStatus.Healthy ||
            this.locationHealthStatus == LocationHealthStatus.HealthyWithFailures ||
            this.locationHealthStatus == LocationHealthStatus.HealthyTentative;
    }

    public int getExceptionCountForWriteForCircuitBreaking() {
        return this.exceptionCountForWriteForCircuitBreaking;
    }

    public int getSuccessCountForWriteForRecovery() {
        return this.successCountForWriteForRecovery;
    }

    public int getExceptionCountForReadForCircuitBreaking() {
        return this.exceptionCountForReadForCircuitBreaking;
    }

    public int getSuccessCountForReadForRecovery() {
        return this.successCountForReadForRecovery;
    }

    public Instant getUnavailableSince() {
        return this.unavailableSince;
    }

    public LocationHealthStatus getLocationHealthStatus() {
        return this.locationHealthStatus;
    }

    static class Builder {

        private int exceptionCountForWriteForCircuitBreaking;
        private int successCountForWriteForRecovery;
        private int exceptionCountForReadForCircuitBreaking;
        private int successCountForReadForRecovery;
        private Instant unavailableSince;
        private LocationHealthStatus locationHealthStatus;
        private boolean isExceptionThresholdBreached;

        public Builder() {}

        public Builder withExceptionCountForWriteForCircuitBreaking(int exceptionCountForWriteForCircuitBreaking) {
            this.exceptionCountForWriteForCircuitBreaking = exceptionCountForWriteForCircuitBreaking;
            return this;
        }

        public Builder withSuccessCountForWriteForRecovery(int successCountForWriteForRecovery) {
            this.successCountForWriteForRecovery = successCountForWriteForRecovery;
            return this;
        }

        public Builder withExceptionCountForReadForCircuitBreaking(int exceptionCountForReadForCircuitBreaking) {
            this.exceptionCountForReadForCircuitBreaking = exceptionCountForReadForCircuitBreaking;
            return this;
        }

        public Builder withSuccessCountForReadForRecovery(int successCountForReadForRecovery) {
            this.successCountForReadForRecovery = successCountForReadForRecovery;
            return this;
        }

        public Builder withUnavailableSince(Instant unavailableSince) {
            this.unavailableSince = unavailableSince;
            return this;
        }

        public Builder withLocationHealthStatus(LocationHealthStatus locationHealthStatus) {
            this.locationHealthStatus = locationHealthStatus;
            return this;
        }

        public Builder withExceptionThresholdBreached(boolean exceptionThresholdBreached) {
            isExceptionThresholdBreached = exceptionThresholdBreached;
            return this;
        }

        public LocationSpecificHealthContext build() {

            return new LocationSpecificHealthContext(
                this.successCountForWriteForRecovery,
                this.exceptionCountForWriteForCircuitBreaking,
                this.successCountForReadForRecovery,
                this.exceptionCountForReadForCircuitBreaking,
                this.unavailableSince,
                this.locationHealthStatus,
                this.isExceptionThresholdBreached);
        }
    }

    static class LocationSpecificHealthContextSerializer extends com.fasterxml.jackson.databind.JsonSerializer<LocationSpecificHealthContext> {

        @Override
        public void serialize(LocationSpecificHealthContext value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeStartObject();

            gen.writeNumberField("exceptionCountForWriteForCircuitBreaking", value.exceptionCountForWriteForCircuitBreaking);
            gen.writeNumberField("exceptionCountForReadForCircuitBreaking", value.exceptionCountForReadForCircuitBreaking);
            gen.writeNumberField("successCountForWriteForRecovery", value.successCountForWriteForRecovery);
            gen.writeNumberField("successCountForReadForRecovery", value.successCountForReadForRecovery);
            gen.writePOJOField("locationHealthStatus", value.locationHealthStatus);
            gen.writeStringField("unavailableSince", toInstantString(value.unavailableSince));

            gen.writeEndObject();
        }

        private String toInstantString(Instant instant) {
            return DiagnosticsInstantSerializer.fromInstant(instant);
        }
    }
}
