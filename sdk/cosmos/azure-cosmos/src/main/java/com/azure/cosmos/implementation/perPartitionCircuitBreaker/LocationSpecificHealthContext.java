// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.perPartitionCircuitBreaker;

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
    private final FailbackDiagnostics failbackDiagnostics;

    LocationSpecificHealthContext(
        int successCountForWriteForRecovery,
        int exceptionCountForWriteForCircuitBreaking,
        int successCountForReadForRecovery,
        int exceptionCountForReadForCircuitBreaking,
        Instant unavailableSince,
        LocationHealthStatus locationHealthStatus,
        boolean isExceptionThresholdBreached,
        FailbackDiagnostics failbackDiagnostics) {

        this.successCountForWriteForRecovery = successCountForWriteForRecovery;
        this.exceptionCountForWriteForCircuitBreaking = exceptionCountForWriteForCircuitBreaking;
        this.successCountForReadForRecovery = successCountForReadForRecovery;
        this.exceptionCountForReadForCircuitBreaking = exceptionCountForReadForCircuitBreaking;
        this.unavailableSince = unavailableSince;
        this.locationHealthStatus = locationHealthStatus;
        this.isExceptionThresholdBreached = isExceptionThresholdBreached;
        this.failbackDiagnostics = failbackDiagnostics;
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

    public Instant getLastFailbackAttemptTime() {
        return this.failbackDiagnostics == null ? null : this.failbackDiagnostics.lastAttemptedAt;
    }

    public FailbackOutcome getLastFailbackOutcome() {
        return this.failbackDiagnostics == null ? null : this.failbackDiagnostics.outcome;
    }

    LocationSpecificHealthContext withFailbackAttempt(
        Instant attemptTime,
        FailbackOutcome outcome,
        String failureStage,
        Throwable failure) {

        boolean failed = outcome == FailbackOutcome.Failed;
        return new Builder(this)
            .withFailbackDiagnostics(new FailbackDiagnostics(
                attemptTime,
                outcome,
                failed ? failureStage : null,
                failed && failure != null ? failure.getClass().getName() : null,
                failed && failure != null ? failure.getMessage() : null))
            .build();
    }

    public enum FailbackOutcome {
        Attempting,
        Succeeded,
        Failed
    }

    private static class FailbackDiagnostics {
        private final Instant lastAttemptedAt;
        private final FailbackOutcome outcome;
        private final String failureStage;
        private final String failureType;
        private final String failureMessage;

        private FailbackDiagnostics(
            Instant lastAttemptedAt,
            FailbackOutcome outcome,
            String failureStage,
            String failureType,
            String failureMessage) {

            this.lastAttemptedAt = lastAttemptedAt;
            this.outcome = outcome;
            this.failureStage = failureStage;
            this.failureType = failureType;
            this.failureMessage = failureMessage;
        }
    }

    static class Builder {

        private int exceptionCountForWriteForCircuitBreaking;
        private int successCountForWriteForRecovery;
        private int exceptionCountForReadForCircuitBreaking;
        private int successCountForReadForRecovery;
        private Instant unavailableSince;
        private LocationHealthStatus locationHealthStatus;
        private boolean isExceptionThresholdBreached;
        private FailbackDiagnostics failbackDiagnostics;

        public Builder() {}

        Builder(LocationSpecificHealthContext source) {
            this.exceptionCountForWriteForCircuitBreaking = source.exceptionCountForWriteForCircuitBreaking;
            this.successCountForWriteForRecovery = source.successCountForWriteForRecovery;
            this.exceptionCountForReadForCircuitBreaking = source.exceptionCountForReadForCircuitBreaking;
            this.successCountForReadForRecovery = source.successCountForReadForRecovery;
            this.unavailableSince = source.unavailableSince;
            this.locationHealthStatus = source.locationHealthStatus;
            this.isExceptionThresholdBreached = source.isExceptionThresholdBreached;
            this.failbackDiagnostics = source.failbackDiagnostics;
        }

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

        Builder withFailbackDiagnostics(FailbackDiagnostics failbackDiagnostics) {
            this.failbackDiagnostics = failbackDiagnostics;
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
                this.isExceptionThresholdBreached,
                this.failbackDiagnostics);
        }
    }

    static class LocationSpecificHealthContextSerializer extends com.fasterxml.jackson.databind.JsonSerializer<LocationSpecificHealthContext> {

        @Override
        public void serialize(LocationSpecificHealthContext value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeStartObject();

            gen.writePOJOField("st", value.locationHealthStatus);
            gen.writeNumberField("rErr", value.exceptionCountForReadForCircuitBreaking);
            gen.writeNumberField("wErr", value.exceptionCountForWriteForCircuitBreaking);
            gen.writeNumberField("rOk", value.successCountForReadForRecovery);
            gen.writeNumberField("wOk", value.successCountForWriteForRecovery);
            gen.writeStringField("unavailableSince", toInstantString(value.unavailableSince));

            if (value.failbackDiagnostics != null) {
                gen.writeObjectFieldStart("failback");
                gen.writeStringField("lastAttemptedAt", toInstantString(value.failbackDiagnostics.lastAttemptedAt));
                gen.writePOJOField("outcome", value.failbackDiagnostics.outcome);
                if (value.failbackDiagnostics.outcome == FailbackOutcome.Failed) {
                    gen.writeObjectFieldStart("failure");
                    gen.writeStringField("stage", value.failbackDiagnostics.failureStage);
                    gen.writeStringField("type", value.failbackDiagnostics.failureType);
                    gen.writeStringField("message", value.failbackDiagnostics.failureMessage);
                    gen.writeEndObject();
                }
                gen.writeEndObject();
            }

            gen.writeEndObject();
        }

        private String toInstantString(Instant instant) {
            return DiagnosticsInstantSerializer.fromInstant(instant);
        }
    }
}
