/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.credentials;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.rest.credentials.TokenCredentials;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;

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
        for (String endpoint : environment().endpoints().values()) {
            if (host.contains(endpoint)) {
                // Remove leading dots
                host = endpoint.replaceAll("^\\.*", "");
                break;
            }
        }
        String resource = String.format("https://%s/", host);
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
