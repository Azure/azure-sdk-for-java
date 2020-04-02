/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.graphrbac.implementation;

import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.serializer.AzureJacksonAdapter;
import com.azure.management.AzureTokenCredential;
import com.azure.management.RestClient;
import com.azure.management.RestClientBuilder;
import com.azure.management.Utils;
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
import com.azure.management.resources.fluentcore.policy.ProviderRegistrationPolicy;
import com.azure.management.resources.fluentcore.policy.ResourceManagerThrottlingPolicy;
import com.azure.management.resources.fluentcore.utils.SdkContext;

/**
 * Entry point to Azure Graph RBAC management.
 */
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
     * @param credential the credentials to use
     * @return the GraphRbacManager instance
     */
    public static GraphRbacManager authenticate(AzureTokenCredential credential) {
        return authenticate(new RestClientBuilder()
                .withBaseUrl(credential.getEnvironment().getGraphEndpoint())
                //.withInterceptor(new RequestIdHeaderInterceptor())
                .withCredential(credential)
                .withSerializerAdapter(new AzureJacksonAdapter())
                //.withResponseBuilderFactory(new AzureResponseBuilder.Factory())
                .withPolicy(new ProviderRegistrationPolicy(credential))
                .withPolicy(new ResourceManagerThrottlingPolicy())
                .buildClient(), credential.getDomain());
    }

    /**
     * Creates an instance of GraphRbacManager that exposes Graph RBAC management API entry points.
     *
     * @param restClient the RestClient to be used for API calls
     * @param tenantId the tenantId in Active Directory
     * @return the interface exposing Graph RBAC management API entry points that work across subscriptions
     */
    public static GraphRbacManager authenticate(RestClient restClient, String tenantId) {
        return authenticate(restClient, tenantId, new SdkContext());
    }

    /**
     * Creates an instance of GraphRbacManager that exposes Graph RBAC management API entry points.
     *
     * @param restClient the RestClient to be used for API calls
     * @param tenantId the tenantId in Active Directory
     * @param sdkContext the sdk context
     * @return the interface exposing Graph RBAC management API entry points that work across subscriptions
     */
    public static GraphRbacManager authenticate(RestClient restClient, String tenantId, SdkContext sdkContext) {
        return new GraphRbacManager(restClient, tenantId, sdkContext);
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

    /**
     * The interface allowing configurations to be set.
     */
    public interface Configurable extends AzureConfigurable<Configurable> {
        /**
         * Creates an instance of GraphRbacManager that exposes resource management API entry points.
         *
         * @param credentials the credentials to use
         * @return the interface exposing resource management API entry points that work across subscriptions
         */
        GraphRbacManager authenticate(AzureTokenCredential credentials);
    }

    /**
     * The implementation for Configurable interface.
     */
    private static class ConfigurableImpl extends AzureConfigurableImpl<Configurable> implements Configurable {
        public GraphRbacManager authenticate(AzureTokenCredential credential) {
            return GraphRbacManager.authenticate(
                    buildRestClient(credential, AzureEnvironment.Endpoint.RESOURCE_MANAGER),
                    credential.getDomain());
        }
    }

    private GraphRbacManager(RestClient restClient, String tenantId, SdkContext sdkContext) {
        String graphEndpoint = AzureEnvironment.AZURE.getGraphEndpoint();
        String resourceManagerEndpoint = restClient.getBaseUrl().toString();
        if (restClient.getCredential() instanceof AzureTokenCredential) {
            graphEndpoint = ((AzureTokenCredential) restClient.getCredential()).getEnvironment().getGraphEndpoint();
            resourceManagerEndpoint = ((AzureTokenCredential) restClient.getCredential()).getEnvironment().getResourceManagerEndpoint();
        }
        RestClient graphRestClient = restClient.newBuilder()
                .withBaseUrl(graphEndpoint)
                .withScope(AzureEnvironment.AZURE.getGraphEndpoint() + "/.default")
                .buildClient();
        this.graphRbacManagementClient = new GraphRbacManagementClientBuilder()
                .pipeline(graphRestClient.getHttpPipeline())
                .host(graphEndpoint)
                .tenantID(tenantId)
                .build();
        this.authorizationManagementClient = new AuthorizationManagementClientBuilder()
                .pipeline(restClient.getHttpPipeline())
                .host(resourceManagerEndpoint)
                .subscriptionId(Utils.getSubscriptionIdFromRestClient(restClient))
                .build();
        this.tenantId = tenantId;
        this.sdkContext = sdkContext;
    }

    /**
     * @return wrapped inner authorization client providing direct access to
     * auto-generated API implementation, based on Azure REST API
     */
    public AuthorizationManagementClientImpl roleInner() {
        return authorizationManagementClient;
    }

    /**
     * @return the tenant ID the graph client is associated with
     */
    public String tenantId() {
        return tenantId;
    }

    /**
     * @return the sdk context in graph manager
     */
    public SdkContext sdkContext() {
        return sdkContext;
    }

    /**
     * @return the Active Directory user management API entry point
     */
    public ActiveDirectoryUsers users() {
        if (activeDirectoryUsers == null) {
            activeDirectoryUsers = new ActiveDirectoryUsersImpl(this);
        }
        return activeDirectoryUsers;
    }

    /**
     * @return the Active Directory group management API entry point
     */
    public ActiveDirectoryGroups groups() {
        if (activeDirectoryGroups == null) {
            activeDirectoryGroups = new ActiveDirectoryGroupsImpl(this);
        }
        return activeDirectoryGroups;
    }

    /**
     * @return the service principal management API entry point
     */
    public ServicePrincipals servicePrincipals() {
        if (servicePrincipals == null) {
            servicePrincipals = new ServicePrincipalsImpl(graphRbacManagementClient.servicePrincipals(), this);
        }
        return servicePrincipals;
    }

    /**
     * @return the application management API entry point
     */
    public ActiveDirectoryApplications applications() {
        if (applications == null) {
            applications = new ActiveDirectoryApplicationsImpl(graphRbacManagementClient.applications(), this);
        }
        return applications;
    }

    /**
     * @return the role assignment management API entry point
     */
    public RoleAssignments roleAssignments() {
        if (roleAssignments == null) {
            roleAssignments = new RoleAssignmentsImpl(this);
        }
        return roleAssignments;
    }

    /**
     * @return the role definition management API entry point
     */
    public RoleDefinitions roleDefinitions() {
        if (roleDefinitions == null) {
            roleDefinitions = new RoleDefinitionsImpl(this);
        }
        return roleDefinitions;
    }
}
