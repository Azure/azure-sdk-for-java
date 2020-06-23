// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.management.graphrbac.implementation;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.management.graphrbac.ActiveDirectoryApplications;
import com.azure.management.graphrbac.ActiveDirectoryGroups;
import com.azure.management.graphrbac.ActiveDirectoryUsers;
import com.azure.management.graphrbac.RoleAssignments;
import com.azure.management.graphrbac.RoleDefinitions;
import com.azure.management.graphrbac.ServicePrincipals;
import com.azure.management.graphrbac.models.AuthorizationManagementClientBuilder;
import com.azure.management.graphrbac.models.AuthorizationManagementClientImpl;
import com.azure.management.graphrbac.models.GraphRbacManagementClientBuilder;
import com.azure.management.graphrbac.models.GraphRbacManagementClientImpl;
import com.azure.management.resources.fluentcore.arm.AzureConfigurable;
import com.azure.management.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.azure.management.resources.fluentcore.model.HasInner;
import com.azure.management.resources.fluentcore.profile.AzureProfile;
import com.azure.management.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.management.resources.fluentcore.utils.SdkContext;

/** Entry point to Azure Graph RBAC management. */
public final class GraphRbacManager implements HasInner<GraphRbacManagementClientImpl> {
    private String tenantId;
    private SdkContext sdkContext;
    // The sdk clients
    private final GraphRbacManagementClientImpl graphRbacManagementClient;
    private final AuthorizationManagementClientImpl authorizationManagementClient;
    // The collections
    private ActiveDirectoryUsers activeDirectoryUsers;
    private ActiveDirectoryGroups activeDirectoryGroups;
    private ServicePrincipals servicePrincipals;
    private ActiveDirectoryApplications applications;
    private RoleAssignments roleAssignments;
    private RoleDefinitions roleDefinitions;

    /**
     * Creates an instance of GraphRbacManager that exposes Graph RBAC management API entry points.
     *
     * @param credential the credential to use
     * @param profile the profile to use
     * @return the GraphRbacManager instance
     */
    public static GraphRbacManager authenticate(TokenCredential credential, AzureProfile profile) {
        return authenticate(HttpPipelineProvider.buildHttpPipeline(credential, profile), profile);
    }

    /**
     * Creates an instance of GraphRbacManager that exposes Graph RBAC management API entry points.
     *
     * @param httpPipeline the HttpPipeline to be used for API calls
     * @param profile the profile used in Active Directory
     * @return the interface exposing Graph RBAC management API entry points that work across subscriptions
     */
    public static GraphRbacManager authenticate(HttpPipeline httpPipeline, AzureProfile profile) {
        return authenticate(httpPipeline, profile, new SdkContext());
    }

    /**
     * Creates an instance of GraphRbacManager that exposes Graph RBAC management API entry points.
     *
     * @param httpPipeline the HttpPipeline to be used for API calls
     * @param profile the profile used in Active Directory
     * @param sdkContext the sdk context
     * @return the interface exposing Graph RBAC management API entry points that work across subscriptions
     */
    public static GraphRbacManager authenticate(
        HttpPipeline httpPipeline, AzureProfile profile, SdkContext sdkContext) {
        return new GraphRbacManager(httpPipeline, profile, sdkContext);
    }

    /**
     * Get a Configurable instance that can be used to create GraphRbacManager with optional configuration.
     *
     * @return the instance allowing configurations
     */
    public static Configurable configure() {
        return new GraphRbacManager.ConfigurableImpl();
    }

    @Override
    public GraphRbacManagementClientImpl inner() {
        return this.graphRbacManagementClient;
    }

    /** The interface allowing configurations to be set. */
    public interface Configurable extends AzureConfigurable<Configurable> {
        /**
         * Creates an instance of GraphRbacManager that exposes resource management API entry points.
         *
         * @param credential the credential to use
         * @param profile the profile to use
         * @return the interface exposing resource management API entry points that work across subscriptions
         */
        GraphRbacManager authenticate(TokenCredential credential, AzureProfile profile);
    }

    /** The implementation for Configurable interface. */
    private static class ConfigurableImpl extends AzureConfigurableImpl<Configurable> implements Configurable {
        public GraphRbacManager authenticate(TokenCredential credential, AzureProfile profile) {
            return GraphRbacManager
                .authenticate(HttpPipelineProvider.buildHttpPipeline(credential, profile), profile);
        }
    }

    private GraphRbacManager(HttpPipeline httpPipeline, AzureProfile profile, SdkContext sdkContext) {
        this.graphRbacManagementClient =
            new GraphRbacManagementClientBuilder()
                .pipeline(httpPipeline)
                .host(profile.environment().getGraphEndpoint())
                .tenantId(profile.tenantId())
                .buildClient();
        this.authorizationManagementClient =
            new AuthorizationManagementClientBuilder()
                .pipeline(httpPipeline)
                .host(profile.environment().getResourceManagerEndpoint())
                .subscriptionId(profile.subscriptionId())
                .buildClient();
        this.tenantId = profile.tenantId();
        this.sdkContext = sdkContext;
    }

    /**
     * @return wrapped inner authorization client providing direct access to auto-generated API implementation, based on
     *     Azure REST API
     */
    public AuthorizationManagementClientImpl roleInner() {
        return authorizationManagementClient;
    }

    /** @return the tenant ID the graph client is associated with */
    public String tenantId() {
        return tenantId;
    }

    /** @return the sdk context in graph manager */
    public SdkContext sdkContext() {
        return sdkContext;
    }

    /** @return the Active Directory user management API entry point */
    public ActiveDirectoryUsers users() {
        if (activeDirectoryUsers == null) {
            activeDirectoryUsers = new ActiveDirectoryUsersImpl(this);
        }
        return activeDirectoryUsers;
    }

    /** @return the Active Directory group management API entry point */
    public ActiveDirectoryGroups groups() {
        if (activeDirectoryGroups == null) {
            activeDirectoryGroups = new ActiveDirectoryGroupsImpl(this);
        }
        return activeDirectoryGroups;
    }

    /** @return the service principal management API entry point */
    public ServicePrincipals servicePrincipals() {
        if (servicePrincipals == null) {
            servicePrincipals = new ServicePrincipalsImpl(graphRbacManagementClient.servicePrincipals(), this);
        }
        return servicePrincipals;
    }

    /** @return the application management API entry point */
    public ActiveDirectoryApplications applications() {
        if (applications == null) {
            applications = new ActiveDirectoryApplicationsImpl(graphRbacManagementClient.applications(), this);
        }
        return applications;
    }

    /** @return the role assignment management API entry point */
    public RoleAssignments roleAssignments() {
        if (roleAssignments == null) {
            roleAssignments = new RoleAssignmentsImpl(this);
        }
        return roleAssignments;
    }

    /** @return the role definition management API entry point */
    public RoleDefinitions roleDefinitions() {
        if (roleDefinitions == null) {
            roleDefinitions = new RoleDefinitionsImpl(this);
        }
        return roleDefinitions;
    }
}
