/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation.api;

import com.microsoft.azure.AzureClient;
import com.microsoft.azure.AzureServiceClient;
import com.microsoft.azure.serializer.AzureJacksonMapperAdapter;
import com.microsoft.rest.credentials.ServiceClientCredentials;
import com.microsoft.rest.RestClient;

/**
 * Initializes a new instance of the ResourceManagementClientImpl class.
 */
public final class ResourceManagementClientImpl extends AzureServiceClient {
    /** the {@link AzureClient} used for long running operations. */
    private AzureClient azureClient;

    /**
     * Gets the {@link AzureClient} used for long running operations.
     * @return the azure client;
     */
    public AzureClient getAzureClient() {
        return this.azureClient;
    }

    /** Gets subscription credentials which uniquely identify Microsoft Azure subscription. The subscription ID forms part of the URI for every service call. */
    private String subscriptionId;

    /**
     * Gets Gets subscription credentials which uniquely identify Microsoft Azure subscription. The subscription ID forms part of the URI for every service call.
     *
     * @return the subscriptionId value.
     */
    public String subscriptionId() {
        return this.subscriptionId;
    }

    /**
     * Sets Gets subscription credentials which uniquely identify Microsoft Azure subscription. The subscription ID forms part of the URI for every service call.
     *
     * @param subscriptionId the subscriptionId value.
     */
    public void withSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    /** Client Api Version. */
    private String apiVersion;

    /**
     * Gets Client Api Version.
     *
     * @return the apiVersion value.
     */
    public String apiVersion() {
        return this.apiVersion;
    }

    /** Gets or sets the preferred language for the response. */
    private String acceptLanguage;

    /**
     * Gets Gets or sets the preferred language for the response.
     *
     * @return the acceptLanguage value.
     */
    public String acceptLanguage() {
        return this.acceptLanguage;
    }

    /**
     * Sets Gets or sets the preferred language for the response.
     *
     * @param acceptLanguage the acceptLanguage value.
     */
    public void withAcceptLanguage(String acceptLanguage) {
        this.acceptLanguage = acceptLanguage;
    }

    /** Gets or sets the retry timeout in seconds for Long Running Operations. Default value is 30. */
    private int longRunningOperationRetryTimeout;

    /**
     * Gets Gets or sets the retry timeout in seconds for Long Running Operations. Default value is 30.
     *
     * @return the longRunningOperationRetryTimeout value.
     */
    public int longRunningOperationRetryTimeout() {
        return this.longRunningOperationRetryTimeout;
    }

    /**
     * Sets Gets or sets the retry timeout in seconds for Long Running Operations. Default value is 30.
     *
     * @param longRunningOperationRetryTimeout the longRunningOperationRetryTimeout value.
     */
    public void withLongRunningOperationRetryTimeout(int longRunningOperationRetryTimeout) {
        this.longRunningOperationRetryTimeout = longRunningOperationRetryTimeout;
    }

    /** When set to true a unique x-ms-client-request-id value is generated and included in each request. Default is true. */
    private boolean generateClientRequestId;

    /**
     * Gets When set to true a unique x-ms-client-request-id value is generated and included in each request. Default is true.
     *
     * @return the generateClientRequestId value.
     */
    public boolean generateClientRequestId() {
        return this.generateClientRequestId;
    }

    /**
     * Sets When set to true a unique x-ms-client-request-id value is generated and included in each request. Default is true.
     *
     * @param generateClientRequestId the generateClientRequestId value.
     */
    public void withGenerateClientRequestId(boolean generateClientRequestId) {
        this.generateClientRequestId = generateClientRequestId;
    }

    /**
     * The DeploymentsInner object to access its operations.
     */
    private DeploymentsInner deployments;

    /**
     * Gets the DeploymentsInner object to access its operations.
     * @return the DeploymentsInner object.
     */
    public DeploymentsInner deployments() {
        return this.deployments;
    }

    /**
     * The ProvidersInner object to access its operations.
     */
    private ProvidersInner providers;

    /**
     * Gets the ProvidersInner object to access its operations.
     * @return the ProvidersInner object.
     */
    public ProvidersInner providers() {
        return this.providers;
    }

    /**
     * The ResourceGroupsInner object to access its operations.
     */
    private ResourceGroupsInner resourceGroups;

    /**
     * Gets the ResourceGroupsInner object to access its operations.
     * @return the ResourceGroupsInner object.
     */
    public ResourceGroupsInner resourceGroups() {
        return this.resourceGroups;
    }

    /**
     * The ResourcesInner object to access its operations.
     */
    private ResourcesInner resources;

    /**
     * Gets the ResourcesInner object to access its operations.
     * @return the ResourcesInner object.
     */
    public ResourcesInner resources() {
        return this.resources;
    }

    /**
     * The TagsInner object to access its operations.
     */
    private TagsInner tags;

    /**
     * Gets the TagsInner object to access its operations.
     * @return the TagsInner object.
     */
    public TagsInner tags() {
        return this.tags;
    }

    /**
     * The DeploymentOperationsInner object to access its operations.
     */
    private DeploymentOperationsInner deploymentOperations;

    /**
     * Gets the DeploymentOperationsInner object to access its operations.
     * @return the DeploymentOperationsInner object.
     */
    public DeploymentOperationsInner deploymentOperations() {
        return this.deploymentOperations;
    }

    /**
     * The ResourceProviderOperationDetailsInner object to access its operations.
     */
    private ResourceProviderOperationDetailsInner resourceProviderOperationDetails;

    /**
     * Gets the ResourceProviderOperationDetailsInner object to access its operations.
     * @return the ResourceProviderOperationDetailsInner object.
     */
    public ResourceProviderOperationDetailsInner resourceProviderOperationDetails() {
        return this.resourceProviderOperationDetails;
    }

    /**
     * The PolicyDefinitionsInner object to access its operations.
     */
    private PolicyDefinitionsInner policyDefinitions;

    /**
     * Gets the PolicyDefinitionsInner object to access its operations.
     * @return the PolicyDefinitionsInner object.
     */
    public PolicyDefinitionsInner policyDefinitions() {
        return this.policyDefinitions;
    }

    /**
     * The PolicyAssignmentsInner object to access its operations.
     */
    private PolicyAssignmentsInner policyAssignments;

    /**
     * Gets the PolicyAssignmentsInner object to access its operations.
     * @return the PolicyAssignmentsInner object.
     */
    public PolicyAssignmentsInner policyAssignments() {
        return this.policyAssignments;
    }

    /**
     * Initializes an instance of ResourceManagementClient client.
     *
     * @param credentials the management credentials for Azure
     */
    public ResourceManagementClientImpl(ServiceClientCredentials credentials) {
        this("https://management.azure.com", credentials);
    }

    /**
     * Initializes an instance of ResourceManagementClient client.
     *
     * @param baseUrl the base URL of the host
     * @param credentials the management credentials for Azure
     */
    public ResourceManagementClientImpl(String baseUrl, ServiceClientCredentials credentials) {
        this(new RestClient.Builder(baseUrl)
                .withMapperAdapter(new AzureJacksonMapperAdapter())
                .withCredentials(credentials)
                .build());
    }

    /**
     * Initializes an instance of ResourceManagementClient client.
     *
     * @param restClient the REST client to connect to Azure.
     */
    public ResourceManagementClientImpl(RestClient restClient) {
        super(restClient);
        initialize();
    }

    protected void initialize() {
        this.apiVersion = "2015-11-01";
        this.acceptLanguage = "en-US";
        this.longRunningOperationRetryTimeout = 30;
        this.generateClientRequestId = true;
        this.deployments = new DeploymentsInner(restClient().retrofit(), this);
        this.providers = new ProvidersInner(restClient().retrofit(), this);
        this.resourceGroups = new ResourceGroupsInner(restClient().retrofit(), this);
        this.resources = new ResourcesInner(restClient().retrofit(), this);
        this.tags = new TagsInner(restClient().retrofit(), this);
        this.deploymentOperations = new DeploymentOperationsInner(restClient().retrofit(), this);
        this.resourceProviderOperationDetails = new ResourceProviderOperationDetailsInner(restClient().retrofit(), this);
        this.policyDefinitions = new PolicyDefinitionsInner(restClient().retrofit(), this);
        this.policyAssignments = new PolicyAssignmentsInner(restClient().retrofit(), this);
        this.azureClient = new AzureClient(this);
    }

    /**
     * Gets the User-Agent header for the client.
     *
     * @return the user agent string.
     */
    @Override
    public String userAgent() {
        return String.format("Azure-SDK-For-Java/%s (%s)",
                getClass().getPackage().getImplementationVersion(),
                "ResourceManagementClient, 2015-11-01");
    }
}
