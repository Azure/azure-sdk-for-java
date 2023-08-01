// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.implementation;

import com.azure.core.annotation.Fluent;
import com.azure.security.keyvault.certificates.models.LifetimeAction;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Action and the trigger that will be performed by Key Vault over the lifetime
 * of a certificate.
 */
@Fluent
public final class LifetimeActionRequest {
    /**
     * The condition that will execute the action.
     */
    @JsonProperty(value = "trigger")
    private Trigger trigger;

    /**
     * The action that will be executed.
     */
    @JsonProperty(value = "action")
    private Action action;

    /**
     * Creates an instance of {@link LifetimeActionRequest}.
     *
     * @param lifeTimeAction The lifetime action the request is based on.
     */
    public LifetimeActionRequest(LifetimeAction lifeTimeAction) {
        action = new Action()
            .setActionType(lifeTimeAction.getAction());
        trigger = new Trigger()
            .daysBeforeExpiry(lifeTimeAction.getDaysBeforeExpiry())
            .lifetimePercentage(lifeTimeAction.getLifetimePercentage());
    }

    /**
     * Get the trigger value.
     *
     * @return the trigger value
     */
    public Trigger trigger() {
        return this.trigger;
    }

    /**
     * Set the trigger value.
     *
     * @param trigger the trigger value to set
     * @return the LifetimeActionRequest object itself.
     */
    public LifetimeActionRequest withTrigger(Trigger trigger) {
        this.trigger = trigger;
        return this;
    }

    /**
     * Get the action value.
     *
     * @return the action value
     */
    public Action action() {
        return this.action;
    }

    /**
     * Set the action value.
     *
     * @param action the action value to set
     * @return the LifetimeActionRequest object itself.
     */
    public LifetimeActionRequest action(Action action) {
        this.action = action;
        return this;
    }

}
