// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates;

import com.azure.security.keyvault.certificates.models.CertificatePolicyAction;
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
    private CertificatePolicyAction certificatePolicyAction;

    /**
     * Get the certificatePolicyAction value.
     *
     * @return the updated certificatePolicyAction value
     */
    CertificatePolicyAction getActionType() {
        return this.certificatePolicyAction;
    }

    /**
     * Set the certificatePolicyAction value.
     *
     * @param certificatePolicyAction the certificatePolicyAction value to set
     * @return the Action object itself.
     */
    Action setActionType(CertificatePolicyAction certificatePolicyAction) {
        this.certificatePolicyAction = certificatePolicyAction;
        return this;
    }
}
