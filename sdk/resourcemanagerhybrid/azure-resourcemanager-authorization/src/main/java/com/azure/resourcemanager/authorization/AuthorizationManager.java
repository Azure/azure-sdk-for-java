// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.authorization;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.authorization.fluent.AuthorizationManagementClient;
import com.azure.resourcemanager.authorization.fluent.MicrosoftGraphClient;
import com.azure.resourcemanager.authorization.implementation.AuthorizationManagementClientBuilder;
import com.azure.resourcemanager.authorization.implementation.ActiveDirectoryApplicationsImpl;
import com.azure.resourcemanager.authorization.implementation.ActiveDirectoryGroupsImpl;
import com.azure.resourcemanager.authorization.implementation.ActiveDirectoryUsersImpl;
import com.azure.resourcemanager.authorization.implementation.MicrosoftGraphClientBuilder;
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
import com.azure.resourcemanager.resources.fluentcore.model.HasServiceClient;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;

/** Entry point to Azure Authorization and Graph RBAC management. */
public final class AuthorizationManager implements HasServiceClient<MicrosoftGraphClient> {
    private final String tenantId;
    private final AzureEnvironment environment;
    private ResourceManagerUtils.InternalRuntimeContext internalContext;
    // The sdk clients
    private final MicrosoftGraphClient microsoftGraphClient;
    private final AuthorizationManagementClient authorizationManagementClient;
    // The collections
    private ActiveDirectoryUsers activeDirectoryUsers;
    private ActiveDirectoryGroups activeDirectoryGroups;
    private ServicePrincipals servicePrincipals;
    private ActiveDirectoryApplications applications;
    private RoleAssignments roleAssignments;
    private RoleDefinitions roleDefinitions;

    private static final String DEFAULT_GRAPH_ENDPOINT_SUFFIX = "v1.0";

    /**
     * Creates an instance of AuthorizationManager that exposes Authorization
     * and Graph RBAC management API entry points.
     *
     * @param credential the credential to use
     * @param profile the profile to use
     * @return the AuthorizationManager instance
     */
    public static AuthorizationManager authenticate(TokenCredential credential, AzureProfile profile) {
        return authenticate(HttpPipelineProvider.buildHttpPipeline(credential, profile), profile);
    }

    /**
     * Creates an instance of AuthorizationManager that exposes Authorization
     * and Graph RBAC management API entry points.
     *
     * @param httpPipeline the HttpPipeline to be used for API calls
     * @param profile the profile used in Active Directory
     * @return the AuthorizationManager instance
     */
    private static AuthorizationManager authenticate(HttpPipeline httpPipeline, AzureProfile profile) {
        return new AuthorizationManager(httpPipeline, profile);
    }

    /**
     * Get a Configurable instance that can be used to create AuthorizationManager with optional configuration.
     *
     * @return the instance allowing configurations
     */
    public static Configurable configure() {
        return new AuthorizationManager.ConfigurableImpl();
    }

    @Override
    public MicrosoftGraphClient serviceClient() {
        return this.microsoftGraphClient;
    }

    /** The interface allowing configurations to be set. */
    public interface Configurable extends AzureConfigurable<Configurable> {
        /**
         * Creates an instance of AuthorizationManager that exposes Authorization
         * and Graph RBAC management API entry points.
         *
         * @param credential the credential to use
         * @param profile the profile to use
         * @return the AuthorizationManager instance
         */
        AuthorizationManager authenticate(TokenCredential credential, AzureProfile profile);
    }

    /** The implementation for Configurable interface. */
    private static class ConfigurableImpl extends AzureConfigurableImpl<Configurable> implements Configurable {
        public AuthorizationManager authenticate(TokenCredential credential, AzureProfile profile) {
            return AuthorizationManager
                .authenticate(buildHttpPipeline(credential, profile), profile);
        }
    }

    private AuthorizationManager(HttpPipeline httpPipeline, AzureProfile profile) {
        String graphEndpoint = profile.getEnvironment().getMicrosoftGraphEndpoint();
        graphEndpoint = graphEndpoint.endsWith("/")
            ? graphEndpoint + DEFAULT_GRAPH_ENDPOINT_SUFFIX
            : graphEndpoint + "/" + DEFAULT_GRAPH_ENDPOINT_SUFFIX;

        this.microsoftGraphClient =
            new MicrosoftGraphClientBuilder()
                .pipeline(httpPipeline)
                .endpoint(graphEndpoint)
                .buildClient();
        this.authorizationManagementClient =
            new AuthorizationManagementClientBuilder()
                .pipeline(httpPipeline)
                .endpoint(profile.getEnvironment().getResourceManagerEndpoint())
                .subscriptionId(profile.getSubscriptionId())
                .buildClient();
        this.tenantId = profile.getTenantId();
        this.environment = profile.getEnvironment();
    }

    /**
     * @return wrapped inner authorization client providing direct access to auto-generated API implementation, based on
     *     Azure REST API
     */
    public AuthorizationManagementClient roleServiceClient() {
        return authorizationManagementClient;
    }

    /** @return the tenant ID the graph client is associated with */
    public String tenantId() {
        return tenantId;
    }

    /** @return the environment the graph client is associated with */
    public AzureEnvironment environment() {
        return environment;
    }

    /** @return the {@link ResourceManagerUtils.InternalRuntimeContext} associated with this manager */
    public ResourceManagerUtils.InternalRuntimeContext internalContext() {
        if (internalContext == null) {
            internalContext = new ResourceManagerUtils.InternalRuntimeContext();
        }
        return internalContext;
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
            servicePrincipals = new ServicePrincipalsImpl(this);
        }
        return servicePrincipals;
    }

    /** @return the application management API entry point */
    public ActiveDirectoryApplications applications() {
        if (applications == null) {
            applications = new ActiveDirectoryApplicationsImpl(this);
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
