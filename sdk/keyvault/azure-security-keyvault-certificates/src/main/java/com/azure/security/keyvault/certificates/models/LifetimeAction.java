// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.models;

import com.azure.security.keyvault.certificates.implementation.models.Action;
import com.azure.security.keyvault.certificates.implementation.models.Trigger;

/**
 * Represents a LifeTimeAction in {@link CertificatePolicy}
 */
public final class LifetimeAction {
    private final com.azure.security.keyvault.certificates.implementation.models.LifetimeAction impl;

    com.azure.security.keyvault.certificates.implementation.models.LifetimeAction getImpl() {
        return this.impl;
    }

    /**
     * Creates a new LifetimeAction instance, with the provided {@link CertificatePolicyAction}.
     * @param action The action type of this LifetimeAction.
     */
    public LifetimeAction(CertificatePolicyAction action) {
        this.impl = new com.azure.security.keyvault.certificates.implementation.models.LifetimeAction()
            .setAction(new Action().setActionType(action));
    }

    LifetimeAction(com.azure.security.keyvault.certificates.implementation.models.LifetimeAction impl) {
        this.impl = impl;
    }

    /**
     * Get the lifetime percentage.
     *
     * @return the lifetime percentage
     */
    public Integer getLifetimePercentage() {
        return impl.getTrigger() == null ? null : impl.getTrigger().getLifetimePercentage();
    }

    /**
     * Set the lifetime percentage.
     *
     * @param lifetimePercentage The lifetime percentage to set
     * @return the LifetimeAction object itself.
     */
    public LifetimeAction setLifetimePercentage(Integer lifetimePercentage) {
        if (impl.getTrigger() == null) {
            impl.setTrigger(new Trigger());
        }

        impl.getTrigger().setLifetimePercentage(lifetimePercentage);
        return this;
    }

    /**
     * Get the days before expiry.
     *
     * @return the days before expiry
     */
    public Integer getDaysBeforeExpiry() {
        return impl.getTrigger() == null ? null : impl.getTrigger().getDaysBeforeExpiry();
    }

    /**
     * Set the days before expiry.
     *
     * @param daysBeforeExpiry The days before expiry to set
     * @return the LifetimeAction object itself.
     */
    public LifetimeAction setDaysBeforeExpiry(Integer daysBeforeExpiry) {
        if (impl.getTrigger() == null) {
            impl.setTrigger(new Trigger());
        }

        impl.getTrigger().setDaysBeforeExpiry(daysBeforeExpiry);
        return this;
    }

    /**
     * Get the lifetime action.
     *
     * @return the lifetime action
     */
    public CertificatePolicyAction getAction() {
        return impl.getAction() == null ? null : impl.getAction().getActionType();
    }
}
