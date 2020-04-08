// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.management.resources.fluentcore.authentication;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.AzureEnvironment;

/**
 * A credential provider for Azure authentication
 * before using the APIs provided in the library.
 */
public class AzureTokenCredential {

    private String domain;
    private String defaultSubscriptionId;
    private String clientId;
    private AzureEnvironment environment;
    private TokenCredential value;

    /**
     * Constructor to create an AzureTokenCredential.
     * @param domain the domain or tenant ID.
     * @param defaultSubscriptionId the default subscription ID.
     * @param clientId the client ID.
     * @param environment the Azure environment.
     * @param value the credential value.
     */
    AzureTokenCredential(String domain, String defaultSubscriptionId, String clientId, AzureEnvironment environment, TokenCredential value) {
        this.domain = domain;
        this.defaultSubscriptionId = defaultSubscriptionId;
        this.clientId = clientId;
        this.environment = environment;
        this.value = value;
    }

    /**
     * @return the domain.
     */
    public String getDomain() {
        return this.domain;
    }

    /**
     * @return the default subscription ID.
     */
    public String getDefaultSubscriptionId() {
        return this.defaultSubscriptionId;
    }

    /**
     * @return the client ID.
     */
    public String getClientId() {
        return this.clientId;
    }

    /**
     * @return the Azure environment.
     */
    public AzureEnvironment getEnvironment() {
        return this.environment;
    }

    /**
     * @return the credential value.
     */
    public TokenCredential getValue() {
        return this.value;
    }
}
