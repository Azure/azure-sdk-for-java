// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.containerinstance.fluent;

import com.azure.core.http.HttpPipeline;
import java.time.Duration;

/**
 * The interface for ContainerInstanceManagementClient class.
 */
public interface ContainerInstanceManagementClient {
    /**
     * Gets Subscription credentials which uniquely identify Microsoft Azure subscription. The subscription ID forms
     * part of the URI for every service call.
     * 
     * @return the subscriptionId value.
     */
    String getSubscriptionId();

    /**
     * Gets server parameter.
     * 
     * @return the endpoint value.
     */
    String getEndpoint();

    /**
     * Gets Api Version.
     * 
     * @return the apiVersion value.
     */
    String getApiVersion();

    /**
     * Gets The HTTP pipeline to send requests through.
     * 
     * @return the httpPipeline value.
     */
    HttpPipeline getHttpPipeline();

    /**
     * Gets The default poll interval for long-running operation.
     * 
     * @return the defaultPollInterval value.
     */
    Duration getDefaultPollInterval();

    /**
     * Gets the ContainerGroupsClient object to access its operations.
     * 
     * @return the ContainerGroupsClient object.
     */
    ContainerGroupsClient getContainerGroups();

    /**
     * Gets the OperationsClient object to access its operations.
     * 
     * @return the OperationsClient object.
     */
    OperationsClient getOperations();

    /**
     * Gets the LocationsClient object to access its operations.
     * 
     * @return the LocationsClient object.
     */
    LocationsClient getLocations();

    /**
     * Gets the ContainersClient object to access its operations.
     * 
     * @return the ContainersClient object.
     */
    ContainersClient getContainers();

    /**
     * Gets the SubnetServiceAssociationLinksClient object to access its operations.
     * 
     * @return the SubnetServiceAssociationLinksClient object.
     */
    SubnetServiceAssociationLinksClient getSubnetServiceAssociationLinks();
}
