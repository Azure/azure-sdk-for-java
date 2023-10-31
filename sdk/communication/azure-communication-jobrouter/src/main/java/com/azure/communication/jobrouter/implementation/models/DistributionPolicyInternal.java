// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.jobrouter.implementation.models;

import com.azure.communication.jobrouter.models.DistributionMode;
import com.azure.core.annotation.Fluent;
import com.azure.core.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Duration;

/** Policy governing how jobs are distributed to workers. */
@Fluent
public final class DistributionPolicyInternal {

    /*
     * The unique identifier of the policy.
     */
    @Generated
    @JsonProperty(value = "id", access = JsonProperty.Access.WRITE_ONLY)
    private String id;

    /*
     * The human readable name of the policy.
     */
    @Generated
    @JsonProperty(value = "name")
    private String name;

    /*
     * The number of seconds after which any offers created under this policy will be
     * expired.
     */
    @Generated
    @JsonProperty(value = "offerExpiresAfterSeconds")
    private double offerExpiresAfterSeconds;

    /*
     * Abstract base class for defining a distribution mode
     */
    @Generated
    @JsonProperty(value = "mode")
    private DistributionMode mode;

    /** Creates an instance of DistributionPolicyInternal class. */
    @Generated
    public DistributionPolicyInternal() {}

    /**
     * Get the id property: The unique identifier of the policy.
     *
     * @return the id value.
     */
    @Generated
    public String getId() {
        return this.id;
    }

    /**
     * Get the name property: The human readable name of the policy.
     *
     * @return the name value.
     */
    @Generated
    public String getName() {
        return this.name;
    }

    /**
     * Set the name property: The human readable name of the policy.
     *
     * @param name the name value to set.
     * @return the DistributionPolicyInternal object itself.
     */
    @Generated
    public DistributionPolicyInternal setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the offerExpiresAfterSeconds property: The number of seconds after which any offers created under this policy
     * will be expired.
     *
     * @return the offerExpiresAfterSeconds value.
     */
    public double getOfferExpiresAfterSeconds() {
        return this.offerExpiresAfterSeconds;
    }

    /**
     * Set the offerExpiresAfterSeconds property: The number of seconds after which any offers created under this policy
     * will be expired.
     *
     * @param offerExpiresAfterSeconds the offerExpiresAfterSeconds value to set.
     * @return the DistributionPolicyInternal object itself.
     */
    public DistributionPolicyInternal setOfferExpiresAfterSeconds(double offerExpiresAfterSeconds) {
        this.offerExpiresAfterSeconds = offerExpiresAfterSeconds;
        return this;
    }

    /**
     * Get the mode property: Abstract base class for defining a distribution mode.
     *
     * @return the mode value.
     */
    @Generated
    public DistributionMode getMode() {
        return this.mode;
    }

    /**
     * Set the mode property: Abstract base class for defining a distribution mode.
     *
     * @param mode the mode value to set.
     * @return the DistributionPolicyInternal object itself.
     */
    @Generated
    public DistributionPolicyInternal setMode(DistributionMode mode) {
        this.mode = mode;
        return this;
    }

    /*
     * Concurrency Token.
     */
    @Generated
    @JsonProperty(value = "etag", access = JsonProperty.Access.WRITE_ONLY)
    private String etag;

    /**
     * Get the etag property: Concurrency Token.
     *
     * @return the etag value.
     */
    @Generated
    public String getEtag() {
        return this.etag;
    }
}

