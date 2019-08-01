/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.msiAuthTokenProvider;

/**
 * Defines the configuration to be used for retrieving access token from
 * within a VM with user assigned or system assigned MSI enabled.
 */
public class MSIConfigurationForVirtualMachine {
    private final String managementEndpoint;
    private String resource;
    private MSITokenSource tokenSource = MSITokenSource.IMDS_ENDPOINT;
    private String objectId;
    private String clientId;
    private String identityId;
    private int maxRetry = -1;
    private int customTimeout = -1;

    /**
     * Creates MSIConfigurationForVirtualMachine.
     *
     * @param managementEndpoint azure management endpoint
     */
    public MSIConfigurationForVirtualMachine(String managementEndpoint) {
        this.managementEndpoint = managementEndpoint;
    }

    /**
     * Creates MSIConfigurationForVirtualMachine.
     */
    public MSIConfigurationForVirtualMachine() {
        this(MSICredentials.DEFAULT_AZURE_MANAGEMENT_ENDPOINT);
    }

    /**
     * @return the azure management Endpoint.
     */
    public String managementEndpoint() {
        return this.managementEndpoint;
    }

    /**
     * @return the token retrieval source (either MSI extension running in VM or IMDS service).
     */
    public MSITokenSource tokenSource() {
        if (this.tokenSource == null) {
            this.tokenSource = MSITokenSource.IMDS_ENDPOINT;
        }
        return this.tokenSource;
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
     * @return the principal id of user assigned or system assigned identity.
     */
    public String objectId() {
        return this.objectId;
    }
    /**
     * @return the client id of user assigned or system assigned identity.
     */
    public String clientId() {
        return this.clientId;
    }
    /**
     * @return the ARM resource id of the user assigned identity resource.
     */
    public String identityId() {
        return this.identityId;
    }

    /**
     * @return the maximum retries allowed.
     */
    public int maxRetry() {
        return this.maxRetry;
    }

    /**
     * @return the custom timeout (in milliseconds) when getting the token from IMDS
     */
    public int customTimeout() { return this.customTimeout; }

    /**
     * Specifies the token retrieval source.
     *
     * @param tokenSource the source of token
     *
     * @return MSIConfigurationForVirtualMachine
     */
    public MSIConfigurationForVirtualMachine withTokenSource(MSITokenSource tokenSource) {
        this.tokenSource = tokenSource;
        return this;
    }

    /**
     * Specifies the token audience.
     *
     * @param resource the audience of the token.
     *
     * @return MSIConfigurationForVirtualMachine
     */
    public MSIConfigurationForVirtualMachine withResource(String resource) {
        this.resource = resource;
        return this;
    }

    /**
     * specifies the principal id of user assigned or system assigned identity.
     *
     * @param objectId the object (principal) id
     * @return MSIConfigurationForVirtualMachine
     */
    public MSIConfigurationForVirtualMachine withObjectId(String objectId) {
        this.objectId = objectId;
        return this;
    }

    /**
     * Specifies the client id of user assigned or system assigned identity.
     *
     * @param clientId the client id
     * @return MSIConfigurationForVirtualMachine
     */
    public MSIConfigurationForVirtualMachine withClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    /**
     * Specifies the ARM resource id of the user assigned identity resource.
     *
     * @param identityId the identity ARM id
     * @return MSIConfigurationForVirtualMachine
     */
    public MSIConfigurationForVirtualMachine withIdentityId(String identityId) {
        this.identityId = identityId;
        return this;
    }

    /**
     * Specifies the the maximum retries allowed.
     *
     * @param maxRetry max retry count
     * @return MSIConfigurationForVirtualMachine
     */
    public MSIConfigurationForVirtualMachine withMaxRetry(int maxRetry) {
        this.maxRetry = maxRetry;
        return this;
    }

    /**
     * Specifies the custom timeout (in milliseconds) to be used for IMDS retries.
     *
     * @param timeoutInMs the total timeout value in milliseconds;
     * @return MSIConfigurationForVirtualMachine
     */
    public MSIConfigurationForVirtualMachine withCustomTimeout(int timeoutInMs) {
        this.customTimeout = timeoutInMs;
        return this;
    }

    @Override
    public MSIConfigurationForVirtualMachine clone() {
        MSIConfigurationForVirtualMachine copy = new MSIConfigurationForVirtualMachine(this.managementEndpoint);
        if (this.clientId() != null) {
            copy.withClientId(this.clientId());
        }
        if (this.identityId() != null) {
            copy.withIdentityId(this.identityId());
        }
        if (this.objectId() != null) {
            copy.withObjectId(this.objectId());
        }
        if (this.resource() != null) {
            copy.withResource(this.resource());
        }
        if (this.tokenSource() != null) {
            copy.withTokenSource(this.tokenSource());
        }
        copy.withMaxRetry(this.maxRetry());
        copy.withCustomTimeout(this.customTimeout());
        return copy;
    }


    /**
     * The source of MSI token.
     */
    public enum MSITokenSource {
        /**
         * Indicate that token should be retrieved from IMDS service.
         */
        IMDS_ENDPOINT
    }
}
