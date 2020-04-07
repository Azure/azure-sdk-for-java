// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.management;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.AzureEnvironment;

import java.net.Proxy;

/**
 * AzureTokenCredential represents a credential object with access to Azure Resource management.
 */
public abstract class AzureTokenCredential implements TokenCredential {

    private final AzureEnvironment environment;

    private final String domain;

    private String defaultSubscription;

    private Proxy proxy;

    /**
     * Initializes a new instance of the AzureTokenCredential.
     *
     * @param environment the Azure environment to use
     * @param domain the tenant or domain the credential is authorized to
     */
    public AzureTokenCredential(AzureEnvironment environment, String domain) {
        this.environment = (environment == null) ? AzureEnvironment.AZURE : environment;
        this.domain = domain;
    }

    /**
     * Set default subscription ID.
     *
     * @param subscriptionId the default subscription ID.
     * @return the credentials object itself.
     */
    public AzureTokenCredential defaultSubscriptionId(String subscriptionId) {
        this.defaultSubscription = subscriptionId;
        return this;
    }

    /**
     * @param proxy the proxy being used for accessing Active Directory
     * @return the credential itself
     */
    public AzureTokenCredential proxy(Proxy proxy) {
        this.proxy = proxy;
        return this;
    }

    /**
     * Get default scope of MSAL for ARM
     *
     * @return default scope in string
     */
    protected String getDefaultScope() {
        return this.getEnvironment().getResourceManagerEndpoint() + "/.default";
    }

    /**
     * Override this method to provide the domain or tenant ID the token is valid in.
     *
     * @return the domain or tenant ID string
     */
    public String getDomain() {
        return domain;
    }

    /**
     * @return the environment details the credential has access to.
     */
    public AzureEnvironment getEnvironment() {
        return environment;
    }

    /**
     * @return The default subscription ID, if any
     */
    public String getDefaultSubscriptionId() {
        return defaultSubscription;
    }


    /**
     * @return the proxy being used for accessing Active Directory.
     */
    public Proxy getProxy() {
        return proxy;
    }
}
