// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.management.resources.fluentcore.authentication;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.util.Configuration;
import com.azure.identity.EnvironmentCredentialBuilder;

/**
 * Fluent credential builder for instantiating a {@link AzureTokenCredential}.
 *
 * @see AzureTokenCredential
 */
public class AzureTokenCredentialBuilder {

    private Configuration configuration;
    private String domain;
    private String defaultSubscriptionId;
    private String clientId;
    private AzureEnvironment environment;
    private TokenCredential value;

    /**
     * Constructor to create an AzureTokenCredentialBuilder.
     */
    public AzureTokenCredentialBuilder() {
        this.configuration = Configuration.getGlobalConfiguration().clone();
    }

    /**
     * Sets the domain or tenant ID for the authentication.
     * @param domain the domain or tenant ID.
     * @return the AzureTokenCredentialBuilder itself
     */
    public AzureTokenCredentialBuilder withDomain(String domain) {
        this.domain = domain;
        return this;
    }

    /**
     * Sets the default subscription ID for the authentication.
     * @param defaultSubscriptionId the default subscription ID.
     * @return the AzureTokenCredentialBuilder itself
     */
    public AzureTokenCredentialBuilder withDefaultSubscriptionId(String defaultSubscriptionId) {
        this.defaultSubscriptionId = defaultSubscriptionId;
        return this;
    }

    /**
     * Sets the client ID for the authentication.
     * @param clientId the client ID.
     * @return the AzureTokenCredentialBuilder itself
     */
    public AzureTokenCredentialBuilder withClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    /**
     * Sets the Azure environment for the authentication.
     * @param environment the Azure environment.
     * @return the AzureTokenCredentialBuilder itself
     */
    public AzureTokenCredentialBuilder withEnvironment(AzureEnvironment environment) {
        this.environment = environment;
        return this;
    }

    /**
     * Sets the credential value for the authentication.
     * @param value the credential value.
     * @return the AzureTokenCredentialBuilder itself
     */
    public AzureTokenCredentialBuilder withValue(TokenCredential value) {
        this.value = value;
        return this;
    }

    /**
     * Creates a new {@link AzureTokenCredential} with the current configurations.
     *
     * @return a {@link AzureTokenCredential} with the current configurations.
     */
    public AzureTokenCredential build() {
        if (this.domain == null) {
            this.domain = this.configuration.get(Configuration.PROPERTY_AZURE_TENANT_ID);
        }
        if (this.defaultSubscriptionId == null) {
            this.defaultSubscriptionId = this.configuration.get(Configuration.PROPERTY_AZURE_SUBSCRIPTION_ID);
        }
        if (this.clientId == null) {
            this.clientId = this.configuration.get(Configuration.PROPERTY_AZURE_CLIENT_ID);
        }
        if (this.environment == null) {
            this.environment = AzureEnvironment.AZURE;
        }
        if (this.value == null) {
            if (this.configuration.get(Configuration.PROPERTY_AZURE_CLIENT_ID) == null) {
                throw new IllegalArgumentException("Please set credential values either by credential builder or environment variables.");
            }
            this.value = new EnvironmentCredentialBuilder()
                .authorityHost(this.environment.getActiveDirectoryEndpoint())
                .build();
        }
        return new AzureTokenCredential(this.domain, this.defaultSubscriptionId, this.clientId, this.environment, this.value);
    }
}
