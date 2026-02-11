// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Deprecated generated code

package com.azure.resourcemanager.appservice.fluent;

import com.azure.core.http.HttpPipeline;
import java.time.Duration;

/**
 * The interface for DomainRegistrationManagementClient class.
 */
public interface DomainRegistrationManagementClient {
    /**
     * Gets The ID of the target subscription.
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
     * Gets the DomainRegistrationProvidersClient object to access its operations.
     * 
     * @return the DomainRegistrationProvidersClient object.
     */
    DomainRegistrationProvidersClient getDomainRegistrationProviders();

    /**
     * Gets the DomainsClient object to access its operations.
     * 
     * @return the DomainsClient object.
     */
    DomainsClient getDomains();

    /**
     * Gets the TopLevelDomainsClient object to access its operations.
     * 
     * @return the TopLevelDomainsClient object.
     */
    TopLevelDomainsClient getTopLevelDomains();
}
