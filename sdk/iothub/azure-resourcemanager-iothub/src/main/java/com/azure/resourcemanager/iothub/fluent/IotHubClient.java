// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.iothub.fluent;

import com.azure.core.http.HttpPipeline;
import java.time.Duration;

/**
 * The interface for IotHubClient class.
 */
public interface IotHubClient {
    /**
     * Gets The subscription identifier.
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
     * Gets the OperationsClient object to access its operations.
     * 
     * @return the OperationsClient object.
     */
    OperationsClient getOperations();

    /**
     * Gets the IotHubResourcesClient object to access its operations.
     * 
     * @return the IotHubResourcesClient object.
     */
    IotHubResourcesClient getIotHubResources();

    /**
     * Gets the ResourceProviderCommonsClient object to access its operations.
     * 
     * @return the ResourceProviderCommonsClient object.
     */
    ResourceProviderCommonsClient getResourceProviderCommons();

    /**
     * Gets the CertificatesClient object to access its operations.
     * 
     * @return the CertificatesClient object.
     */
    CertificatesClient getCertificates();

    /**
     * Gets the IotHubsClient object to access its operations.
     * 
     * @return the IotHubsClient object.
     */
    IotHubsClient getIotHubs();

    /**
     * Gets the PrivateLinkResourcesOperationsClient object to access its operations.
     * 
     * @return the PrivateLinkResourcesOperationsClient object.
     */
    PrivateLinkResourcesOperationsClient getPrivateLinkResourcesOperations();

    /**
     * Gets the PrivateEndpointConnectionsClient object to access its operations.
     * 
     * @return the PrivateEndpointConnectionsClient object.
     */
    PrivateEndpointConnectionsClient getPrivateEndpointConnections();
}
