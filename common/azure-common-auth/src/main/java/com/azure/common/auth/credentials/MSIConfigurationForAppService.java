/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.common.auth.credentials;

import com.azure.common.AzureEnvironment;

/**
 * Defines the configuration to be used for retrieving access token from
 * within an app-service with system assigned MSI enabled.
 */
public class MSIConfigurationForAppService {
    private final AzureEnvironment environment;
    private String resource;
    private String msiEndpoint;
    private String msiSecret;

    /**
     * Creates MSIConfigurationForAppService.
     *
     * @param environment azure environment
     */
    public MSIConfigurationForAppService(AzureEnvironment environment) {
        this.environment = environment;
    }

    /**
     * Creates MSIConfigurationForAppService.
     */
    public MSIConfigurationForAppService() {
        this(AzureEnvironment.AZURE);
    }

    /**
     * @return the azure environment.
     */
    public AzureEnvironment azureEnvironment() {
        return this.environment;
    }
    /**
     * @return the audience identifying who will consume the token.
     */
    public String resource() {
        if (this.resource == null) {
            this.resource = this.environment.managementEndpoint();
        }
        return this.resource;
    }
    /**
     * @return the endpoint from which token needs to be retrieved.
     */
    public String msiEndpoint() {
        if (this.msiEndpoint == null) {
            this.msiEndpoint = System.getenv("MSI_ENDPOINT");
        }
        return this.msiEndpoint;
    }
    /**
     * @return the secret to use to retrieve the token.
     */
    public String msiSecret() {
        if (this.msiSecret == null) {
            this.msiSecret = System.getenv("MSI_SECRET");
        }
        return this.msiSecret;
    }
    /**
     * Specifies the token audience.
     *
     * @param resource the audience of the token.
     *
     * @return MSIConfigurationForAppService
     */
    public MSIConfigurationForAppService withResource(String resource) {
        this.resource = resource;
        return this;
    }
    /**
     * Specifies the endpoint from which token needs to retrieved.
     *
     * @param msiEndpoint the token endpoint.
     *
     * @return MSIConfigurationForAppService
     */
    public MSIConfigurationForAppService withMsiEndpoint(String msiEndpoint) {
        this.msiSecret = msiEndpoint;
        return this;
    }
    /**
     * Specifies secret to use to retrieve the token.
     *
     * @param msiSecret the secret.
     *
     * @return MSIConfigurationForAppService
     */
    public MSIConfigurationForAppService withMsiSecret(String msiSecret) {
        this.msiSecret = msiSecret;
        return this;
    }

    @Override
    public MSIConfigurationForAppService clone() {
        MSIConfigurationForAppService copy = new MSIConfigurationForAppService(this.azureEnvironment());
        if (this.resource() != null) {
            copy.withResource(this.resource());
        }
        if (this.msiEndpoint() != null) {
            copy.withMsiEndpoint(this.msiEndpoint());
        }
        if (this.msiSecret() != null) {
            copy.withMsiSecret(this.msiSecret());
        }
        return copy;
    }
}
