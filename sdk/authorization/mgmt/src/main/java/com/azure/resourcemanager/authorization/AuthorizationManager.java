// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.authorization;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.resourcemanager.authorization.implementation.ActiveDirectoryApplicationsImpl;
import com.azure.resourcemanager.authorization.implementation.ActiveDirectoryGroupsImpl;
import com.azure.resourcemanager.authorization.implementation.ActiveDirectoryUsersImpl;
import com.azure.resourcemanager.authorization.implementation.RoleAssignmentsImpl;
import com.azure.resourcemanager.authorization.implementation.RoleDefinitionsImpl;
import com.azure.resourcemanager.authorization.implementation.ServicePrincipalsImpl;
import com.azure.resourcemanager.authorization.models.ActiveDirectoryApplications;
import com.azure.resourcemanager.authorization.models.ActiveDirectoryGroups;
import com.azure.resourcemanager.authorization.models.ActiveDirectoryUsers;
import com.azure.resourcemanager.authorization.models.RoleAssignments;
import com.azure.resourcemanager.authorization.models.RoleDefinitions;
import com.azure.resourcemanager.authorization.models.ServicePrincipals;
import com.azure.resourcemanager.resources.fluentcore.arm.AzureConfigurable;
import com.azure.resourcemanager.resources.fluentcore.arm.implementation.AzureConfigurableImpl;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.resourcemanager.resources.fluentcore.utils.SdkContext;

/** Entry point to Azure Graph RBAC management. */
public final class AuthorizationManager implements HasInner<GraphRbacManagementClient> {
    private final String tenantId;
    private final SdkContext sdkContext;
    // The sdk clients
    private final GraphRbacManagementClient graphRbacManagementClient;
    private final AuthorizationManagementClient authorizationManagementClient;
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
    public static AuthorizationManager authenticate(TokenCredential credential, AzureProfile profile) {
        return authenticate(HttpPipelineProvider.buildHttpPipeline(credential, profile), profile);
    }

    /**
     * Creates an instance of GraphRbacManager that exposes Graph RBAC management API entry points.
     *
     * @param httpPipeline the HttpPipeline to be used for API calls
     * @param profile the profile used in Active Directory
     * @return the interface exposing Graph RBAC management API entry points that work across subscriptions
     */
    public static AuthorizationManager authenticate(HttpPipeline httpPipeline, AzureProfile profile) {
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
    public static AuthorizationManager authenticate(
        HttpPipeline httpPipeline, AzureProfile profile, SdkContext sdkContext) {
        return new AuthorizationManager(httpPipeline, profile, sdkContext);
    }

    /**
     * Get a Configurable instance that can be used to create GraphRbacManager with optional configuration.
     *
     * @return the instance allowing configurations
     */
    public static Configurable configure() {
        return new AuthorizationManager.ConfigurableImpl();
    }

    @Override
    public GraphRbacManagementClient inner() {
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
        AuthorizationManager authenticate(TokenCredential credential, AzureProfile profile);
    }

    /** The implementation for Configurable interface. */
    private static class ConfigurableImpl extends AzureConfigurableImpl<Configurable> implements Configurable {
        public AuthorizationManager authenticate(TokenCredential credential, AzureProfile profile) {
            return AuthorizationManager
                .authenticate(HttpPipelineProvider.buildHttpPipeline(credential, profile), profile);
        }
    }

    private AuthorizationManager(HttpPipeline httpPipeline, AzureProfile profile, SdkContext sdkContext) {
        this.graphRbacManagementClient =
            new GraphRbacManagementClientBuilder()
                .pipeline(httpPipeline)
                .endpoint(profile.environment().getGraphEndpoint())
                .tenantId(profile.tenantId())
                .buildClient();
        this.authorizationManagementClient =
            new AuthorizationManagementClientBuilder()
                .pipeline(httpPipeline)
                .endpoint(profile.environment().getResourceManagerEndpoint())
                .subscriptionId(profile.subscriptionId())
                .buildClient();
        this.tenantId = profile.tenantId();
        this.sdkContext = sdkContext;
    }

    /**
     * @return wrapped inner authorization client providing direct access to auto-generated API implementation, based on
     *     Azure REST API
     */
    public AuthorizationManagementClient roleInner() {
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
            servicePrincipals = new ServicePrincipalsImpl(graphRbacManagementClient.getServicePrincipals(), this);
        }
        return servicePrincipals;
    }

    /** @return the application management API entry point */
    public ActiveDirectoryApplications applications() {
        if (applications == null) {
            applications = new ActiveDirectoryApplicationsImpl(graphRbacManagementClient.getApplications(), this);
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
