// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.trafficmanager.models;

import com.azure.resourcemanager.trafficmanager.fluent.models.TrafficManagerNameAvailabilityInner;

/** The result of checking for DNS name availability. */
public class CheckProfileDnsNameAvailabilityResult {
    private final TrafficManagerNameAvailabilityInner inner;

    /**
     * Creates an instance of CheckProfileDnsNameAvailabilityResult.
     *
     * @param inner the inner object
     */
    public CheckProfileDnsNameAvailabilityResult(TrafficManagerNameAvailabilityInner inner) {
        this.inner = inner;
    }

    /**
     * @return true if the DNS name is available to use, false if the name has already been taken or invalid and cannot
     *     be used.
     */
    public boolean isAvailable() {
        return inner.nameAvailable();
    }

    /** @return the reason that the DNS name could not be used */
    public ProfileDnsNameUnavailableReason reason() {
        return new ProfileDnsNameUnavailableReason(inner.reason());
    }

    /** @return an error message explaining the reason value in more detail */
    public String message() {
        return inner.message();
    }
}
