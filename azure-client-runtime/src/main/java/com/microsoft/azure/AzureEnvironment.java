/**
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 *
 */

package com.microsoft.azure;

import com.microsoft.rest.protocol.Environment;

import java.lang.reflect.Field;

/**
 * An instance of this class describes an environment in Azure.
 */
public final class AzureEnvironment implements Environment {
    /**
     * Base URL for calls to Azure management API.
     */
    private String resourceManagerEndpoint;

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
    public String resourceManagerEndpoint() {
        return this.resourceManagerEndpoint;
    }

    /**
     * @return the ActiveDirectory Endpoint for the Azure Environment.
     */
    public String authenticationEndpoint() {
        return authenticationEndpoint;
    }

    /**
     * @return the Azure Resource Manager endpoint for the environment.
     */
    public String managementEndpoint() {
        return managementEndpoint;
    }

    /**
     * @return the Graph API endpoint.
     */
    public String graphEndpoint() {
        return graphEndpoint;
    }

    /**
     * The enum representing available endpoints in an environment.
     */
    public enum Endpoint implements Environment.Endpoint {
        /** Azure Resource Manager endpoint. */
        RESOURCE_MANAGER("resourceManagerEndpoint"),
        /** Azure Active Directory Graph APIs endpoint. */
        GRAPH("graphEndpoint");

        private String field;

        Endpoint(String value) {
            this.field = value;
        }

        @Override
        public String identifier() {
            return field;
        }

        @Override
        public String toString() {
            return field;
        }
    }

    /**
     * Get the endpoint URL for the current environment.
     *
     * @param endpoint the endpoint
     * @return the URL
     */
    public String url(Environment.Endpoint endpoint) {
        try {
            Field f = AzureEnvironment.class.getDeclaredField(endpoint.identifier());
            f.setAccessible(true);
            return (String) f.get(this);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Unable to reflect on field " + endpoint.identifier(), e);
        }
    }
}
