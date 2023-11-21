// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.stress;

public class FaultInjectionProbabilities {
    // f: Full response
    // p: Partial Response (full headers, 50% of body), then wait indefinitely
    // pc: Partial Response (full headers, 50% of body), then close (TCP FIN)
    // pa: Partial Response (full headers, 50% of body), then abort (TCP RST)
    // pn: Partial Response (full headers, 50% of body), then finish normally
    // n: No response, then wait indefinitely
    // nc: No response, then close (TCP FIN)
    // na: No response, then abort (TCP RST)
    private double noResponseIndefinite;
    private double noResponseClose;
    private double noResponseAbort;
    private double partialResponseIndefinite;
    private double partialResponseClose;
    private double partialResponseAbort;
    private double partialResponseFinishNormal;

    private double getExistingSum() {
        return noResponseIndefinite + noResponseClose + noResponseAbort + partialResponseIndefinite +
            partialResponseClose + partialResponseAbort + partialResponseFinishNormal;
    }
    private void validateProbabilityChange(double netChange) {
        double newSum = getExistingSum() + netChange;
        if (newSum > 1d) {
            throw new IllegalStateException(String.format("Probability sum cannot exceed 1. Got %4f.", newSum));
        }
    }

    public double getNoResponseIndefinite() {
        return noResponseIndefinite;
    }

    public FaultInjectionProbabilities setNoResponseIndefinite(double noResponseIndefinite) {
        validateProbabilityChange(noResponseIndefinite - this.noResponseIndefinite);
        this.noResponseIndefinite = noResponseIndefinite;
        return this;
    }

    public double getNoResponseClose() {
        return noResponseClose;
    }

    public FaultInjectionProbabilities setNoResponseClose(double noResponseClose) {
        validateProbabilityChange(noResponseClose - this.noResponseClose);
        this.noResponseClose = noResponseClose;
        return this;
    }

    public double getNoResponseAbort() {
        return noResponseAbort;
    }

    public FaultInjectionProbabilities setNoResponseAbort(double noResponseAbort) {
        validateProbabilityChange(noResponseAbort - this.noResponseAbort);
        this.noResponseAbort = noResponseAbort;
        return this;
    }

    public double getPartialResponseIndefinite() {
        return partialResponseIndefinite;
    }

    public FaultInjectionProbabilities setPartialResponseIndefinite(double partialResponseIndefinite) {
        validateProbabilityChange(partialResponseIndefinite - this.partialResponseIndefinite);
        this.partialResponseIndefinite = partialResponseIndefinite;
        return this;
    }

    public double getPartialResponseClose() {
        return partialResponseClose;
    }

    public FaultInjectionProbabilities setPartialResponseClose(double partialResponseClose) {
        validateProbabilityChange(partialResponseClose - this.partialResponseClose);
        this.partialResponseClose = partialResponseClose;
        return this;
    }

    public double getPartialResponseAbort() {
        return partialResponseAbort;
    }

    public FaultInjectionProbabilities setPartialResponseAbort(double partialResponseAbort) {
        validateProbabilityChange(partialResponseAbort - this.partialResponseAbort);
        this.partialResponseAbort = partialResponseAbort;
        return this;
    }

    public double getPartialResponseFinishNormal() {
        return partialResponseFinishNormal;
    }

    public FaultInjectionProbabilities setPartialResponseFinishNormal(double partialResponseFinishNormal) {
        validateProbabilityChange(partialResponseFinishNormal - this.partialResponseFinishNormal);
        this.partialResponseFinishNormal = partialResponseFinishNormal;
        return this;
    }
}
