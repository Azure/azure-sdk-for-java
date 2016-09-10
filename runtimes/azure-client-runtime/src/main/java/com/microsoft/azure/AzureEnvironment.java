/**
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 *
 */

package com.microsoft.azure;

/**
 * An instance of this class describes an environment in Azure.
 */
public final class AzureEnvironment {
    /**
     * Base URL for calls to Azure management API.
     */
    private final String resourceManagerEndpoint;

    /**
     * ActiveDirectory Endpoint for the authentications.
     */
    private String authenticationEndpoint;

    /**
     * Base URL for calls to service management and authentications to Active Directory.
     */
    private String managementEndpoint;

    /**
     * Base URL for calls to graph API.
     */
    private String graphEndpoint;

    /**
     * Determines whether the authentication endpoint should
     * be validated with Azure AD. Default value is true.
     */
    private boolean validateAuthority;

    /**
     * Initializes an instance of AzureEnvironment class.
     *
     * @param authenticationEndpoint ActiveDirectory Endpoint for the Azure Environment.
     * @param managementEndpoint token audience for an endpoint.
     * @param resourceManagerEndpoint the base URL for the current environment.
     * @param graphEndpoint the base URL for graph API.
     */
    public AzureEnvironment(
            String authenticationEndpoint,
            String managementEndpoint,
            String resourceManagerEndpoint,
            String graphEndpoint) {
        this.authenticationEndpoint = authenticationEndpoint;
        this.managementEndpoint = managementEndpoint;
        this.resourceManagerEndpoint = resourceManagerEndpoint;
        this.graphEndpoint = graphEndpoint;
        this.validateAuthority = false;
    }

    /**
     * Provides the settings for authentication with Azure.
     */
    public static final AzureEnvironment AZURE = new AzureEnvironment(
            "https://login.microsoftonline.com/",
            "https://management.core.windows.net/",
            "https://management.azure.com/",
            "https://graph.windows.net/");

    /**
     * Provides the settings for authentication with Azure China.
     */
    public static final AzureEnvironment AZURE_CHINA = new AzureEnvironment(
            "https://login.chinacloudapi.cn/",
            "https://management.core.chinacloudapi.cn/",
            "https://management.chinacloudapi.cn/",
            "https://graph.chinacloudapi.cn/");

    /**
     * Provides the settings for authentication with Azure US Government.
     */
    public static final AzureEnvironment AZURE_US_GOVERNMENT = new AzureEnvironment(
            "https://login.microsoftonline.com/",
            "https://management.core.usgovcloudapi.net/",
            "https://management.usgovcloudapi.net/",
            "https://graph.windows.net/");

    /**
     * Provides the settings for authentication with Azure Germany.
     */
    public static final AzureEnvironment AZURE_GERMANY = new AzureEnvironment(
            "https://login.microsoftonline.de/",
            "https://management.core.cloudapi.de/",
            "https://management.microsoftazure.de/",
            "https://graph.cloudapi.de/");

    /**
     * Gets the base URL of the management service.
     *
     * @return the Base URL for the management service.
     */
    public String getBaseUrl() {
        return this.resourceManagerEndpoint;
    }

    /**
     * @return a builder for the rest client.
     */
    public RestClient.Builder.Buildable newRestClientBuilder() {
        return new RestClient.Builder()
                .withDefaultBaseUrl(this)
                .withInterceptor(new RequestIdHeaderInterceptor());
    }

    /**
     * @return the ActiveDirectory Endpoint for the Azure Environment.
     */
    public String getAuthenticationEndpoint() {
        return authenticationEndpoint;
    }

    /**
     * @return the Azure Resource Manager endpoint for the environment.
     */
    public String getManagementEndpoint() {
        return managementEndpoint;
    }

    /**
     * @return the Graph API endpoint.
     */
    public String getGraphEndpoint() {
        return graphEndpoint;
    }

    /**
     * Gets whether the authentication endpoint should
     * be validated with Azure AD.
     *
     * @return true if the authentication endpoint should be validated with
     * Azure AD, false otherwise.
     */
    public boolean isValidateAuthority() {
        return validateAuthority;
    }

    /**
     * Sets whether the authentication endpoint should
     * be validated with Azure AD.
     *
     * @param validateAuthority true if the authentication endpoint should
     * be validated with Azure AD, false otherwise.
     */
    public void setValidateAuthority(boolean validateAuthority) {
        this.validateAuthority = validateAuthority;
    }
}
