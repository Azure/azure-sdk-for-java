/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.v2.credentials;

import com.microsoft.azure.v2.AzureEnvironment;
import com.microsoft.rest.v2.credentials.TokenCredentials;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.util.Map;

/**
 * AzureTokenCredentials represents a credentials object with access to Azure
 * Resource management.
 */
public abstract class AzureTokenCredentials extends TokenCredentials {
    private final AzureEnvironment environment;
    private final String domain;
    private String defaultSubscription;

    private Proxy proxy;

    /**
     * Initializes a new instance of the AzureTokenCredentials.
     *
     * @param environment the Azure environment to use
     * @param domain the tenant or domain the credential is authorized to
     */
    public AzureTokenCredentials(AzureEnvironment environment, String domain) {
        super("Bearer", null);
        this.environment = (environment == null) ? AzureEnvironment.AZURE : environment;
        this.domain = domain;
    }

    /**
     * Gets the token from the given endpoint.
     *
     * @param uri the url
     * @return the token
     * @throws IOException IOException
     */
    public final String getTokenFromUri(String uri) throws IOException {
        URL url = new URL(uri);
        String host = url.getHost();
        String resource = environment().activeDirectoryResourceId();
        for (Map.Entry<String, String> endpoint : environment().endpoints().entrySet()) {
            if (host.contains(endpoint.getValue())) {
                if (endpoint.getKey().equals(AzureEnvironment.Endpoint.KEYVAULT.identifier())) {
                    resource = String.format("https://%s/", endpoint.getValue().replaceAll("^\\.*", ""));
                    break;
                } else if (endpoint.getKey().equals(AzureEnvironment.Endpoint.GRAPH.identifier())) {
                    resource = environment().graphEndpoint();
                } else if (endpoint.getKey().equals(AzureEnvironment.Endpoint.DATA_LAKE_STORE.identifier())
                               || endpoint.getKey().equals(AzureEnvironment.Endpoint.DATA_LAKE_ANALYTICS.identifier())) {
                    resource = environment().dataLakeEndpointResourceId();
                }
            }
        }
        return getToken(resource);
    }

    /**
     * Override this method to provide the mechanism to get a token.
     *
     * @param resource the resource the access token is for
     * @return the token to access the resource
     * @throws IOException exceptions from IO
     */
    public abstract String getToken(String resource) throws IOException;

    @Override
    public String authorizationHeaderValue(String uri) throws IOException {
        return "Bearer " + getTokenFromUri(uri);
    }

    /**
     * Override this method to provide the domain or tenant ID the token is valid in.
     *
     * @return the domain or tenant ID string
     */
    public String domain() {
        return domain;
    }

    /**
     * @return the environment details the credential has access to.
     */
    public AzureEnvironment environment() {
        return environment;
    }

    /**
     * @return The default subscription ID, if any
     */
    public String defaultSubscriptionId() {
        return defaultSubscription;
    }

    /**
     * Set default subscription ID.
     *
     * @param subscriptionId the default subscription ID.
     * @return the credentials object itself.
     */
    public AzureTokenCredentials withDefaultSubscriptionId(String subscriptionId) {
        this.defaultSubscription = subscriptionId;
        return this;
    }

    /**
     * @return the proxy being used for accessing Active Directory.
     */
    public Proxy proxy() {
        return proxy;
    }

    /**
     * Set the proxy used for accessing Active Directory.
     * @param proxy the proxy to use
     * @return the credential itself
     */
    public AzureTokenCredentials withProxy(Proxy proxy) {
        this.proxy = proxy;
        return this;
    }
}
