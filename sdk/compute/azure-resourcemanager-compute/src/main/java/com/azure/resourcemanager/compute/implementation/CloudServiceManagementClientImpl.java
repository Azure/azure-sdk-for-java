// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Deprecated generated code

package com.azure.resourcemanager.compute.implementation;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.resourcemanager.compute.fluent.CloudServiceManagementClient;
import com.azure.resourcemanager.compute.fluent.CloudServiceOperatingSystemsClient;
import com.azure.resourcemanager.compute.fluent.CloudServiceRoleInstancesClient;
import com.azure.resourcemanager.compute.fluent.CloudServiceRolesClient;
import com.azure.resourcemanager.compute.fluent.CloudServicesClient;
import com.azure.resourcemanager.compute.fluent.CloudServicesUpdateDomainsClient;
import com.azure.resourcemanager.resources.fluentcore.AzureServiceClient;
import java.time.Duration;

/**
 * Initializes a new instance of the CloudServiceManagementClientImpl type.
 */
@ServiceClient(builder = CloudServiceManagementClientBuilder.class)
public final class CloudServiceManagementClientImpl extends AzureServiceClient implements CloudServiceManagementClient {
    /**
     * Subscription credentials which uniquely identify Microsoft Azure subscription. The subscription ID forms part of
     * the URI for every service call.
     */
    private final String subscriptionId;

    /**
     * Gets Subscription credentials which uniquely identify Microsoft Azure subscription. The subscription ID forms
     * part of the URI for every service call.
     * 
     * @return the subscriptionId value.
     */
    public String getSubscriptionId() {
        return this.subscriptionId;
    }

    /**
     * server parameter.
     */
    private final String endpoint;

    /**
     * Gets server parameter.
     * 
     * @return the endpoint value.
     */
    public String getEndpoint() {
        return this.endpoint;
    }

    /**
     * Api Version.
     */
    private final String apiVersion;

    /**
     * Gets Api Version.
     * 
     * @return the apiVersion value.
     */
    public String getApiVersion() {
        return this.apiVersion;
    }

    /**
     * The HTTP pipeline to send requests through.
     */
    private final HttpPipeline httpPipeline;

    /**
     * Gets The HTTP pipeline to send requests through.
     * 
     * @return the httpPipeline value.
     */
    public HttpPipeline getHttpPipeline() {
        return this.httpPipeline;
    }

    /**
     * The serializer to serialize an object into a string.
     */
    private final SerializerAdapter serializerAdapter;

    /**
     * Gets The serializer to serialize an object into a string.
     * 
     * @return the serializerAdapter value.
     */
    SerializerAdapter getSerializerAdapter() {
        return this.serializerAdapter;
    }

    /**
     * The default poll interval for long-running operation.
     */
    private final Duration defaultPollInterval;

    /**
     * Gets The default poll interval for long-running operation.
     * 
     * @return the defaultPollInterval value.
     */
    public Duration getDefaultPollInterval() {
        return this.defaultPollInterval;
    }

    /**
     * The CloudServiceRoleInstancesClient object to access its operations.
     */
    private final CloudServiceRoleInstancesClient cloudServiceRoleInstances;

    /**
     * Gets the CloudServiceRoleInstancesClient object to access its operations.
     * 
     * @return the CloudServiceRoleInstancesClient object.
     */
    public CloudServiceRoleInstancesClient getCloudServiceRoleInstances() {
        return this.cloudServiceRoleInstances;
    }

    /**
     * The CloudServiceRolesClient object to access its operations.
     */
    private final CloudServiceRolesClient cloudServiceRoles;

    /**
     * Gets the CloudServiceRolesClient object to access its operations.
     * 
     * @return the CloudServiceRolesClient object.
     */
    public CloudServiceRolesClient getCloudServiceRoles() {
        return this.cloudServiceRoles;
    }

    /**
     * The CloudServicesClient object to access its operations.
     */
    private final CloudServicesClient cloudServices;

    /**
     * Gets the CloudServicesClient object to access its operations.
     * 
     * @return the CloudServicesClient object.
     */
    public CloudServicesClient getCloudServices() {
        return this.cloudServices;
    }

    /**
     * The CloudServicesUpdateDomainsClient object to access its operations.
     */
    private final CloudServicesUpdateDomainsClient cloudServicesUpdateDomains;

    /**
     * Gets the CloudServicesUpdateDomainsClient object to access its operations.
     * 
     * @return the CloudServicesUpdateDomainsClient object.
     */
    public CloudServicesUpdateDomainsClient getCloudServicesUpdateDomains() {
        return this.cloudServicesUpdateDomains;
    }

    /**
     * The CloudServiceOperatingSystemsClient object to access its operations.
     */
    private final CloudServiceOperatingSystemsClient cloudServiceOperatingSystems;

    /**
     * Gets the CloudServiceOperatingSystemsClient object to access its operations.
     * 
     * @return the CloudServiceOperatingSystemsClient object.
     */
    public CloudServiceOperatingSystemsClient getCloudServiceOperatingSystems() {
        return this.cloudServiceOperatingSystems;
    }

    /**
     * Initializes an instance of CloudServiceManagementClient client.
     * 
     * @param httpPipeline The HTTP pipeline to send requests through.
     * @param serializerAdapter The serializer to serialize an object into a string.
     * @param defaultPollInterval The default poll interval for long-running operation.
     * @param environment The Azure environment.
     * @param subscriptionId Subscription credentials which uniquely identify Microsoft Azure subscription. The
     * subscription ID forms part of the URI for every service call.
     * @param endpoint server parameter.
     */
    CloudServiceManagementClientImpl(HttpPipeline httpPipeline, SerializerAdapter serializerAdapter,
        Duration defaultPollInterval, AzureEnvironment environment, String subscriptionId, String endpoint) {
        super(httpPipeline, serializerAdapter, environment);
        this.httpPipeline = httpPipeline;
        this.serializerAdapter = serializerAdapter;
        this.defaultPollInterval = defaultPollInterval;
        this.subscriptionId = subscriptionId;
        this.endpoint = endpoint;
        this.apiVersion = "2024-11-04";
        this.cloudServiceRoleInstances = new CloudServiceRoleInstancesClientImpl(this);
        this.cloudServiceRoles = new CloudServiceRolesClientImpl(this);
        this.cloudServices = new CloudServicesClientImpl(this);
        this.cloudServicesUpdateDomains = new CloudServicesUpdateDomainsClientImpl(this);
        this.cloudServiceOperatingSystems = new CloudServiceOperatingSystemsClientImpl(this);
    }
}
