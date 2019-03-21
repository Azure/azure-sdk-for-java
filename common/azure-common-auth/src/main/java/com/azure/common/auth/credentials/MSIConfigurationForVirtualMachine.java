/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.common.auth.credentials;

import com.azure.common.AzureEnvironment;

/**
 * Defines the configuration to be used for retrieving access token from
 * within a VM with user assigned or system assigned MSI enabled.
 */
public class MSIConfigurationForVirtualMachine {
    private final AzureEnvironment environment;
    private String resource;
    private MSITokenSource tokenSource;
    private String objectId;
    private String clientId;
    private String identityId;
    private Integer msiPort = null;
    private int maxRetry = -1;

    /**
     * Creates MSIConfigurationForVirtualMachine.
     *
     * @param environment azure environment
     */
    public MSIConfigurationForVirtualMachine(AzureEnvironment environment) {
        this.environment = environment;
    }

    /**
     * Creates MSIConfigurationForVirtualMachine.
     */
    public MSIConfigurationForVirtualMachine() {
        this(AzureEnvironment.AZURE);
    }

    /**
     * @return the azure environment.
     */
    public AzureEnvironment azureEnvironment() {
        return this.environment;
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
            this.resource = this.environment.managementEndpoint();
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
     * @return the port of token retrieval service running in the extension.
     */
    public int msiPort() {
        if (this.msiPort == null) {
            this.msiPort = 50342;
        }
        return this.msiPort;
    }

    /**
     * @return the maximum retries allowed.
     */
    public int maxRetry() {
        return this.maxRetry;
    }

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
     * Specifies the port of token retrieval msi extension service.
     *
     * @param msiPort the port
     * @return MSIConfigurationForVirtualMachine
     */
    public MSIConfigurationForVirtualMachine withMsiPort(int msiPort) {
        this.msiPort = msiPort;
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

    @Override
    public MSIConfigurationForVirtualMachine clone() {
        MSIConfigurationForVirtualMachine copy = new MSIConfigurationForVirtualMachine(this.azureEnvironment());
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
        copy.withMsiPort(this.msiPort());
        return copy;
    }


    /**
     * The source of MSI token.
     */
    public enum MSITokenSource {
        /**
         * Indicate that token should be retrieved from MSI extension installed in the VM.
         */
        MSI_EXTENSION,
        /**
         * Indicate that token should be retrieved from IMDS service.
         */
        IMDS_ENDPOINT
    }
}