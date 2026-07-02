// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.metadatahedging;

/**
 * Diagnostic record describing the outcome of a single
 * {@link MetadataHedgingStrategy#executeAsync} invocation. Attached to the request trace for
 * supportability.
 * <p>
 * Java port of the .NET {@code MetadataHedgingStrategy.MetadataHedgeDiagnostics}
 * (Azure/azure-cosmos-dotnet-v3#5923, design &sect;9).
 */
public final class MetadataHedgeDiagnostics {

    private volatile boolean eligible;
    private volatile MetadataHedgeSkipReason skipReason = MetadataHedgeSkipReason.NONE;
    private final String resourceType;
    private volatile String primaryRegion;
    private volatile String hedgeRegion;
    private final double thresholdMs;
    private volatile Double hedgeFiredElapsedMs;
    private volatile String winningRegion;
    private volatile int totalAttempts;
    private volatile boolean hedgeFired;
    private volatile String hedgeOutcome;

    public MetadataHedgeDiagnostics(String resourceType, double thresholdMs) {
        this.resourceType = resourceType;
        this.thresholdMs = thresholdMs;
    }

    public boolean isEligible() {
        return this.eligible;
    }

    public void setEligible(boolean eligible) {
        this.eligible = eligible;
    }

    public MetadataHedgeSkipReason getSkipReason() {
        return this.skipReason;
    }

    public void setSkipReason(MetadataHedgeSkipReason skipReason) {
        this.skipReason = skipReason;
    }

    public String getResourceType() {
        return this.resourceType;
    }

    public String getPrimaryRegion() {
        return this.primaryRegion;
    }

    public void setPrimaryRegion(String primaryRegion) {
        this.primaryRegion = primaryRegion;
    }

    public String getHedgeRegion() {
        return this.hedgeRegion;
    }

    public void setHedgeRegion(String hedgeRegion) {
        this.hedgeRegion = hedgeRegion;
    }

    public double getThresholdMs() {
        return this.thresholdMs;
    }

    public Double getHedgeFiredElapsedMs() {
        return this.hedgeFiredElapsedMs;
    }

    public void setHedgeFiredElapsedMs(Double hedgeFiredElapsedMs) {
        this.hedgeFiredElapsedMs = hedgeFiredElapsedMs;
    }

    public String getWinningRegion() {
        return this.winningRegion;
    }

    public void setWinningRegion(String winningRegion) {
        this.winningRegion = winningRegion;
    }

    public int getTotalAttempts() {
        return this.totalAttempts;
    }

    public void setTotalAttempts(int totalAttempts) {
        this.totalAttempts = totalAttempts;
    }

    public boolean isHedgeFired() {
        return this.hedgeFired;
    }

    public void setHedgeFired(boolean hedgeFired) {
        this.hedgeFired = hedgeFired;
    }

    public String getHedgeOutcome() {
        return this.hedgeOutcome;
    }

    public void setHedgeOutcome(String hedgeOutcome) {
        this.hedgeOutcome = hedgeOutcome;
    }

    @Override
    public String toString() {
        return "MetadataHedgeDiagnostics{"
            + "eligible=" + this.eligible
            + ", skipReason=" + this.skipReason
            + ", resourceType='" + this.resourceType + '\''
            + ", primaryRegion='" + this.primaryRegion + '\''
            + ", hedgeRegion='" + this.hedgeRegion + '\''
            + ", thresholdMs=" + this.thresholdMs
            + ", hedgeFiredElapsedMs=" + this.hedgeFiredElapsedMs
            + ", winningRegion='" + this.winningRegion + '\''
            + ", totalAttempts=" + this.totalAttempts
            + ", hedgeFired=" + this.hedgeFired
            + ", hedgeOutcome='" + this.hedgeOutcome + '\''
            + '}';
    }
}
