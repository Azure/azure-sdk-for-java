// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Represents a LifeTimeAction in {@link CertificatePolicy}
 */
public final class LifetimeAction {

    /**
     * The type of the action. Possible values include: 'EmailContacts',
     * 'AutoRenew'.
     */
    private CertificatePolicyAction certificatePolicyAction;

    /**
     * Percentage of lifetime at which to trigger. Value should be between 1
     * and 99.
     */
    private Integer lifetimePercentage;

    /**
     * Days before expiry to attempt renewal. Value should be between 1 and
     * validity_in_months multiplied by 27. If validity_in_months is 36, then
     * value should be between 1 and 972 (36 * 27).
     */
    private Integer daysBeforeExpiry;

    LifetimeAction() { }

    /**
     * Creates a new LifetimeAction instance, with the provided {@link CertificatePolicyAction}.
     * @param action The action type of this LifetimeAction.
     */
    public LifetimeAction(CertificatePolicyAction action) {
        this.certificatePolicyAction = action;
    }

    /**
     * Get the lifetime percentage.
     *
     * @return the lifetime percentage
     */
    public Integer getLifetimePercentage() {
        return this.lifetimePercentage;
    }

    /**
     * Set the lifetime percentage.
     *
     * @param lifetimePercentage The lifetime percentage to set
     * @return the LifetimeAction object itself.
     */
    public LifetimeAction setLifetimePercentage(Integer lifetimePercentage) {
        this.lifetimePercentage = lifetimePercentage;
        return this;
    }

    /**
     * Get the days before expiry.
     *
     * @return the days before expiry
     */
    public Integer getDaysBeforeExpiry() {
        return this.daysBeforeExpiry;
    }

    /**
     * Set the days before expiry.
     *
     * @param daysBeforeExpiry The days before expiry to set
     * @return the LifetimeAction object itself.
     */
    public LifetimeAction setDaysBeforeExpiry(Integer daysBeforeExpiry) {
        this.daysBeforeExpiry = daysBeforeExpiry;
        return this;
    }

    /**
     * Get the lifetime action.
     *
     * @return the lifetime action
     */
    public CertificatePolicyAction getAction() {
        return this.certificatePolicyAction;
    }


    @JsonProperty(value = "action")
    private void unpackAction(Map<String, Object> action) {
        certificatePolicyAction = CertificatePolicyAction.fromString((String) action.get("action_type"));
    }

    @JsonProperty(value = "trigger")
    private void unpackTrigger(Map<String, Object> triggerProps) {
        lifetimePercentage = (Integer) triggerProps.get("lifetime_percentage");
        daysBeforeExpiry = (Integer) triggerProps.get("days_before_expiry");
    }
}
