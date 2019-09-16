// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The credentials to be used for the certificate issuer.
 */
class IssuerCredentials {
    /**
     * The user name/account name/account id.
     */
    @JsonProperty(value = "account_id")
    private String accountId;

    /**
     * The password/secret/account key.
     */
    @JsonProperty(value = "pwd")
    private String password;

    /**
     * Get the accountId value.
     *
     * @return the accountId value
     */
    String accountId() {
        return this.accountId;
    }

    /**
     * Set the accountId value.
     *
     * @param accountId the accountId value to set
     * @return the IssuerCredentials object itself.
     */
    IssuerCredentials accountId(String accountId) {
        this.accountId = accountId;
        return this;
    }

    /**
     * Get the password value.
     *
     * @return the password value
     */
    String password() {
        return this.password;
    }

    /**
     * Set the password value.
     *
     * @param password the password value to set
     * @return the IssuerCredentials object itself.
     */
    IssuerCredentials password(String password) {
        this.password = password;
        return this;
    }
}
