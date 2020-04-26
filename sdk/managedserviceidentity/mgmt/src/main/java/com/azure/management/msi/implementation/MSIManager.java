// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.management.msi.implementation;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.management.graphrbac.implementation.GraphRbacManager;
import com.azure.management.msi.Identities;
import com.azure.management.msi.models.ManagedServiceIdentityClientBuilder;
import com.azure.management.msi.models.ManagedServiceIdentityClientImpl;
import com.azure.management.resources.fluentcore.arm.AzureConfigurable;
import com.azure.management.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.azure.management.resources.fluentcore.arm.implementation.Manager;
import com.azure.management.resources.fluentcore.profile.AzureProfile;
import com.azure.management.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.management.resources.fluentcore.utils.SdkContext;

/**
 * Entry point to Azure Managed Service Identity (MSI) resource management.
 */
public final class MSIManager extends Manager<MSIManager, ManagedServiceIdentityClientImpl> {
    private final GraphRbacManager rbacManager;

    private Identities identities;

    /**
     * Get a Configurable instance that can be used to create MSIManager with optional configuration.
     *
     * @return the instance allowing configurations
     */
    public static Configurable configure() {
        return new MSIManager.ConfigurableImpl();
    }

    /**
     * Creates an instance of MSIManager that exposes Managed Service Identity (MSI)
     * resource management API entry points.
     *
     * @param credential the credential to use
     * @param profile the profile to use
     * @return the MSIManager
     */
    public static MSIManager authenticate(TokenCredential credential, AzureProfile profile) {
        return new MSIManager(HttpPipelineProvider.buildHttpPipeline(credential, profile), profile, new SdkContext());
    }

    /**
     * Creates an instance of MSIManager that exposes Managed Service Identity (MSI)
     * resource management API entry points.
     *
     * @param httpPipeline the HttpPipeline to be used for API calls.
     * @param profile the profile to use
     * @return the MSIManager
     */
    public static MSIManager authenticate(HttpPipeline httpPipeline, AzureProfile profile) {
        return  authenticate(httpPipeline, profile, new SdkContext());
    }

    /**
     * Creates an instance of MSIManager that exposes Managed Service Identity (MSI)
     * resource management API entry points.
     *
     * @param httpPipeline the HttpPipeline to be used for API calls.
     * @param profile the profile to use
     * @param sdkContext the sdk context
     * @return the MSIManager
     */
    public static MSIManager authenticate(HttpPipeline httpPipeline, AzureProfile profile, SdkContext sdkContext) {
        return new MSIManager(httpPipeline, profile, sdkContext);
    }

    /**
     * The interface allowing configurations to be set.
     */
    public interface Configurable extends AzureConfigurable<Configurable> {
        /**
         * Creates an instance of MSIManager that exposes EventHub management API entry points.
         *
         * @param credential the credential to use
         * @param profile the profile to use
         * @return the interface exposing Managed Service Identity (MSI)
         * resource management API entry points that work across subscriptions
         */
        MSIManager authenticate(TokenCredential credential, AzureProfile profile);
    }

    /**
     * The implementation for Configurable interface.
     */
    private static final class ConfigurableImpl extends AzureConfigurableImpl<Configurable> implements Configurable {
        public MSIManager authenticate(TokenCredential credential, AzureProfile profile) {
            return MSIManager.authenticate(buildHttpPipeline(credential, profile), profile);
        }
    }

    private MSIManager(HttpPipeline httpPipeline, AzureProfile profile, SdkContext sdkContext) {
        super(httpPipeline, profile, new ManagedServiceIdentityClientBuilder()
                .pipeline(httpPipeline)
                .host(profile.environment().getResourceManagerEndpoint())
                .subscriptionId(profile.subscriptionId())
                .buildClient(),
                sdkContext);
        rbacManager = GraphRbacManager.authenticate(httpPipeline,
            profile,
            sdkContext);
    }

    /**
     * @return entry point to Azure MSI Identity resource management API
     */
    public Identities identities() {
        if (identities == null) {
            this.identities = new IdentitesImpl(this.inner().userAssignedIdentities(), this);
        }
        return this.identities;
    }

    /**
     * @return the Graph RBAC manager.
     */
    public GraphRbacManager graphRbacManager() {
        return this.rbacManager;
    }
}
