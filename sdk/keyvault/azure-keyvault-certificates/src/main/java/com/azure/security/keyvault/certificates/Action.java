// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates;

import com.azure.security.keyvault.certificates.models.LifetimeActionType;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The action configured in certificate policy that will be executed at a specific trigger scenario.
 */
class Action {
    /**
     * The type of the action. Possible values include: 'EmailContacts',
     * 'AutoRenew'.
     */
    @JsonProperty(value = "action_type")
    private LifetimeActionType lifetimeActionType;

    /**
     * Get the lifetimeActionType value.
     *
     * @return the lifetimeActionType value
     */
    LifetimeActionType getActionType() {
        return this.lifetimeActionType;
    }

    /**
     * Set the lifetimeActionType value.
     *
     * @param lifetimeActionType the lifetimeActionType value to set
     * @return the Action object itself.
     */
    Action setActionType(LifetimeActionType lifetimeActionType) {
        this.lifetimeActionType = lifetimeActionType;
        return this;
    }
}
