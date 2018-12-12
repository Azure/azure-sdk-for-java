/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.msiAuthTokenProvider;

/**
 * Defines the configuration to be used for retrieving access token from
 * within an app-service with system assigned MSI enabled.
 */
public class MSIConfigurationForAppService {
    private final String managementEndpoint;
    private String resource;
    private String msiEndpoint;
    private String msiSecret;
    private String clientId;
    private String objectId;

    /**
     * Creates MSIConfigurationForAppService.
     *
     * @param managementEndpoint azure management endpoint
     */
    public MSIConfigurationForAppService(String managementEndpoint) {
        this.managementEndpoint = managementEndpoint;
    }

    /**
     * Creates MSIConfigurationForAppService.
     */
    public MSIConfigurationForAppService() {
        this(MSICredentials.DEFAULT_AZURE_MANAGEMENT_ENDPOINT);
    }

    /**
     * @return the azure management Endpoint.
     */
    public String managementEndpoint() {
        return this.managementEndpoint;
    }

    /**
     * @return the audience identifying who will consume the token.
     */
    public String resource() {
        if (this.resource == null) {
            this.resource = this.managementEndpoint;
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
     * @return the object id
     */
    public String msiObjectId() {
        return this.objectId;
    }

    /**
     * @return the client id
     */
    public String msiClientId() {
        return this.clientId;
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
        this.msiEndpoint = msiEndpoint;
        return this;
    }

    /**
     * Specify the client Id (to be used or user assigned identities)
     * @param clientId the client ID fot eh user assigned identity
     * @return MSIConfigurationForAppService
     */
    public MSIConfigurationForAppService withClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    /**
     * Specify the object Id (to be used or user assigned identities)
     * @param objectId the object ID fot eh user assigned identity
     * @return MSIConfigurationForAppService
     */
    public MSIConfigurationForAppService withObjectId(String objectId) {
        this.objectId = objectId;
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
        MSIConfigurationForAppService copy = new MSIConfigurationForAppService(this.managementEndpoint);
        if (this.resource() != null) {
            copy.withResource(this.resource());
        }
        if (this.msiEndpoint() != null) {
            copy.withMsiEndpoint(this.msiEndpoint());
        }
        if (this.msiSecret() != null) {
            copy.withMsiSecret(this.msiSecret());
        }
        if (this.msiClientId() != null) {
            copy.withClientId(this.msiClientId());
        }
        if (this.msiObjectId() != null) {
            copy.withObjectId(this.msiObjectId());
        }
        return copy;
    }
}