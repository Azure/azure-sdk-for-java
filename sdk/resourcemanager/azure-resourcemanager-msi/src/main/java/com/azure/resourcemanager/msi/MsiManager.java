// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.msi;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.resourcemanager.authorization.AuthorizationManager;
import com.azure.resourcemanager.msi.fluent.ManagedServiceIdentityClient;
import com.azure.resourcemanager.msi.implementation.ManagedServiceIdentityClientBuilder;
import com.azure.resourcemanager.msi.implementation.IdentitesImpl;
import com.azure.resourcemanager.msi.models.Identities;
import com.azure.resourcemanager.resources.fluentcore.arm.AzureConfigurable;
import com.azure.resourcemanager.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.azure.resourcemanager.resources.fluentcore.arm.Manager;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;

/**
 * Entry point to Azure Managed Service Identity (MSI) resource management.
 */
public final class MsiManager extends Manager<ManagedServiceIdentityClient> {
    private final AuthorizationManager authorizationManager;

    private Identities identities;

    /**
     * Get a Configurable instance that can be used to create MsiManager with optional configuration.
     *
     * @return the instance allowing configurations
     */
    public static Configurable configure() {
        return new MsiManager.ConfigurableImpl();
    }

    /**
     * Creates an instance of MsiManager that exposes Managed Service Identity (MSI)
     * resource management API entry points.
     *
     * @param credential the credential to use
     * @param profile the profile to use
     * @return the MsiManager
     */
    public static MsiManager authenticate(TokenCredential credential, AzureProfile profile) {
        return new MsiManager(HttpPipelineProvider.buildHttpPipeline(credential, profile), profile);
    }

    /**
     * Creates an instance of MsiManager that exposes Managed Service Identity (MSI)
     * resource management API entry points.
     *
     * @param httpPipeline the HttpPipeline to be used for API calls.
     * @param profile the profile to use
     * @return the MsiManager
     */
    private static MsiManager authenticate(HttpPipeline httpPipeline, AzureProfile profile) {
        return  new MsiManager(httpPipeline, profile);
    }

    /**
     * The interface allowing configurations to be set.
     */
    public interface Configurable extends AzureConfigurable<Configurable> {
        /**
         * Creates an instance of MsiManager that exposes EventHub management API entry points.
         *
         * @param credential the credential to use
         * @param profile the profile to use
         * @return the interface exposing Managed Service Identity (MSI)
         * resource management API entry points that work across subscriptions
         */
        MsiManager authenticate(TokenCredential credential, AzureProfile profile);
    }

    /**
     * The implementation for Configurable interface.
     */
    private static final class ConfigurableImpl extends AzureConfigurableImpl<Configurable> implements Configurable {
        public MsiManager authenticate(TokenCredential credential, AzureProfile profile) {
            return MsiManager.authenticate(buildHttpPipeline(credential, profile), profile);
        }
    }

    private MsiManager(HttpPipeline httpPipeline, AzureProfile profile) {
        super(httpPipeline, profile, new ManagedServiceIdentityClientBuilder()
                .pipeline(httpPipeline)
                .endpoint(profile.getEnvironment().getResourceManagerEndpoint())
                .subscriptionId(profile.getSubscriptionId())
                .buildClient());
        authorizationManager = AzureConfigurableImpl
            .configureHttpPipeline(httpPipeline, AuthorizationManager.configure())
            .authenticate(null, profile);
    }

    /**
     * @return entry point to Azure MSI Identity resource management API
     */
    public Identities identities() {
        if (identities == null) {
            this.identities = new IdentitesImpl(this.serviceClient().getUserAssignedIdentities(), this);
        }
        return this.identities;
    }

    /**
     * @return the authorization manager.
     */
    public AuthorizationManager authorizationManager() {
        return this.authorizationManager;
    }
}
